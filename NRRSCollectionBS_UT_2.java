package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSDeleteCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSRetrieveCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.NRRSCollectionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.PaymentTransactionData;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CollectionServiceCommonResponse;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CreateExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.DeleteExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.NRRSRemittanceType;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionResponse;
import gov.usda.fsa.nrrs.collections.client.NRRSCollectionServiceProxy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * NRRSCollectionBS_UT_Mockito
 * 
 * Unit tests for NRRSCollectionBS using Mockito framework instead of PowerMock.
 * This version eliminates PowerMock dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class NRRSCollectionBS_UT extends DLSExternalCommonTestMockBase {

    private static final String NRRS_COLLECTION_SERVICE_JNDI = "gov/usda/fsa/fcao/flp/flpids/common/nrrs_collection_service_type";
    
    private NRRSCollectionBS nrrsCollectionBS;
    
    @Mock
    private NRRSCollectionServiceProxy mockNrrsCollectionServiceProxy;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Create the service instance and inject the mock proxy
        nrrsCollectionBS = new TestableNRRSCollectionBS(mockNrrsCollectionServiceProxy);
    }
    
    /**
     * Testable version of NRRSCollectionBS that allows mock injection
     */
    private static class TestableNRRSCollectionBS extends NRRSCollectionBS {
        private final NRRSCollectionServiceProxy mockProxy;
        
        public TestableNRRSCollectionBS(NRRSCollectionServiceProxy mockProxy) {
            this.mockProxy = mockProxy;
        }
        
        @Override
        protected NRRSCollectionServiceProxy getNRRSCollectionServiceProxy(AgencyToken agencyToken) {
            return mockProxy;
        }
    }
    
    protected AgencyToken createAgencyToken() {
        return createAgencyToken("DLMTest_User");
    }

    protected AgencyToken createAgencyToken(String inUserId) {
        AgencyToken agencyToken = new AgencyToken();
        try {
            agencyToken.setProcessingNode(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        agencyToken.setApplicationIdentifier("OY");
        agencyToken.setRequestHost("localhost");
        agencyToken.setUserIdentifier(inUserId);
        agencyToken.setReadOnly(true);
        return agencyToken;
    }

    /**
     * Test case to create Collection
     * 
     * @throws Exception
     */
    @Test
    public void testCreateExternalCollection() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);

        CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
        response.setConfirmationNumber("5555");
        response.setTransactionRequestId("12345");

        when(mockNrrsCollectionServiceProxy.createExternalCollection(any(CreateExternalCollectionContract.class)))
            .thenReturn(response);

        // Act
        NRRSCollectionResponseBO result = nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);

        // Assert
        assertNotNull("Response should not be null", result);
        assertEquals("Confirmation number should match", "5555", result.getConfirmationNumber());
        assertEquals("Transaction request ID should match", "12345", result.getTransactionRequestId());
        
        verify(mockNrrsCollectionServiceProxy, times(1)).createExternalCollection(any(CreateExternalCollectionContract.class));
    }

    /**
     * Test case to create Collection with validation errors and exception messages
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_WithErrors() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);

        CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
        List<String> validationErrors = new ArrayList<String>();
        validationErrors.add("Test validation error");
        response.setValidationErrors(validationErrors);
        
        List<String> exceptionMessages = new ArrayList<String>();
        exceptionMessages.add("Test Exception message.");
        response.setExceptionMessages(exceptionMessages);

        when(mockNrrsCollectionServiceProxy.createExternalCollection(any(CreateExternalCollectionContract.class)))
            .thenReturn(response);

        // Act - should throw DLSBCInvalidDataStopException
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to create Collection with AgencyException
     * 
     * @throws Exception
     */
    @Test(expected = DLSBusinessServiceException.class)
    public void testCreateExternalCollection_WithAgencyException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);

        when(mockNrrsCollectionServiceProxy.createExternalCollection(any(CreateExternalCollectionContract.class)))
            .thenThrow(new AgencyException("Test agency exception"));

        // Act - should throw DLSBusinessServiceException
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to create Collection with Throwable exception.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_WithThrowableException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);

        when(mockNrrsCollectionServiceProxy.createExternalCollection(any(CreateExternalCollectionContract.class)))
            .thenThrow(new DLSBCInvalidDataStopException("Test invalid data stop exception"));

        // Act - should throw DLSBCInvalidDataStopException
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    @Test
    public void testCreateCollectionWhenNRRSServiceIsUnavailable() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);

        AgencyException exception = new AgencyException("NRRS Exception: ");

        when(mockNrrsCollectionServiceProxy.createExternalCollection(any(CreateExternalCollectionContract.class)))
            .thenThrow(exception);

        // Act & Assert
        try {
            nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
            fail("Expected DLSBusinessServiceException");
        } catch (DLSBusinessServiceException ex) {
            assertEquals("Should have one error in map", 1, ex.getExtSystemErrorMap().size());
            assertTrue("Should contain external system error key", 
                      ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
        }
        
        verify(mockNrrsCollectionServiceProxy, times(1)).createExternalCollection(any(CreateExternalCollectionContract.class));
    }

    /**
     * Test case to create Collection with null contract.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_NullContract() throws Exception {
        nrrsCollectionBS.createExternalCollection(null);
    }

    /**
     * Test case to create Collection with null transaction amount.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_NullTransactionAmount() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);
        nrrsCollectionBC.getGlDataList().get(0).setTransactionAmount(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to create Collection with null transaction code.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_NullTransactionCode() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);
        nrrsCollectionBC.getGlDataList().get(0).setTransactionCode(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to create Collection with null budget fiscal year.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_NullBudgetFiscalYear() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        populateNRRSCollectionBC(nrrsCollectionBC);
        nrrsCollectionBC.getGlDataList().get(0).setBudgetFiscalYear(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to test Create Collection with invalid contract
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testCreateExternalCollection_EmptyContract() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
        // Don't populate the contract - should fail validation

        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to retrieve Collection
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveExternalCollection() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
        nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
        nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

        RetrieveExternalCollectionResponse response = new RetrieveExternalCollectionResponse();
        response.setConfirmationNumber("5555");
        response.setRemittanceType(NRRSRemittanceType.CHECK);
        response.setTransactionRequestId("12345");

        when(mockNrrsCollectionServiceProxy.retrieveExternalCollection(any(RetrieveExternalCollectionContract.class)))
            .thenReturn(response);

        // Act
        NRRSCollectionResponseBO result = nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);

        // Assert
        assertNotNull("Response should not be null", result);
        assertEquals("Confirmation number should match", "5555", result.getConfirmationNumber());
        assertEquals("Transaction request ID should match", "12345", result.getTransactionRequestId());
        
        verify(mockNrrsCollectionServiceProxy, times(1)).retrieveExternalCollection(any(RetrieveExternalCollectionContract.class));
    }

    @Test
    public void testRetrieveExternalCollectionWhenServiceIsUnavailable() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
        nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
        nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

        when(mockNrrsCollectionServiceProxy.retrieveExternalCollection(any(RetrieveExternalCollectionContract.class)))
            .thenThrow(new AgencyException("Service unavailable"));

        // Act & Assert
        try {
            nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
            fail("Expected DLSBusinessServiceException");
        } catch (DLSBusinessServiceException ex) {
            assertEquals("Should have one error in map", 1, ex.getExtSystemErrorMap().size());
            assertTrue("Should contain external system error key", 
                      ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
        }
        
        verify(mockNrrsCollectionServiceProxy, times(1)).retrieveExternalCollection(any(RetrieveExternalCollectionContract.class));
    }

    /**
     * Test case to retrieve Collection with validation errors and exception messages
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveExternalCollection_WithErrors() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
        nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
        nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

        RetrieveExternalCollectionResponse response = new RetrieveExternalCollectionResponse();
        List<String> validationErrors = new ArrayList<String>();
        validationErrors.add("Test validation error");
        response.setValidationErrors(validationErrors);
        
        List<String> exceptionMessages = new ArrayList<String>();
        exceptionMessages.add("Test exception message");
        response.setExceptionMessages(exceptionMessages);
        
        response.setConfirmationNumber("5555");
        response.setRemittanceType(NRRSRemittanceType.CHECK);

        when(mockNrrsCollectionServiceProxy.retrieveExternalCollection(any(RetrieveExternalCollectionContract.class)))
            .thenReturn(response);
            
        // Act & Assert
        try {
            nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            assertEquals("Should have 2 error messages", 2, ex.getErrorMessageList().size());
        }
        
        verify(mockNrrsCollectionServiceProxy, times(1)).retrieveExternalCollection(any(RetrieveExternalCollectionContract.class));
    }

    /**
     * Test case to retrieve Collection with Agency Exception
     * 
     * @throws Exception
     */
    @Test(expected = DLSBusinessServiceException.class)
    public void testRetrieveExternalCollection_WithAgencyException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
        nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
        nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

        when(mockNrrsCollectionServiceProxy.retrieveExternalCollection(any(RetrieveExternalCollectionContract.class)))
            .thenThrow(new AgencyException("Test agency exception"));

        // Act - should throw DLSBusinessServiceException
        nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
    }

    /**
     * Test case to retrieve Collection with null contract.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveExternalCollection_NullContract() throws Exception {
        nrrsCollectionBS.retrieveExternalCollection(null);
    }

    /**
     * Test case to retrieve Collection with null confirmation number and null
     * transaction request id.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveExternalCollection_NullConfirmationNumberAndTransactionRequestId() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
        nrrsRetrieveCollectionBC.setConfirmationNumber(null);
        nrrsRetrieveCollectionBC.setTransactionRequestId(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
    }

    /**
     * Test case to Delete Collection
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteExternalCollection() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber("14001");
        nrrsCollectionBC.setTransactionRequestSourceCode("OY");

        CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
        response.setConfirmationNumber("5555");
        response.setTransactionRequestId("12345");

        when(mockNrrsCollectionServiceProxy.deleteExternalCollection(any(DeleteExternalCollectionContract.class)))
            .thenReturn(response);

        // Act
        NRRSCollectionResponseBO result = nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);

        // Assert
        assertNotNull("Response should not be null", result);
        assertEquals("Confirmation number should match", "5555", result.getConfirmationNumber());
        assertEquals("Transaction request ID should match", "12345", result.getTransactionRequestId());
        
        verify(mockNrrsCollectionServiceProxy, times(1)).deleteExternalCollection(any(DeleteExternalCollectionContract.class));
    }

    /**
     * Test case to Delete Collection with errors
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testDeleteExternalCollection_UnsuccessfulResponse() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber("14001");
        nrrsCollectionBC.setTransactionRequestSourceCode("OY");

        CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
        List<String> validationErrors = new ArrayList<String>();
        validationErrors.add("Test validation error");
        response.setValidationErrors(validationErrors);
        
        List<String> exceptionMessages = new ArrayList<String>();
        exceptionMessages.add("Test Exception message.");
        response.setExceptionMessages(exceptionMessages);
        
        response.setConfirmationNumber("5555");
        response.setTransactionRequestId("1");

        when(mockNrrsCollectionServiceProxy.deleteExternalCollection(any(DeleteExternalCollectionContract.class)))
            .thenReturn(response);

        // Act - should throw DLSBCInvalidDataStopException
        nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to Delete Collection with DLSBCInvalidDataStopException.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testDeleteExternalCollection_DLSBCInvalidDataStopException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber("14001");
        nrrsCollectionBC.setTransactionRequestSourceCode("OY");

        when(mockNrrsCollectionServiceProxy.deleteExternalCollection(any(DeleteExternalCollectionContract.class)))
            .thenThrow(new DLSBCInvalidDataStopException("Test invalid data stop exception"));

        // Act - should throw DLSBCInvalidDataStopException
        nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to Delete Collection with AgencyException.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBusinessServiceException.class)
    public void testDeleteExternalCollection_AgencyException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber("14001");
        nrrsCollectionBC.setTransactionRequestSourceCode("OY");

        when(mockNrrsCollectionServiceProxy.deleteExternalCollection(any(DeleteExternalCollectionContract.class)))
            .thenThrow(new AgencyException("Test agency exception"));

        // Act - should throw DLSBusinessServiceException
        nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
    }

    @Test
    public void testDeleteCollectionWhenNRRSIsUnavailable() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber("14001");
        nrrsCollectionBC.setTransactionRequestSourceCode("OY");

        AgencyException exception = new AgencyException("NRRS Exception: ");

        when(mockNrrsCollectionServiceProxy.deleteExternalCollection(any(DeleteExternalCollectionContract.class)))
            .thenThrow(exception);

        // Act & Assert
        try {
            nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
            fail("Expected DLSBusinessServiceException");
        } catch (DLSBusinessServiceException ex) {
            assertEquals("Should have one error in map", 1, ex.getExtSystemErrorMap().size());
            assertTrue("Should contain external system error key", 
                      ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
        }
        
        verify(mockNrrsCollectionServiceProxy, times(1)).deleteExternalCollection(any(DeleteExternalCollectionContract.class));
    }

    /**
     * Test case to Delete Collection with null contract.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testDeleteExternalCollection_NullContract() throws Exception {
        nrrsCollectionBS.deleteExternalCollection(null);
    }

    /**
     * Test case to Delete Collection with null confirmation number.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testDeleteExternalCollection_NullConfirmationNumber() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setConfirmationNumber(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to Delete Collection with null transaction request source code.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testDeleteExternalCollection_NullTransactionRequestSourceCode() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
        nrrsCollectionBC.setTransactionRequestSourceCode(null);
        
        // Act - should throw DLSBCInvalidDataStopException during validation
        nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
    }

    /**
     * Test case to check nrrs health with invalid agency token.
     * 
     * @throws Exception
     */
    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testIsHealthy_InvalidAgencyToken() throws Exception {
        nrrsCollectionBS.isHealthy(null);
    }
    
    /**
     * Test case to check nrrs health with valid agency token.
     * 
     * @throws Exception
     */
    @Test
    public void testIsHealthy_ValidAgencyToken() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        
        when(mockNrrsCollectionServiceProxy.isHealthy()).thenReturn(true);
        
        // Act
        Boolean result = nrrsCollectionBS.isHealthy(agencyToken);
        
        // Assert
        assertTrue("Service should be healthy", result);
        verify(mockNrrsCollectionServiceProxy, times(1)).isHealthy();
    }

    // ===== HELPER METHODS =====
    
    private void populateNRRSCollectionBC(NRRSCollectionBC nrrsCollectionBC) {
        nrrsCollectionBC.setTransactionRequestSourceCode("VA");
        nrrsCollectionBC.setTransactionRequestId("022020141423");
        nrrsCollectionBC.setCoreCustomerId(Long.valueOf("1533454"));
        nrrsCollectionBC.setCustomerSourceCode("SCIMS");
        nrrsCollectionBC.setPrimaryAccountingReferenceCode("LN");
        nrrsCollectionBC.setPrimaryAccountingReferenceNumber("13");
        nrrsCollectionBC.setCollectionAmount(BigDecimal.valueOf(200.00));
        nrrsCollectionBC.setProgramCode("0210");
        nrrsCollectionBC.setProgramYear("2014");
        nrrsCollectionBC.setCommodityCode("");
        nrrsCollectionBC.setStateCode("01");
        nrrsCollectionBC.setCountyCode("125");

        List<PaymentTransactionData> glDataList = new ArrayList<PaymentTransactionData>();
        
        PaymentTransactionData paymentTransactionData1 = new PaymentTransactionData();
        paymentTransactionData1.setBudgetFiscalYear("2014");
        paymentTransactionData1.setTransactionAmount(BigDecimal.valueOf(150.00));
        paymentTransactionData1.setTransactionCode("470");
        glDataList.add(paymentTransactionData1);
        
        PaymentTransactionData paymentTransactionData2 = new PaymentTransactionData();
        paymentTransactionData2.setBudgetFiscalYear("2014");
        paymentTransactionData2.setTransactionAmount(BigDecimal.valueOf(50.00));
        paymentTransactionData2.setTransactionCode("473");
        glDataList.add(paymentTransactionData2);

        nrrsCollectionBC.setGlDataList(glDataList);

        nrrsCollectionBC.setRemittanceType("CHECK");
        nrrsCollectionBC.setOfficeID(Long.valueOf(105917));
        nrrsCollectionBC.setRemittanceAmount(BigDecimal.valueOf(200.00));
        nrrsCollectionBC.setRemitterName("Smith Nelson");
        nrrsCollectionBC.setEffectiveDate(Calendar.getInstance().getTime());
        nrrsCollectionBC.setCheckNumber("12345");
        nrrsCollectionBC.setObligationConfirmationNumber(new BigInteger("1"));
    }
}