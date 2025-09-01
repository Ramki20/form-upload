package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTNoDataException;

/**
 * 
 * @author ramesh.ponugoti
 * 
 */

public class MRTFacadeBusinessServiceImpl implements MRTFacadeBusinessService {
	private StateDataServiceProxy stateBusinessService;
	private LocationAreaDataServiceProxy locationBusinessService;
	private OfficeDataServiceProxy officeBusinessService;
	
	private static final Logger LOG = LogManager
			.getLogger(MRTFacadeBusinessServiceImpl.class);

	private static List<StateBO> CACHED_ALL_STATE_LIST = new ArrayList<StateBO>();
	private static Map<String, List<LocationArea>> STATE_ABBR_COUNTY_LOCATION_MAP = new HashMap<String, List<LocationArea>>();

	@SuppressWarnings("unchecked")
	public List<StateBO> getStatesList() throws ServiceUnavailableException,
			InvalidBusinessContractDataException {
		if(CACHED_ALL_STATE_LIST.isEmpty()){
			List<State> stateReadFacades = new ArrayList<State>();
			try {
				stateReadFacades = stateBusinessService.allFlp();
				
				for (State stateReadFacade : stateReadFacades) {
					StateBO sbo = new StateBO();
					sbo.setStateCode(stateReadFacade.getCode());
					sbo.setStateName(stateReadFacade.getName());
					if (stateReadFacade.getCode().equals("61")) {
						sbo.setStateAbbr("HI");
					} else if (stateReadFacade.getCode().equals("62")) {
						sbo.setStateAbbr("FM");
					} else {
						sbo.setStateAbbr(stateReadFacade.getAbbreviation());
					}
		
					if (!stateReadFacade.getCode().equals("62") && !stateReadFacade.getCode().equals("61")) {
						CACHED_ALL_STATE_LIST.add(sbo);
					} else if (stateReadFacade.getCode().equals("62")
							&& stateReadFacade.getName().equals(
									"Federated States of Micronesia")) {
						CACHED_ALL_STATE_LIST.add(sbo);
					}
					 else if (stateReadFacade.getCode().equals("61")
								&& stateReadFacade.getName().equals(
										"Hawaii")) {
						 CACHED_ALL_STATE_LIST.add(sbo);
					}		
				}
				BeanComparator com=new BeanComparator("stateCode");
				Collections.sort(CACHED_ALL_STATE_LIST, com);
				
			} catch (Exception e) {
				LOG.error(e);
				throw new ServiceUnavailableException(
						"Error Retrieving States from MRT: " + e.getMessage());	
			}			
		}		
		return CACHED_ALL_STATE_LIST;
	}

	@SuppressWarnings("unchecked")
	public List<CountyBO> getCountiesList(StateBC stateBC)
			throws ServiceUnavailableException,
			InvalidBusinessContractDataException {		
		stateBC.validate();		
		List<CountyBO> countiesList = new ArrayList<CountyBO>();
		List<LocationArea> countyReadFacades = STATE_ABBR_COUNTY_LOCATION_MAP.get(stateBC.getStateAbbr());
		if(countyReadFacades == null || countyReadFacades.isEmpty()){
			try {
				countyReadFacades = locationBusinessService.flpByStateAbbr(stateBC.getStateAbbr());
				STATE_ABBR_COUNTY_LOCATION_MAP.put(stateBC.getStateAbbr(), countyReadFacades);
			} catch (Exception e) {
				LOG.error(e);
				throw new ServiceUnavailableException(
						"Error Retrieving Counties from MRT: " + e.getMessage());
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("State Abbreivation" + stateBC.getStateAbbr());
				LOG.debug("State Code" + stateBC.getStateCode());
			}
		}
		for (LocationArea locationAreaBO : countyReadFacades) {			
			if (locationAreaBO.getStateCode().equals(stateBC.getStateCode())
				//&& (!(locationAreaBO.getCode().equals("000"))) 
					) {
					CountyBO cbo = new CountyBO();
					cbo.setCountyCode(locationAreaBO.getCode());
					cbo.setCountyName(locationAreaBO.getName());
					countiesList.add(cbo);
			}
		}
		BeanComparator com=new BeanComparator("countyCode");
		Collections.sort(countiesList, com);
		return countiesList;

	}

	public List<MailCodeBO> getMailCodesList(StateBC stateBC)
			throws ServiceUnavailableException,
			InvalidBusinessContractDataException {
		stateBC.validate();
		List<MailCodeBO> mailCodesList = new ArrayList<MailCodeBO>();		
		List<Office> serviceCenterReadFacades = new ArrayList<Office>();
		try {
			serviceCenterReadFacades = officeBusinessService
					.fsaFlpServiceCenterOfficesByStateAbbr(stateBC.getStateAbbr());
		} catch (Exception e) {
			LOG.error(e);
			throw new ServiceUnavailableException(
					"Error Retrieving Mail Codes from MRT: " + e.getMessage());
		}
		for (Office serviceCenter : serviceCenterReadFacades) {
			MailCodeBO mbo = new MailCodeBO();
			mbo.setMailCode(serviceCenter.getOfficeCode().trim());
			mbo.setCountyName(serviceCenter.getCountyName());
			mbo.setCityName(serviceCenter.getLocCityName());
			mailCodesList.add(mbo);
		}
		return mailCodesList;
	}

	public String getMailCode(StateLocationAreaCodeBC contract)
			throws ServiceUnavailableException,
			InvalidBusinessContractDataException,
			MRTNoDataException{
		if(LOG.isDebugEnabled()){
			LOG.debug("------------>getMailCode");
		}
		contract.validateContents();
		
		List<String> stateLocationAreaCodes = new ArrayList<String>();
		stateLocationAreaCodes.add(contract.getStateLocationAreaCode());
		
		List<Office> serviceCenterReadFacades = 
			new ArrayList<Office>();
		try {
			String[] stateLocationAreaCodesArray = stateLocationAreaCodes.toArray(new String[0]);
			
			serviceCenterReadFacades = officeBusinessService
					.flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray);
			
		} catch (Exception e) {
			LOG.error(e);
			throw new ServiceUnavailableException(
					"Error Retrieving Mail Code from MRT: " + e.getMessage());
		}
		if(serviceCenterReadFacades.size() == 0){
			throw new MRTNoDataException("No MailCode found for Input: " + 
					contract.getStateLocationAreaCode());
		}
		Office office = serviceCenterReadFacades
				.get(0);
		if(LOG.isDebugEnabled()){
			LOG.debug("<------------getMailCode");
			LOG.debug("Mail Code----------" + office.getOfficeCode());
		}
		return office.getOfficeCode().trim();
	}

	public void setStateBusinessService(
			StateDataServiceProxy stateBusinessService) {
		this.stateBusinessService = stateBusinessService;
	}

	public void setLocationBusinessService(
			LocationAreaDataServiceProxy locationBusinessService) {
		this.locationBusinessService = locationBusinessService;
	}

	public void setOfficeBusinessService(
			OfficeDataServiceProxy officeBusinessService) {
		this.officeBusinessService = officeBusinessService;
	}
	
}
