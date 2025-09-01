package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSDeleteCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSRetrieveCollectionBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.NRRSCollectionResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.PaymentTransactionData;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CollectionServiceCommonResponse;
import gov.usda.fsa.nrrs.core.biz.contract.collection.CreateExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.DeleteExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.NRRSRemittanceType;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionContract;
import gov.usda.fsa.nrrs.core.biz.contract.collection.RetrieveExternalCollectionResponse;
import gov.usda.fsa.nrrs.collections.client.NRRSCollectionServiceFactory;
import gov.usda.fsa.nrrs.collections.client.NRRSCollectionServiceProxy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.core.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ NRRSCollectionBS_UT.class, NRRSCollectionBS.class, NRRSCollectionResponseBO.class,
		NRRSCollectionBC.class, NRRSCollectionServiceProxy.class, NRRSCollectionServiceFactory.class,
		CreateExternalCollectionContract.class, AgencyToken.class, BigDecimal.class, ServiceAgentFacade.class,
		ApplicationContext.class, DeleteExternalCollectionContract.class })
@PowerMockIgnore("org.apache.http.conn.ssl.*")
public class NRRSCollectionBS_UT extends DLSExternalCommonTestMockBase {

	private final static String NRRS_COLLECTION_SERVICE_JNDI = "gov/usda/fsa/fcao/flpids/common/nrrs_collection_service_type";
	ServiceAgentFacade serviceAgentFacade;

	@Mock
	NRRSCollectionBS nrrsCollectionBS = new NRRSCollectionBS();

	@Before
	public void setUp() throws Exception {
		super.setUp();
		serviceAgentFacade = ServiceAgentFacade.getInstance();
	}

	protected AgencyToken createAgencyToken() {
		return createAgencyToken("DLMTest_User");
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

	/**
	 * Test case to create Collection
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateExternalCollection() throws Exception {

		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);

		PowerMockito.mock(CreateExternalCollectionContract.class);

		// Create Response
		CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
		response.setConfirmationNumber("5555");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		NRRSCollectionResponseBO nrrsCollectionResponseBO = nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
		Assert.isTrue(nrrsCollectionResponseBO.getConfirmationNumber() != null);
	}

	/**
	 * Test case to create Collection with validation errors and exception messages
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_WithErrors() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);

		PowerMockito.mock(CreateExternalCollectionContract.class);

		// Create Response
		CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
		List<String> validationErrors = new ArrayList<String>();
		validationErrors.add("Test validation error");
		response.setValidationErrors(validationErrors);
		List<String> exceptionMessages = new ArrayList<String>();
		exceptionMessages.add("Test Exception message.");
		response.setExceptionMessages(exceptionMessages);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to create Collection with AgencyException
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBusinessServiceException.class)
	public void testCreateExternalCollection_WithAgencyException() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);

		PowerMockito.mock(CreateExternalCollectionContract.class);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenThrow(new AgencyException("Test agency exception"));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to create Collection with Throwable exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_WithThrowableException() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);

		PowerMockito.mock(CreateExternalCollectionContract.class);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenThrow(new DLSBCInvalidDataStopException("Test null pointer exception"));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	@Test
	public void testCreateCollectionWhenNRRSServiceIsUnavailable() throws Exception {
		// initialize
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);

		// mock
		PowerMockito.mock(CreateExternalCollectionContract.class);
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		AgencyException exception = new AgencyException("NRRS Exception: ");

		// rules
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenThrow(exception);
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// execute
		try {
			nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertEquals(1, ex.getExtSystemErrorMap().size());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	/**
	 * Test case to create Collection with null contract.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_NullContract() throws Exception {
		nrrsCollectionBS.createExternalCollection(null);
	}

	/**
	 * Test case to create Collection with null transaction amount.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_NullTransactionAmount() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);
		nrrsCollectionBC.getGlDataList().get(0).setTransactionAmount(null);
		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to create Collection with null transaction code.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_NullTransactionCode() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);
		nrrsCollectionBC.getGlDataList().get(0).setTransactionCode(null);
		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to create Collection with null budget fiscal year.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_NullBudgetFiscalYear() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		populateNRRSCollectionBC(nrrsCollectionBC);
		nrrsCollectionBC.getGlDataList().get(0).setBudgetFiscalYear(null);
		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to test Create Collection with invalid contract
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testCreateExternalCollection_noContract() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSCollectionBC nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
		// populateNRRSCollectionBC(nrrsCollectionBC);

		PowerMockito.mock(CreateExternalCollectionContract.class);

		// Create Response
		// NRRSCollectionResponseBO nrrsCollectionResponseBO = new
		// NRRSCollectionResponseBO();
		// nrrsCollectionResponseBO.setConfirmationNumber("1234");
		CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
		response.setConfirmationNumber("5555");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createExternalCollection(Mockito.any(CreateExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// INRRSCollectionBS nrrsCollectionBS =
		// ServiceAgentFacade.getInstance().getCollectionBusinessService();
		nrrsCollectionBS.createExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to retrieve Collection
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRetrieveExternalCollection() throws Exception {

		AgencyToken agencyToken = createAgencyToken();
		NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
		nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
		nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

		// Create Response
		RetrieveExternalCollectionResponse response = new RetrieveExternalCollectionResponse();
		response.setConfirmationNumber("5555");
		response.setRemittanceType(NRRSRemittanceType.CHECK);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.retrieveExternalCollection(Mockito.any(RetrieveExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// INRRSCollectionBS nrrsCollectionBS =
		// ServiceAgentFacade.getInstance().getCollectionBusinessService();
		NRRSCollectionResponseBO nrrsCollectionResponseBO = nrrsCollectionBS
				.retrieveExternalCollection(nrrsRetrieveCollectionBC);
		Assert.isTrue(nrrsCollectionResponseBO.getConfirmationNumber() != null);
	}

	@Test
	public void testRetrieveExternalCollectionWhenServiceIsUnavailable() throws Exception {

		AgencyToken agencyToken = createAgencyToken();
		NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
		nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
		nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

		// Create Response
		RetrieveExternalCollectionResponse response = new RetrieveExternalCollectionResponse();
		response.setConfirmationNumber("5555");
		response.setRemittanceType(NRRSRemittanceType.CHECK);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.retrieveExternalCollection(Mockito.any(RetrieveExternalCollectionContract.class)))
				.thenThrow(new AgencyException(""));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// execute
		try {
			nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertEquals(1, ex.getExtSystemErrorMap().size());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	/**
	 * Test case to retrieve Collection with validation errors and exception
	 * messages
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRetrieveExternalCollection_WithErrors() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
		nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
		nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

		// Create Response
		RetrieveExternalCollectionResponse response = new RetrieveExternalCollectionResponse();
		List<String> validationErrors = new ArrayList<String>();
		validationErrors.add("Test validation error");
		response.setValidationErrors(validationErrors);
		List<String> exceptionMessages = new ArrayList<String>();
		exceptionMessages.add("Test exception message");
		response.setExceptionMessages(exceptionMessages);
		response.setConfirmationNumber("5555");
		response.setRemittanceType(NRRSRemittanceType.CHECK);

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.retrieveExternalCollection(Mockito.any(RetrieveExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);
		try {
			nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
		} catch (DLSBCInvalidDataStopException ex) {
			assertEquals(2, ex.getErrorMessageList().size());
		}
	}

	/**
	 * Test case to retrieve Collection with Agency Exception
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBusinessServiceException.class)
	public void testRetrieveExternalCollection_WithAgencyException() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
		nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
		nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.retrieveExternalCollection(Mockito.any(RetrieveExternalCollectionContract.class)))
				.thenThrow(new AgencyException("Test agency exception"));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);
		nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
	}

	/**
	 * Test case to retrieve Collection with null contract.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveExternalCollection_NullContract() throws Exception {
		nrrsCollectionBS.retrieveExternalCollection(null);
	}

	/**
	 * Test case to retrieve Collection with null confirmation number and null
	 * transaction request id.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveExternalCollection_NullConfirmationNumberAndTransactionRequestId() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSRetrieveCollectionBC nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
		nrrsRetrieveCollectionBC.setConfirmationNumber(null);
		nrrsRetrieveCollectionBC.setTransactionRequestId(null);
		nrrsCollectionBS.retrieveExternalCollection(nrrsRetrieveCollectionBC);
	}

	/**
	 * Test case to Delete Collection
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteExternalCollection() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber("14001");
		nrrsCollectionBC.setTransactionRequestSourceCode("OY");

		// Create response
		CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();
		response.setConfirmationNumber("5555");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.deleteExternalCollection(Mockito.any(DeleteExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// INRRSCollectionBS nrrsCollectionBS =
		// ServiceAgentFacade.getInstance().getCollectionBusinessService();
		NRRSCollectionResponseBO nrrsCollectionResponseBO = nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
		Assert.isTrue(nrrsCollectionResponseBO.getConfirmationNumber() != null);

	}

	/**
	 * Test case to Delete Collection
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testDeleteExternalCollection_UnsuccessfulResponse() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber("14001");
		nrrsCollectionBC.setTransactionRequestSourceCode("OY");

		// Create response
		CollectionServiceCommonResponse response = new CollectionServiceCommonResponse();

		List<String> validationErrors = new ArrayList<String>();
		validationErrors.add("Test validation error");
		response.setValidationErrors(validationErrors);
		List<String> exceptionMessages = new ArrayList<String>();
		exceptionMessages.add("Test Exception message.");
		response.setExceptionMessages(exceptionMessages);
		response.setConfirmationNumber("5555");
		response.setTransactionRequestId("1");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.deleteExternalCollection(Mockito.any(DeleteExternalCollectionContract.class)))
				.thenReturn(response);

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// INRRSCollectionBS nrrsCollectionBS =
		// ServiceAgentFacade.getInstance().getCollectionBusinessService();
		NRRSCollectionResponseBO nrrsCollectionResponseBO = nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
		Assert.isTrue(nrrsCollectionResponseBO.getConfirmationNumber() != null);

	}

	/**
	 * Test case to Delete Collection with DLSBCInvalidDataStopException.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testDeleteExternalCollection_DLSBCInvalidDataStopException() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber("14001");
		nrrsCollectionBC.setTransactionRequestSourceCode("OY");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.deleteExternalCollection(Mockito.any(DeleteExternalCollectionContract.class)))
				.thenThrow(new DLSBCInvalidDataStopException("Test invalid data stop exception"));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);

	}

	/**
	 * Test case to Delete Collection with AgencyException.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBusinessServiceException.class)
	public void testDeleteExternalCollection_AgencyException() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber("14001");
		nrrsCollectionBC.setTransactionRequestSourceCode("OY");

		// Mock NRRSCollectionServiceProxy
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.deleteExternalCollection(Mockito.any(DeleteExternalCollectionContract.class)))
				.thenThrow(new AgencyException("Test invalid data stop exception"));

		// Mock createService method
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);

	}

	@Test
	public void testDeleteCollectionWhenNRRSIsUnavailable() throws Exception {
		// init
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber("14001");
		nrrsCollectionBC.setTransactionRequestSourceCode("OY");

		AgencyException exception = new AgencyException("NRRS Exception: ");

		// mock
		NRRSCollectionServiceProxy proxyMock = PowerMockito.mock(NRRSCollectionServiceProxy.class);

		// rules
		Mockito.when(proxyMock.deleteExternalCollection(Mockito.any(DeleteExternalCollectionContract.class)))
				.thenThrow(exception);
		PowerMockito.mockStatic(NRRSCollectionServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSCollectionServiceFactory.class, "createService", agencyToken,
				NRRS_COLLECTION_SERVICE_JNDI);

		// execute
		try {
			nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
		} catch (DLSBusinessServiceException ex) {
			// verify
			assertEquals(1, ex.getExtSystemErrorMap().size());
			assertTrue(ex.getExtSystemErrorMap().containsKey("error.external.system.unavailable"));
		}
	}

	/**
	 * Test case to Delete Collection with null contract.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testDeleteExternalCollection_NullContract() throws Exception {
		nrrsCollectionBS.deleteExternalCollection(null);
	}

	/**
	 * Test case to Delete Collection with null confirmation number.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testDeleteExternalCollection_NullConfirmationNumber() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setConfirmationNumber(null);
		nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to Delete Collection with null transaction request source code.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testDeleteExternalCollection_NullTransactionRequestSoruceCode() throws Exception {
		AgencyToken agencyToken = createAgencyToken();
		NRRSDeleteCollectionBC nrrsCollectionBC = new NRRSDeleteCollectionBC(agencyToken);
		nrrsCollectionBC.setTransactionRequestSourceCode(null);
		nrrsCollectionBS.deleteExternalCollection(nrrsCollectionBC);
	}

	/**
	 * Test case to check nrrs health with invalid agency token..
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testIsHealthy_InvalidAgencyToken() throws Exception {
		nrrsCollectionBS.isHealthy(null);
	}

	// Below test cases can be used to test actual web service call
	/**
	 * Test case for creating External Collection
	 * 
	 * @throws Exception
	 */
	/*
	 * @Test public void testcreateExternalCollection_NRRS_CALL() throws Exception {
	 * 
	 * AgencyToken agencyToken = createAgencyToken(); NRRSCollectionBC
	 * nrrsCollectionBC = new NRRSCollectionBC(agencyToken);
	 * populateNRRSCollectionBC(nrrsCollectionBC); ApplicationContext
	 * applicationContext =
	 * ServiceAgentFacade.getInstance().getApplicationContext(); NRRSCollectionBS
	 * nrrsCollectionBS =
	 * (NRRSCollectionBS)applicationContext.getBean("nrrsCollectionBS");
	 * NRRSCollectionResponseBO nrrsCollectionResponseBO =
	 * nrrsCollectionBS.createExternalCollection(agencyToken, nrrsCollectionBC);
	 * Assert.isTrue(nrrsCollectionResponseBO.getConfirmationNumber() != null); }
	 */

	/**
	 * Test case for retrieving External Collection by Transaction Id
	 * 
	 * @throws DLSBusinessServiceException
	 * @throws DLSBCInvalidDataStopException
	 * @throws Exception
	 */
	/*
	 * @Test public void testretrieveExternalCollection_ActualCall() throws
	 * DLSBCInvalidDataStopException, DLSBusinessServiceException { AgencyToken
	 * agencyToken = createAgencyToken(); NRRSRetrieveCollectionBC
	 * nrrsRetrieveCollectionBC = new NRRSRetrieveCollectionBC(agencyToken);
	 * populateNRRSRetrieveCollectionBC(nrrsRetrieveCollectionBC); INRRSCollectionBS
	 * nrrsCollectionBS =
	 * ServiceAgentFacade.getInstance().getCollectionBusinessService();
	 * NRRSCollectionResponseBO nrrsCollectionResponseBO =
	 * nrrsCollectionBS.retrieveExternalCollection(agencyToken,
	 * nrrsRetrieveCollectionBC);
	 * Assert.isTrue(nrrsCollectionResponseBO.getTransactionRequestId().equals(
	 * nrrsRetrieveCollectionBC.getTransactionRequestId())); }
	 */

	/*
	 * @Test public void testDeleteExternalCollection() throws Exception {
	 * AgencyToken agencyToken = createAgencyToken(); NRRSDeleteCollectionBC
	 * nrrsCollectionBC = new NRRSDeleteCollectionBC(createAgencyToken());
	 * nrrsCollectionBC.setConfirmationNumber("14001");
	 * nrrsCollectionBC.setTransactionRequestSourceCode("OY"); //ApplicationContext
	 * applicationContext =
	 * ServiceAgentFacade.getInstance().getApplicationContext(); INRRSCollectionBS
	 * nrrsCollectionBS =
	 * ServiceAgentFacade.getInstance().getCollectionBusinessService();
	 * NRRSCollectionResponseBO nrrsCollectionResponseBO =
	 * nrrsCollectionBS.deleteExternalCollection(agencyToken, nrrsCollectionBC);
	 * Assert.isTrue(nrrsCollectionResponseBO != null &&
	 * nrrsCollectionResponseBO.getConfirmationNumber() != null); }
	 */

	private void populateNRRSCollectionBC(NRRSCollectionBC nrrsCollectionBC) {
		nrrsCollectionBC.setTransactionRequestSourceCode("VA");
		nrrsCollectionBC.setTransactionRequestId("022020141423");
		nrrsCollectionBC.setCoreCustomerId(Long.valueOf("1533454"));
		nrrsCollectionBC.setCustomerSourceCode("SCIMS");
		nrrsCollectionBC.setPrimaryAccountingReferenceCode("LN");
		nrrsCollectionBC.setPrimaryAccountingReferenceNumber("13");
		nrrsCollectionBC.setCollectionAmount(BigDecimal.valueOf(200.00));
		nrrsCollectionBC.setProgramCode("0210");
		nrrsCollectionBC.setProgramYear("2014");
		nrrsCollectionBC.setCommodityCode("");
		nrrsCollectionBC.setStateCode("01");
		nrrsCollectionBC.setCountyCode("125");

		List<PaymentTransactionData> glDataList = new ArrayList<PaymentTransactionData>();
		PaymentTransactionData paymentTransactionData = new PaymentTransactionData();
		paymentTransactionData.setBudgetFiscalYear("2014");
		paymentTransactionData.setTransactionAmount(BigDecimal.valueOf(150.00));
		paymentTransactionData.setTransactionCode("470");
		glDataList.add(paymentTransactionData);
		paymentTransactionData = new PaymentTransactionData();
		paymentTransactionData.setBudgetFiscalYear("2014");
		paymentTransactionData.setTransactionAmount(BigDecimal.valueOf(50.00));
		paymentTransactionData.setTransactionCode("473");
		glDataList.add(paymentTransactionData);

		nrrsCollectionBC.setGlDataList(glDataList);

		nrrsCollectionBC.setRemittanceType("CHECK");
		nrrsCollectionBC.setOfficeID(Long.valueOf(105917));
		nrrsCollectionBC.setRemittanceAmount(BigDecimal.valueOf(200.00));
		nrrsCollectionBC.setRemitterName("Smith Nelson");
		nrrsCollectionBC.setEffectiveDate(Calendar.getInstance().getTime());
		nrrsCollectionBC.setCheckNumber("12345");
		nrrsCollectionBC.setObligationConfirmationNumber(new BigInteger("1"));

	}

	/*
	 * private void populateNRRSRetrieveCollectionBC(NRRSRetrieveCollectionBC
	 * nrrsRetrieveCollectionBC) {
	 * 
	 * //nrrsRetrieveCollectionBC.setConfirmationNumber("4002");
	 * nrrsRetrieveCollectionBC.setTransactionRequestId("01125201413");
	 * nrrsRetrieveCollectionBC.setTransactionRequestSourceCode("OY"); }
	 */
}
