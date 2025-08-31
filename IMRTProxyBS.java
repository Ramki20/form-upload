package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.usda.fsa.citso.cbs.bc.Surrogate;
import gov.usda.fsa.citso.cbs.bc.TaxId;
import gov.usda.fsa.citso.cbs.client.BusinessPartyDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.CountyDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.EmployeeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.StateDataServiceProxy;
import gov.usda.fsa.citso.cbs.dto.BusinessPartyRole;
import gov.usda.fsa.citso.cbs.dto.Calendar;
import gov.usda.fsa.citso.cbs.dto.EmployeeOrgChart;
import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.citso.cbs.ex.BusinessServiceBindingException;
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
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.OfficeInfo;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.OfficeInfoBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
/*Need to create proxy for new location domain shared services
 * https://wiki.tools.fsa.usda.gov/x/ppRaBQ is the main page for location domain.

	https://wiki.tools.fsa.usda.gov/x/xwh0BQ is the page for configuration, let me know if you need further assistance.

 */
public interface IMRTProxyBS {

	public Map<String, Surrogate> retrieveSurrogateIdForTaxId(List<String> taxIdList);
	
	public Map<String, TaxId> retrieveTaxIdForSurrogateId(List<String> surrogateIdList);
		
	public List<Office> retrieveFLPOfficeCodeListByFsaStateCountyCode(String fsaStateCountyCode);
	
	public List<Office> retrieveFlpServiceCentersByOIP(List<String> stateOffices)
			throws InvalidBusinessContractDataException;
			
	public MRTFacadeBusinessService getMrtFacadeBusinessService();

	public List<Office> retrieveFLPOfficeMRTBusinessObjectList(String stateAbbr)
			throws DLSBusinessStopException;

	public List<State> retrieveFLPStateList() throws DLSBusinessStopException;

	public List<MrtLookUpBO> retrieveFSAStateList(
			RetrieveFSAStateListBC contract) throws DLSBusinessFatalException;
	
	public List<MrtLookUpBO> retrieveFSAOneCMStateList(
			RetrieveFSAStateListBC contract) throws DLSBusinessFatalException;


	public double retrieveInterestRateForAssistanceType(
			RetrieveInterestRateForAssistanceTypeBC contract);

	public List<MrtLookUpBO> retrieveStateList(RetrieveMRTStateListBC contract)
			throws DLSBusinessFatalException;

	public List<MrtLookUpBO> retrieveServiceCenterList(
			RetrieveMRTServiceCenterListBC contract)
			throws DLSBusinessFatalException;

	public List<MrtLookUpBO> retrieveCountyList(RetrieveMRTCountyListBC contract)
			throws DLSBusinessFatalException;

	public Map<String, String> getFSAStateCountyOfficesFromFLPCodes( List<String> flpOfficeCodes);
		
	public String getFSAStateCountyCodeFromStateLocationAreaFLPCode(
			String office_flp_code_str) throws DLSBusinessFatalException;

	public List<LocationArea> retrieveFLPOfficeCodeListForScimsCustomer(
			List<String> fsaStAndLocArCodes, AgencyToken token)
			throws DLSBusinessFatalException;

	public List<LocationArea> retrieveFlpLocationAreaCodesByServiceCenterOffices(
			String[] serviceCenters) throws DLSBusinessFatalException;

	public List<Office> retrieveFLPOfficeLocArXREFMRTBOListForScimsCustomer(
			List<LocationArea> flpOfficeList, AgencyToken token)
			throws DLSBusinessFatalException;

	public Map<String, Office> retriveFLPOfficesByOfficeFLPCdMap(
			List<String> flpStringCodes) throws BusinessServiceBindingException;

	public List<Office> retrieveOfficesByFlpCodes(List<String> flpStringCodes)
			throws BusinessServiceBindingException;

	public List<Office> retrieveFSAOfficeListByOfficeIdentifierList(
			List<Integer> flpIdCodes) throws BusinessServiceBindingException;

	public List<MrtLookUpBO> retrieveFSACountyList(
			RetrieveFsaCountyListBC contract) throws DLSBusinessFatalException;

	/**
	 * Adding new interface methods to support dls-impl in dls-ls
	 */
	public InterestRate retrieveInterestRate(Integer typeId, Date date)
			throws DLSBusinessFatalException;

	public List<InterestRate> retrieveInterestRates(Integer[] typeIds, Date date)
			throws DLSBusinessFatalException;

	public BusinessPartyRole lookupUser(String eAuthID)
			throws DLSBusinessFatalException;

	public String lookupUserEmployeeCamsId(String eAuthID)
			throws DLSBusinessFatalException;
	
	public List<Office> retrieveFlpStateOffices()
			throws DLSBusinessFatalException;// allFlpStateOffices
/**
 * Lighter version. Only load state name, abbreviation, office code and state flp code
 */
	public List<Office> retrieveFlpStateOffices_light()
		throws DLSBusinessFatalException;
	
	public List<Office> retrieveFlpServiceCentersByStateOffices(
			String[] stateOffices) throws DLSBusinessFatalException; // fsaFlpServiceCentreOfficesByOfficeFlpCodeList

	public List<Office> retrieveFlpServiceCentersByStateOffices(
			List<String> stateOffices) throws InvalidBusinessContractDataException, DLSBusinessFatalException;
/**
 * Lighter version Only load officeCode, CuontyFlpCode and CountyName
 */
	public List<Office> retrieveFlpServiceCentersByStateOffices_light(
			String[] stateOffices) throws DLSBusinessFatalException; 
	
	public List<Office> retrieveFlpServiceCentersByStateAbbr(String stateAbbr)
			throws DLSBusinessFatalException;

	/**
	 * added to support SS
	 */
	public State retrieveStateByFlpFipsCode(String flpFipsCode)
			throws DLSBusinessFatalException;

	/**
	 * Added to support CM
	 */
	public List<Office> retrieveFsaFlpServiceCenterOfficesByStateAbbr(
			String stateAbbr) throws DLSBusinessFatalException;

	public List<LocationArea> retrieveFlpAreaListByStateAbbr(String abbr)
			throws DLSBusinessFatalException;

	/**
	 * Added to support FLPRA
	 */
	public List<EmployeeOrgChart> retrieveOrgChartsByEmployeeId(
			String employeeId) throws DLSBusinessFatalException;

	public List<LocationArea> flpByFlpCodeList(String[] flpCds)
			throws DLSBusinessFatalException;

/**
 * Lighter version: only load code, county name and short name
 */
	public List<LocationArea> flpByFlpCodeList_light(String[] flpCds)
		throws DLSBusinessFatalException;
	
	public Map<String, MrtLookUpBO> retrieveFSAStateMap(
			RetrieveFSAStateListBC contract) throws DLSBusinessFatalException;

	public List<gov.usda.fsa.citso.cbs.dto.InterestRate> retrieveIntRatesByRateTypeIdLstAndDtRng(
			List<String> typeIds, Date fromDate, Date toDate)
			throws DLSBusinessFatalException;

	List<LocationArea> retrieveFSALocationAreasByFSAStateCode(String stateFsaCode)
              throws DLSBusinessFatalException;

	List<Office> retrieveFSAOfficeListByFSAStateCodeAndFSALocationAreaCode(String stateFsaCode,
                                                                           String locAreaFsaCode)
              throws DLSBusinessFatalException;
	
	public BusinessPartyDataServiceProxy getBusinessPartyDataService();
	public EmployeeDataServiceProxy getEmployeeDataServiceProxy();
	public CountyDataServiceProxy getCountyDataServiceProxy();
	public StateDataServiceProxy getStateDataServiceProxy();
	public List<State> retrieveFSAStates() throws DLSBusinessStopException;
	public List<gov.usda.fsa.citso.cbs.dto.County> retrieveCountyByStateFipsCode(String stateFipsCode) throws DLSBusinessStopException;
	public List<gov.usda.fsa.citso.cbs.dto.County> retrieveCountyByStateAbbrev(String stateAbbrev) throws DLSBusinessStopException;
	
	
	public EmployeeData retrieveEmployeeData(AgencyToken token, String agencyCode);
	public EmployeeData retrieveEmployeeData(String employeeID, String agencyCode);
	public Office retrieveIcammsEmployeeOffice(Integer officeId);
	public String retrieveEmployeeEmail(String agencyCodeStr , String employeeIdStr);
	
	public List<FipsOfficeLocationAreaBO> retrieveFipsServiceCenterAndStateLocationAreaListByStateAbbr(String stateAbbr);
	public List<FlpOfficeLocationAreaBO> retrieveFlpServiceCenterAndStateLocationAreaListByStateAbbr(String stateAbbr) ;
	public List<LocationArea> retrieveFlpLocationAreasByFipsLocationAreaCode(
			String fipsCode);

	public Map<String,Set<String>> retrieveFlpCodesByFsaCodeMapForSCIMSCustomer(
			List<String> fsaStAndLocArCodes, AgencyToken token) throws DLSBusinessFatalException;
	
	public OfficeInfo retrieveFsaOfficeForScimsLandingFromUserEasOfficeCodes(List<String> officeCodeList);

	public Calendar retrieveCalendarByDate(Date todayDate) throws DLSBusinessFatalException;
	
	public OfficeDataServiceProxy getFlpOfficeMRTBusinessService();

}