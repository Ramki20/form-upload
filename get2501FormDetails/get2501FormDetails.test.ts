import { handler, resetCache } from './handler/handler'
const { fpacPrismaClient } = require('/opt/prisma')
import { getParameterValue } from '../../lambda_layers/utils'
import axios from 'axios';

jest.mock('../../lambda_layers/utils/get-parameter-value')
jest.mock('axios')
jest.mock('/opt/prisma')

const getParameterValueMock = getParameterValue as jest.MockedFunction<typeof getParameterValue>
const axiosMock = axios as jest.Mocked<typeof axios>
const fpacPrismaClientMock = fpacPrismaClient as jest.MockedFunction<typeof fpacPrismaClient>

// Sample data from your document
const mockRequestDocuments = [

  {
    rqst_doc_id: 441,
    rqst_id: 1262,
    doc_type_cd: 'RF',
    doc_id: 841,
    doc_type_ot_desc: 'Applicant',
    data_stat_cd: 'A',
    cre_dt: '2025-05-14T14:50:21.090Z',
    cre_user_nm: '28200310169021026877',
    last_chg_dt: '2025-05-14T14:50:21.090Z',
    last_chg_user_nm: '28200310169021026877',
    loan_id: null
  },
  {
    rqst_doc_id: 443,
    rqst_id: 1262,
    doc_type_cd: 'RF',
    doc_id: 843,
    doc_type_ot_desc: 'Applicant',
    data_stat_cd: 'A',
    cre_dt: '2025-05-14T15:17:28.359Z',
    cre_user_nm: '28200310169021026877',
    last_chg_dt: '2025-05-14T15:17:28.359Z',
    last_chg_user_nm: '28200310169021026877',
    loan_id: null
  },
  {
    rqst_doc_id: 444,
    rqst_id: 1262,
    doc_type_cd: 'RF',
    doc_id: 844,
    doc_type_ot_desc: 'Applicant',
    data_stat_cd: 'A',
    cre_dt: '2025-05-14T15:25:09.766Z',
    cre_user_nm: '28200310169021026877',
    last_chg_dt: '2025-05-14T15:25:09.766Z',
    last_chg_user_nm: '28200310169021026877',
    loan_id: null
  },
  {
    rqst_doc_id: 2210,
    rqst_id: 1262,
    doc_type_cd: 'OTH',
    doc_id: 2646,
    doc_type_ot_desc: null,
    data_stat_cd: 'A',
    cre_dt: '2025-06-22T15:06:50.028Z',
    cre_user_nm: '28200310169021026877',
    last_chg_dt: '2025-06-22T15:06:50.028Z',
    last_chg_user_nm: '28200310169021026877',
    loan_id: null
  }
];

const mockDocumentDetails = [

  {
    documentName: 'Test',
    documentCategoryCd: null,
    documentId: 841,
    documentReceivedDate: '2025-05-13T05:00:00.000Z',
    documentTypeCd: null,
    fileExtensionCd: 'txt',
    flpCustomerId: 139,
    otherDocumentDesc: null,
    storageAddressTxt: '684636',
    documentUploadDate: '2025-05-14T00:00:00.000Z',
    documentSourceCd: 'APT'
  },
  {
    documentName: 'Test',
    documentCategoryCd: null,
    documentId: 843,
    documentReceivedDate: '2025-05-09T05:00:00.000Z',
    documentTypeCd: null,
    fileExtensionCd: 'docx',
    flpCustomerId: 139,
    otherDocumentDesc: null,
    storageAddressTxt: '684638',
    documentUploadDate: '2025-05-14T00:00:00.000Z',
    documentSourceCd: 'APT'
  },
  {
    documentName: 'Test',
    documentCategoryCd: null,
    documentId: 844,
    documentReceivedDate: '2025-05-12T05:00:00.000Z',
    documentTypeCd: null,
    fileExtensionCd: 'pdf',
    flpCustomerId: 139,
    otherDocumentDesc: null,
    storageAddressTxt: '684639',
    documentUploadDate: '2025-05-14T00:00:00.000Z',
    documentSourceCd: 'APT'
  },
  {
    documentName: 'FSA-2501 Ernest H. Ambler jr 41-07',
    documentCategoryCd: null,
    documentId: 2646,
    documentReceivedDate: null,
    documentTypeCd: null,
    fileExtensionCd: 'pdf',
    flpCustomerId: 139,
    otherDocumentDesc: null,
    storageAddressTxt: '692039',
    documentUploadDate: '2025-06-22T00:00:00.000Z',
    documentSourceCd: 'FLX'
  }
];

const mockPrismaClient = {
  rqst_doc: {
    findMany: jest.fn()
  }
};

describe('get2501FormDetails Lambda Tests', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    resetCache();
    process.env.PNAME_PREFIX = 'test-prefix-';
   
    // Default mocks
    fpacPrismaClientMock.mockResolvedValue(mockPrismaClient);
    mockPrismaClient.rqst_doc.findMany.mockResolvedValue(mockRequestDocuments);
    axiosMock.get.mockResolvedValue({
      status: 200,
      data: {
        message: 'Success',
        data: mockDocumentDetails,
        count: 4
      }
    });
  });

  // Test successful execution with basic parameters
  test('happy path - basic request with rqstId only', async () => {

    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {
        Authorization: 'Bearer test-token',
        Origin: 'https://test.example.com'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body  as string);
    expect(body.message).toBe('Success');
    expect(body.data).toHaveLength(4);
    expect(body.count).toBe(4);
   
    // Verify that each item has both request document data and document details
    body.data.forEach((item:any ) => {
      expect(item).toHaveProperty('rqst_doc_id');
      expect(item).toHaveProperty('doc_id');
      expect(item).toHaveProperty('documentDetails');
    });
  });

  // Test with fundCode and loanNumber filtering
  test('successful execution with fundCode and loanNumber filtering', async () => {

    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');

    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '41',
        loanNumber: '07'
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body  as string);
    expect(body.message).toBe('Success');
   
    // Should only return documents that match the fund code and loan number pattern
    expect(body.data).toHaveLength(1);
    expect(body.data[0].doc_id).toBe(2646);
    expect(body.data[0].documentDetails.documentName).toContain('41-07');
  });

  // Test with loanId filtering
  test('successful execution with loanId filtering', async () => {

    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');

    // Mock data with loan_id
    const mockWithLoanId = [...mockRequestDocuments];
    //mockWithLoanId[0].loan_id = 123;
    Object.assign({}, mockRequestDocuments[0], { loan_id: 123 })  
    mockPrismaClient.rqst_doc.findMany.mockResolvedValue([mockWithLoanId[0]]);

    const event = {
      queryStringParameters: {
        rqstId: '1262',
        loanId: '123'
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body as string);
    expect(body.message).toBe('Success');
    expect(mockPrismaClient.rqst_doc.findMany).toHaveBeenCalledWith(
      expect.objectContaining({
        where: {
          rqst_id: 1262,
          loan_id: 123
        }
      })
    );
  });  

  // Test missing rqstId parameter
  test('should return 400 when rqstId is missing', async () => {
    const event = {
      queryStringParameters: {},
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('rqstId query parameter is required');
  });

  // Test invalid rqstId parameter
  test('should return 400 when rqstId is invalid', async () => {
    const event = {
      queryStringParameters: {
        rqstId: 'invalid'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Invalid rqstId: invalid. Request ID must be a positive integer.');
  });

  // Test invalid fundCode format
  test('should return 400 when fundCode format is invalid', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '1',
        loanNumber: '07'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Fund code must be exactly 2 digits');
  });

  // Test invalid loanNumber format
  test('should return 400 when loanNumber format is invalid', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '41',
        loanNumber: '7'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Loan number must be exactly 2 digits');
  });

  // Test conflicting parameters
  test('should return 400 when both fundCode/loanNumber and loanId are provided', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '41',
        loanNumber: '07',
        loanId: '123'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Cannot provide both fundCode/loanNumber and loanId parameters. Use either fundCode+loanNumber or loanId, not both.');
  });

  // Test missing loanNumber when fundCode is provided
  test('should return 400 when fundCode is provided without loanNumber', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '41'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Both fundCode and loanNumber must be provided together');
  });

  // Test invalid loanId
  test('should return 400 when loanId is invalid', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262',
        loanId: 'invalid'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Invalid loanId: invalid. Loan ID must be a positive integer.');
  });

  // Test Prisma client failure
  test('should return 400 when Prisma client fails to initialize', async () => {
    fpacPrismaClientMock.mockResolvedValue(null);

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(400);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Failed to instantiate fpacPrismaClient');
  });

  // Test external API failure (should not fail the entire request)
  test('should handle external API failure gracefully', async () => {
    axiosMock.get.mockRejectedValue(new Error('API Error'));

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body as string);
    expect(body.message).toBe('Success');
   
    // Should still return request documents, but with null documentDetails
    body.data.forEach((item:any) => {
      expect(item.documentDetails).toBeNull();
    });
  });

  // Test no documents found
  test('should handle empty document results', async () => {
    mockPrismaClient.rqst_doc.findMany.mockResolvedValue([]);

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body as string);
    expect(body.message).toBe('Success');
    expect(body.data).toHaveLength(0);
    expect(body.count).toBe(0);
  });

  // Test case sensitivity for headers
  test('should handle case-insensitive headers', async () => {
    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {
        authorization: 'Bearer test-token',  // lowercase
        origin: 'https://test.example.com'   // lowercase
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    expect(axiosMock.get).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer test-token',
          'Origin': 'https://test.example.com'
        })
      })
    );
  });

  // Test database error handling
  test('should handle database errors', async () => {
    mockPrismaClient.rqst_doc.findMany.mockRejectedValue(new Error('Database error'));

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {}
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(500);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Database error');
  });

  // Test parameter value retrieval error
  /*
  test('should handle parameter value retrieval error', async () => {

    getParameterValueMock.mockRejectedValue(new Error('Parameter retrieval failed'));

    const event = {
      queryStringParameters: {
        rqstId: '1262'
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(500);
    const body = JSON.parse(result.body as string);
    expect(body.errors).toContain('Parameter retrieval failed');
  });
  */

  // Test document filtering edge cases
  test('should filter documents correctly with complex names', async () => {

    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');

    const mockComplexDocuments = [{
      ...mockDocumentDetails[0],
      documentId: 2646,
      documentName: 'FSA-2501 Complex Company LLC 41-07'
    }];
   
    axiosMock.get.mockResolvedValue({
      status: 200,
      data: {
        message: 'Success',
        data: mockComplexDocuments,
        count: 1
      }
    });

    const event = {
      queryStringParameters: {
        rqstId: '1262',
        fundCode: '41',
        loanNumber: '07'
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    const body = JSON.parse(result.body as string);
    expect(body.data).toHaveLength(1);
    expect(body.data[0].documentDetails.documentName).toContain('41-07');
  });

  // Test trimming of query parameters
  test('should handle whitespace in query parameters', async () => {
    const event = {
      queryStringParameters: {
        rqstId: ' 1262 ',
        fundCode: ' 41 ',
        loanNumber: ' 07 '
      },
      headers: {
        Authorization: 'Bearer test-token'
      }
    };
    const context = {};

    const result = await handler(event, context);

    expect(result.statusCode).toBe(200);
    expect(mockPrismaClient.rqst_doc.findMany).toHaveBeenCalledWith(
      expect.objectContaining({
        where: {
          rqst_id: 1262
        }
      })
    );
  });
});
