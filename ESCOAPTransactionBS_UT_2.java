package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.CancelESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.FindTransactionByTransactionIdBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.SubmitESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.client.ESCOAPTransactionServiceFactory;
import gov.usda.fsa.afao.escoap.sharedservice.model.ESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.model.FindESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.service.ESCOAPTransactionService;
import gov.usda.fsa.afao.escoap.sharedservice.util.exception.ESCOAPException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCancelTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransaction;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPRetrieveTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ESCOAPTransactionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * ESCOAPTransactionBS_UT_Mockito
 * 
 * Unit tests for ESCOAPTransactionBS using Mockito framework
 * 
 * @author Generated
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ESCOAPTransactionBS_UT_Mockito extends DLSExternalCommonTestMockBase {

    private ESCOAPTransactionBS escoapTransactionBS;
    private AgencyToken agencyToken;
    
    @Mock
    private ESCOAPTransactionService mockEscoapService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        escoapTransactionBS = new ESCOAPTransactionBS();
        agencyToken = createAgencyToken("DLMTest_User");
    }

    protected AgencyToken createAgencyToken(String userId) {
        AgencyToken token = new AgencyToken();
        try {
            token.setProcessingNode(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            token.setProcessingNode("localhost");
        }
        token.setApplicationIdentifier("OY");
        token.setRequestHost("localhost");
        token.setUserIdentifier(userId);
        token.setReadOnly(true);
        return token;
    }

    // ===== CREATE TRANSACTION TESTS =====

    @Test
    public void testCreateTransaction() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC createTransactionBC = createValidESCOAPCreateTransactionBC();
        ESCOAPTransactionResponse mockResponse = createSuccessfulESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.createTransaction(createTransactionBC);

            // Assert
            assertNotNull("Response should not be null", result);
            assertEquals("Confirmation number should match", Long.valueOf(12345), 
                        result.getConfirmationNumberList().get(0));
            assertTrue("Transaction should be successful", result.getSuccessful());
        }
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullDataSourceAcronym() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setDataSourceAcronym(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullTransactionAmount() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setTransactionAmount(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullTransactionQuantity() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setTransactionQuantity(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullTransactionRequestId() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setTransactionRequestId(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullReferenceOneCode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingReferenceOneCode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullReferenceOneNumber() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingReferenceOneNumber(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_EmptyConfirmationNumberList() throws Exception {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(agencyToken);
        contract.setApplicationSystemCode("OY");
        contract.setConfirmationNumberList(new ArrayList<>());
        escoapTransactionBS.cancelESCOAPTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCancelTransaction_NullConfirmationNumber() throws Exception {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(agencyToken);
        contract.setApplicationSystemCode("OY");
        List<Long> confirmationNumbers = new ArrayList<>();
        confirmationNumbers.add(null);
        contract.setConfirmationNumberList(confirmationNumbers);
        escoapTransactionBS.cancelESCOAPTransaction(contract);
    }

    // ===== INTEGRATION TESTS =====

    @Test
    public void testCreateTransaction_FullWorkflow() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC createContract = createValidESCOAPCreateTransactionBC();
        ESCOAPTransactionResponse mockResponse = createSuccessfulESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.createTransaction(createContract);

            // Assert
            assertNotNull("Response should not be null", result);
            assertNotNull("Confirmation number list should not be null", result.getConfirmationNumberList());
            assertFalse("Confirmation number list should not be empty", result.getConfirmationNumberList().isEmpty());
            assertEquals("Should have one confirmation number", 1, result.getConfirmationNumberList().size());
            
            // Verify service was called with correct parameters
            verify(mockEscoapService, times(1))
                .submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class));
        }
    }

    @Test
    public void testCancelTransaction_FullWorkflow() throws Exception {
        // Arrange
        ESCOAPCancelTransactionBC cancelContract = createValidESCOAPCancelTransactionBC();
        ESCOAPTransactionResponse mockResponse = createSuccessfulESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.cancelESCOAPTransaction(cancelContract);

            // Assert
            assertNotNull("Response should not be null", result);
            assertNotNull("Confirmation number list should not be null", result.getConfirmationNumberList());
            
            // Verify service was called
            verify(mockEscoapService, times(1))
                .cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class));
        }
    }

    @Test
    public void testRetrieveTransaction_FullWorkflow() throws Exception {
        // Arrange
        ESCOAPRetrieveTransactionBC retrieveContract = createValidESCOAPRetrieveTransactionBC();
        FindESCOAPTransactionResponse mockResponse = createSuccessfulFindESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.findESCOAPTransactionByTransactionId(any(FindTransactionByTransactionIdBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.retrieveTransaction(retrieveContract);

            // Assert
            assertNotNull("Response should not be null", result);
            assertNotNull("Confirmation number list should not be null", result.getConfirmationNumberList());
            assertEquals("Transaction request ID should match", "011252014125", 
                        result.getTransactionRequestIdentifier());
            assertEquals("Transaction status should match", "ACTIVE", 
                        result.getTransactionStatusCode());
            
            // Verify service was called
            verify(mockEscoapService, times(1))
                .findESCOAPTransactionByTransactionId(any(FindTransactionByTransactionIdBusinessContract.class));
        }
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    public void testCreateTransaction_MultipleErrors() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        ESCOAPTransactionResponse mockResponse = new ESCOAPTransactionResponse();
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error.key.1", "First error message");
        errors.put("error.key.2", "Second error message");
        mockResponse.setEscoapErrors(errors);
        mockResponse.setSuccessful(false);
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act & Assert
            try {
                escoapTransactionBS.createTransaction(contract);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertEquals("Should have 2 errors", 2, ex.getExtSystemErrorMap().size());
                assertTrue("Should contain first error", ex.getExtSystemErrorMap().containsKey("error.key.1"));
                assertTrue("Should contain second error", ex.getExtSystemErrorMap().containsKey("error.key.2"));
            }
        }
    }

    @Test
    public void testHealthCheck_ServiceDown() throws Exception {
        // Arrange
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.isHealthy()).thenReturn(false);

            // Act
            Boolean result = escoapTransactionBS.isHealthy(agencyToken);

            // Assert
            assertFalse("Service should be unhealthy", result);
        }
    }

    @Test
    public void testGenericException_HandledProperly() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            try {
                escoapTransactionBS.createTransaction(contract);
                fail("Expected DLSBusinessServiceException");
            } catch (DLSBusinessServiceException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertTrue("Should contain system unavailable error", 
                          ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
                assertEquals("Error message should match", "Unexpected error", 
                           ex.getExtSystemErrorMap().get("error.external.system.unavailable"));
            }
        }
    }
}CInvalidDataStopException.class)
    public void testCreateTransaction_NullContract() throws Exception {
        escoapTransactionBS.createTransaction(null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullAgencyToken() throws Exception {
        ESCOAPCreateTransactionBC contract = new ESCOAPCreateTransactionBC(null);
        contract.setEscoapTransactionList(createValidTransactionList());
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullTransactionList() throws Exception {
        ESCOAPCreateTransactionBC contract = new ESCOAPCreateTransactionBC(agencyToken);
        contract.setEscoapTransactionList(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test
    public void testCreateTransaction_ServiceUnavailable() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC createTransactionBC = createValidESCOAPCreateTransactionBC();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenThrow(new ESCOAPException("Service unavailable"));

            // Act & Assert
            try {
                escoapTransactionBS.createTransaction(createTransactionBC);
                fail("Expected DLSBusinessServiceException");
            } catch (DLSBusinessServiceException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertTrue("Should contain system unavailable error", 
                          ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
            }
        }
    }

    @Test
    public void testCreateTransaction_ServiceReturnsErrors() throws Exception {
        // Arrange
        ESCOAPCreateTransactionBC createTransactionBC = createValidESCOAPCreateTransactionBC();
        ESCOAPTransactionResponse mockResponse = createESCOAPTransactionResponseWithErrors();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act & Assert
            try {
                escoapTransactionBS.createTransaction(createTransactionBC);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertEquals("Should have 1 error", 1, ex.getExtSystemErrorMap().size());
            }
        }
    }

    // ===== CANCEL TRANSACTION TESTS =====

    @Test
    public void testCancelTransaction() throws Exception {
        // Arrange
        ESCOAPCancelTransactionBC cancelTransactionBC = createValidESCOAPCancelTransactionBC();
        ESCOAPTransactionResponse mockResponse = createSuccessfulESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.cancelESCOAPTransaction(cancelTransactionBC);

            // Assert
            assertNotNull("Response should not be null", result);
            assertEquals("Confirmation number should match", Long.valueOf(12345), 
                        result.getConfirmationNumberList().get(0));
        }
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCancelTransaction_NullContract() throws Exception {
        escoapTransactionBS.cancelESCOAPTransaction(null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCancelTransaction_NullAgencyToken() throws Exception {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(null);
        escoapTransactionBS.cancelESCOAPTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCancelTransaction_NullApplicationSystemCode() throws Exception {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(agencyToken);
        contract.setApplicationSystemCode(null);
        List<Long> confirmationNumbers = new ArrayList<>();
        confirmationNumbers.add(12345L);
        contract.setConfirmationNumberList(confirmationNumbers);
        escoapTransactionBS.cancelESCOAPTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCancelTransaction_NullConfirmationNumberList() throws Exception {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(agencyToken);
        contract.setApplicationSystemCode("OY");
        contract.setConfirmationNumberList(null);
        escoapTransactionBS.cancelESCOAPTransaction(contract);
    }

    @Test
    public void testCancelTransaction_ServiceUnavailable() throws Exception {
        // Arrange
        ESCOAPCancelTransactionBC cancelTransactionBC = createValidESCOAPCancelTransactionBC();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class)))
                .thenThrow(new ESCOAPException("Service unavailable"));

            // Act & Assert
            try {
                escoapTransactionBS.cancelESCOAPTransaction(cancelTransactionBC);
                fail("Expected DLSBusinessServiceException");
            } catch (DLSBusinessServiceException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertTrue("Should contain system unavailable error", 
                          ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
            }
        }
    }

    // ===== RETRIEVE TRANSACTION TESTS =====

    @Test
    public void testRetrieveTransaction() throws Exception {
        // Arrange
        ESCOAPRetrieveTransactionBC retrieveTransactionBC = createValidESCOAPRetrieveTransactionBC();
        FindESCOAPTransactionResponse mockResponse = createSuccessfulFindESCOAPTransactionResponse();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.findESCOAPTransactionByTransactionId(any(FindTransactionByTransactionIdBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act
            ESCOAPTransactionResponseBO result = escoapTransactionBS.retrieveTransaction(retrieveTransactionBC);

            // Assert
            assertNotNull("Response should not be null", result);
            assertEquals("Confirmation number should match", Long.valueOf(12345), 
                        result.getConfirmationNumberList().get(0));
        }
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveTransaction_NullContract() throws Exception {
        escoapTransactionBS.retrieveTransaction(null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveTransaction_NullAgencyToken() throws Exception {
        ESCOAPRetrieveTransactionBC contract = new ESCOAPRetrieveTransactionBC(null);
        escoapTransactionBS.retrieveTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveTransaction_NullTransactionRequestId() throws Exception {
        ESCOAPRetrieveTransactionBC contract = new ESCOAPRetrieveTransactionBC(agencyToken);
        contract.setAccountingProgramYear("2014");
        contract.setApplicationSystemCode("OY");
        contract.setTransactionRequestIdentifier(null);
        escoapTransactionBS.retrieveTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveTransaction_NullAccountingProgramYear() throws Exception {
        ESCOAPRetrieveTransactionBC contract = new ESCOAPRetrieveTransactionBC(agencyToken);
        contract.setAccountingProgramYear(null);
        contract.setApplicationSystemCode("OY");
        contract.setTransactionRequestIdentifier("011252014125");
        escoapTransactionBS.retrieveTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveTransaction_NullApplicationSystemCode() throws Exception {
        ESCOAPRetrieveTransactionBC contract = new ESCOAPRetrieveTransactionBC(agencyToken);
        contract.setAccountingProgramYear("2014");
        contract.setApplicationSystemCode(null);
        contract.setTransactionRequestIdentifier("011252014125");
        escoapTransactionBS.retrieveTransaction(contract);
    }

    @Test
    public void testRetrieveTransaction_ServiceUnavailable() throws Exception {
        // Arrange
        ESCOAPRetrieveTransactionBC retrieveTransactionBC = createValidESCOAPRetrieveTransactionBC();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.findESCOAPTransactionByTransactionId(any(FindTransactionByTransactionIdBusinessContract.class)))
                .thenThrow(new ESCOAPException("Service unavailable"));

            // Act & Assert
            try {
                escoapTransactionBS.retrieveTransaction(retrieveTransactionBC);
                fail("Expected DLSBusinessServiceException");
            } catch (DLSBusinessServiceException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertTrue("Should contain system unavailable error", 
                          ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
            }
        }
    }

    @Test
    public void testRetrieveTransaction_ServiceReturnsErrors() throws Exception {
        // Arrange
        ESCOAPRetrieveTransactionBC retrieveTransactionBC = createValidESCOAPRetrieveTransactionBC();
        FindESCOAPTransactionResponse mockResponse = createFindESCOAPTransactionResponseWithErrors();
        
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() => ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.findESCOAPTransactionByTransactionId(any(FindTransactionByTransactionIdBusinessContract.class)))
                .thenReturn(mockResponse);

            // Act & Assert
            try {
                escoapTransactionBS.retrieveTransaction(retrieveTransactionBC);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertNotNull("Error map should not be null", ex.getExtSystemErrorMap());
                assertEquals("Should have 1 error", 1, ex.getExtSystemErrorMap().size());
            }
        }
    }

    // ===== HEALTH CHECK TESTS =====

    @Test
    public void testIsHealthy() throws Exception {
        // Arrange
        try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
             MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(any(), anyString()))
                      .thenReturn(mockEscoapService);
            
            when(mockEscoapService.isHealthy()).thenReturn(true);

            // Act
            Boolean result = escoapTransactionBS.isHealthy(agencyToken);

            // Assert
            assertTrue("Service should be healthy", result);
        }
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testIsHealthy_NullAgencyToken() throws Exception {
        escoapTransactionBS.isHealthy(null);
    }

    // ===== HELPER METHODS =====

    private ESCOAPCreateTransactionBC createValidESCOAPCreateTransactionBC() {
        ESCOAPCreateTransactionBC contract = new ESCOAPCreateTransactionBC(agencyToken);
        contract.setEscoapTransactionList(createValidTransactionList());
        return contract;
    }

    private List<ESCOAPCreateTransaction> createValidTransactionList() {
        List<ESCOAPCreateTransaction> transactionList = new ArrayList<>();
        ESCOAPCreateTransaction transaction = new ESCOAPCreateTransaction(agencyToken);
        
        // Set required fields
        transaction.setAccountingProgramCode("0210");
        transaction.setAccountingProgramTransactionCode("470");
        transaction.setAccountingProgramYear("2014");
        transaction.setAccountingReferenceOneCode("LN");
        transaction.setAccountingReferenceOneNumber("1");
        transaction.setAccountingTransactionDate(Calendar.getInstance());
        transaction.setBusinessPartyIdentification("1533454");
        transaction.setBusinessTypeCode("00");
        transaction.setStateFSACode("01");
        transaction.setCountyFSACode("125");
        transaction.setCustomerName("SMITH NELSON");
        transaction.setDataSourceAcronym("SCIMS");
        transaction.setApplicationSystemCode("OY");
        transaction.setTransactionAmount(BigDecimal.valueOf(10.00));
        transaction.setTransactionRequestId("011252014125");
        transaction.setTransactionQuantity(BigDecimal.valueOf(0));
        transaction.setCommodityCode("");
        transaction.setLegacyTransactionRequestId("011252014125");
        transaction.setObligationConfirmationNumber(new BigInteger("1234567890"));
        transaction.setReversalIndicator("R");
        
        transactionList.add(transaction);
        return transactionList;
    }

    private ESCOAPCancelTransactionBC createValidESCOAPCancelTransactionBC() {
        ESCOAPCancelTransactionBC contract = new ESCOAPCancelTransactionBC(agencyToken);
        contract.setApplicationSystemCode("OY");
        List<Long> confirmationNumbers = new ArrayList<>();
        confirmationNumbers.add(12345L);
        contract.setConfirmationNumberList(confirmationNumbers);
        return contract;
    }

    private ESCOAPRetrieveTransactionBC createValidESCOAPRetrieveTransactionBC() {
        ESCOAPRetrieveTransactionBC contract = new ESCOAPRetrieveTransactionBC(agencyToken);
        contract.setAccountingProgramYear("2014");
        contract.setApplicationSystemCode("OY");
        contract.setTransactionRequestIdentifier("011252014125");
        return contract;
    }

    private ESCOAPTransactionResponse createSuccessfulESCOAPTransactionResponse() {
        ESCOAPTransactionResponse response = new ESCOAPTransactionResponse();
        List<Long> confirmationNumbers = new ArrayList<>();
        confirmationNumbers.add(12345L);
        response.setConfirmationNumberList(confirmationNumbers);
        response.setSuccessful(true);
        response.setEscoapErrors(new HashMap<>());
        return response;
    }

    private ESCOAPTransactionResponse createESCOAPTransactionResponseWithErrors() {
        ESCOAPTransactionResponse response = new ESCOAPTransactionResponse();
        List<Long> confirmationNumbers = new ArrayList<>();
        confirmationNumbers.add(12345L);
        response.setConfirmationNumberList(confirmationNumbers);
        response.setSuccessful(false);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("service.error.key", "Service error text");
        response.setEscoapErrors(errors);
        return response;
    }

    private FindESCOAPTransactionResponse createSuccessfulFindESCOAPTransactionResponse() {
        FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
        response.setConfirmationNumber(12345L);
        response.setAccountingTransactionDate(Calendar.getInstance());
        response.setCancelConfirmationNumber(0L);
        response.setSuccessful(true);
        response.setTransactionRequestId("011252014125");
        response.setTransactionStatusCode("ACTIVE");
        response.setEscoapErrors(new HashMap<>());
        return response;
    }

    private FindESCOAPTransactionResponse createFindESCOAPTransactionResponseWithErrors() {
        FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
        response.setConfirmationNumber(12345L);
        response.setSuccessful(false);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("service.error.key", "Service error text");
        response.setEscoapErrors(errors);
        return response;
    }

    // ===== VALIDATION TESTS FOR REQUIRED FIELDS =====

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullProgramCode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingProgramCode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullProgramTransactionCode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingProgramTransactionCode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullProgramYear() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingProgramYear(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullTransactionDate() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setAccountingTransactionDate(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullBusinessPartyId() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setBusinessPartyIdentification(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullBusinessTypeCode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setBusinessTypeCode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullStateFSACode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setStateFSACode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullCountyFSACode() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setCountyFSACode(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateTransaction_NullCustomerName() throws Exception {
        ESCOAPCreateTransactionBC contract = createValidESCOAPCreateTransactionBC();
        contract.getEscoapTransactionList().get(0).setCustomerName(null);
        escoapTransactionBS.createTransaction(contract);
    }

    @Test(expected = DLSB