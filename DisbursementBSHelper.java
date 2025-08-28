package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.acct.nps.connector.NPSConnectorException;
import gov.usda.fsa.acct.nps.connector.contract.CancelRequestConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.FinancialEligibilityConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.PaymentRequestConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.RetrieveOverpaymentStatusByURIConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.RetrievePaymentStatusByURIConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.RetrievePaymentStatusConnectorContract;
import gov.usda.fsa.acct.nps.connector.response.FinancialEligibilityConnectorResponse;
import gov.usda.fsa.acct.nps.connector.response.OverpaymentConnectorResponse;
import gov.usda.fsa.acct.nps.connector.response.PaymentConnectorResponse;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.DisbursementRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FinancialEligibilityRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveOverpaymentDetailsByUriBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrievePaymentDetailsByUriBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.DisbursementResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FinancialEligibilityResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ECConstants;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DisbursementBSHelper {

	private static final Logger logger = LogManager
			.getLogger(DisbursementBSHelper.class);

	public static final int VALID_MAX_STRING_LENGTH = 2500;

	public static CancelRequestConnectorContract loadCancelRequestConnectorContract(
			AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber) {

		// Populate CancelRequestConnectorContract.
		CancelRequestConnectorContract contract = new CancelRequestConnectorContract(
				agencyToken);
		contract.setPaymentId(paymentConfirmationNumber);
		contract.setAppName(applicationName);

		return contract;
	}

	public static DisbursementResponseBO loadDisbursementResponseBO(
			OverpaymentConnectorResponse response) {

		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();

		disbursementResponseBO.setReceivableId(response.getReceivableId());
		disbursementResponseBO.setStatus(response.getStatus());
		disbursementResponseBO.setUniqueRequestIdentifier(response
				.getUniqueRequestIdentifier());

		return disbursementResponseBO;
	}

	public static FinancialEligibilityConnectorContract loadFinancialEligibilityConnectorContract(
			AgencyToken agencyToken,
			FinancialEligibilityRequestBC financialEligibilityRequestBC) {

		// Populate FinancialEligibilityConnectorContract.
		FinancialEligibilityConnectorContract contract = new FinancialEligibilityConnectorContract(
				agencyToken);
		contract.setAppName(financialEligibilityRequestBC.getAppName());
		contract.setVendorId(String.valueOf(financialEligibilityRequestBC
				.getVendorId()));
		contract.setVendorSource(financialEligibilityRequestBC
				.getVendorSource());
		contract.setStateCode(financialEligibilityRequestBC.getStateCode());
		contract.setCountyCode(financialEligibilityRequestBC.getCountyCode());
		contract.setProgramCode(financialEligibilityRequestBC.getProgramCode());
		contract.setProgramYear(String.valueOf(financialEligibilityRequestBC
				.getProgramYear()));

		return contract;
	}

	public static FinancialEligibilityResponseBO loadFinancialEligibilityResponseBO(
			FinancialEligibilityConnectorResponse response) {

		FinancialEligibilityResponseBO financialEligibilityResponseBO = new FinancialEligibilityResponseBO();
		// Populate the response object with the response from NPS.
		financialEligibilityResponseBO.setAssignmentIndicator(response
				.isAssignmentIndicator());
		financialEligibilityResponseBO.setClaimIndicator(response
				.isClaimIndicator());
		financialEligibilityResponseBO.setReceivableIndicator(response
				.isReceivableIndicator());
		financialEligibilityResponseBO.setBankruptcyIndicator(response
				.isBankruptcyIndicator());
		financialEligibilityResponseBO.setOtherAgencyIndicator(response
				.isOtherAgencyIndicator());

		return financialEligibilityResponseBO;
	}

	public static PaymentRequestConnectorContract loadPaymentRequestConnectorContract(
			AgencyToken agencyToken, DisbursementRequestBC disbursementRequestBC) {

		// Populate PaymentRequestConnectorContract with values from the
		// disbursement request contract.
		PaymentRequestConnectorContract contract = new PaymentRequestConnectorContract(
				agencyToken);
		contract.setUniqueRequestIdentifier(disbursementRequestBC
				.getUniqueRequestIdentifier());
		contract.setProgramCode(disbursementRequestBC.getProgramCode());
		contract.setApplicationSystemCode(disbursementRequestBC
				.getApplicationSystemCode());
		contract.setBudgetFiscalYear(disbursementRequestBC
				.getBudgetFiscalYear().toString());
		contract.setBureauOfIndianAffairsIndicator(disbursementRequestBC
				.getBureauOfIndianAffairsIndicator());
		contract.setAppName(disbursementRequestBC.getAppName());
		contract.setTransactionAmount(disbursementRequestBC
				.getTransactionAmount());
		contract.setVendorId(disbursementRequestBC.getVendorId().toString());
		contract.setStateCode(disbursementRequestBC.getStateCode());
		contract.setCountyCode(disbursementRequestBC.getCountyCode());
		contract.setProgramYear(disbursementRequestBC.getProgramYear()
				.toString());
		contract.setAccountingServiceRequestDate(disbursementRequestBC
				.getAccountingServiceRequestDate());
		contract.setPaymentIssueDate(disbursementRequestBC
				.getPaymentIssueDate());
		contract.setTransactionQuantity(disbursementRequestBC
				.getTransactionQuantity());
		contract.setVendorName(disbursementRequestBC.getVendorName());
		contract.setUniqueRequestIdentifier(disbursementRequestBC
				.getUniqueRequestIdentifier());
		contract.setForeignPersonFlag(disbursementRequestBC
				.getForeignPersonFlag());
		contract.setVendorSource(disbursementRequestBC.getVendorSource());
		contract.setForcedCheck(disbursementRequestBC.getForcedCheck());
		contract.setAlternatePayee(disbursementRequestBC.getAlternatePayee());
		contract.addReference(ECConstants.NPS_REFERENCE_CODE_LOAN_NUMBER,
				String.format("%05d", disbursementRequestBC.getLoanNumber()));
		contract.addReference(ECConstants.NPS_REFERENCE_CODE_TERM,
				String.format("%02d", disbursementRequestBC.getLoanTerm()));
		contract.setObligationId(disbursementRequestBC
				.getObligationConfirmationNumber());

		return contract;
	}

	public static RetrieveOverpaymentStatusByURIConnectorContract loadRetrieveOverpaymentStatusByURIConnectorContract(
			AgencyToken agencyToken,
			RetrieveOverpaymentDetailsByUriBC retrieveOverpaymentDetailsByUriBC) {

		RetrieveOverpaymentStatusByURIConnectorContract contract = new RetrieveOverpaymentStatusByURIConnectorContract(
				agencyToken);
		contract.setApplicationSystemCode(retrieveOverpaymentDetailsByUriBC
				.getApplicationSystemCode());
		contract.setAppName(retrieveOverpaymentDetailsByUriBC.getAppName());
		contract.setProgramYear(retrieveOverpaymentDetailsByUriBC
				.getProgramYear());
		contract.setUniqueRequestIdentifier(retrieveOverpaymentDetailsByUriBC
				.getUniqueRequestIdentifier());

		return contract;
	}

	public static RetrievePaymentStatusByURIConnectorContract loadRetrievePaymentStatusByURIConnectorContract(
			AgencyToken agencyToken,
			RetrievePaymentDetailsByUriBC retrievePaymentDetailsByUriBC) {
		// Create RetrievePaymentStatusByURIConnectorContract and then set its
		// app name property
		// you
		RetrievePaymentStatusByURIConnectorContract contract = new RetrievePaymentStatusByURIConnectorContract(
				agencyToken,
				retrievePaymentDetailsByUriBC.getUniqueRequestIdentifier(),
				retrievePaymentDetailsByUriBC.getApplicationSystemCode(),
				retrievePaymentDetailsByUriBC.getProgramYear().toString());

		contract.setAppName(retrievePaymentDetailsByUriBC.getAppName());

		return contract;
	}

	/**
	 * This method will read all the NPS errors and append it to
	 * NpsRequestFailureReason string using ';' delimiter
	 * 
	 * @param disbursementResponseBO
	 * @param errorMessages
	 */
	public static void readErrors(
			DisbursementResponseBO disbursementResponseBO,
			Map<String, String> errorMessages) {
		if (errorMessages != null && !errorMessages.isEmpty()) {
			disbursementResponseBO.setExtSystemErrorMap(errorMessages);
			// Add all the error messages from NPS to
			// DLSBCInvalidDataStopException object
			// implement error handling
			for (Map.Entry<String, String> entry : errorMessages.entrySet()) {
				String tempErrorMessage = disbursementResponseBO
						.getNpsRequestFailureReasonDescription();

				// If tempError is null then set its value for the first time.
				if (ValidationUtils.isNullOrEmpty(tempErrorMessage)) {
					tempErrorMessage = entry.getValue();
				} else {
					// Append errors.
					tempErrorMessage = tempErrorMessage
							+ ECConstants.ERROR_MESSAGE_DELIMITER
							+ entry.getValue();
				}
				// If string length of errors is more then 2500 then (Database
				// restriction)
				if (tempErrorMessage.length() < VALID_MAX_STRING_LENGTH) {
					disbursementResponseBO
							.setNpsRequestFailureReasonDescription(tempErrorMessage);
				} else {
					disbursementResponseBO
							.setNpsRequestFailureReasonDescription(tempErrorMessage
									.substring(0, 2500));
				}

				String tempErrorKey = disbursementResponseBO
						.getNpsRequestFailureReason();

				// If tempError is null then set its value for the first time.
				if (ValidationUtils.isNullOrEmpty(tempErrorKey)) {
					tempErrorKey = entry.getKey();
				} else {
					// Append errors.
					tempErrorKey = tempErrorKey
							+ ECConstants.ERROR_MESSAGE_DELIMITER
							+ entry.getKey();
				}
				// If string length of errors is more then 255 then(Database
				// restriction)
				if (tempErrorKey.length() < ECConstants.VALUD_MAX_STRING_LENGTH) {
					disbursementResponseBO
							.setNpsRequestFailureReason(tempErrorKey);
				} else {
					disbursementResponseBO
							.setNpsRequestFailureReason(tempErrorKey.substring(
									0, 256));
				}

				// Log errors from nps
				logger.error(entry.getValue());
			}
		}
	}

	/**
	 * Populate the response BO object with the response of the
	 * PaymentConnectorResponse.
	 * 
	 * @param response
	 *            : PaymentConnectorResponse object that contains the reponse of
	 *            the request made.
	 * @param disbursementResponseBO
	 * @return DisbursementResponseBO
	 */
	public static DisbursementResponseBO populateResponseBO(
			PaymentConnectorResponse response,
			DisbursementResponseBO disbursementResponseBO) {
		disbursementResponseBO.setInitReqDate(response.getInitReqDate());
		disbursementResponseBO.setCancelConfirmationNumber(response
				.getCancelConfirmationNumber());
		disbursementResponseBO.setPaymentId(response.getPaymentId());
		disbursementResponseBO.setStatus(response.getStatus());
		disbursementResponseBO.setUniqueRequestIdentifier(response
				.getUniqueRequestIdentifier());
		return disbursementResponseBO;
	}

	public static RetrievePaymentStatusConnectorContract loadRetrievePaymentStatusConnectorContract(
			AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber) {

		// Populate RetrievePaymentStatusConnectorContract.
		RetrievePaymentStatusConnectorContract contract = new RetrievePaymentStatusConnectorContract(
				agencyToken);
		contract.setPaymentId(paymentConfirmationNumber);
		contract.setAppName(applicationName);

		return contract;
	}

	public static void validateCancelAPaymentRequest(AgencyToken agencyToken,
			String applicationName, Long paymentConfirmationNumber)
			throws DLSBCInvalidDataStopException {
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();

		// Agency token cannot be null
		if (agencyToken == null) {
			ex.addErrorMessage(ECConstants.ERROR_AGENCY_TOKEN_NOT_NULL);
		}

		// Check if Payment Confirmation Number is null or empty.
		if (ValidationUtils.isNullOrEmpty(paymentConfirmationNumber)) {
			ex.addErrorMessage(ECConstants.ERROR_DIS_PAYMENT_CONFIRMATION_NUMBER_NOT_NULL_NOT_EMPTY);
		}

		// Check if application name is null or empty.
		if (ValidationUtils.isNullOrEmpty(applicationName)) {
			ex.addErrorMessage(ECConstants.ERROR_DIS_APP_NAME_NOT_NULL);
		}

		// If Payment Confirmation number or application name is null or empty
		// then throw exception.
		if (!ex.isErrorMessageListEmpty()) {
			throw ex;
		}
	}

	public static void validateRetrievePaymentRequestByConfirmationNumber(
			AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber)
			throws DLSBCInvalidDataStopException {

		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();

		// Agency token cannot be null
		if (agencyToken == null) {
			ex.addErrorMessage(ECConstants.ERROR_AGENCY_TOKEN_NOT_NULL);
		}

		// Payment Confirmation is required.
		if (ValidationUtils.isNullOrEmpty(paymentConfirmationNumber)) {
			ex.addErrorMessage(ECConstants.ERROR_DIS_PAYMENT_CONFIRMATION_NUMBER_NOT_NULL_NOT_EMPTY);
		}

		// Application name is required
		if (ValidationUtils.isNullOrEmpty(applicationName)) {
			ex.addErrorMessage(ECConstants.ERROR_DIS_APP_NAME_NOT_NULL);
		}

		// Throw error if required fields are missing.
		if (!ex.isErrorMessageListEmpty()) {
			throw ex;
		}
	}

	public static void generateBusinessServiceException(
			NPSConnectorException npsConEx) throws DLSBusinessServiceException {
		DLSBusinessServiceException ex = new DLSBusinessServiceException(
				"", npsConEx);
		Map<String, String> errMap = new HashMap<String, String>();
		errMap.put("error.external.system.unavailable", npsConEx.getMessage());		
		ex.setExtSystemErrorMap(errMap);
		throw ex;
	}
}
