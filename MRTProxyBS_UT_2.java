package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;

/**
 * MRTProxyBS_UT - Converted to use Mockito instead of PowerMock
 * 
 * Unit tests for MRTProxyBS using Mockito framework with isolated Spring context.
 * This version eliminates PowerMock dependencies for faster, more isolated testing.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MRTProxyBS_UT {
    
    private IMRTProxyBS service; // Keep as interface
    private MRTProxyBS serviceImpl; // Reference to concrete impl for dependency injection
    private ApplicationContext testContext;
    
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
        // Create isolated Spring context for this test
        createTestSpringContext();
        
        // Get service from our isolated context
        service = testContext.getBean("mrtProxyBS", IMRTProxyBS.class);
        
        // Get concrete implementation for dependency injection
        if (service instanceof MRTProxyBS) {
            serviceImpl = (MRTProxyBS) service;
        } else {
            // Handle proxy case - try to get the target object
            serviceImpl = extractTargetFromProxy(service);
        }
        
        if (serviceImpl == null) {
            fail("Could not obtain MRTProxyBS instance for testing");
        }
        
        // Inject mocked dependencies into the concrete implementation
        serviceImpl.setFlpOfficeMRTBusinessService(mockOfficeDataServiceProxy);
        serviceImpl.setFlpStateMRTBusinessService(mockStateDataServiceProxy);
        serviceImpl.setInterestRateDataMartBusinessService(mockInterestRateDataServiceProxy);
        serviceImpl.setFlpLocationAreaDataMartBusinessService(mockLocationAreaDataServiceProxy);
        serviceImpl.setBusinessPartyDataService(mockBusinessPartyDataServiceProxy);
        serviceImpl.setEmployeeDataServiceProxy(mockEmployeeDataServiceProxy);
        serviceImpl.setSurrogateService(mockSurrogateService);
        serviceImpl.setCountyDataServiceProxy(mockCountyDataServiceProxy);
        serviceImpl.setCalendarDataServiceProxy(mockCalendarDataServiceProxy);
        serviceImpl.setMrtFacadeBusinessService(mockMRTFacadeBusinessService);
    }
    
    @After
    public void tearDown() throws Exception {
        // Close test context if it exists
        if (testContext != null && testContext instanceof org.springframework.context.ConfigurableApplicationContext) {
            ((org.springframework.context.ConfigurableApplicationContext) testContext).close();
        }
    }
    
    private void createTestSpringContext() {
        // Create a fresh Spring context for each test to avoid interference
        String[] springConfigs = {
            "classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/common-external-service-spring-config.xml"
        };
        testContext = new ClassPathXmlApplicationContext(springConfigs);
    }
    
    /**
     * Extract the target object from a Spring proxy if needed
     */
    private MRTProxyBS extractTargetFromProxy(Object proxy) {
        try {
            // Try AOP proxy extraction
            if (proxy.getClass().getName().contains("$Proxy") || 
                proxy.getClass().getName().contains("CGLIB")) {
                
                // Try to get target through AopUtils (if available)
                try {
                    Class<?> aopUtilsClass = Class.forName("org.springframework.aop.support.AopUtils");
                    Method getTargetMethod = aopUtilsClass.getMethod("getTargetObject", Object.class);
                    Object target = getTargetMethod.invoke(null, proxy);
                    if (target instanceof MRTProxyBS) {
                        return (MRTProxyBS) target;
                    }
                } catch (Exception e) {
                    // AopUtils not available or failed, try other approaches
                }
                
                // Try to access through advised interface
                try {
                    if (proxy instanceof org.springframework.aop.framework.Advised) {
                        org.springframework.aop.framework.Advised advised = (org.springframework.aop.framework.Advised) proxy;
                        Object target = advised.getTargetSource().getTarget();
                        if (target instanceof MRTProxyBS) {
                            return (MRTProxyBS) target;
                        }
                    }
                } catch (Exception e) {
                    // Advised interface not available or failed
                }
            }
            
            // If it's already the concrete class, return it
            if (proxy instanceof MRTProxyBS) {
                return (MRTProxyBS) proxy;
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Could not extract target from proxy: " + e.getMessage());
        }
        
        return null;
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
                .thenReturn(mockBusinessPartyInfo);
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
        service.retrieveFSAStateList(retrieveFSAStateListBC);
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

    // ===== HELPER METHODS =====
    
    private AgencyToken createAgencyToken(){
        gov.usda.fsa.common.base.AgencyToken token = new AgencyToken();
        token.setRequestHost("FCAO");
        token.setApplicationIdentifier("FCAO");
        token.setUserIdentifier("FCAO");
        token.setProcessingNode("DLS_Common");
        token.setReadOnly();
        return token;
    }

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
}FsaOfficeProperties.id), eq(FsaOfficeProperties.locStateAbbrev), 
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
            eq(