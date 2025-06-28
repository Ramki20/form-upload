const { LambdaHandlerResponse, Logger, getParameterValue } = require('/opt/utils');
const { S3Client, GetObjectCommand } = require("@aws-sdk/client-s3");
const axios = require('axios');
const FormData = require('form-data');

const cache = {
  flpCommonApiUrl: null,
};
 
const resetCache = () => {
  cache.flpCommonApiUrl = null;
};

const logger = new Logger();
const s3Client = new S3Client();

async function handler (event) {

  const response = new LambdaHandlerResponse();
  
  try {
  
      // Extract headers and request data
      const request = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
      const headers = event.headers;
      const corsOrigin = headers?.Origin || headers?.origin || headers?.ORIGIN;
      const authToken = headers?.Authorization || headers?.authorization;
      
      if (!request.documentName) {
           throw new Error('Document name is required');
      }
      
      const getObjectCommand = new GetObjectCommand({
        Bucket: process.env.SCRATCHPAD_BUCKET_NAME,
        Key: `FSA-2501Form/${request.documentName}`
      });
     
      const s3Response = await s3Client.send(getObjectCommand);
     
      // Convert the ReadableStream to Buffer
      const chunks = [];
      for await (const chunk of s3Response.Body) {
          chunks.push(chunk);
      }
      const documentBuffer = Buffer.concat(chunks);
      logger.debug('documentBuffer:',documentBuffer);

      if(!cache.flpCommonApiUrl)
      {
        const flpCommonApiURLAWSParamName = process.env.PNAME_PREFIX + 'global/flp-common-api-Url';
        cache.flpCommonApiUrl = await getParameterValue(flpCommonApiURLAWSParamName);
        logger.info('flpCommonApiUrl:',cache.flpCommonApiUrl);
      }
      const cmbsUploadUrl = cache.flpCommonApiUrl+`document`;

      const formData = new FormData();
      //formData.append('documentTypeCd', null);  //TODO:
      formData.append('flpCustomerId', request.flpCustomerId);
      formData.append('documentSourceCode', 'FLX');
      formData.append('file', documentBuffer, {
          filename: request.documentName,
          contentType: 'application/pdf'
      });
      formData.append('auditUser', process.env.AUDIT_USER);
     
      logger.debug('Uploading document to third party repository...');
     
      // Prepare headers for the upload request
      const uploadHeaders = {
        'Authorization': authToken,
        'Content-Type': 'multipart/form-data'
      };

      // Add Origin header if present
      if (corsOrigin) {
          uploadHeaders['Origin'] = corsOrigin;
      }
     
      // logger.debug('uploadHeaders:',uploadHeaders);

      // Upload to third party document repository
      const uploadResponse = await axios.put(cmbsUploadUrl, formData, {
          headers: uploadHeaders,
          maxContentLength: Infinity,
          maxBodyLength: Infinity
      });
     
      logger.debug('Document uploaded successfully:', uploadResponse.data);

      response.setHeader('Authorization',authToken);
      response.setHeader('Origin',corsOrigin);
      request.doc_id= uploadResponse.data.data.documentId;
    
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

module.exports = { handler };