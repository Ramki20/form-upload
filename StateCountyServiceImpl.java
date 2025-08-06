package gov.usda.fsa.fcao.flp.ola.core.service.external.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.bo.AnsiCounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.AnsiState;
import gov.usda.fsa.fcao.flp.ola.core.bo.FSACounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.FSAState;
import gov.usda.fsa.fcao.flp.ola.core.bo.FSAStateCounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLACounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLAState;
import gov.usda.fsa.fcao.flp.ola.core.cache.CacheEntry;
import gov.usda.fsa.fcao.flp.ola.core.service.external.IStateCountyService;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.CBSClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.LocationServiceClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.AssociatedAnsiCounty;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.CBSOffice;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.County;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Office;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.State;

/**
 * Provides services for managing state and county codes.
 * <p>
 * United States Department of Agriculture (USDA)<br>
 * Farm Service Agency (FSA)
 * 
 * 
 */
@Component
public class StateCountyServiceImpl implements IStateCountyService {

	private static final Logger LOGGER = LogManager.getLogger(StateCountyServiceImpl.class);

	public static final String ANSI = "ANSI";
	public static final String FLP = "FLP";
	public static final String FSA = "FSA";
	public static final String NA = "NA";

	private static Map<String, CacheEntry<Set<OLAState>>> cache = new HashMap<>();
	private static Map<String, CacheEntry<Set<FSAState>>> cacheFsa = new HashMap<>();
	private static Map<String, CacheEntry<Set<AnsiState>>> cacheAnsi = new HashMap<>();
	private static Map<String, CacheEntry<Map<String, Office>>> cacheFLPOffice = new HashMap<>();
	private static Map<String, CacheEntry<Map<String, String>>> cacheFlpAnsiCountyCode = new HashMap<>();
	private static Map<String, CacheEntry<Map<String, String>>> cacheAnsiFlpCountyCode = new HashMap<>();
	private static Map<String, String> officeIdentifierByStateCountyCodeMap = new HashMap<>();

	@Autowired
	private LocationServiceClient locationServiceClient;

	@Autowired
	private CBSClient cbsClient;
	
	@Autowired
	protected AgencyToken agencyToken;

	public StateCountyServiceImpl() {

		if (officeIdentifierByStateCountyCodeMap.isEmpty()) {
			initializeMap();
		}

	}

	private void initializeMap() {
		if (officeIdentifierByStateCountyCodeMap.isEmpty()) {

			officeIdentifierByStateCountyCodeMap.put("08051", "08301");// District of Columbia

			officeIdentifierByStateCountyCodeMap.put("23093", "23301");// cumberland
			officeIdentifierByStateCountyCodeMap.put("23053", "23301");// cumberland

			officeIdentifierByStateCountyCodeMap.put("23033", "23329");// Somerset

			officeIdentifierByStateCountyCodeMap.put("23062", "23303");// Aroostook
			officeIdentifierByStateCountyCodeMap.put("23045", "23303");// Aroostook

			officeIdentifierByStateCountyCodeMap.put("23066", "23303");// Hancock

			officeIdentifierByStateCountyCodeMap.put("23025", "23303");// Washington

			officeIdentifierByStateCountyCodeMap.put("46113", "47304");// Oglala Lakota
			officeIdentifierByStateCountyCodeMap.put("46102", "47304");// Oglala Lakota

			officeIdentifierByStateCountyCodeMap.put("104007", "63308");// Oglala Lakota

		}

	}

	@Override
	public Map<String, Office> getOfficeFLPCodeOfficeMap() {

		CacheEntry<Map<String, Office>> entry = cacheFLPOffice.get(FLP_OFFICES);
		if (entry == null) {
			findAllStatesAndCounties();
			entry = cacheFLPOffice.get(FLP_OFFICES);
		}
		return entry.getContext();
	}

	@Override
	public Map<String, String> getFlpAnsiCountyCodeMap() {

		CacheEntry<Map<String, String>> entry = cacheFlpAnsiCountyCode.get(FLP_ANSI_COUNTIES);
		if (entry == null) {
			findAllStatesAndCounties();
			entry = cacheFlpAnsiCountyCode.get(FLP_ANSI_COUNTIES);
		}
		return entry.getContext();
	}

	@Override
	public Map<String, String> getAnsiFlpCountyCodeMap() {

		CacheEntry<Map<String, String>> entry = cacheAnsiFlpCountyCode.get(ANSI_FLP_COUNTIES);
		if (entry == null) {
			findAllStatesAndCounties();
			entry = cacheAnsiFlpCountyCode.get(ANSI_FLP_COUNTIES);
		}
		return entry.getContext();
	}

	@Override
	public Set<OLAState> findAllStatesAndCounties() {

		CacheEntry<Set<OLAState>> entry = cache.get(STATES_COUNTIES);
		Map<String, Office> officeFLPCodeOfficeMap = new HashMap<>();
		Map<String, String> flpAnsiCountyCodeMap = new HashMap<>();
		Map<String, String> ansiFlpCountyCodeMap = new HashMap<>();

		if (entry != null) {
			LOGGER.info("States and counties were retrieved from the cache...");
			return entry.getContext();
		}

		long startTime = System.currentTimeMillis();

		//List<State> allFLPStatesList = locationService.findStates("FLP");
		List<State> allFLPStatesList = locationServiceClient.getStatesByStandard(FLP).getData();

		Set<OLAState> olaStatesList = getOlaStateList(allFLPStatesList);

		if (!olaStatesList.isEmpty()) {

			fillCounties(olaStatesList, flpAnsiCountyCodeMap, ansiFlpCountyCodeMap);

			Map<String, Office> offceMapByIdentifier = findOfficeMap(olaStatesList, officeFLPCodeOfficeMap);

			for (OLAState olaState : olaStatesList) {

				Set<Office> offices = new HashSet<>();

				for (OLACounty olaCOunty : olaState.getCounties()) {

					Office office = offceMapByIdentifier.get(olaCOunty.getOfficeIdentifier());

					if (office != null) {
							
						String stateFlpCode = office.getStateCode();
						String hqCountyCode = office.getHqCountyCode();

						olaCOunty.setFlpOfficeCode(stateFlpCode.trim().concat(hqCountyCode.trim()));
						
						offices.add(office);

					} else if (olaCOunty.getOfficeIdentifier() != null) {

						String officeFLPCode = officeIdentifierByStateCountyCodeMap
								.get(olaCOunty.getOfficeIdentifier());

						Optional<Office> officeOptional = offices.stream()
								.filter(o -> o.getStateCode().concat(o.getHqCountyCode()).equals(officeFLPCode))
								.findAny();

						
						if (officeFLPCode != null && !officeOptional.isPresent()) {

							//List<gov.usda.fsa.citso.cbs.dto.Office> officeList = CBSUtil
							//		.retrieveOfficesByFLPCodes(officeFLPCode);
							
							List<String> officeFLPCodes = new ArrayList<>();
							officeFLPCodes.add(officeFLPCode);
							
							List<CBSOffice> officeList =  cbsClient.findFlpOfficesByFlpCodeList(OlaServiceUtil.getOlaAgencyToken(agencyToken), officeFLPCodes);
							
							if (officeList != null && !officeList.isEmpty()) {

								Office flpOffice = new Office();
								flpOffice.setStateCode(officeFLPCode.substring(0, 2));
								flpOffice.setHqCountyCode(officeFLPCode.substring(2));
								flpOffice.setLocationCityName(officeList.get(0).getLocCityName());
								flpOffice.setOfficeId(Long.valueOf(officeList.get(0).getId()));
								flpOffice.setLocationStateAbbreviation(officeList.get(0).getLocStateAbbrev());
								flpOffice.setOfficeName(officeList.get(0).getName());
								flpOffice.setLocationStreetAddress(officeList.get(0).getLocStreetAddr());
								flpOffice.setOfficeType(officeList.get(0).getAgencyOfficeType());
								flpOffice.setMailingAddressInformationLine(officeList.get(0).getMailingAddrLine());
								flpOffice.setLocationZipCode(officeList.get(0).getZipCode());
								LOGGER.info("Missed offices from MRT{}", officeList);
								offices.add(flpOffice);
								
								offceMapByIdentifier.put(olaCOunty.getOfficeIdentifier(), flpOffice);
								officeFLPCodeOfficeMap.put(flpOffice.getStateCode() + flpOffice.getHqCountyCode(),
										flpOffice);

							}
							olaCOunty.setFlpOfficeCode(officeFLPCode);
						} 

					}

				}

				olaState.setOffices(offices);

			}

			entry = new CacheEntry<>(olaStatesList);
			cache.put(STATES_COUNTIES, entry);
			CacheEntry<Map<String, Office>> flpOfficesEntry = new CacheEntry<>(officeFLPCodeOfficeMap);
			LOGGER.info("officeFLPCodeOfficeMap size:" + officeFLPCodeOfficeMap.size());
			cacheFLPOffice.put(FLP_OFFICES, flpOfficesEntry);

			CacheEntry<Map<String, String>> flpAnsiCountiesEntry = new CacheEntry<>(flpAnsiCountyCodeMap);
			LOGGER.info("flpAnsiCountyCodeMap size:" + flpAnsiCountyCodeMap.size());
			cacheFlpAnsiCountyCode.put(FLP_ANSI_COUNTIES, flpAnsiCountiesEntry);

			CacheEntry<Map<String, String>> ansiFlpCountiesEntry = new CacheEntry<>(ansiFlpCountyCodeMap);
			LOGGER.info("ansiFlpCountyCodeMap size:" + ansiFlpCountyCodeMap.size());
			cacheAnsiFlpCountyCode.put(ANSI_FLP_COUNTIES, ansiFlpCountiesEntry);

			long endTime = System.currentTimeMillis();

			LOGGER.info("findAllStatesAndCounties took {} {}", (endTime - startTime), MS);

		}

		return olaStatesList;
	}

	private Map<String, Office> findOfficeMap(Set<OLAState> olaStatesList, Map<String, Office> officeFLPCodeOfficeMap) {
		Map<String, Office> offceMapByIdentifier = new HashMap<>();

		for (OLAState olaState : olaStatesList) {

			StringBuilder officeIdentifiers = new StringBuilder();
			for (OLACounty olaCounty : olaState.getCounties()) {

				if (olaCounty.getOfficeIdentifier() != null) {
					officeIdentifiers.append(olaCounty.getOfficeIdentifier()).append(",");
				}
			}

			//List<Office> offices = locationService.findOffices(FLP, officeIdentifiers.toString());
			
			List<Office> offices = locationServiceClient.getOfficesByIdsAndStandard(officeIdentifiers.toString(), FLP).getData();

			if(offices != null) {
				for (Office office : offices) {
	
					offceMapByIdentifier.put(String.valueOf(office.getOfficeId()), office);
					officeFLPCodeOfficeMap.put(office.getStateCode() + office.getHqCountyCode(), office);
					
				}
			}

		}
		return offceMapByIdentifier;
	}

	private Set<OLAState> fillCounties(Set<OLAState> olaStatesList, Map<String, String> flpAnsiCountyCodeMap,
			Map<String, String> ansiFlpCountyCodeMap) {

		for (OLAState state : olaStatesList) {

			String stateCode = state.getStateCode();
			//List<StandardCountyDetail> standardCountyDetailList = locationService.findCounties(FLP, stateCode);
			
			List<County> standardCountyDetailList = locationServiceClient.getCountiesByStateAndStandard(stateCode, FLP).getData();
			
			Set<OLACounty> olaCountiesList = new HashSet<>();
			if (standardCountyDetailList != null && !standardCountyDetailList.isEmpty()) {

				OLACounty olaCounty = null;
				for (County countyDetail : standardCountyDetailList) {

					olaCounty = OlaServiceUtil.createOlaCounty(countyDetail, stateCode);
					populateFlpAnsiCodeMap(olaCounty, flpAnsiCountyCodeMap, ansiFlpCountyCodeMap);

					if (olaCounty.getFlpOfficeCode() != null) {

						olaCountiesList.add(olaCounty);

					} else {

						olaCountiesList.add(getUpdatedCounty(olaCounty));

					}
				}

				state.setCounties(olaCountiesList);
			}

		}
		return olaStatesList;
	}

	private void populateFlpAnsiCodeMap(OLACounty flpCounty, Map<String, String> flpAnsiCountyCodeMap,
			Map<String, String> ansiFlpCountyCodeMap) {
		List<AssociatedAnsiCounty> associatedAnsiCounties = flpCounty.getAssociatedAnsiCounties();
		if (associatedAnsiCounties != null && !associatedAnsiCounties.isEmpty()) {
			// This won't work correctly if name doesn't match exactly
			// County ansiCounty = flpCounty.getAssociatedAnsiCounties().stream()
			// .filter(county ->
			// flpCounty.getCountyName().contains(county.getName())).findFirst().orElse(null);

			AssociatedAnsiCounty ansiCounty = flpCounty.getAssociatedAnsiCounties().get(0);

			if (ansiCounty != null) {
				String flpStateCountyCode = flpCounty.getStateFLPCode() + flpCounty.getCountyCode();
				String ansiStateCountyCode = flpAnsiCountyCodeMap.get(flpStateCountyCode);

				if (ansiStateCountyCode == null) {
					flpAnsiCountyCodeMap.put(flpStateCountyCode, ansiCounty.getStateCode() + ansiCounty.getCode());
				} else {
					LOGGER.warn("Key flpStateCountyCode: {} already exists in flpAnsiCountyCodeMap.",
							flpStateCountyCode);
				}

				ansiStateCountyCode = ansiCounty.getStateCode() + ansiCounty.getCode();
				flpStateCountyCode = ansiFlpCountyCodeMap.get(ansiStateCountyCode);
				
				if (flpStateCountyCode == null) {
					ansiFlpCountyCodeMap.put(ansiStateCountyCode,
							flpCounty.getStateFLPCode() + flpCounty.getCountyCode());
				} else {
					LOGGER.warn(
							"Key ansiStateCountyCode: {} already exists (existing value: {}) in ansiFlpCountyCodeMap."
									+ " another related flpStateCode: {}, another related flpCountyCode: {}",
							ansiStateCountyCode, flpStateCountyCode, flpCounty.getStateFLPCode(),
							flpCounty.getCountyCode());
				}
			} else {
				LOGGER.warn("Ansi County not found for flp stateCode: {}, countyCode: {}", flpCounty.getStateFLPCode(),
						flpCounty.getCountyCode());
			}
		}

	}

	private Set<FSAState> fillFSACounties(Set<FSAState> fsaStatesList) {

		for (FSAState state : fsaStatesList) {

			String stateCode = state.getStateCode();
			//List<StandardCountyDetail> standardCountyDetailList = locationService.findCounties(FSA, stateCode);
			List<County> standardCountyDetailList = locationServiceClient.getCountiesByStateAndStandard(stateCode, FSA).getData();

			Set<FSACounty> fsaCountiesList = new HashSet<>();
			if (standardCountyDetailList != null && !standardCountyDetailList.isEmpty()) {

				FSACounty fsaCounty = null;
				for (County countyDetail : standardCountyDetailList) {

					fsaCounty = OlaServiceUtil.createFsaCounty(countyDetail, stateCode);
					fsaCountiesList.add(fsaCounty);

				}

				state.setCounties(fsaCountiesList);
			}

		}
		return fsaStatesList;
	}

	private OLACounty getUpdatedCounty(OLACounty olaCounty) {

		String stateFLPCode = olaCounty.getStateFLPCode();
		String countyCode = olaCounty.getCountyCode();

		if (stateFLPCode != null && countyCode != null) {

			String key = stateFLPCode.concat(countyCode);

			String officeFLPCode = officeIdentifierByStateCountyCodeMap.get(key);

			if (officeFLPCode != null) {

				olaCounty.setFlpOfficeCode(String.valueOf(officeFLPCode));

			}

		}
		return olaCounty;
	}

	public String findOfficeFLPCode(String stateCode, String countyCode, String officeIdentifier) {

		String officeFLPCode = null;

		if (officeIdentifier == null) {

			String message = stateCode.concat("/").concat(countyCode);

			LOGGER.error("Error fetching officeFLPCode for the State/County : {}", message);

			return officeFLPCode;
		}

		//List<Office> offices = locationService.findOffices(FLP, officeIdentifier);
		
		List<Office> offices =  locationServiceClient.getOfficesByIdsAndStandard(FLP, officeIdentifier).getData();

		if (offices != null && !offices.isEmpty()) {

			if (offices.size() > 1) {

				LOGGER.error("Multiple Offices found : {}", offices);
			}

			for (Office office : offices) {

				String stateFlpCode = office.getStateCode();
				String hqCountyCode = office.getHqCountyCode();

				officeFLPCode = stateFlpCode.trim().concat(hqCountyCode.trim());

			}

		}

		return officeFLPCode;
	}

	private static Set<OLAState> getOlaStateList(List<State> allFLPStatesList) {
		OLAState olaState = null;
		Set<OLAState> olaStatesList = new HashSet<>();
		if (allFLPStatesList != null && !allFLPStatesList.isEmpty()) {

			for (State state : allFLPStatesList) {

				olaState = OlaServiceUtil.createOlaState(state);

				olaStatesList.add(olaState);
			}
		}
		return olaStatesList;
	}

	private static Set<FSAState> getFsaStateList(List<State> allFSAStatesList) {

		FSAState fsaState = null;
		Set<FSAState> fsaStatesList = new HashSet<>();

		if (allFSAStatesList != null && !allFSAStatesList.isEmpty()) {

			for (State state : allFSAStatesList) {

				fsaState = OlaServiceUtil.createFsaState(state);

				fsaStatesList.add(fsaState);
			}
		}
		return fsaStatesList;
	}

	@Override
	public OLACounty findCounty(String stateCode, String countyCode) {

		Set<OLAState> allStateCounties = findAllStatesAndCounties();
		OLAState olaState = null;
		OLACounty olaCounty = null;
		if (allStateCounties != null && !allStateCounties.isEmpty()) {

			Optional<OLAState> optionalStates = allStateCounties.stream()
					.filter(state -> state.getStateCode().equalsIgnoreCase(stateCode)).findFirst();

			if (optionalStates.isPresent()) {

				olaState = optionalStates.get();
			}

			if (olaState != null && olaState.getCounties() != null) {

				Optional<OLACounty> optionalCounties = olaState.getCounties().stream()
						.filter(county -> county.getCountyCode().equalsIgnoreCase(countyCode)).findFirst();

				if (optionalCounties.isPresent()) {

					olaCounty = optionalCounties.get();
				}

			}

		}
		return olaCounty;

	}

	@Override
	public OLAState findState(String stateCode) {
		Set<OLAState> allStateCounties = findAllStatesAndCounties();
		OLAState olaState = null;
		if (allStateCounties != null && !allStateCounties.isEmpty()) {

			Optional<OLAState> optionalStates = allStateCounties.stream()
					.filter(state -> state.getStateCode().equalsIgnoreCase(stateCode)).findFirst();

			if (optionalStates.isPresent()) {

				olaState = optionalStates.get();
			}

		}
		return olaState;
	}

	@Override
	public void clearCache() {

		LOGGER.info("State/County Caches clearing...");

		cache.remove(STATES_COUNTIES);
//		cache.remove(STATES_COUNTIES_FLP);
		cacheFLPOffice.remove(FLP_OFFICES);
		cacheFsa.remove(STATES_COUNTIES_FSA);
		cacheAnsi.remove(STATES_COUNTIES_ANSI);
		cacheFlpAnsiCountyCode.remove(FLP_ANSI_COUNTIES);
		cacheAnsiFlpCountyCode.remove(ANSI_FLP_COUNTIES);

		LOGGER.info("State/County Caches cleared.");
	}

	@Override
	public Set<FSAState> findAllFSAStatesAndCounties() {

		CacheEntry<Set<FSAState>> entry = cacheFsa.get(STATES_COUNTIES_FSA);

		if (entry != null) {
			LOGGER.info("FSA States and counties were retrieved from the cache...");
			return entry.getContext();
		}

		//List<State> allFLPStatesList = locationService.findStates("FSA");
		List<State> allFSAStatesList = locationServiceClient.getStatesByStandard(FSA).getData();

		Set<FSAState> fsaStatesList = getFsaStateList(allFSAStatesList);

		if (!fsaStatesList.isEmpty()) {

			fillFSACounties(fsaStatesList);

		}

		entry = new CacheEntry<>(fsaStatesList);
		cacheFsa.put(STATES_COUNTIES_FSA, entry);

		return fsaStatesList;
	}

	@Override
	public FSAStateCounty findFSAStateCounty(String flpStateCode, String flpCountyCode) {

		FSAStateCounty fsaStateCounty = null;
		if (flpStateCode != null && flpCountyCode != null) {

			OLACounty flpCounty = findCounty(flpStateCode, flpCountyCode);

			if (flpCounty.getAssociatedAnsiCounties() != null) {

				// This won't work correctly if name doesn't match exactly
				// Optional<County> countyOptional =
				// flpCounty.getAssociatedAnsiCounties().stream()
				// .filter(county ->
				// county.getName().equalsIgnoreCase(flpCounty.getCountyName())).findFirst();

				AssociatedAnsiCounty ansiCountyDetail = flpCounty.getAssociatedAnsiCounties().get(0);

				if (ansiCountyDetail != null) {

					String ansiStateCode = ansiCountyDetail.getStateCode();
					String ansiCountyCode = ansiCountyDetail.getCode();

					fsaStateCounty = getFSAStateCounty(ansiStateCode, ansiCountyCode);

				}
			}

		}
		return fsaStateCounty;

	}

	public FSAStateCounty getFSAStateCounty(String stateCode, String countyCode) {

		Set<FSAState> allFSAStateCounties = findAllFSAStatesAndCounties();
		FSAStateCounty fsaStateCounty = null;
		FSAState fsaState = null;
		FSACounty fsaCounty = null;
		if (allFSAStateCounties != null && !allFSAStateCounties.isEmpty()) {

			fsaState = allFSAStateCounties.stream().filter(state -> state.getStateCode().equalsIgnoreCase(stateCode))
					.findFirst().orElse(null);

			if (fsaState != null && fsaState.getCounties() != null && !fsaState.getCounties().isEmpty()) {

				fsaCounty = fsaState.getCounties().stream()
						.filter(county -> county.getCountyCode().equalsIgnoreCase(countyCode)).findFirst().orElse(null);

				fsaStateCounty = new FSAStateCounty();
				fsaStateCounty.setStateCode(stateCode);
				fsaStateCounty.setStateAbbreviation(fsaState.getStateAbbreviation());
				fsaStateCounty.setStateName(fsaState.getStateName());
				fsaStateCounty.setCounty(fsaCounty);

			}

		}
		return fsaStateCounty;

	}

	@Override
	public List<Office> findFLPOffices(String officeFLPCode) {

		/*
		if (StringUtils.isNotBlank(officeFLPCode) && officeFLPCode.length() == 5) {
			String stateCode = officeFLPCode.substring(0, 2);
			String countyCode = "0";
			countyCode = countyCode.concat(officeFLPCode.substring(3, 5));
			return locationService.findOffices(FLP, FLP, stateCode, countyCode, false);
		} else {
			return Collections.emptyList();
		}
		*/
		
		Map<String, Office> officeFLPCodeOfficeMap =  getOfficeFLPCodeOfficeMap();
		Office office = officeFLPCodeOfficeMap.get(officeFLPCode);
		LOGGER.info("findFLPOffices office:"+office);
		List<Office> officeList = new ArrayList<>();  //TODO: fix the return type. it will be a single office (not multiple)  
		if(office!=null)
		{
			officeList.add(office);
		}
		return officeList;
	}

	@Override
	public void initializeMapCache() {

		LOGGER.info("Loading StateCounty Caches...");
		Set<OLAState> olaState = findAllStatesAndCounties();

		LOGGER.info("findAllStatesAndCounties loaded " + olaState.size());

		Set<FSAState> fsaState = findAllFSAStatesAndCounties();

		LOGGER.info("findAllFSAStatesAndCounties loaded " + fsaState.size());

		Set<AnsiState> ansiStateMap = findAnsiStatesAndCounties();

		LOGGER.info("findAnsiFSAStatesAndCounties loaded " + ansiStateMap.size());
		
		LOGGER.info("Loaded StateCounty Caches");

	}

	@Override
	public Set<AnsiState> findAnsiStatesAndCounties() {

		CacheEntry<Set<AnsiState>> entry = cacheAnsi.get(STATES_COUNTIES_ANSI);
		
		if (entry != null) {
			LOGGER.info("ANSI States and counties were retrieved from the cache...");
			return entry.getContext();
		}

		Set<AnsiState> statesMap = buildAnsiStatesAndCounties();
		
		entry = new CacheEntry<>(statesMap);
		cacheAnsi.put(STATES_COUNTIES_ANSI, entry);

		return statesMap;
	}
	
	@Override
	public String getServiceCenterOfficeInfo(String flpServiceCenterOfficeCode, boolean includeOfficeCode) {

		if (flpServiceCenterOfficeCode == null) {

			return NA;
		}

		StringBuilder builder = new StringBuilder();
		
		Map<String, Office> officeFLPCodeOfficeMap = getOfficeFLPCodeOfficeMap();
		Office office = officeFLPCodeOfficeMap.get(flpServiceCenterOfficeCode);
		
		if (office!=null) {

			if (includeOfficeCode) {
				builder.append(flpServiceCenterOfficeCode);
				builder.append("-");
			}

			builder.append(office.getLocationStateAbbreviation());
			builder.append("/");
			builder.append(office.getLocationCityName());

		} else {

			LOGGER.error("Service center information not found for the office FLP code: {}",
					flpServiceCenterOfficeCode);
			return NA;

		}
		
		return builder.toString();
	}

	Set<AnsiState> buildAnsiStatesAndCounties() {

		Set<AnsiState> statesList = this.fillAnsiStates();

		this.fillAnsiCounties(statesList);

		return statesList;

	}

	Set<AnsiState> fillAnsiStates() {

		/*
		 * Using FSA instead of ANSI for the standard parameter to the location service,
		 * because ANSI will only return entries for the 50 states, no territories.
		 */
		//List<State> allAnsiStatesList = locationService.findStates(FSA);
		List<State> allAnsiStatesList = locationServiceClient.getStatesByStandard(FSA).getData();

		Set<AnsiState> ansiStatesList = getAnsiStateList(allAnsiStatesList);

		return ansiStatesList;
	}

	static Set<AnsiState> getAnsiStateList(List<State> ansiStatesList) {

		Set<AnsiState> fsaStatesSet = new LinkedHashSet<>();

		if (ansiStatesList != null && !ansiStatesList.isEmpty()) {

			for (State state : ansiStatesList) {

				/*
				 * Only include states/territories that have an ansi code value. There are some
				 * cases where 'FSA' states will have a null ansi code value.
				 */
				if (state.getAnsiCode() != null) {
					AnsiState ansiState = OlaServiceUtil.createAnsiState(state);
					fsaStatesSet.add(ansiState);
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("State with stateCode " + state.getCode()
								+ " not added to map of ansi states - ansiCode value was null");
					}
				}
			}
		} else {
			LOGGER.info("No entries in the list of states (ansi) provided from locationService - returning empty set");
		}
		return fsaStatesSet;
	}

	void fillAnsiCounties(Set<AnsiState> statesSet) {

		if (null != statesSet && statesSet.size() > 0) {

			for (AnsiState ansiState : statesSet) {

				//List<StandardCountyDetail> standardCountyDetailList = locationService.findCounties(ANSI, ansiState.getAnsiCode());
				
				List<County> standardCountyDetailList = locationServiceClient.getCountiesByStateAndStandard(ansiState.getAnsiCode(), ANSI).getData();

				Set<AnsiCounty> ansiCountiesSet = new LinkedHashSet<>();

				if (standardCountyDetailList != null && !standardCountyDetailList.isEmpty()) {

					for (County countyDetail : standardCountyDetailList) {
						ansiCountiesSet.add(OlaServiceUtil.createAnsiCounty(countyDetail, ansiState.getAnsiCode()));
					}

				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("No counties found for stateCode " + ansiState.getAnsiCode());
					}
				}
				
				ansiState.setCounties(ansiCountiesSet);
			}
		} else {
			LOGGER.info("Passed statesMap is null reference or is empty.");
		}
	}
}
