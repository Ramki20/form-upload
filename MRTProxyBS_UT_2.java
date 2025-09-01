package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.ArgumentMatchers;

import gov.usda.fsa.citso.cbs.bc.InterestTypeId;
import gov.usda.fsa.citso.cbs.bc.Surrogate;
import gov.usda.fsa.citso.cbs.bc.TaxId;
import gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.CalendarDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.CountyDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.EmployeeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.InterestRateDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.StateDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.TaxIdSurrogateBusinessServiceProxy;
import gov.usda.fsa.citso.cbs.dto.AgencyEmployee;
import gov.usda.fsa.citso.cbs.dto.BusinessPartyInfo;
import gov.usda.fsa.citso.cbs.dto.BusinessPartyRole;
import gov.usda.fsa.citso.cbs.dto.EmployeeOrgChart;
import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.citso.cbs.dto.metadata.AgencyEmployeeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpStateProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaStateProperties;
import gov.usda.fsa.citso.cbs.ex.BusinessServiceBindingException;
import gov.usda.fsa.citso.cbs.service.ErrorMessage;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.common.base.InvalidBusinessContractDataException;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFSAStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFsaCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveInterestRateForAssistanceTypeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTServiceCenterListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FipsOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FlpOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MrtLookUpBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;

/**
 * MRTProxyBS_UT - Converted to use Mockito instead of PowerMock
 * 
 * Unit tests for MRTProxyBS using Mockito framework instead of PowerMock.
 * This version eliminates PowerMock dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MRTProxyBS_UT extends DLSExternalCommonTestMockBase {
    
    private IMRTProxyBS service; // Keep as interface
    
    @Mock
    private OfficeDataServiceProxy mockOfficeDataServiceProxy;
    
    @Mock
    private StateDataServiceProxy mockStateDataServiceProxy;
    
    @Mock
    private InterestRateDataServiceProxy mockInterestRateDataServiceProxy;
    
    @Mock
    private LocationAreaDataServiceProxy mockLocationAreaDataServiceProxy;
    
    @Mock
    private BusinessPartyDataServiceProxy mockBusinessPartyDataServiceProxy;
    
    @Mock
    private EmployeeDataServiceProxy mockEmployeeDataServiceProxy;
    
    @Mock
    private TaxIdSurrogateBusinessServiceProxy mockSurrogateService;
    
    @Mock
    private CountyDataServiceProxy mockCountyDataServiceProxy;
    
    @Mock
    private CalendarDataServiceProxy mockCalendarDataServiceProxy;
    
    @Mock
    private MRTFacadeBusinessService mockMRTFacadeBusinessService;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Get the Spring-managed service instance
        service = ServiceAgentFacade.getInstance().getMrtProxyBusinessService();
        
        // Cast to implementation to inject mocks
        assertTrue("Service should be instance of MRTProxyBS", 
                  service instanceof MRTProxyBS);
        MRTProxyBS mrtProxyBS = (MRTProxyBS) service;
        
        // Inject mocked dependencies
        mrtProxyBS.setFlpOfficeMRTBusinessService(mockOfficeDataServiceProxy);
        mrtProxyBS.setFlpStateMRTBusinessService(mockStateDataServiceProxy);
        mrtProxyBS.setInterestRateDataMartBusinessService(mockInterestRateDataServiceProxy);
        mrtProxyBS.setFlpLocationAreaDataMartBusinessService(mockLocationAreaDataServiceProxy);
        mrtProxyBS.setBusinessPartyDataService(mockBusinessPartyDataServiceProxy);
        mrtProxyBS.setEmployeeDataServiceProxy(mockEmployeeDataServiceProxy);
        mrtProxyBS.setSurrogateService(mockSurrogateService);
        mrtProxyBS.setCountyDataServiceProxy(mockCountyDataServiceProxy);
        mrtProxyBS.setCalendarDataServiceProxy(mockCalendarDataServiceProxy);
        mrtProxyBS.setMrtFacadeBusinessService(mockMRTFacadeBusinessService);
    }
    
    @Test
    public void testRetrieveSurrogateIdForTaxId() throws Exception {
        // Arrange
        List<String> taxIdList = new ArrayList<String>();
        String taxId1 = "123456789";
        taxIdList.add(taxId1);
        
        Map<String, Surrogate> expectedMap = new HashMap<String, Surrogate>();
        when(mockSurrogateService.transformTaxId(taxIdList)).thenReturn(expectedMap);
        
        // Act
        Map<String, Surrogate> taxIdSurrogateMap = service.retrieveSurrogateIdForTaxId(taxIdList);
        
        // Assert
        assertNotNull("Tax ID surrogate map should not be null", taxIdSurrogateMap);
        verify(mockSurrogateService, times(1)).transformTaxId(taxIdList);
    }

    @Test
    public void testRetrieveTaxIdForSurrogateId() throws Exception {
        // Arrange
        List<String> surrIdList = new ArrayList<String>();
        String surrId = "123456789";
        surrIdList.add(surrId);
        
        Map<String, TaxId> expectedMap = new HashMap<String, TaxId>();
        when(mockSurrogateService.transformSurrogate(surrIdList)).thenReturn(expectedMap);
        
        // Act
        Map<String, TaxId> surrogateIdTaxIdMap = service.retrieveTaxIdForSurrogateId(surrIdList);
        
        // Assert
        assertNotNull("Surrogate ID tax ID map should not be null", surrogateIdTaxIdMap);
        verify(mockSurrogateService, times(1)).transformSurrogate(surrIdList);
    }

    @Test
    public void test_lookupUserForEmployeeId_exception() throws Exception {
        // Arrange
        when(mockBusinessPartyDataServiceProxy.infoByAuthId("dummyEAuthID", AgencyEmployeeProperties.camsEmployeeId))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act
        String employeeId = service.lookupUserEmployeeCamsId("dummyEAuthID");
        
        // Assert
        assertTrue("Employee ID should be empty on exception", StringUtil.isEmptyString(employeeId));
    }

    @Test
    public void test_lookupUserForEmployeeId() throws Exception {
        // Arrange
        BusinessPartyInfo mockBusinessPartyInfo = mock(BusinessPartyInfo.class);
        AgencyEmployee[] agencyEmployees = new AgencyEmployee[1];
        AgencyEmployee agencyEmployee = new AgencyEmployee();
        agencyEmployees[0] = agencyEmployee;
        agencyEmployee.setCamsEmployeeId("testing ID");

        when(mockBusinessPartyDataServiceProxy.infoByAuthId("dummyEAuthID", AgencyEmployeeProperties.camsEmployeeId))
                .thenReturn(officeList);

        // Act
        String fsaCode = service.getFSAStateCountyCodeFromStateLocationAreaFLPCode(flpCode);

        // Assert
        assertNotNull("FSA code should not be null", fsaCode);
        assertEquals("FSA code should match expected format", expectedResult, fsaCode);
        assertTrue("FSA code should be longer than 7 characters", expectedResult.length() > 7);
        assertNotNull("Site ID part should not be null", expectedResult.substring(7));
        assertEquals("Site ID part should be 4 characters", 4, expectedResult.substring(7).length());
    }

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerEmptyInput() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();

        // Act
        List<LocationArea> objects = service.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return empty list for empty input", 0, objects.size());
    }

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerCorrectCode() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("04001");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        LocationArea object1 = new LocationArea();
        LocationArea object2 = new LocationArea();
        LocationArea object3 = new LocationArea();
        mockedLocationAreaObject.add(object);
        mockedLocationAreaObject.add(object1);
        mockedLocationAreaObject.add(object2);
        mockedLocationAreaObject.add(object3);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("04001"))
                .thenReturn(mockedLocationAreaObject);

        // Act
        List<LocationArea> objects = service.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return 4 location areas", 4, objects.size());
    }

    @Test
    public void testRetrieveFLPOfficeCodeListForScimsCustomerWrongCode() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("00000");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
                .thenReturn(mockedLocationAreaObject);

        // Act
        List<LocationArea> objects = service.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertEquals("Should return empty list for wrong code", 0, objects.size());
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerEmptyInput() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        AgencyToken token = null;

        // Act
        List<Office> offices = service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);

        // Assert
        assertTrue("Should return empty list for empty input", offices.isEmpty());
    }

    @Test
    public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerNoneEmptyInputWithData() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("58062");
        flpOfficeList.add(item);

        List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        flpOfficeLocXRefMRTBusinessObjects.add(office);

        String[] completeFlpLocArLstArray = { "58062" };
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
                .thenReturn(flpOfficeLocXRefMRTBusinessObjects);

        // Act
        List<Office> results = service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, null);

        // Assert
        assertEquals("Should return one office", 1, results.size());
    }

    @Test
    public void test_retrieveStateByFlpFipsCode() throws Exception {
        // Arrange
        String flpCode = "20324";
        State mockedState = new State();
        
        when(mockStateDataServiceProxy.byFlpFipsCode(flpCode, FlpStateProperties.code, 
            FlpStateProperties.name, FlpStateProperties.abbreviation)).thenReturn(mockedState);

        // Act
        State state = service.retrieveStateByFlpFipsCode(flpCode);

        // Assert
        assertNotNull("State should not be null", state);
    }

    @Test
    public void test_retrieveStateByFlpFipsCode_noStatefound() throws Exception {
        // Arrange
        String flpCode = "";
        State mockedState = null;
        
        when(mockStateDataServiceProxy.byFlpFipsCode(flpCode, FlpStateProperties.code, 
            FlpStateProperties.name, FlpStateProperties.abbreviation)).thenReturn(mockedState);

        // Act
        State state = service.retrieveStateByFlpFipsCode(flpCode);

        // Assert
        assertNull("State should be null when not found", state);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveStateByFlpFipsCode_exception() throws Exception {
        // Arrange
        String flpCode = "error";
        
        when(mockStateDataServiceProxy.byFlpFipsCode(flpCode, FlpStateProperties.code, 
            FlpStateProperties.name, FlpStateProperties.abbreviation))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveStateByFlpFipsCode(flpCode);
    }

    @Test
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq(stateAbbr), 
            eq(FlpOfficeProperties.id), eq(FlpOfficeProperties.mailingZipCode), 
            eq(FlpOfficeProperties.mailingAddrInfoLine), eq(FlpOfficeProperties.mailingAddrLine), 
            eq(FlpOfficeProperties.mailingCity), eq(FlpOfficeProperties.mailingStateAbbrev), 
            eq(FlpOfficeProperties.stateFipsCode), eq(FlpOfficeProperties.stateName), 
            eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.countyName), eq(FlpOfficeProperties.locCityName)))
            .thenReturn(serviceCenterOffices);

        // Act
        List<Office> offices = service.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

        // Assert
        assertFalse("Offices should not be empty", offices.isEmpty());
    }

    @Test
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_noData() throws Exception {
        // Arrange
        String stateAbbr = "N/A";
        List<Office> serviceCenterOffices = new ArrayList<Office>();

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq(stateAbbr), 
            eq(FlpOfficeProperties.id), eq(FlpOfficeProperties.mailingZipCode), 
            eq(FlpOfficeProperties.mailingAddrInfoLine), eq(FlpOfficeProperties.mailingAddrLine), 
            eq(FlpOfficeProperties.mailingCity), eq(FlpOfficeProperties.mailingStateAbbrev), 
            eq(FlpOfficeProperties.stateFipsCode), eq(FlpOfficeProperties.stateName), 
            eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name),
            eq(FlpOfficeProperties.countyName), eq(FlpOfficeProperties.locCityName)))
            .thenReturn(serviceCenterOffices);

        // Act
        List<Office> offices = service.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Offices should be empty", offices.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_exception() throws Exception {
        // Arrange
        String stateAbbr = "ERROR";

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq(stateAbbr), 
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class)))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);
    }

    @Test
    public void test_retrieveFlpAreaListByStateAbbr() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();
        LocationArea locArea = new LocationArea();
        fLPLocationAreaMRTBusinessObjectList.add(locArea);

        when(mockLocationAreaDataServiceProxy.flpByStateAbbr(stateAbbr, FlpLocationAreaProperties.stateCode,
                FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
                FlpLocationAreaProperties.shortName, FlpLocationAreaProperties.stateRefId))
                .thenReturn(fLPLocationAreaMRTBusinessObjectList);

        // Act
        List<LocationArea> locationAreaList = service.retrieveFlpAreaListByStateAbbr(stateAbbr);
        
        // Assert
        assertFalse("Location area list should not be empty", locationAreaList.isEmpty());
    }

    @Test
    public void test_retrieveFlpAreaListByStateAbbr_noData() throws Exception {
        // Arrange
        String stateAbbr = "N/A";
        List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

        when(mockLocationAreaDataServiceProxy.flpByStateAbbr(stateAbbr, FlpLocationAreaProperties.stateCode,
                FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
                FlpLocationAreaProperties.shortName, FlpLocationAreaProperties.stateRefId))
                .thenReturn(fLPLocationAreaMRTBusinessObjectList);

        // Act
        List<LocationArea> locationAreaList = service.retrieveFlpAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Location area list should be empty", locationAreaList.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSAStateList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        
        when(mockStateDataServiceProxy.allFSA(FsaStateProperties.code, FsaStateProperties.name,
                FsaStateProperties.abbreviation)).thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode(stateFsaCode, locAreaFsaCode);
    }

    // ===== ADDITIONAL COMPREHENSIVE TESTS =====

    @Test
    public void test_retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer_withEmptyStateLocationAreaCode() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode(""); // Empty code
        flpOfficeList.add(item);

        // Act
        List<Office> offices = service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, null);

        // Assert
        assertTrue("Should return empty list for empty state location area code", offices.isEmpty());
    }

    @Test
    public void test_retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer_withNullStateLocationAreaCode() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode(null); // Null code
        flpOfficeList.add(item);

        // Act
        List<Office> offices = service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, null);

        // Assert
        assertTrue("Should return empty list for null state location area code", offices.isEmpty());
    }

    @Test
    public void test_retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer_noDataFoundException() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("58062");
        flpOfficeList.add(item);

        String[] completeFlpLocArLstArray = { "58062" };
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("no data found", "e1")));

        // Act
        List<Office> offices = service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, null);

        // Assert
        assertTrue("Should return empty list when no data found", offices.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer_otherException() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("58062");
        flpOfficeList.add(item);

        String[] completeFlpLocArLstArray = { "58062" };
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("Other error", "e1")));

        // Act
        service.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, null);
    }

    @Test
    public void test_retrieveFlpCodesByFsaCodeMapForSCIMSCustomer() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("30010");

        List<LocationArea> locationAreas = new ArrayList<LocationArea>();
        LocationArea area1 = new LocationArea();
        area1.setStateLocationAreaCode("30010");
        LocationArea area2 = new LocationArea();
        area2.setStateLocationAreaCode("30011");
        locationAreas.add(area1);
        locationAreas.add(area2);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("30010")).thenReturn(locationAreas);

        // Act
        Map<String, java.util.Set<String>> result = service.retrieveFlpCodesByFsaCodeMapForSCIMSCustomer(fsaStAndLocArCodes, null);

        // Assert
        assertNotNull("Result should not be null", result);
        assertTrue("Should contain mapping for FSA code", result.containsKey("30010"));
        assertEquals("Should contain 2 FLP codes", 2, result.get("30010").size());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpCodesByFsaCodeMapForSCIMSCustomer_exception() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("30010");

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("30010"))
                .thenThrow(new BusinessServiceBindingException("Service error"));

        // Act
        service.retrieveFlpCodesByFsaCodeMapForSCIMSCustomer(fsaStAndLocArCodes, null);
    }

    @Test
    public void test_retrieveEmployeeData_withAgencyToken() throws Exception {
        // Arrange
        AgencyToken token = this.createAgencyToken();
        String agencyCode = "FSA";

        BusinessPartyInfo mockBusinessPartyInfo = mock(BusinessPartyInfo.class);
        AgencyEmployee[] agencyEmployees = new AgencyEmployee[1];
        AgencyEmployee agencyEmployee = new AgencyEmployee();
        agencyEmployees[0] = agencyEmployee;
        agencyEmployee.setCamsEmployeeId("EMP123");

        when(mockBusinessPartyDataServiceProxy.infoByAuthId(token.getUserIdentifier(), 
                AgencyEmployeeProperties.camsEmployeeId)).thenReturn(mockBusinessPartyInfo);
        when(mockBusinessPartyInfo.getAgencyEmployee()).thenReturn(agencyEmployees);

        // Act
        gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData result = 
            service.retrieveEmployeeData(token, agencyCode);

        // Assert
        assertNotNull("Employee data should not be null", result);
        assertEquals("Agency code should match", agencyCode, result.getAgencyCode());
    }

    @Test
    public void test_retrieveEmployeeEmail() throws Exception {
        // Arrange
        String agencyCode = "FSA";
        String employeeId = "EMP123";
        String expectedEmail = "test@usda.gov";

        gov.usda.fsa.citso.cbs.dto.IcamsEmployee mockEmployee = mock(gov.usda.fsa.citso.cbs.dto.IcamsEmployee.class);
        when(mockEmployee.getEmailAddress()).thenReturn(expectedEmail);

        when(mockEmployeeDataServiceProxy.findEmployeeByEmployeeIdentifier(
                ArgumentMatchers.any(gov.usda.fsa.citso.cbs.bc.EmployeeId.class),
                ArgumentMatchers.any(gov.usda.fsa.citso.cbs.bc.AgencyCode.class),
                ArgumentMatchers.any())).thenReturn(mockEmployee);

        // Act
        String result = service.retrieveEmployeeEmail(agencyCode, employeeId);

        // Assert
        assertEquals("Email should match", expectedEmail, result);
    }

    @Test
    public void test_retrieveEmployeeEmail_notFound() throws Exception {
        // Arrange
        String agencyCode = "FSA";
        String employeeId = "NOTFOUND";

        when(mockEmployeeDataServiceProxy.findEmployeeByEmployeeIdentifier(
                ArgumentMatchers.any(gov.usda.fsa.citso.cbs.bc.EmployeeId.class),
                ArgumentMatchers.any(gov.usda.fsa.citso.cbs.bc.AgencyCode.class),
                ArgumentMatchers.any())).thenReturn(null);

        // Act
        String result = service.retrieveEmployeeEmail(agencyCode, employeeId);

        // Assert
        assertEquals("Should return empty string when employee not found", "", result);
    }

    @Test
    public void test_retrieveCalendarByDate() throws Exception {
        // Arrange
        Date testDate = new Date();
        gov.usda.fsa.citso.cbs.dto.Calendar expectedCalendar = new gov.usda.fsa.citso.cbs.dto.Calendar();
        expectedCalendar.setFederalHolidayIndicator(true);

        when(mockCalendarDataServiceProxy.byCalendarDate(eq(testDate),
                eq(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.federalHolidayIndicator),
                eq(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.weekDayIndicator),
                eq(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.calendarDate)))
                .thenReturn(expectedCalendar);

        // Act
        gov.usda.fsa.citso.cbs.dto.Calendar result = service.retrieveCalendarByDate(testDate);

        // Assert
        assertNotNull("Calendar should not be null", result);
        assertTrue("Should be federal holiday", result.getFederalHolidayIndicator());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveCalendarByDate_exception() throws Exception {
        // Arrange
        Date testDate = new Date();
        
        when(mockCalendarDataServiceProxy.byCalendarDate(eq(testDate),
                any(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.class),
                any(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.class),
                any(gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties.class)))
                .thenThrow(new BusinessServiceBindingException("Service error"));

        // Act
        service.retrieveCalendarByDate(testDate);
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
}StateList(retrieveFSAStateListBC);
    }

    @Test
    public void test_retrieveFSAStateList() throws Exception {
        // Arrange
        List<State> mockedStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockedStateList.add(state);

        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        when(mockStateDataServiceProxy.allFSA(FsaStateProperties.code, FsaStateProperties.name,
                FsaStateProperties.abbreviation)).thenReturn(mockedStateList);

        // Act
        List<MrtLookUpBO> mrtLookupBO = service.retrieveFSAStateList(retrieveFSAStateListBC);

        // Assert
        assertNotNull("MRT lookup BO should not be null", mrtLookupBO);
        assertTrue("Should return one state", mrtLookupBO.size() == 1);
        assertNotNull("Code should not be null", mrtLookupBO.get(0).getCode());
        assertTrue("Code should match", "69".equalsIgnoreCase(mrtLookupBO.get(0).getCode()));
    }

    @Test
    public void test_retrieveFSAStateMap() throws Exception {
        // Arrange
        List<State> mockStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockStateList.add(state);

        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        when(mockStateDataServiceProxy.allFSA(FsaStateProperties.code, FsaStateProperties.name,
                FsaStateProperties.abbreviation)).thenReturn(mockStateList);

        // Act
        Map<String, MrtLookUpBO> mrtLookupBOMapReturned = service.retrieveFSAStateMap(retrieveFSAStateListBC);
        
        // Assert
        assertNotNull("MRT lookup BO map should not be null", mrtLookupBOMapReturned);
        assertTrue("Should return one state in map", mrtLookupBOMapReturned.size() == 1);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSAStateMap_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
        
        when(mockStateDataServiceProxy.allFSA(FsaStateProperties.code, FsaStateProperties.name,
                FsaStateProperties.abbreviation)).thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveFSAStateMap(retrieveFSAStateListBC);
    }

    @Test
    public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices() throws Exception {
        // Arrange
        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        LocationArea object1 = new LocationArea();
        LocationArea object2 = new LocationArea();
        LocationArea object3 = new LocationArea();
        mockedLocationAreaObject.add(object);
        mockedLocationAreaObject.add(object1);
        mockedLocationAreaObject.add(object2);
        mockedLocationAreaObject.add(object3);
        
        String[] serviceCenters = { "04001" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
                .thenReturn(mockedLocationAreaObject);
                
        // Act
        List<LocationArea> fLPLocationAreListReturned = service
                .retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
                
        // Assert
        assertNotNull("Location area list should not be null", fLPLocationAreListReturned);
        assertTrue("Should return 4 location areas", fLPLocationAreListReturned.size() == 4);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices_DLSBusinessFatalException() throws Exception {
        // Arrange
        String[] serviceCenters = { "04001" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
                
        // Act
        service.retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveFLPOfficeCodeListForScimsCustomer_DLSBusinessFatalException() throws Exception {
        // Arrange
        List<String> fsaStAndLocArCodes = new ArrayList<String>();
        fsaStAndLocArCodes.add("00000");
        
        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

        // Act
        service.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);
    }

    @Test
    public void test_retrieveIntRatesByRateTypeIdLstAndDtRng() throws Exception {
        // Arrange
        List<String> typeIds = new ArrayList<String>();
        typeIds.add("1");
        List<InterestRate> interestRates = new ArrayList<InterestRate>();
        InterestRate interestRate = new InterestRate();
        interestRate.setId(1);
        interestRate.setIntRate(BigDecimal.ONE);
        interestRates.add(interestRate);
        Date fromDate = Calendar.getInstance().getTime();
        Date toDate = Calendar.getInstance().getTime();

        when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(
                ArgumentMatchers.<List<InterestTypeId>>any(),
                ArgumentMatchers.any(Date.class),
                ArgumentMatchers.any(Date.class))).thenReturn(interestRates);

        // Act
        List<InterestRate> interestRateListReturned = service
                .retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
                
        // Assert
        assertNotNull("Interest rate list should not be null", interestRateListReturned);
        assertTrue("Should return one interest rate", interestRateListReturned.size() == 1);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveIntRatesByRateTypeIdLstAndDtRng_DLSBusinessFatalException() throws Exception {
        // Arrange
        List<String> typeIds = new ArrayList<String>();
        typeIds.add("1");
        Date fromDate = Calendar.getInstance().getTime();
        Date toDate = Calendar.getInstance().getTime();

        when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(
                ArgumentMatchers.<List<InterestTypeId>>any(),
                ArgumentMatchers.any(Date.class),
                ArgumentMatchers.any(Date.class)))
                .thenThrow(new RuntimeException("Test Exception"));

        // Act
        service.retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
    }

    @Test
    public void test_retrieveFlpStateOffices_light() throws Exception {
        // Arrange
        List<Office> allStateOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setId(1);
        office.setAgencyAbbr("FSA");
        office.setCounty(true);
        allStateOffices.add(office);
        
        when(mockOfficeDataServiceProxy.allFlpStateOffices(FlpOfficeProperties.stateName,
                FlpOfficeProperties.stateAbbrev, FlpOfficeProperties.stateFipsCode, 
                FlpOfficeProperties.officeCode)).thenReturn(allStateOffices);

        // Act
        List<Office> officeListReturned = service.retrieveFlpStateOffices_light();
        
        // Assert
        assertNotNull("Office list should not be null", officeListReturned);
        assertTrue("Should return one office", officeListReturned.size() == 1);
        assertTrue("Office ID should match", officeListReturned.get(0).getId() == 1);
        assertTrue("Agency abbreviation should match", "FSA".equalsIgnoreCase(officeListReturned.get(0).getAgencyAbbr()));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpStateOffices_light_ExceptionCovered() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.allFlpStateOffices(FlpOfficeProperties.stateName,
                FlpOfficeProperties.stateAbbrev, FlpOfficeProperties.stateFipsCode, 
                FlpOfficeProperties.officeCode)).thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveFlpStateOffices_light();
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateOffices_light() throws Exception {
        // Arrange
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);
        
        String[] stateOffices = { "OF" };
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(stateOffices,
                FlpOfficeProperties.officeCode, FlpOfficeProperties.countyFipsCode, 
                FlpOfficeProperties.countyName, FlpOfficeProperties.stateFipsCode))
                .thenReturn(serviceCenterOffices);

        // Act
        List<Office> officeListReturned = service.retrieveFlpServiceCentersByStateOffices_light(stateOffices);
        
        // Assert
        assertNotNull("Office list should not be null", officeListReturned);
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateAbbr() throws Exception {
        // Arrange
        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOffices.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("AL"))
                .thenReturn(serviceCenterOffices);
                
        // Act
        List<Office> officeListReturned = service.retrieveFlpServiceCentersByStateAbbr("AL");
        
        // Assert
        assertNotNull("Office list should not be null", officeListReturned);
        assertTrue("Should return one office", officeListReturned.size() == 1);
        assertTrue("Office ID should match", officeListReturned.get(0).getId() == 21047);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpServiceCentersByStateAbbr_ExceptionCovered() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr("AL"))
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveFlpServiceCentersByStateAbbr("AL");
    }

    @Test
    public void test_retrieveOrgChartsByEmployeeId() throws Exception {
        // Arrange
        List<EmployeeOrgChart> employeeOrgChartList = new ArrayList<EmployeeOrgChart>();
        EmployeeOrgChart employeeOrgChart = new EmployeeOrgChart();
        employeeOrgChart.setEmployeeId("1");
        employeeOrgChart.setNoteText("Test");
        employeeOrgChartList.add(employeeOrgChart);
        
        when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId("1"))
                .thenReturn(employeeOrgChartList);
                
        // Act
        List<EmployeeOrgChart> employeeOrgChartListReturned = service.retrieveOrgChartsByEmployeeId("1");
        
        // Assert
        assertNotNull("Employee org chart list should not be null", employeeOrgChartListReturned);
        assertTrue("Should return one org chart", employeeOrgChartListReturned.size() == 1);
        assertTrue("Employee ID should match", "1".equalsIgnoreCase(employeeOrgChartListReturned.get(0).getEmployeeId()));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveOrgChartsByEmployeeId_ExceptionCovered() throws Exception {
        // Arrange
        when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId("1"))
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveOrgChartsByEmployeeId("1");
    }

    @Test
    public void test_flpByFlpCodeList() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);
        
        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds)).thenReturn(flpOfficeList);
        
        // Act
        List<LocationArea> flpOfficeListReturned = service.flpByFlpCodeList(flpCds);
        
        // Assert
        assertNotNull("FLP office list should not be null", flpOfficeListReturned);
        assertTrue("Should return one location area", flpOfficeListReturned.size() == 1);
        assertTrue("Location area ID should match", 1 == flpOfficeListReturned.get(0).getId());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_ExceptionCovered() throws Exception {
        // Arrange
        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.flpByFlpCodeList(flpCds);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_BusinessServiceBindingExceptionCovered() throws Exception {
        // Arrange
        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
                
        // Act
        service.flpByFlpCodeList(flpCds);
    }

    @Test
    public void test_flpByFlpCodeList_light() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);

        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
                FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
                FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, 
                FlpLocationAreaProperties.shortName)).thenReturn(flpOfficeList);

        // Act
        List<LocationArea> flpOfficeListReturned = service.flpByFlpCodeList_light(flpCds);
        
        // Assert
        assertNotNull("FLP office list should not be null", flpOfficeListReturned);
        assertTrue("Should return one location area", flpOfficeListReturned.size() == 1);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_light_BusinessServiceBindingException() throws Exception {
        // Arrange
        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
                FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
                FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, 
                FlpLocationAreaProperties.shortName))
                .thenThrow(new BusinessServiceBindingException("Test Error"));
                
        // Act
        service.flpByFlpCodeList_light(flpCds);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_flpByFlpCodeList_light_Exception() throws Exception {
        // Arrange
        String[] flpCds = { "CA" };
        when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
                FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
                FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, 
                FlpLocationAreaProperties.shortName))
                .thenThrow(new RuntimeException("Test Error"));
                
        // Act
        service.flpByFlpCodeList_light(flpCds);
    }

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();
        LocationArea flpLocationArea1 = new LocationArea();
        flpLocationArea1.setStateLocationAreaCode("01001");
        flpLocationArea.add(flpLocationArea1);

        Office office = new Office();
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
                .thenReturn(flpLocationArea);
        when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(ArgumentMatchers.anyList()))
                .thenReturn(officeList);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = service.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should not be empty", !allStateOffices.isEmpty());
    }

    @Test
    public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode_emptyLocationAreaList() throws Exception {
        // Arrange
        List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("01020"))
                .thenReturn(flpLocationArea);

        String fsaStateCountyCode = "01020";

        // Act
        List<Office> allStateOffices = service.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

        // Assert
        assertTrue("State offices should be empty", allStateOffices.isEmpty());
    }

    @Test
    public void test_retrieveFlpServiceCentersByOIP() throws Exception {
        // Arrange
        List<String> stateOffices = new ArrayList<String>();
        stateOffices.add("01300");

        List<Office> officeList = new ArrayList<Office>();
        Office office1 = new Office();
        office1.setId(123213);
        officeList.add(office1);

        List<Office> officeList2 = new ArrayList<Office>();
        Office office2 = new Office();
        office2.setOfficeCode("01301");
        officeList2.add(office2);

        String[] stateOfficesArray = stateOffices.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(stateOfficesArray))
                .thenReturn(officeList);

        Integer[] officeIdArray = { 123213 };
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByOfficeIdList(officeIdArray))
                .thenReturn(officeList2);

        // Act
        List<Office> allStateOffices = service.retrieveFlpServiceCentersByOIP(stateOffices);

        // Assert
        assertTrue("State offices should not be empty", !allStateOffices.isEmpty());
    }

    @Test(expected = InvalidBusinessContractDataException.class)
    public void test_retrieveFlpServiceCentersByOIP_invalidInput() throws Exception {
        // Arrange
        List<String> stateOffices = new ArrayList<String>();
        stateOffices.add("123"); // Invalid FLP code (too short)

        // Act
        service.retrieveFlpServiceCentersByOIP(stateOffices);
    }

    @Test
    public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
        // Arrange
        List<Office> officeList = new ArrayList<Office>();
        Office office1 = new Office();
        office1.setOfficeCode("63300");
        office1.setStateName("HAWAII");
        office1.setCountyName("state");
        officeList.add(office1);
        
        Office office2 = new Office();
        office2.setOfficeCode("63301");
        office2.setStateName("HAWAII");
        office2.setCountyName("County");
        officeList.add(office2);
        
        Office office3 = new Office();
        office3.setOfficeCode("63303");
        office3.setStateName("HAWAII");
        office3.setCountyName("County2");
        officeList.add(office3);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("HI"),
                eq(FlpOfficeProperties.stateFipsCode), eq(FlpOfficeProperties.stateName), 
                eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name), 
                eq(FlpOfficeProperties.countyName))).thenReturn(officeList);

        String stateAbbr = "HI";

        // Act
        List<FlpOfficeLocationAreaBO> allFlpOfficeLocationAreas = service
                .retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("FLP office location areas should not be empty", !allFlpOfficeLocationAreas.isEmpty());
    }

    @Test
    public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr_Exception() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("HI"),
                any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
                any(FlpOfficeProperties.class), any(FlpOfficeProperties.class)))
                .thenThrow(new RuntimeException("Service error"));

        String stateAbbr = "HI";

        // Act
        List<FlpOfficeLocationAreaBO> allFlpOfficeLocationAreas = service
                .retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("FLP office location areas should be empty on exception", allFlpOfficeLocationAreas.isEmpty());
    }

    @Test
    public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
        // Arrange
        List<LocationArea> fsaLocAreaList = new ArrayList<LocationArea>();

        LocationArea locationArea1 = new LocationArea();
        locationArea1.setStateLocationAreaCode("63");
        locationArea1.setCode("001");
        locationArea1.setStateName("HAWAII");
        locationArea1.setName("Name 1");
        fsaLocAreaList.add(locationArea1);

        LocationArea locationArea2 = new LocationArea();
        locationArea2.setStateLocationAreaCode("63");
        locationArea2.setCode("000"); // This should be filtered out
        locationArea2.setStateName("HAWAII");
        locationArea2.setName("Name 2");
        fsaLocAreaList.add(locationArea2);

        LocationArea locationArea3 = new LocationArea();
        locationArea3.setStateLocationAreaCode("63");
        locationArea3.setCode("003");
        locationArea3.setStateName("HAWAII");
        locationArea3.setName("Name 3");
        fsaLocAreaList.add(locationArea3);

        when(mockLocationAreaDataServiceProxy.byStateAbbr(eq("HI"),
                eq(FsaLocationAreaProperties.stateLocationAreaCode), eq(FsaLocationAreaProperties.stateName),
                eq(FsaLocationAreaProperties.name), eq(FsaLocationAreaProperties.code)))
                .thenReturn(fsaLocAreaList);

        String stateAbbr = "HI";

        // Act
        List<FipsOfficeLocationAreaBO> result = service
                .retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Result should not be empty", !result.isEmpty());
        assertEquals("Should return 2 items (filtering out code 000)", 2, result.size());
    }

    @Test
    public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr_exception() throws Exception {
        // Arrange
        when(mockLocationAreaDataServiceProxy.byStateAbbr(eq("HI"),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class)))
                .thenThrow(new RuntimeException("Service error"));

        String stateAbbr = "HI";

        // Act
        List<FipsOfficeLocationAreaBO> result = service
                .retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

        // Assert
        assertTrue("Result should be empty on exception", result.isEmpty());
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType_BusinessServiceBindingException() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = 
            new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);
            
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

        // Act
        Double interestRate = service.retrieveInterestRateForAssistanceType(contract);
        
        // Assert
        assertTrue("Interest rate should be 0.0 on exception", 0.00 == interestRate.doubleValue());
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType_Exception() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = 
            new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);
            
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
                .thenThrow(new RuntimeException("TestException"));

        // Act
        Double interestRate = service.retrieveInterestRateForAssistanceType(contract);
        
        // Assert
        assertTrue("Interest rate should be 0.0 on exception", 0.00 == interestRate.doubleValue());
    }

    @Test(expected = DLSBusinessStopException.class)
    public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList_DLSBusinessStopException() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("MO"),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class)))
            .thenThrow(new RuntimeException("TestException"));
            
        // Act
        service.retrieveFLPOfficeMRTBusinessObjectList("MO");
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveStateList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(null);
        
        when(mockStateDataServiceProxy.allFlp(any(FlpStateProperties.class), any(FlpStateProperties.class),
                any(FlpStateProperties.class), any(FlpOfficeProperties.class), any(FlpStateProperties.class)))
            .thenThrow(new RuntimeException("TestException"));

        // Act
        service.retrieveStateList(contract);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveServiceCenterList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("MO"),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class),
            any(FlpOfficeProperties.class), any(FlpOfficeProperties.class), any(FlpOfficeProperties.class)))
            .thenThrow(new RuntimeException("TestException"));
            
        // Act
        service.retrieveServiceCenterList(contract);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveCountyList_DLSBusinessFatalException() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123))
            .thenThrow(new RuntimeException("TestException"));
            
        // Act
        service.retrieveCountyList(contract);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void testRetrieveCountyList_BusinessServiceBindingException() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123))
            .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
            
        // Act
        service.retrieveCountyList(contract);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_DLSBusinessFatalException() throws Exception {
        // Arrange
        String office_flp_code_str = "123";
        String[] flpStringCodesArray = { office_flp_code_str };
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
                .thenThrow(new RuntimeException("TestException"));

        // Act
        String code = service.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertEquals("Should return CBS_ERROR on exception", "CBS_ERROR", code);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_NoError() throws Exception {
        // Arrange
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);

        String office_flp_code_str = "123";
        String[] flpStringCodesArray = { office_flp_code_str };
        Integer[] flpIntegerCodesArray = { 21047 };
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
                .thenReturn(serviceCenterOfficesList);
        when(mockOfficeDataServiceProxy.byOfficeIdList(eq(flpIntegerCodesArray), 
            eq(FsaOfficeProperties.id), eq(FsaOfficeProperties.locStateAbbrev), 
            eq(FsaOfficeProperties.locCityName), eq(FsaOfficeProperties.stateAbbrev), 
            eq(FsaOfficeProperties.officeCode), eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode), eq(FsaOfficeProperties.refId), 
            eq(FsaOfficeProperties.siteId), eq(FsaOfficeProperties.mailingZipCode), 
            eq(FsaOfficeProperties.mailingAddrInfoLine), eq(FsaOfficeProperties.mailingAddrLine)))
                .thenReturn(serviceCenterOfficesList);

        // Act
        String fsaCode = service.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertTrue("FSA code should match expected format", "123::8247".equalsIgnoreCase(fsaCode));
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_BusinessServiceBindingException() throws Exception {
        // Arrange
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);
        
        String office_flp_code_str = "123";
        String[] flpStringCodesArray = { office_flp_code_str };
        Integer[] flpIntegerCodesArray = { 21047 };
        
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
                .thenReturn(serviceCenterOfficesList);
        when(mockOfficeDataServiceProxy.byOfficeIdList(flpIntegerCodesArray))
                .thenThrow(new BusinessServiceBindingException("TestException"));

        // Act
        String fsaCode = service.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);
        
        // Assert
        assertTrue("FSA code should be empty on exception", StringUtil.isEmptyString(fsaCode));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveInterestRates_DLSBusinessFatalException() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Integer[] typeIds = { 50500 };
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDate(typeIds, date.getTime()))
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveInterestRates(typeIds, date.getTime());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpStateOffices_DLSBusinessFatalException() throws Exception {
        // Arrange
        when(mockOfficeDataServiceProxy.allFlpStateOffices())
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveFlpStateOffices();
    }

    @Test
    public void test_retrieveOfficesByFlpCodes() throws Exception {
        // Arrange
        List<String> stringList = new ArrayList<String>();
        stringList.add("A");
        List<Office> serviceCenterOfficesList = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);
        serviceCenterOfficesList.add(office);
        
        String[] stringArray = stringList.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(stringArray))
                .thenReturn(serviceCenterOfficesList);
                
        // Act
        List<Office> officeList = service.retrieveOfficesByFlpCodes(stringList);
        
        // Assert
        assertNotNull("Office list should not be null", officeList);
        assertTrue("Should return one office", officeList.size() == 1);
    }

    @Test
    public void test_retrieveFSACountyList() throws Exception {
        // Arrange
        List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
        LocationArea item = new LocationArea();
        item.setStateLocationAreaCode("40001");
        item.setCode("C");
        item.setName("TestName");
        item.setId(1);
        flpOfficeList.add(item);
        
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        when(mockLocationAreaDataServiceProxy.agByStateFsa(eq("CA"),
                eq(FsaLocationAreaProperties.id), eq(FsaLocationAreaProperties.code),
                eq(FsaLocationAreaProperties.name), eq(FsaLocationAreaProperties.shortName),
                eq(FsaLocationAreaProperties.categoryName))).thenReturn(flpOfficeList);
                
        // Act
        List<MrtLookUpBO> mrtLookupList = service.retrieveFSACountyList(retrieveFsaCountyListBC);
        
        // Assert
        assertNotNull("MRT lookup list should not be null", mrtLookupList);
        assertTrue("Should return one county", mrtLookupList.size() == 1);
        assertTrue("Reference identifier should match", "1".equalsIgnoreCase(mrtLookupList.get(0).getRefenceIdentifier()));
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSACountyList_BusinessServiceBindingExceptionCovered() throws Exception {
        // Arrange
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        
        when(mockLocationAreaDataServiceProxy.agByStateFsa(eq("CA"),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class),
                any(FsaLocationAreaProperties.class)))
                .thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
                
        // Act
        service.retrieveFSACountyList(retrieveFsaCountyListBC);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSACountyList_ThrowableCovered() throws Exception {
        // Arrange
        RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
        
        when(mockLocationAreaDataServiceProxy.agByStateFsa(eq("CA"),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class),
                any(FsaLocationAreaProperties.class), any(FsaLocationAreaProperties.class),
                any(FsaLocationAreaProperties.class)))
                .thenThrow(new RuntimeException("TestException"));
                
        // Act
        service.retrieveFSACountyList(retrieveFsaCountyListBC);
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateOffices_listVersion() throws Exception {
        // Arrange
        List<String> stateOffices = new ArrayList<String>();
        stateOffices.add("01300");

        List<Office> serviceCenterOffices = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("01301");
        serviceCenterOffices.add(office);

        String[] stateOfficesArray = stateOffices.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(stateOfficesArray))
                .thenReturn(serviceCenterOffices);

        // Act
        List<Office> result = service.retrieveFlpServiceCentersByStateOffices(stateOffices);

        // Assert
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
    }

    @Test(expected = InvalidBusinessContractDataException.class)
    public void test_retrieveFlpServiceCentersByStateOffices_invalidInput() throws Exception {
        // Arrange
        List<String> stateOffices = new ArrayList<String>();
        stateOffices.add("123"); // Invalid FLP code

        // Act
        service.retrieveFlpServiceCentersByStateOffices(stateOffices);
    }

    // ===== ADDITIONAL HELPER TESTS =====

    @Test
    public void test_getFSAStateCountyOfficesFromFLPCodes() throws Exception {
        // Arrange
        List<String> flpOfficeCodes = new ArrayList<String>();
        flpOfficeCodes.add("01305");

        List<Office> flpOfficeList = new ArrayList<Office>();
        Office flpOffice = new Office();
        flpOffice.setId(100);
        flpOffice.setOfficeCode("01305");
        flpOfficeList.add(flpOffice);

        List<Office> fsaOfficeList = new ArrayList<Office>();
        Office fsaOffice = new Office();
        fsaOffice.setId(100);
        fsaOffice.setOfficeCode("FSA305");
        fsaOfficeList.add(fsaOffice);

        String[] flpOfficeCodeArray = flpOfficeCodes.toArray(new String[0]);
        Integer[] flpOfficeIdArray = { 100 };

        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpOfficeCodeArray,
                FlpOfficeProperties.id, FlpOfficeProperties.officeCode)).thenReturn(flpOfficeList);
        when(mockOfficeDataServiceProxy.byOfficeIdList(flpOfficeIdArray, FsaOfficeProperties.id,
                FsaOfficeProperties.officeCode)).thenReturn(fsaOfficeList);

        // Act
        Map<String, String> result = service.getFSAStateCountyOfficesFromFLPCodes(flpOfficeCodes);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should contain mapping", "FSA305", result.get("01305"));
    }

    @Test
    public void test_retrieveFlpLocationAreasByFipsLocationAreaCode() throws Exception {
        // Arrange
        String fipsCode = "12345";
        List<LocationArea> expectedResult = new ArrayList<LocationArea>();
        LocationArea area = new LocationArea();
        area.setStateLocationAreaCode("54321");
        expectedResult.add(area);

        when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea(fipsCode,
                FlpLocationAreaProperties.stateLocationAreaCode)).thenReturn(expectedResult);

        // Act
        List<LocationArea> result = service.retrieveFlpLocationAreasByFipsLocationAreaCode(fipsCode);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return one location area", 1, result.size());
        assertEquals("State location area code should match", "54321", 
                    result.get(0).getStateLocationAreaCode());
    }

    @Test
    public void test_retrieveFSALocationAreasByFSAStateCode() throws Exception {
        // Arrange
        String stateFsaCode = "30";
        List<LocationArea> expectedResult = new ArrayList<LocationArea>();
        LocationArea area = new LocationArea();
        area.setCode("010");
        area.setName("Test County");
        expectedResult.add(area);

        when(mockLocationAreaDataServiceProxy.byStateFsa(stateFsaCode,
                FsaLocationAreaProperties.code, FsaLocationAreaProperties.name, 
                FsaLocationAreaProperties.stateCode, FsaLocationAreaProperties.stateLocationAreaCode))
                .thenReturn(expectedResult);

        // Act
        List<LocationArea> result = service.retrieveFSALocationAreasByFSAStateCode(stateFsaCode);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return one location area", 1, result.size());
        assertEquals("County name should match", "Test County", result.get(0).getName());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSALocationAreasByFSAStateCode_exception() throws Exception {
        // Arrange
        String stateFsaCode = "30";
        
        when(mockLocationAreaDataServiceProxy.byStateFsa(stateFsaCode,
                FsaLocationAreaProperties.code, FsaLocationAreaProperties.name, 
                FsaLocationAreaProperties.stateCode, FsaLocationAreaProperties.stateLocationAreaCode))
                .thenThrow(new BusinessServiceBindingException("Service error"));

        // Act
        service.retrieveFSALocationAreasByFSAStateCode(stateFsaCode);
    }

    @Test
    public void test_retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode() throws Exception {
        // Arrange
        String stateFsaCode = "30";
        String locAreaFsaCode = "010";
        
        List<Office> expectedResult = new ArrayList<Office>();
        Office office = new Office();
        office.setOfficeCode("30010");
        office.setCountyName("Test County");
        expectedResult.add(office);

        when(mockOfficeDataServiceProxy.byStateAndLocationArea(eq(stateFsaCode), eq(locAreaFsaCode),
                eq(FsaOfficeProperties.id), eq(FsaOfficeProperties.officeCode), eq(FsaOfficeProperties.name),
                eq(FsaOfficeProperties.stateAbbrev), eq(FsaOfficeProperties.countyName), 
                eq(FsaOfficeProperties.siteName), eq(FsaOfficeProperties.countyFipsCode)))
                .thenReturn(expectedResult);

        // Act
        List<Office> result = service.retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode(stateFsaCode, locAreaFsaCode);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return one office", 1, result.size());
        assertEquals("Office code should match", "30010", result.get(0).getOfficeCode());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode_exception() throws Exception {
        // Arrange
        String stateFsaCode = "30";
        String locAreaFsaCode = "010";
        
        when(mockOfficeDataServiceProxy.byStateAndLocationArea(eq(stateFsaCode), eq(locAreaFsaCode),
                any(FsaOfficeProperties.class), any(FsaOfficeProperties.class), any(FsaOfficeProperties.class),
                any(FsaOfficeProperties.class), any(FsaOfficeProperties.class), any(FsaOfficeProperties.class),
                any(FsaOfficeProperties.class)))
                .thenThrow(new BusinessServiceBindingException("Service error"));

        // Act
        service.retrieveFSAmockBusinessPartyInfo);
        when(mockBusinessPartyInfo.getAgencyEmployee()).thenReturn(agencyEmployees);
        
        // Act
        String employeeId = service.lookupUserEmployeeCamsId("dummyEAuthID");

        // Assert
        assertNotNull("Employee ID should not be null", employeeId);
        assertEquals("Employee ID should match", "testing ID", employeeId);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_lookupUser_exception() throws Exception {
        // Arrange
        when(mockBusinessPartyDataServiceProxy.roleByAuthId(anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        service.lookupUser("testUser");
    }

    @Test
    public void test_lookupUser_valid() throws Exception {
        // Arrange
        BusinessPartyRole businessPartyRoleMock = new BusinessPartyRole();
        businessPartyRoleMock.setName("SO");
        when(mockBusinessPartyDataServiceProxy.roleByAuthId("28200310169021026877"))
            .thenReturn(businessPartyRoleMock);

        // Act
        BusinessPartyRole businessPartyRole = service.lookupUser("28200310169021026877");

        // Assert
        assertNotNull("Business party role should not be null", businessPartyRole);
        assertEquals("Role name should match", "SO", businessPartyRole.getName());
    }

    @Test
    public void test_retrieveInterestRate() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);

        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.TEN);
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, date.getTime())).thenReturn(rate);

        // Act
        InterestRate interestRate = service.retrieveInterestRate(50010, date.getTime());

        // Assert
        assertNotNull("Interest rate should not be null", interestRate);
        assertTrue("Interest rate should be greater than 0", interestRate.getIntRate().doubleValue() > 0.0);
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveInterestRates_exception() throws Exception {
        // Arrange
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, null))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveInterestRate(50010, null);
    }

    @Test
    public void test_retrieveInterestRates() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Integer[] typeIds = { 50500 };

        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.TEN);
        List<InterestRate> interestRatesMock = new ArrayList<InterestRate>();
        interestRatesMock.add(rate);
        
        when(mockInterestRateDataServiceProxy.byTypeIdListAndDate(typeIds, date.getTime()))
            .thenReturn(interestRatesMock);

        // Act
        List<InterestRate> interestRates = service.retrieveInterestRates(typeIds, date.getTime());

        // Assert
        assertNotNull("Interest rates should not be null", interestRates);
        assertFalse("Interest rates should not be empty", interestRates.isEmpty());
        assertEquals("Should return one interest rate", 1, interestRates.size());
        assertTrue("Interest rate should be greater than 0", 
                  interestRates.get(0).getIntRate().doubleValue() > 0.0);
    }

    @Test
    public void test_retrieveFlpStateOffices() throws Exception {
        // Arrange
        Office office = new Office();
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        when(mockOfficeDataServiceProxy.allFlpStateOffices()).thenReturn(officeList);

        // Act
        List<Office> allStateOffices = service.retrieveFlpStateOffices();

        // Assert
        assertFalse("State offices should not be empty", allStateOffices.isEmpty());
    }

    @Test(expected = DLSBusinessFatalException.class)
    public void test_retrieveFlpServiceCentersByStateOffices_exception() throws Exception {
        // Arrange
        String[] states = { "StateNotExist" };
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(states))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        service.retrieveFlpServiceCentersByStateOffices(states);
    }

    @Test
    public void test_retrieveFlpServiceCentersByStateOffices() throws Exception {
        // Arrange
        String[] states = { "MO" };
        Office office = new Office();
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(states))
            .thenReturn(officeList);

        // Act
        List<Office> serviceCenterOffices = service.retrieveFlpServiceCentersByStateOffices(states);

        // Assert
        assertFalse("Service center offices should not be empty", serviceCenterOffices.isEmpty());
    }

    @Test
    public void testRetrieveFSAOfficeListByOfficeIdentifierList() throws Exception {
        // Arrange
        List<Integer> flpIdCodes = new ArrayList<Integer>();
        flpIdCodes.add(60157);

        Office office = new Office();
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        Integer[] flpIdCodesArray = flpIdCodes.toArray(new Integer[0]);
        when(mockOfficeDataServiceProxy.byOfficeIdList(eq(flpIdCodesArray), 
            eq(FsaOfficeProperties.id), eq(FsaOfficeProperties.locStateAbbrev), 
            eq(FsaOfficeProperties.locCityName), eq(FsaOfficeProperties.stateAbbrev), 
            eq(FsaOfficeProperties.officeCode), eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode), eq(FsaOfficeProperties.refId), 
            eq(FsaOfficeProperties.siteId), eq(FsaOfficeProperties.mailingZipCode), 
            eq(FsaOfficeProperties.mailingAddrInfoLine), eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(officeList);

        // Act
        List<Office> ofcList = service.retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes);

        // Assert
        assertNotNull("Office list should not be null", ofcList);
        assertTrue("Office list should not be empty", ofcList.size() > 0);
    }

    @Test
    public void testRetriveFLPOfficesByOfficeFLPCdMap() throws Exception {
        // Arrange
        List<String> flpCodeList = new ArrayList<String>();
        flpCodeList.add("01305");

        Office office = new Office();
        office.setOfficeCode("01305");
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

        // Act
        Map<String, Office> result = service.retriveFLPOfficesByOfficeFLPCdMap(flpCodeList);
        
        // Assert
        Office ofc = result.get("01305");
        assertNotNull("Office should not be null", ofc);
    }

    @Test
    public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList() throws Exception {
        // Arrange
        String stateAbbr = "MO";
        Office office = new Office();
        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq(stateAbbr),
            eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name), eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode), eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev))).thenReturn(officeList);

        // Act
        List<Office> flpOfficeList = service.retrieveFLPOfficeMRTBusinessObjectList(stateAbbr);

        // Assert
        assertNotNull("FLP office list should not be null", flpOfficeList);
        assertTrue("FLP office list should not be empty", flpOfficeList.size() > 0);
    }

    @Test
    public void testRetrieveFLPStateList() throws Exception {
        // Arrange
        State state = new State();
        List<State> mockedStateList = new ArrayList<State>();
        mockedStateList.add(state);
        
        when(mockStateDataServiceProxy.allFlp(FlpStateProperties.code, FlpStateProperties.name,
                FlpStateProperties.abbreviation, FlpStateProperties.fipsCode, FlpStateProperties.activeId))
            .thenReturn(mockedStateList);

        // Act
        List<State> flpStateList = service.retrieveFLPStateList();

        // Assert
        assertNotNull("FLP state list should not be null", flpStateList);
        assertTrue("FLP state list should not be empty", flpStateList.size() > 0);
    }

    @Test
    public void testRretrieveInterestRateForAssistanceType() throws Exception {
        // Arrange
        Calendar date = Calendar.getInstance();
        date.set(2012, 1, 1);
        Date cutOffDate = date.getTime();
        RetrieveInterestRateForAssistanceTypeBC contract = 
            new RetrieveInterestRateForAssistanceTypeBC(null, "50010", cutOffDate);

        InterestRate rate = new InterestRate();
        rate.setIntRate(BigDecimal.ONE);
        when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate)).thenReturn(rate);

        // Act
        double interest = service.retrieveInterestRateForAssistanceType(contract);

        // Assert
        assertTrue("Interest rate should be greater than 0", 
                  Double.valueOf(interest).compareTo(Double.valueOf(0.0)) > 0);
    }

    @Test
    public void testRetrieveCountyList() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(this.createAgencyToken(), "123");

        List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
        LocationArea object = new LocationArea();
        object.setStateLocationAreaCode("CD");
        object.setStateRefId(1);
        object.setAlternateName("Alt_Name");
        mockedLocationAreaObject.add(object);
        
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(123)).thenReturn(mockedLocationAreaObject);

        // Act
        List<MrtLookUpBO> resultList = service.retrieveCountyList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertEquals("Should return one county", 1, resultList.size());
    }

    @Test
    public void testRetrieveCountyListWithResult() throws Exception {
        // Arrange
        RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "58303");
        List<LocationArea> emptyList = new ArrayList<LocationArea>();
        when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(58303)).thenReturn(emptyList);

        // Act
        List<MrtLookUpBO> resultList = service.retrieveCountyList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertEquals("Should return empty list", 0, resultList.size());
    }

    @Test
    public void testRetrieveStateList() throws Exception {
        // Arrange
        RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(null);

        List<State> mockedStateList = new ArrayList<State>();
        State state = new State();
        state.setCode("69");
        state.setName("Missouri");
        state.setAbbreviation("MO");
        mockedStateList.add(state);
        
        when(mockStateDataServiceProxy.allFlp(FlpStateProperties.code, FlpStateProperties.name,
                FlpStateProperties.abbreviation, FlpStateProperties.fipsCode, FlpStateProperties.activeId))
            .thenReturn(mockedStateList);

        // Act
        List<MrtLookUpBO> resultList = service.retrieveStateList(contract);
        
        // Assert
        MrtLookUpBO mrtLookUpBO = resultList.get(0);
        assertNotNull("Result list should not be null", resultList);
        assertTrue("Result list should not be empty", resultList.size() > 0);
        assertNotNull("Code should not be null", mrtLookUpBO.getCode());
        assertNotNull("Description should not be null", mrtLookUpBO.getDescription());
        assertNotNull("Reference identifier should not be null", mrtLookUpBO.getRefenceIdentifier());
        assertNull("Type should be null", mrtLookUpBO.getType());
    }

    @Test
    public void testRetrieveServiceCenterList() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");

        List<Office> boList = new ArrayList<Office>();
        Office bo = new Office();
        bo.setOfficeCode("Code");
        bo.setName("Name");
        bo.setOfficeResp("offRefId");
        bo.setRefId(12345);
        boList.add(bo);

        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("MO"),
            eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name), eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode), eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev))).thenReturn(boList);

        // Act
        List<MrtLookUpBO> resultList = service.retrieveServiceCenterList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertTrue("Result list should not be empty", resultList.size() > 0);
    }

    @Test
    public void testRetrieveServiceCenterListWrongState() throws Exception {
        // Arrange
        RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "AA");

        List<Office> boList = new ArrayList<Office>();
        when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(eq("AA"),
            eq(FlpOfficeProperties.officeCode), eq(FlpOfficeProperties.name), eq(FlpOfficeProperties.refId),
            eq(FlpOfficeProperties.cityFipsCode), eq(FlpOfficeProperties.locCityName),
            eq(FlpOfficeProperties.locStateAbbrev))).thenReturn(boList);

        // Act
        List<MrtLookUpBO> resultList = service.retrieveServiceCenterList(contract);

        // Assert
        assertNotNull("Result list should not be null", resultList);
        assertTrue("Result list should be empty", resultList.size() == 0);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode() throws Exception {
        // Arrange
        String office_flp_code_str = "123";
        Office office = new Office();
        office.setOfficeCode("123");
        office.setId(123);

        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);
        
        String[] flpStringCodesArray = { office_flp_code_str };
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

        // Mock empty FSA office list (no FSA office found for the FLP office)
        Integer[] flpIdCodesArray = { 123 };
        when(mockOfficeDataServiceProxy.byOfficeIdList(eq(flpIdCodesArray), 
            eq(FsaOfficeProperties.id), eq(FsaOfficeProperties.locStateAbbrev), 
            eq(FsaOfficeProperties.locCityName), eq(FsaOfficeProperties.stateAbbrev), 
            eq(FsaOfficeProperties.officeCode), eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode), eq(FsaOfficeProperties.refId), 
            eq(FsaOfficeProperties.siteId), eq(FsaOfficeProperties.mailingZipCode), 
            eq(FsaOfficeProperties.mailingAddrInfoLine), eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(new ArrayList<Office>());

        // Act
        String fsaCode = service.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

        // Assert
        assertNotNull("FSA code should not be null", fsaCode);
        assertEquals("FSA code should be empty when no FSA office found", "", fsaCode);
    }

    @Test
    public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_withSiteId() throws Exception {
        // Arrange
        String flpCode = "21047";
        String expectedResult = "21047::8247";

        Office office = new Office();
        office.setOfficeCode("21047");
        office.setId(21047);
        office.setSiteId(8247);

        List<Office> officeList = new ArrayList<Office>();
        officeList.add(office);

        String[] flpStringCodesArray = { flpCode };
        when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

        Integer[] flpIdCodesArray = { 21047 };
        when(mockOfficeDataServiceProxy.byOfficeIdList(eq(flpIdCodesArray), 
            eq(FsaOfficeProperties.id), eq(FsaOfficeProperties.locStateAbbrev), 
            eq(FsaOfficeProperties.locCityName), eq(FsaOfficeProperties.stateAbbrev), 
            eq(FsaOfficeProperties.officeCode), eq(FsaOfficeProperties.name),
            eq(FsaOfficeProperties.cityFipsCode), eq(FsaOfficeProperties.refId), 
            eq(FsaOfficeProperties.siteId), eq(FsaOfficeProperties.mailingZipCode), 
            eq(FsaOfficeProperties.mailingAddrInfoLine), eq(FsaOfficeProperties.mailingAddrLine)))
            .thenReturn(