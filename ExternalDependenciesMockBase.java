package gov.usda.fsa.fcao.flp.flpids.util;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import gov.usda.fsa.citso.cbs.bs.BusinessPartyDataService;
import gov.usda.fsa.citso.cbs.bs.EmployeeDataService;
import gov.usda.fsa.citso.cbs.bs.InterestRateDataService;
import gov.usda.fsa.citso.cbs.bs.LocationAreaDataService;
import gov.usda.fsa.citso.cbs.bs.OfficeDataService;
import gov.usda.fsa.citso.cbs.bs.StateDataService;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IDisbursementBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IESCOAPTransactionBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IFarmRecordBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.INRRSCollectionBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.INRRSReceivableBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.MRTFacadeBusinessService;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.MRTProxyBS;

public class ExternalDependenciesMockBase {
	protected MRTProxyBS mockMrtProxyBusinessService;	
	protected OfficeDataService mockFlpOfficeMRTBusinessService;
	protected StateDataService mockFlpStateMRTBusinessService;
	@SuppressWarnings("deprecation")
	protected InterestRateDataService mockInterestRateDataMartBusinessService;
	protected LocationAreaDataService mockFlpLocationAreaDataMartBusinessService;
	protected BusinessPartyDataService mockBusinessPartyDataService;
	protected EmployeeDataService mockEmployeeDataServiceProxy;
	protected MRTFacadeBusinessService mocMrtFacadeBusinessService; 
	
	protected IFarmRecordBS farmRecordsBusinessService;
	protected IDisbursementBS disbursementBusinessService;
	protected INRRSReceivableBS receivableBusinessService;
	protected INRRSCollectionBS collectionBusinessService;
	protected IESCOAPTransactionBS legacyTransactionBusinessService;
	protected static List<StateBO> ALL_STATE_LIST = new ArrayList<StateBO>();
	@Before
	public void setUp() throws Exception{
		setMockMrtProxyBusinessService(mock(MRTProxyBS.class));		
		setMockFlpStateMRTBusinessService(mock(StateDataService.class));
		setMockInterestRateDataMartBusinessService(mock(InterestRateDataService.class));
		setMockFlpLocationAreaDataMartBusinessService(mock(LocationAreaDataService.class));
		setMockBusinessPartyDataService(mock(BusinessPartyDataService.class));
		setMockEmployeeDataServiceProxy(mock(EmployeeDataService.class));
		setMocMrtFacadeBusinessService(mock(MRTFacadeBusinessService.class));
		setFarmRecordsBusinessService(mock(IFarmRecordBS.class));
		setDisbursementBusinessService(mock(IDisbursementBS.class));
		setReceivableBusinessService(mock(INRRSReceivableBS.class));
		setCollectionBusinessService(mock(INRRSCollectionBS.class));
		setLegacyTransactionBusinessService(mock(IESCOAPTransactionBS.class));
		
		setMockFlpOfficeMRTBusinessService(mock(OfficeDataService.class));
	}

	public MRTProxyBS getMockMrtProxyBusinessService() {
		return mockMrtProxyBusinessService;
	}

	public void setMockMrtProxyBusinessService(
			MRTProxyBS mockMrtProxyBusinessService) {
		this.mockMrtProxyBusinessService = mockMrtProxyBusinessService;
	}

	public OfficeDataService getMockFlpOfficeMRTBusinessService() {
		return mockFlpOfficeMRTBusinessService;
	}

	public void setMockFlpOfficeMRTBusinessService(
			OfficeDataService mockFlpOfficeMRTBusinessService) {
		this.mockFlpOfficeMRTBusinessService = mockFlpOfficeMRTBusinessService;
	}

	public StateDataService getMockFlpStateMRTBusinessService() {
		return mockFlpStateMRTBusinessService;
	}

	public void setMockFlpStateMRTBusinessService(
			StateDataService mockFlpStateMRTBusinessService) {
		this.mockFlpStateMRTBusinessService = mockFlpStateMRTBusinessService;
	}

	@SuppressWarnings("deprecation")
	public InterestRateDataService getMockInterestRateDataMartBusinessService() {
		return mockInterestRateDataMartBusinessService;
	}

	public void setMockInterestRateDataMartBusinessService(
			@SuppressWarnings("deprecation") InterestRateDataService mockInterestRateDataMartBusinessService) {
		this.mockInterestRateDataMartBusinessService = mockInterestRateDataMartBusinessService;
	}

	public LocationAreaDataService getMockFlpLocationAreaDataMartBusinessService() {
		return mockFlpLocationAreaDataMartBusinessService;
	}

	public void setMockFlpLocationAreaDataMartBusinessService(
			LocationAreaDataService mockFlpLocationAreaDataMartBusinessService) {
		this.mockFlpLocationAreaDataMartBusinessService = mockFlpLocationAreaDataMartBusinessService;
	}

	public BusinessPartyDataService getMockBusinessPartyDataService() {
		return mockBusinessPartyDataService;
	}

	public void setMockBusinessPartyDataService(
			BusinessPartyDataService mockBusinessPartyDataService) {
		this.mockBusinessPartyDataService = mockBusinessPartyDataService;
	}

	public EmployeeDataService getMockEmployeeDataServiceProxy() {
		return mockEmployeeDataServiceProxy;
	}

	public void setMockEmployeeDataServiceProxy(
			EmployeeDataService mockEmployeeDataServiceProxy) {
		this.mockEmployeeDataServiceProxy = mockEmployeeDataServiceProxy;
	}

	public MRTFacadeBusinessService getMocMrtFacadeBusinessService() {
		return mocMrtFacadeBusinessService;
	}

	public void setMocMrtFacadeBusinessService(
			MRTFacadeBusinessService mocMrtFacadeBusinessService) {
		this.mocMrtFacadeBusinessService = mocMrtFacadeBusinessService;
	}

	public IFarmRecordBS getFarmRecordsBusinessService() {
		return farmRecordsBusinessService;
	}

	public void setFarmRecordsBusinessService(
			IFarmRecordBS farmRecordsBusinessService) {
		this.farmRecordsBusinessService = farmRecordsBusinessService;
	}

	public IDisbursementBS getDisbursementBusinessService() {
		return disbursementBusinessService;
	}

	public void setDisbursementBusinessService(
			IDisbursementBS disbursementBusinessService) {
		this.disbursementBusinessService = disbursementBusinessService;
	}

	public INRRSReceivableBS getReceivableBusinessService() {
		return receivableBusinessService;
	}

	public void setReceivableBusinessService(
			INRRSReceivableBS receivableBusinessService) {
		this.receivableBusinessService = receivableBusinessService;
	}

	public INRRSCollectionBS getCollectionBusinessService() {
		return collectionBusinessService;
	}

	public void setCollectionBusinessService(
			INRRSCollectionBS collectionBusinessService) {
		this.collectionBusinessService = collectionBusinessService;
	}

	public IESCOAPTransactionBS getLegacyTransactionBusinessService() {
		return legacyTransactionBusinessService;
	}

	public void setLegacyTransactionBusinessService(
			IESCOAPTransactionBS legacyTransactionBusinessService) {
		this.legacyTransactionBusinessService = legacyTransactionBusinessService;
	}
	
	  public  static List<StateBO> populateStateList(){
	    	if(ALL_STATE_LIST.isEmpty()){
	    		StateBO stateBO = new StateBO("60","AK-Alaska", "AK");
	    		ALL_STATE_LIST.add(stateBO);
	        	        	
	    		stateBO = new StateBO("02","AZ-Arizona", "AZ");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("03","AR-Arkansas", "AR");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("04","CA-California", "CA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("05","CO-Colorado", "CO");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("06","CT-Connecticut", "CT");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("07","DE-Delaware", "DE");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("09","FL-Florida", "FL");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("10","GA-Georgia", "GA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("61","HI-Hawaii", "HI");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("62","HI-Hawaii", "HI");
	    		ALL_STATE_LIST.add(stateBO);
	    	        	
	    		stateBO = new StateBO("12","ID-Idaho", "ID");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("13","IL-Illinois", "IL");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("14","IL-Illinois", "IL");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("15","IN-Indiana", "IN");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("16","IA-Iowa", "IA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("18","KS-Kansas", "KS");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("19","KS-Kansas", "KS");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("20","KY-Kentucky", "KY");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("21","KY-Kentucky", "KY");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("22","LA-Louisiana", "LA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("23","ME-Maine", "ME");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("24","MD-Maryland", "MD");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("25","MA-Massachusetts", "MA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("26","MI-Michigan", "MI");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		
	    		stateBO = new StateBO("27","MN-Minnesota", "MN");
	    		ALL_STATE_LIST.add(stateBO);

	    		stateBO = new StateBO("28","MS-Mississippi", "MS");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("29","MO-Missouri", "MO");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("30","MO-Missouri", "MO");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("31","MT-Montana", "MT");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("32","NE-Nebraska", "NE");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("33","NV-Nevada", "NV");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("34","NH-New Hampshire", "NH");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("35","NJ-New Jersey", "NJ");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("36","NM-New Mexico", "NM");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("37","NY-New York", "NY");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("38","NC-North Carolina", "NC");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("39","NC-North Carolina", "NC");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("40","ND-North Dakota", "ND");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		
	    		stateBO = new StateBO("41","OH-Ohio", "OH");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("42","OK-Oklahoma", "OK");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("43","OR-Oregon", "OR");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("44","PA-Pennsylvania", "PA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("63","PR-Puerto Rico", "PR");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("45","RI-Rhode Island", "RI");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("46","SC-South Carolina", "SC");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("47","SD-South Dakota", "SD");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("48","TN-Tennessee", "TN");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("49","TX-Texas", "TX");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("50","TX-Texas", "TX");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("51","TX-Texas", "TX");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		stateBO = new StateBO("64","VI-U.S. Virgin Islands", "VI");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("52","UT-Utah", "UT");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("53","VT-Vermont", "VT");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("54","VA-Virginia", "VA");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("55","VA-Virginia", "VA");
	    		ALL_STATE_LIST.add(stateBO);
	    		
	    		
	    		stateBO = new StateBO("56","WA-Washington", "WA");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("57","WV-West Virginia", "WV");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("58","WI-Wisconsin", "WI");
	    		ALL_STATE_LIST.add(stateBO);
	    		stateBO = new StateBO("59","WY-Wyoming", "WY");
	    		ALL_STATE_LIST.add(stateBO);	    		
	    	}
	    	return ALL_STATE_LIST;
	    }
}
