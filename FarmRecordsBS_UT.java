package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FarmRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.parmo.farmrecords.dto.FarmDTO;
import gov.usda.fsa.parmo.farmrecords.dto.FarmResultDTO;
import gov.usda.fsa.parmo.frs.ejb.client.reply.RetrieveFarmsResultWrapper;
import gov.usda.fsa.parmo.frs.ejb.service.FarmRecordsExternalService;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FarmRecordBS using Mockito for mocking dependencies.
 * This version avoids Spring context loading and SOAP service calls.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceAgentFacade.class})
public class FarmRecordsBS_UT {

    @Mock
    private FarmRecordsExternalService mockFarmRecordsServiceBean;
   
    @Mock
    private ServiceAgentFacade mockServiceAgentFacade;
   
    @InjectMocks
    private FarmRecordBS farmRecordBS;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
       
        // Mock the ServiceAgentFacade singleton
        PowerMockito.mockStatic(ServiceAgentFacade.class);
        when(ServiceAgentFacade.getInstance()).thenReturn(mockServiceAgentFacade);
        when(mockServiceAgentFacade.getFarmRecordsBusinessService()).thenReturn(farmRecordBS);
       
        // Set the mock service bean on our business service
        farmRecordBS.setFarmRecordsServiceBean(mockFarmRecordsServiceBean);
    }

    @Test
    public void testRetrieveFarmRecordByCustomer() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        FarmRequestBC contract1 = new FarmRequestBC(agencyToken);
        contract1.setCoreCustomerId(9680545L);
        contract1.setYear(2017);

        // Create mock response data
        RetrieveFarmsResultWrapper mockWrapper = createMockWrapper();
        when(mockFarmRecordsServiceBean.retrieveFarms(any())).thenReturn(mockWrapper);

        // Act
        List<FarmResponseBO> farmResponseBO = farmRecordBS.retrieveFarmRecordByCustomer(contract1);

        // Assert
        assertNotNull("Farm response should not be null", farmResponseBO);
        assertFalse("Farm response list should not be empty", farmResponseBO.isEmpty());
        assertEquals("Should return 2 farm records", 2, farmResponseBO.size());
       
        // Verify the first farm record
        FarmResponseBO firstFarm = farmResponseBO.get(0);
        assertEquals("123", firstFarm.getFarmNumber());
        assertEquals("IA", firstFarm.getAdminStateCode());
        assertEquals("001", firstFarm.getAdminCountyCode());
       
        // Verify service was called once
        verify(mockFarmRecordsServiceBean, times(1)).retrieveFarms(any());
    }

    @Test
    public void testRetrieveFarmRecordByCustomerUnhappyValues() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        FarmRequestBC contract1 = new FarmRequestBC(agencyToken);
        contract1.setCoreCustomerId(968054L);
        contract1.setYear(2016);
        contract1.setIncludeCustomerWithResponse(null);
        contract1.setIncludeCropWithResponse(null);
        contract1.setIncludeNonActiveCustomerWithResponse(null);
        contract1.setIncludeTractInfoWithResponse(null);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract1);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            // Verify that validation caught the null values
            assertTrue("Should have validation errors", ex.getErrorMessageList().size() > 0);
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomerContractNull() throws Exception {
        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(null);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            // Verify that validation caught the null contract
            assertEquals("Should have 1 validation error", 1, ex.getErrorMessageList().size());
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomerNullValues() throws Exception {
        // Arrange
        FarmRequestBC contract1 = new FarmRequestBC(null);
        contract1.setCoreCustomerId(null);
        contract1.setYear(null);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract1);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            // Verify that validation caught the null values
            assertTrue("Should have validation errors", ex.getErrorMessageList().size() >= 3);
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomerWithServiceException() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        FarmRequestBC contract1 = new FarmRequestBC(agencyToken);
        contract1.setCoreCustomerId(9680545L);
        contract1.setYear(2017);

        // Create mock response with exception
        RetrieveFarmsResultWrapper mockWrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO mockResult = new FarmResultDTO();
        mockResult.setExceptionMessage("Service temporarily unavailable");
        mockWrapper.setResult(mockResult);
       
        when(mockFarmRecordsServiceBean.retrieveFarms(any())).thenReturn(mockWrapper);

        // Act & Assert
        try {
            farmRecordBS.retrieveFarmRecordByCustomer(contract1);
            fail("Expected DLSBCInvalidDataStopException");
        } catch (DLSBCInvalidDataStopException ex) {
            assertTrue("Exception message should contain service error",
                      ex.getMessage().contains("Invalid Data received from Farm Record Services"));
        }
    }

    @Test
    public void testRetrieveFarmRecordByCustomerEmptyResults() throws Exception {
        // Arrange
        AgencyToken agencyToken = createAgencyToken();
        FarmRequestBC contract1 = new FarmRequestBC(agencyToken);
        contract1.setCoreCustomerId(9680545L);
        contract1.setYear(2017);

        // Create mock response with no farms
        RetrieveFarmsResultWrapper mockWrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO mockResult = new FarmResultDTO();
        mockResult.setFarmList(new FarmDTO[0]); // Empty array
        mockResult.setEndOfListIndicator(true);
        mockWrapper.setResult(mockResult);
       
        when(mockFarmRecordsServiceBean.retrieveFarms(any())).thenReturn(mockWrapper);

        // Act
        List<FarmResponseBO> farmResponseBO = farmRecordBS.retrieveFarmRecordByCustomer(contract1);

        // Assert
        assertNotNull("Farm response should not be null", farmResponseBO);
        assertTrue("Farm response list should be empty", farmResponseBO.isEmpty());
    }

    /**
     * Helper method to create mock wrapper with sample farm data
     */
    private RetrieveFarmsResultWrapper createMockWrapper() {
        RetrieveFarmsResultWrapper mockWrapper = new RetrieveFarmsResultWrapper();
        FarmResultDTO mockResult = new FarmResultDTO();
       
        // Create sample farm data
        FarmDTO farm1 = new FarmDTO();
        farm1.setNumber("123");
        farm1.setAdminStateCode("IA");
        farm1.setAdminCountyCode("001");
        farm1.setIdentifier(1L);
        farm1.setDescription("Test Farm 1");
       
        FarmDTO farm2 = new FarmDTO();
        farm2.setNumber("456");
        farm2.setAdminStateCode("IL");
        farm2.setAdminCountyCode("002");
        farm2.setIdentifier(2L);
        farm2.setDescription("Test Farm 2");
       
        FarmDTO[] farmArray = {farm1, farm2};
        mockResult.setFarmList(farmArray);
        mockResult.setEndOfListIndicator(true); // Indicate end of list
        mockWrapper.setResult(mockResult);
       
        return mockWrapper;
    }

    /**
     * Helper method to create a test agency token
     */
    private AgencyToken createAgencyToken() {
        return createAgencyToken("DLMTest_User");
    }

    /**
     * Helper method to create a test agency token with specific user ID
     */
    private AgencyToken createAgencyToken(String inUserId) {
        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setProcessingNode("DLM_jUnit_TEST");
        agencyToken.setApplicationIdentifier("DLM-Test");
        agencyToken.setRequestHost("localhost");
        agencyToken.setUserIdentifier(inUserId);
        agencyToken.setReadOnly(true);
        return agencyToken;
    }
}