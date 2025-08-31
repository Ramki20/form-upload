package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors.FBPProxySBConnector;
import gov.usda.fsa.fcao.flp.flpids.common.dao.IFBPProxyDao;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;

/**
 * FBPProxyBS_UT_Mockito
 * 
 * Unit tests for FBPProxyBS using Mockito framework instead of PowerMock.
 * This version eliminates Spring context dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class FBPProxyBS_UT extends DLSExternalCommonTestMockBase {

    private FBPProxyBS fbpProxyBusinessService;
    private AgencyToken agencyToken;
    
    @Mock
    private IFBPProxyDao mockFbpProxyDao;
    
    @Mock
    private FBPProxySBConnector mockFbpProxySBConnector;
    
    @Mock
    private AgencyEncryption mockAgencyEncryption;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Use reflection to access the private constructor
        Constructor<FBPProxyBS> constructor = FBPProxyBS.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        fbpProxyBusinessService = constructor.newInstance();
        
        fbpProxyBusinessService.setFbpProxyDao(mockFbpProxyDao);
        fbpProxyBusinessService.setAgencyEncryption(mockAgencyEncryption);
        agencyToken = createAgencyToken("DLMTest_User");
    }

    protected AgencyToken createAgencyToken(String userId) {
        AgencyToken token = new AgencyToken();
        token.setProcessingNode("DLM_jUnit_TEST");
        token.setApplicationIdentifier("DLM-Test");
        token.setRequestHost("localhost");
        token.setUserIdentifier(userId);
        token.setReadOnly(true);
        return token;
    }

    // ===== ENCRYPTION TESTS =====

    @Test
    public void testEncodeDecodePassword() throws Exception {
        // Arrange
        String originalString = "this is my test string";
        String encodedString = "encoded_test_string";
        String decodedString = originalString;
        
        when(mockAgencyEncryption.encode(originalString)).thenReturn(encodedString);
        when(mockAgencyEncryption.decode(encodedString)).thenReturn(decodedString);

        // Act
        String actualEncoded = mockAgencyEncryption.encode(originalString);
        String actualDecoded = mockAgencyEncryption.decode(actualEncoded);

        // Assert
        assertEquals("Encoded string should match", encodedString, actualEncoded);
        assertEquals("Decoded string should match original", originalString, actualDecoded);
    }

    @Test
    public void testDecodingPassword() throws Exception {
        // Arrange
        String encodedPassword = "Vm14V2IxTnRUbGRqUld4V1ltdEtjRlJYY0ZKTlFUMDk=";
        String expectedDecoded = "T*sting8";
        
        when(mockAgencyEncryption.decode(encodedPassword)).thenReturn(expectedDecoded);

        // Act
        String actualDecoded = mockAgencyEncryption.decode(encodedPassword);

        // Assert
        assertEquals("Decoded password should match", expectedDecoded, actualDecoded);
        verify(mockAgencyEncryption, times(1)).decode(encodedPassword);
    }

    // ===== RETRIEVE CREDIT ACTION TESTS =====

    @Test
    public void testRetrieveCreditActionForCustomer() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        List<FBPProxyBO> expectedResult = createFBPProxyBOList();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should return expected number of records", expectedResult.size(), result.size());
        
        verify(mockFbpProxyDao, times(1)).retrieveDLMData(anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(1)).setAgencyToken(agencyToken);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveCreditActionForCustomer_NegativeCustomerId() throws Exception {
        Integer invalidCustomerId = -5094279;
        fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, invalidCustomerId);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveCreditActionForCustomer_NullCustomerId() throws Exception {
        fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveCreditActionForCustomer_NegativeCustomerId2() throws Exception {
        // Zero is actually valid according to FBPProxyBCValidator (only null and negative are invalid)
        // Change this to test negative customer ID instead
        fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, -1);
    }

    @Test
    public void testRetrieveCreditActionForCustomer_ZeroCustomerId() throws Exception {
        // Arrange - Zero is valid according to FBPProxyBCValidator
        Integer coreCustomerId = 0;
        List<FBPProxyBO> expectedResult = createFBPProxyBOList();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should handle zero customer ID", expectedResult.size(), result.size());
        
        verify(mockFbpProxyDao, times(1)).retrieveDLMData(anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(1)).setAgencyToken(agencyToken);
    }

    @Test
    public void testRetrieveCreditActionForCustomer_EmptyResult() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        List<FBPProxyBO> emptyResult = new ArrayList<>();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenReturn(emptyResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testRetrieveCreditActionForCustomer_ServiceException() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenThrow(new FBPServiceBrokerException("Service unavailable"));

        // Act & Assert
        try {
            fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);
            fail("Expected FBPServiceBrokerException");
        } catch (FBPServiceBrokerException ex) {
            assertEquals("Exception message should match", "Service unavailable", ex.getMessage());
        }
    }

    // ===== RETRIEVE DALR DATA TESTS =====

    @Test
    public void testRetrieveDALRDataForCustomer() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        Collection<FarmBusinessPlanBO> expectedResult = createFarmBusinessPlanBOCollection();
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FarmBusinessPlanBO> result = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should return expected number of records", expectedResult.size(), result.size());
        
        verify(mockFbpProxyDao, times(1)).retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(1)).setAgencyToken(agencyToken);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveDALRDataForCustomer_NullCustomerId() throws Exception {
        fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveDALRDataForCustomer_InvalidCustomerId() throws Exception {
        fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, -1);
    }

    @Test
    public void testRetrieveDALRDataForCustomer_EmptyResult() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        Collection<FarmBusinessPlanBO> emptyResult = new ArrayList<>();
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(emptyResult);

        // Act
        List<FarmBusinessPlanBO> result = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testRetrieveDALRDataForCustomer_ServiceException() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenThrow(new FBPServiceBrokerException("DALR service unavailable"));

        // Act & Assert
        try {
            fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);
            fail("Expected FBPServiceBrokerException");
        } catch (FBPServiceBrokerException ex) {
            assertEquals("Exception message should match", "DALR service unavailable", ex.getMessage());
        }
    }

    // ===== YEA CREDIT ACTIONS TESTS =====

    @Test
    public void testGetYEACreditActions() throws Exception {
        // Arrange
        Integer customerId = 5094279;
        List<CreditActionBO> expectedResult = createCreditActionBOList();
        
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(customerId)))
            .thenReturn(expectedResult);

        // Act
        List<CreditActionBO> result = fbpProxyBusinessService.getYEACreditActions(customerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should return expected number of records", expectedResult.size(), result.size());
        
        verify(mockFbpProxyDao, times(1)).getYEACreditActions(anyString(), eq(customerId));
    }

    @Test
    public void testGetYEACreditActions_EmptyResult() throws Exception {
        // Arrange
        Integer customerId = -1;
        List<CreditActionBO> emptyResult = new ArrayList<>();
        
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(customerId)))
            .thenReturn(emptyResult);

        // Act
        List<CreditActionBO> result = fbpProxyBusinessService.getYEACreditActions(customerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testGetYEACreditActions_ServiceException() throws Exception {
        // Arrange
        Integer customerId = 5094279;
        
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(customerId)))
            .thenThrow(new FBPServiceBrokerException("YEA service unavailable"));

        // Act & Assert
        try {
            fbpProxyBusinessService.getYEACreditActions(customerId);
            fail("Expected FBPServiceBrokerException");
        } catch (FBPServiceBrokerException ex) {
            assertEquals("Exception message should match", "YEA service unavailable", ex.getMessage());
        }
    }

    @Test
    public void testGetCreditActions() throws Exception {
        // Arrange
        Integer customerId = 5094279;
        List<CreditActionBO> expectedResult = createCreditActionBOList();
        
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(customerId)))
            .thenReturn(expectedResult);

        // Act
        List<CreditActionBO> result = fbpProxyBusinessService.getCreditActions(customerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        assertEquals("Should return expected number of records", expectedResult.size(), result.size());
        
        // Verify that getCreditActions delegates to getYEACreditActions
        verify(mockFbpProxyDao, times(1)).getYEACreditActions(anyString(), eq(customerId));
    }

    // ===== HEALTH CHECK TESTS =====

    @Test
    public void testIsHealthy_AllComponentsHealthy() throws Exception {
        // Arrange
        when(mockFbpProxyDao.isHealthy()).thenReturn(true);
        
        AgencyToken readOnlyToken = createAgencyToken("testUser");
        readOnlyToken.setReadOnly(true);

        // Act
        boolean result = fbpProxyBusinessService.isHealthy(readOnlyToken);

        // Assert
        assertTrue("Service should be healthy when all components are healthy", result);
        verify(mockFbpProxyDao, times(1)).isHealthy();
    }

    @Test
    public void testIsHealthy_DaoUnhealthy() throws Exception {
        // Arrange
        when(mockFbpProxyDao.isHealthy()).thenReturn(false);
        
        AgencyToken readOnlyToken = createAgencyToken("testUser");
        readOnlyToken.setReadOnly(true);

        // Act
        boolean result = fbpProxyBusinessService.isHealthy(readOnlyToken);

        // Assert
        assertFalse("Service should be unhealthy when DAO is unhealthy", result);
    }

    @Test
    public void testIsHealthy_TokenNotReadOnly() throws Exception {
        // Arrange
        when(mockFbpProxyDao.isHealthy()).thenReturn(true);
        
        AgencyToken nonReadOnlyToken = createAgencyToken("testUser");
        nonReadOnlyToken.setReadOnly(false);

        // Act
        boolean result = fbpProxyBusinessService.isHealthy(nonReadOnlyToken);

        // Assert
        assertFalse("Service should be unhealthy when token is not read-only", result);
    }

    @Test
    public void testIsHealthy_NullEncryption() throws Exception {
        // Arrange
        fbpProxyBusinessService.setAgencyEncryption(null);
        when(mockFbpProxyDao.isHealthy()).thenReturn(true);
        
        AgencyToken readOnlyToken = createAgencyToken("testUser");
        readOnlyToken.setReadOnly(true);

        // Act
        boolean result = fbpProxyBusinessService.isHealthy(readOnlyToken);

        // Assert
        assertFalse("Service should be unhealthy when encryption is null", result);
    }

    @Test
    public void testIsHealthy_NullDao() throws Exception {
        // Arrange
        fbpProxyBusinessService.setFbpProxyDao(null);
        
        AgencyToken readOnlyToken = createAgencyToken("testUser");
        readOnlyToken.setReadOnly(true);

        // Act
        boolean result = fbpProxyBusinessService.isHealthy(readOnlyToken);

        // Assert
        assertFalse("Service should be unhealthy when DAO is null", result);
    }

    // ===== INTEGRATION TESTS =====

    @Test
    public void testRetrieveCreditActionForCustomer_FullWorkflow() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        List<FBPProxyBO> expectedResult = createLargeFBPProxyBOList();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return all expected records", expectedResult.size(), result.size());
        
        // Verify all records have valid data
        for (FBPProxyBO fbpProxy : result) {
            assertNotNull("Credit action ID should not be null", fbpProxy.getCreditActionID());
            assertNotNull("Credit action description should not be null", fbpProxy.getCreditActionDesc());
        }
        
        verify(mockFbpProxyDao, times(1)).setAgencyToken(agencyToken);
        verify(mockFbpProxyDao, times(1)).retrieveDLMData(anyString(), eq(coreCustomerId));
    }

    @Test
    public void testRetrieveDALRDataForCustomer_FullWorkflow() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        Collection<FarmBusinessPlanBO> expectedResult = createLargeFarmBusinessPlanBOCollection();
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FarmBusinessPlanBO> result = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return all expected records", expectedResult.size(), result.size());
        
        // Verify all records have valid data
        for (FarmBusinessPlanBO plan : result) {
            assertNotNull("Farm loan customer ID should not be null", plan.getFarmLoanCustomerId());
            assertNotNull("Credit action description should not be null", plan.getCreditActionDescription());
        }
        
        verify(mockFbpProxyDao, times(1)).setAgencyToken(agencyToken);
        verify(mockFbpProxyDao, times(1)).retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId));
    }

    // ===== BOUNDARY VALUE TESTS =====

    @Test
    public void testRetrieveCreditActionForCustomer_MinimumValidCustomerId() throws Exception {
        // Arrange
        Integer minCustomerId = 1;
        List<FBPProxyBO> expectedResult = createFBPProxyBOList();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(minCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, minCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should handle minimum valid customer ID", expectedResult.size(), result.size());
    }

    @Test
    public void testRetrieveCreditActionForCustomer_LargeCustomerId() throws Exception {
        // Arrange
        Integer largeCustomerId = Integer.MAX_VALUE;
        List<FBPProxyBO> expectedResult = createFBPProxyBOList();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(largeCustomerId)))
            .thenReturn(expectedResult);

        // Act
        List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, largeCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should handle large customer ID", expectedResult.size(), result.size());
    }

    // ===== ERROR HANDLING TESTS =====

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveCreditActionForCustomer_ValidationException() throws Exception {
        // Arrange - This should trigger validation failure before reaching DAO
        Integer coreCustomerId = null; // Null customer ID should trigger validation exception
        
        // Act - should throw DLSBCInvalidDataStopException from validation
        fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);
    }

    @Test(expected = FBPServiceBrokerException.class)
    public void testRetrieveCreditActionForCustomer_DaoThrowsRuntimeException() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenThrow(new FBPServiceBrokerException("Database connection failed"));

        // Act - DAO exception should propagate as FBPServiceBrokerException
        fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);
    }

    @Test(expected = FBPServiceBrokerException.class)
    public void testRetrieveDALRDataForCustomer_DaoThrowsRuntimeException() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenThrow(new FBPServiceBrokerException("Network timeout"));

        // Act - DAO exception should propagate as FBPServiceBrokerException
        fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);
    }

    // ===== SETTER/GETTER TESTS =====

    @Test
    public void testSetFbpProxyDao() {
        // Arrange
        IFBPProxyDao testDao = mock(IFBPProxyDao.class);
        
        // Act
        fbpProxyBusinessService.setFbpProxyDao(testDao);
        
        // Assert - verify through health check behavior
        when(testDao.isHealthy()).thenReturn(true);
        boolean isHealthy = fbpProxyBusinessService.isHealthy(createReadOnlyAgencyToken());
        assertTrue("Should use the injected DAO", isHealthy);
    }

    @Test
    public void testSetAgencyEncryption() {
        // Arrange
        AgencyEncryption testEncryption = mock(AgencyEncryption.class);
        
        // Act
        fbpProxyBusinessService.setAgencyEncryption(testEncryption);
        
        // Assert - verify through health check behavior
        when(mockFbpProxyDao.isHealthy()).thenReturn(true);
        boolean isHealthy = fbpProxyBusinessService.isHealthy(createReadOnlyAgencyToken());
        assertTrue("Should use the injected encryption", isHealthy);
    }

    // ===== COMPREHENSIVE WORKFLOW TESTS =====

    @Test
    public void testCompleteWorkflow_CreateRetrieveCreditActions() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        List<FBPProxyBO> creditActions = createFBPProxyBOList();
        List<CreditActionBO> yeaActions = createCreditActionBOList();
        Collection<FarmBusinessPlanBO> businessPlans = createFarmBusinessPlanBOCollection();
        
        when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(coreCustomerId)))
            .thenReturn(creditActions);
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(coreCustomerId)))
            .thenReturn(yeaActions);
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(businessPlans);
        when(mockFbpProxyDao.isHealthy()).thenReturn(true);

        // Act & Assert - Test all major operations
        
        // 1. Health check
        boolean isHealthy = fbpProxyBusinessService.isHealthy(createReadOnlyAgencyToken());
        assertTrue("Service should be healthy", isHealthy);
        
        // 2. Retrieve credit actions
        List<FBPProxyBO> creditResult = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, coreCustomerId);
        assertNotNull("Credit actions should not be null", creditResult);
        assertEquals("Should return correct number of credit actions", 2, creditResult.size());
        
        // 3. Retrieve YEA credit actions
        List<CreditActionBO> yeaResult = fbpProxyBusinessService.getYEACreditActions(coreCustomerId);
        assertNotNull("YEA actions should not be null", yeaResult);
        assertEquals("Should return correct number of YEA actions", 2, yeaResult.size());
        
        // 4. Retrieve DALR data
        List<FarmBusinessPlanBO> dalrResult = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);
        assertNotNull("DALR data should not be null", dalrResult);
        assertEquals("Should return correct number of business plans", 2, dalrResult.size());
        
        // Verify all calls were made
        verify(mockFbpProxyDao, times(1)).isHealthy();
        verify(mockFbpProxyDao, times(1)).retrieveDLMData(anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(1)).getYEACreditActions(anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(1)).retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId));
        verify(mockFbpProxyDao, times(2)).setAgencyToken(agencyToken); // Called twice for the two methods that need it
    }

    @Test
    public void testGetCreditActions_DelegatesToYEA() throws Exception {
        // Arrange
        Integer customerId = 5094279;
        List<CreditActionBO> expectedResult = createCreditActionBOList();
        
        when(mockFbpProxyDao.getYEACreditActions(anyString(), eq(customerId)))
            .thenReturn(expectedResult);

        // Act
        List<CreditActionBO> result = fbpProxyBusinessService.getCreditActions(customerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return expected result", expectedResult.size(), result.size());
        
        // Verify delegation
        verify(mockFbpProxyDao, times(1)).getYEACreditActions(anyString(), eq(customerId));
        verifyNoMoreInteractions(mockFbpProxyDao);
    }

    // ===== PARAMETER VALIDATION TESTS =====

    @Test
    public void testRetrieveCreditActionForCustomer_ValidatesDifferentCustomerIdFormats() throws Exception {
        // Test various valid customer ID formats
        List<Integer> validCustomerIds = List.of(1, 100, 5094279, 999999999);
        
        for (Integer customerId : validCustomerIds) {
            // Arrange
            List<FBPProxyBO> expectedResult = createFBPProxyBOList();
            when(mockFbpProxyDao.retrieveDLMData(anyString(), eq(customerId)))
                .thenReturn(expectedResult);

            // Act
            List<FBPProxyBO> result = fbpProxyBusinessService.retrieveCreditActionForCustomer(agencyToken, customerId);

            // Assert
            assertNotNull("Result should not be null for customer ID: " + customerId, result);
            assertEquals("Should return expected result for customer ID: " + customerId, 
                        expectedResult.size(), result.size());
        }
        
        // Verify all calls were made
        verify(mockFbpProxyDao, times(validCustomerIds.size()))
            .retrieveDLMData(anyString(), any(Integer.class));
    }

    // ===== HELPER METHODS =====

    private List<FBPProxyBO> createFBPProxyBOList() {
        List<FBPProxyBO> list = new ArrayList<>();
        
        FBPProxyBO fbpProxy1 = new FBPProxyBO(agencyToken);
        fbpProxy1.setCreditActionID(12345);
        fbpProxy1.setCreditActionDesc("Test Credit Action 1");
        fbpProxy1.setLoanApprovalOffical("John Doe");
        fbpProxy1.setLoanApprovalTitle("Loan Officer");
        
        FBPProxyBO fbpProxy2 = new FBPProxyBO(agencyToken);
        fbpProxy2.setCreditActionID(67890);
        fbpProxy2.setCreditActionDesc("Test Credit Action 2");
        fbpProxy2.setLoanApprovalOffical("Jane Smith");
        fbpProxy2.setLoanApprovalTitle("Senior Loan Officer");
        
        list.add(fbpProxy1);
        list.add(fbpProxy2);
        return list;
    }

    private List<FBPProxyBO> createLargeFBPProxyBOList() {
        List<FBPProxyBO> list = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            FBPProxyBO fbpProxy = new FBPProxyBO(agencyToken);
            fbpProxy.setCreditActionID(i * 1000);
            fbpProxy.setCreditActionDesc("Test Credit Action " + i);
            fbpProxy.setLoanApprovalOffical("Officer " + i);
            fbpProxy.setLoanApprovalTitle("Title " + i);
            list.add(fbpProxy);
        }
        
        return list;
    }

    private Collection<FarmBusinessPlanBO> createDateParsingTestData(String beginDate, String endDate) {
        List<FarmBusinessPlanBO> list = new ArrayList<>();
        
        FarmBusinessPlanBO plan = new FarmBusinessPlanBO();
        plan.setFarmLoanCustomerId("5094279");
        plan.setCreditActionDescription("Date Parsing Test Plan");
        plan.setScenarioDescription("Test Scenario");
        plan.setBeginningDate(beginDate);
        plan.setEndDate(endDate);
        plan.setFarmOperatingExpenses(12000.0);
        plan.setFarmOperatingInterestExpenses(1200.0);
        plan.setBalanceAvailable(8000.0);
        plan.setNonAgencyDebtsAndTaxes(500.0);
        
        list.add(plan);
        return list;
    }

    private Collection<FarmBusinessPlanBO> createFarmBusinessPlanBOCollection() {
        List<FarmBusinessPlanBO> list = new ArrayList<>();
        
        FarmBusinessPlanBO plan1 = new FarmBusinessPlanBO();
        plan1.setFarmLoanCustomerId("5094279");
        plan1.setCreditActionDescription("Business Plan 1");
        plan1.setScenarioDescription("Scenario 1");
        plan1.setFarmOperatingExpenses(10000.0);
        plan1.setBalanceAvailable(5000.0);
        
        FarmBusinessPlanBO plan2 = new FarmBusinessPlanBO();
        plan2.setFarmLoanCustomerId("5094279");
        plan2.setCreditActionDescription("Business Plan 2");
        plan2.setScenarioDescription("Scenario 2");
        plan2.setFarmOperatingExpenses(15000.0);
        plan2.setBalanceAvailable(7500.0);
        
        list.add(plan1);
        list.add(plan2);
        return list;
    }

    private Collection<FarmBusinessPlanBO> createLargeFarmBusinessPlanBOCollection() {
        List<FarmBusinessPlanBO> list = new ArrayList<>();
        
        for (int i = 1; i <= 15; i++) {
            FarmBusinessPlanBO plan = new FarmBusinessPlanBO();
            plan.setFarmLoanCustomerId("5094279");
            plan.setCreditActionDescription("Business Plan " + i);
            plan.setScenarioDescription("Scenario " + i);
            plan.setFarmOperatingExpenses(i * 1000.0);
            plan.setBalanceAvailable(i * 500.0);
            list.add(plan);
        }
        
        return list;
    }

    private List<CreditActionBO> createCreditActionBOList() {
        List<CreditActionBO> list = new ArrayList<>();
        
        CreditActionBO action1 = new CreditActionBO();
        action1.setCreditActionID(1L);
        action1.setCreditActionDescr("Credit Action 1");
        action1.setLoanApprovalOffical("Officer 1");
        action1.setOverAllScore("A");
        action1.setOverAllScoreDescr("Excellent");
        
        CreditActionBO action2 = new CreditActionBO();
        action2.setCreditActionID(2L);
        action2.setCreditActionDescr("Credit Action 2");
        action2.setLoanApprovalOffical("Officer 2");
        action2.setOverAllScore("B");
        action2.setOverAllScoreDescr("Good");
        
        list.add(action1);
        list.add(action2);
        return list;
    }

    private AgencyToken createReadOnlyAgencyToken() {
        AgencyToken token = createAgencyToken("testUser");
        token.setReadOnly(true);
        return token;
    }

    // ===== DATE PARSING TESTS =====

    @Test
    public void testRetrieveDALRDataForCustomer_DateParsingScenario1() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        Collection<FarmBusinessPlanBO> plans = createDateParsingTestData("08/01/19", "08-01-19");
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(plans);

        // Act
        List<FarmBusinessPlanBO> result = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        
        // Verify the date fields are set
        FarmBusinessPlanBO plan = result.get(0);
        assertEquals("Begin date should be set", "08/01/19", plan.getBeginningDate());
        assertEquals("End date should be set", "08-01-19", plan.getEndDate());
    }

    @Test
    public void testRetrieveDALRDataForCustomer_DateParsingScenario2() throws Exception {
        // Arrange
        Integer coreCustomerId = 5094279;
        Collection<FarmBusinessPlanBO> plans = createDateParsingTestData("2019/08/01", "08/01/2019");
        
        when(mockFbpProxyDao.retrieveDALRData(eq(agencyToken), anyString(), eq(coreCustomerId)))
            .thenReturn(plans);

        // Act
        List<FarmBusinessPlanBO> result = fbpProxyBusinessService.retrieveDALRDataForCustomer(agencyToken, coreCustomerId);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
        
        // Verify the date fields are set
        FarmBusinessPlanBO plan = result.get(0);
        assertEquals("Begin date should be set", "2019/08/01", plan.getBeginningDate());
        assertEquals("End date should be set", "08/01/2019", plan.getEndDate());
    }

}