import { handler } from './handler/handler'
import { getParameterValue } from '../../lambda_layers/utils'
import { mockClient } from 'aws-sdk-client-mock';
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';
import axios from 'axios';
import { PDFDocument } from 'pdf-lib';

// Mock the utils functions
jest.mock('../../lambda_layers/utils/get-parameter-value')
jest.mock('axios')
jest.mock('pdf-lib')
jest.mock('archiver')

const getParameterValueMock = getParameterValue as jest.MockedFunction<typeof getParameterValue>
const axiosMock = axios as jest.Mocked<typeof axios>

// Mock S3 Client
const s3Mock = mockClient(S3Client);

// Mock PDF-lib
const mockPDFDocument = {
  load: jest.fn(),
  getForm: jest.fn(),
  save: jest.fn()
};

const mockForm = {
  getFields: jest.fn(),
  getTextField: jest.fn(),
  getCheckBox: jest.fn()
};

const mockTextField = {
  setText: jest.fn()
};

const mockCheckBox = {
  check: jest.fn(),
  uncheck: jest.fn()
};

// Mock archiver
const mockArchiver = {
  on: jest.fn(),
  append: jest.fn(),
  finalize: jest.fn()
};

describe('fill2501Form Lambda Tests', () => {
  const mockSingleFormEvent = {
    headers: {
      origin: 'http://localhost:4200',
      authorization: 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...'
    },
    body: JSON.stringify({
      request_id: 4827,
      loan_id: 1910108,
      task_id: 1,
      addm_dt: "2017-09-13T05:00:00.000Z",
      dstr_dsgt_cd: "Z2024",
      eff_dt: "2017-09-13T05:00:00.000Z",
      istl_dt: "2024-09-13T05:00:00.000Z",
      istl_set_asd_amt: 1,
      istl_paid_amt: 1,
      eauth_id: "28200310169021026877",
      set_asd_type_cd: "DBSA",
      flpCustomerId: 264,
      caseNumber: "260760386202691",
      fundCode: 44,
      lastCashCreditDate: "2022-11-29T00:00:00-06:00",
      loanAmount: 64293.26,
      loanClosingDate: "2017-09-13T00:00:00-05:00",
      loanExpirationDate: "2032-09-13T00:00:00-05:00",
      loanNumber: "04",
      loanRelationshipTypeCode: "PR",
      loanType: "OL",
      nextInstallmentAmount: 14321,
      totalLoanScheduledAmount: -14321,
      totalUnpaidInterestAmount: 12127.39
    })
  };

  const mockMultipleFormEvent = {
    headers: {
      origin: 'http://localhost:4200',
      authorization: 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...'
    },
    body: JSON.stringify({
      request_id: 4784,
      loan_id: 1204755,
      task_id: 1,
      addm_dt: "2017-09-13T05:00:00.000Z",
      dstr_dsgt_cd: "Z2024",
      eff_dt: "2017-09-13T05:00:00.000Z",
      istl_dt: "2024-09-13T05:00:00.000Z",
      istl_set_asd_amt: 1,
      istl_paid_amt: 1,
      eauth_id: "28200310169021026877",
      set_asd_type_cd: "DBSA",
      flpCustomerId: 907,
      caseNumber: "260760386202691",
      fundCode: 43,
      lastCashCreditDate: "2022-11-29T00:00:00-06:00",
      loanAmount: 108270,
      loanClosingDate: "2017-09-13T00:00:00-05:00",
      loanExpirationDate: "2032-09-13T00:00:00-05:00",
      loanNumber: "28",
      loanRelationshipTypeCode: "PR",
      loanType: "EM",
      nextInstallmentAmount: 14321,
      totalLoanScheduledAmount: -14321,
      totalUnpaidInterestAmount: 12127.39
    })
  };

  const mockSingleBorrowerRelatedEntityData = {
    primary: {
      name: "Steve T. Rosga"
    },
    nonPrimaryList: [
      {
        fullName: "Jane Doe"
      }
    ]
  };

  const mockMultipleBorrowerRelatedEntityData = {
    primary: {
      name: "REED DAIRY FARMS LLC"
    },
    nonPrimaryList: [
      { fullName: "Borrower 2" },
      { fullName: "Borrower 3" },
      { fullName: "Borrower 4" },
      { fullName: "Borrower 5" },
      { fullName: "Borrower 6" },
      { fullName: "Borrower 7" },
      { fullName: "Borrower 8" },
      { fullName: "Borrower 9" },
      { fullName: "Borrower 10" },
      { fullName: "Borrower 11" },
      { fullName: "Borrower 12" },
      { fullName: "Borrower 13" }
    ]
  };

  const mockPDFBuffer = Buffer.from('mock-pdf-content');

  beforeEach(() => {
    jest.clearAllMocks();
    s3Mock.reset();
   
    // Set up environment variables
    process.env.PNAME_PREFIX = 'test-prefix/';
    process.env.PNAME_API_PREFIX = 'test-api-prefix/';
    process.env.SCRATCHPAD_BUCKET_NAME = 'test-bucket';
   
    // Default mock implementations
    getParameterValueMock
      .mockResolvedValueOnce('https://apps.int.fsa.fpac.usda.gov/fls/api/common/') // flp-common-api-Url
      .mockResolvedValueOnce('12345'); // FSA_2501_FILE_ID

    // Mock PDF-lib
    (PDFDocument.load as jest.Mock).mockResolvedValue(mockPDFDocument);
    mockPDFDocument.getForm.mockReturnValue(mockForm);
    mockPDFDocument.save.mockResolvedValue(mockPDFBuffer);
    mockForm.getFields.mockReturnValue([{ name: 'field1' }, { name: 'field2' }]);
    mockForm.getTextField.mockReturnValue(mockTextField);
    mockForm.getCheckBox.mockReturnValue(mockCheckBox);

  });

  test('fill2501Form - Single form happy path', async () => {
    // Mock axios calls
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(mockSingleFormEvent);

    // Verify response structure
    expect(result.statusCode).toBe(200);
    const responseBody = JSON.parse(result.body as string);
   
    expect(responseBody.documentName).toBe('FSA-2501 Steve T. Rosga 44-04.pdf');
    expect(responseBody.totalFormsGenerated).toBe(1);
    expect(responseBody.totalBorrowers).toBe(2);
    expect(responseBody.fileType).toBe('pdf');
    expect(responseBody.containsMultipleForms).toBe(false);
    expect(responseBody.formsDetails).toHaveLength(1);
    expect(responseBody.formsDetails[0].isFirstForm).toBe(true);
    expect(responseBody.formsDetails[0].borrowerCount).toBe(2);

    // Verify parameter retrieval
    expect(getParameterValueMock).toHaveBeenCalledWith('test-prefix/global/flp-common-api-Url');
    expect(getParameterValueMock).toHaveBeenCalledWith('test-api-prefix/FSA_2501_FILE_ID');

    // Verify API calls
    expect(axiosMock.get).toHaveBeenCalledWith(
      'https://apps.int.fsa.fpac.usda.gov/fls/api/common/related-entity-info-by-loan?loanId=1910108&eauthId=28200310169021026877',
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': mockSingleFormEvent.headers.authorization,
          'Origin': 'http://localhost:4200'
        }
      }
    );

    expect(axiosMock.get).toHaveBeenCalledWith(
      'https://apps.int.fsa.fpac.usda.gov/fls/api/common/usda-forms/12345',
      {
        headers: { 'Accept': 'application/pdf' },
        responseType: 'arraybuffer'
      }
    );

    // Verify S3 upload
    expect(s3Mock.commandCalls(PutObjectCommand)).toHaveLength(1);
    const s3Call = s3Mock.commandCalls(PutObjectCommand)[0];
    expect(s3Call.args[0].input.Bucket).toBe('test-bucket');
    expect(s3Call.args[0].input.Key).toBe('FSA-2501Form/FSA-2501 Steve T. Rosga 44-04.pdf');
    expect(s3Call.args[0].input.ContentType).toBe('application/pdf');

    // Verify PDF processing
    expect(PDFDocument.load).toHaveBeenCalledWith(mockPDFBuffer);
    expect(mockPDFDocument.getForm).toHaveBeenCalled();
    expect(mockPDFDocument.save).toHaveBeenCalled();
  });

  test('fill2501Form - Multiple forms ZIP creation', async () => {
    // Mock archiver for ZIP creation
    const mockArchiverInstance: any = {
      on: jest.fn((event: string, callback: any) => {
        if (event === 'data') {
          // Simulate archive data chunks
          setTimeout(() => callback(Buffer.from('zip-chunk-1')), 0);
          setTimeout(() => callback(Buffer.from('zip-chunk-2')), 0);
        } else if (event === 'end') {
          setTimeout(callback, 0);
        }
        return mockArchiverInstance;
      }),
      append: jest.fn(),
      finalize: jest.fn()
    };

    const archiver = require('archiver');
    archiver.mockReturnValue(mockArchiverInstance);

    // Mock axios calls
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockMultipleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(mockMultipleFormEvent);

    // Verify response structure for multiple forms
    expect(result.statusCode).toBe(200);
    const responseBody = JSON.parse(result.body as string);
   
    expect(responseBody.documentName).toBe('FSA-2501 REED DAIRY FARMS LLC 43-28.zip');
    expect(responseBody.totalFormsGenerated).toBe(4);
    expect(responseBody.totalBorrowers).toBe(13);
    expect(responseBody.fileType).toBe('zip');
    expect(responseBody.containsMultipleForms).toBe(true);
    expect(responseBody.formsDetails).toHaveLength(4);

    // Verify form details
    expect(responseBody.formsDetails[0].isFirstForm).toBe(true);
    expect(responseBody.formsDetails[0].isContinuation).toBe(false);
    expect(responseBody.formsDetails[1].isFirstForm).toBe(false);
    expect(responseBody.formsDetails[1].isContinuation).toBe(true);

    // Verify S3 upload with ZIP
    const s3Call = s3Mock.commandCalls(PutObjectCommand)[0];
    expect(s3Call.args[0].input.ContentType).toBe('application/zip');
    expect(s3Call.args[0].input.Key).toBe('FSA-2501Form/FSA-2501 REED DAIRY FARMS LLC 43-28.zip');

    // Verify PDF was created multiple times (4 forms)
    expect(PDFDocument.load).toHaveBeenCalledTimes(4);
    expect(mockPDFDocument.save).toHaveBeenCalledTimes(4);
  });

  test('fill2501Form - Related entity API failure', async () => {
    // Mock API failure
    axiosMock.get.mockRejectedValueOnce(new Error('API not available'));

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('API not available');
  });

  test('fill2501Form - PDF template fetch failure', async () => {
    // Mock successful related entity call but failed PDF fetch
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockRejectedValueOnce(new Error('PDF template not found'));

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('PDF template not found');
  });

  test('fill2501Form - S3 upload failure', async () => {
    // Mock successful API calls but failed S3 upload
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    s3Mock.on(PutObjectCommand).rejects(new Error('S3 upload failed'));

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('S3 upload failed');
  });

  test('fill2501Form - PDF processing failure', async () => {
    // Mock successful API calls but failed PDF processing
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    (PDFDocument.load as jest.Mock).mockRejectedValueOnce(new Error('Invalid PDF'));

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Invalid PDF');
  });

  test('fill2501Form - Parameter retrieval failure', async () => {
    // Mock parameter retrieval failure
    getParameterValueMock.mockReset();
    getParameterValueMock.mockRejectedValueOnce(new Error('Parameter not found'));

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Parameter not found');
  });

  test('fill2501Form - Invalid JSON body', async () => {
    const eventWithInvalidJson = {
      ...mockSingleFormEvent,
      body: 'invalid json'
    };

    const result = await handler(eventWithInvalidJson);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toHaveLength(1);
    expect(responseBody.errors[0]).toMatch(/Unexpected token/);
  });

  test('fill2501Form - Object body instead of string', async () => {
    const eventWithObjectBody = {
      ...mockSingleFormEvent,
      body: JSON.parse(mockSingleFormEvent.body) // Object instead of string
    };

    // Mock successful execution
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(eventWithObjectBody);

    expect(result.statusCode).toBe(200);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.documentName).toBe('FSA-2501 Steve T. Rosga 44-04.pdf');
  });

  test('fill2501Form - Missing headers', async () => {
    const eventWithMissingHeaders = {
      headers: {}, // No origin or authorization
      body: mockSingleFormEvent.body
    };

    // Mock successful execution
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(eventWithMissingHeaders);

    expect(result.statusCode).toBe(200);
   
    // Verify API call was made without Origin header
    expect(axiosMock.get).toHaveBeenCalledWith(
      expect.stringContaining('related-entity-info-by-loan'),
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': undefined
        }
      }
    );
  });

  test('fill2501Form - Different header cases', async () => {
    const eventWithDifferentCaseHeaders = {
      headers: {
        Origin: 'http://localhost:4200', // Capital O
        Authorization: mockSingleFormEvent.headers.authorization // Capital A
      },
      body: mockSingleFormEvent.body
    };

    // Mock successful execution
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(eventWithDifferentCaseHeaders);

    expect(result.statusCode).toBe(200);
   
    // Verify headers were handled correctly
    expect(result.headers?.Origin).toBe('http://localhost:4200');
    expect(result.headers?.Authorization).toBe(mockSingleFormEvent.headers.authorization);
  });

  test('fill2501Form - Cache usage on second call', async () => {
    // First call
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    await handler(mockSingleFormEvent);

    // Reset mocks but keep the same instance (to test caching)
    getParameterValueMock.mockClear();
    axiosMock.get.mockClear();

    // Mock second call
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockSingleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    // Second call - should use cached values
    await handler(mockSingleFormEvent);

    // Should not call getParameterValue again for cached values
    expect(getParameterValueMock).not.toHaveBeenCalled();
   
    // But should still make API calls
    expect(axiosMock.get).toHaveBeenCalledTimes(2);
  });

  test('fill2501Form - ZIP creation failure fallback', async () => {
    // Mock archiver failure
    const mockArchiverInstance: any = {
      on: jest.fn((event: string, callback: any) => {
        if (event === 'error') {
          setTimeout(() => callback(new Error('ZIP creation failed')), 0);
        }
        return mockArchiverInstance;
      }),
      append: jest.fn(),
      finalize: jest.fn()
    };

    const archiver = require('archiver');
    archiver.mockReturnValue(mockArchiverInstance);

    // Mock axios calls for multiple forms
    axiosMock.get
      .mockResolvedValueOnce({
        data: mockMultipleBorrowerRelatedEntityData
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(mockMultipleFormEvent);

    // Should still succeed but fall back to PDF instead of ZIP
    expect(result.statusCode).toBe(200);
    const responseBody = JSON.parse(result.body as string);
   
    // Should fall back to first PDF when ZIP creation fails
    expect(responseBody.fileType).toBe('pdf');
    expect(responseBody.documentName).toContain('.pdf');
  });

  test('fill2501Form - Empty related entity data', async () => {
    // Mock API call with empty data
    axiosMock.get
      .mockResolvedValueOnce({
        data: { primary: null, nonPrimaryList: [] }
      })
      .mockResolvedValueOnce({
        status: 200,
        data: mockPDFBuffer
      });

    const result = await handler(mockSingleFormEvent);

    expect(result.statusCode).toBe(200);
    const responseBody = JSON.parse(result.body as string);
   
    // Should handle empty borrower data gracefully
    expect(responseBody.totalBorrowers).toBe(1); // 1 primary + 0 co-borrowers
    expect(responseBody.totalFormsGenerated).toBe(1);
  });
});