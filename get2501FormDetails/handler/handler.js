const { fpacPrismaClient } = require('/opt/prisma');
const { LambdaHandlerResponse, Logger, LambdaError, getParameterValue } = require('/opt/utils');
const axios = require('axios');
const logger = new Logger();

// Warm Lambda Caching of Prisma Client
const cache = {
   flpCommonApiUrl: null,
   prisma: null,
};
 
const resetCache = () => {
   cache.flpCommonApiUrl = null;
   cache.prisma = null;
};

const handler = async (event, context) => {
  const lambdaHandlerResponse = new LambdaHandlerResponse();
  logger.debug('context: ' + JSON.stringify(context, null, 2));
  logger.debug('event: ' + JSON.stringify(event, null, 2));

  try {
    // Extract query parameters
    const queryParams = event.queryStringParameters || {};
    const rqstIdParam = queryParams.rqstId;
    const fundCode = queryParams.fundCode;
    const loanNumber = queryParams.loanNumber;
    const loanIdParam = queryParams.loanId;
    const headers = event.headers;
    const corsOrigin = headers?.Origin || headers?.origin || headers?.ORIGIN;
    const authToken = headers?.Authorization || headers?.authorization;
   
    logger.info('rqstIdParam: ' + rqstIdParam);
    logger.info('fundCode: ' + fundCode + ', loanNumber: ' + loanNumber + ', loanId: ' + loanIdParam);
   
    // Parse and validate required request ID
    const rqstId = parseAndValidateRqstId(rqstIdParam);
    logger.info('parsed rqst_id: ' + rqstId);
   
    // Validate optional filter parameters
    const filterCriteria = validateFilterParameters(fundCode, loanNumber, loanIdParam);
   
    // Initialize Prisma client if not already done
    if (!cache.prisma) {
      cache.prisma = await fpacPrismaClient();
      if (!cache.prisma) {
        throw new LambdaError('Failed to instantiate fpacPrismaClient', 400);
      }
    }
   
    // Fetch request documents using the single request ID and filter criteria
    const requestDocuments = await getRequestDocuments(rqstId, filterCriteria);
   
    // Extract doc_ids from the request documents
    const docIds = requestDocuments.map(doc => doc.doc_id).filter(id => id !== null && id !== undefined);
    logger.info('extracted doc_ids: ' + JSON.stringify(docIds));
   
    let documentDetails = [];

    const apiHeaders = {
      'Authorization': authToken,
      'Content-Type': 'application/json'
    };

    // Add Origin header if present
    if (corsOrigin) {
      apiHeaders['Origin'] = corsOrigin;
    }
   
    // If we have doc_ids, fetch document details from external API
    if (docIds.length > 0) {
      documentDetails = await fetchDocumentDetails(docIds, apiHeaders);
     
      // Apply document name filtering only if fund code and loan number are provided
      if (filterCriteria.shouldFilterByDocumentName) {

        documentDetails = filterDocumentsByFundCodeLoanNumber(documentDetails, filterCriteria);
        logger.info('filtered documentDetails count: ' + documentDetails.length);
      }
    }
   
    // Combine request documents with document details
    const result = combineDocumentData(requestDocuments, documentDetails, filterCriteria);
   
    const body = {
      message: 'Success',
      data: result,
      count: result.length
    };
   
    logger.info('Result: ' + JSON.stringify(result, null, 2));
    lambdaHandlerResponse.body = body;
   
  } catch (error) {
    logger.error('Error occurred:',error);
    resetCache();
    lambdaHandlerResponse.errors = new Array(error.message);
    lambdaHandlerResponse.setError(error.statusCode || 500);

  }

  return lambdaHandlerResponse.toAPIGatewayResponse();
};

/**
* Parse and validate required request ID
* @param {string} rqstIdParam - Request ID string from query parameter
* @returns {number} Valid integer request ID
*/
function parseAndValidateRqstId(rqstIdParam) {
  if (!rqstIdParam || rqstIdParam.trim().length === 0) {
    throw new LambdaError('rqstId query parameter is required', 400);
  }

  const trimmedId = rqstIdParam.trim();
  const parsed = parseInt(trimmedId, 10);

  if (isNaN(parsed) || parsed <= 0) {
    throw new LambdaError(`Invalid rqstId: ${trimmedId}. Request ID must be a positive integer.`, 400);
  }

  return parsed;
}

/**
* Validate optional filter parameters
* @param {string} fundCode - 2-digit fund code
* @param {string} loanNumber - 2-digit loan number
* @param {string} loanIdParam - Loan ID
* @returns {Object} Filter criteria object
*/
function validateFilterParameters(fundCode, loanNumber, loanIdParam) {
  const result = {
    shouldFilterByLoanId: false,
    shouldFilterByDocumentName: false,
    fundCode: null,
    loanNumber: null,
    loanId: null,
    filterPattern: null
  };

  const hasFundCodeLoanNumber = fundCode || loanNumber;
  const hasLoanId = loanIdParam;

  // Check for conflicting parameters
  if (hasFundCodeLoanNumber && hasLoanId) {
    throw new LambdaError('Cannot provide both fundCode/loanNumber and loanId parameters. Use either fundCode+loanNumber or loanId, not both.', 400);
  }

  // Validate fundCode and loanNumber combination
  if (hasFundCodeLoanNumber) {
    if (!fundCode || !loanNumber) {
      throw new LambdaError('Both fundCode and loanNumber must be provided together', 400);
    }
   
    // Validate fund code (2 digits)
    const fundCodeTrimmed = fundCode.trim();
    if (!/^\d{2}$/.test(fundCodeTrimmed)) {
      throw new LambdaError('Fund code must be exactly 2 digits', 400);
    }
   
    // Validate loan number (2 digits)
    const loanNumberTrimmed = loanNumber.trim();
    if (!/^\d{2}$/.test(loanNumberTrimmed)) {
      throw new LambdaError('Loan number must be exactly 2 digits', 400);
    }
   
    result.shouldFilterByDocumentName = true;
    result.fundCode = fundCodeTrimmed;
    result.loanNumber = loanNumberTrimmed;
    result.filterPattern = `${fundCodeTrimmed}-${loanNumberTrimmed}`;
  }

  // Validate loanId
  if (hasLoanId) {
    const loanIdTrimmed = loanIdParam.trim();
    const parsedLoanId = parseInt(loanIdTrimmed, 10);
   
    if (isNaN(parsedLoanId) || parsedLoanId <= 0) {
      throw new LambdaError(`Invalid loanId: ${loanIdTrimmed}. Loan ID must be a positive integer.`, 400);
    }
   
    result.shouldFilterByLoanId = true;
    result.loanId = parsedLoanId;
  }

  return result;
}

/**
* Get request documents by single request ID with optional filtering
* @param {number} rqstId - Single request ID
* @param {Object} filterCriteria - Filter criteria object
* @returns {Object[]} Array of request document objects
*/
async function getRequestDocuments(rqstId, filterCriteria) {
  const whereClause = {
    rqst_id: rqstId
  };

  // Add loan_id filter if specified
  if (filterCriteria.shouldFilterByLoanId) {
    whereClause.loan_id = filterCriteria.loanId;
    logger.info(`Filtering by loan_id: ${filterCriteria.loanId}`);
  }

  const args = {
    where: whereClause,

    select: {
      rqst_doc_id: true,
      rqst_id: true,
      doc_type_cd: true,
      doc_id: true,
      doc_type_ot_desc: true,
      data_stat_cd: true,
      cre_dt: true,
      cre_user_nm: true,
      last_chg_dt: true,
      last_chg_user_nm: true,
      loan_id: true
    },
    orderBy: {
      rqst_doc_id: 'asc'
    }
  };

  const result = await cache.prisma.rqst_doc.findMany(args);
  return result;
}

/**
* Fetch document details from external API
* @param {number[]} docIds - Array of document IDs
* @returns {Object[]} Array of document detail objects
*/
async function fetchDocumentDetails(docIds, apiHeaders) {
  try {
    // Remove duplicates and filter out invalid IDs
    const uniqueDocIds = [...new Set(docIds)].filter(id => id > 0);
   
    if (uniqueDocIds.length === 0) {
      logger.warn('No valid document IDs to fetch');
      return [];
    }
   
    if(!cache.flpCommonApiUrl)
    {
      const flpCommonApiURLAWSParamName = process.env.PNAME_PREFIX + 'global/flp-common-api-Url';
      cache.flpCommonApiUrl = await getParameterValue(flpCommonApiURLAWSParamName);
      logger.info('flpCommonApiUrl:',cache.flpCommonApiUrl);
    }

    // Create comma-separated string of doc IDs
    const docIdsString = uniqueDocIds.join(',');
    const apiUrl =  cache.flpCommonApiUrl+`document/${docIdsString}`;
   
    logger.info('Calling external API: ' + apiUrl);

    const response = await axios.get(apiUrl, {
      headers: apiHeaders
    });
   
    if (response.status === 200 && response.data && response.data.data) {
      logger.info('Successfully fetched document details, count: ' + response.data.data.length);
      return response.data.data;
    } else {
      logger.warn('Unexpected response from document API: ' + JSON.stringify(response.data));
      return [];
    }
   
  } catch (error) {
    logger.error('Error fetching document details from external API:', error);
   
    // Don't throw error - return empty array to allow partial success
    // This way the request documents are still returned even if external API fails
    if (error.response) {
      logger.error('API Response Error - Status: ' + error.response.status + ', Data: ' + JSON.stringify(error.response.data));
    } else if (error.request) {
      logger.error('API Request Error - No response received');
    } else {
      logger.error('API Setup Error: ' + error.message);
    }
   
    return [];
  }
}

/**
* Filter document details by fund code and loan number
* @param {Object[]} documentDetails - Array of document detail objects
* @param {Object} filterCriteria - Filter criteria object
* @returns {Object[]} Filtered array of document detail objects
*/
function filterDocumentsByFundCodeLoanNumber(documentDetails, filterCriteria) {
  if (!filterCriteria.shouldFilterByDocumentName || !documentDetails || documentDetails.length === 0) {


    return documentDetails;
  }

  const { filterPattern } = filterCriteria;
  logger.info(`Filtering documents by pattern: ${filterPattern}`);

  return documentDetails.filter(doc => {
    if (!doc.documentName) {
      logger.debug(`Document ${doc.documentId} has no documentName, excluding from results`);
      return false;
    }
   
    // Extract the fund code and loan number from document name
    // Expected format: FSA-2501 [name] [fundcode-loannumber]
    // Examples: "FSA-2501 Smith 44-01", "FSA-2501 Reed LLC 44-01"
   
    const documentName = doc.documentName.trim();
   
    // Find the last space in the document name
    const lastSpaceIndex = documentName.lastIndexOf(' ');
   
    if (lastSpaceIndex === -1) {
      logger.debug(`Document ${doc.documentId} name "${documentName}" does not contain expected format, excluding`);
      return false;
    }
   
    // Extract the last part which should be the fund code and loan number
    const lastPart = documentName.substring(lastSpaceIndex + 1);
   
    // Check if the last part matches the expected pattern (XX-XX)
    if (!/^\d{2}-\d{2}$/.test(lastPart)) {
      logger.debug(`Document ${doc.documentId} name "${documentName}" last part "${lastPart}" does not match XX-XX pattern, excluding`);
      return false;
    }
   
    // Check if it matches our filter pattern
    const matches = lastPart === filterPattern;
   
    if (matches) {
      logger.debug(`Document ${doc.documentId} name "${documentName}" matches filter pattern ${filterPattern}, including`);
    } else {
      logger.debug(`Document ${doc.documentId} name "${documentName}" with pattern "${lastPart}" does not match filter ${filterPattern}, excluding`);
    }
   
    return matches;
  });
}

/**
* Combine request documents with document details
* @param {Object[]} requestDocuments - Array of request document objects
* @param {Object[]} documentDetails - Array of document detail objects
* @returns {Object[]} Array of combined document objects
*/
function combineDocumentData(requestDocuments, documentDetails, filterCriteria) {
  // Create a map of document details by documentId for efficient lookup
  const documentDetailsMap = new Map();
  documentDetails.forEach(doc => {
    if (doc.documentId) {
      documentDetailsMap.set(doc.documentId, doc);
    }
  });

  // Combine request documents with their corresponding document details
  const combinedData = requestDocuments.map(rqstDoc => {
    const docDetails = documentDetailsMap.get(rqstDoc.doc_id);
   
    return {
      // Request document fields
      rqst_doc_id: rqstDoc.rqst_doc_id,
      rqst_id: rqstDoc.rqst_id,
      doc_type_cd: rqstDoc.doc_type_cd,
      doc_id: rqstDoc.doc_id,
      doc_type_ot_desc: rqstDoc.doc_type_ot_desc,
      data_stat_cd: rqstDoc.data_stat_cd,
      cre_dt: rqstDoc.cre_dt,
      cre_user_nm: rqstDoc.cre_user_nm,
      last_chg_dt: rqstDoc.last_chg_dt,
      last_chg_user_nm: rqstDoc.last_chg_user_nm,
      loan_id: rqstDoc.loan_id,
     
      // Document details from external API (if available)
      documentDetails: docDetails || null
    };
  });

  // If filtering by fund code and loan number, only return records with matching document details
  if (filterCriteria.shouldFilterByDocumentName) {
    logger.info('Filtering combined data to only include records with matching document details');
    return combinedData.filter(item => item.documentDetails !== null);
  }

  // For loanId filtering or no filtering, return all combined data
  return combinedData;

}

module.exports = { handler, resetCache };
