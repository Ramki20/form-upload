package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.StateDataServiceProxy;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.common.base.InvalidBusinessContractDataException;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateLocationAreaCodeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CountyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MailCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTNoDataException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * MRTFacadeBusinessServiceImpl_UT_Mockito
 * 
 * Unit tests for MRTFacadeBusinessServiceImpl using Mockito framework instead of PowerMock.
 * This version eliminates PowerMock dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MRTFacadeBusinessServiceImpl_UT extends DLSExternalCommonTestMockBase {
    
    private MRTFacadeBusinessServiceImpl service;
    
    @Mock
    private OfficeDataServiceProxy mockOfficeDataServiceProxy;
    
    @Mock
    private StateDataServiceProxy mockStateDataServiceProxy;
    
    @Mock
    private LocationAreaDataServiceProxy mockLocationAreaDataServiceProxy;
    
    private StateLocationAreaCodeBC contract;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Create instance using reflection to access constructor
        Constructor<MRTFacadeBusinessServiceImpl> constructor = MRTFacadeBusinessServiceImpl.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        service = constructor.newInstance();
        
        // Inject mocked dependencies
        service.setOfficeBusinessService(mockOfficeDataServiceProxy);
        service.setStateBusinessService(mockStateDataServiceProxy);
        service.setLocationBusinessService(mockLocationAreaDataServiceProxy);
        
        contract = new StateLocationAreaCodeBC(this.createAgencyToken(), "12345");
    }
    
    @Test
    public void testGetStatesList() throws Exception {
        // Arrange
        List<State> stateReadFacades = new ArrayList<State>();
        
        State st = new State();
        st.setCode("61");
        st.setAbbreviation("HI");
        st.setName("Hawaii");

        State st1 = new State();
        st1.setCode("62");
        st1.setAbbreviation("FM");
        st1.setName("Federated States of Micronesia");
        
        stateReadFacades.add(st);
        stateReadFacades.add(st1);

        when(mockStateDataServiceProxy.allFlp()).thenReturn(stateReadFacades);
        
        // Act
        List<StateBO> states = service.getStatesList();
        
        // Assert
        assertNotNull("States list should not be null", states);
        assertTrue("States list should not be empty", states.size() > 0);
        assertEquals("Should return both states", 2, states.size());
        
        // Verify state data
        StateBO hawaiiState = states.stream()
            .filter(s -> "HI".equals(s.getStateAbbr()))
            .findFirst()
            .orElse(null);
        assertNotNull("Hawaii state should be present", hawaiiState);
        assertEquals("Hawaii state code should be correct", "61", hawaiiState.getStateCode());
        assertEquals("Hawaii state name should be correct", "Hawaii", hawaiiState.getStateName());
        
        verify(mockStateDataServiceProxy, times(1)).allFlp();
    }
    
    @Test
    public void testGetStatesList_CachingBehavior() throws Exception {
        // Arrange
        List<State> stateReadFacades = createBasicStateList();
        when(mockStateDataServiceProxy.allFlp()).thenReturn(stateReadFacades);
        
        // Act - Call twice to test caching
        List<StateBO> states1 = service.getStatesList();
        List<StateBO> states2 = service.getStatesList();
        
        // Assert
        assertNotNull("First call should return states", states1);
        assertNotNull("Second call should return states", states2);
        assertEquals("Both calls should return same size", states1.size(), states2.size());
        
        // Verify service was only called once due to caching
        verify(mockStateDataServiceProxy, times(1)).allFlp();
    }
    
    @Test
    public void testGetCountriesListWrongStateCode() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "123", "MO");
        List<LocationArea> countyReadFacades = new ArrayList<LocationArea>();
        
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
        
        // Act
        List<CountyBO> countries = service.getCountiesList(contract);
        
        // Assert
        assertNotNull("Countries list should not be null", countries);
        assertEquals("Should return empty list for wrong state code", 0, countries.size());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByStateAbbr("MO");
    }
    
    @Test
    public void testGetCountriesListWithCorrectCode() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30", "MO");
        List<LocationArea> countyReadFacades = new ArrayList<LocationArea>();
        
        LocationArea county = new LocationArea();
        county.setStateCode("30");
        county.setCode("010");
        county.setName("Johnson");
        countyReadFacades.add(county);
        
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
        
        // Act
        List<CountyBO> countries = service.getCountiesList(contract);
        
        // Assert
        assertNotNull("Countries list should not be null", countries);
        assertTrue("Countries list should not be empty", countries.size() > 0);
        
        CountyBO countyBo = countries.get(0);
        assertNotNull("County object should not be null", countyBo);
        assertNotNull("County code should not be null", countyBo.getCountyCode());
        assertNotNull("County name should not be null", countyBo.getCountyName());
        assertEquals("County code should match", "010", countyBo.getCountyCode());
        assertEquals("County name should match", "Johnson", countyBo.getCountyName());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByStateAbbr("MO");
    }
    
    @Test
    public void testGetCountriesList_CachingBehavior() throws Exception {
        // Arrange
        StateBC contract1 = new StateBC(this.createAgencyToken(), "30", "MO");
        StateBC contract2 = new StateBC(this.createAgencyToken(), "30", "MO");
        
        List<LocationArea> countyReadFacades = createLocationAreaList("30");
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
        
        // Act - Call twice with same state to test caching
        List<CountyBO> countries1 = service.getCountiesList(contract1);
        List<CountyBO> countries2 = service.getCountiesList(contract2);
        
        // Assert
        assertNotNull("First call should return countries", countries1);
        assertNotNull("Second call should return countries", countries2);
        assertEquals("Both calls should return same size", countries1.size(), countries2.size());
        
        // Verify service was only called once due to caching
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByStateAbbr("MO");
    }
    
    @Test(expected = MRTNoDataException.class)
    public void testGetMailCodeNoResult() throws Exception {
        // Arrange - Empty list will trigger MRTNoDataException
        List<Office> emptyServiceCenterList = new ArrayList<Office>();
        String[] stateLocationAreaCodesArray = {"12345"};
        
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenReturn(emptyServiceCenterList);
        
        // Act - should throw MRTNoDataException
        service.getMailCode(contract);
    }
    
    @Test
    public void testGetMailCodeWithResult() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode("58062");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("01305");
        serviceCenterReadFacades.add(office);
        
        String[] stateLocationAreaCodesArray = {"58062"};
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        String mailCode = service.getMailCode(contract);
        
        // Assert
        assertNotNull("Mail code should not be null", mailCode);
        assertEquals("Mail code should match", "01305", mailCode);
        
        verify(mockOfficeDataServiceProxy, times(1))
            .flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray);
    }
    
    @Test(expected = InvalidBusinessContractDataException.class)
    public void testGetMailCodeInvalidContract() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode(null);
        
        // Act - should throw InvalidBusinessContractDataException from contract validation
        service.getMailCode(contract);
    }
    
    @Test
    public void testGetMailCodeListWithCorrectCode() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30");
        contract.setStateAbbr("MO");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("01305");
        office.setCountyName("Jackson");
        office.setLocCityName("Kansas City");
        serviceCenterReadFacades.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("MO"))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        List<MailCodeBO> mailCodes = service.getMailCodesList(contract);
        
        // Assert
        assertNotNull("Mail codes list should not be null", mailCodes);
        assertTrue("Mail codes list should not be empty", mailCodes.size() > 0);
        
        MailCodeBO mailCodeBO = mailCodes.get(0);
        assertNotNull("Mail code object should not be null", mailCodeBO);
        assertNotNull("City name should not be null", mailCodeBO.getCityName());
        assertNotNull("County name should not be null", mailCodeBO.getCountyName());
        assertNotNull("Mail code should not be null", mailCodeBO.getMailCode());
        
        assertEquals("Mail code should match", "01305", mailCodeBO.getMailCode());
        assertEquals("County name should match", "Jackson", mailCodeBO.getCountyName());
        assertEquals("City name should match", "Kansas City", mailCodeBO.getCityName());
        
        verify(mockOfficeDataServiceProxy, times(1)).fsaFlpServiceCenterOfficesByStateAbbr("MO");
    }
    
    // ===== ADDITIONAL EDGE CASE TESTS =====
    
    @Test
    public void testGetStatesList_EmptyResult() throws Exception {
        // Arrange
        List<State> emptyStateList = new ArrayList<State>();
        when(mockStateDataServiceProxy.allFlp()).thenReturn(emptyStateList);
        
        // Act
        List<StateBO> states = service.getStatesList();
        
        // Assert
        assertNotNull("States list should not be null", states);
        assertTrue("States list should be empty", states.isEmpty());
        
        verify(mockStateDataServiceProxy, times(1)).allFlp();
    }
    
    @Test
    public void testGetStatesList_FilteringLogic() throws Exception {
        // Arrange - Test the special filtering logic for codes 61 and 62
        List<State> stateReadFacades = new ArrayList<State>();
        
        // Hawaii - should be included
        State hawaii = new State();
        hawaii.setCode("61");
        hawaii.setAbbreviation("HI");
        hawaii.setName("Hawaii");
        
        // FM with correct name - should be included
        State fmCorrect = new State();
        fmCorrect.setCode("62");
        fmCorrect.setAbbreviation("FM");
        fmCorrect.setName("Federated States of Micronesia");
        
        // FM with wrong name - should be excluded
        State fmWrong = new State();
        fmWrong.setCode("62");
        fmWrong.setAbbreviation("FM");
        fmWrong.setName("Wrong Name");
        
        // Regular state - should be included
        State missouri = new State();
        missouri.setCode("30");
        missouri.setAbbreviation("MO");
        missouri.setName("Missouri");
        
        stateReadFacades.add(hawaii);
        stateReadFacades.add(fmCorrect);
        stateReadFacades.add(fmWrong);
        stateReadFacades.add(missouri);
        
        when(mockStateDataServiceProxy.allFlp()).thenReturn(stateReadFacades);
        
        // Act
        List<StateBO> states = service.getStatesList();
        
        // Assert
        assertNotNull("States list should not be null", states);
        assertEquals("Should include Hawaii, correct FM, and Missouri", 3, states.size());
        
        // Verify Hawaii is included with correct abbreviation
        StateBO hawaiiBO = states.stream()
            .filter(s -> "61".equals(s.getStateCode()))
            .findFirst()
            .orElse(null);
        assertNotNull("Hawaii should be included", hawaiiBO);
        assertEquals("Hawaii abbreviation should be HI", "HI", hawaiiBO.getStateAbbr());
        
        // Verify correct FM is included
        StateBO fmBO = states.stream()
            .filter(s -> "62".equals(s.getStateCode()))
            .findFirst()
            .orElse(null);
        assertNotNull("Correct FM should be included", fmBO);
        assertEquals("FM abbreviation should be FM", "FM", fmBO.getStateAbbr());
        
        // Verify Missouri is included
        StateBO moBO = states.stream()
            .filter(s -> "30".equals(s.getStateCode()))
            .findFirst()
            .orElse(null);
        assertNotNull("Missouri should be included", moBO);
        assertEquals("Missouri abbreviation should be MO", "MO", moBO.getStateAbbr());
    }
    
    @Test(expected = InvalidBusinessContractDataException.class)
    public void testGetCountiesList_InvalidContract() throws Exception {
        // Arrange - Create invalid contract (this will depend on StateBC validation logic)
        StateBC invalidContract = new StateBC(null, null, null);
        
        // Act - should throw InvalidBusinessContractDataException
        service.getCountiesList(invalidContract);
    }
    
    @Test
    public void testGetCountiesList_MultipleCounties() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30", "MO");
        
        List<LocationArea> countyReadFacades = new ArrayList<LocationArea>();
        
        LocationArea county1 = new LocationArea();
        county1.setStateCode("30");
        county1.setCode("010");
        county1.setName("Johnson");
        
        LocationArea county2 = new LocationArea();
        county2.setStateCode("30");
        county2.setCode("020");
        county2.setName("Jackson");
        
        // Add a county with different state code (should be filtered out)
        LocationArea differentState = new LocationArea();
        differentState.setStateCode("40");
        differentState.setCode("030");
        differentState.setName("Different State County");
        
        countyReadFacades.add(county1);
        countyReadFacades.add(county2);
        countyReadFacades.add(differentState);
        
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
        
        // Act
        List<CountyBO> countries = service.getCountiesList(contract);
        
        // Assert
        assertNotNull("Countries list should not be null", countries);
        assertEquals("Should return only counties with matching state code", 2, countries.size());
        
        // Verify counties are sorted by county code
        assertEquals("First county should have code 010", "010", countries.get(0).getCountyCode());
        assertEquals("Second county should have code 020", "020", countries.get(1).getCountyCode());
        
        verify(mockLocationAreaDataServiceProxy, times(1)).flpByStateAbbr("MO");
    }
    
    @Test(expected = InvalidBusinessContractDataException.class)
    public void testGetMailCodesList_InvalidContract() throws Exception {
        // Arrange
        StateBC invalidContract = new StateBC(null, null, null);
        
        // Act - should throw InvalidBusinessContractDataException
        service.getMailCodesList(invalidContract);
    }
    
    @Test
    public void testGetMailCodesList_MultipleOffices() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30");
        contract.setStateAbbr("MO");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        
        Office office1 = new Office();
        office1.setOfficeCode("01305");
        office1.setCountyName("Jackson");
        office1.setLocCityName("Kansas City");
        
        Office office2 = new Office();
        office2.setOfficeCode("01306");
        office2.setCountyName("Clay");
        office2.setLocCityName("Liberty");
        
        serviceCenterReadFacades.add(office1);
        serviceCenterReadFacades.add(office2);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("MO"))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        List<MailCodeBO> mailCodes = service.getMailCodesList(contract);
        
        // Assert
        assertNotNull("Mail codes list should not be null", mailCodes);
        assertEquals("Should return all offices", 2, mailCodes.size());
        
        MailCodeBO mailCode1 = mailCodes.get(0);
        assertEquals("First mail code should match", "01305", mailCode1.getMailCode());
        assertEquals("First county name should match", "Jackson", mailCode1.getCountyName());
        assertEquals("First city name should match", "Kansas City", mailCode1.getCityName());
        
        MailCodeBO mailCode2 = mailCodes.get(1);
        assertEquals("Second mail code should match", "01306", mailCode2.getMailCode());
        assertEquals("Second county name should match", "Clay", mailCode2.getCountyName());
        assertEquals("Second city name should match", "Liberty", mailCode2.getCityName());
        
        verify(mockOfficeDataServiceProxy, times(1)).fsaFlpServiceCenterOfficesByStateAbbr("MO");
    }
    
    @Test
    public void testGetMailCode_TrimWhitespace() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode("58062");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("  01305  "); // Office code with whitespace
        serviceCenterReadFacades.add(office);
        
        String[] stateLocationAreaCodesArray = {"58062"};
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        String mailCode = service.getMailCode(contract);
        
        // Assert
        assertNotNull("Mail code should not be null", mailCode);
        assertEquals("Mail code should be trimmed", "01305", mailCode);
    }
    
    // ===== ERROR HANDLING TESTS =====
    
    @Test(expected = javax.naming.ServiceUnavailableException.class)
    public void testGetStatesList_ServiceException() throws Exception {
        // Arrange
        when(mockStateDataServiceProxy.allFlp())
            .thenThrow(new RuntimeException("Service unavailable"));
        
        // Act - should wrap exception in ServiceUnavailableException
        service.getStatesList();
    }
    
    @Test(expected = javax.naming.ServiceUnavailableException.class)
    public void testGetCountiesList_ServiceException() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30", "MO");
        
        when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO"))
            .thenThrow(new RuntimeException("Service unavailable"));
        
        // Act - should wrap exception in ServiceUnavailableException
        service.getCountiesList(contract);
    }
    
    @Test(expected = javax.naming.ServiceUnavailableException.class)
    public void testGetMailCodesList_ServiceException() throws Exception {
        // Arrange
        StateBC contract = new StateBC(this.createAgencyToken(), "30");
        contract.setStateAbbr("MO");
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("MO"))
            .thenThrow(new RuntimeException("Service unavailable"));
        
        // Act - should wrap exception in ServiceUnavailableException
        service.getMailCodesList(contract);
    }
    
    @Test(expected = javax.naming.ServiceUnavailableException.class)
    public void testGetMailCode_ServiceException() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode("58062");
        String[] stateLocationAreaCodesArray = {"58062"};
        
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenThrow(new RuntimeException("Service unavailable"));
        
        // Act - should wrap exception in ServiceUnavailableException
        service.getMailCode(contract);
    }
    
    // ===== BOUNDARY VALUE TESTS =====
    
    @Test
    public void testGetMailCode_SingleResult() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode("58062");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("01305");
        serviceCenterReadFacades.add(office);
        
        String[] stateLocationAreaCodesArray = {"58062"};
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        String mailCode = service.getMailCode(contract);
        
        // Assert
        assertNotNull("Mail code should not be null", mailCode);
        assertEquals("Should return first office code", "01305", mailCode);
    }
    
    @Test
    public void testGetMailCode_MultipleResults() throws Exception {
        // Arrange
        contract.setStateLocationAreaCode("58062");
        
        List<Office> serviceCenterReadFacades = new ArrayList<Office>();
        
        Office office1 = new Office();
        office1.setOfficeCode("01305");
        
        Office office2 = new Office();
        office2.setOfficeCode("01306");
        
        serviceCenterReadFacades.add(office1);
        serviceCenterReadFacades.add(office2);
        
        String[] stateLocationAreaCodesArray = {"58062"};
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray))
            .thenReturn(serviceCenterReadFacades);
        
        // Act
        String mailCode = service.getMailCode(contract);
        
        // Assert
        assertNotNull("Mail code should not be null", mailCode);
        assertEquals("Should return first office code when multiple results", "01305", mailCode);
    }
    
    // ===== SETTER TESTS =====
    
    @Test
    public void testSetStateBusinessService() {
        // Arrange
        StateDataServiceProxy testProxy = mock(StateDataServiceProxy.class);
        
        // Act
        service.setStateBusinessService(testProxy);
        
        // Assert - verify through behavior (would need to test actual usage)
        // This is primarily a setter test to ensure no exceptions are thrown
        assertNotNull("Service should still be available", service);
    }
    
    @Test
    public void testSetLocationBusinessService() {
        // Arrange
        LocationAreaDataServiceProxy testProxy = mock(LocationAreaDataServiceProxy.class);
        
        // Act
        service.setLocationBusinessService(testProxy);
        
        // Assert
        assertNotNull("Service should still be available", service);
    }
    
    @Test
    public void testSetOfficeBusinessService() {
        // Arrange
        OfficeDataServiceProxy testProxy = mock(OfficeDataServiceProxy.class);
        
        // Act
        service.setOfficeBusinessService(testProxy);
        
        // Assert
        assertNotNull("Service should still be available", service);
    }
    
    // ===== HELPER METHODS =====
    
    private List<State> createBasicStateList() {
        List<State> stateList = new ArrayList<State>();
        
        State missouri = new State();
        missouri.setCode("30");
        missouri.setAbbreviation("MO");
        missouri.setName("Missouri");
        
        State kansas = new State();
        kansas.setCode("17");
        kansas.setAbbreviation("KS");
        kansas.setName("Kansas");
        
        stateList.add(missouri);
        stateList.add(kansas);
        
        return stateList;
    }
    
    private List<LocationArea> createLocationAreaList(String stateCode) {
        List<LocationArea> locationList = new ArrayList<LocationArea>();
        
        LocationArea location1 = new LocationArea();
        location1.setStateCode(stateCode);
        location1.setCode("010");
        location1.setName("Johnson");
        
        LocationArea location2 = new LocationArea();
        location2.setStateCode(stateCode);
        location2.setCode("020");
        location2.setName("Jackson");
        
        locationList.add(location1);
        locationList.add(location2);
        
        return locationList;
    }
    
    private List<Office> createOfficeList() {
        List<Office> officeList = new ArrayList<Office>();
        
        Office office1 = new Office();
        office1.setOfficeCode("01305");
        office1.setCountyName("Jackson");
        office1.setLocCityName("Kansas City");
        
        Office office2 = new Office();
        office2.setOfficeCode("01306");
        office2.setCountyName("Clay");
        office2.setLocCityName("Liberty");
        
        officeList.add(office1);
        officeList.add(office2);
        
        return officeList;
    }
}