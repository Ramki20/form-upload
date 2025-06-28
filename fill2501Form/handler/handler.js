const { LambdaHandlerResponse, getParameterValue, Logger, flpHttpsRequest } = require('/opt/utils');
const { formatAmount, formatDate } = require('/opt/utils');
const { PDFDocument, PDFForm } = require('pdf-lib');
const axios = require('axios');
const { S3Client, PutObjectCommand } = require('@aws-sdk/client-s3');
const archiver = require('archiver');

const cache = {
   flpCommonApiUrl: null,
   form2501FileId: null,
};
 
const resetCache = () => {
   cache.flpCommonApiUrl = null;
   cache.form2501FileId = null;
};

const logger = new Logger();
const s3Client = new S3Client();

async function handler (event) {

  const response = new LambdaHandlerResponse();

  try {

    logger.debug('event:',event);

    // Extract headers and request data
    const request = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    const headers = event.headers;
    const corsOrigin = headers?.Origin || headers?.origin || headers?.ORIGIN;
    const authToken = headers?.Authorization || headers?.authorization;
    logger.debug('request:::',request);
  
    const apiHeaders = {
        'Content-Type': 'application/json',
        'Authorization': authToken
    };
  
    if (corsOrigin) {
        apiHeaders['Origin'] = corsOrigin;
    }
  
    if(!cache.flpCommonApiUrl)
    {
      const flpCommonApiURLAWSParamName = process.env.PNAME_PREFIX + 'global/flp-common-api-Url';
      cache.flpCommonApiUrl = await getParameterValue(flpCommonApiURLAWSParamName);
      logger.info('flpCommonApiUrl:',cache.flpCommonApiUrl);
    }
    const relatedEntityUrl = cache.flpCommonApiUrl+`related-entity-info-by-loan?loanId=${request.loan_id}&eauthId=${request.eauth_id}`;
  
    const relatedEntity = await axios.get(relatedEntityUrl, {
      headers: apiHeaders
    });
  
    logger.debug('relatedEntity:',relatedEntity.data);
  
    // Create borrower groups for multiple forms if needed
    const borrowerGroups = createBorrowerGroups(relatedEntity.data);
   
    if(!cache.form2501FileId)
    {
      const form2501FileIdAWSParamName = process.env.PNAME_API_PREFIX + 'FSA_2501_FILE_ID';
      cache.form2501FileId = await getParameterValue(form2501FileIdAWSParamName);
      logger.info('form2501FileId:',cache.form2501FileId);
    }
  
    const usdaForms2501Url = cache.flpCommonApiUrl+`usda-forms/${cache.form2501FileId}`;
  
    // Get the PDF template
    const result = await axios.get(usdaForms2501Url, {
      headers: {'Accept': 'application/pdf'},
      responseType: 'arraybuffer'
    });
   
    logger.debug('PDF template loaded, status:', result.status);
   
    const generatedDocuments = [];
    const pdfFilesForZip = []; // Store PDF files for ZIP creation
   
    // Process each borrower group (create separate forms)
    for (let i = 0; i < borrowerGroups.length; i++) {
      const borrowerGroup = borrowerGroups[i];
      logger.debug(`\n=== Processing Form ${i + 1} of ${borrowerGroups.length} ===`);
     
      // Create form data for this group
      const formData = createFormDataForGroup(request, borrowerGroup);
     
      // Load a fresh copy of the PDF for each form
      const pdfDoc = await PDFDocument.load(result.data);
      logger.debug(`PDF document loaded for form ${i + 1}`);
  
      // Get the form from the PDF
      const form = pdfDoc.getForm();
  
      // Fill form fields
      await fillFormFields(form, formData);
  
      // Serialize the PDF
      const filledPDFBytes = await pdfDoc.save();
  
      // Generate individual document name (for ZIP contents or single file)
      const individualDocumentName = borrowerGroups.length === 1
        ? `FSA-2501 ${formData.name} ${formData.fundCode}-${formData.loanNumber}.pdf`
        : `FSA-2501 ${formData.name} ${formData.fundCode}-${formData.loanNumber} (Form ${i + 1} of ${borrowerGroups.length}).pdf`;
     
      logger.info('Individual PDF name: ', individualDocumentName);
  
      // Store PDF data for potential ZIP creation
      pdfFilesForZip.push({
        name: individualDocumentName,
        buffer: Buffer.from(filledPDFBytes)
      });
     
      generatedDocuments.push({
        documentName: individualDocumentName,
        formNumber: i + 1,
        totalForms: borrowerGroups.length,
        borrowerCount: borrowerGroup.isFirstForm ? 1 + borrowerGroup.coBorrowers.length : borrowerGroup.coBorrowers.length,
        isFirstForm: borrowerGroup.isFirstForm,
        isContinuation: borrowerGroup.isContinuation
      });
    }
  
    // Determine final upload: single PDF or ZIP file
    let finalDocumentName;
    let finalFileBuffer;
    let contentType;
   
    if (borrowerGroups.length === 1) {
      // Single form - upload as PDF
      finalDocumentName = pdfFilesForZip[0].name;
      finalFileBuffer = pdfFilesForZip[0].buffer;
      contentType = 'application/pdf';
      logger.debug('Single form - uploading as PDF');
    } else {
      // Multiple forms - create ZIP and upload
      logger.debug(`Multiple forms (${borrowerGroups.length}) - creating ZIP file`);
     
      const primaryBorrowerName = relatedEntity.data.primary?.name || 'Unknown';
      finalDocumentName = `FSA-2501 ${primaryBorrowerName} ${request.fundCode}-${request.loanNumber}.zip`;
     
      try {
        finalFileBuffer = await createZipFile(pdfFilesForZip);
        contentType = 'application/zip';
        logger.debug('ZIP file created successfully');
      } catch (zipError) {
        logger.error('Failed to create ZIP file:', zipError);
        // Fallback: upload the first form as PDF
        finalDocumentName = pdfFilesForZip[0].name;
        finalFileBuffer = pdfFilesForZip[0].buffer;
        contentType = 'application/pdf';
        logger.debug('ZIP creation failed - falling back to first PDF only');
      }
    }
  
    // Store final document in S3
    const s3Params = {
      Bucket: process.env.SCRATCHPAD_BUCKET_NAME,
      Key: `FSA-2501Form/${finalDocumentName}`,
      Body: finalFileBuffer,
      ContentType: contentType
    };
  
    logger.debug('Uploading to S3:', finalDocumentName);
  
    // Create a PutObjectCommand object
    const command = new PutObjectCommand(s3Params);
  
    // Send the command to S3
    const data = await s3Client.send(command);
   
    logger.debug(`Final document uploaded to S3 successfully: ${finalDocumentName}`);
  
    response.setHeader('Authorization',authToken);
    response.setHeader('Origin',corsOrigin);
   
    request.documentName = finalDocumentName;
    request.totalFormsGenerated = borrowerGroups.length;
    request.totalBorrowers = 1 + (relatedEntity.data.nonPrimaryList ? relatedEntity.data.nonPrimaryList.length : 0);
    request.fileType = contentType === 'application/zip' ? 'zip' : 'pdf';
    request.containsMultipleForms = borrowerGroups.length > 1;
   
    // Include individual form details for reference (optional)
    request.formsDetails = generatedDocuments;
 
    response.body = request;

  }
  catch (e) {
    logger.error('Error occurred:',e);
    resetCache();
    response.errors = new Array(e.message);
    response.setError(e.statusCode || 500);
  }      

  return response.toAPIGatewayResponse();
}


/**
 * Create borrower groups for multiple forms
 */
function createBorrowerGroups(relatedEntityData) {
  const groups = [];
  const primary = relatedEntityData.primary;
  const nonPrimaryList = relatedEntityData.nonPrimaryList || [];
 
  // Total borrowers = 1 primary + nonPrimaryList length
  const totalBorrowers = 1 + nonPrimaryList.length;
 
  logger.debug(`Total borrowers: ${totalBorrowers} (1 primary + ${nonPrimaryList.length} co-borrowers)`);
 
  if (totalBorrowers <= 4) {
    // Single form scenario
    const group = {
      primary: primary,
      coBorrowers: nonPrimaryList,
      isFirstForm: true
    };
    groups.push(group);
    logger.debug('Single form needed - all borrowers fit in one form');
  } else {
    // Multiple forms scenario
    // First form: primary + first 3 co-borrowers (use all 4 borrower fields: 6A, 7A, 8A, 9A)
    const firstGroup = {
      primary: primary,
      coBorrowers: nonPrimaryList.slice(0, 3),
      isFirstForm: true,
      isContinuation: false
    };
    groups.push(firstGroup);
    logger.debug('First form: primary + first 3 co-borrowers');
   
    // Continuation forms: 4 co-borrowers each (using fields 6A, 7A, 8A, 9A)
    let remainingCoBorrowers = nonPrimaryList.slice(3);
    let formNumber = 2;
   
    while (remainingCoBorrowers.length > 0) {
      const coBorrowersForThisForm = remainingCoBorrowers.slice(0, 4);
      const continuationGroup = {
        primary: primary, // Same primary info on all forms
        coBorrowers: coBorrowersForThisForm,
        isFirstForm: false,
        isContinuation: true,
        formNumber: formNumber
      };
      groups.push(continuationGroup);
      logger.debug(`Continuation form ${formNumber}: ${coBorrowersForThisForm.length} co-borrowers`);
     
      remainingCoBorrowers = remainingCoBorrowers.slice(4);
      formNumber++;
    }
  }
 
  logger.debug(`Total forms to generate: ${groups.length}`);
  return groups;
}

/**
 * Create form data for a specific borrower group
 */
function createFormDataForGroup(request, borrowerGroup) {
  const formData = {
    name: borrowerGroup.primary?.name || '',
    fullCaseNumber: request.caseNumber,
    fundCode: request.fundCode,
    loanNumber: request.loanNumber,
    loanClosingDate: formatDate(request.loanClosingDate),
    loanAmount: formatAmount(request.loanAmount),
    disasterDesignationNumber: request.dstr_dsgt_cd,
    installmentDate: formatDate(request.istl_dt),
    installmentAmount: formatAmount(request.istl_set_asd_amt),
    setAsideType: request.set_asd_type_cd,
   
    // Borrower fields
    borrower1Name: '', // Field 6A
    borrower2Name: '', // Field 7A
    borrower3Name: '', // Field 8A
    borrower4Name: '', // Field 9A
   
    // Form metadata
    isFirstForm: borrowerGroup.isFirstForm,
    isContinuation: borrowerGroup.isContinuation,
    formNumber: borrowerGroup.formNumber || 1
  };
 
  if (borrowerGroup.isFirstForm) {
    // First form: 6A is primary, 7A-9A are first 3 co-borrowers
    formData.borrower1Name = borrowerGroup.primary?.name || '';
   
    borrowerGroup.coBorrowers.forEach((coBorrower, index) => {
      const fieldName = `borrower${index + 2}Name`; // borrower2Name, borrower3Name, borrower4Name
      if (coBorrower.fullName && coBorrower.fullName.trim() !== '') {
        formData[fieldName] = coBorrower.fullName;
      } else if (coBorrower.businessName && coBorrower.businessName.trim() !== '') {
        formData[fieldName] = coBorrower.businessName;
      } else {
        formData[fieldName] = '';
      }
    });
  } else {
    // Continuation forms: 6A-9A are all co-borrowers from nonPrimaryList
    borrowerGroup.coBorrowers.forEach((coBorrower, index) => {
      const fieldName = `borrower${index + 1}Name`; // borrower1Name, borrower2Name, borrower3Name, borrower4Name
      if (coBorrower.fullName && coBorrower.fullName.trim() !== '') {
        formData[fieldName] = coBorrower.fullName;
      } else if (coBorrower.businessName && coBorrower.businessName.trim() !== '') {
        formData[fieldName] = coBorrower.businessName;
      } else {
        formData[fieldName] = '';
      }
    });
  }
 
  return formData;
}


/**
 * Fill form fields with provided data
 * Enhanced with better PDF analysis and fallback options
 */
async function fillFormFields(form, data) {
  try {
      // Enhanced form analysis
      const fields = form.getFields();
      logger.debug(`Total form fields found: ${fields.length}`);
     
      if (fields.length === 0) {
          logger.warn('No form fields found in PDF. PDF may not have fillable form fields.');
          throw new Error('PDF_NO_FORM_FIELDS');
      }
     
      // Helper function to safely fill text fields
      const fillTextField = (fieldName, value) => {
          try {
              const field = form.getTextField(fieldName);
              field.setText(String(value || ''));
              logger.debug(`✓ Filled text field '${fieldName}': ${value}`);
              return true;
          } catch (err) {
              logger.warn(`✗ Could not fill text field '${fieldName}':`, err.message);
              return false;
          }
      };
     
      // Helper function to safely fill checkboxes
      const fillCheckBox = (fieldName, shouldCheck = true) => {
          try {
              const field = form.getCheckBox(fieldName);
              if (shouldCheck) {
                  field.check();
              } else {
                  field.uncheck();
              }
              logger.debug(`✓ Set checkbox '${fieldName}': ${shouldCheck}`);
              return true;
          } catch (err) {
              logger.warn(`✗ Could not fill checkbox '${fieldName}':`, err.message);
              return false;
          }
      };
     
      logger.debug('=== ATTEMPTING TO FILL TEXT FIELD NAMES ===');
      logger.debug(`Form Type: ${data.isFirstForm ? 'First Form' : 'Continuation Form'} ${data.formNumber || 1}`);
     
      const textFieldMappings = [
          ['1 Name', data.name],
          ['6A Borrower Name', data.borrower1Name],
          ['7A Borrower Name', data.borrower2Name],
          ['8A Borrower Name', data.borrower3Name],
          ['9A Borrower Name', data.borrower4Name],
          ['2A State Code', data.fullCaseNumber.substring(0,2)],
          ['2B County', data.fullCaseNumber.substring(2,5)],
          ['2C Tax ID', data.fullCaseNumber.substring(6)],
          ['3A Fund Code', data.fundCode],
          ['3B Loan Number', data.loanNumber],
          ['3C Date', data.loanClosingDate],
          ['4C Amount Setaside', data.installmentAmount],
          ['3D Amount', data.loanAmount],
          ['4A Disaster Designation Number', data.disasterDesignationNumber],
          ['4B Date of Installment Setaside', data.installmentDate]
      ];
     
      let filledCount = 0;
      textFieldMappings.forEach(([fieldName, value]) => {
          if (fillTextField(fieldName, value)) {
              filledCount++;
          }
      });

      logger.debug('=== ATTEMPTING TO FILL CHECKBOX FIELD NAMES ===');

      const checkBoxFieldMappings = [
        ['5A Disaster Set-Aside', data.setAsideType === 'DSA'],
        ['5B Distressed Set-Aside', data.setAsideType === 'DBSA'],
      ];

      checkBoxFieldMappings.forEach(([fieldName, value]) => {
        if (fillCheckBox(fieldName, value)) {
            filledCount++;
        }
      });
     
      logger.debug(`=== SUMMARY: ${filledCount} fields filled successfully ===`);
     
      if (filledCount === 0) {
        logger.warn('Warning: No fields were filled. This might indicate field name mismatches.');
      }
     
  } catch (error) {
    logger.error('Error in fillFormFields:', error);
    throw error;
  }
}

/**
 * Create a ZIP file from multiple PDF buffers
 */
async function createZipFile(pdfFiles) {
  return new Promise((resolve, reject) => {
    const archive = archiver('zip', {
      zlib: { level: 9 } // Maximum compression
    });
   
    const chunks = [];
   
    archive.on('data', (chunk) => {
      chunks.push(chunk);
    });
   
    archive.on('end', () => {
      const zipBuffer = Buffer.concat(chunks);
      logger.debug(`ZIP file created successfully, size: ${zipBuffer.length} bytes`);
      resolve(zipBuffer);
    });
   
    archive.on('error', (err) => {
      logger.error('Error creating ZIP file:', err);
      reject(err);
    });
   
    // Add each PDF to the archive
    pdfFiles.forEach(file => {
      archive.append(file.buffer, { name: file.name });
      logger.debug(`Added ${file.name} to ZIP archive`);
    });
   
    // Finalize the archive
    archive.finalize();
  });
}

module.exports = { handler };