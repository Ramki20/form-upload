import { handler } from './handler/handler'
import { getParameterValue, validateAllowedOrigins } from '../../lambda_layers/utils'
import { mockClient } from 'aws-sdk-client-mock';
import { StartSyncExecutionCommand, SFNClient } from '@aws-sdk/client-sfn';

// Mock the utils functions
jest.mock('../../lambda_layers/utils/get-parameter-value')
jest.mock('../../lambda_layers/utils/validate-allowed-origins')

const getParameterValueMock = getParameterValue as jest.MockedFunction<typeof getParameterValue>
const validateAllowedOriginsMock = validateAllowedOrigins as jest.MockedFunction<typeof validateAllowedOrigins>

// Mock SFN Client
const sfnMock = mockClient(SFNClient);

describe('setAsideRequestParent Lambda Tests', () => {
  const mockEvent = {
    headers: {
      origin: 'http://localhost:4200',
      authorization: 'Bearer eyJraWQiOiI0c056aksyRENpalNJK0Q5aWpOcWpEVGxNTGFBVnNzc2NHdHVWUmhac3pZPSIsImFsZyI6IlJTMjU2In0...'
    },
    body: JSON.stringify({
      rqst_id: 4827,
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

  const mockStepFunctionOutput = {
    statusCode: 200,
    headers: {
      "content-type": "application/json",
      "access-control-allow-origin": "*",
      "access-control-allow-credentials": true,
      "access-control-allow-methods": "GET,HEAD,OPTIONS,POST,PUT",
      "access-control-allow-headers": "X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept, Authorization, X-Api-Key, X-Amz-Security-Token"
    },
    body: JSON.stringify({
      setAsideRequestData: {
        set_asd_rqst_id: 240,
        task_id: 1,
        loan_id: 1910108,
        addm_dt: "2017-09-13T05:00:00.000Z",
        dstr_dsgt_cd: "Z2024",
        eff_dt: "2017-09-13T05:00:00.000Z",
        istl_dt: "2024-09-13T05:00:00.000Z",
        istl_set_asd_amt: "1",
        istl_paid_amt: "1",
        data_stat_cd: "A",
        cre_dt: "2025-06-19T18:46:09.403Z",
        cre_user_nm: "28200310169021026877",
        last_chg_dt: "2025-06-19T18:46:09.403Z",
        last_chg_user_nm: "28200310169021026877",
        set_asd_type_cd: "DBSA",
        rqst_id: 4827,
        documentName: "FSA-2501 Steve T. Rosga 44-04.pdf"
      }
    }),
    isBase64Encoded: false,
    status: "Set aside request processed successfully"
  };

  beforeEach(() => {
    jest.clearAllMocks();
    sfnMock.reset();
   
    // Set up environment variables
    process.env.PNAME_PREFIX = 'test-prefix/';
    process.env.PNAME_API_PREFIX = 'test-api-prefix/';
   
    // Default mock implementations
    getParameterValueMock
      .mockResolvedValueOnce('http://localhost:4200') // allowedOrigins
      .mockResolvedValueOnce('arn:aws:states:us-east-1:515735035:stateMachine:fls-dev-svcn-sfn-set-aside-request-parent'); // stepFunctionArn
   
    validateAllowedOriginsMock.mockImplementation(() => {});
  });

  test('setAsideRequestParent - Happy path', async () => {
    // Mock successful step function execution
    sfnMock.on(StartSyncExecutionCommand).resolves({
      output: JSON.stringify(mockStepFunctionOutput)
    });

    const result = await handler(mockEvent);

    // Verify the response structure
    expect(result).toEqual({
      statusCode: 200,
      "isBase64Encoded": false,
      headers: {
        "access-control-allow-credentials": true,
        "access-control-allow-headers": "X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept, Authorization, X-Api-Key, X-Amz-Security-Token",
        "access-control-allow-methods": "GET,HEAD,OPTIONS,POST,PUT",
        "access-control-allow-origin": "*",
        "content-type": "application/json",
      },
      body: JSON.stringify({
        data: {
          set_asd_rqst_id: 240,
          task_id: 1,
          loan_id: 1910108,
          addm_dt: "2017-09-13T05:00:00.000Z",
          dstr_dsgt_cd: "Z2024",
          eff_dt: "2017-09-13T05:00:00.000Z",
          istl_dt: "2024-09-13T05:00:00.000Z",
          istl_set_asd_amt: "1",
          istl_paid_amt: "1",
          data_stat_cd: "A",
          cre_dt: "2025-06-19T18:46:09.403Z",
          cre_user_nm: "28200310169021026877",
          last_chg_dt: "2025-06-19T18:46:09.403Z",
          last_chg_user_nm: "28200310169021026877",
          set_asd_type_cd: "DBSA",
          rqst_id: 4827,
          documentName: "FSA-2501 Steve T. Rosga 44-04.pdf"
        }
      })
    });

    // Verify parameter retrieval calls
    expect(getParameterValueMock).toHaveBeenCalledWith('test-prefix/global/ALLOWED_ORIGINS');
    expect(getParameterValueMock).toHaveBeenCalledWith('test-api-prefix/sfn/set-aside-request-parent');

    // Verify origin validation
    expect(validateAllowedOriginsMock).toHaveBeenCalledWith(mockEvent, 'http://localhost:4200');

    // Verify step function execution
    expect(sfnMock.commandCalls(StartSyncExecutionCommand)).toHaveLength(1);
    const sfnCall = sfnMock.commandCalls(StartSyncExecutionCommand)[0];
    expect(sfnCall.args[0].input.stateMachineArn).toBe('arn:aws:states:us-east-1:515735035:stateMachine:fls-dev-svcn-sfn-set-aside-request-parent');
   
    const stepFunctionInput = JSON.parse(sfnCall.args[0].input.input as string);
    expect(stepFunctionInput.body).toEqual(JSON.parse(mockEvent.body));
    expect(stepFunctionInput.headers.origin).toBe('http://localhost:4200');
    expect(stepFunctionInput.headers.authorization).toBe(mockEvent.headers.authorization);
  });

  test('setAsideRequestParent - Missing required field', async () => {
    const eventWithMissingField = {
      ...mockEvent,
      body: JSON.stringify({
        rqst_id: 4827,
        loan_id: 1910108,
        // Missing task_id and other required fields
        addm_dt: "2017-09-13T05:00:00.000Z"
      })
    };

    const result = await handler(eventWithMissingField);

    expect(result.statusCode).toBe(400);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Missing required field: task_id');
  });

  test('setAsideRequestParent - Multiple missing required fields', async () => {
    const eventWithMissingFields = {
      ...mockEvent,
      body: JSON.stringify({
        request_id: 4827
        // Missing most required fields
      })
    };

    const result = await handler(eventWithMissingFields);

    expect(result.statusCode).toBe(400);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toHaveLength(1);
    expect(responseBody.errors[0]).toMatch(/Missing required field:/);
  });

  test('setAsideRequestParent - Invalid JSON body', async () => {
    const eventWithInvalidJson = {
      ...mockEvent,
      body: 'invalid json'
    };

    const result = await handler(eventWithInvalidJson);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toHaveLength(1);
    expect(responseBody.errors[0]).toMatch(/Unexpected token/);
  });

  test('setAsideRequestParent - Step function execution failure', async () => {
    // Mock step function failure
    sfnMock.on(StartSyncExecutionCommand).rejects(new Error('Step function execution failed'));

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Step function execution failed');
  });

  test('setAsideRequestParent - Parameter retrieval failure', async () => {
    // Mock parameter retrieval failure
    getParameterValueMock.mockReset();
    getParameterValueMock.mockRejectedValueOnce(new Error('Parameter not found'));

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(500);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Parameter not found');
  });

  test('setAsideRequestParent - Origin validation failure', async () => {
    // Mock origin validation failure
    validateAllowedOriginsMock.mockImplementation(() => {
      const error = new Error('Origin not allowed') as Error & { statusCode?: number };
      error.statusCode = 403;
      throw error;
    });

    const result = await handler(mockEvent);

    expect(result.statusCode).toBe(403);
    const responseBody = JSON.parse(result.body as string);
    expect(responseBody.errors).toContain('Origin not allowed');
  });

  test('setAsideRequestParent - Headers with different case', async () => {
    const eventWithDifferentCaseHeaders = {
      ...mockEvent,
      headers: {
        Origin: 'http://localhost:4200', // Capital O
        Authorization: mockEvent.headers.authorization // Capital A
      }
    };

    sfnMock.on(StartSyncExecutionCommand).resolves({
      output: JSON.stringify(mockStepFunctionOutput)
    });

    const result = await handler(eventWithDifferentCaseHeaders);

    expect(result.statusCode).toBe(200);
   
    // Verify step function was called with correct headers
    const sfnCall = sfnMock.commandCalls(StartSyncExecutionCommand)[0];
    const stepFunctionInput = JSON.parse(sfnCall.args[0].input.input as string);
    expect(stepFunctionInput.headers.origin).toBe('http://localhost:4200');
    expect(stepFunctionInput.headers.authorization).toBe(mockEvent.headers.authorization);
  });

  test('setAsideRequestParent - Object body instead of string', async () => {
    const eventWithObjectBody = {
      ...mockEvent,
      body: JSON.parse(mockEvent.body) // Object instead of string
    };

    sfnMock.on(StartSyncExecutionCommand).resolves({
      output: JSON.stringify(mockStepFunctionOutput)
    });

    const result = await handler(eventWithObjectBody);

    expect(result.statusCode).toBe(200);
   
    // Verify the body was processed correctly
    const sfnCall = sfnMock.commandCalls(StartSyncExecutionCommand)[0];
    const stepFunctionInput = JSON.parse(sfnCall.args[0].input.input as string);


    expect(stepFunctionInput.body).toEqual(JSON.parse(mockEvent.body));
  });

  test('setAsideRequestParent - Cache usage on second call', async () => {
    // First call
    sfnMock.on(StartSyncExecutionCommand).resolves({
      output: JSON.stringify(mockStepFunctionOutput)
    });

    await handler(mockEvent);

    // Reset mocks but keep the same instance (to test caching)
    getParameterValueMock.mockClear();
    validateAllowedOriginsMock.mockClear();

    // Second call - should use cached values
    await handler(mockEvent);

    // Should not call getParameterValue again for cached values
    expect(getParameterValueMock).not.toHaveBeenCalled();
    // But should still validate origins
    expect(validateAllowedOriginsMock).toHaveBeenCalledTimes(1);
  });

  test('setAsideRequestParent - Step function returns error status', async () => {
    const errorOutput = {
      statusCode: 400,
      body: JSON.stringify({
        error: "Validation failed"
      })
    };

    sfnMock.on(StartSyncExecutionCommand).resolves({
      output: JSON.stringify(errorOutput)
    });

    const result = await handler(mockEvent);

    // The lambda should still return success since step function executed successfully
    // The actual error handling depends on the step function output parsing logic
    expect(result.statusCode).toBe(200);
  });
});