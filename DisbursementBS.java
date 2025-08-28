package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.acct.nps.connector.NPSConnector;
import gov.usda.fsa.acct.nps.connector.NPSConnectorException;
import gov.usda.fsa.acct.nps.connector.NPSConnectorFactory;
import gov.usda.fsa.acct.nps.connector.contract.CheckHealthConnectorContract;
import gov.usda.fsa.acct.nps.connector.response.CheckHealthConnectorResponse;
import gov.usda.fsa.acct.nps.connector.response.FinancialEligibilityConnectorResponse;
import gov.usda.fsa.acct.nps.connector.response.OverpaymentConnectorResponse;
import gov.usda.fsa.acct.nps.connector.response.PaymentConnectorResponse;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.DisbursementRequestBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.FinancialEligibilityRequestBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.RetrieveOverpaymentDetailsByUriValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.RetrievePaymentRequestByURIValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.DisbursementRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FinancialEligibilityRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveOverpaymentDetailsByUriBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrievePaymentDetailsByUriBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.DisbursementResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FinancialEligibilityResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;

import java.util.Map;

/**
 * DisbursementBS <br>
 * <br>
 * Encapsulates all the implementation to create, cancel, retrieve a
 * disbursement for NPS System.
 * 
 * @author kartik.dhingra, partha.chowdhury
 * @version Aug 14, 2013
 * @version 10/31/13 Updated this file to follow coding standards.
 * @version 11/08/13 Updated some methods not to throw NPS exception but to
 *          append all NPS errors into npsRequestFailureReason property.
 * @version 03/16/16 Updated createAPaymentRequest method to pass the
 *          obligationConfirmationNumber to NPSConnector.obligationId
 * 
 */
public class DisbursementBS implements IDisbursementBS {

	/**
	 * Create a payment request for NPS System.
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 *            is a business contract that needs to be processed.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 *             DLSBCInvalidDataStopException Exception thrown by DLS.
	 */
	@SuppressWarnings("unchecked")
	public DisbursementResponseBO createAPaymentRequest(
			DisbursementRequestBC disbursementRequestBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {

		DisbursementRequestBCValidator.validate(disbursementRequestBC);

		// get agency token from DLSAgencyTokenFactory
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(disbursementRequestBC
						.getAgencyToken());

		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();

		try {
			// Create a connector to process the NPS request
			NPSConnector connector = NPSConnectorFactory
					.getConnector(agencyToken);

			// Process the payment using the NPS connector and receive a
			// response.
			PaymentConnectorResponse response = connector
					.processPayment(DisbursementBSHelper.loadPaymentRequestConnectorContract(
							agencyToken, disbursementRequestBC));

			// Populate the response object with the response from NPS.
			disbursementResponseBO = DisbursementBSHelper.populateResponseBO(response,
					disbursementResponseBO);

			// Check if there are any error messages from NPS
			DisbursementBSHelper.readErrors(disbursementResponseBO, response.getErrors());
		} catch (NPSConnectorException npsConEx) {
			DisbursementBSHelper.generateBusinessServiceException(npsConEx);
		}
		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}

	/**
	 * 
	 * Cancel a nps payment request on the basis of payment confirmation number.
	 * 
	 * @param agencyToken
	 *            Agency's token
	 * @param applicationName
	 *            Name of the application
	 * @param paymentConfirmationNumber
	 *            Confirmation number for the disbursement that needs to be
	 *            cancelled.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 *             Exception thrown by DLS.
	 */
	@SuppressWarnings("unchecked")
	public DisbursementResponseBO cancelAPaymentRequest(
			AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber) throws DLSBusinessServiceException,
			DLSBCInvalidDataStopException {

		DisbursementBSHelper.validateCancelAPaymentRequest(agencyToken, applicationName,
				paymentConfirmationNumber);

		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();
		try {
			// get agency token from DLSAgencyTokenFactory
			agencyToken = DLSAgencyTokenFactory
					.createAgencyTokenFromDLSAgencyToken(agencyToken);

			// Create a connector to process the NPS cancel request
			NPSConnector connector = NPSConnectorFactory
					.getConnector(agencyToken);

			PaymentConnectorResponse response = connector
					.cancelPayment(DisbursementBSHelper.loadCancelRequestConnectorContract(
							agencyToken, applicationName,
							paymentConfirmationNumber));

			// Populate the response object with the response from NPS.
			disbursementResponseBO = DisbursementBSHelper.populateResponseBO(response,
					disbursementResponseBO);

			// Check the response for any errors.
			DisbursementBSHelper.readErrors(disbursementResponseBO, response.getErrors());

		} catch (NPSConnectorException npsConEx) {
			DisbursementBSHelper.generateBusinessServiceException(npsConEx);
		}

		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}

	/**
	 * 
	 * Retrieve financial Eligibility details.
	 * 
	 * @param agencyToken
	 *            Agency's token
	 * @param financialEligibilityRequestBO
	 * @return FinancialEligibilityResponseBO Response from NPS
	 * @throws DLSBusinessServiceException.
	 * @throws DLSBCInvalidDataStopException
	 *             Exception thrown by DLS.
	 */
	public FinancialEligibilityResponseBO retrieveFinancialEligibility(
			FinancialEligibilityRequestBC financialEligibilityRequestBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {
		// Validate the contract.
		FinancialEligibilityRequestBCValidator
				.validate(financialEligibilityRequestBC);

		FinancialEligibilityResponseBO financialEligibilityResponseBO = new FinancialEligibilityResponseBO();

		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(financialEligibilityRequestBC
						.getAgencyToken());

		try {
			// Create a connector to process the NPS cancel request
			NPSConnector connector = NPSConnectorFactory
					.getConnector(agencyToken);

			// Send request to NPS
			FinancialEligibilityConnectorResponse response = connector
					.retrieveEligibilityInformation(DisbursementBSHelper.loadFinancialEligibilityConnectorContract(
							agencyToken, financialEligibilityRequestBC));

			financialEligibilityResponseBO = DisbursementBSHelper.loadFinancialEligibilityResponseBO(response);

			DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();

			// Check the response for any errors.
			@SuppressWarnings("unchecked")
			Map<String, String> errorMessages = response.getErrors();
			if (errorMessages != null && !errorMessages.isEmpty()) {
				// implement error handling
				for (Map.Entry<String, String> entry : errorMessages.entrySet()) {
					ex.addErrorMessage(entry.getValue());
				}
				throw ex;
			}
		} catch (NPSConnectorException npsConEx) {
			DisbursementBSHelper.generateBusinessServiceException(npsConEx);
		}

		// return Response BO which contains NPS response.
		return financialEligibilityResponseBO;
	}


	/**
	 * 
	 * Retrieve a payment request by using payment confirmation number.
	 * 
	 * @param agencyToken
	 *            Agency's token
	 * @param applicationName
	 *            Name of the application
	 * @param paymentConfirmationNumber
	 *            Confirmation number for the disbursement that needs to be
	 *            retrieved.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws NPSConnectorException
	 *             Exeptions thrown by NPS.
	 * @throws DLSBCInvalidDataStopException
	 *             Exception thrown by DLS.
	 * @throws DLSBusinessServiceException
	 */
	@SuppressWarnings("unchecked")
	public DisbursementResponseBO retrievePaymentRequestByConfirmationNumber(
			AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber)
			throws DLSBCInvalidDataStopException, DLSBusinessServiceException {

		DisbursementBSHelper.validateRetrievePaymentRequestByConfirmationNumber(agencyToken,
				applicationName, paymentConfirmationNumber);

		// get from DLS agency token
		agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(agencyToken);

		// Create a disbursement response object.
		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();
		try {
			// Create a connector to process the NPS cancel request
			NPSConnector connector = NPSConnectorFactory
					.getConnector(agencyToken);

			// Send request to NPS and receive a response.
			PaymentConnectorResponse response = connector
					.retrievePaymentStatus(DisbursementBSHelper.loadRetrievePaymentStatusConnectorContract(
							agencyToken, applicationName,
							paymentConfirmationNumber));

			disbursementResponseBO = DisbursementBSHelper.populateResponseBO(response,
					disbursementResponseBO);

			// Check the response for any errors.
			DisbursementBSHelper.readErrors(disbursementResponseBO, response.getErrors());
		} catch (NPSConnectorException npsConEx) {
			DisbursementBSHelper.generateBusinessServiceException(npsConEx);
		}

		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}


	/**
	 * 
	 * Retrieve a payment request by using Unique Request Identifier.
	 * 
	 * @param agencyToken
	 *            Agency's token
	 * @param retrievePaymentDetailsByUriBC
	 *            Business contract to retrieve payment details by using Unique
	 *            Request Identifier.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws DLSBCInvalidDataStopException
	 *             Exception thrown by DLS.
	 * @throws DLSBusinessServiceException
	 */
	@SuppressWarnings("unchecked")
	public DisbursementResponseBO retrievePaymentRequestByURI(
			RetrievePaymentDetailsByUriBC retrievePaymentDetailsByUriBC)
			throws DLSBCInvalidDataStopException, DLSBusinessServiceException {
		// Validate the contract.
		RetrievePaymentRequestByURIValidator
				.validate(retrievePaymentDetailsByUriBC);

		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();

		// get from DLS agency token
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(retrievePaymentDetailsByUriBC
						.getAgencyToken());

		try {
			// Create a connector to process the NPS retrieve request
			NPSConnector connector = NPSConnectorFactory
					.getConnector(retrievePaymentDetailsByUriBC
							.getAgencyToken());

			// Populate the response object with the response from NPS.
			PaymentConnectorResponse response = connector
					.retrievePaymentStatusByURI(DisbursementBSHelper.loadRetrievePaymentStatusByURIConnectorContract(
							agencyToken, retrievePaymentDetailsByUriBC));

			disbursementResponseBO = DisbursementBSHelper.populateResponseBO(response,
					disbursementResponseBO);

			// Check the response for any errors.
			DisbursementBSHelper.readErrors(disbursementResponseBO, response.getErrors());
		} catch (NPSConnectorException npsConEx) {
			DisbursementBSHelper.generateBusinessServiceException(npsConEx);
		}

		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}

	/**
	 * Create a payment request for NPS System.
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 *            is a business contract that needs to be processed.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 *             DLSBCInvalidDataStopException Exception thrown by DLS.
	 */
	@SuppressWarnings("unchecked")
	public DisbursementResponseBO retrieveOverpaymentStatusByURI(
			RetrieveOverpaymentDetailsByUriBC retrieveOverpaymentDetailsByUriBC)
			throws DLSBusinessServiceException, DLSBCInvalidDataStopException {
		RetrieveOverpaymentDetailsByUriValidator
				.validate(retrieveOverpaymentDetailsByUriBC);

		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();

		// get from DLS agency token
		AgencyToken agencyToken = DLSAgencyTokenFactory
				.createAgencyTokenFromDLSAgencyToken(retrieveOverpaymentDetailsByUriBC
						.getAgencyToken());

		try {
			NPSConnector connector = NPSConnectorFactory
					.getConnector(agencyToken);

			@SuppressWarnings("deprecation")
			OverpaymentConnectorResponse response = connector
					.retrieveOverpaymentStatusByURI(DisbursementBSHelper.loadRetrieveOverpaymentStatusByURIConnectorContract(
							agencyToken, retrieveOverpaymentDetailsByUriBC));

			disbursementResponseBO = DisbursementBSHelper.loadDisbursementResponseBO(response);

			DisbursementBSHelper.readErrors(disbursementResponseBO, response.getErrors());
		} catch (NPSConnectorException overPayex) {
			DisbursementBSHelper.generateBusinessServiceException(overPayex);
		}

		return disbursementResponseBO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IDisbursementBS
	 * #isHealthy(gov.usda.fsa.common.base.AgencyToken)
	 */
	public boolean isHealthy(AgencyToken token) throws Exception {
		CheckHealthConnectorResponse response = null;
		try {
			NPSConnector connector = NPSConnectorFactory.getConnector(token);
			CheckHealthConnectorContract contract = new CheckHealthConnectorContract(
					token);

			response = connector.checkHealth(contract);
		} catch (Exception e) {
			throw new DLSBusinessServiceException("", e);
		}
		if (response == null) {
			return false;
		} else if (!response.getErrorMessages().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
