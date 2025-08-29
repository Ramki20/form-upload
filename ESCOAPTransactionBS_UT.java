package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.CancelESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.FindTransactionByTransactionIdBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.SubmitESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.client.ESCOAPTransactionServiceFactory;
import gov.usda.fsa.afao.escoap.sharedservice.model.ESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.model.FindESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.service.ESCOAPTransactionService;
import gov.usda.fsa.afao.escoap.sharedservice.util.exception.ESCOAPBusinessStopException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCancelTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransaction;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPRetrieveTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ESCOAPTransactionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import junit.framework.Assert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * 
 * ESCOAPTransactionBS_UT Encapsulates all the test cases for
 * ESCOAPTransactionBS
 * 
 * @author Naresh.Gotoor
 * @version 03/04/2014
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ESCOAPTransactionService.class, ESCOAPTransactionServiceFactory.class,
		SubmitESCOAPTransactionBusinessContract.class, FindTransactionByTransactionIdBusinessContract.class })
@PowerMockIgnore("org.apache.http.conn.ssl.*")
public class ESCOAPTransactionBS_UT extends DLSExternalCommonTestMockBase {
	private final static String ESCOAP_SERVICE_JNDI = "gov/usda/fsa/common/escoapsharedservice_url";

	@Mock
	ESCOAPTransactionBS eSCOAPTransactionBS = new ESCOAPTransactionBS();
	AgencyToken agencyToken = null;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		agencyToken = createAgencyToken("DLMTest_User");
	}

	protected AgencyToken createAgencyToken(String inUserId) {
		AgencyToken agencyToken = new AgencyToken();

		try {
			agencyToken.setProcessingNode(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		agencyToken.setApplicationIdentifier("OY");
		agencyToken.setRequestHost("localhost");
		agencyToken.setUserIdentifier(inUserId);
		agencyToken.setReadOnly(true);
		return agencyToken;
	}

	@Test
	public void testCreateTransaction() throws Exception {

		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();

		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();

		// Mock NRRSServiceProxy
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// Mock createService method
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);

		// Mock createReceivables method
		Mockito.when(proxyMock.submitESCOAPTransaction(Mockito.any(SubmitESCOAPTransactionBusinessContract.class)))
				.thenReturn(response);

		// Call ESCOAP Service
		// Call actual method
		// IESCOAPTransactionBS eSCOAPTransactionBS =
		// ServiceAgentFacade.getInstance().getLegacyTransactionBusinessService();
		ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
				.createTransaction(escoapTransactionBC);

		assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));

	}

	@Test
	public void testCreateTransactionWhenServiceIsUnavailable() throws Exception {
		// mock
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// rules
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.submitESCOAPTransaction(Mockito.any(SubmitESCOAPTransactionBusinessContract.class)))
				.thenThrow(new ESCOAPBusinessStopException());

		// execute
		try {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	@Test
	public void testCreateTransactionWhenServiceThrowsError() throws Exception {
		// mock
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();

		Map<String, String> escoapErrors = new HashMap<String, String>();
		escoapErrors.put("service.error.key", "service error text");
		response.setEscoapErrors(escoapErrors);

		// mock
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// rules
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.submitESCOAPTransaction(Mockito.any(SubmitESCOAPTransactionBusinessContract.class)))
				.thenReturn(response);

		// execute
		try {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		} catch (DLSBCInvalidDataStopException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertEquals(1, ex.getExtSystemErrorMap().size());
		}
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_NullContract() throws Exception {
		eSCOAPTransactionBS.createTransaction(null);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_AgencyTokenNull() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = new ESCOAPCreateTransactionBC(null);
		ESCOAPCreateTransaction escoapCreateTransaction = new ESCOAPCreateTransaction(this.createAgencyToken());
		List<ESCOAPCreateTransaction> escoapCreateTransactionList = new ArrayList<ESCOAPCreateTransaction>();
		escoapCreateTransactionList.add(escoapCreateTransaction);
		escoapTransactionBC.setEscoapTransactionList(escoapCreateTransactionList);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_EscoapTransactionListNull() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = new ESCOAPCreateTransactionBC(this.createAgencyToken());
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_ErrorProgramCodeNotNullNotEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramCode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_ProgramTransactionCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramTransactionCode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_ProgramYearNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramYear(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_AccountingReferenceOneCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingReferenceOneCode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_AccountingReferenceOneNumberNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingReferenceOneNumber(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_AccountingTransactionDateNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingTransactionDate(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_BusinessPartyIdentificationNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setBusinessPartyIdentification(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_BusinessTypeCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setBusinessTypeCode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_CountyFSACodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setCountyFSACode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_CustomerNameNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setCustomerName(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_DataSourceAcronymNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setDataSourceAcronym(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_StateFSACodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setStateFSACode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_ApplicationSystemCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setApplicationSystemCode(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_TransactionAmountNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionAmount(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_TransactionQuantityNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionQuantity(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateTransaction_TransactionRequestIdNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionRequestId(null);
		eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
	}

	@Test
	public void testRetrieveTransaction() throws Exception {

		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
		response.setConfirmationNumber(Long.valueOf("12345"));

		// Mock NRRSServiceProxy
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// Mock createService method
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);

		// Mock createReceivables method
		Mockito.when(proxyMock.findESCOAPTransactionByTransactionId(
				Mockito.any(FindTransactionByTransactionIdBusinessContract.class))).thenReturn(response);

		// Call ESCOAP Service
		// Call actual method
		// IESCOAPTransactionBS eSCOAPTransactionBS =
		// ServiceAgentFacade.getInstance().getLegacyTransactionBusinessService();
		ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
				.retrieveTransaction(escoapRetrieveTransactionBC);

		assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));

	}

	@Test
	public void testRetrieveTransactionWhenServiceIsUnavailable() throws Exception {
		// init
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
		response.setConfirmationNumber(Long.valueOf("12345"));

		// mock
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// rules
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.findESCOAPTransactionByTransactionId(
				Mockito.any(FindTransactionByTransactionIdBusinessContract.class)))
				.thenThrow(new ESCOAPBusinessStopException());

		// execute
		try {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	@Test
	public void testRetrieveTransactionWhenServiceThrowsError() throws Exception {
		// init
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
		response.setConfirmationNumber(Long.valueOf("12345"));
		Map<String, String> escoapErrors = new HashMap<String, String>();
		escoapErrors.put("service.error.key", "service error text");
		response.setEscoapErrors(escoapErrors);

		// mock
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// rules
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.findESCOAPTransactionByTransactionId(
				Mockito.any(FindTransactionByTransactionIdBusinessContract.class))).thenReturn(response);

		// execute
		try {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		} catch (DLSBCInvalidDataStopException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertEquals(1, ex.getExtSystemErrorMap().size());
		}
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveTransaction_NullESCOAPRetrieveTransactionBC() throws Exception {
		eSCOAPTransactionBS.retrieveTransaction(null);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveTransaction_NullAgencyToken() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = new ESCOAPRetrieveTransactionBC(null);
		eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveTransaction_TransactionRequestIdNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setTransactionRequestIdentifier(null);
		eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveTransaction_AccountingProgramYearNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setAccountingProgramYear(null);
		eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveTransaction_ApplicationSystemCodeNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setApplicationSystemCode(null);
		eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
	}

	@Test
	public void testCancelTransaction() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);

		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();

		// Mock NRRSServiceProxy
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// Mock createService method
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);

		// Mock createReceivables method
		Mockito.when(proxyMock.cancelESCOAPTransaction(Mockito.any(CancelESCOAPTransactionBusinessContract.class)))
				.thenReturn(response);

		// Call ESCOAP Service
		ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
				.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);

		assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));

	}

	@Test
	public void testCancelTransactionWhenServiceIsUnavailable() throws Exception {
		// init
		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);

		// mock
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.cancelESCOAPTransaction(Mockito.any(CancelESCOAPTransactionBusinessContract.class)))
				.thenReturn(response);

		// execute
		try {
			eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
		} catch (DLSBCInvalidDataStopException ex) {
			// verify
			assertNotNull(ex.getMessage());
			assertTrue(ex.getMessage().contains("error.external.system.unavailable"));
		}
	}

	@Test
	public void testCancelTransactionWhenServiceThrowsError() throws Exception {
		// init
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);

		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();
		Map<String, String> escoapErrors = new HashMap<String, String>();
		escoapErrors.put("service.error.key", "service error text");
		response.setEscoapErrors(escoapErrors);

		// mock
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.cancelESCOAPTransaction(Mockito.any(CancelESCOAPTransactionBusinessContract.class)))
				.thenReturn(response);

		// execute
		try {
			eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
		} catch (DLSBCInvalidDataStopException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertEquals(1, ex.getExtSystemErrorMap().size());
		}
	}

	@Test
	public void testCancelTransactionWhenServiceIsUnavailable1() throws Exception {
		// mock
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);
		ESCOAPTransactionService proxyMock = PowerMockito.mock(ESCOAPTransactionService.class);

		// rules
		PowerMockito.mockStatic(ESCOAPTransactionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(ESCOAPTransactionServiceFactory.class, "createService", agencyToken,
				ESCOAP_SERVICE_JNDI);
		Mockito.when(proxyMock.cancelESCOAPTransaction(Mockito.any(CancelESCOAPTransactionBusinessContract.class)))
				.thenThrow(new ESCOAPBusinessStopException());

		// execute
		try {
			eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCancelTransaction_NullESCOAPCancelTransactionBC() throws Exception {
		eSCOAPTransactionBS.cancelESCOAPTransaction(null);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCancelTransaction_NullAgencyToken() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(null);
		eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCancelTransaction_ApplicationSystemCodeNullOrEmpty() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(this.createAgencyToken());
		eSCOAPCancelTransactionBC.setApplicationSystemCode(null);
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);
		eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCancelTransaction_ConfirmationNumberListNullOrEmpty() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(this.createAgencyToken());
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		eSCOAPCancelTransactionBC.setConfirmationNumberList(null);
		eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCancelTransaction_ConfirmationNumberNullOrEmpty() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(this.createAgencyToken());
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(null);
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);
		eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
	}

	
	private ESCOAPTransactionResponse createESCOAPTransactionResponse() {
		ESCOAPTransactionResponse eSCOAPTransactionResponse = new ESCOAPTransactionResponse();
		List<Long> confirmationNumbers = new ArrayList<Long>();
		confirmationNumbers.add(Long.valueOf("12345"));
		eSCOAPTransactionResponse.setConfirmationNumberList(confirmationNumbers);
		return eSCOAPTransactionResponse;
	}

	private ESCOAPRetrieveTransactionBC populateESCOAPRetrieveTransactionBC() {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = new ESCOAPRetrieveTransactionBC(agencyToken);
		escoapRetrieveTransactionBC.setAgencyToken(agencyToken);
		escoapRetrieveTransactionBC.setAccountingProgramYear("2014");
		escoapRetrieveTransactionBC.setApplicationSystemCode("OY");
		escoapRetrieveTransactionBC.setTransactionRequestIdentifier("011252014125");

		return escoapRetrieveTransactionBC;
	}

	private ESCOAPCreateTransactionBC populateESCOAPTransactionBC() {
		ESCOAPCreateTransactionBC escoapTransactionBC = new ESCOAPCreateTransactionBC(agencyToken);
		List<ESCOAPCreateTransaction> escoapTransactionList = new ArrayList<ESCOAPCreateTransaction>();

		ESCOAPCreateTransaction escoapCreateTransaction = new ESCOAPCreateTransaction(agencyToken);

		// Required Fields
		escoapCreateTransaction.setAccountingProgramCode("0210");
		escoapCreateTransaction.setAccountingProgramTransactionCode("470");// 470 for Principal and 473 for Interest
		escoapCreateTransaction.setAccountingProgramYear("2014"); // can be spaces
		escoapCreateTransaction.setAccountingReferenceOneCode("LN"); // Loan number
		escoapCreateTransaction.setAccountingReferenceOneNumber("1");
		escoapCreateTransaction.setAccountingTransactionDate(Calendar.getInstance());
		escoapCreateTransaction.setBusinessPartyIdentification("1533454"); // Core Customer id
		escoapCreateTransaction.setBusinessTypeCode("00"); // farm_loan_customer, 00 � individual, 01 � business
		escoapCreateTransaction.setStateFSACode("01");
		escoapCreateTransaction.setCountyFSACode("125");
		escoapCreateTransaction.setCustomerName("SMITH NELSON");
		escoapCreateTransaction.setDataSourceAcronym("SCIMS");
		escoapCreateTransaction.setApplicationSystemCode("OY");
		escoapCreateTransaction.setTransactionAmount(BigDecimal.valueOf(10.00));
		escoapCreateTransaction.setTransactionRequestId("011252014125");
		escoapCreateTransaction.setTransactionQuantity(BigDecimal.valueOf(0)); // Zero for FSFL Transactions
		escoapCreateTransaction.setCommodityCode("");
		escoapCreateTransaction.setLegacyTransactionRequestId("011252014125");
		escoapCreateTransaction.setObligationConfirmationNumber(new BigInteger("1234567890"));
		// Required to Reverse Payment and optional for creating Payment in ESCOAP
		escoapCreateTransaction.setReversalIndicator("R");// R is other allowed value
		
		escoapTransactionList.add(escoapCreateTransaction);
		escoapTransactionBC.setEscoapTransactionList(escoapTransactionList);

		return escoapTransactionBC;
	}

	
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testIsHealthy_InvalidAgencyToken() throws Exception {
		eSCOAPTransactionBS.isHealthy(null);
	}
}
