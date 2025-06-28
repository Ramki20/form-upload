import { handler } from './handler/handler';
import { getParameterValue } from '../../lambda_layers/utils';
import { mockClient } from 'aws-sdk-client-mock';
import { S3Client, GetObjectCommand, GetObjectCommandOutput } from '@aws-sdk/client-s3';
import axios from 'axios';
import { Readable } from 'stream';
import { sdkStreamMixin } from '@smithy/util-stream';

// Mock the utils functions
jest.mock('../../lambda_layers/utils/get-parameter-value');
jest.mock('axios');

const getParameterValueMock = getParameterValue as jest.MockedFunction<typeof getParameterValue>;
const axiosMock = axios as jest.Mocked<typeof axios>;

// Create S3 client mock
const s3Mock = mockClient(S3Client);

describe('upload2501Form Lambda Tests', () => {
  const mockEvent = {
    headers: {
      'content-type': 'application/json',
      'access-control-allow-origin': '*',
      'access-control-allow-credentials': true,
      'access-control-allow-methods': 'GET,HEAD,OPTIONS,POST,PUT',
      'access-control-allow-headers': 'X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept, Authorization, X-Api-Key, X-Amz-Security-Token',
      'Authorization': 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...',
      'Origin': 'http://localhost:4200'
    },
    body: JSON.stringify({
      request_id: 4827,
      loan_id: 1910108,
      flpCustomerId: 264,
      documentName: 'FSA-2501 Steve T. Rosga 44-04.pdf',
      caseNumber: '260760386202691',
      fundCode: 44,
      loanAmount: 64293.26,
      totalBorrowers: 2
    })
  };

  // Create a proper S3 response with the correct stream type
  const createMockS3Response = (): GetObjectCommandOutput => {
    const stream = new Readable();
    stream.push(Buffer.from('mock pdf content'));
    stream.push(null);
   
    return {
      Body: sdkStreamMixin(stream),
      $metadata: {}
    };
  };

  const mockAxiosResponse = {
    data: {
      data: {
        documentId: 2587
      }
    }
  };

  beforeEach(() => {
    jest.clearAllMocks();
    s3Mock.reset();
   
    // Set up environment variables
    process.env.SCRATCHPAD_BUCKET_NAME = 'test-bucket';
    process.env.PNAME_PREFIX = 'test-';
    process.env.AUDIT_USER = 'test-audit-user';
  });

  // Test successful execution
  test('upload2501Form - happy path', async () => {
    // Mock parameter store response
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
   
    // Mock S3 response
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());

   
    // Mock axios response
    axiosMock.put.mockResolvedValue(mockAxiosResponse);

    const result = await handler(mockEvent);

    // Verify S3 was called with correct parameters
    expect(s3Mock.commandCalls(GetObjectCommand)).toHaveLength(1);
    expect(s3Mock.commandCalls(GetObjectCommand)[0].args[0].input).toEqual({
      Bucket: 'test-bucket',
      Key: 'FSA-2501Form/FSA-2501 Steve T. Rosga 44-04.pdf'
    });

    // Verify parameter store was called
    expect(getParameterValueMock).toHaveBeenCalledWith('test-global/flp-common-api-Url');

    // Verify axios was called with correct parameters
    expect(axiosMock.put).toHaveBeenCalledWith(
      'https://apps.int.fsa.fpac.usda.gov/fls/api/common/document',
      expect.any(Object), // FormData object
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...',
          'Content-Type': 'multipart/form-data',
          'Origin': 'http://localhost:4200'
        }),
        maxContentLength: Infinity,
        maxBodyLength: Infinity
      })
    );

    // Verify response structure
    expect(result.statusCode).toBe(200);
    expect(result.headers).toEqual(expect.objectContaining({
      'content-type': 'application/json',
      'Authorization': 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...',
      'Origin': 'http://localhost:4200'
    }));

    const responseBody = JSON.parse(result.body as string);

    expect(responseBody).toEqual(expect.objectContaining({
      request_id: 4827,
      loan_id: 1910108,
      flpCustomerId: 264,
      documentName: 'FSA-2501 Steve T. Rosga 44-04.pdf',
      doc_id: 2587
    }));
  });

  // Test missing document name
  test('upload2501Form - missing document name', async () => {
    const eventWithoutDocName = {
      ...mockEvent,
      body: JSON.stringify({
        request_id: 4827,
        flpCustomerId: 264
        // documentName is missing
      })
    };

    const result = await handler(eventWithoutDocName);

    expect(result.statusCode).toBe(500);
    expect(result.body).toContain('Document name is required');
    expect(s3Mock.commandCalls(GetObjectCommand)).toHaveLength(0);
    expect(axiosMock.put).not.toHaveBeenCalled();
  });

  // Test S3 error
  test('upload2501Form - S3 error', async () => {
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
    s3Mock.on(GetObjectCommand).rejects(new Error('S3 access denied'));

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(500);
    expect(result.body).toContain('S3 access denied');
    expect(axiosMock.put).not.toHaveBeenCalled();
  });

  // Test parameter store error
  test('upload2501Form - parameter store error', async () => {
    getParameterValueMock.mockRejectedValue(new Error('Parameter not found'));
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(500);
    expect(result.body).toContain('Parameter not found');
    expect(axiosMock.put).not.toHaveBeenCalled();
  });

  // Test axios upload error
  test('upload2501Form - third party upload error', async () => {
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());
   
    const uploadError = new Error('Upload failed') as Error & { statusCode?: number };
    uploadError.statusCode = 400;
    axiosMock.put.mockRejectedValue(uploadError);

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(400);
    expect(result.body).toContain('Upload failed');
  });

  // Test with string body (JSON parsing)
  test('upload2501Form - string body parsing', async () => {
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());
    axiosMock.put.mockResolvedValue(mockAxiosResponse);

    // Event with string body instead of object
    const stringBodyEvent = {
      ...mockEvent,
      body: JSON.stringify({
        request_id: 4827,
        flpCustomerId: 264,
        documentName: 'FSA-2501 Steve T. Rosga 44-04.pdf'
      })
    };

    const result = await handler(stringBodyEvent);

    expect(result.statusCode).toBe(200);
    expect(s3Mock.commandCalls(GetObjectCommand)).toHaveLength(1);
    expect(axiosMock.put).toHaveBeenCalled();
  });

  // Test without Origin header
  test('upload2501Form - no Origin header', async () => {
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());
    axiosMock.put.mockResolvedValue(mockAxiosResponse);

    const eventWithoutOrigin = {
      ...mockEvent,
      headers: {
        ...mockEvent.headers
      }
    };
    delete (eventWithoutOrigin.headers as any).Origin;

    const result = await handler(eventWithoutOrigin);

    expect(result.statusCode).toBe(200);
   
    // Verify axios was called without Origin header
    expect(axiosMock.put).toHaveBeenCalledWith(
      expect.any(String),
      expect.any(Object),
      expect.objectContaining({
        headers: expect.not.objectContaining({
          'Origin': expect.anything()
        })
      })
    );
  });

  // Test different header case variations
  test('upload2501Form - case insensitive headers', async () => {
    getParameterValueMock.mockResolvedValue('https://apps.int.fsa.fpac.usda.gov/fls/api/common/');
    s3Mock.on(GetObjectCommand).resolves(createMockS3Response());
    axiosMock.put.mockResolvedValue(mockAxiosResponse);

    const eventWithLowercaseHeaders = {
      ...mockEvent,
      headers: {
        ...mockEvent.headers,
        'authorization': 'Bearer test-token',
        'origin': 'http://test.com'
      }
    };
    delete (eventWithLowercaseHeaders.headers as any).Authorization;
    delete (eventWithLowercaseHeaders.headers as any).Origin;

    const result = await handler(eventWithLowercaseHeaders);

    expect(result.statusCode).toBe(200);
    expect(axiosMock.put).toHaveBeenCalledWith(
      expect.any(String),
      expect.any(Object),
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer test-token',
          'Origin': 'http://test.com'
        })
      })
    );
  });
});
