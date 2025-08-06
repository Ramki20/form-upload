package gov.usda.fsa.fcao.flp.ola.core.service.external.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usda.fsa.common.base.AgencyBusinessException;
import gov.usda.fsa.fcao.flp.ola.core.bo.AnsiCounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.AnsiState;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLACounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLAState;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.CBSClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.LocationServiceClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApiResponse;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.AssociatedAnsiCounty;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.County;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Office;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.State;

/**
 * @author Matt Tirupathi
 * 
 */
@ExtendWith(MockitoExtension.class)
public class StateCountyServiceImplTest {

	@Autowired
	private OlaAgencyToken token;

	@InjectMocks
	private StateCountyServiceImpl stateCountyService = new StateCountyServiceImpl();

	@Mock
	LocationServiceClient locationServiceClient;
	
	@Mock
	CBSClient cbsClient;
	
	Office office = new Office();
	Office otherOffice = new Office();

	public StateCountyServiceImpl getService() {
		return stateCountyService;
	}

	@SuppressWarnings("deprecation")
	@Test
	public void getStates() throws AgencyBusinessException {
		MockitoAnnotations.initMocks(this);

		State state = new State();
		state.setCode("01");
		state.setName("Alabama");
		state.setAbbreviation("AL");
		state.setAnsiCode("01001");

		County county = new County();
		county.setCode("001");
		county.setName("Autunga");
		county.setOfficeIdentifier("1234");
		county.setStateCode("01");
		county.setAssociatedAnsiCounties(new ArrayList<AssociatedAnsiCounty>());
		List<County> counties = new ArrayList<>();
		counties.add(county);

		OLACounty flpcounty = new OLACounty();
		Set<OLACounty> countySet = new HashSet<>();
		flpcounty.setFlpOfficeCode("300");
		countySet.add(flpcounty);

		List<State> states = new ArrayList<>();
		states.add(state);
		
		Office office = new Office();
		
		office.setHqCountyCode("21236");
		office.setLocationCityName("townsville");
		office.setLocationStateAbbreviation("AL");
		office.setLocationStreetAddress("321 Street St.");
		office.setLocationZipCode("45621");
		office.setMailingAddressInformationLine("Fake");
		office.setOfficeId(1234l);
		office.setOfficeName("Dunder");
		office.setOfficeType("Paper");
		//office.setServicedLocationAreas("yes");
		
		office.setStateCode("Alive"); 
		List<Office> offices= new ArrayList<Office>();
		offices.add(office);

		ApiResponse<Office> apiResponseOffice = new ApiResponse<>();
		apiResponseOffice.setData(offices);
		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(states);
		
		ApiResponse<County> apiResponseCounty = new ApiResponse<>();
		apiResponseCounty.setData(counties);

		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);
		
		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty);
		
		Mockito.when(locationServiceClient.getOfficesByIdsAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseOffice);

		stateCountyService.clearCache();
		Set<OLAState> statesReceived = stateCountyService.findAllStatesAndCounties();

		Assertions.assertTrue(statesReceived != null, "true is true");

		for (OLAState olaState : statesReceived) {

			Assertions.assertEquals("01", olaState.getStateCode());

			Assertions.assertEquals("Alabama", olaState.getStateName());
			Assertions.assertEquals("AL", olaState.getStateAbbreviation());
			Assertions.assertEquals("01001", olaState.getAnsiCode());

			for (OLACounty olaCounty : olaState.getCounties()) {

				Assertions.assertEquals("001", olaCounty.getCountyCode());
				Assertions.assertEquals("Autunga", olaCounty.getCountyName());
				Assertions.assertEquals("01", olaCounty.getStateFLPCode());
				Assertions.assertNotNull(countySet.size());

			}

		}
	}

	@Test
	public void getAnsiStateList() {

		Set<AnsiState> ansiStateSet = stateCountyService.getAnsiStateList(getStateList());

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());

	}

	@Test
	public void fillAnsiStates() {

		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(getStateList());
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);
		

		Set<AnsiState> ansiStateSet = stateCountyService.fillAnsiStates();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());

		for (AnsiState ansiState : ansiStateSet) {

			if (ansiState.getAnsiCode().equals("01")) {

				Assertions.assertEquals("01", ansiState.getAnsiCode());
				Assertions.assertEquals("Alabama", ansiState.getStateName());
				Assertions.assertEquals("AL", ansiState.getStateAbbreviation());
				Assertions.assertNull(ansiState.getCounties());

			} else if (ansiState.getAnsiCode().equals("02")) {

				Assertions.assertEquals("02", ansiState.getAnsiCode());
				Assertions.assertEquals("Alaska", ansiState.getStateName());
				Assertions.assertEquals("AK", ansiState.getStateAbbreviation());
				Assertions.assertNull(ansiState.getCounties());

			} else if (ansiState.getAnsiCode().equals("05")) {
				Assertions.assertEquals("05", ansiState.getAnsiCode());
				Assertions.assertEquals("Arizona", ansiState.getStateName());
				Assertions.assertEquals("AZ", ansiState.getStateAbbreviation());
				Assertions.assertNull(ansiState.getCounties());

			} else {
				Assertions.assertTrue(false);
			}
		}
	}

	@Test
	public void fillAnsiStates_empty() {

		List<State> allAnsiStatesList = new ArrayList<>();
		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(allAnsiStatesList);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);
		
		Set<AnsiState> ansiStateSet = stateCountyService.fillAnsiStates();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(0, ansiStateSet.size());

	}

	@Test
	public void fillAnsiCounties() {

		ApiResponse<County> apiResponseCounty01 = new ApiResponse<>();
		apiResponseCounty01.setData(getCounties01());

		ApiResponse<County> apiResponseCounty02 = new ApiResponse<>();
		apiResponseCounty02.setData(getCounties02());
		
		ApiResponse<County> apiResponseCounty05 = new ApiResponse<>();
		apiResponseCounty05.setData(getCounties05());
		
		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty01)
		.thenReturn(apiResponseCounty02).thenReturn(apiResponseCounty05);		

		Set<AnsiState> ansiStateSet = stateCountyService.getAnsiStateList(getStateList());

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());

		stateCountyService.fillAnsiCounties(ansiStateSet);

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());

		for (AnsiState ansiState : ansiStateSet) {

			if (ansiState.getAnsiCode().equals("01")) {

				Assertions.assertEquals("01", ansiState.getAnsiCode());
				Assertions.assertEquals("Alabama", ansiState.getStateName());
				Assertions.assertEquals("AL", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(2, ansiState.getCounties().size());

				for (AnsiCounty ansiCounty : ansiState.getCounties()) {

					if (ansiCounty.getCountyCode().equals("001")) {
						Assertions.assertEquals("001", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Autunga", ansiCounty.getCountyName());

					} else if (ansiCounty.getCountyCode().equals("003")) {
						Assertions.assertEquals("003", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Baldwin", ansiCounty.getCountyName());
					} else {
						Assertions.assertTrue(false);
					}
				}

			} else if (ansiState.getAnsiCode().equals("02")) {

				Assertions.assertEquals("02", ansiState.getAnsiCode());
				Assertions.assertEquals("Alaska", ansiState.getStateName());
				Assertions.assertEquals("AK", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(3, ansiState.getCounties().size());

			} else if (ansiState.getAnsiCode().equals("05")) {
				Assertions.assertEquals("05", ansiState.getAnsiCode());
				Assertions.assertEquals("Arizona", ansiState.getStateName());
				Assertions.assertEquals("AZ", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(4, ansiState.getCounties().size());

			} else {
				Assertions.assertTrue(false);
			}
		}
	}

	@Test
	public void buildAnsiStatesAndCounties() {

		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(getStateList());
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		ApiResponse<County> apiResponseCounty01 = new ApiResponse<>();
		apiResponseCounty01.setData(getCounties01());

		ApiResponse<County> apiResponseCounty02 = new ApiResponse<>();
		apiResponseCounty02.setData(getCounties02());
		
		ApiResponse<County> apiResponseCounty05 = new ApiResponse<>();
		apiResponseCounty05.setData(getCounties05());
		
		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty01)
		.thenReturn(apiResponseCounty02).thenReturn(apiResponseCounty05);		

		Set<AnsiState> ansiStateSet = stateCountyService.buildAnsiStatesAndCounties();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());
	}

	@Test
	public void buildAnsiStatesAndCounties_empty() {

		List<State> allAnsiStatesList = new ArrayList<>();
		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(allAnsiStatesList);

		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);
		
		Set<AnsiState> ansiStateSet = stateCountyService.buildAnsiStatesAndCounties();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(0, ansiStateSet.size());

	}

	@Test
	public void buildAnsiStatesAndCounties_null() {
		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(null);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		Set<AnsiState> ansiStateSet = stateCountyService.buildAnsiStatesAndCounties();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(0, ansiStateSet.size());

	}

	@Test
	public void findAnsiStatesAndCounties() {

		
		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(getStateList());
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);
		
		ApiResponse<County> apiResponseCounty01 = new ApiResponse<>();
		apiResponseCounty01.setData(getCounties01());

		ApiResponse<County> apiResponseCounty02 = new ApiResponse<>();
		apiResponseCounty02.setData(getCounties02());
		
		ApiResponse<County> apiResponseCounty05 = new ApiResponse<>();
		apiResponseCounty05.setData(getCounties05());
		
		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty01)
		.thenReturn(apiResponseCounty02).thenReturn(apiResponseCounty05);		

		/*
		 * Builds
		 */
		Set<AnsiState> ansiStateSet = stateCountyService.findAnsiStatesAndCounties();

		Assertions.assertNotNull(ansiStateSet);

		Assertions.assertEquals(3, ansiStateSet.size());
		
		for (AnsiState ansiState : ansiStateSet) {

			if (ansiState.getAnsiCode().equals("01")) {

				Assertions.assertEquals("01", ansiState.getAnsiCode());
				Assertions.assertEquals("Alabama", ansiState.getStateName());
				Assertions.assertEquals("AL", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(2, ansiState.getCounties().size());

				for (AnsiCounty ansiCounty : ansiState.getCounties()) {

					if (ansiCounty.getCountyCode().equals("001")) {
						Assertions.assertEquals("001", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Autunga", ansiCounty.getCountyName());

					} else if (ansiCounty.getCountyCode().equals("003")) {
						Assertions.assertEquals("003", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Baldwin", ansiCounty.getCountyName());
					} else {
						Assertions.assertTrue(false);
					}
				}

			} else if (ansiState.getAnsiCode().equals("02")) {

				Assertions.assertEquals("02", ansiState.getAnsiCode());
				Assertions.assertEquals("Alaska", ansiState.getStateName());
				Assertions.assertEquals("AK", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(3, ansiState.getCounties().size());

			} else if (ansiState.getAnsiCode().equals("05")) {
				Assertions.assertEquals("05", ansiState.getAnsiCode());
				Assertions.assertEquals("Arizona", ansiState.getStateName());
				Assertions.assertEquals("AZ", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(4, ansiState.getCounties().size());

			} else {
				Assertions.assertTrue(false);
			}
		}

		/*
		 * Loads from Cache
		 */
		Set<AnsiState> ansiStateSet2 = stateCountyService.findAnsiStatesAndCounties();

		Assertions.assertNotNull(ansiStateSet2);

		Assertions.assertEquals(3, ansiStateSet2.size());

		for (AnsiState ansiState : ansiStateSet2) {

			if (ansiState.getAnsiCode().equals("01")) {

				Assertions.assertEquals("01", ansiState.getAnsiCode());
				Assertions.assertEquals("Alabama", ansiState.getStateName());
				Assertions.assertEquals("AL", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(2, ansiState.getCounties().size());

				for (AnsiCounty ansiCounty : ansiState.getCounties()) {

					if (ansiCounty.getCountyCode().equals("001")) {
						Assertions.assertEquals("001", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Autunga", ansiCounty.getCountyName());

					} else if (ansiCounty.getCountyCode().equals("003")) {
						Assertions.assertEquals("003", ansiCounty.getCountyCode());
						Assertions.assertEquals("01", ansiCounty.getStateCode());
						Assertions.assertEquals("Baldwin", ansiCounty.getCountyName());
					} else {
						Assertions.assertTrue(false);
					}
				}

			} else if (ansiState.getAnsiCode().equals("02")) {

				Assertions.assertEquals("02", ansiState.getAnsiCode());
				Assertions.assertEquals("Alaska", ansiState.getStateName());
				Assertions.assertEquals("AK", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(3, ansiState.getCounties().size());

			} else if (ansiState.getAnsiCode().equals("05")) {
				Assertions.assertEquals("05", ansiState.getAnsiCode());
				Assertions.assertEquals("Arizona", ansiState.getStateName());
				Assertions.assertEquals("AZ", ansiState.getStateAbbreviation());
				Assertions.assertNotNull(ansiState.getCounties());
				Assertions.assertEquals(4, ansiState.getCounties().size());

			} else {
				Assertions.assertTrue(false);
			}
		}	
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void getStatesAndCountiesTest() throws AgencyBusinessException {
		MockitoAnnotations.initMocks(this);

		office.setHqCountyCode("1234");
		office.setLocationCityName("townsville");
		office.setLocationStateAbbreviation("AL");
		office.setLocationStreetAddress("321 Street St.");
		office.setLocationZipCode("45621");
		office.setMailingAddressInformationLine("Fake");
		office.setOfficeId(1234l);
		office.setOfficeName("Dunder");
		office.setOfficeType("Paper");
		//office.setServicedLocationAreas("yes");
		
		office.setStateCode("Alive"); 
		List<Office> offices= new ArrayList<Office>();
		offices.add(office);
		
		State state = new State();
		state.setCode("01");
		state.setName("Alabama");
		state.setAbbreviation("AL");
		state.setAnsiCode("01001");

		County county = new County();
		county.setCode("001");
		county.setName("Autunga");
		county.setOfficeIdentifier("1234");
		county.setStateCode("01");
		county.setAssociatedAnsiCounties(new ArrayList<AssociatedAnsiCounty>());
		List<County> counties = new ArrayList<>();
		counties.add(county);

		OLACounty flpcounty = new OLACounty();
		Set<OLACounty> countySet = new HashSet<>();
		flpcounty.setFlpOfficeCode("300");
		countySet.add(flpcounty);

		List<State> states = new ArrayList<>();
		states.add(state);

		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(states);
		
		ApiResponse<County> apiResponseCounty = new ApiResponse<>();
		apiResponseCounty.setData(counties);

		ApiResponse<Office> apiResponseOffice = new ApiResponse<>();
		apiResponseOffice.setData(offices);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty);
		
		Mockito.when(locationServiceClient.getOfficesByIdsAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseOffice);
		
		stateCountyService.clearCache();
		Set<OLAState> statesReceived = stateCountyService.findAllStatesAndCounties();

		Assertions.assertTrue(statesReceived != null, "true is true");
		
		Map<String, Office> actual = stateCountyService.getOfficeFLPCodeOfficeMap();
		Assertions.assertNotNull(statesReceived);
		OLAState onlyState = null; 
		for(OLAState testState : statesReceived ) {
			onlyState = testState; 
		}
		Assertions.assertNotNull(onlyState);
		Assertions.assertTrue(onlyState.getOffices().contains(office));
		stateCountyService.clearCache();
		
	}

	@Test 
	public void findFLPOffices(){
		stateCountyService.clearCache();
		Office office = new Office();
		
		office.setHqCountyCode("21236");
		office.setLocationCityName("townsville");
		office.setLocationStateAbbreviation("AL");
		office.setLocationStreetAddress("321 Street St.");
		office.setLocationZipCode("45621");
		office.setMailingAddressInformationLine("Fake");
		office.setOfficeId(1234l);
		office.setOfficeName("Dunder");
		office.setOfficeType("Paper");
		//office.setServicedLocationAreas("yes");
		
		office.setStateCode("Alive"); 
		List<Office> offices= new ArrayList<Office>();
		offices.add(office);
		
		State state = new State();
		state.setCode("01");
		state.setName("Alabama");
		state.setAbbreviation("AL");
		state.setAnsiCode("01001");

		County county = new County();
		county.setCode("001");
		county.setName("Autunga");
		county.setOfficeIdentifier("1234");
		county.setStateCode("01");
		county.setAssociatedAnsiCounties(new ArrayList<AssociatedAnsiCounty>());
		List<County> counties = new ArrayList<>();
		counties.add(county);

		OLACounty flpcounty = new OLACounty();
		Set<OLACounty> countySet = new HashSet<>();
		flpcounty.setFlpOfficeCode("300");
		countySet.add(flpcounty);

		List<State> states = new ArrayList<>();
		states.add(state);

		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(states);
		
		ApiResponse<County> apiResponseCounty = new ApiResponse<>();
		apiResponseCounty.setData(counties);

		ApiResponse<Office> apiResponseOffice = new ApiResponse<>();
		apiResponseOffice.setData(offices);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty);
		
		Mockito.when(locationServiceClient.getOfficesByIdsAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseOffice);
		
		stateCountyService.getOfficeFLPCodeOfficeMap().put("21236", office);
		
		List<Office> actual = stateCountyService.findFLPOffices("21236");
		stateCountyService.initializeMapCache();
		Assertions.assertNotNull(actual);
		stateCountyService.clearCache();
		
	}
	
	@Test 
	public void findFLPOffices_null(){
		stateCountyService.clearCache();
		
		Office office = new Office();
		
		office.setHqCountyCode("21236");
		office.setLocationCityName("townsville");
		office.setLocationStateAbbreviation("AL");
		office.setLocationStreetAddress("321 Street St.");
		office.setLocationZipCode("45621");
		office.setMailingAddressInformationLine("Fake");
		office.setOfficeId(1234l);
		office.setOfficeName("Dunder");
		office.setOfficeType("Paper");
		//office.setServicedLocationAreas("yes");
		
		office.setStateCode("Alive"); 
		List<Office> offices= new ArrayList<Office>();
		offices.add(office);
		
		State state = new State();
		state.setCode("01");
		state.setName("Alabama");
		state.setAbbreviation("AL");
		state.setAnsiCode("01001");

		County county = new County();
		county.setCode("001");
		county.setName("Autunga");
		county.setOfficeIdentifier("1234");
		county.setStateCode("01");
		county.setAssociatedAnsiCounties(new ArrayList<AssociatedAnsiCounty>());
		List<County> counties = new ArrayList<>();
		counties.add(county);

		OLACounty flpcounty = new OLACounty();
		Set<OLACounty> countySet = new HashSet<>();
		flpcounty.setFlpOfficeCode("300");
		countySet.add(flpcounty);

		List<State> states = new ArrayList<>();
		states.add(state);

		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(states);
		
		ApiResponse<County> apiResponseCounty = new ApiResponse<>();
		apiResponseCounty.setData(counties);

		ApiResponse<Office> apiResponseOffice = new ApiResponse<>();
		apiResponseOffice.setData(offices);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty);
		
		Mockito.when(locationServiceClient.getOfficesByIdsAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseOffice);
		
		
		List<Office> actual = stateCountyService.findFLPOffices("3");
		stateCountyService.initializeMapCache();
		Assertions.assertTrue(actual.isEmpty());
		stateCountyService.clearCache();
		
	}
	@Test 
	public void initializeMapCacheTest(){
		
		stateCountyService.clearCache();
		
		Office office = new Office();
		
		office.setHqCountyCode("21236");
		office.setLocationCityName("townsville");
		office.setLocationStateAbbreviation("AL");
		office.setLocationStreetAddress("321 Street St.");
		office.setLocationZipCode("45621");
		office.setMailingAddressInformationLine("Fake");
		office.setOfficeId(1234l);
		office.setOfficeName("Dunder");
		office.setOfficeType("Paper");
		//office.setServicedLocationAreas("yes");
		
		office.setStateCode("Alive"); 
		List<Office> offices= new ArrayList<Office>();
		offices.add(office);
		
		State state = new State();
		state.setCode("01");
		state.setName("Alabama");
		state.setAbbreviation("AL");
		state.setAnsiCode("01001");

		County county = new County();
		county.setCode("001");
		county.setName("Autunga");
		county.setOfficeIdentifier("1234");
		county.setStateCode("01");
		county.setAssociatedAnsiCounties(new ArrayList<AssociatedAnsiCounty>());
		List<County> counties = new ArrayList<>();
		counties.add(county);

		OLACounty flpcounty = new OLACounty();
		Set<OLACounty> countySet = new HashSet<>();
		flpcounty.setFlpOfficeCode("300");
		countySet.add(flpcounty);

		List<State> states = new ArrayList<>();
		states.add(state);

		ApiResponse<State> apiResponseState = new ApiResponse<>();
		apiResponseState.setData(states);
		
		ApiResponse<County> apiResponseCounty = new ApiResponse<>();
		apiResponseCounty.setData(counties);

		ApiResponse<Office> apiResponseOffice = new ApiResponse<>();
		apiResponseOffice.setData(offices);
		
		Mockito.when(locationServiceClient.getStatesByStandard(Mockito.any())).thenReturn(apiResponseState);

		Mockito.when(locationServiceClient.getCountiesByStateAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseCounty);
		
		Mockito.when(locationServiceClient.getOfficesByIdsAndStandard(Mockito.any(), Mockito.any())).thenReturn(apiResponseOffice);
		
		stateCountyService.initializeMapCache();
		Mockito.verify(locationServiceClient, Mockito.atLeast(3)).getStatesByStandard(Mockito.anyString());
		stateCountyService.clearCache();
		
	}
	private static List<State> getStateList() {

		List<State> allAnsiStatesList = new ArrayList<>();

		State state0 = new State();
		state0.setCode("00");
		state0.setName("USA");
		state0.setAbbreviation(null);
		state0.setAnsiCode(null);

		allAnsiStatesList.add(state0);

		State state1 = new State();
		state1.setCode("01");
		state1.setName("Alabama");
		state1.setAbbreviation("AL");
		state1.setAnsiCode("01");

		allAnsiStatesList.add(state1);

		State state2 = new State();
		state2.setCode("02");
		state2.setName("Alaska");
		state2.setAbbreviation("AK");
		state2.setAnsiCode("02");

		allAnsiStatesList.add(state2);

		State state3 = new State();
		state3.setCode("05");
		state3.setName("Arizona");
		state3.setAbbreviation("AZ");
		state3.setAnsiCode("05");

		allAnsiStatesList.add(state3);

		return allAnsiStatesList;

	}

	private static List<County> getCounties01() {

		List<County> counties = new ArrayList<>();

		County county = new County();
		county.setCode("001");
		county.setStateCode("01");
		county.setName("Autunga");
		county.setOfficeIdentifier(null);
		county.setAssociatedAnsiCounties(null);
		counties.add(county);

		County county2 = new County();
		county2.setCode("003");
		county2.setStateCode("01");
		county2.setName("Baldwin");
		county2.setOfficeIdentifier(null);
		county2.setAssociatedAnsiCounties(null);
		counties.add(county2);

		return counties;
	}

	private static List<County> getCounties02() {

		List<County> counties = new ArrayList<>();

		County county = new County();
		county.setCode("013");
		county.setStateCode("02");
		county.setName("Aleutians East");
		county.setOfficeIdentifier(null);
		county.setAssociatedAnsiCounties(null);
		counties.add(county);

		County county2 = new County();
		county2.setCode("016");
		county2.setStateCode("02");
		county2.setName("Aleutians West");
		county2.setOfficeIdentifier(null);
		county2.setAssociatedAnsiCounties(null);
		counties.add(county2);

		County county3 = new County();
		county3.setCode("020");
		county3.setStateCode("02");
		county3.setName("Anchorage");
		county3.setOfficeIdentifier(null);
		county3.setAssociatedAnsiCounties(null);
		counties.add(county3);

		return counties;
	}

	private static List<County> getCounties05() {

		List<County> counties = new ArrayList<>();

		County county = new County();
		county.setCode("001");
		county.setStateCode("05");
		county.setName("Arkansas");
		county.setOfficeIdentifier(null);
		county.setAssociatedAnsiCounties(null);
		counties.add(county);

		County county2 = new County();
		county2.setCode("003");
		county2.setStateCode("05");
		county2.setName("Ashley");
		county2.setOfficeIdentifier(null);
		county2.setAssociatedAnsiCounties(null);
		counties.add(county2);

		County county3 = new County();
		county3.setCode("005");
		county3.setStateCode("05");
		county3.setName("Baxter");
		county3.setOfficeIdentifier(null);
		county3.setAssociatedAnsiCounties(null);
		counties.add(county3);

		County county4 = new County();
		county4.setCode("007");
		county4.setStateCode("05");
		county4.setName("Benton");
		county4.setOfficeIdentifier(null);
		county4.setAssociatedAnsiCounties(null);
		counties.add(county4);

		return counties;
	}

}
