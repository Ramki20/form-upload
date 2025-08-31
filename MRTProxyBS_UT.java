package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.usda.fsa.citso.cbs.bc.InterestTypeId;
import gov.usda.fsa.citso.cbs.bc.Surrogate;
import gov.usda.fsa.citso.cbs.bc.TaxId;
import gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy;
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
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MRTProxyBS_UT.class, MRTProxyBS.class, OfficeDataServiceProxy.class,
		TaxIdSurrogateBusinessServiceProxy.class, StateDataServiceProxy.class, InterestRateDataServiceProxy.class,
		LocationAreaDataServiceProxy.class, BusinessPartyDataServiceProxy.class, EmployeeDataServiceProxy.class,
		MRTFacadeBusinessService.class })
public class MRTProxyBS_UT extends DLSExternalCommonTestMockBase {
	private IMRTProxyBS mrtProxyBusinessService;

	private gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy mockOfficeDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.StateDataServiceProxy mockStateDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.InterestRateDataServiceProxy mockInterestRateDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy mockLocationAreaDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy mockBusinessPartyDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.EmployeeDataServiceProxy mockEmployeeDataServiceProxy;
	private MRTFacadeBusinessService mockMRTFacadeBusinessService;

	private TaxIdSurrogateBusinessServiceProxy mockSurrogateService;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		mockSurrogateService = mock(gov.usda.fsa.citso.cbs.client.TaxIdSurrogateBusinessServiceProxy.class);
		mockOfficeDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy.class);
		mockStateDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.StateDataServiceProxy.class);
		mockInterestRateDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.InterestRateDataServiceProxy.class);
		mockLocationAreaDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy.class);
		mockBusinessPartyDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy.class);
		mockEmployeeDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.EmployeeDataServiceProxy.class);
		mockMRTFacadeBusinessService = mock(MRTFacadeBusinessService.class);

		mrtProxyBusinessService = ServiceAgentFacade.getInstance().getMrtProxyBusinessService();

		Assert.assertTrue(mrtProxyBusinessService instanceof MRTProxyBS);
		MRTProxyBS mrtProxyBS = (MRTProxyBS) mrtProxyBusinessService;
		mrtProxyBS.setSurrogateService((TaxIdSurrogateBusinessServiceProxy) mockSurrogateService);
		mrtProxyBS.setBusinessPartyDataService((BusinessPartyDataServiceProxy) mockBusinessPartyDataServiceProxy);
		mrtProxyBS.setEmployeeDataServiceProxy((EmployeeDataServiceProxy) mockEmployeeDataServiceProxy);
		mrtProxyBS.setFlpLocationAreaDataMartBusinessService(
				(LocationAreaDataServiceProxy) mockLocationAreaDataServiceProxy);
		mrtProxyBS.setFlpOfficeMRTBusinessService((OfficeDataServiceProxy) mockOfficeDataServiceProxy);
		mrtProxyBS.setFlpStateMRTBusinessService((StateDataServiceProxy) mockStateDataServiceProxy);
		mrtProxyBS.setInterestRateDataMartBusinessService(
				(InterestRateDataServiceProxy) mockInterestRateDataServiceProxy);
		mrtProxyBS.setMrtFacadeBusinessService(mockMRTFacadeBusinessService);
	}

	@Test
	public void testRetrieveSurrogateIdForTaxId() throws Exception {
		List<String> taxIdList = new ArrayList<String>();
		String taxId1 = "123456789";
		taxIdList.add(taxId1);
		Map<String, Surrogate> taxIdSurrogateMap = mrtProxyBusinessService.retrieveSurrogateIdForTaxId(taxIdList);
		Assert.assertNotNull(taxIdSurrogateMap);
//		Assert.assertTrue(surrogateMap.size()>0);
//		Assert.assertNotNull(surrogateMap.get(taxId1));
	}

	@Test
	public void testRetrieveTaxIdForSurrogateId() throws Exception {
		List<String> surrIdList = new ArrayList<String>();
		String surrId = "123456789";
		surrIdList.add(surrId);
		Map<String, TaxId> surrogateIdTaxIdMap = mrtProxyBusinessService.retrieveTaxIdForSurrogateId(surrIdList);
		Assert.assertNotNull(surrogateIdTaxIdMap);
//		Assert.assertTrue(surrogateMap.size()>0);
//		Assert.assertNotNull(surrogateMap.get(taxId1));
	}

	@Test
	public void test_lookupUserForEmployeeId_exception() throws Exception {
		// call lookupUser
		String employeeId = mrtProxyBusinessService.lookupUserEmployeeCamsId("dummyEAuthID");

		Assert.assertTrue(StringUtil.isEmptyString(employeeId));
	}

	@Test
	public void test_lookupUserForEmployeeId() throws Exception {
		BusinessPartyInfo mockBusinessPartyInfo = mock(BusinessPartyInfo.class);
		AgencyEmployee[] agencyEmployees = new AgencyEmployee[1];
		AgencyEmployee agencyEmployee = new AgencyEmployee();
		agencyEmployees[0] = agencyEmployee;
		agencyEmployee.setCamsEmployeeId("testing ID");

		when(mockBusinessPartyDataServiceProxy.infoByAuthId("dummyEAuthID", AgencyEmployeeProperties.camsEmployeeId))
				.thenReturn(mockBusinessPartyInfo);
		Mockito.when(mockBusinessPartyInfo.getAgencyEmployee()).thenReturn(agencyEmployees);
		// call lookupUser
		String employeeId = mrtProxyBusinessService.lookupUserEmployeeCamsId("dummyEAuthID");

		Assert.assertNotNull(employeeId);
		Assert.assertEquals("testing ID", employeeId);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_lookupUser_exception() throws Exception {

		// set condition
		// Mockito.when(mrtProxyBusinessService.lookupUser(Mockito.anyString()).thenReturn(DLSBusinessFatalException.class));
		Mockito.doThrow(Mockito.mock(DLSBusinessFatalException.class))
				.when(mrtProxyBusinessService.lookupUser(Mockito.anyString()));

		// call lookupUser
		mrtProxyBusinessService.lookupUser(null);

	}

	/**
	 * emp0007965 user
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_lookupUser_valid() throws Exception {

		// mock the return object
		BusinessPartyRole businessPartyRoleMock = new BusinessPartyRole();
		businessPartyRoleMock.setName("SO");
		Mockito.when(mrtProxyBusinessService.lookupUser("28200310169021026877")).thenReturn(businessPartyRoleMock);

		// call to business service
		BusinessPartyRole businessPartyRole = mrtProxyBusinessService.lookupUser("28200310169021026877");

		Assert.assertNotNull(businessPartyRole);
		Assert.assertEquals("SO", businessPartyRole.getName());
	}

	@Test
	public void test_retrieveInterestRate() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);

		// mock the return object
		InterestRate rate = new InterestRate();
		rate.setIntRate(BigDecimal.TEN);
		Mockito.when(mrtProxyBusinessService.retrieveInterestRate(50010, date.getTime())).thenReturn(rate);

		// call to service
		InterestRate interestRate = mrtProxyBusinessService.retrieveInterestRate(50010, date.getTime());

		Assert.assertNotNull(interestRate);
		Assert.assertTrue(interestRate.getIntRate().doubleValue() > 0.0);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveInterestRates_exception() throws Exception {

		// mock test cases
		Mockito.doThrow(Mockito.mock(DLSBusinessFatalException.class))
				.when(mrtProxyBusinessService.retrieveInterestRate(50010, null));

		mrtProxyBusinessService.retrieveInterestRate(50010, null);
	}

	@Test
	public void test_retrieveInterestRates() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);
		Integer[] typeIds = { 50500 };

		// mock test cases
		InterestRate rate = new InterestRate();
		rate.setIntRate(BigDecimal.TEN);
		List<InterestRate> interestRatesMock = new ArrayList<InterestRate>();
		interestRatesMock.add(rate);
		Mockito.when(mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime()))
				.thenReturn(interestRatesMock);

		// call to business service
		List<InterestRate> interestRates = mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime());

		Assert.assertNotNull(interestRates);
		Assert.assertFalse(interestRates.isEmpty());
		Assert.assertEquals(1, interestRates.size());
		Assert.assertTrue(interestRates.get(0).getIntRate().doubleValue() > 0.0);
	}

	@Test
	public void test_retrieveFlpStateOffices() throws Exception {

		// mock test cases
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mrtProxyBusinessService.retrieveFlpStateOffices()).thenReturn(officeList);

		// call to business service
		List<Office> allStateOffices = mrtProxyBusinessService.retrieveFlpStateOffices();

		Assert.assertFalse(allStateOffices.isEmpty());

	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpServiceCentersByStateOffices_exception() throws Exception {

		String[] states = { "StateNotExist" };

		// mock test cases
		Mockito.doThrow(Mockito.mock(DLSBusinessFatalException.class))
				.when(mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states));

		// call to service
		mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states);
	}

	@Test
	public void test_retrieveFlpServiceCentersByStateOffices() throws Exception {
		String[] states = { "MO" };

		// mock test cases
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states)).thenReturn(officeList);

		// call service
		List<Office> serviceCenterOffices = mrtProxyBusinessService.retrieveFlpServiceCentersByStateOffices(states);

		Assert.assertFalse(serviceCenterOffices.isEmpty());

	}

	@Test
	public void testRetrieveFSAOfficeListByOfficeIdentifierList() throws Exception {

		List<Integer> flpIdCodes = new ArrayList<Integer>();
		flpIdCodes.add(60157);

		// mock service
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mrtProxyBusinessService.retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes))
				.thenReturn(officeList);

		// call to service
		List<Office> ofcList = mrtProxyBusinessService.retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes);

		assertNotNull(ofcList);
		assertTrue(ofcList.size() > 0);

	}

	@Test
	public void testRetriveFLPOfficesByOfficeFLPCdMap() throws Exception {

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add("01305");

		// mock service
		Office office = new Office();
		office.setOfficeCode("01305");

		Map<String, Office> mockedMap = new HashMap<String, Office>();
		mockedMap.put("01305", office);

		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

		// call to service
		Map<String, Office> result = mrtProxyBusinessService.retriveFLPOfficesByOfficeFLPCdMap(flpCodeList);
		Office ofc = (Office) result.get("01305");
		assertNotNull(ofc);

	}

	@Test
	public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList() throws Exception {
		String stateAbbr = "MO";

		// mock service
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList(stateAbbr)).thenReturn(officeList);

		// call to service
		List<Office> flpOfficeList = mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList(stateAbbr);

		Assert.assertNotNull(flpOfficeList);
		Assert.assertTrue(flpOfficeList.size() > 0);
	}

	@Test
	public void testRetrieveFLPStateList() throws Exception {

		// mock service
		State state = new State();
		List<State> mockedStateList = new ArrayList<State>();
		mockedStateList.add(state);
		Mockito.when(mrtProxyBusinessService.retrieveFLPStateList()).thenReturn(mockedStateList);

		// call to service
		List<State> flpStateList = mrtProxyBusinessService.retrieveFLPStateList();

		Assert.assertNotNull(flpStateList);
		Assert.assertTrue(flpStateList.size() > 0);
	}

	@Test
	public void testRretrieveInterestRateForAssistanceType() throws Exception {
		gov.usda.fsa.common.base.AgencyToken agencyToken = null;

		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);
		Date cutOffDate = date.getTime();
		RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(agencyToken,
				"50010", cutOffDate);

		// mock service
		InterestRate rate = new InterestRate();
		rate.setIntRate(BigDecimal.ONE);

		Mockito.when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate)).thenReturn(rate);

		// call to service
		double interest = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);

		Assert.assertTrue(Double.valueOf(interest).compareTo(Double.valueOf(0.0)) > 0);
	}

	@Test
	public void testRetrieveCountyList() throws Exception {

		RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(this.createAgencyToken(), "123");

		List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
		LocationArea object = new LocationArea();
		object.setStateLocationAreaCode("CD");
		object.setStateRefId(1);
		object.setAlternateName("Alt_Name");
		mockedLocationAreaObject.add(object);
		Mockito.when(mockLocationAreaDataServiceProxy.flpByOfficeRefId(Mockito.any(Integer.class)))
				.thenReturn(mockedLocationAreaObject);

		// call service
		List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveCountyList(contract);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(1, resultList.size());
	}

	@Test
	public void testRetrieveCountyListWithResult() throws Exception {
		gov.usda.fsa.common.base.AgencyToken agencyToken = null;
		RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(agencyToken, "58303");// 58303

		// mock service
		List<MrtLookUpBO> boList = new ArrayList<MrtLookUpBO>();
		Mockito.when(mrtProxyBusinessService.retrieveCountyList(contract)).thenReturn(boList);

		// call service
		List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveCountyList(contract);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(0, resultList.size());
	}

	@Test
	public void testRetrieveStateList() throws Exception {
		gov.usda.fsa.common.base.AgencyToken agencyToken = null;
		RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(agencyToken);

		// mock service
		List<State> mockedStateList = new ArrayList<State>();
		State state = new State();
		state.setCode("69");
		state.setName("Missouri");
		state.setAbbreviation("MO");

		mockedStateList.add(state);
		Mockito.when(mrtProxyBusinessService.retrieveFLPStateList()).thenReturn(mockedStateList);

		// call service
		List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveStateList(contract);
		MrtLookUpBO mrtLookUpBO = resultList.get(0);

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() > 0);
		Assert.assertNotNull(mrtLookUpBO.getCode());
		Assert.assertNotNull(mrtLookUpBO.getDescription());
		Assert.assertNotNull(mrtLookUpBO.getRefenceIdentifier());
		Assert.assertNull(mrtLookUpBO.getType());
	}

	@Test
	public void testRetrieveServiceCenterList() throws Exception {
		gov.usda.fsa.common.base.AgencyToken agencyToken = null;
		RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(agencyToken, "MO");

		// mock service
		List<Office> boList = new ArrayList<Office>();
		Office bo = new Office();
		bo.setOfficeCode("Code");
		bo.setName("Name");
		bo.setOfficeResp("offRefId");
		bo.setRefId(12345);
		boList.add(bo);

		Mockito.when(mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("MO")).thenReturn(boList);

		// call to service
		List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveServiceCenterList(contract);

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() > 0);
	}

	@Test
	public void testRetrieveServiceCenterListWrongState() throws Exception {
		gov.usda.fsa.common.base.AgencyToken agencyToken = null;
		RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(agencyToken, "MO");
		contract.setStateCode("AA");

		// mock service
		List<Office> boList = new ArrayList<Office>();
		Mockito.when(mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("AA")).thenReturn(boList);

		// call to service
		List<MrtLookUpBO> resultList = mrtProxyBusinessService.retrieveServiceCenterList(contract);

		Assert.assertNotNull(resultList);
		Assert.assertTrue(resultList.size() == 0);
	}

	@Test
	public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode() throws Exception {
		String office_flp_code_str = "123";

		// mock service
		Office office = new Office();
		office.setOfficeCode("123");
		office.setId(123);

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add(office_flp_code_str);

		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

		// call to service
		String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

		Assert.assertNotNull(fsaCode);
		Assert.assertEquals("", fsaCode);
	}

	@Test
	public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_withSiteId() throws Exception {

		String flpCode = "21047";
		String expectedResult = "21047::8247";

		// mock service
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add(flpCode);

		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray)).thenReturn(officeList);

		List<Integer> flpIdCodes = new ArrayList<Integer>();
		flpIdCodes.add(21047);
		Mockito.when(mrtProxyBusinessService.retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes))
				.thenReturn(officeList);

		// call service
		String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(flpCode);

		Assert.assertNotNull(fsaCode);
		Assert.assertEquals(expectedResult, fsaCode);
		Assert.assertTrue(expectedResult.length() > 7);
		Assert.assertNotNull(expectedResult.substring(7));
		Assert.assertEquals(4, expectedResult.substring(7).length());
	}

	@Test
	public void testRetrieveFLPOfficeCodeListForScimsCustomerEmptyInput() throws Exception {
		List<String> fsaStAndLocArCodes = new ArrayList<String>();

		// mock service
		List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("")).thenReturn(mockedLocationAreaObject);

		// call to service
		List<LocationArea> objects = mrtProxyBusinessService
				.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

		Assert.assertEquals(0, objects.size());

	}

	@Test
	public void testRetrieveFLPOfficeCodeListForScimsCustomerCorrectCode() throws Exception {
		List<String> fsaStAndLocArCodes = new ArrayList<String>();
		fsaStAndLocArCodes.add("04001");

		// mock service
		List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
		LocationArea object = new LocationArea();
		LocationArea object1 = new LocationArea();
		LocationArea object2 = new LocationArea();
		LocationArea object3 = new LocationArea();
		mockedLocationAreaObject.add(object);
		mockedLocationAreaObject.add(object1);
		mockedLocationAreaObject.add(object2);
		mockedLocationAreaObject.add(object3);

		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("04001"))
				.thenReturn(mockedLocationAreaObject);

		// call to service
		List<LocationArea> objects = mrtProxyBusinessService
				.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

		Assert.assertEquals(4, objects.size());
	}

	@Test
	public void testRetrieveFLPOfficeCodeListForScimsCustomerWrongCode() throws Exception {
		List<String> fsaStAndLocArCodes = new ArrayList<String>();
		fsaStAndLocArCodes.add("00000");

		// mock service
		List<LocationArea> mockedLocationAreaObject = new ArrayList<LocationArea>();
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
				.thenReturn(mockedLocationAreaObject);

		// call service
		List<LocationArea> objects = mrtProxyBusinessService
				.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);

		Assert.assertEquals(0, objects.size());

	}


	public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerEmptyInput() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		AgencyToken token = null;

		// call to service
		List<Office> offices = 
		mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
		
		Assert.assertTrue(offices.isEmpty());
	}

	public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerExpectedException() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		AgencyToken token = null;
		LocationArea item = new LocationArea();
		flpOfficeList.add(item);

		List<Office> offices = 
		mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
		
		Assert.assertTrue(offices.isEmpty());

	}

	//@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerNoneEmptyInput() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		LocationArea item = new LocationArea();
		item.setStateLocationAreaCode("40001");
		flpOfficeList.add(item);
		LocationArea item2 = new LocationArea();
		item2.setStateLocationAreaCode("40002");
		flpOfficeList.add(item2);

		AgencyToken token = null;

		// mock service
		List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();

		String[] completeFlpLocArLstArray = { "40001", "40002" };
		Mockito.when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
				.thenReturn(flpOfficeLocXRefMRTBusinessObjects);

		// call to service
		List<Office> offices = 
		mrtProxyBusinessService.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);
		
		Assert.assertTrue(offices.isEmpty());
	}

	@Test
	public void testRetrieveFLPOfficeLocArXREFMRTBOListForScimsCustomerNoneEmptyInputWithData() throws Exception {
		// gov.usda.fsa.common.agencytoken.AgencyToken agencyToken = null;
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		LocationArea item = new LocationArea();
		item.setStateLocationAreaCode("58062");
		// item.setLocationAreaFLPCode("58062");
		flpOfficeList.add(item);

		// mock service
		List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);
		flpOfficeLocXRefMRTBusinessObjects.add(office);

		String[] completeFlpLocArLstArray = { "58062" };
		Mockito.when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray))
				.thenReturn(flpOfficeLocXRefMRTBusinessObjects);

		// call to service
		gov.usda.fsa.common.base.AgencyToken token = null;
		List<Office> results = mrtProxyBusinessService
				.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(flpOfficeList, token);

		Assert.assertEquals(1, results.size());
	}

	@Test
	public void test_retrieveStateByFlpFipsCode() throws Exception {
		String flpCode = "20324";

		// mock service
		State mokcedState = new State();
		Mockito.when(mockStateDataServiceProxy.byFlpFipsCode(flpCode, FlpStateProperties.code, FlpStateProperties.name,
				FlpStateProperties.abbreviation)).thenReturn(mokcedState);

		// call service
		State state = mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);

		Assert.assertNotNull(state);
	}

	@Test
	public void test_retrieveStateByFlpFipsCode_noStatefound() throws Exception {
		String flpCode = "";

		State mokcedState = null;
		Mockito.when(mockStateDataServiceProxy.byFlpFipsCode(flpCode, FlpStateProperties.code, FlpStateProperties.name,
				FlpStateProperties.abbreviation)).thenReturn(mokcedState);

		// call service
		State state = mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);

		Assert.assertNull(state);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveStateByFlpFipsCode_exception() throws Exception {
		String flpCode = null;

		// mock service
		Mockito.doThrow(Mockito.mock(DLSBusinessFatalException.class))
				.when(mrtProxyBusinessService.retrieveStateByFlpFipsCode(null));

		// call service
		mrtProxyBusinessService.retrieveStateByFlpFipsCode(flpCode);
	}

	@Test
	public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr() throws Exception {
		String stateAbbr = "MO";

		// mock service
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOffices.add(office);

		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr, FlpOfficeProperties.id,
				FlpOfficeProperties.mailingZipCode, FlpOfficeProperties.mailingAddrInfoLine,
				FlpOfficeProperties.mailingAddrLine, FlpOfficeProperties.mailingCity,
				FlpOfficeProperties.mailingStateAbbrev, FlpOfficeProperties.stateFipsCode,
				FlpOfficeProperties.stateName, FlpOfficeProperties.officeCode, FlpOfficeProperties.name,
				FlpOfficeProperties.countyName, FlpOfficeProperties.locCityName)).thenReturn(serviceCenterOffices);

		// call to service
		List<Office> offices = mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

		Assert.assertFalse(offices.isEmpty());
	}

	@Test
	public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_noData() throws Exception {
		String stateAbbr = "N/A";

		// mock service
		List<Office> serviceCenterOffices = new ArrayList<Office>();

		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr, FlpOfficeProperties.id,
				FlpOfficeProperties.mailingZipCode, FlpOfficeProperties.mailingAddrInfoLine,
				FlpOfficeProperties.mailingAddrLine, FlpOfficeProperties.mailingCity,
				FlpOfficeProperties.mailingStateAbbrev, FlpOfficeProperties.stateFipsCode,
				FlpOfficeProperties.stateName, FlpOfficeProperties.officeCode, FlpOfficeProperties.countyName,
				FlpOfficeProperties.locCityName)).thenReturn(serviceCenterOffices);

		// call to service
		List<Office> offices = mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);

		Assert.assertTrue(offices.isEmpty());
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFsaFlpServiceCenterOfficesByStateAbbr_exception() throws Exception {
		String stateAbbr = null;

		// mock service
		Mockito.doThrow(Mockito.mock(DLSBusinessFatalException.class))
				.when(mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(null));

		// call the service
		mrtProxyBusinessService.retrieveFsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);
	}

	@Test
	public void test_retrieveFlpAreaListByStateAbbr() throws Exception {
		String stateAbbr = "MO";

		// mock service
		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();
		LocationArea locArea = new LocationArea();
		fLPLocationAreaMRTBusinessObjectList.add(locArea);

		Mockito.when(mockLocationAreaDataServiceProxy.flpByStateAbbr(stateAbbr, FlpLocationAreaProperties.stateCode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.shortName, FlpLocationAreaProperties.stateRefId))
				.thenReturn(fLPLocationAreaMRTBusinessObjectList);

		// call to service
		List<LocationArea> locationAreaList = mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr(stateAbbr);
		Assert.assertFalse(locationAreaList.isEmpty());
	}

	@Test
	public void test_retrieveFlpAreaListByStateAbbr_noData() throws Exception {
		String stateAbbr = "N/A";

		// mock service
		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

		Mockito.when(mockLocationAreaDataServiceProxy.flpByStateAbbr(stateAbbr, FlpLocationAreaProperties.stateCode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.shortName, FlpLocationAreaProperties.stateRefId))
				.thenReturn(fLPLocationAreaMRTBusinessObjectList);

		// call to service
		List<LocationArea> locationAreaList = mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(locationAreaList.isEmpty());
	}

	public void test_retrieveFlpAreaListByStateAbbr_exception() throws Exception {
		String stateAbbr = null;

		List<LocationArea> locationAreaList = mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(locationAreaList.isEmpty());
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFSAStateList_DLSBusinessFatalException() throws Exception {
		List<MrtLookUpBO> mrtLookupBOList = new ArrayList<MrtLookUpBO>();

		MrtLookUpBO mrtLookupBO = new MrtLookUpBO(this.createAgencyToken());
		mrtLookupBO.setType("T");
		mrtLookupBOList.add(mrtLookupBO);
		RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
		Mockito.when(mrtProxyBusinessService.retrieveFSAStateList(Mockito.any(RetrieveFSAStateListBC.class)))
				.thenReturn(mrtLookupBOList);

		// call to service
		mrtProxyBusinessService.retrieveFSAStateList(retrieveFSAStateListBC);
	}

	@Test
	public void test_retrieveFSAStateList() throws Exception {
		List<State> mockedStateList = new ArrayList<State>();

		State state = new State();
		state.setCode("69");
		state.setName("Missouri");
		state.setAbbreviation("MO");
		mockedStateList.add(state);

		RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
		// Mockito.when(mockStateDataServiceProxy.allFSA()).thenReturn(mockedStateList);
		Mockito.when(mockStateDataServiceProxy.allFSA(FsaStateProperties.code, FsaStateProperties.name,
				FsaStateProperties.abbreviation)).thenReturn(mockedStateList);

		// call to service
		List<MrtLookUpBO> mrtLookupBO = mrtProxyBusinessService.retrieveFSAStateList(retrieveFSAStateListBC);

		Assert.assertNotNull(mrtLookupBO);
		Assert.assertTrue(mrtLookupBO.size() == 1);
		Assert.assertNotNull(mrtLookupBO.get(0).getCode());
		Assert.assertTrue("69".equalsIgnoreCase(mrtLookupBO.get(0).getCode()));
	}

	@Test
	public void test_retrieveFSAStateMap() throws Exception {
		List<State> mockStateList = new ArrayList<State>();

		State state = new State();
		state.setCode("69");
		state.setName("Missouri");
		state.setAbbreviation("MO");
		mockStateList.add(state);
		Map<String, MrtLookUpBO> mrtLookupBOMap = new HashMap<String, MrtLookUpBO>();

		MrtLookUpBO mrtLookupBO = new MrtLookUpBO(this.createAgencyToken());
		mrtLookupBOMap.put("1", mrtLookupBO);
		RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());

		Mockito.when(mockStateDataServiceProxy.allFSA(Mockito.any(FsaStateProperties.class),
				Mockito.any(FsaStateProperties.class), Mockito.any(FsaStateProperties.class)))
				.thenReturn(mockStateList);

		// call to service
		Map<String, MrtLookUpBO> mrtLookupBOMapReturned = mrtProxyBusinessService
				.retrieveFSAStateMap(retrieveFSAStateListBC);
		Assert.assertNotNull(mrtLookupBOMapReturned);
		Assert.assertTrue(mrtLookupBOMapReturned.size() == 1);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFSAStateMap_DLSBusinessFatalException() throws Exception {
		Map<String, MrtLookUpBO> mrtLookupBOMap = new HashMap<String, MrtLookUpBO>();

		MrtLookUpBO mrtLookupBO = new MrtLookUpBO(this.createAgencyToken());
		mrtLookupBOMap.put("1", mrtLookupBO);
		RetrieveFSAStateListBC retrieveFSAStateListBC = new RetrieveFSAStateListBC(this.createAgencyToken());
		Mockito.when(mrtProxyBusinessService.retrieveFSAStateMap(Mockito.any(RetrieveFSAStateListBC.class)))
				.thenReturn(mrtLookupBOMap);

		// call to service
		mrtProxyBusinessService.retrieveFSAStateMap(retrieveFSAStateListBC);
	}

	@Test
	public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices() throws Exception {
		List<String> fsaStAndLocArCodes = new ArrayList<String>();
		fsaStAndLocArCodes.add("04001");
		// mock service
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
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
				.thenReturn(mockedLocationAreaObject);
		List<LocationArea> fLPLocationAreListReturned = mrtProxyBusinessService
				.retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
		Assert.assertNotNull(fLPLocationAreListReturned);
		Assert.assertTrue(fLPLocationAreListReturned.size() == 4);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpLocationAreaCodesByServiceCenterOffices_DLSBusinessFatalException() throws Exception {
		String[] serviceCenters = { "04001" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(serviceCenters))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
		mrtProxyBusinessService.retrieveFlpLocationAreaCodesByServiceCenterOffices(serviceCenters);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveFLPOfficeCodeListForScimsCustomer_DLSBusinessFatalException() throws Exception {
		List<String> fsaStAndLocArCodes = new ArrayList<String>();
		fsaStAndLocArCodes.add("00000");
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea("00000"))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

		// call service
		mrtProxyBusinessService.retrieveFLPOfficeCodeListForScimsCustomer(fsaStAndLocArCodes, null);
	}

	@Test
	public void testRretrieveInterestRateForAssistanceType_BusinessServiceBindingException() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);
		Date cutOffDate = date.getTime();
		RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(null, "50010",
				cutOffDate);
		Mockito.when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));

		Double interestRate = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);
		Assert.assertTrue(0.00 == interestRate.doubleValue());
	}

	@Test
	public void testRretrieveInterestRateForAssistanceType_Exception() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);
		Date cutOffDate = date.getTime();
		RetrieveInterestRateForAssistanceTypeBC contract = new RetrieveInterestRateForAssistanceTypeBC(null, "50010",
				cutOffDate);
		Mockito.when(mockInterestRateDataServiceProxy.byTypeIdAndDate(50010, cutOffDate))
				.thenThrow(new NullPointerException("TestException"));

		Double interestRate = mrtProxyBusinessService.retrieveInterestRateForAssistanceType(contract);
		Assert.assertTrue(0.00 == interestRate.doubleValue());
	}

	@Test(expected = DLSBusinessStopException.class)
	public void testRetrieveFLPOfficeMRTBusinessObjectReadFacadeList_DLSBusinessStopException() throws Exception {
		Mockito.when(mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("MO"))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("MO");
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveStateList_DLSBusinessFatalException() throws Exception {
		RetrieveMRTStateListBC contract = new RetrieveMRTStateListBC(null);
		Mockito.when(mrtProxyBusinessService.retrieveFLPStateList())
				.thenThrow(new NullPointerException("TestException"));

		// call service
		mrtProxyBusinessService.retrieveStateList(contract);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveServiceCenterList_DLSBusinessFatalException() throws Exception {

		RetrieveMRTServiceCenterListBC contract = new RetrieveMRTServiceCenterListBC(null, "MO");
		Mockito.when(mrtProxyBusinessService.retrieveFLPOfficeMRTBusinessObjectList("MO"))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveServiceCenterList(contract);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveCountyList_DLSBusinessFatalException() throws Exception {

		RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
		Mockito.when(mrtProxyBusinessService.retrieveCountyList(contract))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveCountyList(contract);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void testRetrieveCountyList_BusinessServiceBindingException() throws Exception {

		RetrieveMRTCountyListBC contract = new RetrieveMRTCountyListBC(null, "123");
		Mockito.when(mrtProxyBusinessService.retrieveCountyList(contract))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
		mrtProxyBusinessService.retrieveCountyList(contract);
	}

	@Test // (expected = DLSBusinessFatalException.class)
	public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_DLSBusinessFatalException() throws Exception {
		String office_flp_code_str = "123";

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add(office_flp_code_str);

		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
				.thenThrow(new NullPointerException("TestException"));

		String code = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

		Assert.assertEquals("CBS_ERROR", code);
	}

	@Test
	public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_NoError() throws Exception {
		List<Office> serviceCenterOfficesList = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("123");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOfficesList.add(office);

		Office office2 = new Office();
		office2.setOfficeCode("312");
		office2.setId(61012);
		office2.setSiteId(6112);
		serviceCenterOfficesList.add(office2);
		String office_flp_code_str = "123";

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add(office_flp_code_str);
		List<Integer> flpCodeIntegerList = new ArrayList<Integer>();
		flpCodeIntegerList.add(21047);
		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		Integer[] flpIntegerCodesArray = flpCodeIntegerList.toArray(new Integer[0]);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
				.thenReturn(serviceCenterOfficesList);
		Mockito.when(mockOfficeDataServiceProxy.byOfficeIdList(flpIntegerCodesArray,FsaOfficeProperties.id, 
				FsaOfficeProperties.locStateAbbrev,	FsaOfficeProperties.locCityName,
				FsaOfficeProperties.stateAbbrev, FsaOfficeProperties.officeCode,FsaOfficeProperties.name,
				FsaOfficeProperties.cityFipsCode, FsaOfficeProperties.refId, FsaOfficeProperties.siteId,
				FsaOfficeProperties.mailingZipCode, FsaOfficeProperties.mailingAddrInfoLine, FsaOfficeProperties.mailingAddrLine))
				.thenReturn(serviceCenterOfficesList);

		String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);

		Assert.assertTrue("123::8247".equalsIgnoreCase(fsaCode));
	}

	@Test
	public void testGetFSAStateCountyCodeFromStateLocationAreaFLPCode_BusinessServiceBindingException()
			throws Exception {
		List<Office> serviceCenterOfficesList = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("123");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOfficesList.add(office);
		String office_flp_code_str = "123";

		List<String> flpCodeList = new ArrayList<String>();
		flpCodeList.add(office_flp_code_str);
		List<Integer> flpCodeIntegerList = new ArrayList<Integer>();
		flpCodeIntegerList.add(21047);
		String[] flpStringCodesArray = flpCodeList.toArray(new String[0]);
		Integer[] flpIntegerCodesArray = flpCodeIntegerList.toArray(new Integer[0]);
		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(flpStringCodesArray))
				.thenReturn(serviceCenterOfficesList);
		Mockito.when(mockOfficeDataServiceProxy.byOfficeIdList(flpIntegerCodesArray))
				.thenThrow(new BusinessServiceBindingException("TestException"));

		String fsaCode = mrtProxyBusinessService.getFSAStateCountyCodeFromStateLocationAreaFLPCode(office_flp_code_str);
		Assert.assertTrue(StringUtil.isEmptyString(fsaCode));
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveInterestRates_DLSBusinessFatalException() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2012, 1, 1);
		Integer[] typeIds = { 50500 };
		Mockito.when(mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime()))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveInterestRates(typeIds, date.getTime());
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpStateOffices_DLSBusinessFatalException() throws Exception {

		Mockito.when(mrtProxyBusinessService.retrieveFlpStateOffices())
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveFlpStateOffices();

	}

	@Test
	public void test_retrieveOfficesByFlpCodes() {
		List<String> stringList = new ArrayList<String>();
		stringList.add("A");
		List<Office> serviceCenterOfficesList = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOfficesList.add(office);
		Mockito.when(mrtProxyBusinessService.retrieveOfficesByFlpCodes(stringList))
				.thenReturn(serviceCenterOfficesList);
		List<Office> officeList = mrtProxyBusinessService.retrieveOfficesByFlpCodes(stringList);
		Assert.assertNotNull(officeList);
		Assert.assertTrue(officeList.size() == 1);
	}

	@Test
	public void test_retrieveFSACountyList() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		LocationArea item = new LocationArea();
		item.setStateLocationAreaCode("40001");
		item.setCode("C");
		item.setName("TestName");
		item.setId(1);
		flpOfficeList.add(item);
		RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
		Mockito.when(mockLocationAreaDataServiceProxy.agByStateFsa(Mockito.any(String.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class))).thenReturn(flpOfficeList);
		List<MrtLookUpBO> mrtLookupList = mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
		assertNotNull(mrtLookupList);
		assertTrue(mrtLookupList.size() == 1);
		assertTrue("1".equalsIgnoreCase(mrtLookupList.get(0).getRefenceIdentifier()));
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFSACountyList_BusinessServiceBindingExceptionCovered() throws Exception {
		RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
		Mockito.when(mockLocationAreaDataServiceProxy.agByStateFsa(Mockito.any(String.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class)))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
		mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFSACountyList_ThrowableCovered() throws Exception {
		RetrieveFsaCountyListBC retrieveFsaCountyListBC = new RetrieveFsaCountyListBC(this.createAgencyToken(), "CA");
		Mockito.when(mockLocationAreaDataServiceProxy.agByStateFsa(Mockito.any(String.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class), Mockito.any(FsaLocationAreaProperties.class),
				Mockito.any(FsaLocationAreaProperties.class))).thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveFSACountyList(retrieveFsaCountyListBC);
	}

	@Test
	public void test_retrieveIntRatesByRateTypeIdLstAndDtRng() throws Exception {
		List<String> typeIds = new ArrayList<String>();
		typeIds.add("1");
		List<InterestRate> interestRates = new ArrayList<InterestRate>();
		InterestRate interestRate = new InterestRate();
		interestRate.setId(1);
		interestRate.setIntRate(BigDecimal.ONE);
		interestRates.add(interestRate);
		Date fromDate = Calendar.getInstance().getTime();
		Date toDate = Calendar.getInstance().getTime();
		
		Mockito.when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(
			    ArgumentMatchers.<List<InterestTypeId>>any(),
			    ArgumentMatchers.any(Date.class),
			    ArgumentMatchers.any(Date.class))).thenReturn(interestRates);
		
		// Mockito.when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(interestTypeIdList,fromDate,toDate)).thenReturn(interestRates);
		List<InterestRate> interestRateListReturned = mrtProxyBusinessService
				.retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
		Assert.assertNotNull(interestRateListReturned);
		Assert.assertTrue(interestRateListReturned.size() == 1);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveIntRatesByRateTypeIdLstAndDtRng_DLSBusinessFatalException() throws Exception {
		List<String> typeIds = new ArrayList<String>();
		typeIds.add("1");
		Date fromDate = Calendar.getInstance().getTime();
		Date toDate = Calendar.getInstance().getTime();
		
		Mockito.when(mockInterestRateDataServiceProxy.byTypeIdListAndDateRange(
			    ArgumentMatchers.<List<InterestTypeId>>any(),
			    ArgumentMatchers.any(Date.class),
			    ArgumentMatchers.any(Date.class)))
		        .thenThrow(new NullPointerException("Test Exception"));
		
		mrtProxyBusinessService.retrieveIntRatesByRateTypeIdLstAndDtRng(typeIds, fromDate, toDate);
	}

	@Test
	public void test_retrieveFlpStateOffices_light() throws Exception {
		List<Office> allStateOffices = new ArrayList<Office>();
		Office office = new Office();
		office.setId(1);
		office.setAgencyAbbr("FSA");
		office.setCounty(true);
		allStateOffices.add(office);
		Mockito.when(mockOfficeDataServiceProxy.allFlpStateOffices(Mockito.any(FlpOfficeProperties.class),
				Mockito.any(FlpOfficeProperties.class), Mockito.any(FlpOfficeProperties.class),
				Mockito.any(FlpOfficeProperties.class))).thenReturn(allStateOffices);

		List<Office> officeListReturned = mrtProxyBusinessService.retrieveFlpStateOffices_light();
		Assert.assertNotNull(officeListReturned);
		Assert.assertTrue(officeListReturned.size() == 1);
		Assert.assertTrue(officeListReturned.get(0).getId() == 1);
		Assert.assertTrue("FSA".equalsIgnoreCase(officeListReturned.get(0).getAgencyAbbr()));
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpStateOffices_light_ExceptionCovered() throws Exception {
		Mockito.when(mockOfficeDataServiceProxy.allFlpStateOffices(Mockito.any(FlpOfficeProperties.class),
				Mockito.any(FlpOfficeProperties.class), Mockito.any(FlpOfficeProperties.class),
				Mockito.any(FlpOfficeProperties.class))).thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveFlpStateOffices_light();
	}

	@Test
	public void test_retrieveFlpServiceCentersByStateOffices_light() throws Exception {

		List<Office> serviceCenterOffices = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOffices.add(office);
		String[] flp = { "CA" };
		String[] stateOffices = { "OF" };
		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(flp,
				FlpOfficeProperties.officeCode, FlpOfficeProperties.countyFipsCode, FlpOfficeProperties.countyName))
				.thenReturn(serviceCenterOffices);

		List<Office> officeListReturned = mrtProxyBusinessService
				.retrieveFlpServiceCentersByStateOffices_light(stateOffices);
		Assert.assertNotNull(officeListReturned);
	}

	@Test
	public void test_retrieveFlpServiceCentersByStateAbbr() throws Exception {
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("21047");
		office.setId(21047);
		office.setSiteId(8247);
		serviceCenterOffices.add(office);
		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(Mockito.any(String.class)))
				.thenReturn(serviceCenterOffices);
		List<Office> officeListReturned = mrtProxyBusinessService.retrieveFlpServiceCentersByStateAbbr("AL");
		Assert.assertNotNull(officeListReturned);
		Assert.assertTrue(officeListReturned.size() == 1);
		Assert.assertTrue(officeListReturned.get(0).getId() == 21047);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpAreaListByStateAbbr_BusinessServiceBindingExceptionCovered() throws Exception {
		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(Mockito.any(String.class)))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveFlpServiceCentersByStateAbbr("AL");
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveFlpServiceCentersByStateAbbr_ExceptionCovered() throws Exception {
		Mockito.when(mockLocationAreaDataServiceProxy.flpByStateAbbr(Mockito.any(String.class),
				Mockito.any(FlpLocationAreaProperties.class), Mockito.any(FlpLocationAreaProperties.class),
				Mockito.any(FlpLocationAreaProperties.class), Mockito.any(FlpLocationAreaProperties.class),
				Mockito.any(FlpLocationAreaProperties.class)))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
		mrtProxyBusinessService.retrieveFlpAreaListByStateAbbr("AL");
	}

	@Test
	public void test_retrieveOrgChartsByEmployeeId() throws Exception {
		List<EmployeeOrgChart> employeeOrgChartList = new ArrayList<EmployeeOrgChart>();
		EmployeeOrgChart employeeOrgChart = new EmployeeOrgChart();
		employeeOrgChart.setEmployeeId("1");
		employeeOrgChart.setNoteText("Test");
		employeeOrgChartList.add(employeeOrgChart);
		Mockito.when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId(Mockito.any(String.class)))
				.thenReturn(employeeOrgChartList);
		List<EmployeeOrgChart> employeeOrgChartListReturned = mrtProxyBusinessService
				.retrieveOrgChartsByEmployeeId("1");
		Assert.assertNotNull(employeeOrgChartListReturned);
		Assert.assertTrue(employeeOrgChartListReturned.size() == 1);
		Assert.assertTrue("1".equalsIgnoreCase(employeeOrgChartListReturned.get(0).getEmployeeId()));

	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_retrieveOrgChartsByEmployeeId_ExceptionCovered() throws Exception {
		Mockito.when(mockEmployeeDataServiceProxy.orgChartsByEmployeeId(Mockito.any(String.class)))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.retrieveOrgChartsByEmployeeId("1");
	}

	@Test
	public void test_flpByFlpCodeList() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		LocationArea item = new LocationArea();
		item.setStateLocationAreaCode("40001");
		item.setCode("C");
		item.setName("TestName");
		item.setId(1);
		flpOfficeList.add(item);
		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds)).thenReturn(flpOfficeList);
		List<LocationArea> flpOfficeListReturned = mrtProxyBusinessService.flpByFlpCodeList(flpCds);
		Assert.assertNotNull(flpOfficeListReturned);
		Assert.assertTrue(flpOfficeListReturned.size() == 1);
		Assert.assertTrue(1 == flpOfficeListReturned.get(0).getId());
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_flpByFlpCodeList_ExceptionCovered() throws Exception {
		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
				.thenThrow(new NullPointerException("TestException"));
		mrtProxyBusinessService.flpByFlpCodeList(flpCds);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_flpByFlpCodeList_BusinessServiceBindingExceptionCovered() throws Exception {
		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds))
				.thenThrow(new BusinessServiceBindingException(new ErrorMessage("TestException", "e1")));
		mrtProxyBusinessService.flpByFlpCodeList(flpCds);
	}

	@Test
	public void test_flpByFlpCodeList_light() throws Exception {
		List<LocationArea> flpOfficeList = new ArrayList<LocationArea>();
		LocationArea item = new LocationArea();
		item.setStateLocationAreaCode("40001");
		item.setCode("C");
		item.setName("TestName");
		item.setId(1);
		flpOfficeList.add(item);

		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, FlpLocationAreaProperties.shortName))
				.thenReturn(flpOfficeList);

		List<LocationArea> flpOfficeListReturned = mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
		Assert.assertNotNull(flpOfficeListReturned);
		Assert.assertTrue(flpOfficeListReturned.size() == 1);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_flpByFlpCodeList_light_BusinessServiceBindingException() throws Exception {
		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, FlpLocationAreaProperties.shortName))
				.thenThrow(new BusinessServiceBindingException("Test Error"));
		mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
	}

	@Test(expected = DLSBusinessFatalException.class)
	public void test_flpByFlpCodeList_light_Exception() throws Exception {
		String[] flpCds = { "CA" };
		Mockito.when(mockLocationAreaDataServiceProxy.flpByFlpCodeList(flpCds, FlpLocationAreaProperties.stateCode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.code, FlpLocationAreaProperties.name, FlpLocationAreaProperties.shortName))
				.thenThrow(new NullPointerException("Test Error"));
		mrtProxyBusinessService.flpByFlpCodeList_light(flpCds);
	}

//	public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
//
//	}
//
//	public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {
//
//	}

	@Test
	public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode() throws Exception {

		List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();
		LocationArea flpLocationArea1 = new LocationArea();
		flpLocationArea1.setStateLocationAreaCode("01001");
		flpLocationArea.add(flpLocationArea1);

		// mock test cases
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);

		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea(Mockito.any(String.class)))
				.thenReturn(flpLocationArea);

		Mockito.when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(Mockito.anyList()))
				.thenReturn(officeList);

		String fsaStateCountyCode = "01020";

		// call to business service
		List<Office> allStateOffices = mrtProxyBusinessService
				.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

		Assert.assertTrue(!allStateOffices.isEmpty());

	}

	@Test
	public void test_retrieveFLPOfficeCodeListByFsaStateCountyCode_emptyLocataionAreaList() throws Exception {

		List<LocationArea> flpLocationArea = new ArrayList<LocationArea>();

		// mock test cases
		Office office = new Office();
		List<Office> officeList = new ArrayList<Office>();
		officeList.add(office);

		Mockito.when(mockLocationAreaDataServiceProxy.flpByFsaStateLocArea(Mockito.any(String.class)))
				.thenReturn(flpLocationArea);

		Mockito.when(mockOfficeDataServiceProxy.flpServiceCenterOfficesByFlpStateAndLocAreas(Mockito.anyList()))
				.thenReturn(officeList);

		String fsaStateCountyCode = "01020";

		// call to business service
		List<Office> allStateOffices = mrtProxyBusinessService
				.retrieveFLPOfficeCodeListByFsaStateCountyCode(fsaStateCountyCode);

		Assert.assertTrue(allStateOffices.isEmpty());

	}

	@Test
	public void test_retrieveFlpServiceCentersByOIP() throws Exception {

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

		Mockito.when(mockOfficeDataServiceProxy.flpOfficesByFlpCodeList(Mockito.any(String[].class)))
				.thenReturn(officeList);

		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByOfficeIdList(Mockito.any(Integer[].class)))
				.thenReturn(officeList2);

		// call to business service
		List<Office> allStateOffices = mrtProxyBusinessService.retrieveFlpServiceCentersByOIP(stateOffices);

		Assert.assertTrue(!allStateOffices.isEmpty());

	}

	@Test
	public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {

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

		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
			    ArgumentMatchers.anyString(),
			    ArgumentMatchers.any(FlpOfficeProperties.class))).thenReturn(officeList);		

		String stateAbbr = "HI";

		// call to business service
		List<FlpOfficeLocationAreaBO> allFlpOfficeLocationAreas = mrtProxyBusinessService
				.retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(!allFlpOfficeLocationAreas.isEmpty());

	}

	@Test
	public void test_retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr_Exception() throws Exception {

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

		//Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(Mockito.anyString(),
		//		(FlpOfficeProperties) Mockito.anyVararg())).thenThrow(new BusinessServiceBindingException());
		
		Mockito.when(mockOfficeDataServiceProxy.fsaFlpServiceCenterOfficesByStateAbbr(
			    ArgumentMatchers.anyString(),
			    ArgumentMatchers.any())).thenThrow(new BusinessServiceBindingException());		

		String stateAbbr = "HI";

		// call to business service
		List<FlpOfficeLocationAreaBO> allFlpOfficeLocationAreas = mrtProxyBusinessService
				.retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(allFlpOfficeLocationAreas.isEmpty());

	}

	@Test
	public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr() throws Exception {

		List<LocationArea> fsaLocAreaList = new ArrayList<LocationArea>();

		LocationArea locationArea1 = new LocationArea();
		locationArea1.setStateLocationAreaCode("63");
		locationArea1.setCode("001");
		locationArea1.setStateName("HAWAII");
		locationArea1.setName("Name 1");
		fsaLocAreaList.add(locationArea1);

		LocationArea locationArea2 = new LocationArea();
		locationArea2.setStateLocationAreaCode("63");
		locationArea2.setCode("000");
		locationArea2.setStateName("HAWAII");
		locationArea2.setName("Name 2");
		fsaLocAreaList.add(locationArea2);

		LocationArea locationArea3 = new LocationArea();
		locationArea3.setStateLocationAreaCode("63");
		locationArea3.setCode("003");
		locationArea3.setStateName("HAWAII");
		locationArea3.setName("Name 3");
		fsaLocAreaList.add(locationArea3);

		//Mockito.when(mockLocationAreaDataServiceProxy.byStateAbbr(Mockito.anyString(),
		//		(FsaLocationAreaProperties) Mockito.anyVararg())).thenReturn(fsaLocAreaList);
		
		Mockito.when(mockLocationAreaDataServiceProxy.byStateAbbr(
			    ArgumentMatchers.anyString(),
			    ArgumentMatchers.any())).thenThrow(new BusinessServiceBindingException());		

		String stateAbbr = "HI";

		List<FipsOfficeLocationAreaBO> result = mrtProxyBusinessService
				.retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(!result.isEmpty());

	}

	@Test
	public void test_retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr_exception() throws Exception {

		List<LocationArea> fsaLocAreaList = new ArrayList<LocationArea>();

		LocationArea locationArea1 = new LocationArea();
		locationArea1.setStateLocationAreaCode("63");
		locationArea1.setCode("001");
		locationArea1.setStateName("HAWAII");
		locationArea1.setName("Name 1");
		fsaLocAreaList.add(locationArea1);

		LocationArea locationArea2 = new LocationArea();
		locationArea2.setStateLocationAreaCode("63");
		locationArea2.setCode("000");
		locationArea2.setStateName("HAWAII");
		locationArea2.setName("Name 2");
		fsaLocAreaList.add(locationArea2);

		LocationArea locationArea3 = new LocationArea();
		locationArea3.setStateLocationAreaCode("63");
		locationArea3.setCode("003");
		locationArea3.setStateName("HAWAII");
		locationArea3.setName("Name 3");
		fsaLocAreaList.add(locationArea3);

		//Mockito.when(mockLocationAreaDataServiceProxy.byStateAbbr(Mockito.anyString(),
		//		(FsaLocationAreaProperties) Mockito.anyVararg())).thenThrow(new BusinessServiceBindingException());
		
		Mockito.when(mockLocationAreaDataServiceProxy.byStateAbbr(
			    ArgumentMatchers.anyString(),
			    ArgumentMatchers.any())).thenThrow(new BusinessServiceBindingException());		

		String stateAbbr = "HI";

		List<FipsOfficeLocationAreaBO> result = mrtProxyBusinessService
				.retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(stateAbbr);

		Assert.assertTrue(result.isEmpty());

	}

}
