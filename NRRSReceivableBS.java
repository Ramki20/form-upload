package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.NRRSReceivableBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSReceivableBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.NRRSReceivableResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalCreateReceivableContract;
import gov.usda.fsa.nrrs.services.client.NRRSServiceFactory;
import gov.usda.fsa.nrrs.services.client.NRRSServiceProxy;
import gov.usda.fsa.nrrs.servicewrapper.exception.NRRSServiceException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
 
/**
 * NRRSReceivableBS
 * <br><br>
 * Encapsulates all the implementation to create Receivable in NRRS system.
 * 
 * @author Naresh.Gotoor
 * @version 12/10/2013
 * @version 05/23/2014 theodore.jeschke
 * 						Set InitialNotificationLetterDate, ProgramPrincipalInterest, ProgramInterestAmount
 * @version 08/22/2016 Charles Landergott
 * 						 Included parsing of the error code returned from NRRS. The format returned at the time 
 * 						 of creating the parser is Error occurred while invoking NRRS External Service via web service
 * 						 to create Receivables: Error(s) returned from web service call to create Receivables: 
 * 						 \nContract #1 - error.invalid.token\nContract #1 - error.invalid.obligation.id
 * 
 * 
 */
public class NRRSReceivableBS implements INRRSReceivableBS
{
	private final static String NRRS_EXTERNAL_SERVICE_SPECIFIER = "gov/usda/fsa/fcao/flpids/common/nrrs_external_service_type";
	public static final String ERROR_DELIMITER = "Contract #1 - ";
	
	/**
	 * Create a Receivable in NRRS System.
	 * 
	 * @param  nrrsReceivableBC is a business contract that needs to be processed.
	 * @return NRRSReceivableResponseBO Response from NRRS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException DLSBCInvalidDataStopException Exception thrown by DLS.
	 */
	public NRRSReceivableResponseBO createReceivables(NRRSReceivableBC nrrsReceivableBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException
	{

		NRRSReceivableBCValidator.validate(nrrsReceivableBC);
		
		// get agency token from DLSAgencyTokenFactory
		AgencyToken agencyToken = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(nrrsReceivableBC.getAgencyToken());
		NRRSReceivableResponseBO nrrsReceivableResponseBO = new NRRSReceivableResponseBO();
		
		try
		{
			NRRSServiceProxy proxy = getNRRSExternalServiceProxy(agencyToken);
			List<BigDecimal> receivableIds = proxy.createReceivables(loadContract(agencyToken, nrrsReceivableBC));
			nrrsReceivableResponseBO.setReceivableIds(receivableIds);
		}
		catch (NRRSServiceException nrrsConnex)
		{
			/*
			 * Unable to obtain reference to the Proxy.
			 */
			DLSBusinessServiceException ex = new DLSBusinessServiceException("NRRS Exception: " + nrrsConnex.getMessage(), nrrsConnex);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", nrrsConnex.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		catch (AgencyException nrrsConnex)
		{
			parseErrors(nrrsConnex);
			DLSBusinessServiceException ex = new DLSBusinessServiceException("NRRS Exception: " + nrrsConnex.getMessage(), nrrsConnex);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", nrrsConnex.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		catch (Throwable nrrsConnex)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException("NRRS Exception: " + nrrsConnex.getMessage(), nrrsConnex);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.failure", nrrsConnex.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		return nrrsReceivableResponseBO;
	}

	private void parseErrors(AgencyException nrrsConnex) throws DLSBCInvalidDataStopException {
		if ((nrrsConnex.getMessage().contains("Contract")) || !(nrrsConnex.getMessage().contains("The service cannot be found "))) {
			DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
			Map<String,String> errors =  new HashMap<String,String>();
			String[] tokens = StringUtils.tokenizeToStringArray(nrrsConnex.getMessage(), ":");
			for (String token : tokens) {
				if (token.contains("Contract")) {
					String[] errorTokens = StringUtils.tokenizeToStringArray(token, "\n");
					for (String error : errorTokens) {
						String[] errorKeys = StringUtils.tokenizeToStringArray(error, "-");
						for (String key : errorKeys) {
							if (!key.contains("Contract")) {
								errors.put(key.trim(), ""); 
							}
						}
					}
				}
				else {
					String[] errorTokens = StringUtils.tokenizeToStringArray(token, "\n");
					for (String error : errorTokens) {
						String[] errorValue = StringUtils.tokenizeToStringArray(error, "-");
						for (String value : errorValue) {
							errors.put("", value.toString());							
						}
					} 
				}
				
			}
			ex.setExtSystemErrorMap(errors);
			throw ex;		
		}
	}
	
	private List<ExternalCreateReceivableContract> loadContract(AgencyToken agencyToken, NRRSReceivableBC nrrsReceivableBC) {
		// create contract(s) and populate
		ExternalCreateReceivableContract contract = new ExternalCreateReceivableContract(agencyToken);
		contract.setProgramCode(nrrsReceivableBC.getProgramCode());
		
		contract.setProgramYear(nrrsReceivableBC.getProgramYear());//optional
		contract.setProgramPrincipalAmount(nrrsReceivableBC.getProgramPrincipalAmount());
		
		contract.setProgramChargesAmount(BigDecimal.ZERO);
		contract.setDebtDiscoveryCode(nrrsReceivableBC.getDebtDiscoveryCode()); 
		contract.setDebtReasonCode(nrrsReceivableBC.getDebtReasonCode()); 
		contract.setOrigStateCode(nrrsReceivableBC.getOrigStateCode());
		contract.setOrigCountyCode(nrrsReceivableBC.getOrigCountyCode());
		contract.setBudgetFiscalYear(nrrsReceivableBC.getBudgetFiscalYear());
		contract.setDateOfIndebtedness(nrrsReceivableBC.getDateOfIndebtedness());			
		contract.setUri(nrrsReceivableBC.getUri());	
		
		contract.setReferenceFields(nrrsReceivableBC.getReferenceFields());
		contract.setCustomerSourceSystemCode(nrrsReceivableBC.getCustomerSourceSystemCode());
		
		contract.setCustomers(nrrsReceivableBC.getCustomers());
		contract.setInitialNotificationLetterDate(nrrsReceivableBC.getInitialNotificationLetterDate());
		contract.setProgramPrincipalInterest(nrrsReceivableBC.getProgramInterestRate());
		
		// set the decimal value only when it is greater than 0.00 per NRRS specification
		if (nrrsReceivableBC.getProgramInterestAmount()!= null && 
				nrrsReceivableBC.getProgramInterestAmount().compareTo(BigDecimal.ZERO) == 1)
		{
			contract.setProgramInterestAmount(nrrsReceivableBC.getProgramInterestAmount());
		}
		
		contract.setPayableId(nrrsReceivableBC.getPayableId());
		contract.setCommodityCode(nrrsReceivableBC.getCommodityCode());
		contract.setObligationId(nrrsReceivableBC.getObligationConfirmationNumber().toString());
		
		List<ExternalCreateReceivableContract> contractList = new ArrayList<ExternalCreateReceivableContract>();
		contractList.add(contract);
		
		return contractList;
	}
	
	/**
	 * Is external service is healthy or not
	 * 
	 * @param  agencyToken
	 * @return Boolean
	 * 
	 * @throws DLSBCInvalidDataStopException 
	 * @throws DLSBusinessServiceException
	 */
	public Boolean isHealthy(AgencyToken agencyToken) throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		NRRSServiceProxy proxy;
		Boolean result;
		
		if(!NRRSReceivableBCValidator.validateAgencyToken(agencyToken, true))
		{
			throw new DLSBCInvalidDataStopException("Invalid Token passed");
		}
		
		// get agency token from DLSAgencyTokenFactory
		AgencyToken token = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(agencyToken);
		
		try
		{
			proxy = getNRRSExternalServiceProxy(token);
			
			result = proxy.isHealthy();
		}
		catch (NRRSServiceException nrrsConnex)
		{
			throw new DLSBusinessServiceException("NRRS Exception: " + nrrsConnex.getMessage(), nrrsConnex);
		}
		
		return result;
	}
	
	/**
	 * get NRRSServiceProxy based on JNDI type i.e. WebService or EJB Service.
	 * 
	 * @param agencyToken
	 * @return NRRSServiceProxy
	 * 
	 * @throws NRRSServiceException 
	 * @throws DLSBCInvalidDataStopException 
	 */
	private NRRSServiceProxy getNRRSExternalServiceProxy(AgencyToken agencyToken) throws NRRSServiceException
	{
		return NRRSServiceFactory.createService(agencyToken, NRRS_EXTERNAL_SERVICE_SPECIFIER);
	}
}