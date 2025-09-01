package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.NRRSCollectionBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSDeleteCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSRetrieveCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.NRRSCollectionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.PaymentTransactionData;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ECConstants;
import gov.usda.fsa.nrrs.collections.client.NRRSCollectionServiceFactory;
import gov.usda.fsa.nrrs.collections.client.NRRSCollectionServiceProxy;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CollectionServiceCommonResponse;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CreateExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.DeleteExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.GLTransactionData;
import gov.usda.fsa.nrrs.core.biz.contract.collection.NRRSRemittanceType;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * NRRSCollectionBS <br>
 * <br>
 * Encapsulates all the implementation to create/retrieve and delete Collection
 * in NRRS system.
 * 
 * @author Naresh.Gotoor, partha.chowdhury - adding service type and java doc.
 * @version 01/10/2014, 05/05/2014
 * @version 08/17/2016 ajit.engineer - error translation
 */
public class NRRSCollectionBS implements INRRSCollectionBS {
	private static final Logger logger = LogManager
			.getLogger(NRRSCollectionBS.class);
	private final static String NRRS_COLLECTION_SERVICE_SERVICE_SPECIFIER = "gov/usda/fsa/fcao/flpids/common/nrrs_collection_service_type";

	/**
	 * Create a Collection in NRRS System.
	 * 
	 * @param agencyToken
	 * @param nrrsReceivableBC
	 *            is a business contract that needs to be processed.
	 * @return NRRSCollectionResponseBO Response from NRRS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	public NRRSCollectionResponseBO createExternalCollection(
			NRRSCollectionBC nrrsCollectionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {
		if (logger.isDebugEnabled()) {
			logger.debug("START-NRRSCollectionBS.createExternalCollection.nrrsCollectionBC."
					+ nrrsCollectionBC);
		}

		NRRSCollectionResponseBO nrrsCollectionResponseBO = new NRRSCollectionResponseBO();
		NRRSCollectionBCValidator.validateCreateCollection(nrrsCollectionBC);

		// get agency token from DLSAgencyTokenFactory
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(nrrsCollectionBC
						.getAgencyToken());

		try {
			NRRSCollectionServiceProxy proxy = getNRRSCollectionServiceProxy(agencyToken);

			// Create new collection contract and populate
			CreateExternalCollectionContract createContract = new CreateExternalCollectionContract();
			createContract.setToken(agencyToken);
			createContract.setTransactionRequestSourceCode(nrrsCollectionBC
					.getTransactionRequestSourceCode());
			createContract.setTransactionRequestId(nrrsCollectionBC
					.getTransactionRequestId());
			createContract.setCoreCustomerId(nrrsCollectionBC
					.getCoreCustomerId());
			createContract.setCustomerSourceCode(nrrsCollectionBC
					.getCustomerSourceCode());
			createContract.setPrimaryReferenceCode(nrrsCollectionBC
					.getPrimaryAccountingReferenceCode());
			createContract.setPrimaryReferenceNumber(nrrsCollectionBC
					.getPrimaryAccountingReferenceNumber());
			createContract.setCollectionAmount(nrrsCollectionBC
					.getCollectionAmount());
			createContract.setProgramCode(nrrsCollectionBC.getProgramCode());
			createContract.setProgramYear(nrrsCollectionBC.getProgramYear());
			createContract.setCommodityCode("");
			createContract.setStateCode(nrrsCollectionBC.getStateCode());
			createContract.setCountyCode(nrrsCollectionBC.getCountyCode());
			createContract.setAdditionalReferenceCodeNumberMap(nrrsCollectionBC
					.getAdditionalReferenceCodeInfo());

			List<GLTransactionData> glTransactionDataList = new ArrayList<GLTransactionData>();
			List<PaymentTransactionData> PaymentTransactionDataList = nrrsCollectionBC
					.getGlDataList();
			for (PaymentTransactionData paymentTransactionData : PaymentTransactionDataList) {
				GLTransactionData glTransactionData = new GLTransactionData();
				glTransactionData.setBudgetFiscalYear(paymentTransactionData
						.getBudgetFiscalYear());
				glTransactionData.setTransactionAmount(paymentTransactionData
						.getTransactionAmount());
				glTransactionData.setTransactionCode(paymentTransactionData
						.getTransactionCode());
				glTransactionDataList.add(glTransactionData);
			}

			createContract.setGlTransactionDataList(glTransactionDataList);

			// NRRSRemittanceType remittanceType = NRRSRemittanceType.CHECK;
			createContract.setRemittanceType(NRRSRemittanceType
					.valueOf(nrrsCollectionBC.getRemittanceType()));
			createContract.setOfficeId(nrrsCollectionBC.getOfficeID());
			createContract.setRemitCoreCustomerId(nrrsCollectionBC
					.getRemitterCoreCustomerId());
			createContract.setRemitCustomerSourceCode(nrrsCollectionBC
					.getCustomerSourceCode());
			createContract.setRemittanceAmount(nrrsCollectionBC
					.getRemittanceAmount());
			createContract.setRemitterName(nrrsCollectionBC.getRemitterName());
			createContract.setCheckNumber(nrrsCollectionBC.getCheckNumber());
			createContract
					.setEffectiveDate(nrrsCollectionBC.getEffectiveDate());

			createContract.setObligationId(nrrsCollectionBC
					.getObligationConfirmationNumber().toString());

			// invoke proxy to create an external collection
			CollectionServiceCommonResponse response;
			response = proxy.createExternalCollection(createContract);
			checkDLSBCInvalidDataStopException(response);
			populateNRRSCollectionResponseBO(nrrsCollectionResponseBO, response);

			if (logger.isDebugEnabled()) {
				logger.debug("END-NRRSCollectionBS.createExternalCollection.nrrsCollectionBC.");
			}
		} catch (DLSBCInvalidDataStopException ex) {
			throw ex;
		} catch (AgencyException nrrsEx) {
			logger.error(
					"AgencyException in createExternalCollection, rolling back transaction...",
					nrrsEx);
			DLSBusinessServiceException ex = new DLSBusinessServiceException(
					"NRRS Agency Exception Creation Failure:", nrrsEx);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("error.external.system.unavailable", nrrsEx.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}

		return nrrsCollectionResponseBO;
	}

	/**
	 * Retrieve Collection from NRRS System.
	 * 
	 * @param nrrsReceivableBC
	 *            is a business contract that needs to be processed.
	 * @return NRRSCollectionResponseBO Response from NRRS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	public NRRSCollectionResponseBO retrieveExternalCollection(
			NRRSRetrieveCollectionBC nrrsretrieveCollectionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {
		if (logger.isDebugEnabled()) {
			logger.debug("START-NRRSCollectionBS.retrieveExternalCollection.nrrsretrieveCollectionBC."
					+ nrrsretrieveCollectionBC);
		}

		NRRSCollectionResponseBO nrrsCollectionResponseBO = new NRRSCollectionResponseBO();
		NRRSCollectionBCValidator
				.validateRetrieveCollection(nrrsretrieveCollectionBC);

		// get agency token from DLSAgencyTokenFactory
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(nrrsretrieveCollectionBC
						.getAgencyToken());

		try {
			NRRSCollectionServiceProxy proxy = getNRRSCollectionServiceProxy(agencyToken);
			// Create new collection contract and populate
			RetrieveExternalCollectionContract contract = new RetrieveExternalCollectionContract();
			contract.setTransactionRequestId(nrrsretrieveCollectionBC
					.getTransactionRequestId());
			contract.setConfirmationNumber(nrrsretrieveCollectionBC
					.getConfirmationNumber());
			contract.setToken(agencyToken);
			contract.setTransactionRequestSourceCode(nrrsretrieveCollectionBC
					.getTransactionRequestSourceCode());
			RetrieveExternalCollectionResponse response;

			response = proxy.retrieveExternalCollection(contract);
			checkDLSBCInvalidDataStopException(response);
			populateNRRSCollectionResponseBORetrieveCollection(
					nrrsCollectionResponseBO, response);

			if (logger.isDebugEnabled()) {
				logger.debug("END-NRRSCollectionBS.retrieveExternalCollection");
			}

		} catch (DLSBCInvalidDataStopException ex) {
			throw ex;
		} catch (AgencyException nrrsEx) {
			logger.error(
					"AgencyException in retrieveExternalCollection, rolling back transaction...",
					nrrsEx);
			DLSBusinessServiceException ex = new DLSBusinessServiceException(
					"NRRS Agency Exception Creation Failure:", nrrsEx);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("error.external.system.unavailable", nrrsEx.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}

		return nrrsCollectionResponseBO;
	}

	/**
	 * Delete Collection from NRRS System.
	 * 
	 * @param nrrsReceivableBC
	 *            is a business contract that needs to be processed.
	 * @return NRRSCollectionResponseBO Response from NRRS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	public NRRSCollectionResponseBO deleteExternalCollection(
			NRRSDeleteCollectionBC nrrsCollectionBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {
		if (logger.isDebugEnabled()) {
			logger.debug("START-NRRSCollectionBS.deleteExternalCollection.nrrsCollectionBC."
					+ nrrsCollectionBC);
		}

		NRRSCollectionResponseBO nrrsCollectionResponseBO = new NRRSCollectionResponseBO();
		NRRSCollectionBCValidator.validateDeleteCollection(nrrsCollectionBC);

		// get agency token from DLSAgencyTokenFactory
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(nrrsCollectionBC
						.getAgencyToken());

		NRRSCollectionServiceProxy proxy = getNRRSCollectionServiceProxy(agencyToken);

		try {
			DeleteExternalCollectionContract contract = new DeleteExternalCollectionContract();

			contract.setConfirmationNumber(nrrsCollectionBC
					.getConfirmationNumber());
			contract.setTransactionRequestSourceCode(nrrsCollectionBC
					.getTransactionRequestSourceCode());
			contract.setToken(agencyToken);
			CollectionServiceCommonResponse response = proxy
					.deleteExternalCollection(contract);
			checkDLSBCInvalidDataStopException(response);
			populateNRRSCollectionResponseBO(nrrsCollectionResponseBO, response);

			if (logger.isDebugEnabled()) {
				logger.debug("END-NRRSCollectionBS.deleteExternalCollection");
			}
		} catch (DLSBCInvalidDataStopException nrrsEx) {
			throw nrrsEx;
		}  catch (AgencyException nrrsEx) {
			logger.error(
					"AgencyException in deleteExternalCollection, rolling back transaction...",
					nrrsEx);
			
			if (nrrsEx instanceof DLSBCInvalidDataStopException) {

				throw new DLSBCInvalidDataStopException("" + nrrsEx);
			}
			DLSBusinessServiceException ex = new DLSBusinessServiceException(
					"NRRS Agency Exception Creation Failure:", nrrsEx);
			Map<String, String> errors = new HashMap<String, String>();
			errors.put("error.external.system.unavailable", nrrsEx.getMessage());
			ex.setExtSystemErrorMap(errors);
			throw ex;
		}
		return nrrsCollectionResponseBO;
	}

	/**
	 * populate NRRSCollectionResponse
	 * 
	 * @param nrrsCollectionResponseBO
	 * @param response
	 * @throws DLSBCInvalidDataStopException
	 */
	private void populateNRRSCollectionResponseBO(
			NRRSCollectionResponseBO nrrsCollectionResponseBO,
			CollectionServiceCommonResponse response)
			throws DLSBCInvalidDataStopException {
		nrrsCollectionResponseBO.setConfirmationNumber(response
				.getConfirmationNumber());
		nrrsCollectionResponseBO.setTransactionRequestId(response
				.getTransactionRequestId());

	}

	/**
	 * populate NRRS Response for RetrieveCollection
	 * 
	 * @param nrrsCollectionResponseBO
	 * @param response
	 * @throws DLSBCInvalidDataStopException
	 */
	private void populateNRRSCollectionResponseBORetrieveCollection(
			NRRSCollectionResponseBO nrrsCollectionResponseBO,
			RetrieveExternalCollectionResponse response)
			throws DLSBCInvalidDataStopException {
		nrrsCollectionResponseBO.setTransactionRequestSourceCode(response
				.getTransactionRequestSourceCode());
		nrrsCollectionResponseBO.setTransactionRequestId(response
				.getTransactionRequestId());
		nrrsCollectionResponseBO.setConfirmationNumber(response
				.getConfirmationNumber());
		nrrsCollectionResponseBO
				.setCoreCustomerId(response.getCoreCustomerId());
		nrrsCollectionResponseBO.setCustomerSourceCode(response
				.getCustomerSourceCode());
		nrrsCollectionResponseBO.setStatus(response.getStatus());
		nrrsCollectionResponseBO.setCreatedDate(response.getCreatedDate());
		nrrsCollectionResponseBO.setPrimaryAccountingReferenceCode(response
				.getPrimaryAccountingReferenceCode());
		nrrsCollectionResponseBO.setPrimaryAccountingReferenceNumber(response
				.getPrimaryAccountingReferenceNumber());
		nrrsCollectionResponseBO.setCollectionAmount(response
				.getCollectionAmount());
		nrrsCollectionResponseBO.setProgramCode(response.getProgramCode());
		nrrsCollectionResponseBO.setProgramYear(response.getProgramYear());
		nrrsCollectionResponseBO.setCommodityCode(response.getCommodityCode());
		nrrsCollectionResponseBO.setStateCode(response.getStateCode());
		nrrsCollectionResponseBO.setCountyCode(response.getCountyCode());
		nrrsCollectionResponseBO.setOfficeId(response.getOfficeId());
		nrrsCollectionResponseBO.setRemitCoreCustomerId(response
				.getRemitCoreCustomerId());
		nrrsCollectionResponseBO.setRemitCustomerSourceCode(response
				.getRemitCustomerSourceCode());
		nrrsCollectionResponseBO.setRemittanceAmount(response
				.getRemittanceAmount());
		if (response.getRemittanceType() != null) {
			nrrsCollectionResponseBO.setRemittanceType(response
					.getRemittanceType().getCode());
		}
		nrrsCollectionResponseBO.setRemitterName(response.getRemitterName());
		nrrsCollectionResponseBO.setCheckNumber(response.getCheckNumber());
		nrrsCollectionResponseBO.setEffectiveDate(response.getEffectiveDate());
		nrrsCollectionResponseBO.setRemittanceId(response.getRemittanceId());
		nrrsCollectionResponseBO.setRemittanceStatus(response
				.getRemittanceStatus());

	}

	private void checkDLSBCInvalidDataStopException(
			CollectionServiceCommonResponse response)
			throws DLSBCInvalidDataStopException {

		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		List<String> exceptionMessages = response.getExceptionMessages();

		if (exceptionMessages != null && !exceptionMessages.isEmpty()) {

			for (String exceptionMessage : exceptionMessages) {
				ex.addErrorMessage(exceptionMessage
						.concat(ECConstants.ERROR_MESSAGE_DELIMITER));
			}
		}

		List<String> validationErrorMessages = response.getValidationErrors();
		// Check the response for any validation errors.
		if (validationErrorMessages != null
				&& !validationErrorMessages.isEmpty()) {

			for (String errorMessage : validationErrorMessages) {
				ex.addErrorMessage(errorMessage
						.concat(ECConstants.ERROR_MESSAGE_DELIMITER));
			}
		}

		if (!ex.isErrorMessageListEmpty()) {
			throw ex;
		}
	}

	/**
	 * Is external service is healthy or not
	 * 
	 * @param agencyToken
	 * @return Boolean
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	public Boolean isHealthy(AgencyToken agencyToken)
			throws DLSBCInvalidDataStopException, DLSBusinessServiceException {
		NRRSCollectionServiceProxy proxy;
		Boolean result;

		if (!NRRSCollectionBCValidator.validateAgencyToken(agencyToken, true)) {
			throw new DLSBCInvalidDataStopException("Invalid Agency Token");
		}

		// get agency token from DLSAgencyTokenFactory
		AgencyToken token = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(agencyToken);

		proxy = getNRRSCollectionServiceProxy(token);
		result = proxy.isHealthy();

		return result;
	}

	/**
	 * get NRRSCollectionServiceProxy based on JNDI type i.e. WebService or EJB
	 * Service.
	 * 
	 * @param agencyToken
	 * @return NRRSCollectionServiceProxy
	 */
	private NRRSCollectionServiceProxy getNRRSCollectionServiceProxy(
			AgencyToken agencyToken) {
		return NRRSCollectionServiceFactory.createService(agencyToken,
				NRRS_COLLECTION_SERVICE_SERVICE_SPECIFIER);
	}
}
