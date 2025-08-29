package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.CancelESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.FindTransactionByTransactionIdBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.businesscontract.SubmitESCOAPTransactionBusinessContract;
import gov.usda.fsa.afao.escoap.sharedservice.client.ESCOAPTransactionServiceFactory;
import gov.usda.fsa.afao.escoap.sharedservice.model.ESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.model.FindESCOAPTransactionResponse;
import gov.usda.fsa.afao.escoap.sharedservice.service.ESCOAPTransactionService;
import gov.usda.fsa.afao.escoap.sharedservice.util.exception.ESCOAPBusinessStopException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCancelTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransaction;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPCreateTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ESCOAPRetrieveTransactionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ESCOAPTransactionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 
 * ESCOAPTransactionBS_UT Encapsulates all the test cases for
 * ESCOAPTransactionBS
 * 
 * @author Naresh.Gotoor
 * @version 03/04/2014
 */
@ExtendWith(MockitoExtension.class)
public class ESCOAPTransactionBS_UT extends DLSExternalCommonTestMockBase {
	private final static String ESCOAP_SERVICE_JNDI = "gov/usda/fsa/common/escoapsharedservice_url";

	private ESCOAPTransactionBS eSCOAPTransactionBS;
	
	@Mock
	private ESCOAPTransactionService mockESCOAPService;
	
	private AgencyToken agencyToken = null;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		eSCOAPTransactionBS = new ESCOAPTransactionBS();
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

		// Use try-with-resources for MockedStatic
		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			// Mock static methods
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			// Mock service method
			when(mockESCOAPService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
					.thenReturn(response);

			// Call actual method
			ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
					.createTransaction(escoapTransactionBC);

			assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));
		}
	}

	@Test
	public void testCreateTransactionWhenServiceIsUnavailable() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
					.thenThrow(new ESCOAPBusinessStopException());

			// Execute and verify exception
			DLSBusinessServiceException ex = assertThrows(DLSBusinessServiceException.class, () -> {
				eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
			});

			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	@Test
	public void testCreateTransactionWhenServiceThrowsError() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();

		Map<String, String> escoapErrors = new HashMap<String, String>();
		escoapErrors.put("service.error.key", "service error text");
		response.setEscoapErrors(escoapErrors);

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.submitESCOAPTransaction(any(SubmitESCOAPTransactionBusinessContract.class)))
					.thenReturn(response);

			// Execute and verify exception
			DLSBCInvalidDataStopException ex = assertThrows(DLSBCInvalidDataStopException.class, () -> {
				eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
			});

			assertNotNull(ex.getExtSystemErrorMap());
			assertEquals(1, ex.getExtSystemErrorMap().size());
		}
	}

	@Test
	public void testCreateTransaction_NullContract() throws Exception {
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(null);
		});
	}

	@Test
	public void testCreateTransaction_AgencyTokenNull() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = new ESCOAPCreateTransactionBC(null);
		ESCOAPCreateTransaction escoapCreateTransaction = new ESCOAPCreateTransaction(this.createAgencyToken());
		List<ESCOAPCreateTransaction> escoapCreateTransactionList = new ArrayList<ESCOAPCreateTransaction>();
		escoapCreateTransactionList.add(escoapCreateTransaction);
		escoapTransactionBC.setEscoapTransactionList(escoapCreateTransactionList);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_EscoapTransactionListNull() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = new ESCOAPCreateTransactionBC(this.createAgencyToken());
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_ErrorProgramCodeNotNullNotEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_ProgramTransactionCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramTransactionCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_ProgramYearNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingProgramYear(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_AccountingReferenceOneCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingReferenceOneCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_AccountingReferenceOneNumberNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingReferenceOneNumber(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_AccountingTransactionDateNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setAccountingTransactionDate(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_BusinessPartyIdentificationNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setBusinessPartyIdentification(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_BusinessTypeCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setBusinessTypeCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_CountyFSACodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setCountyFSACode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_CustomerNameNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setCustomerName(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_DataSourceAcronymNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setDataSourceAcronym(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_StateFSACodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setStateFSACode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_ApplicationSystemCodeNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setApplicationSystemCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_TransactionAmountNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionAmount(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_TransactionQuantityNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionQuantity(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testCreateTransaction_TransactionRequestIdNotNullOrEmpty() throws Exception {
		ESCOAPCreateTransactionBC escoapTransactionBC = populateESCOAPTransactionBC();
		escoapTransactionBC.getEscoapTransactionList().get(0).setTransactionRequestId(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.createTransaction(escoapTransactionBC);
		});
	}

	@Test
	public void testRetrieveTransaction() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
		response.setConfirmationNumber(Long.valueOf("12345"));

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.findESCOAPTransactionByTransactionId(
					any(FindTransactionByTransactionIdBusinessContract.class))).thenReturn(response);

			ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
					.retrieveTransaction(escoapRetrieveTransactionBC);

			assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));
		}
	}

	@Test
	public void testRetrieveTransactionWhenServiceIsUnavailable() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.findESCOAPTransactionByTransactionId(
					any(FindTransactionByTransactionIdBusinessContract.class)))
					.thenThrow(new ESCOAPBusinessStopException());

			DLSBusinessServiceException ex = assertThrows(DLSBusinessServiceException.class, () -> {
				eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
			});

			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	@Test
	public void testRetrieveTransactionWhenServiceThrowsError() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();

		FindESCOAPTransactionResponse response = new FindESCOAPTransactionResponse();
		response.setConfirmationNumber(Long.valueOf("12345"));
		Map<String, String> escoapErrors = new HashMap<String, String>();
		escoapErrors.put("service.error.key", "service error text");
		response.setEscoapErrors(escoapErrors);

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.findESCOAPTransactionByTransactionId(
					any(FindTransactionByTransactionIdBusinessContract.class))).thenReturn(response);

			DLSBCInvalidDataStopException ex = assertThrows(DLSBCInvalidDataStopException.class, () -> {
				eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
			});

			assertNotNull(ex.getExtSystemErrorMap());
			assertEquals(1, ex.getExtSystemErrorMap().size());
		}
	}

	@Test
	public void testRetrieveTransaction_NullESCOAPRetrieveTransactionBC() throws Exception {
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.retrieveTransaction(null);
		});
	}

	@Test
	public void testRetrieveTransaction_NullAgencyToken() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = new ESCOAPRetrieveTransactionBC(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		});
	}

	@Test
	public void testRetrieveTransaction_TransactionRequestIdNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setTransactionRequestIdentifier(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		});
	}

	@Test
	public void testRetrieveTransaction_AccountingProgramYearNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setAccountingProgramYear(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		});
	}

	@Test
	public void testRetrieveTransaction_ApplicationSystemCodeNullOrEmpty() throws Exception {
		ESCOAPRetrieveTransactionBC escoapRetrieveTransactionBC = populateESCOAPRetrieveTransactionBC();
		escoapRetrieveTransactionBC.setApplicationSystemCode(null);
		
		assertThrows(DLSBCInvalidDataStopException.class, () -> {
			eSCOAPTransactionBS.retrieveTransaction(escoapRetrieveTransactionBC);
		});
	}

	@Test
	public void testCancelTransaction() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);

		ESCOAPTransactionResponse response = createESCOAPTransactionResponse();

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class)))
					.thenReturn(response);

			ESCOAPTransactionResponseBO escoapTransactionResponseBO = eSCOAPTransactionBS
					.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);

			assertTrue("12345".equals(escoapTransactionResponseBO.getConfirmationNumberList().get(0).toString()));
		}
	}

	@Test
	public void testCancelTransactionWhenServiceIsUnavailable() throws Exception {
		ESCOAPCancelTransactionBC eSCOAPCancelTransactionBC = new ESCOAPCancelTransactionBC(agencyToken);
		eSCOAPCancelTransactionBC.setApplicationSystemCode("OY");
		List<Long> confirmationNumberList = new ArrayList<Long>();
		confirmationNumberList.add(Long.valueOf(12345));
		eSCOAPCancelTransactionBC.setConfirmationNumberList(confirmationNumberList);

		try (MockedStatic<ESCOAPTransactionServiceFactory> factoryMock = mockStatic(ESCOAPTransactionServiceFactory.class);
			 MockedStatic<DLSAgencyTokenFactory> tokenFactoryMock = mockStatic(DLSAgencyTokenFactory.class)) {
			
			factoryMock.when(() -> ESCOAPTransactionServiceFactory.createService(eq(agencyToken), eq(ESCOAP_SERVICE_JNDI)))
					.thenReturn(mockESCOAPService);
			tokenFactoryMock.when(() -> DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(any(AgencyToken.class)))
					.thenReturn(agencyToken);

			when(mockESCOAPService.cancelESCOAPTransaction(any(CancelESCOAPTransactionBusinessContract.class)))
					.thenThrow(new ESCOAPBusinessStopException());

			DLSBusinessServiceException ex = assertThrows(DLSBusinessServiceException.class, () -> {
				eSCOAPTransactionBS.cancelESCOAPTransaction(eSCOAPCancelTransactionBC);
			});

			assertNotNull(ex.getExtSystemErrorMap());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

