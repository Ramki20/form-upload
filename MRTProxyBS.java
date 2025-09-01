package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usda.fsa.citso.cbs.bc.AgencyCode;
import gov.usda.fsa.citso.cbs.bc.EmployeeId;
import gov.usda.fsa.citso.cbs.bc.StateLocAreaCode;
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
import gov.usda.fsa.citso.cbs.dto.Calendar;
import gov.usda.fsa.citso.cbs.dto.County;
import gov.usda.fsa.citso.cbs.dto.EmployeeOrgChart;
import gov.usda.fsa.citso.cbs.dto.IcamsEmployee;
import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.citso.cbs.dto.metadata.AgencyEmployeeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.CalendarProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.CountyProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FlpStateProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaLocationAreaProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaOfficeProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.FsaStateProperties;
import gov.usda.fsa.citso.cbs.dto.metadata.IcamsEmployeeProperties;
import gov.usda.fsa.citso.cbs.ex.BusinessServiceBindingException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.common.base.InvalidBusinessContractDataException;
import gov.usda.fsa.fcao.flp.constants.EmployeeConstants;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFSAStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveFsaCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveInterestRateForAssistanceTypeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTCountyListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTServiceCenterListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveMRTStateListBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FipsOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FlpOfficeLocationAreaBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MrtLookUpBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.OfficeInfo;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.AddressBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.PartyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ValidationUtils;

public class MRTProxyBS implements IMRTProxyBS {
	static final int FLP_CODE_SIZE = 5;
	static final String FLP_VALID_FORMAT = "[0-9][0-9]3[0-9][0-9]";
	private static final String CONSTANCE_3 = "3";
	private static final String CONSTANCE_300 = "300";
	private static final String SCIMS_DEFAULT_LANDING_01001 = "01001";
	private static final String SCIMS_DEFAULT_LANDING_CBS_ERROR = "CBS_ERROR";
	private static final Logger logger = LogManager.getLogger(MRTProxyBS.class);
	private OfficeDataServiceProxy flpOfficeMRTBusinessService;
	private StateDataServiceProxy flpStateMRTBusinessService;
	private InterestRateDataServiceProxy interestRateDataMartBusinessService;
	private LocationAreaDataServiceProxy flpLocationAreaDataMartBusinessService;
	private BusinessPartyDataServiceProxy businessPartyDataService;
	private EmployeeDataServiceProxy employeeDataServiceProxy;
	private MRTFacadeBusinessService mrtFacadeBusinessService;
	private TaxIdSurrogateBusinessServiceProxy surrogateService;
	private CountyDataServiceProxy countyDataServiceProxy;
	private CalendarDataServiceProxy calendarDataServiceProxy;

	private final static String NODATAFOUND = "no data found";
	/**
	 * call CBS-Surrogate service to return the Surrogate Map for the Tax Id list.
	 * 
	 * @param taxIdList
	 * @return
	 */
	public Map<String, Surrogate> retrieveSurrogateIdForTaxId(List<String> taxIdList) {
		Map<String, Surrogate> taxIdSurrogateMap = getSurrogateService().transformTaxId(taxIdList);
		return taxIdSurrogateMap;
	}

	/**
	 * call CBS-Surrogate service to return the TaxId Map for the Surrogate Id list.
	 * 
	 * @param Surr
	 * @return
	 */
	public Map<String, TaxId> retrieveTaxIdForSurrogateId(List<String> surrogateIdList) {
		Map<String, TaxId> surrogateMap = getSurrogateService().transformSurrogate(surrogateIdList);
		return surrogateMap;
	}

	public double retrieveInterestRateForAssistanceType(RetrieveInterestRateForAssistanceTypeBC contract) {

		double intRate = 0.00;
		try {
			String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
			Date date = (contract.getDateOfLoanApproval() == null) ? (sdf.parse(sdf.format(new Date())))
					: sdf.parse(sdf.format(contract.getDateOfLoanApproval()));
			int asstType = Integer.parseInt(contract.getAssistanceTypeCode());
			try {
				intRate = ((interestRateDataMartBusinessService.byTypeIdAndDate(asstType, date)).getIntRate())
						.doubleValue();

			} catch (BusinessServiceBindingException e1) {
				logger.error("MRT Error in retrieveInterestRateForAssistanceType");
				logger.error(
						"Exception when retrieving Interest Rate from MRTProxyBS.retrieveInterestRateForAssistanceType() : ",
						e1);
				logger.error("MRT Error End retrieveInterestRateForAssistanceType");
			}
		} catch (Exception e) {
			logger.error("MRT Error in retrieveInterestRateForAssistanceType");
			logger.error(
					"Exception when retrieving Interest Rate from MRTProxyBS.retrieveInterestRateForAssistanceType() : ",
					e);
			logger.error("MRT Error End retrieveInterestRateForAssistanceType");
		}
		return intRate;
	}

	public List<Office> retrieveFLPOfficeCodeListByFsaStateCountyCode(String fsaStateCountyCode) {
		List<Office> ofcList = new ArrayList<Office>();
		List<LocationArea> flpLocationArea = flpLocationAreaDataMartBusinessService
				.flpByFsaStateLocArea(fsaStateCountyCode);
		if (!flpLocationArea.isEmpty()) {
			LocationArea flpStateCountyCode = flpLocationArea.get(0);

			StateLocAreaCode flpStateLocAreaCode = new StateLocAreaCode(flpStateCountyCode.getStateLocationAreaCode());
			List<StateLocAreaCode> list = new ArrayList<StateLocAreaCode>();
			list.add(flpStateLocAreaCode);
			ofcList = flpOfficeMRTBusinessService.flpServiceCenterOfficesByFlpStateAndLocAreas(list);
		}
		return ofcList;
	}

	public List<Office> retrieveFlpServiceCentersByOIP(List<String> stateOffices)
			throws InvalidBusinessContractDataException {
		List<Office> serviceCenters = new ArrayList<Office>();

		validateStateOfficeList(stateOffices);
		// look up this ofc's state abbreviation (61300 returns HI)
		Integer oipCode = lookupOIP(stateOffices);
		List<Integer> oipList = new ArrayList<Integer>();
		oipList.add(oipCode);
		List<Office> officeList = retrieveFsaFlpServiceCenterOfficesByOfficeIdList(oipList);
		if (null != officeList && !officeList.isEmpty()) {
			for (Office ofc : officeList) {
				if (ofc.getOfficeCode().substring(2, 3).equalsIgnoreCase(CONSTANCE_3)
						&& !ofc.getOfficeCode().substring(2, 5).equalsIgnoreCase(CONSTANCE_300)
						&& ofc.getOfficeCode().substring(0, 2).equalsIgnoreCase(stateOffices.get(0).substring(0, 2))) {
					serviceCenters.add(ofc);
				}
			}
		}

		return serviceCenters;
	}

	public List<Office> retrieveFLPOfficeMRTBusinessObjectList(String stateAbbr) throws DLSBusinessStopException {

		try {
			List<Office> serviceCenterOffices = new ArrayList<Office>();
			if (!ValidationUtils.isNumber(stateAbbr)) {
				serviceCenterOffices = flpOfficeMRTBusinessService.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr,
						FlpOfficeProperties.officeCode, FlpOfficeProperties.name, FlpOfficeProperties.refId,
						FlpOfficeProperties.cityFipsCode, FlpOfficeProperties.locCityName,
						FlpOfficeProperties.locStateAbbrev);
			}
			return serviceCenterOffices;
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving MRTProxyBS.retrieveFLPOfficeMRTBusinessObjectList() ";
			logger.error(errorMsg, e);
			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage(), e);
		}
	}

	public List<State> retrieveFLPStateList() throws DLSBusinessStopException {

		try {
			return flpStateMRTBusinessService.allFlp(FlpStateProperties.code, FlpStateProperties.name,
					FlpStateProperties.abbreviation, FlpStateProperties.fipsCode, FlpStateProperties.activeId);
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving  MRTProxyBS.retrieveFLPStateList() ";
			logger.info("MRT Error in retrieveFLPStateList");
			logger.error(errorMsg, e);
			logger.info("MRT Error End retrieveFLPStateList");
			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage(), e);

		}
	}

	public List<MrtLookUpBO> retrieveStateList(RetrieveMRTStateListBC contract) throws DLSBusinessFatalException {
		try {
			List<State> result = retrieveFLPStateList();

			List<MrtLookUpBO> list = new ArrayList<MrtLookUpBO>();
			Iterator<State> it = result.iterator();
			while (it.hasNext()) {
				State item = it.next();
				if (null != item) {
					MrtLookUpBO stateLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), item.getCode(),
							item.getName(), item.getAbbreviation());
					list.add(stateLookUpBO);
				}
			}
			return list;
		} catch (Exception ex) {
			String errorMsg = "Exception thrown in  MRTProxyBS.retrieveStateList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}

	}

	public List<State> retrieveFSAStates() throws DLSBusinessStopException {

		try {
			return flpStateMRTBusinessService.allFSA(FsaStateProperties.code, FsaStateProperties.name,
					FsaStateProperties.abbreviation);
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving  MRTProxyBS.retrieveFSAStateList() ";
			if (logger.isDebugEnabled()) {
				logger.debug("MRT Error in retrieveFSAStateList");
				logger.debug("MRT Error End retrieveFSAStateList");
			}
			logger.error(errorMsg, e);

			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage());

		}
	}

	public List<State> retrieveFSAOneCMStates() throws DLSBusinessStopException {

		try {
			return flpStateMRTBusinessService.oneCM(FsaStateProperties.code, FsaStateProperties.name,
					FsaStateProperties.abbreviation);
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving  MRTProxyBS.retrieveFSAStateList() ";
			logger.info("MRT Error in retrieveFSAStateList");
			logger.error(errorMsg, e);
			logger.info("MRT Error End retrieveFSAStateList");
			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage());

		}
	}

	public List<MrtLookUpBO> retrieveFSAStateList(RetrieveFSAStateListBC contract) throws DLSBusinessFatalException {
		try {
			List<State> result = retrieveFSAStates();

			List<MrtLookUpBO> list = new ArrayList<MrtLookUpBO>();
			Iterator<State> it = result.iterator();
			while (it.hasNext()) {
				State item = it.next();
				if (null != item) {
					MrtLookUpBO stateLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), item.getCode(),
							item.getName(), item.getAbbreviation());
					list.add(stateLookUpBO);
				}
			}
			return list;
		} catch (Exception ex) {
			String errorMsg = "Exception thrown in  MRTProxyBS.retrieveFSAStateList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}
	}

	public List<MrtLookUpBO> retrieveFSAOneCMStateList(RetrieveFSAStateListBC contract)
			throws DLSBusinessFatalException {
		try {
			List<State> result = retrieveFSAOneCMStates();

			List<MrtLookUpBO> list = new ArrayList<MrtLookUpBO>();
			Iterator<State> it = result.iterator();
			while (it.hasNext()) {
				State item = it.next();
				if (null != item) {
					MrtLookUpBO stateLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), item.getCode(),
							item.getName(), item.getAbbreviation());
					list.add(stateLookUpBO);
				}
			}
			return list;
		} catch (Throwable ex) {
			String errorMsg = "Exception thrown in  MRTProxyBS.retrieveFSAStateList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}
	}

	/**
	 * Retrieves a collection of FSA states in a Map with the FSA state code as the
	 * key.
	 * 
	 * @param contract
	 * @return
	 * @throws DLSBusinessFatalException
	 */
	public Map<String, MrtLookUpBO> retrieveFSAStateMap(RetrieveFSAStateListBC contract)
			throws DLSBusinessFatalException {
		Map<String, MrtLookUpBO> stateMap = new HashMap<String, MrtLookUpBO>();
		try {
			List<State> stateList = retrieveFSAStates();
			for (State state : stateList) {
				MrtLookUpBO stateLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), state.getCode(), state.getName(),
						state.getAbbreviation());
				stateMap.put(state.getCode(), stateLookUpBO);
			}
		} catch (Throwable ex) {
			String errorMsg = "Exception thrown in  MRTProxyBS.retrieveFSAStateMap() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}

		return stateMap;
	}

	public List<MrtLookUpBO> retrieveServiceCenterList(RetrieveMRTServiceCenterListBC contract)
			throws DLSBusinessFatalException {
		List<MrtLookUpBO> list = new ArrayList<MrtLookUpBO>();
		try {
			List<Office> servicelist = retrieveFLPOfficeMRTBusinessObjectList(contract.getStateCode());

			Office item = null;
			Iterator<Office> it = servicelist.iterator();
			while (it.hasNext()) {
				item = it.next();

				if (item != null) {
					int offRefId = item.getRefId();
					MrtLookUpBO serviceLookUp = new MrtLookUpBO(contract.getAgencyToken(), item.getOfficeCode().trim(),
							item.getName(), Integer.toString(offRefId));

					list.add(serviceLookUp);
				}
			}
		} catch (Throwable ex) {
			String errorMsg = "Exception thrown in  MRTProxyBS.retrieveServiceCenterList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}
		return list;
	}

	public Map<String, Set<String>> retrieveFlpCodesByFsaCodeMapForSCIMSCustomer(List<String> fsaStAndLocArCodes,
			AgencyToken token) throws DLSBusinessFatalException {

		Map<String, Set<String>> flpsCodesByFsaCodeMap = new HashMap<String, Set<String>>();

		try {
			for (String fsaStateCountyCode : fsaStAndLocArCodes) {

				List<LocationArea> flpLocArLst = flpLocationAreaDataMartBusinessService
						.flpByFsaStateLocArea(fsaStateCountyCode);

				if ((flpLocArLst != null) && (!flpLocArLst.isEmpty())) {

					Set<String> flpCodes = new HashSet<String>();

					for (LocationArea area : flpLocArLst) {

						flpCodes.add(area.getStateLocationAreaCode());

					}

					flpsCodesByFsaCodeMap.put(fsaStateCountyCode, flpCodes);

				}
			}
		} catch (BusinessServiceBindingException dmbse) {
			logger.error("MRTProxyBS.retrieveFlpCodesByFsaCodeMapForSCIMSCustomer datamart business stop error ",
					dmbse);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveFlpCodesByFsaCodeMapForSCIMSCustomer datamart business stop error "
							+ dmbse.getMessage());
		}

		return flpsCodesByFsaCodeMap;
	}

	public List<LocationArea> retrieveFLPOfficeCodeListForScimsCustomer(List<String> fsaStAndLocArCodes,
			AgencyToken token) throws DLSBusinessFatalException {

		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

		try {
			for (String code : fsaStAndLocArCodes) {

				List<LocationArea> flpLocArLst = flpLocationAreaDataMartBusinessService.flpByFsaStateLocArea(code);

				if ((flpLocArLst != null) && (!flpLocArLst.isEmpty())) {
					for (LocationArea item : flpLocArLst) {
						fLPLocationAreaMRTBusinessObjectList.add(item);
					}
				}
			}
		} catch (BusinessServiceBindingException dmbse) {
			logger.error("MRTProxyBS.retrieveFLPOfficeCodeListForScimsCustomer datamart business stop error ", dmbse);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveFLPOfficeCodeListForScimsCustomer datamart business stop error "
							+ dmbse.getMessage());
		}

		return fLPLocationAreaMRTBusinessObjectList;
	}

	public List<LocationArea> retrieveFlpLocationAreaCodesByServiceCenterOffices(String[] serviceCenters)
			throws DLSBusinessFatalException {

		List<LocationArea> fLPLocationAreList = new ArrayList<LocationArea>();
		try {
			fLPLocationAreList = flpLocationAreaDataMartBusinessService.flpByFlpCodeList(serviceCenters);
		} catch (BusinessServiceBindingException e) {
			logger.error("MRTProxyBS.retrieveFlpLocationAreaCodesByServiceCenterOffices business stop error ", e);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveFlpLocationAreaCodesByServiceCenterOffices business stop error "
							+ e.getMessage());
		}

		return fLPLocationAreList;
	}

	/**
	 * 
	 * 
	 * @param stateFsaCode
	 * @return
	 * @throws DLSBusinessFatalException
	 */
	@Override
	public List<LocationArea> retrieveFSALocationAreasByFSAStateCode(String stateFsaCode)
			throws DLSBusinessFatalException {
		List<LocationArea> fLPLocationAreList = new ArrayList<LocationArea>();
		try {
			fLPLocationAreList = flpLocationAreaDataMartBusinessService.byStateFsa(stateFsaCode,
					FsaLocationAreaProperties.code, FsaLocationAreaProperties.name, FsaLocationAreaProperties.stateCode,
					FsaLocationAreaProperties.stateLocationAreaCode);
		} catch (BusinessServiceBindingException e) {
			logger.error("MRTProxyBS.retrieveFSALocationAreasByFSAStateCode business stop error ", e);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveFSALocationAreasByFSAStateCode business stop error " + e.getMessage());
		}

		return fLPLocationAreList;
	}

	/**
	 * @param stateFsaCode
	 * @param locAreaFsaCode
	 * @return
	 * @throws BusinessServiceBindingException
	 * 
	 */
	@Override
	public List<Office> retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode(String stateFsaCode,
			String locAreaFsaCode) throws DLSBusinessFatalException {
		List<Office> officeObjectList = null;
		try {

			officeObjectList = flpOfficeMRTBusinessService.byStateAndLocationArea(stateFsaCode, locAreaFsaCode,
					FsaOfficeProperties.id, FsaOfficeProperties.officeCode, FsaOfficeProperties.name,
					FsaOfficeProperties.stateAbbrev, FsaOfficeProperties.countyName, FsaOfficeProperties.siteName,
					FsaOfficeProperties.countyFipsCode);
		} catch (BusinessServiceBindingException e) {
			logger.error("MRTProxyBS.retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode business stop error ", e);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode business stop error " + e.getMessage());
		}
		return officeObjectList;

	}

	/**
	 * Need to check which attribute to use, stateLoacationAreaFLPCode or
	 * stateLocationAreaCode. MRTFacadeBusinessServiceImpl uses the later (works)
	 * and MRTProxyBS uses the first which does not work. FLSS-6510 if an office
	 * does not exist (not mapped to location Area), just igore it.
	 */
	public List<Office> retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(List<LocationArea> flpOfficeList,
			AgencyToken token) throws DLSBusinessFatalException {
		List<Office> flpOfficeLocXRefMRTBusinessObjects = new ArrayList<Office>();

		for (LocationArea item : flpOfficeList) {
			try {
				List<String> completeFlpLocArLst = new ArrayList<String>();
				String code = item.getStateLocationAreaCode();
				if(!StringUtil.isEmptyString(code)){
					completeFlpLocArLst.add(code);	
					if (!completeFlpLocArLst.isEmpty()) {
						String[] completeFlpLocArLstArray = completeFlpLocArLst.toArray(new String[0]);
						List<Office> officeList = flpOfficeMRTBusinessService
								.flpServiceCenterOfficesByFlpStateAndLocAreas(completeFlpLocArLstArray);
						if (officeList != null && !officeList.isEmpty()) {
							flpOfficeLocXRefMRTBusinessObjects.addAll(officeList);	
						}
					}
				}
			} catch (BusinessServiceBindingException dmbse) {
				logger.error(
						"MRTProxyBS.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer datamart business stop error ",
						dmbse);
				if (!dmbse.getMessage().contains(NODATAFOUND)) {
					throw new DLSBusinessFatalException(
							"ServiceFacade.retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer datamart business stop error "
									+ dmbse.getMessage());
				}
			}
		}
		return flpOfficeLocXRefMRTBusinessObjects;
	}

	public List<MrtLookUpBO> retrieveCountyList(RetrieveMRTCountyListBC contract) throws DLSBusinessFatalException {
		List<MrtLookUpBO> locationAreaLookUpList = new ArrayList<MrtLookUpBO>();
		try {

			List<LocationArea> locationAreaList = flpLocationAreaDataMartBusinessService
					.flpByOfficeRefId(Integer.valueOf(contract.getOfficeReferenceCode()));

			LocationArea item = null;
			Iterator<LocationArea> it = locationAreaList.iterator();
			while (it.hasNext()) {
				item = it.next();
				if (item != null) {
					String code = item.getStateLocationAreaCode();
					String stateRefId = item.getStateRefId().toString();
					MrtLookUpBO countyLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), code,
							item.getAlternateName(), stateRefId);
					locationAreaLookUpList.add(countyLookUpBO);
				}
			}
		} catch (BusinessServiceBindingException ex) {
			String errorMsg = "Exception thrown in MRTProxyBS.retrieveCountyList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		} catch (Throwable ex) {
			String errorMsg = "Exception thrown in MRTProxyBS.retrieveCountyList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}
		return locationAreaLookUpList;
	}

	public OfficeInfo retrieveFsaOfficeForScimsLandingFromUserEasOfficeCodes(List<String> officeCodeList) {
		OfficeInfo officeInfo = new OfficeInfo();
		if (officeCodeList == null || officeCodeList.size() == 0) {
			setScimsDefaultLanding(officeInfo);
			return officeInfo;
		}
		for (int i = 0; i < officeCodeList.size(); i++) {
			String fsaCode = getFSAStateCountyCodeFromStateLocationAreaFLPCode(officeCodeList.get(i));
			if (SCIMS_DEFAULT_LANDING_CBS_ERROR.equals(fsaCode)) {
				break;
			}
			if (!StringUtil.isEmptyString(fsaCode) && fsaCode.length() > 4) {
				officeInfo.setServiceCenterState(fsaCode.substring(0, 2));
				officeInfo.setServiceCenterCounty(fsaCode.substring(2, 5));
				officeInfo.setFsaCode(fsaCode);
				break;
			}
		}

		if (StringUtil.isEmptyString(officeInfo.getServiceCenterState())) {
			String[] flpStateCode = { officeCodeList.get(0).substring(0, 2).concat("300") };

			try {
				List<Office> ofcList = retrieveFlpServiceCentersByStateOffices_light(flpStateCode);
				officeInfo.setServiceCenterState(ofcList.get(0).getStateFipsCode());
			} catch (DLSBusinessFatalException e) {
				setScimsDefaultLanding(officeInfo);
			}

		}
		return officeInfo;

	}

	private void setScimsDefaultLanding(OfficeInfo officeInfo) {
		officeInfo.setFsaCode(SCIMS_DEFAULT_LANDING_01001);
		officeInfo.setServiceCenterState("01");
	}

	
	@Override
	public Map<String, String> getFSAStateCountyOfficesFromFLPCodes( List<String> flpOfficeCodes) {
		Map<String, String> flpOfficeCodeToFSAOfficeCodeMap = new HashMap<String,String>();
		
		String[] flpOfficeCodeArray = flpOfficeCodes.toArray(new String[0]);
		List<Office> flpOfficeList = null;
		try {
			flpOfficeList = 
				flpOfficeMRTBusinessService.flpOfficesByFlpCodeList(flpOfficeCodeArray,
						FlpOfficeProperties.id,
						FlpOfficeProperties.officeCode);
		}catch(Exception ex) {
			logger.info("faield to flpOfficesByFlpCodeList()..." + ex.getMessage(), ex);
		}
		
		if(flpOfficeList != null && !flpOfficeList.isEmpty()) {
			List<Integer> flpOfficeIdList = new ArrayList<Integer>();
			for(Office flpOffice: flpOfficeList) {
				flpOfficeIdList.add(flpOffice.getId());
			}
			
			Integer[] flpOfficeIdArray = flpOfficeIdList.toArray(new Integer[0]);
			
			List<Office> fsaOfficeObjects = null;
			try {
				fsaOfficeObjects = flpOfficeMRTBusinessService.byOfficeIdList(flpOfficeIdArray, FsaOfficeProperties.id,
					FsaOfficeProperties.officeCode);
			}catch(Exception ex) {
				logger.info("faield to flpOfficeMRTBusinessService.byOfficeIdList()..." + ex.getMessage(), ex);
			}
			if(fsaOfficeObjects != null && !fsaOfficeObjects.isEmpty()) {
				Integer flpOfficeId;
				String flpOfficeCode;
				for(Office flpOffice: flpOfficeList) {
					flpOfficeId = flpOffice.getId();
					flpOfficeCode = flpOffice.getOfficeCode();
					
					for(Office fsaOffice: fsaOfficeObjects) {
						if(flpOfficeId.equals(fsaOffice.getId())) {
							flpOfficeCodeToFSAOfficeCodeMap.put(flpOfficeCode, fsaOffice.getOfficeCode());
						}
					}
				}
			}
		}
		return flpOfficeCodeToFSAOfficeCodeMap;
	}
		
	@Override
	public String getFSAStateCountyCodeFromStateLocationAreaFLPCode(String office_flp_code_str) {
		StringBuilder fsaCode = new StringBuilder();

		/*
		 * DLS-4340: SCIMS_DEFAULT_LANDING_01001; 1) Use the same CBS
		 * rtvFLPOfficesByOfficeFLPCdLst to get the Office's OIP Office Identifier.
		 */
		List<String> officeStrCodes = new ArrayList<String>();
		officeStrCodes.add(office_flp_code_str);
		Map<String, Office> officeObjectOfficeStrCodeMap = new HashMap<String, Office>();
		try {
			officeObjectOfficeStrCodeMap = retriveFLPOfficesByOfficeFLPCdMap(officeStrCodes);
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				String errorMsg = "Exception thrown in  MRTProxyBS.retriveFLPOfficesByOfficeFLPCdMap() ";
				logger.debug(errorMsg, ex);
			}
			fsaCode.append(SCIMS_DEFAULT_LANDING_CBS_ERROR);
		}

		/*
		 * 2)Use a CBS like the following, RetrieveFSAOfficeListByOfficeIdentifierList,
		 * to get the corresponding FSA Office.
		 */
		Office flpObject = officeObjectOfficeStrCodeMap.get(office_flp_code_str);
		if (flpObject != null) {
			int flpId = flpObject.getId();
			List<Integer> flpIdCodes = new ArrayList<Integer>();
			flpIdCodes.add(flpId);
			List<Office> fsaOfficeObjects = retrieveFSAOfficeListByOfficeIdentifierList(flpIdCodes);
			String siteId = "";
			String toke = "::";
			for (Office item : fsaOfficeObjects) {
				if (!fsaCode.toString().contains(toke)) {
					fsaCode.append(item.getOfficeCode().trim());
					siteId = Integer.toString(item.getSiteId());
					fsaCode.append(toke);
					fsaCode.append(siteId);
				}
			}
		}
		return fsaCode.toString();
	}

	public List<Office> retrieveFSAOfficeListByOfficeIdentifierList(List<Integer> flpIdCodes) {
		List<Office> officeObjectList = new ArrayList<Office>();
		try {
			Integer[] flpIdCodesArray = new Integer[flpIdCodes.size()];
			for (int index = 0; index < flpIdCodes.size(); index++) {
				flpIdCodesArray[index] = flpIdCodes.get(index);
			}
			officeObjectList = flpOfficeMRTBusinessService.byOfficeIdList(flpIdCodesArray, FsaOfficeProperties.id,
					FsaOfficeProperties.locStateAbbrev, FsaOfficeProperties.locCityName,
					FsaOfficeProperties.stateAbbrev, FsaOfficeProperties.officeCode, FsaOfficeProperties.name,
					FsaOfficeProperties.cityFipsCode, FsaOfficeProperties.refId, FsaOfficeProperties.siteId,
					FsaOfficeProperties.mailingZipCode, FsaOfficeProperties.mailingAddrInfoLine,
					FsaOfficeProperties.mailingAddrLine);
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("failed to get office from retrieveFSAOfficeListByOfficeIdentifierList() ... ", ex);
			}
		}
		return officeObjectList;

	}

	public Map<String, Office> retriveFLPOfficesByOfficeFLPCdMap(List<String> flpStringCodes) {
		String[] flpStringCodesArray = flpStringCodes.toArray(new String[0]);
		return convertToMap(flpOfficeMRTBusinessService.flpOfficesByFlpCodeList(flpStringCodesArray));

	}

	public List<Office> retrieveOfficesByFlpCodes(List<String> flpStringCodes) {
		String[] flpStringCodesArray = flpStringCodes.toArray(new String[0]);
		return flpOfficeMRTBusinessService.flpOfficesByFlpCodeList(flpStringCodesArray);
	}

	private Map<String, Office> convertToMap(List<Office> flpOfficesByFlpCodeList) {
		Map<String, Office> flpOfficesMap = new HashMap<String, Office>();
		for (Office ofc : flpOfficesByFlpCodeList) {
			flpOfficesMap.put(ofc.getOfficeCode().trim(), ofc);
		}
		return flpOfficesMap;
	}

	public void setFlpOfficeMRTBusinessService(OfficeDataServiceProxy flpOfficeMRTBusinessService) {
		this.flpOfficeMRTBusinessService = flpOfficeMRTBusinessService;
	}

	public void setFlpStateMRTBusinessService(StateDataServiceProxy flpStateMRTBusinessService) {
		this.flpStateMRTBusinessService = flpStateMRTBusinessService;
	}

	public void setInterestRateDataMartBusinessService(
			InterestRateDataServiceProxy interestRateDataMartBusinessService) {
		this.interestRateDataMartBusinessService = interestRateDataMartBusinessService;
	}

	public void setFlpLocationAreaDataMartBusinessService(
			LocationAreaDataServiceProxy flpLocationAreaDataMartBusinessService) {
		this.flpLocationAreaDataMartBusinessService = flpLocationAreaDataMartBusinessService;
	}

	public void setBusinessPartyDataService(BusinessPartyDataServiceProxy businessPartyDataService) {
		this.businessPartyDataService = businessPartyDataService;
	}

	public List<MrtLookUpBO> retrieveFSACountyList(RetrieveFsaCountyListBC contract) throws DLSBusinessFatalException {
		List<MrtLookUpBO> fsalocationAreaLookUpList = new ArrayList<MrtLookUpBO>();

		try {

			List<LocationArea> countyList = flpLocationAreaDataMartBusinessService.agByStateFsa(
					contract.getFsaStateCode(), FsaLocationAreaProperties.id, FsaLocationAreaProperties.code,
					FsaLocationAreaProperties.name, FsaLocationAreaProperties.shortName,
					FsaLocationAreaProperties.categoryName);

			if (countyList != null && !countyList.isEmpty()) {
				for (LocationArea cbsCounty : countyList) {
					String fsaCode = cbsCounty.getCode();
					String countyName = cbsCounty.getName();
					boolean isMultipleCounty = (cbsCounty.getCategoryName() != null
							&& cbsCounty.getCategoryName().contains("Multiple"));

					if (isMultipleCounty && !ValidationUtils.isNullOrEmpty(cbsCounty.getShortName())) {
						countyName = cbsCounty.getShortName();
					}

					MrtLookUpBO countyLookUpBO = new MrtLookUpBO(contract.getAgencyToken(), fsaCode, countyName,
							cbsCounty.getId().toString());
					fsalocationAreaLookUpList.add(countyLookUpBO);

				}
			}

		} catch (BusinessServiceBindingException ex) {
			String errorMsg = "Exception thrown in MRTProxyBS.retrieveFSACountyList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		} catch (Throwable ex) {
			String errorMsg = "Exception thrown in MRTProxyBS.retrieveFSACountyList() ";
			logger.error(errorMsg, ex);
			throw new DLSBusinessFatalException(errorMsg + ": " + ex.getMessage());
		}
		return fsalocationAreaLookUpList;
	}

	public InterestRate retrieveInterestRate(Integer typeId, Date date) throws DLSBusinessFatalException {
		InterestRate interestRate = null;
		try {
			interestRate = interestRateDataMartBusinessService.byTypeIdAndDate(typeId, date);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveInterestRates", e);
		}
		return interestRate;
	}

	public List<InterestRate> retrieveInterestRates(Integer[] typeIds, Date date) throws DLSBusinessFatalException {
		List<InterestRate> interestRates = new ArrayList<InterestRate>();
		try {
			interestRates = interestRateDataMartBusinessService.byTypeIdListAndDate(typeIds, date);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveInterestRates", e);
		}
		return interestRates;
	}

	public List<gov.usda.fsa.citso.cbs.dto.InterestRate> retrieveIntRatesByRateTypeIdLstAndDtRng(List<String> typeIds,
			Date fromDate, Date toDate) throws DLSBusinessFatalException {
		List<InterestRate> interestRates = new ArrayList<InterestRate>();
		try {
			if (typeIds != null) {
				List<gov.usda.fsa.citso.cbs.bc.InterestTypeId> interestTypeIds = new ArrayList<gov.usda.fsa.citso.cbs.bc.InterestTypeId>();
				for (String typeId : typeIds) {
					gov.usda.fsa.citso.cbs.bc.InterestTypeId interestTypeId = new gov.usda.fsa.citso.cbs.bc.InterestTypeId(
							Integer.valueOf(typeId));
					interestTypeIds.add(interestTypeId);
				}

				interestRates = interestRateDataMartBusinessService.byTypeIdListAndDateRange(interestTypeIds, fromDate,
						toDate);
			}

		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: rtvIntRatesByRateTypeIdLstAndDtRng", e);
		}
		return interestRates;
	}

	public BusinessPartyRole lookupUser(String eAuthID) throws DLSBusinessFatalException {
		BusinessPartyRole businessPartyRole = null;
		try {
			businessPartyRole = businessPartyDataService.roleByAuthId(eAuthID);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: lookupUser", e);
		}
		return businessPartyRole;
	}

	public String lookupUserEmployeeCamsId(String eAuthID) {
		String empoyeeId = "";
		try {
			BusinessPartyInfo businessPartyInfo = businessPartyDataService.infoByAuthId(eAuthID,
					AgencyEmployeeProperties.camsEmployeeId);
			if (businessPartyInfo != null) {
				AgencyEmployee[] agencyEmployees = businessPartyInfo.getAgencyEmployee();
				for (AgencyEmployee agencyEmployee : agencyEmployees) {
					empoyeeId = agencyEmployee.getCamsEmployeeId();
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Failed to find employeeID for eAuth User: " + eAuthID);
		}
		return empoyeeId;
	}

	public List<Office> retrieveFlpStateOffices() throws DLSBusinessFatalException {
		List<Office> allStateOffices = new ArrayList<Office>();
		try {
			allStateOffices = flpOfficeMRTBusinessService.allFlpStateOffices();
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFlpStateOffices", e);
		}
		return allStateOffices;
	}

	/**
	 * Only need to load state name, abbreviation and officeCode for each state
	 * 
	 */
	public List<Office> retrieveFlpStateOffices_light() throws DLSBusinessFatalException {
		List<Office> allStateOffices = new ArrayList<Office>();
		try {
			allStateOffices = flpOfficeMRTBusinessService.allFlpStateOffices(FlpOfficeProperties.stateName,
					FlpOfficeProperties.stateAbbrev, FlpOfficeProperties.stateFipsCode, FlpOfficeProperties.officeCode);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFlpStateOffices", e);
		}
		return allStateOffices;
	}

	public List<Office> retrieveFlpServiceCentersByStateOffices(String[] stateOffices)
			throws DLSBusinessFatalException {
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		try {

			serviceCenterOffices = flpOfficeMRTBusinessService
					.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(stateOffices);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFlpServiceCentersByStateOffices", e);
		}
		return serviceCenterOffices;
	}

	public List<Office> retrieveFlpServiceCentersByStateOffices(List<String> stateOffices)
			throws InvalidBusinessContractDataException, DLSBusinessFatalException {
		validateStateOfficeList(stateOffices);
		String[] stateOfficeIds = stateOffices.toArray(new String[stateOffices.size()]);
		return retrieveFlpServiceCentersByStateOffices(stateOfficeIds);
	}

	/**
	 * Lighter version: only load officeCode, countyFLPCode and CountyName
	 */
	public List<Office> retrieveFlpServiceCentersByStateOffices_light(String[] stateOffices)
			throws DLSBusinessFatalException {
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		try {
			serviceCenterOffices = flpOfficeMRTBusinessService.fsaFlpServiceCentreOfficesByOfficeFlpCodeList(
					stateOffices, FlpOfficeProperties.officeCode, FlpOfficeProperties.countyFipsCode,
					FlpOfficeProperties.countyName, FlpOfficeProperties.stateFipsCode);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFlpServiceCentersByStateOffices", e);
		}
		return serviceCenterOffices;
	}

	public List<Office> retrieveFlpServiceCentersByStateAbbr(String stateAbbr) throws DLSBusinessFatalException {
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		try {
			serviceCenterOffices = flpOfficeMRTBusinessService.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFlpServiceCentersByStateOffices", e);
		}
		return serviceCenterOffices;
	}

	public State retrieveStateByFlpFipsCode(String flpFipsCode) throws DLSBusinessFatalException {
		State state = null;
		try {
			state = flpStateMRTBusinessService.byFlpFipsCode(flpFipsCode, FlpStateProperties.code,
					FlpStateProperties.name, FlpStateProperties.abbreviation);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveStateByFlpFipsCode", e);
		}
		return state;
	}

	public List<Office> retrieveFsaFlpServiceCenterOfficesByStateAbbr(String stateAbbr)
			throws DLSBusinessFatalException {
		List<Office> serviceCenterOffices = new ArrayList<Office>();
		try {
			serviceCenterOffices = flpOfficeMRTBusinessService.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr,
					FlpOfficeProperties.id, FlpOfficeProperties.mailingZipCode, FlpOfficeProperties.mailingAddrInfoLine,
					FlpOfficeProperties.mailingAddrLine, FlpOfficeProperties.mailingCity,
					FlpOfficeProperties.mailingStateAbbrev, FlpOfficeProperties.stateFipsCode,
					FlpOfficeProperties.stateName, FlpOfficeProperties.officeCode, FlpOfficeProperties.name,
					FlpOfficeProperties.countyName, FlpOfficeProperties.locCityName);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("exception: retrieveFsaFlpServiceCenterOfficesByStateAbbr", e);
		}
		return serviceCenterOffices;
	}

	public List<LocationArea> retrieveFlpAreaListByStateAbbr(String abbr) throws DLSBusinessFatalException {
		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

		try {

			fLPLocationAreaMRTBusinessObjectList = flpLocationAreaDataMartBusinessService.flpByStateAbbr(abbr,
					FlpLocationAreaProperties.stateCode, FlpLocationAreaProperties.stateLocationAreaCode,
					FlpLocationAreaProperties.stateName, FlpLocationAreaProperties.shortName,
					FlpLocationAreaProperties.stateRefId);
		} catch (BusinessServiceBindingException dmbse) {
			throw new DLSBusinessFatalException("retrieveFlpAreaListByStateAbbr " + dmbse.getMessage(), dmbse);
		}
		return fLPLocationAreaMRTBusinessObjectList;
	}

	private MRTProxyBS() {

	}

	public List<EmployeeOrgChart> retrieveOrgChartsByEmployeeId(String employeeId) throws DLSBusinessFatalException {
		List<EmployeeOrgChart> employeeOrgChartList = new ArrayList<EmployeeOrgChart>();

		try {
			employeeOrgChartList = getEmployeeDataServiceProxy().orgChartsByEmployeeId(employeeId);
		} catch (Exception e) {
			throw new DLSBusinessFatalException("retrieveOrgChartsByEmployeeId " + e.getMessage(), e);
		}

		return employeeOrgChartList;
	}

	public EmployeeData retrieveEmployeeData(AgencyToken token, String agencyCode) {
		String employeeID = lookupUserEmployeeCamsId(token.getUserIdentifier());
		return retrieveEmployeeData(employeeID, agencyCode);
	}

	public EmployeeData retrieveEmployeeData(String employeeID, String agencyCode) {
		EmployeeData ouput = new EmployeeData(agencyCode, employeeID);
		if (StringUtil.isEmptyString(employeeID)) {
			if (logger.isDebugEnabled()) {
				logger.debug(" Empty parameter unable to retrieve employee data from Icams for " + employeeID);
			}
			return ouput;
		}
		IcamsEmployee icamsEmployee = retrieveIcammsEmployee(agencyCode, employeeID);
		if (icamsEmployee != null) {
			ouput.setEmail(icamsEmployee.getEmailAddress());
			PartyBO partyBO = new PartyBO();
			partyBO.setFirstName(icamsEmployee.getEmployeeFirstName());
			partyBO.setLastName(icamsEmployee.getEmployeeLastName());
			partyBO.setTitleName(icamsEmployee.getPositionTitle());
			partyBO.setEmployeeIdentification(icamsEmployee.getEmployeeIdentifier());
			ouput.setOfficePhone(icamsEmployee.getPhone());
			AddressBO officeAddressBO = new AddressBO();
			Office office = null;
			try {
				office = retrieveIcammsEmployeeOffice(icamsEmployee.getPrimaryWorkOfficeIdentifier());
				if (office != null) {
					ouput.setOfficeName(office.getName());
					officeAddressBO.setCityName(office.getLocCityName());
					officeAddressBO.setStateAbbreviation(office.getStateAbbrev());
					officeAddressBO.setZipCode(office.getMailingZipCode());
					officeAddressBO.setMailingAddressLine(office.getMailingAddrInfoLine());
					officeAddressBO.setDeliveryAddressLine(office.getMailingAddrLine());
				}
			} catch (Exception ex) {
				if (logger.isDebugEnabled()) {
					logger.debug(" Failed to call retrieveIcammsEmployeeOffice by user's primary working office id "
							+ icamsEmployee.getPrimaryWorkOfficeIdentifier(), ex);
				}
			}
			ouput.setPartyBO(partyBO);
			ouput.setOfficeAddressBO(officeAddressBO);
		}
		return ouput;
	}

	public Office retrieveIcammsEmployeeOffice(Integer officeId) {
		Office office = null;
		if (officeId == null) {
			return office;
		}
		List<Integer> offList = new ArrayList<Integer>();

		offList.add(officeId);
		if (logger.isDebugEnabled()) {
			logger.debug(" Trying to get office info from mrt for office id =" + officeId);
		}
		List<Office> offResult = retrieveFSAOfficeListByOfficeIdentifierList(offList);
		if (null != offResult && !offResult.isEmpty()) {
			office = offResult.get(0);
			if (logger.isDebugEnabled()) {
				logger.debug(" Success getting  office info from mrt for office id =" + officeId + " office code ="
						+ office.getOfficeCode());
			}
		}
		return office;
	}

	public String retrieveEmployeeEmail(String agencyCodeStr, String employeeIdStr) {
		IcamsEmployee employee = retrieveIcammsEmployee(agencyCodeStr, employeeIdStr);
		if (employee == null) {
			return "";
		}
		return employee.getEmailAddress();
	}

	private IcamsEmployee retrieveIcammsEmployee(String agencyCodeStr, String employeeIdStr) {
		EmployeeId employeeId = new EmployeeId(employeeIdStr);
		AgencyCode agencyCode = new AgencyCode(agencyCodeStr);
		IcamsEmployee icamsEmployee = null;
		if (logger.isDebugEnabled()) {
			logger.debug(" Trying to get info from icams for employee id =" + employeeIdStr);
		}
		if (agencyCodeStr == null || employeeIdStr == null) {
			return icamsEmployee;
		}
		try {
			icamsEmployee = getEmployeeDataServiceProxy().findEmployeeByEmployeeIdentifier(employeeId, agencyCode,
					IcamsEmployeeProperties.values());
			if (icamsEmployee == null) {
				AgencyCode agencyCodeCE = new AgencyCode(EmployeeConstants.AGENCY_CODE_CE);
				icamsEmployee = getEmployeeDataServiceProxy().findEmployeeByEmployeeIdentifier(employeeId, agencyCodeCE,
						IcamsEmployeeProperties.values());
				if (logger.isDebugEnabled()) {
					logger.debug(" IcamsEmployee for Agency code CE " + " Employee Id =" + employeeIdStr);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(" Success found in icams employee id =" + employeeIdStr);
			}
			return icamsEmployee;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(" Unable to retrieve Employee Data from Icams for " + employeeIdStr + " : error : "
						+ e.getMessage(), e);
			}
		}
		return icamsEmployee;
	}

	public MRTFacadeBusinessService getMrtFacadeBusinessService() {
		return mrtFacadeBusinessService;
	}

	public void setMrtFacadeBusinessService(MRTFacadeBusinessService mrtFacadeBusinessService) {
		this.mrtFacadeBusinessService = mrtFacadeBusinessService;
	}

	public void setEmployeeDataServiceProxy(EmployeeDataServiceProxy employeeDataServiceProxy) {
		this.employeeDataServiceProxy = employeeDataServiceProxy;
	}

	public List<LocationArea> flpByFlpCodeList(String[] flpCds) throws DLSBusinessFatalException {
		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

		try {

			fLPLocationAreaMRTBusinessObjectList = flpLocationAreaDataMartBusinessService.flpByFlpCodeList(flpCds);
		} catch (BusinessServiceBindingException dmbse) {
			throw new DLSBusinessFatalException("flpByFlpCodeList " + dmbse.getMessage(), dmbse);
		} catch (Exception ee) {
			logger.error("Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() call", ee);
			logger.info(
					"Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() call and dump of input to follow: ");
			logger.info(flpCds);
			throw new DLSBusinessFatalException(
					"Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() " + ee.getMessage(), ee);
		}

		return fLPLocationAreaMRTBusinessObjectList;
	}

	/**
	 * shorter version only load code, county name and short name
	 */
	public List<LocationArea> flpByFlpCodeList_light(String[] flpCds) throws DLSBusinessFatalException {
		List<LocationArea> fLPLocationAreaMRTBusinessObjectList = new ArrayList<LocationArea>();

		try {
			fLPLocationAreaMRTBusinessObjectList = flpLocationAreaDataMartBusinessService.flpByFlpCodeList(flpCds,
					FlpLocationAreaProperties.stateCode, FlpLocationAreaProperties.stateLocationAreaCode,
					FlpLocationAreaProperties.stateName, FlpLocationAreaProperties.code, FlpLocationAreaProperties.name,
					FlpLocationAreaProperties.shortName);
		} catch (BusinessServiceBindingException dmbse) {
			throw new DLSBusinessFatalException("flpByFlpCodeList " + dmbse.getMessage(), dmbse);
		} catch (Exception ee) {
			logger.error("Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() call", ee);
			logger.info(
					"Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() call and dump of input to follow: ");
			logger.info(flpCds);
			throw new DLSBusinessFatalException(
					"Exception from flpLocationAreaDataMartBusinessService.flpByFlpCodeList() " + ee.getMessage(), ee);
		}

		return fLPLocationAreaMRTBusinessObjectList;
	}

	public TaxIdSurrogateBusinessServiceProxy getSurrogateService() {
		return surrogateService;
	}

	public void setSurrogateService(TaxIdSurrogateBusinessServiceProxy surrogateService) {
		this.surrogateService = surrogateService;
	}

	public BusinessPartyDataServiceProxy getBusinessPartyDataService() {
		return businessPartyDataService;
	}

	public EmployeeDataServiceProxy getEmployeeDataServiceProxy() {
		return employeeDataServiceProxy;
	}

	public void setCountyDataServiceProxy(CountyDataServiceProxy countyDataServiceProxy) {
		this.countyDataServiceProxy = countyDataServiceProxy;
	}

	public CountyDataServiceProxy getCountyDataServiceProxy() {
		return countyDataServiceProxy;
	}

	public StateDataServiceProxy getStateDataServiceProxy() {
		return flpStateMRTBusinessService;
	}

	@Override
	public List<County> retrieveCountyByStateFipsCode(String stateFipsCode) throws DLSBusinessStopException {
		try {
			return getCountyDataServiceProxy().byStateFips(stateFipsCode, CountyProperties.countyCode,
					CountyProperties.countyId, CountyProperties.countyName, CountyProperties.countyFips,
					CountyProperties.stateFips, CountyProperties.stateName, CountyProperties.stateCode,
					CountyProperties.stateAndCountyCode, CountyProperties.stateAndCountyFips,
					CountyProperties.stateAbbrev);
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving  MRTProxyBS.retrieveCountyByStateFipsCode() ";
			logger.error(errorMsg, e);
			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage());
		}
	}

	@Override
	public List<County> retrieveCountyByStateAbbrev(String stateAbbrev) throws DLSBusinessStopException {
		try {
			List<State> allStates = retrieveFLPStateList();
			List<gov.usda.fsa.citso.cbs.dto.County> result = null;
			for (State state : allStates) {
				if (stateAbbrev.equalsIgnoreCase(state.getAbbreviation())) {
					result = retrieveCountyByStateFipsCode(state.getFipsCode());
				}
			}
			return result;
		} catch (Exception e) {
			String errorMsg = "Exception when retrieving  MRTProxyBS.retrieveCountyByStateFipsCode() ";
			logger.error(errorMsg, e);
			throw new DLSBusinessStopException(errorMsg + ": " + e.getMessage());
		}
	}

	public IcamsEmployee findEmployeeByEmployeeIdentifier(String employeeIdStr) {
		EmployeeId employeeId = new EmployeeId(employeeIdStr);
		AgencyCode agencyCode = new AgencyCode(EmployeeConstants.AGENCY_CODE);
		return getEmployeeDataServiceProxy().findEmployeeByEmployeeIdentifier(employeeId, agencyCode,
				IcamsEmployeeProperties.values());
	}

	public List<LocationArea> retrieveFlpLocAreaListByFSAStateAndLocAreaCode(String fsaLocAreacode) {
		return flpLocationAreaDataMartBusinessService.flpByFsaStateLocArea(fsaLocAreacode,
				FlpLocationAreaProperties.stateLocationAreaCode, FlpLocationAreaProperties.stateName,
				FlpLocationAreaProperties.name);
	}

	/* DLS-884 FLP list */
	public List<FlpOfficeLocationAreaBO> retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr_old(
			String stateAbbr) {
		List<FlpOfficeLocationAreaBO> flpOfficeLocationAreaBOList = new ArrayList<FlpOfficeLocationAreaBO>();
		FlpOfficeLocationAreaBO mapBO = null;

		try {
			/*
			 * get all the flp state loc area by state abbr:
			 * RetrieveFLPLocationAreaListByStateAbbreviation(CA)
			 */
			List<LocationArea> flpLocAreaList = flpLocationAreaDataMartBusinessService.flpByStateAbbr(stateAbbr,
					FlpLocationAreaProperties.stateName, FlpLocationAreaProperties.stateLocationAreaCode,
					FlpLocationAreaProperties.name);
			for (LocationArea flpLocArea : flpLocAreaList) {
				mapBO = new FlpOfficeLocationAreaBO();
				mapBO.setFlpStateLocationAreaCode(flpLocArea.getStateLocationAreaCode());
				mapBO.setFlpCountyName(flpLocArea.getName());
				mapBO.setStateName(flpLocArea.getStateName());
				/*
				 * getOfcCode for this locArea
				 */
				StateLocAreaCode code = new StateLocAreaCode(flpLocArea.getStateLocationAreaCode());
				List<StateLocAreaCode> arg0 = new ArrayList<StateLocAreaCode>();
				arg0.add(code);
				List<Office> ofc = flpOfficeMRTBusinessService.flpServiceCenterOfficesByFlpStateAndLocAreas(arg0,
						FlpOfficeProperties.stateFipsCode, FlpOfficeProperties.stateName,
						FlpOfficeProperties.officeCode);
				if (!ofc.isEmpty()) {
					mapBO.setFlpMailCode(ofc.get(0).getOfficeCode());
					mapBO.setStateName(ofc.get(0).getStateName());
				}
				flpOfficeLocationAreaBOList.add(mapBO);
			}

		} catch (Exception e) {
			logger.error(
					"Error retrieving MRT service retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() for State "
							+ stateAbbr + " . Error: " + e);
		}
		Collections.sort(flpOfficeLocationAreaBOList, new Comparator<FlpOfficeLocationAreaBO>() {
			public int compare(FlpOfficeLocationAreaBO s1, FlpOfficeLocationAreaBO s2) {
				int mailCd = s1.getFlpMailCode().compareTo(s2.getFlpMailCode());
				if (mailCd != 0) {
					return mailCd;
				}
				int countyName = s1.getFlpCountyName().compareTo(s2.getFlpCountyName());
				if (countyName != 0) {
					return countyName;
				}
				return s1.getFlpStateLocationAreaCode().compareTo(s2.getFlpStateLocationAreaCode());

			}
		});
		return flpOfficeLocationAreaBOList;
	}

	public List<FlpOfficeLocationAreaBO> retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(String stateAbbr) {
		List<FlpOfficeLocationAreaBO> flpOfficeLocationAreaBOList = new ArrayList<FlpOfficeLocationAreaBO>();
		try {
			/*
			 * retrieve list of flp mail code for state
			 */
			List<Office> flpOfcList = flpOfficeMRTBusinessService.fsaFlpServiceCenterOfficesByStateAbbr(stateAbbr,
					FlpOfficeProperties.stateFipsCode, FlpOfficeProperties.stateName, FlpOfficeProperties.officeCode,
					FlpOfficeProperties.name, FlpOfficeProperties.countyName);
			FlpOfficeLocationAreaBO mapBO = null;
			for (Office ofc : flpOfcList) {
				/*
				 * find flp StateLocArea
				 */
				if (!ofc.getOfficeCode().endsWith("300")) {
					try {
						mapBO = new FlpOfficeLocationAreaBO();
						mapBO.setFlpMailCode(ofc.getOfficeCode());
						mapBO.setStateName(ofc.getStateName());
						mapBO.setFlpCountyName(ofc.getCountyName());
						flpOfficeLocationAreaBOList.add(mapBO);
					} catch (Exception e) {
						logger.error(
								"Error retrieving flpLocationAreaDataMartBusinessService.flpByFlpCodeList() for ofce Code "
										+ ofc.getOfficeCode() + " . Error: " + e);
					}
				}

			}
		} catch (Exception e) {
			logger.error(
					"Error retrieving MRT service retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr() for State "
							+ stateAbbr + " . Error: " + e);
		}

		Collections.sort(flpOfficeLocationAreaBOList, new Comparator<FlpOfficeLocationAreaBO>() {
			public int compare(FlpOfficeLocationAreaBO s1, FlpOfficeLocationAreaBO s2) {
				int mailCd = s1.getFlpMailCode().compareTo(s2.getFlpMailCode());
				if (mailCd != 0) {
					return mailCd;
				}
				int countyName = s1.getFlpCountyName().compareTo(s2.getFlpCountyName());
				if (countyName != 0) {
					return countyName;
				}
				return s1.getFlpStateLocationAreaCode().compareTo(s2.getFlpStateLocationAreaCode());

			}
		});

		return flpOfficeLocationAreaBOList;
	}

	/*
	 * DLS-884 FIPS list(non-Javadoc)
	 * 
	 * @see
	 * gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS#
	 * retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(java.lang.
	 * String)
	 */
	public List<FipsOfficeLocationAreaBO> retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(
			String stateAbbr) {
		List<FipsOfficeLocationAreaBO> fipsOfficeLocationAreaBOList = new ArrayList<FipsOfficeLocationAreaBO>();
		try {
			FipsOfficeLocationAreaBO fipsOfficeLocationAreaBO = null;
			/*
			 * Get all the FIPS location Areas for the state.
			 */
			List<LocationArea> fsaLocAreaList = flpLocationAreaDataMartBusinessService.byStateAbbr(stateAbbr,
					FsaLocationAreaProperties.stateLocationAreaCode, FsaLocationAreaProperties.stateName,
					FsaLocationAreaProperties.name, FsaLocationAreaProperties.code);

			for (LocationArea fipsCode : fsaLocAreaList) {
				if (fipsCode.getCode().equalsIgnoreCase("000")) {
					continue;
				}
				fipsOfficeLocationAreaBO = new FipsOfficeLocationAreaBO();
				fipsOfficeLocationAreaBO.setFipsStateLocationAreaCode(fipsCode.getStateLocationAreaCode());
				fipsOfficeLocationAreaBO.setFipsCountyName(fipsCode.getName());
				fipsOfficeLocationAreaBO.setStateName(fipsCode.getStateName());
				fipsOfficeLocationAreaBOList.add(fipsOfficeLocationAreaBO);
			}

		} catch (Exception e) {
			logger.error(
					"Error retrieving MRT service retrieveFlpLocAreaListByFSAStateAndLocAreaCode() in initializeDomainOffcieBOs. Error msg is "
							+ e.getMessage());
		}

		Collections.sort(fipsOfficeLocationAreaBOList, new Comparator<FipsOfficeLocationAreaBO>() {
			public int compare(FipsOfficeLocationAreaBO s1, FipsOfficeLocationAreaBO s2) {
				int mailCd = s1.getFipsStateLocationAreaCode().compareTo(s2.getFipsStateLocationAreaCode());
				if (mailCd != 0) {
					return mailCd;
				}
				return s1.getFipsCountyName().compareTo(s2.getFipsCountyName());

			}
		});
		return fipsOfficeLocationAreaBOList;
	}

	public List<LocationArea> retrieveFlpLocationAreasByFipsLocationAreaCode(String fipsCode) {
		return flpLocationAreaDataMartBusinessService.flpByFsaStateLocArea(fipsCode,
				FlpLocationAreaProperties.stateLocationAreaCode);

	}

	public static String returnAlternateName(String name, String altName) {
		if (StringUtil.isEmptyString(altName)) {
			return name;
		}
		return name.compareToIgnoreCase(altName) != 0 ? altName : name;

	}

	@Override
	public Calendar retrieveCalendarByDate(Date todayDate) throws DLSBusinessFatalException {

		Calendar calendar = new Calendar();
		try {
			calendar = calendarDataServiceProxy.byCalendarDate(todayDate, CalendarProperties.federalHolidayIndicator,
					CalendarProperties.weekDayIndicator, CalendarProperties.calendarDate);

		} catch (BusinessServiceBindingException e) {
			logger.error("MRTProxyBS.retrieveFSALocationAreasByFSAStateCode business stop error ", e);
			throw new DLSBusinessFatalException(
					"MRTProxyBS.retrieveLocationAreasByFSAStateCode business stop error " + e.getMessage());
		}
		return calendar;
	}

	public void setCalendarDataServiceProxy(CalendarDataServiceProxy calendarDataServiceProxy) {
		this.calendarDataServiceProxy = calendarDataServiceProxy;
	}

	private Integer lookupOIP(List<String> stateOffices) throws InvalidBusinessContractDataException {
		Integer oipCode = null;
		if (!stateOffices.isEmpty()) {
			List<Office> offices = retrieveOfficesByFlpCodes(stateOffices);
			if (null == offices || offices.isEmpty()) {
				throw new InvalidBusinessContractDataException(
						"Given MRT offices list is null; Expected List<Office> returned from MRT for :" + stateOffices);
			}
			oipCode = offices.get(0).getId();
		}
		return oipCode;
	}

	private void validateStateOfficeList(List<String> stateOffices) throws InvalidBusinessContractDataException {
		if (null == stateOffices) {
			throw new InvalidBusinessContractDataException(
					"Given stateOffices list is null; Expected List<String(officeFLPCode)>");
		}
		if (stateOffices.isEmpty()) {
			throw new InvalidBusinessContractDataException(
					"Given stateOffices list is empty; Expected List<String(officeFLPCode)>");
		}

		for (Iterator<String> itr = stateOffices.iterator(); itr.hasNext();) {
			Object next = itr.next();
			if (!(next instanceof String)) {
				throw new InvalidBusinessContractDataException("Given stateOffices list has an unexpected type: "
						+ (null == next ? "null value" : next.getClass().getName())
						+ "; Expected List<String(officeFLPCode)>");
			}

			String code = (String) next;
			if (StringUtil.isEmptyString(code) || FLP_CODE_SIZE != code.length()
					|| !Pattern.matches(FLP_VALID_FORMAT, code)) {
				throw new InvalidBusinessContractDataException(
						"Given stateOffice code is null, or not valid (must 5 digits with the third one 3). stateOffice Code:  "
								+ code);
			}
		}
	}

	private List<Office> retrieveFsaFlpServiceCenterOfficesByOfficeIdList(List<Integer> oIpCode) {

		List<Office> officeObjectList;
		Integer[] oIpCodes = oIpCode.toArray(new Integer[0]);
		officeObjectList = flpOfficeMRTBusinessService.fsaFlpServiceCenterOfficesByOfficeIdList(oIpCodes);
		return officeObjectList;

	}

	public OfficeDataServiceProxy getFlpOfficeMRTBusinessService() {
		return flpOfficeMRTBusinessService;
	}
}
