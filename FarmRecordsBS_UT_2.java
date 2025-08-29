package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FarmRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.parmo.farmrecords.dto.FarmDTO;
import gov.usda.fsa.parmo.farmrecords.dto.FarmResultDTO;
import gov.usda.fsa.parmo.frs.ejb.client.contract.RetrieveFarmsServiceContractWrapper;
import gov.usda.fsa.parmo.frs.ejb.client.reply.RetrieveFarmsResultWrapper;
import gov.usda.fsa.parmo.frs.ejb.service.FarmRecordsExternalService;

import java.util.List;

import javax.naming.ServiceUnavailableException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * FarmRecordsBS_UT_Mockito
 * 
 * Unit tests for FarmRecordBS using Mockito framework instead of PowerMock.
 * This version provides cleaner mocking without PowerMock dependencies.
 * 
 * @author Generated
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class FarmRecordsBS_UT_Mockito extends DLSExternalCommonTestMockBase {

    private FarmRecordBS farmRecordBS;
    private AgencyToken agencyToken;
    
    @Mock
    private FarmRecordsExternalService mockFarmRecordsService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        farmRecordBS = new FarmRecordBS();
        farmRecordBS.setFarmRecordsServiceBean(mockFarmRecordsService);
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

    // ===== HAPPY PATH TESTS =====

    @Test
    public void testRetrieveFarmRecordByCustomer() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createSuccessfulMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertFalse("Farm response list should not be empty", result.isEmpty());
            assertEquals("Should return 2 farm records", 2, result.size());
            
            // Verify first farm record
            FarmResponseBO firstFarm = result.get(0);
            assertEquals("Farm number should match", "12345", firstFarm.getFarmNumber());
            assertEquals("State code should match", "IA", firstFarm.getAdminStateCode());
            assertEquals("County code should match", "001", firstFarm.getAdminCountyCode());
            
            // Verify second farm record
            FarmResponseBO secondFarm = result.get(1);
            assertEquals("Farm number should match", "67890", secondFarm.getFarmNumber());
            assertEquals("State code should match", "IL", secondFarm.getAdminStateCode());
            assertEquals("County code should match", "002", secondFarm.getAdminCountyCode());
            
            // Verify service was called
            verify(mockFarmRecordsService, times(1))
                .retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class));
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_EmptyResults() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createEmptyMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertTrue("Farm response list should be empty", result.isEmpty());
            
            verify(mockFarmRecordsService, times(1))
                .retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class));
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_MultiplePages() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper firstPageWrapper = createFirstPageMockWrapper();
        RetrieveFarmsResultWrapper secondPageWrapper = createSecondPageMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(firstPageWrapper)
                .thenReturn(secondPageWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertEquals("Should return 3 farm records from 2 pages", 3, result.size());
            
            // Verify service was called twice (pagination)
            verify(mockFarmRecordsService, times(2))
                .retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class));
        }
    }

    // ===== VALIDATION TESTS =====

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveFarmRecordByCustomer_NullContract() throws Exception {
        farmRecordBS.retrieveFarmRecordByCustomer(null);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveFarmRecordByCustomer_NullAgencyToken() throws Exception {
        FarmRequestBC contract = new FarmRequestBC(null);
        contract.setCoreCustomerId(9680545L);
        contract.setYear(2017);
        farmRecordBS.retrieveFarmRecordByCustomer(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveFarmRecordByCustomer_NullCustomerId() throws Exception {
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(null);
        contract.setYear(2017);
        farmRecordBS.retrieveFarmRecordByCustomer(contract);
    }

    @Test(expected = DLSBCInvalidDataStopException.class)
    public void testRetrieveFarmRecordByCustomer_NullYear() throws Exception {
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(9680545L);
        contract.setYear(null);
        farmRecordBS.retrieveFarmRecordByCustomer(contract);
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_InvalidData() throws Exception {
        // Arrange
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(9680545L);
        contract.setYear(2017);
        // Set invalid boolean values to null
        contract.setIncludeCustomerWithResponse(null);
        contract.setIncludeCropWithResponse(null);
        contract.setIncludeNonActiveCustomerWithResponse(null);
        contract.setIncludeTractInfoWithResponse(null);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            assertTrue("Should have validation errors", ex.getErrorMessageList().size() > 0);
        }
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    public void testRetrieveFarmRecordByCustomer_ServiceException() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createMockWrapperWithError();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act & Assert
            try {
                farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertTrue("Exception message should contain service error",
                          ex.getMessage().contains("Invalid Data received from Farm Record Services"));
                assertNotNull("Should have a cause", ex.getCause());
                assertTrue("Cause should be ServiceUnavailableException", 
                          ex.getCause() instanceof ServiceUnavailableException);
            }
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_ServiceThrowsException() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

            // Act & Assert
            try {
                farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertTrue("Exception message should contain error info",
                          ex.getMessage().contains("Invalid Data received from Farm Record Services"));
                assertEquals("Cause message should match", "Connection timeout", ex.getCause().getMessage());
            }
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_NullFarmInResults() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createMockWrapperWithNullFarm();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertEquals("Should return 1 valid farm (null farm filtered out)", 1, result.size());
            assertEquals("Farm number should match", "12345", result.get(0).getFarmNumber());
        }
    }

    // ===== EDGE CASE TESTS =====

    @Test
    public void testRetrieveFarmRecordByCustomer_ValidBoundaryValues() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        farmRequestBC.setCoreCustomerId(1L); // Minimum valid ID
        farmRequestBC.setYear(1776); // Minimum valid year
        farmRequestBC.setLowerBoundFarmNumber("00001");
        
        RetrieveFarmsResultWrapper mockWrapper = createSuccessfulMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertEquals("Should return 2 farm records", 2, result.size());
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_AllIncludeFlags() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        farmRequestBC.setIncludeCustomerWithResponse(true);
        farmRequestBC.setIncludeCropWithResponse(true);
        farmRequestBC.setIncludeNonActiveCustomerWithResponse(true);
        farmRequestBC.setIncludeTractInfoWithResponse(true);
        
        RetrieveFarmsResultWrapper mockWrapper = createSuccessfulMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertEquals("Should return 2 farm records", 2, result.size());
            
            // Verify the contract was set up correctly
            verify(mockFarmRecordsService).retrieveFarms(argThat(contract -> 
                contract.isIncludeCustomer() && 
                contract.isIncludeCrop() && 
                contract.isIncludeNonActiveCustomer() && 
                contract.isIncludeTract()
            ));
        }
    }

    // ===== INTEGRATION TESTS =====

    @Test
    public void testRetrieveFarmRecordByCustomer_FullWorkflow() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createSuccessfulMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Result should not be null", result);
            assertEquals("Should have correct number of farms", 2, result.size());
            
            // Verify contract mapping
            verify(mockFarmRecordsService).retrieveFarms(argThat(contract -> 
                contract.getCustomerId() == 9680545 &&
                contract.getYear() == 2017 &&
                contract.isIncludeCustomer() == false &&
                contract.isIncludeCrop() == false &&
                contract.isIncludeNonActiveCustomer() == false &&
                contract.isIncludeTract() == false
            ));
        }
    }

    // ===== HELPER METHODS =====

    private FarmRequestBC createValidFarmRequestBC() {
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(9680545L);
        contract.setYear(2017);
        contract.setIncludeCustomerWithResponse(false);
        contract.setIncludeCropWithResponse(false);
        contract.setIncludeNonActiveCustomerWithResponse(false);
        contract.setIncludeTractInfoWithResponse(false);
        contract.setLowerBoundFarmNumber(null);
        return contract;
    }

    private RetrieveFarmsResultWrapper createSuccessfulMockWrapper() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        // Create sample farm data
        FarmDTO farm1 = new FarmDTO();
        farm1.setNumber("12345");
        farm1.setAdminStateCode("IA");
        farm1.setAdminCountyCode("001");
        farm1.setIdentifier(1L);
        farm1.setDescription("Test Farm 1");
        
        FarmDTO farm2 = new FarmDTO();
        farm2.setNumber("67890");
        farm2.setAdminStateCode("IL");
        farm2.setAdminCountyCode("002");
        farm2.setIdentifier(2L);
        farm2.setDescription("Test Farm 2");
        
        FarmDTO[] farmArray = {farm1, farm2};
        result.setFarmList(farmArray);
        result.setEndOfListIndicator(true);
        result.setExceptionMessage(null); // No errors
        
        wrapper.setResult(result);
        return wrapper;
    }

    private RetrieveFarmsResultWrapper createEmptyMockWrapper() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        result.setFarmList(new FarmDTO[0]); // Empty array
        result.setEndOfListIndicator(true);
        result.setExceptionMessage(null);
        
        wrapper.setResult(result);
        return wrapper;
    }

    private RetrieveFarmsResultWrapper createMockWrapperWithError() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        result.setFarmList(new FarmDTO[0]);
        result.setEndOfListIndicator(true);
        result.setExceptionMessage("Service temporarily unavailable");
        
        wrapper.setResult(result);
        return wrapper;
    }

    private RetrieveFarmsResultWrapper createMockWrapperWithNullFarm() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        // Create array with one valid farm and one null
        FarmDTO validFarm = new FarmDTO();
        validFarm.setNumber("12345");
        validFarm.setAdminStateCode("IA");
        validFarm.setAdminCountyCode("001");
        
        FarmDTO[] farmArray = {validFarm, null};
        result.setFarmList(farmArray);
        result.setEndOfListIndicator(true);
        result.setExceptionMessage(null);
        
        wrapper.setResult(result);
        return wrapper;
    }

    private RetrieveFarmsResultWrapper createFirstPageMockWrapper() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        FarmDTO farm1 = new FarmDTO();
        farm1.setNumber("11111");
        farm1.setAdminStateCode("IA");
        farm1.setAdminCountyCode("001");
        
        FarmDTO farm2 = new FarmDTO();
        farm2.setNumber("22222");
        farm2.setAdminStateCode("IL");
        farm2.setAdminCountyCode("002");
        
        FarmDTO[] farmArray = {farm1, farm2};
        result.setFarmList(farmArray);
        result.setEndOfListIndicator(false); // More pages available
        result.setExceptionMessage(null);
        
        wrapper.setResult(result);
        return wrapper;
    }

    private RetrieveFarmsResultWrapper createSecondPageMockWrapper() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        FarmDTO farm3 = new FarmDTO();
        farm3.setNumber("33333");
        farm3.setAdminStateCode("KS");
        farm3.setAdminCountyCode("003");
        
        FarmDTO[] farmArray = {farm3};
        result.setFarmList(farmArray);
        result.setEndOfListIndicator(true); // Last page
        result.setExceptionMessage(null);
        
        wrapper.setResult(result);
        return wrapper;
    }

    // ===== BOUNDARY AND EDGE CASE TESTS =====

    @Test
    public void testRetrieveFarmRecordByCustomer_ZeroCustomerId() throws Exception {
        // Arrange
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(0L);
        contract.setYear(2017);
        contract.setIncludeCustomerWithResponse(false);
        contract.setIncludeCropWithResponse(false);
        contract.setIncludeNonActiveCustomerWithResponse(false);
        contract.setIncludeTractInfoWithResponse(false);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            assertTrue("Should have validation errors for zero customer ID", 
                      ex.getErrorMessageList().size() > 0);
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_InvalidYear() throws Exception {
        // Arrange
        FarmRequestBC contract = new FarmRequestBC(agencyToken);
        contract.setCoreCustomerId(9680545L);
        contract.setYear(1775); // Before valid range
        contract.setIncludeCustomerWithResponse(false);
        contract.setIncludeCropWithResponse(false);
        contract.setIncludeNonActiveCustomerWithResponse(false);
        contract.setIncludeTractInfoWithResponse(false);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            assertTrue("Should have validation errors for invalid year", 
                      ex.getErrorMessageList().size() > 0);
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_ServiceReturnsNull() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(null);

            // Act & Assert
            try {
                farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);
                fail("Expected DLSBCInvalidDataStopException");
            } catch (DLSBCInvalidDataStopException ex) {
                assertTrue("Exception should be caught and wrapped", 
                          ex.getMessage().contains("Invalid Data received from Farm Record Services"));
            }
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomer_LargeDataSet() throws Exception {
        // Arrange
        FarmRequestBC farmRequestBC = createValidFarmRequestBC();
        RetrieveFarmsResultWrapper mockWrapper = createLargeDataSetMockWrapper();
        
        try (MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
            tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any()))
                           .thenReturn(agencyToken);
            
            when(mockFarmRecordsService.retrieveFarms(any(RetrieveFarmsServiceContractWrapper.class)))
                .thenReturn(mockWrapper);

            // Act
            List<FarmResponseBO> result = farmRecordBS.retrieveFarmRecordByCustomer(farmRequestBC);

            // Assert
            assertNotNull("Farm response should not be null", result);
            assertEquals("Should return 100 farm records", 100, result.size());
            
            // Verify all farms have valid data
            for (FarmResponseBO farm : result) {
                assertNotNull("Farm number should not be null", farm.getFarmNumber());
                assertNotNull("State code should not be null", farm.getAdminStateCode());
                assertNotNull("County code should not be null", farm.getAdminCountyCode());
            }
        }
    }

    private RetrieveFarmsResultWrapper createLargeDataSetMockWrapper() {
        RetrieveFarmsResultWrapper wrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO result = new FarmResultDTO();
        
        // Create 100 farm records
        FarmDTO[] farmArray = new FarmDTO[100];
        for (int i = 0; i < 100; i++) {
            FarmDTO farm = new FarmDTO();
            farm.setNumber(String.format("%05d", i + 1));
            farm.setAdminStateCode("IA");
            farm.setAdminCountyCode(String.format("%03d", (i % 10) + 1));
            farm.setIdentifier((long) i + 1);
            farm.setDescription("Test Farm " + (i + 1));
            farmArray[i] = farm;
        }
        
        result.setFarmList(farmArray);
        result.setEndOfListIndicator(true);
        result.setExceptionMessage(null);
        
        wrapper.setResult(result);
        return wrapper;
    }

    // ===== GETTER/SETTER TESTS =====

    @Test
    public void testGetSetFarmRecordsServiceBean() {
        // Arrange
        FarmRecordsExternalService testService = mock(FarmRecordsExternalService.class);
        
        // Act
        farmRecordBS.setFarmRecordsServiceBean(testService);
        FarmRecordsExternalService retrievedService = farmRecordBS.getFarmRecordsServiceBean();
        
        // Assert
        assertEquals("Retrieved service should match set service", testService, retrievedService);
    }

    @Test
    public void testFarmRecordsServiceBean_InitiallyNull() {
        // Arrange
        FarmRecordBS newFarmRecordBS = new FarmRecordBS();
        
        // Act
        FarmRecordsExternalService service = newFarmRecordBS.getFarmRecordsServiceBean();
        
        // Assert
        assertNull("Initial service bean should be null", service);
    }
}