package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.*;
import gov.usda.fsa.acct.nps.connector.NPSConnector;
import gov.usda.fsa.acct.nps.connector.NPSConnectorException;
import gov.usda.fsa.acct.nps.connector.NPSConnectorFactory;
import gov.usda.fsa.acct.nps.connector.contract.RetrievePaymentStatusByURIConnectorContract;
import gov.usda.fsa.acct.nps.connector.contract.RetrievePaymentStatusConnectorContract;
import gov.usda.fsa.acct.nps.connector.response.PaymentConnectorResponse;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.BatchDisbursementRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.DisbursementRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FinancialEligibilityRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrievePaymentDetailsByUriBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.DisbursementResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FinancialEligibilityResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ECConstants;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;
import gov.usda.fsa.parmo.frs.ejb.client.contract.RetrieveFarmsServiceContractWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.core.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * DisbursementBS_UT Encapsulates all the test cases for Disbursment
 * 
 * @author kartik.dhingra
 * @version 8/26/13
 * @version 11/08/2013 Updated test cases to incorporate change of DisbursementBS.java done on 11/08/2013
 * @version 12/18/2013 Updated by kartik.dhingra.
 * 						1) Mock the nps response for test cases so that nps should not be reached to create or cancel payment request.
 * 						2) Added comments to follow the standards.
 */
public class DisbursementBS_UT extends DLSExternalCommonTestMockBase
{

	RetrieveFarmsServiceContractWrapper contract;

	private DisbursementBS disbursement;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		// Loading Mocker.xml into the application context.
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] { "Mocker.xml" });
		// of course, an ApplicationContext is just a BeanFactory
		BeanFactory factory = (BeanFactory) appContext;

		// Retrieving mock disbursement bean from the application context.
		disbursement = (DisbursementBS) factory.getBean("disbursement");
	}

	protected AgencyToken createAgencyToken()
	{
		return createAgencyToken("DLMTest_User");
	}

	protected AgencyToken createAgencyToken(String inUserId)
	{
		AgencyToken agencyToken = new AgencyToken();
		agencyToken.setProcessingNode("DLM_jUnit_TEST");
		agencyToken.setApplicationIdentifier("DLM-Test");
		agencyToken.setRequestHost("localhost");
		agencyToken.setUserIdentifier(inUserId);
		agencyToken.setReadOnly(true);
		return agencyToken;
	}

	/**
	 * Create a payment request for NPS System.
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC is a business contract that needs to be processed.
	 * @return DisbursementResponseBO Response from NPS
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException DLSBCInvalidDataStopException Exception thrown by DLS.
	 */
	private DisbursementResponseBO createAPaymentRequest(AgencyToken agencyToken,
			DisbursementRequestBC disbursementRequestBC) throws DLSBusinessServiceException,
			DLSBCInvalidDataStopException
	{
		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();
		try
		{
			disbursementResponseBO.setInitReqDate(Calendar.getInstance());
			disbursementResponseBO.setCancelConfirmationNumber(Long.valueOf(0));
			disbursementResponseBO.setPaymentId(Long.valueOf(12345));
			disbursementResponseBO.setStatus("QU");
			disbursementResponseBO.setUniqueRequestIdentifier(disbursementRequestBC.getUniqueRequestIdentifier());
		}
		catch (Exception npsConEx)
		{
			throw new DLSBusinessServiceException("NPSConnector Exception :", npsConEx);
		}

		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}

	/**
	 * This method is going to create mock exception data and throw DLSBCInvalidDataStopException.
	 * createAPaymentRequestUnhappy1
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private DisbursementResponseBO createAPaymentRequestUnhappy1(AgencyToken agencyToken,
			DisbursementRequestBC disbursementRequestBC) throws DLSBCInvalidDataStopException
	{
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_NAME_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_PROG_CODE_4_DIGIT_NUM);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_PROG_YEAR_4_DIGIT_NUM);
		ex.addErrorMessage(ECConstants.ERROR_DIS_ACC_SERVICE_REQ_DATE_CURRENT_OR_PAST);
		ex.addErrorMessage(ECConstants.ERROR_DIS_ADD_INFO_LINE_LENGTH_LESS_THEN_35);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_SYSTEM_CODE_LENGTH_2);
		ex.addErrorMessage(ECConstants.ERROR_DIS_BIA_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_CITY_NULL_EMPTY_FOREIGN);
		ex.addErrorMessage(ECConstants.ERROR_DIS_DELIVERY_ADD_LINE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_FORCED_CHECK_INDICATOR_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_COUNTY_CODE_LENGTH_3);
		ex.addErrorMessage(ECConstants.ERROR_DIS_STATE_CODE_NUMBER_WITH_2_DIGITS);
		ex.addErrorMessage(ECConstants.ERROR_DIS_REFERENCE_CODE_LENGTH_2);
		ex.addErrorMessage(ECConstants.ERROR_DIS_REFERENCE_NUMBER_INTEGER_VALUE_BETWEEN_1_AND_12);
		ex.addErrorMessage(ECConstants.ERROR_DIS_STATE_ABBREVIATION_NULL_EMPTY_FOREIGN);
		ex.addErrorMessage(ECConstants.ERROR_DIS_TRANSACTION_AMOUNT_BETWEEN_THE_RANGE);
		ex.addErrorMessage(ECConstants.ERROR_DIS_TRANSACTION_QUANTITY_GREATER_THEN_0);
		ex.addErrorMessage(ECConstants.ERROR_DIS_VENDOR_NAME_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_VENDOR_SOURCE_INVALID);
		ex.addErrorMessage(ECConstants.ERROR_DIS_ZIPCODE_NOTREQUIRED_FOR_FOREIGN_ADDRESS);
		throw ex;
	}

	/**
	 * Test case to create a payment request to nps with unhappy data. testCreateAPaymentRequestUnhappy1
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateAPaymentRequestUnhappy1() throws DLSBusinessServiceException
	{
		// Create a dummy agency token
		AgencyToken agencyToken = createAgencyToken();

		// Create a business contract that would be used to call nps.
		DisbursementRequestBC requestBC = createAPaymentRequestUnhappy1Object(agencyToken, "009001200000006");
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// DisbursementResponseBO resp=disbursement.createAPaymentRequest(agencyToken, requestBC);

			// Mock NPS call. DLSBCInvalidDataStopException is expected(20 errors).
			DisbursementResponseBO resp = createAPaymentRequestUnhappy1(agencyToken, requestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			// Exception would occur if there is already a payment with same uri when we make an actual nps call. 20 are
			// the expected.
			if (ex.getErrorMessageList().size() == 1 || ex.getErrorMessageList().size() == 20)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to create an NPS payment request. testCreateAPaymentRequest
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateAPaymentRequest() throws DLSBusinessServiceException
	{
		// ***********Note: change UniqueRequestIdentifier before running the test every time to successfully create a
		// payment request.************

		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create a business contract that would be used to call nps.
		DisbursementRequestBC requestBC = createDisbursementRequestBCObject(agencyToken, "9001200000042");
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// DisbursementResponseBO resp=disbursement.createAPaymentRequest(agencyToken, requestBC);

			// Mock NPS call.
			DisbursementResponseBO resp = createAPaymentRequest(agencyToken, requestBC);

			// Payment Id should be greater then 0 if the call is successful.
			Assert.isTrue(resp.getPaymentId() > 0);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			// Exception would occur if there is already a payment with same uri.
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock exception data for testCreateAPaymentRequestUnhappyNull and throw
	 * DLSBCInvalidDataStopException. createAPaymentRequestUnhappyNull
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private DisbursementResponseBO createAPaymentRequestUnhappyNull(AgencyToken agencyToken,
			DisbursementRequestBC disbursementRequestBC) throws DLSBCInvalidDataStopException
	{
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		ex.addErrorMessage(ECConstants.ERROR_AGENCY_TOKEN_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_NAME_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_PROG_CODE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_PROG_YEAR_4_DIGIT_NUM);
		ex.addErrorMessage(ECConstants.ERROR_DIS_ACC_SERVICE_REQ_DATE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_SYSTEM_CODE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_BIA_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_DELIVERY_ADD_LINE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_FORCED_CHECK_INDICATOR_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_FOREIGN_PERSON_FLAG_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_PAYMENT_ISSUE_DATE_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_COUNTY_CODE_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_STATE_CODE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_REFERENCE_CODE_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_REFERENCE_NUMBER_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_TRANSACTION_AMOUNT_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_TRANSACTION_QUANTITY_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_URI_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_VENDOR_ID_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_VENDOR_NAME_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_VENDOR_SOURCE_NOT_NULL_NOT_EMPTY);
		throw ex;
	}

	/**
	 * Test to create a payment when all the data of business contract is null. testCreateAPaymentRequestUnhappyNull
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateAPaymentRequestUnhappyNull() throws DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = null;

		// Create a business contract that would be used to call nps.
		DisbursementRequestBC requestBC = createAPaymentRequestUnhappyNullObject(agencyToken);
		try
		{
			// Call to nps is commented out as we dont want to call nps everytime we run the test case.
			// DisbursementResponseBO resp=disbursement.createAPaymentRequest(requestBC.getAgencyToken(), requestBC);

			// Mock NPS call. DLSBCInvalidDataStopException is expected(21 errors).
			DisbursementResponseBO resp = createAPaymentRequestUnhappyNull(agencyToken, requestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			// Exception would occur if there is already a payment with same uri when we make an actual nps call. 21 are
			// the expected.
			if (ex.getErrorMessageList().size() == 1 || ex.getErrorMessageList().size() == 21)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock exception data for testCreateAPaymentRequestUnhappyNullRequestBC and throw
	 * DLSBCInvalidDataStopException. createAPaymentRequestUnhappyNullRequestBC
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private DisbursementResponseBO createAPaymentRequestUnhappyNullRequestBC(AgencyToken agencyToken,
			DisbursementRequestBC disbursementRequestBC) throws DLSBCInvalidDataStopException
	{
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		ex.addErrorMessage(ECConstants.ERROR_DIS_DISBURSEMENT_REQUEST_CONTRACT_NOT_NULL);
		throw ex;
	}

	/**
	 * Test to create a payment request when business contract is null. testCreateAPaymentRequestUnhappyNullRequestBC
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateAPaymentRequestUnhappyNullRequestBC() throws DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();
		try
		{
			// Call to nps is commented out as we dont want to call nps everytime we run the test case.
			// DisbursementResponseBO resp=disbursement.createAPaymentRequest(agencyToken, requestBC);

			// Mock NPS call. DLSBCInvalidDataStopException is expected(1 errors).
			DisbursementResponseBO resp = createAPaymentRequestUnhappyNullRequestBC(agencyToken, null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			// Contract should not be null.
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock data for testCancelPaymentRequest. mockDataForCancelPaymentRequest
	 * 
	 * @param agencyToken
	 * @param applicationName
	 * @param paymentConfirmationNumber
	 * @return
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	private DisbursementResponseBO mockDataForCancelPaymentRequest(AgencyToken agencyToken, String applicationName,
			Long paymentConfirmationNumber) throws DLSBusinessServiceException, DLSBCInvalidDataStopException
	{
		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();
		try
		{
			disbursementResponseBO.setInitReqDate(Calendar.getInstance());
			disbursementResponseBO.setCancelConfirmationNumber(Long.parseLong(paymentConfirmationNumber.toString()));
			disbursementResponseBO.setPaymentId(Long.valueOf(32115038));
			disbursementResponseBO.setStatus("CU");
		}
		catch (Exception npsConEx)
		{
			throw new DLSBusinessServiceException("NPSConnector Exception :", npsConEx);
		}

		// return Response BO which contains NPS response.
		return disbursementResponseBO;
	}

	/**
	 * Test to cancel a payment request by using payment id. testCancelPaymentRequest
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCancelPaymentRequest() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();
		try
		{
			// Call to nps is commented out as we dont want to call nps everytime we run the test case.
			// DisbursementResponseBO disbursementResponseBO=disbursement.cancelAPaymentRequest(agencyToken,
			// "PaymentsMigration", Long.parseLong(String.valueOf(32115038)));

			// Mock NPS call.
			DisbursementResponseBO disbursementResponseBO = mockDataForCancelPaymentRequest(agencyToken,
					"PaymentsMigration", new Long(0));

			// disbursementResponseBO should not be null. Expected.
			Assert.isNotNull(disbursementResponseBO);

			// id of disbursementResponseBO should not be null. Expected.
			Assert.isNotNull(disbursementResponseBO.getPaymentId());

			// Status should change to cancel(CU). Expected.
			Assert.isTrue(disbursementResponseBO.getStatus().equalsIgnoreCase("CU"));
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			// Error should be thrown if no payment request is found by that id.
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock data for testCancelPaymentRequestUnhappyNullConfirmationNumber.
	 * mockDataToCreateAPaymentRequestUnhappyNullRequestBC
	 * 
	 * @param agencyToken
	 * @param applicationName
	 * @param paymentConfirmationNumber
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private DisbursementResponseBO mockDataToCreateAPaymentRequestUnhappyNullRequestBC(AgencyToken agencyToken,
			String applicationName, Long paymentConfirmationNumber) throws DLSBCInvalidDataStopException
	{
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		ex.addErrorMessage(ECConstants.ERROR_AGENCY_TOKEN_NOT_NULL);
		ex.addErrorMessage(ECConstants.ERROR_DIS_PAYMENT_CONFIRMATION_NUMBER_NOT_NULL_NOT_EMPTY);
		ex.addErrorMessage(ECConstants.ERROR_DIS_APP_NAME_NOT_NULL);
		throw ex;
	}

	/**
	 * Test to cancel a payment request when confirmation number is null.
	 * testCancelPaymentRequestUnhappyNullConfirmationNumber
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCancelPaymentRequestUnhappyNullConfirmationNumber() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		try
		{
			// Call to nps is commented out as we dont want to call nps everytime we run the test case.
			// DisbursementResponseBO disbursementResponseBO=disbursement.cancelAPaymentRequest(null ,null, null);

			// Mock NPS call. DLSBCInvalidDataStopException expected.
			DisbursementResponseBO disbursementResponseBO = mockDataToCreateAPaymentRequestUnhappyNullRequestBC(null,
					null, null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 3)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to retrieve financial eligibility with all null values of FinancialEligibilityRequestBC contract.
	 * testRetrieveFinancialEligibilityUnhappyNull
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrieveFinancialEligibilityUnhappyNull() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create dummy agency token.
		AgencyToken agencyToken = null;

		// Create business contract to retrieve financial eligibility.
		FinancialEligibilityRequestBC financialEligibilityRequestBC = new FinancialEligibilityRequestBC(agencyToken);

		// Setting all the attributes of the contract to null.
		financialEligibilityRequestBC.setAppName(null);
		financialEligibilityRequestBC.setVendorId(3005342);
		financialEligibilityRequestBC.setVendorSource(null);
		financialEligibilityRequestBC.setStateCode(null);
		financialEligibilityRequestBC.setCountyCode(null);
		financialEligibilityRequestBC.setProgramCode(null);
		financialEligibilityRequestBC.setProgramYear(null);
		try
		{
			// Call nps to retrieve financial eligibility. DLSBCInvalidDataStopException expected.
			FinancialEligibilityResponseBO financialEligibilityResponseBO = disbursement.retrieveFinancialEligibility(
					financialEligibilityRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 6)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test retrieve financial eligibility. testRetrieveFinancialEligibility
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	//@Test
	public void testRetrieveFinancialEligibility() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create business contract to retrieve financial eligibility.
		FinancialEligibilityRequestBC financialEligibilityRequestBC = new FinancialEligibilityRequestBC(agencyToken);
		financialEligibilityRequestBC.setAppName("PaymentsMigration");
		financialEligibilityRequestBC.setVendorId(3005342);
		financialEligibilityRequestBC.setVendorSource("SCIMS");
		financialEligibilityRequestBC.setStateCode("09");
		financialEligibilityRequestBC.setCountyCode("001");
		financialEligibilityRequestBC.setProgramCode("0210");
		financialEligibilityRequestBC.setProgramYear(2000);
		try
		{
			// Call nps to retrieve financial eligibility.
			FinancialEligibilityResponseBO financialEligibilityResponseBO = disbursement.retrieveFinancialEligibility(
					financialEligibilityRequestBC);

			// If retrieve is successful then financialEligibilityResponseBO should not be null.
			Assert.isNotNull(financialEligibilityResponseBO);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1 || ex.getErrorMessageList().size() == 16)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);
		}

	}

	/**
	 * Test retrieve financial eligibility using null FinancialEligibilityRequestBC contract.
	 * testRetrieveFinancialEligibilityUnhappyNullRequestBC
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrieveFinancialEligibilityUnhappyNullRequestBC() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create a null contract to retrieve financial eligibility.
		FinancialEligibilityRequestBC financialEligibilityRequestBC = null;
		try
		{
			// Call nps to retrieve financial eligibility using the null contract. DLSBCInvalidDataStopException
			// expected.
			FinancialEligibilityResponseBO financialEligibilityResponseBO = disbursement.retrieveFinancialEligibility(
					financialEligibilityRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);
		}

	}

	/**
	 * Test to retrieve Financial Eligibility with invalid values. testRetrieveFinancialEligibilityUnhappyInvalidValues1
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrieveFinancialEligibilityUnhappyInvalidValues1() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create dummy agency token.
		AgencyToken agencyToken = null;

		// Create business contract to retrieve financial eligibility.
		FinancialEligibilityRequestBC financialEligibilityRequestBC = new FinancialEligibilityRequestBC(agencyToken);
		financialEligibilityRequestBC.setAppName("PaymentsMigration");
		financialEligibilityRequestBC.setVendorId(3005342);
		financialEligibilityRequestBC.setVendorSource("SCIMSfd");
		financialEligibilityRequestBC.setStateCode("090");
		financialEligibilityRequestBC.setCountyCode(null);
		financialEligibilityRequestBC.setProgramCode("0210dg");
		financialEligibilityRequestBC.setProgramYear(20004);
		try
		{
			// Call NPS to retrieve financial eligibility. DLSBCInvalidDataStopException expected.
			FinancialEligibilityResponseBO financialEligibilityResponseBO = disbursement.retrieveFinancialEligibility(
					financialEligibilityRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 6)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to retrieve payment request by uri with valid values. testRetrievePaymentRequestByURI
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByURI() throws Exception{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request.
		RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

		// Setting attributes of the contract to valid values.
		paymentUri.setApplicationSystemCode("VA");
		paymentUri.setAppName("PaymentsMigration");
		paymentUri.setProgramYear(2013);
		paymentUri.setUniqueRequestIdentifier("01125201343");

		mockNPSConnector();
		
		// Call nps to retrieve payment request by uri.
		DisbursementResponseBO disbursementResponseBO = disbursement.retrievePaymentRequestByURI(paymentUri);

		// If retrieve is successful then URI and payment id should not be null and payment id should be greater then 0.
		Assert.isNotNull(disbursementResponseBO.getUniqueRequestIdentifier());
		Assert.isTrue(disbursementResponseBO.getPaymentId() > 0);
	}

	

	/**
	 * Test to retrieve payment request by uri with null contract.
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected=DLSBCInvalidDataStopException.class)
	public void testRetrievePaymentRequestByURI_NullContract() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		disbursement.retrievePaymentRequestByURI(null);
	}
	
	/**
	 * Test to retrieve payment request by uri with invalid length of application system code(i.e. not equal to 2).
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected=DLSBCInvalidDataStopException.class)
	public void testRetrievePaymentRequestByURI_InvalidLengthOfApplicationSystemCode() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request.
		RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

		// Setting attributes of the contract to valid values.
		paymentUri.setApplicationSystemCode("VA1");
		paymentUri.setAppName("PaymentsMigration");
		paymentUri.setProgramYear(2013);
		paymentUri.setUniqueRequestIdentifier("01125201343");
		disbursement.retrievePaymentRequestByURI(paymentUri);
	}
	
	/**
	 * Test to retrieve payment request by uri with invalid program year(i.e. not between 1776 and 9999).
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected=DLSBCInvalidDataStopException.class)
	public void testRetrievePaymentRequestByURI_ProgramYearNotBetween1776And1999() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request.
		RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

		// Setting attributes of the contract to valid values.
		paymentUri.setApplicationSystemCode("VA");
		paymentUri.setAppName("PaymentsMigration");
		paymentUri.setProgramYear(1775);
		paymentUri.setUniqueRequestIdentifier("01125201343");
		disbursement.retrievePaymentRequestByURI(paymentUri);
	}
	
	/**
	 * Test to retrieve payment request by uri with invalid length of URI(i.e. greater then 15).
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected=DLSBCInvalidDataStopException.class)
	public void testRetrievePaymentRequestByURI_InvalidLengthOfURI() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request.
		RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

		// Setting attributes of the contract to valid values.
		paymentUri.setApplicationSystemCode("VA");
		paymentUri.setAppName("PaymentsMigration");
		paymentUri.setProgramYear(2013);
		paymentUri.setUniqueRequestIdentifier("0112520134312345");
		disbursement.retrievePaymentRequestByURI(paymentUri);
	}
	
	/**
	 * Test to retrieve payment request by uri with non-numeric URI(i.e. greater then 15).
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected=DLSBCInvalidDataStopException.class)
	public void testRetrievePaymentRequestByURI_NonNumericURI() throws DLSBCInvalidDataStopException, DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request.
		RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

		// Setting attributes of the contract to valid values.
		paymentUri.setApplicationSystemCode("VA");
		paymentUri.setAppName("PaymentsMigration");
		paymentUri.setProgramYear(2013);
		paymentUri.setUniqueRequestIdentifier("abcdefghijklm");
		disbursement.retrievePaymentRequestByURI(paymentUri);
	}

	
	/**
	 * Test to retrieve payment request with null values inside RetrievePaymentDetailsByUriBC contract.
	 * testRetrievePaymentRequestByURIWithNullValues
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByURIWithNullValues() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = null;
		try
		{
			// Create RetrievePaymentDetailsByUriBC contract to retrieve payment request by uri.
			RetrievePaymentDetailsByUriBC paymentUri = new RetrievePaymentDetailsByUriBC(agencyToken);

			// Setting all the attributes of the contract to null.
			paymentUri.setApplicationSystemCode(null);
			paymentUri.setAppName(null);
			paymentUri.setProgramYear(null);
			paymentUri.setUniqueRequestIdentifier(null);

			// NPS call to retrieve payment request by URI. DLSBCInvalidDataStopException expected.
			disbursement.retrievePaymentRequestByURI(paymentUri);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 5)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to retrieve payment request by confirmation number. testRetrievePaymentRequestByConfirmationNumber
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByConfirmationNumber() throws Exception{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();
		mockNPSConnector();
		
		// Call nps to retrieve payment request by confirmation number.
		DisbursementResponseBO disbursementResponseBO = disbursement.retrievePaymentRequestByConfirmationNumber(
				agencyToken, "PaymentsMigration", Long.parseLong(String.valueOf(32115038)));

		// If retrieve is successful then uri and payment id should not be null.
		Assert.isNotNull(disbursementResponseBO.getUniqueRequestIdentifier());
		Assert.isTrue(disbursementResponseBO.getPaymentId() > 0);
	}

	/**
	 * Test to retrieve payment request by confirmation number with agencytoken, appname and confirmation number null.
	 * testRetrievePaymentRequestByConfirmationNumberNullValues
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByConfirmationNumberNullValues() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		try
		{
			// Call nps to retrieve payment request by confirmation number with all parameters null.
			DisbursementResponseBO disbursementResponseBO = disbursement.retrievePaymentRequestByConfirmationNumber(
					null, null, null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 3)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to retrieve payment request by null confirmation number. testRetrievePaymentRequestByConfirmationNumberNull
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByConfirmationNumberNull() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create dummy agency token.
		AgencyToken agencyToken = createAgencyToken();
		try
		{
			// Call nps to retrieve payment request by null confirmation number. DLSBCInvalidDataStopException expected.
			DisbursementResponseBO disbursementResponseBO = disbursement.retrievePaymentRequestByConfirmationNumber(
					agencyToken, "PaymentsMigration", null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * Test to retrieve payment request by confirmation number with null app name.
	 * testRetrievePaymentRequestByConfirmationNumberWithAppNameNull
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testRetrievePaymentRequestByConfirmationNumberWithAppNameNull() throws DLSBCInvalidDataStopException,
			DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();
		try
		{
			// Call nps to retrieve payment request by confirmation number and passing null app name. Exception
			// expected.
			DisbursementResponseBO disbursementResponseBO = disbursement.retrievePaymentRequestByConfirmationNumber(
					agencyToken, null, Long.parseLong(String.valueOf(32061311)));
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock data for testCreateBatchPaymentRequest. mockDataForCreateBatchPaymentRequest
	 * 
	 * @param agencyToken
	 * @param batchDisbursementRequestBC
	 * @return
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 */
	private Map<String, DisbursementResponseBO> mockDataForCreateBatchPaymentRequest(AgencyToken agencyToken,
			BatchDisbursementRequestBC batchDisbursementRequestBC) throws DLSBusinessServiceException,
			DLSBCInvalidDataStopException
	{
		Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = new HashMap<String, DisbursementResponseBO>();
		DisbursementResponseBO disbursementResponseBO = new DisbursementResponseBO();
		try
		{
			disbursementResponseBO.setInitReqDate(Calendar.getInstance());
			disbursementResponseBO.setCancelConfirmationNumber(new Long(0));
			disbursementResponseBO.setPaymentId(Long.valueOf(32115038));
			disbursementResponseBO.setStatus("QU");
			disbursementResponseBO.setUniqueRequestIdentifier(batchDisbursementRequestBC.getDisbursementRequestBC()
					.get(0).getUniqueRequestIdentifier());
			batchDisbursementResponseBOMap.put("32129102", disbursementResponseBO);
		}
		catch (Exception npsConEx)
		{
			throw new DLSBusinessServiceException("NPSConnector Exception :", npsConEx);
		}

		// return Response BO which contains NPS response.
		return batchDisbursementResponseBOMap;
	}

	/**
	 * Test to create batch payment request for nps. testCreateBatchPaymentRequest
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateBatchPaymentRequest() throws DLSBusinessServiceException
	{
		// ***********Note: change UniqueRequestIdentifier before running the test every time to successfully create a
		// payment request.************

		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create a business contract for batch payment.
		BatchDisbursementRequestBC batchDisbursementRequestBC = new BatchDisbursementRequestBC(agencyToken);

		// Create a list of disbursement request.
		List<DisbursementRequestBC> disbursementBatchList = new ArrayList<DisbursementRequestBC>();

		// Add one payment disbursement request into the list.
		disbursementBatchList.add(createDisbursementRequestBCObject(agencyToken, "009001200000060"));
		// disbursementBatchList.add(createDisbursementRequestBCObject(agencyToken,"009001200000019"));

		// Add payment request list to the contract.
		batchDisbursementRequestBC.setDisbursementRequestBC(disbursementBatchList);
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// Map<String, DisbursementResponseBO>
			// batchDisbursementResponseBOMap=disbursement.createBatchPaymentRequest(agencyToken,
			// batchDisbursementRequestBC);

			// Mock NPS call.
			Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = mockDataForCreateBatchPaymentRequest(
					agencyToken, batchDisbursementRequestBC);

			// If the payment request is created successfully then size of the map should be greater then 0.
			Assert.isTrue(batchDisbursementResponseBOMap.size() == 1);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock exception data for testCreateBatchPaymentRequestUnhappy1 and throw
	 * DLSBCInvalidDataStopException. mockDataToCreateBatchPaymentRequestUnhappy1
	 * 
	 * @param agencyToken
	 * @param disbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private Map<String, DisbursementResponseBO> mockDataToCreateBatchPaymentRequestUnhappy1(AgencyToken agencyToken,
			BatchDisbursementRequestBC batchDisbursementRequestBC) throws DLSBCInvalidDataStopException
	{
		createAPaymentRequestUnhappy1(agencyToken, batchDisbursementRequestBC.getDisbursementRequestBC().get(0));
		return null;
	}

	/**
	 * Test to create a batch payment with invalid data. testCreateBatchPaymentRequestUnhappy1
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateBatchPaymentRequestUnhappy1() throws DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = null;

		// Create a business contract for batch payment.
		BatchDisbursementRequestBC batchDisbursementRequestBC = new BatchDisbursementRequestBC(agencyToken);

		// Create a list of disbursement request.
		List<DisbursementRequestBC> disbursementBatchList = new ArrayList<DisbursementRequestBC>();

		// Add one payment disbursement request into the list.
		disbursementBatchList.add(createAPaymentRequestUnhappy1Object(agencyToken, "009001200000023"));
		// disbursementBatchList.add(createDisbursementRequestBCObject(agencyToken,"009001200000019"));

		// Add payment request list to the contract.
		batchDisbursementRequestBC.setDisbursementRequestBC(disbursementBatchList);
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// Map<String, DisbursementResponseBO>
			// batchDisbursementResponseBOMap=disbursement.createBatchPaymentRequest(agencyToken,
			// batchDisbursementRequestBC);

			// Mock NPS call. 20 Expected errors.
			Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = mockDataToCreateBatchPaymentRequestUnhappy1(
					agencyToken, batchDisbursementRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1 || ex.getErrorMessageList().size() == 20)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);
		}
	}

	/**
	 * This method is going to create mock data for testCreateBatchPaymentRequestUnhappyNull and then throws
	 * DLSBCInvalidDataStopException exception. mockDataToCreateBatchPaymentRequestUnhappyNull
	 * 
	 * @param agencyToken
	 * @param batchDisbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private Map<String, DisbursementResponseBO> mockDataToCreateBatchPaymentRequestUnhappyNull(AgencyToken agencyToken,
			BatchDisbursementRequestBC batchDisbursementRequestBC) throws DLSBCInvalidDataStopException
	{
		createAPaymentRequestUnhappyNull(agencyToken, batchDisbursementRequestBC.getDisbursementRequestBC().get(0));
		return null;
	}

	/**
	 * Test to create a batch payment with null disbursementRequestBC contract values inside batch contract.
	 * testCreateBatchPaymentRequestUnhappyNull
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateBatchPaymentRequestUnhappyNull() throws DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = null;

		// Create a business contract for batch payment.
		BatchDisbursementRequestBC batchDisbursementRequestBC = new BatchDisbursementRequestBC(agencyToken);

		// Create a list of disbursement request.
		List<DisbursementRequestBC> disbursementBatchList = new ArrayList<DisbursementRequestBC>();

		// Add one payment disbursement request into the list.
		disbursementBatchList.add(createAPaymentRequestUnhappyNullObject(null));
		// disbursementBatchList.add(createDisbursementRequestBCObject(agencyToken,"009001200000019"));

		// Add payment request list to the contract.
		batchDisbursementRequestBC.setDisbursementRequestBC(disbursementBatchList);
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// Map<String, DisbursementResponseBO>
			// batchDisbursementResponseBOMap=disbursement.createBatchPaymentRequest(batchDisbursementRequestBC.getAgencyToken(),
			// batchDisbursementRequestBC);

			// Mock NPS call. 21 Expected errors.
			Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = mockDataToCreateBatchPaymentRequestUnhappyNull(
					agencyToken, batchDisbursementRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1 || ex.getErrorMessageList().size() == 21)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock data for testCreateBatchPaymentRequestUnhappyNullRequestBC and then throws
	 * DLSBCInvalidDataStopException exception. mockDataToCreateBatchPaymentRequestUnhappyNullRequestBC
	 * 
	 * @param agencyToken
	 * @param batchDisbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private Map<String, DisbursementResponseBO> mockDataToCreateBatchPaymentRequestUnhappyNullRequestBC(
			AgencyToken agencyToken, BatchDisbursementRequestBC batchDisbursementRequestBC)
			throws DLSBCInvalidDataStopException
	{
		createAPaymentRequestUnhappyNullRequestBC(agencyToken, batchDisbursementRequestBC.getDisbursementRequestBC()
				.get(0));
		return null;
	}

	/**
	 * Test to create a batch payment with null DisbursementRequestBC contract.
	 * testCreateBatchPaymentRequestUnhappyNullRequestBC
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateBatchPaymentRequestUnhappyNullRequestBC() throws DLSBusinessServiceException
	{
		// Create a dummy agency token.
		AgencyToken agencyToken = createAgencyToken();

		// Create a business contract for batch payment.
		BatchDisbursementRequestBC batchDisbursementRequestBC = new BatchDisbursementRequestBC(agencyToken);

		// Create a list of disbursement request.
		List<DisbursementRequestBC> disbursementBatchList = new ArrayList<DisbursementRequestBC>();

		// Add a null DisbursementRequestBC to the list.
		disbursementBatchList.add(null);
		batchDisbursementRequestBC.setDisbursementRequestBC(disbursementBatchList);
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// Map<String, DisbursementResponseBO>
			// batchDisbursementResponseBOMap=disbursement.createBatchPaymentRequest(agencyToken,
			// batchDisbursementRequestBC);

			// Mock NPS call. 1 Expected errors.
			Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = mockDataToCreateBatchPaymentRequestUnhappyNullRequestBC(
					agencyToken, batchDisbursementRequestBC);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create mock data for testCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC
	 * and throws DLSBCInvalidDataStopException exception.
	 * mockDataToCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC
	 * 
	 * @param agencyToken
	 * @param batchDisbursementRequestBC
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private Map<String, DisbursementResponseBO> mockDataToCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC(
			AgencyToken agencyToken, BatchDisbursementRequestBC batchDisbursementRequestBC)
			throws DLSBCInvalidDataStopException
	{
		DLSBCInvalidDataStopException ex = new DLSBCInvalidDataStopException();
		ex.addErrorMessage(ECConstants.ERROR_BATCH_DIS_REQ_NOT_NULL);
		throw ex;
	}

	/**
	 * Test to create a batch payment request with BatchDisbursementRequestBC null.
	 * testCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC
	 * 
	 * @throws DLSBusinessServiceException
	 */
	@Test
	public void testCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC() throws DLSBusinessServiceException
	{
		try
		{
			// Call to nps is commented out as we dont want to create a payment everytime we run the test case.
			// Map<String, DisbursementResponseBO>
			// batchDisbursementResponseBOMap=disbursement.createBatchPaymentRequest(null, null);

			// Mock NPS call. 1 Expected errors.
			Map<String, DisbursementResponseBO> batchDisbursementResponseBOMap = mockDataToCreateBatchPaymentRequestUnhappyNullBatchDisbursementRequestBC(
					null, null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{

			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
			else
				Assert.isTrue(false);

		}
	}

	/**
	 * This method is going to create a disbursementRequestBC object. createDisbursementRequestBCObject
	 * 
	 * @param agencyToken
	 * @param uri
	 * @return
	 */
	private DisbursementRequestBC createDisbursementRequestBCObject(AgencyToken agencyToken, String uri)
	{
		DisbursementRequestBC requestBC = new DisbursementRequestBC(agencyToken);
		requestBC.setAppName("PaymentsMigration");
		requestBC.setProgramCode("0210");
		requestBC.setAddressInformationLine("6801 NW 214TH ST");
		requestBC.setDeliveryAddressLine("6801 NW 214TH ST");
		requestBC.setApplicationSystemCode("VA");
		requestBC.setBudgetFiscalYear(2013);
		requestBC.setBureauOfIndianAffairsIndicator(false);
		requestBC.setCityName("Alachua");
		requestBC.setTransactionAmount(new BigDecimal(9000.00));
		requestBC.setZipCode("32615");
		requestBC.setVendorSource("SCIMS");
		requestBC.setVendorId(3005342);
		requestBC.setStateCode("09");
		requestBC.setCountyCode("001");
		requestBC.setStateAbbreviation("FL");
		requestBC.setProgramYear(2000);
		Calendar past = Calendar.getInstance();
		// past.set(Calendar.YEAR, 2014);
		requestBC.setAccountingServiceRequestDate(past);
		requestBC.setPaymentIssueDate(Calendar.getInstance());
		requestBC.setTransactionQuantity(new BigDecimal(3));
		requestBC.setVendorName("Carol");
		requestBC.setUniqueRequestIdentifier(uri);
		requestBC.setForeignPersonFlag(false);
		requestBC.setStateCode("LN");
		requestBC.setLoanNumber(1);
		requestBC.setForcedCheck(true);
		return requestBC;
	}

	/**
	 * This method is going to create a payment request object with invalid data. createAPaymentRequestUnhappy1Object
	 * 
	 * @param agencyToken
	 * @param uri
	 * @return
	 */
	private DisbursementRequestBC createAPaymentRequestUnhappy1Object(AgencyToken agencyToken, String uri)
	{
		DisbursementRequestBC requestBC = new DisbursementRequestBC(agencyToken);
		requestBC.setAppName(null);
		requestBC.setProgramCode("02100");
		requestBC.setAddressInformationLine("6801 NW 214TH STasfasfadfasdfasdfasdfasdfafasfasdfasdf");
		requestBC.setDeliveryAddressLine("");
		requestBC.setApplicationSystemCode("VAA");
		requestBC.setBudgetFiscalYear(2014);
		requestBC.setBureauOfIndianAffairsIndicator(null);
		requestBC.setCityName("Alachua");
		requestBC.setTransactionAmount(new BigDecimal(-1));
		requestBC.setZipCode("3261589");
		requestBC.setVendorSource("lSCIMSj");
		requestBC.setVendorId(3005342);
		requestBC.setStateCode("090");
		requestBC.setCountyCode("0010");
		requestBC.setStateAbbreviation("FL");
		requestBC.setProgramYear(200);
		Calendar past = Calendar.getInstance();
		past.set(Calendar.YEAR, 2014);
		requestBC.setAccountingServiceRequestDate(past);
		requestBC.setPaymentIssueDate(Calendar.getInstance());
		requestBC.setTransactionQuantity(new BigDecimal(-1));
		requestBC.setVendorName("");
		requestBC.setUniqueRequestIdentifier(uri);
		requestBC.setForeignPersonFlag(true);
		requestBC.setStateCode("LNi");
		requestBC.setLoanNumber(15);
		requestBC.setForcedCheck(null);
		return requestBC;
	}

	/**
	 * This method is going to create a payment request object with null values. createAPaymentRequestUnhappyNullObject
	 * 
	 * @param agencyToken
	 * @return
	 */
	private DisbursementRequestBC createAPaymentRequestUnhappyNullObject(AgencyToken agencyToken)
	{
		DisbursementRequestBC requestBC = new DisbursementRequestBC(agencyToken);
		requestBC.setAppName(null);
		requestBC.setProgramCode(null);
		requestBC.setAddressInformationLine(null);
		requestBC.setDeliveryAddressLine(null);
		requestBC.setApplicationSystemCode(null);
		requestBC.setBudgetFiscalYear(2013);
		requestBC.setBureauOfIndianAffairsIndicator(null);
		requestBC.setCityName(null);
		requestBC.setTransactionAmount(null);
		requestBC.setZipCode(null);
		requestBC.setVendorSource(null);
		requestBC.setVendorId(null);
		requestBC.setStateCode(null);
		requestBC.setCountyCode(null);
		requestBC.setStateAbbreviation(null);
		requestBC.setProgramYear(null);
		Calendar past = Calendar.getInstance();
		past.set(Calendar.YEAR, 2014);
		requestBC.setAccountingServiceRequestDate(null);
		requestBC.setPaymentIssueDate(null);
		requestBC.setTransactionQuantity(null);
		requestBC.setVendorName(null);
		requestBC.setUniqueRequestIdentifier(null);
		requestBC.setForeignPersonFlag(null);
		requestBC.setStateCode(null);
		requestBC.setLoanNumber(null);
		requestBC.setForcedCheck(null);
		return requestBC;
	}
	
	private void mockNPSConnector() throws Exception, NPSConnectorException {
		NPSConnector mockConnector = mock(NPSConnector.class);
		NPSConnectorFactory instance = (NPSConnectorFactory)ReflectionUtility.createObject(NPSConnectorFactory.class);
		ReflectionUtility.setAttribute(instance, mockConnector, "connector");		
		PaymentConnectorResponse response = new PaymentConnectorResponse(1l,2l,null, "P",Calendar.getInstance(),
				"123", Calendar.getInstance(), "tester");
		when(mockConnector.retrievePaymentStatusByURI(any(RetrievePaymentStatusByURIConnectorContract.class))).thenReturn(response);
		
		when(mockConnector.retrievePaymentStatus(any(RetrievePaymentStatusConnectorContract.class))).thenReturn(response);
	}
}
