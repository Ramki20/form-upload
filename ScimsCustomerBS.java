package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ParseScimsResultBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByTaxIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CustomerScimsVO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.SCIMSClientProxy;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.SCIMSErrorMessages;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ScimsBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ScimsQueryParser;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.XMLParsingException;

public class ScimsCustomerBS implements SCIMSErrorMessages, IScimsCustomerBS {
	private static final Logger logger = LogManager
			.getLogger(ScimsCustomerBS.class);
		
	@Autowired
	private SCIMSClientProxy scimsClientProxy;
	
	public List<ScimsCustomerBO> retrieveScimsCustomersByTaxId(
			RetrieveScimsCustomersByTaxIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException {

		if ((retrieveBC.getTaxIds() == null) || (retrieveBC.getTaxIds().isEmpty())) {

			return new ArrayList<ScimsCustomerBO>();
		}

		List<String> taxIdAndTypes = new ArrayList<String>();

		List<String> taxIds = retrieveBC.getTaxIds();
		List<String> taxTypes = retrieveBC.getTaxIdTypes();

		if (taxTypes != null && !taxTypes.isEmpty()) {
			for (int i = 0; i < taxIds.size(); i++) {
			
				String taxId = (String) taxIds.get(i);
				String taxType = (String) taxTypes.get(i);
				String value = taxId + taxType;

				taxIdAndTypes.add(value);

			}
		} else {

			taxIdAndTypes = taxIds;
		}

		return scimsClientProxy.getCustomerByTaxIds(retrieveBC.getAgencyToken(), taxIdAndTypes);
				
		
	}
	/**
	 * 
	 */
	public List<ScimsCustomerBO> retrieveScimsCustomersByCoreCustomerId(
			RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException {
		if ((retrieveBC.getCoreCustomerIds() == null) || (retrieveBC.getCoreCustomerIds().isEmpty())) {
			return new ArrayList<ScimsCustomerBO>();
		}

		return scimsClientProxy.getCustomerByCustomerIds(retrieveBC.getAgencyToken(), retrieveBC.getCoreCustomerIds());
	}
	
	/** 
	 * Use this lite method (for much better performance) when you want to retrieve customer info for multiple CCIDs (in 100s) 
	 * if it satisfies your need - check SCIMSClientProxy.createCustomerSearchOptionLite method
	 */
	public Map<Integer, ScimsCustomerBO> retrieveScimsCustomersLiteMapByCoreCustomerId(
			RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException {
		
		Map<Integer, ScimsCustomerBO> coreCustIDscimsCustomerBOMap = new HashMap<Integer, ScimsCustomerBO>();
		if ((retrieveBC.getCoreCustomerIds() == null) || (retrieveBC.getCoreCustomerIds().isEmpty())) {
			return coreCustIDscimsCustomerBOMap;
		}
		long startTimeInMilliseconds = System.currentTimeMillis();
		List<ScimsCustomerBO>  scimsCustomerBOList = scimsClientProxy.getCustomerByCustomerIdsLite(retrieveBC);
		long executionTimeInMillis = System.currentTimeMillis() - startTimeInMilliseconds;
		float executionTimeInSeconds = (float)executionTimeInMillis/(1000) ;
		logger.info("Total execution time for retrieveScimsCustomersLiteMapByCoreCustomerId() in ms:"+ executionTimeInMillis +" (About "+(executionTimeInSeconds)+" seconds)");
		
		for(ScimsCustomerBO bo : scimsCustomerBOList)
		{
			coreCustIDscimsCustomerBOMap.put(Integer.parseInt(bo.getCustomerID()), bo);	
		}
		return coreCustIDscimsCustomerBOMap;
	}
	
	/**
	 * 
	 */
	public CustomerScimsVO retrieveScimsCustomersByCoreCustomerId(
			final AgencyToken token, final Integer coreCustId	)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException {
		CustomerScimsVO result = new CustomerScimsVO();
		if ((coreCustId == null) || (coreCustId < 0)) {
			return result;
		}

		List<Integer> coreCustomerIdentifierList = new ArrayList<Integer>();
		coreCustomerIdentifierList.add(coreCustId);

		List<ScimsCustomerBO> resultList = scimsClientProxy.getCustomerByCustomerIds(token, coreCustomerIdentifierList);
		result.setCustomerId(coreCustId.toString());
		result.setNumberOfCustomersReturned(Integer.toBinaryString(resultList.size()));

		if (!resultList.isEmpty() && resultList.size() > 0) {
			result.setTaxId(resultList.get(0).getTaxID());
		}
		return result;
	}
	public  ScimsCustomerBO parseScimsResultXMLForOneCustomer(
			ParseScimsResultBC contract) throws SCIMSBusinessStopException,
			SCIMSDataNotFoundException, SCIMSBusinessFatalException {
		List<ScimsCustomerBO> scimsCustomerList = parseScimsResultXML(contract);

		if ((scimsCustomerList == null) || (scimsCustomerList.size() < 1)) {
			throw new SCIMSDataNotFoundException(
					"XML can not correctly parsed. Cannot pass the xml to you as it contains PII....");
		} else {
			if (!((ScimsCustomerBO) scimsCustomerList.get(0))
					.getActiveIndicator()) {
				throw new SCIMSBusinessStopException(SCIMS_INACTIVE_CUSTOMER);
			}
		}

		return scimsCustomerList.get(0);
	}
	
	protected  List<ScimsCustomerBO> parseScimsResultXML(
			ParseScimsResultBC contract) throws SCIMSDataNotFoundException,
			SCIMSBusinessFatalException, SCIMSBusinessStopException {
		ScimsBCValidator.validate(contract);

		List<ScimsCustomerBO> scimsCustomerList = new ArrayList<ScimsCustomerBO>();

		try {
			if(!StringUtil.isEmptyString(contract.getXml())){
				scimsCustomerList = ScimsQueryParser.parseScimsResults(contract
						.getXml(), contract.getAgencyToken());
			}
		} catch (XMLParsingException e) {
			logger.error("Exception while processing xml: ", e);
			throw new SCIMSBusinessFatalException(e.getMessage());
		}

		return scimsCustomerList;
	}

	
	

	protected List<ScimsCustomerBO> constructScimsCustomerList(
			AgencyToken token, String responseXML)
			throws SCIMSDataNotFoundException, SCIMSBusinessFatalException,
			SCIMSBusinessStopException {
		ParseScimsResultBC parseBC = new ParseScimsResultBC(token, responseXML);
		List<ScimsCustomerBO> scimsCustomerList = parseScimsResultXML(parseBC);
		return scimsCustomerList;
	}

	
	private ScimsCustomerBS() {
		logger.info("constructor of ScimsCustomerBS is called..");
		if(getScimsClientProxy() == null) {
			logger.info("scimsClientProxy is null..");
		}
			
	}
	public SCIMSClientProxy getScimsClientProxy() {
		return scimsClientProxy;
	}
	public void setScimsClientProxy(SCIMSClientProxy scimsClientProxy) {
		this.scimsClientProxy = scimsClientProxy;
	}
}
