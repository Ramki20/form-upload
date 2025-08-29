package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.CancelESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.FindTransactionByTransactionIdBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.SubmitESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.client.ESCOAPTransactionServiceFactory;
import gov.usda.fsa.afao.escoap.sharedservice.model.ESCOAPTransaction;
import gov.usda.fsa.afao.escoap.sharedservice.model.ESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.model.FindESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.service.ESCOAPTransactionService;
import gov.usda.fsa.afao.escoap.sharedservice.util.exception.ESCOAPException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.ESCOAPTransactionBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.NRRSReceivableBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCancelTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransaction;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPRetrieveTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ESCOAPTransactionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * ESCOAPTransactionBS 
 * <br><br>
 * Encapsulates all the implementation to create or Delete Transaction/Collection in ESCOAP system.
 * 
 * @author Naresh.Gotoor, partha.chowdhury
 * @version 03/04/2014, 05/05/2014 - added isHealthy method and made sure to call DLSAgencyTokenFactory to convert the 
 *  DLSAgencyToken to agencyToken
 */
public class ESCOAPTransactionBS implements IESCOAPTransactionBS
{
	private static final Logger logger = LogManager.getLogger(ESCOAPTransactionBS.class);
	private  static final String ESCOAP_SERVICE_JNDI = "gov/usda/fsa/common/escoapsharedservice_url";

	/**
	 * This service Creates Transaction/Collection in ESCOAP.
	 * 
	 * @param eSCOAPTransactionBC - contract to create Transaction/Collection in ESCOAP.
	 * @return ESCOAPTransactionResponseBO - ESCOAP response
	 * @throws DLSBusinessServiceException - It is thrown if ESCOAP fails to create Transaction.
	 * @throws DLSBCInvalidDataStopException - It is thrown when the invalid contract data is passed.
	 */
	public ESCOAPTransactionResponseBO createTransaction(ESCOAPCreateTransactionBC eSCOAPTransactionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("createTransaction.eSCOAPTransactionBC." + eSCOAPTransactionBC);
		}
		ESCOAPTransactionResponseBO response = new ESCOAPTransactionResponseBO();

		// Validate Contract
		ESCOAPTransactionBCValidator.getInstance().validateESCOAPCreateTransactionBC(eSCOAPTransactionBC);

		// Populate ESCOAP Transaction List		
		List<ESCOAPTransaction> escoapTransactions = populateESCOAPTransaction(eSCOAPTransactionBC);
		
		try
		{
			// get agency token from DLSAgencyTokenFactory
			AgencyToken agencyToken = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(eSCOAPTransactionBC.getAgencyToken());
			
			// Create ESCOAP Contract
			SubmitESCOAPTransactionBusinessContract contract = new SubmitESCOAPTransactionBusinessContract(
					escoapTransactions, agencyToken);
			
			// Create Service
			ESCOAPTransactionService escoapService = ESCOAPTransactionServiceFactory.createService(agencyToken, ESCOAP_SERVICE_JNDI);

			// Call ESCOAP Service
			ESCOAPTransactionResponse escoapResponse = escoapService.submitESCOAPTransaction(contract);

			populateESCOAPTransactionResponse(escoapResponse, response);
			Map<String, String> errorMessages = escoapResponse.getEscoapErrors();
			readErrors(errorMessages);
			
			if (logger.isDebugEnabled())
			{
				logger.debug("createTransaction.eSCOAPTransactionBC.end.");
			}

		}
		catch (ESCOAPException e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		catch (DLSBCInvalidDataStopException ex){
			throw ex;
		}
		catch (Throwable e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}

		return response;
	}
	
	/**
	 * This service Delete/Cancel  Transaction/Collection in ESCOAP.
	 * 
	 * @param eSCOAPCancelTransactionBC - contract to cancel Transaction/Collection in ESCOAP.
	 * @return ESCOAPTransactionResponseBO  - ESCOAP response
	 * @throws DLSBusinessServiceException - It is thrown if ESCOAP fails to create Transaction.
	 * @throws DLSBCInvalidDataStopException - It is thrown when the invalid contract data is passed.
	 */
	public ESCOAPTransactionResponseBO cancelESCOAPTransaction(ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("cancelESCOAPTransaction.eSCOAPCancelTransactionBC." + eSCOAPCancelTransactionBC);
		}
		
		ESCOAPTransactionResponseBO response = new ESCOAPTransactionResponseBO();
		// Validate Contract
		ESCOAPTransactionBCValidator.getInstance().validateESCOAPCancelTransactionBC(eSCOAPCancelTransactionBC);
		
		CancelESCOAPTransactionBusinessContract contract = populateCancelESCOAPTransactionBusinessContract(eSCOAPCancelTransactionBC);
		
		try
		{
			// get agency token from DLSAgencyTokenFactory
			AgencyToken agencyToken = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(eSCOAPCancelTransactionBC.getAgencyToken());
			
			// Create Service
			ESCOAPTransactionService escoapService = ESCOAPTransactionServiceFactory.createService(agencyToken, ESCOAP_SERVICE_JNDI);

			// Call ESCOAP Service
			ESCOAPTransactionResponse escoapResponse = escoapService.cancelESCOAPTransaction(contract);

			populateESCOAPTransactionResponse(escoapResponse, response);
			Map<String, String> errorMessages = escoapResponse.getEscoapErrors();
			readErrors(errorMessages);
			
			if (logger.isDebugEnabled())
			{
				logger.debug("cancelESCOAPTransaction.eSCOAPCancelTransactionBC.end.");
			}

		}
		catch (ESCOAPException e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		catch (DLSBCInvalidDataStopException ex){
			throw ex;
		}
		catch (Throwable e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		return response;
	}

	private CancelESCOAPTransactionBusinessContract populateCancelESCOAPTransactionBusinessContract(
			ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC)
	{
		return  new CancelESCOAPTransactionBusinessContract(eSCOAPCancelTransactionBC.getConfirmationNumberList(), 
								eSCOAPCancelTransactionBC.getApplicationSystemCode(), eSCOAPCancelTransactionBC.getAgencyToken());
		
	}

	/**
	 * This service Retrieves Transaction/Collection from ESCOAP.
	 * 
	 * @param eSCOAPRetrieveTransactionBC - contract to retrieve Transaction/Collection from ESCOAP.
	 * @return ESCOAPTransactionResponseBO - ESCOAP response
	 * @throws DLSBusinessServiceException - It is thrown if ESCAOP fails to retrieve Transaction.
	 * @throws DLSBCInvalidDataStopException - It is thrown when the invalid contract data is passed.
	 */
	public ESCOAPTransactionResponseBO retrieveTransaction(ESCOAPRetrieveTransactionBC eSCOAPRetrieveTransactionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException
	{
		
		if (logger.isDebugEnabled())
		{
			logger.debug("retrieveTransaction.eSCOAPRetrieveTransactionBC." + eSCOAPRetrieveTransactionBC);
		}
		
		ESCOAPTransactionResponseBO response = new ESCOAPTransactionResponseBO();
		
		// Validate Contract
		ESCOAPTransactionBCValidator.getInstance().validateESCOAPRetrieveTransactionBC(eSCOAPRetrieveTransactionBC);
		FindTransactionByTransactionIdBusinessContract contract = populateFindTransactionByTransactionIdBusinessContract(eSCOAPRetrieveTransactionBC);
		
		try
		{
			// get agency token from DLSAgencyTokenFactory
			AgencyToken agencyToken = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(eSCOAPRetrieveTransactionBC
					.getAgencyToken());
			
			// Create Service
			ESCOAPTransactionService escoapService = ESCOAPTransactionServiceFactory.createService(agencyToken, ESCOAP_SERVICE_JNDI);

			// Call ESCOAP Service
			FindESCOAPTransactionResponse escoapResponse = escoapService.findESCOAPTransactionByTransactionId(contract);

			populateFindESCOAPTransactionResponse(escoapResponse, response);
			Map<String, String> errorMessages = escoapResponse.getEscoapErrors();
			readErrors(errorMessages);
			
			if (logger.isDebugEnabled())
			{
				logger.debug("retrieveTransaction.eSCOAPRetrieveTransactionBC.end.");
			}
		}
		catch (ESCOAPException e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		catch (DLSBCInvalidDataStopException ex){
			throw ex;
		}
		catch (Throwable e)
		{
			DLSBusinessServiceException ex = new DLSBusinessServiceException(e.getMessage(), e);
			Map<String,String> errors = new  HashMap<String,String>();
			errors.put("error.external.system.unavailable", e.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		
		return response;
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
	public Boolean isHealthy(AgencyToken agencyToken) throws DLSBCInvalidDataStopException
	{
		Boolean result;
		
		if(!NRRSReceivableBCValidator.validateAgencyToken(agencyToken, true))
		{
			throw new DLSBCInvalidDataStopException("Invalid Token passed");
		}
		
		// get agency token from DLSAgencyTokenFactory
		AgencyToken token = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(agencyToken);
		
		// Create Service
		ESCOAPTransactionService escoapService = ESCOAPTransactionServiceFactory.createService(token, ESCOAP_SERVICE_JNDI);
		result = escoapService.isHealthy();
	
		return result;
	}
	
	/**
	 * Populate FindESCOAPTransactionResponse
	 * 
	 * @param escoapResponse
	 * @param response
	 */
	private void populateFindESCOAPTransactionResponse(FindESCOAPTransactionResponse escoapResponse,
			ESCOAPTransactionResponseBO response)
	{
		List<Long> confirmationNumbers = new ArrayList<Long>();
		confirmationNumbers.add(escoapResponse.getConfirmationNumber());
		response.setAccountingTransactionDate(escoapResponse.getAccountingTransactionDate());
		response.setCancelConfirmationNumber(escoapResponse.getCancelConfirmationNumber());
		response.setConfirmationNumberList(confirmationNumbers);
		response.setSuccessful(escoapResponse.isSuccessful());
		response.setTransactionRequestIdentifier(escoapResponse.getTransactionRequestId());
		response.setTransactionStatusCode(escoapResponse.getTransactionStatusCode());
		
	}

	/**
	 * Populate ESCOAPRetrieveTransactionBC
	 * 
	 * @param eSCOAPRetrieveTransactionBC
	 * @return
	 */
	private FindTransactionByTransactionIdBusinessContract populateFindTransactionByTransactionIdBusinessContract(
			ESCOAPRetrieveTransactionBC eSCOAPRetrieveTransactionBC)
	{
		
		return new FindTransactionByTransactionIdBusinessContract(eSCOAPRetrieveTransactionBC.getTransactionRequestIdentifier(), eSCOAPRetrieveTransactionBC.getAccountingProgramYear(),
				eSCOAPRetrieveTransactionBC.getApplicationSystemCode(), eSCOAPRetrieveTransactionBC.getAgencyToken());
	}

	/**
	 * Populate ESCOAPTransactionResponse
	 * 
	 * @param escoapResponse
	 * @param response
	 */
	private void populateESCOAPTransactionResponse(ESCOAPTransactionResponse escoapResponse,
			ESCOAPTransactionResponseBO response)
	{
		response.setConfirmationNumberList(escoapResponse.getConfirmationNumberList());
		response.setSuccessful(escoapResponse.isSuccessful());
		response.setEscoapErrors(escoapResponse.getEscoapErrors());
	}

	/**
	 * Populate ESCOAPTransaction
	 * 
	 * @param escoapTransaction
	 */
	private List<ESCOAPTransaction> populateESCOAPTransaction(ESCOAPCreateTransactionBC escoapCreateTransactionBC)
	{
		
		List<ESCOAPTransaction> escoapTransactions = new ArrayList<ESCOAPTransaction>();
		List<ESCOAPCreateTransaction> escoapTransactionList = escoapCreateTransactionBC.getEscoapTransactionList();
		for (ESCOAPCreateTransaction escoapCreateTransaction : escoapTransactionList)
		{
			ESCOAPTransaction escoapTransaction = new ESCOAPTransaction();
			escoapTransaction.setSystemCode(escoapCreateTransaction.getApplicationSystemCode());
			escoapTransaction.setTransactionRequestId(escoapCreateTransaction.getTransactionRequestId());
			escoapTransaction.setBudgetFiscalYear(escoapCreateTransaction.getBudgetFiscalYear());
			escoapTransaction.setStateFSACode(escoapCreateTransaction.getStateFSACode());
			escoapTransaction.setCountyFSACode(escoapCreateTransaction.getCountyFSACode());
			escoapTransaction.setAccountingTransactionDate(escoapCreateTransaction.getAccountingTransactionDate());
			escoapTransaction.setAccountingProgramTransactionCode(escoapCreateTransaction
					.getAccountingProgramTransactionCode());
			escoapTransaction.setAccountingProgramCode(escoapCreateTransaction.getAccountingProgramCode());
			escoapTransaction.setAccountingProgramYear(escoapCreateTransaction.getAccountingProgramYear()); // can be spaces																												
			escoapTransaction.setCommodityCode(escoapCreateTransaction.getCommodityCode()); // can be spaces
			escoapTransaction.setTransactionAmount(escoapCreateTransaction.getTransactionAmount());
			escoapTransaction.setTransactionQuantity(escoapCreateTransaction.getTransactionQuantity());
			escoapTransaction.setReversalIndicator(escoapCreateTransaction.getReversalIndicator());// R is other allowed value																									
			escoapTransaction.setBusinessPartyIdentification(escoapCreateTransaction.getBusinessPartyIdentification());
			escoapTransaction.setDataSourceAcronym(escoapCreateTransaction.getDataSourceAcronym());
			escoapTransaction.setCustomerName(escoapCreateTransaction.getCustomerName());
			escoapTransaction.setBusinessTypeCode(escoapCreateTransaction.getBusinessTypeCode());
			escoapTransaction.setAccountingReferenceOneCode(escoapCreateTransaction.getAccountingReferenceOneCode());
			escoapTransaction.setAccountingReferenceOneNumber(escoapCreateTransaction.getAccountingReferenceOneNumber());
			escoapTransaction.setAccountingReferenceTwoCode(escoapCreateTransaction.getAccountingReferenceTwoCode());
			escoapTransaction.setAccountingReferenceTwoNumber(escoapCreateTransaction.getAccountingReferenceTwoNumber());
			escoapTransaction.setAccountingReferenceThreeCode(escoapCreateTransaction.getAccountingReferenceThreeCode());
			escoapTransaction.setAccountingReferenceThreeNumber(escoapCreateTransaction
					.getAccountingReferenceThreeNumber());
			escoapTransaction.setAccountingReferenceFourCode(escoapCreateTransaction.getAccountingReferenceFourCode());
			escoapTransaction.setAccountingReferenceFourNumber(escoapCreateTransaction.getAccountingReferenceFourNumber());
			escoapTransaction.setAccountingReferenceFiveCode(escoapCreateTransaction.getAccountingReferenceFiveCode());
			escoapTransaction.setAccountingReferenceFiveNumber(escoapCreateTransaction.getAccountingReferenceFiveNumber());
			escoapTransaction.setLegacyTransactionRequestId(escoapCreateTransaction.getLegacyTransactionRequestId());
			escoapTransaction.setDirectAttributionGroupingKeyNumber(escoapCreateTransaction
					.getDirectAttributionGroupingKeyNumber());
			escoapTransaction.setLoanDeductionCommodityCount(escoapCreateTransaction.getLoanDeductionCommodityCount());
			escoapTransaction.setLoanDeductionAmount(escoapCreateTransaction.getLoanDeductionAmount());
			escoapTransaction.setProducerCount(escoapCreateTransaction.getProducerCount());
			escoapTransaction.setGinMillCode(escoapCreateTransaction.getGinMillCode());
			escoapTransaction.setReserveProgram0XXIndicator(escoapCreateTransaction.getReserveProgram0XXIndicator());
			escoapTransaction.setLoanTypeCode(escoapCreateTransaction.getLoanTypeCode());
			if(escoapCreateTransaction.getObligationConfirmationNumber() != null) {
				escoapTransaction.setObligationID(escoapCreateTransaction.getObligationConfirmationNumber().intValue());
			}
			escoapTransactions.add(escoapTransaction);
		}
		
		return escoapTransactions;		
	}

	/**
	 * Read errors from Transaction Response
	 * 
	 * @param errorMessages
	 * @throws DLSBCInvalidDataStopException
	 */
	private void readErrors(Map<String, String> errorMessages)
			throws DLSBCInvalidDataStopException
	{

		List<Object> msgList = new ArrayList<Object>();
		DLSBCInvalidDataStopException invex = new DLSBCInvalidDataStopException();
		if (errorMessages != null && errorMessages.size() > 0) {
			invex.setExtSystemErrorMap(errorMessages);
		}
		if (errorMessages != null && !errorMessages.isEmpty())
		{

			for (Map.Entry<String, String> entry : errorMessages.entrySet())
			{
				msgList.add(entry.getValue());
			}
		}

		if (msgList.size() > 0)
		{
			invex = new DLSBCInvalidDataStopException(msgList);
			invex.setExtSystemErrorMap(errorMessages);
			throw invex;
		}

	}
}
