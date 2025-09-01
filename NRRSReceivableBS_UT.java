package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.core.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.NRRSReceivableBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.NRRSReceivableResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessServiceException;
import gov.usda.fsa.fcao.flp.flpids.mock.impl.NRRSServiceImpl;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalCreateReceivableContract;
import gov.usda.fsa.nrrs.services.client.NRRSServiceFactory;
import gov.usda.fsa.nrrs.services.client.NRRSServiceProxy;
import gov.usda.fsa.nrrs.servicewrapper.exception.NRRSServiceException;
import gov.usda.fsa.nrrs.vo.recv.CustomerInfo;

/**
 * 
 * NRRSReceivableBS_UT Encapsulates all the test cases for NRRSReceivable
 * 
 * @author Naresh.Gotoor
 * @version 12/10/2013
 * @version 12/18/2014 partha.chowdhury - changed the JNDI name.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ NRRSReceivableBS_UT.class, NRRSReceivableBS.class, NRRSServiceImpl.class,
		NRRSReceivableResponseBO.class, NRRSReceivableBC.class, NRRSServiceFactory.class, NRRSServiceProxy.class,
		ExternalCreateReceivableContract.class, AgencyToken.class, BigDecimal.class, ServiceAgentFacade.class,
		ApplicationContext.class })
@PowerMockIgnore("org.apache.http.conn.ssl.*")
public class NRRSReceivableBS_UT extends DLSExternalCommonTestMockBase {
	private final static String NRRS_SERVICE_JNDI = "gov/usda/fsa/fcao/flpids/common/nrrs_external_service_type";

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	protected AgencyToken createAgencyToken() {
		return createAgencyToken("DLMTest_User");
	}

	protected AgencyToken createAgencyToken(String inUserId) {
		AgencyToken agencyToken = new AgencyToken();
		// agencyToken.setProcessingNode("DLM_jUnit_TEST");
		try {
			agencyToken.setProcessingNode(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		agencyToken.setApplicationIdentifier("OY");
		agencyToken.setRequestHost("localhost");
		agencyToken.setUserIdentifier(inUserId);
		agencyToken.setReadOnly(true);
		return agencyToken;
	}

	/**
	 * Test case for creating Receivable
	 * 
	 * @throws Exception
	 */
	@Test
	public void testcreateReceivablesSuccess() throws Exception {
		AgencyToken token = createAgencyToken();
		// Create contract
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(token);
		populateNRRSReceivableContract(nrrsReceivableBC);

		// Create response
		NRRSReceivableResponseBO nrrsReceivableResponseBO = new NRRSReceivableResponseBO();
		List<BigDecimal> receivableIds = new ArrayList<BigDecimal>();
		receivableIds.add(BigDecimal.valueOf(99999));
		nrrsReceivableResponseBO.setReceivableIds(receivableIds);

		// Mock NRRSServiceProxy
		NRRSServiceProxy proxyMock = PowerMockito.mock(NRRSServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createReceivables(Mockito.anyList())).thenReturn(receivableIds);

		// Mock createService method
		PowerMockito.mockStatic(NRRSServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSServiceFactory.class, "createService", token, NRRS_SERVICE_JNDI);

		// Call actual method
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableResponseBO = nrrsReceivableBS.createReceivables(nrrsReceivableBC);

		Assert.isTrue(nrrsReceivableResponseBO.getReceivableIds().size() > 0);

	}

	@Test(expected = DLSBusinessServiceException.class)
	public void testcreateReceivables_WithAgencyException() throws Exception {
		AgencyToken token = createAgencyToken();
		// Create contract
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(token);
		populateNRRSReceivableContract(nrrsReceivableBC);

		// Mock NRRSServiceProxy
		NRRSServiceProxy proxyMock = PowerMockito.mock(NRRSServiceProxy.class);

		// Mock createReceivables method
		Mockito.when(proxyMock.createReceivables(Mockito.anyList()))
				.thenThrow(new AgencyException("The service cannot be found "));

		// Mock createService method
		PowerMockito.mockStatic(NRRSServiceFactory.class);
		PowerMockito.doReturn(proxyMock).when(NRRSServiceFactory.class, "createService", token, NRRS_SERVICE_JNDI);

		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(nrrsReceivableBC);
	}

	/**
	 * Test case to handle exception scenario
	 * 
	 * @throws DLSBCInvalidDataStopException
	 * @throws DLSBusinessServiceException
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables() throws DLSBCInvalidDataStopException, DLSBusinessServiceException {
//eh
		AgencyToken token = createAgencyToken();
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();

		NRRSReceivableBC contract = new NRRSReceivableBC(token);
		contract.setProgramCode("0210");
		// contract.setCommodityCode("");//optional
		contract.setProgramYear("2014");// optional
		contract.setProgramPrincipalAmount(BigDecimal.valueOf(5000.00));
		// contract.setProgramPrincipalInterest(BigDecimal.ZERO);
		contract.setProgramChargesAmount(BigDecimal.ZERO);
		contract.setDebtDiscoveryCode("10"); // ???????????
		contract.setDebtReasonCode("341"); // ???????????????
		contract.setOrigStateCode("01");
		contract.setOrigCountyCode("125");
		contract.setBudgetFiscalYear("2014");
		contract.setObligationConfirmationNumber(new BigInteger("1234567890"));

		Date date = Calendar.getInstance().getTime();

		contract.setDateOfIndebtedness(new Timestamp(date.getTime()));
		// contract.setInitialNotificationLetterDate(new java.sql.Timestamp());
		contract.setPayableId(1);
		contract.setUri("99999");
		// contract.setDirectAttriGroupID("");
		Map<String, String> referenceFields = new HashMap<String, String>();
		referenceFields.put("18", "12");
		contract.setReferenceFields(referenceFields);
		contract.setCustomerSourceSystemCode("SCIMS");

		Collection customers = new ArrayList<CustomerInfo>();
		CustomerInfo customerInfo = new CustomerInfo("1533454", true);
		customers.add(customerInfo);
		contract.setCustomers(customers);

		NRRSReceivableResponseBO responseBO = nrrsReceivableBS.createReceivables(contract);
		Assert.isTrue(responseBO.getReceivableIds().size() > 0);
	}

	/**
	 * Test case for creating Receivable with null contract.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables_NullContract() throws Exception {
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(null);
	}

	/**
	 * Test case for creating Receivable with null agency token.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables_NullAgencyToken() throws Exception {
		// AgencyToken token = createAgencyToken();
		// Create contract
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(null);
		// populateNRRSReceivableContract(nrrsReceivableBC);
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(nrrsReceivableBC);
	}

	/**
	 * Test case for creating Receivable with zero value of program principal
	 * amount.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables_InvalidProgramPrincipalAmount() throws Exception {
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(this.createAgencyToken());
		populateNRRSReceivableContract(nrrsReceivableBC);
		nrrsReceivableBC.setProgramPrincipalAmount(BigDecimal.ZERO);
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(nrrsReceivableBC);
	}

	/**
	 * Test case for creating Receivable with null vendor id of the customer.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables_NullVendorIdOfCustomer() throws Exception {
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(this.createAgencyToken());
		populateNRRSReceivableContract(nrrsReceivableBC);
		Collection<CustomerInfo> customers = new ArrayList<CustomerInfo>();
		CustomerInfo customerInfo = new CustomerInfo(null, true);
		customers.add(customerInfo);
		nrrsReceivableBC.setCustomers(customers);
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(nrrsReceivableBC);
	}

	/**
	 * Test case for creating Receivable with empty reference field map.
	 * 
	 * @throws Exception
	 */
	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testcreateReceivables_EmptyReferenceFieldMap() throws Exception {
		NRRSReceivableBC nrrsReceivableBC = new NRRSReceivableBC(this.createAgencyToken());
		populateNRRSReceivableContract(nrrsReceivableBC);
		Map<String, String> referenceFields = new HashMap<String, String>();
		nrrsReceivableBC.setReferenceFields(referenceFields);
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.createReceivables(nrrsReceivableBC);
	}

	private void populateNRRSReceivableContract(NRRSReceivableBC contract) {
		contract.setProgramCode("0210");
		// contract.setCommodityCode("");//optional
		contract.setProgramYear("2014");// optional
		contract.setProgramPrincipalAmount(BigDecimal.valueOf(5000.00));
		// contract.setProgramPrincipalInterest(BigDecimal.ZERO);
		contract.setProgramChargesAmount(BigDecimal.ZERO);
		contract.setDebtDiscoveryCode("10"); // ???????????
		contract.setDebtReasonCode("341"); // ???????????????
		contract.setOrigStateCode("01");
		contract.setOrigCountyCode("125");
		contract.setBudgetFiscalYear("2014");
		contract.setObligationConfirmationNumber(new BigInteger("1234567890"));

		Date date = Calendar.getInstance().getTime();

		contract.setDateOfIndebtedness(new Timestamp(date.getTime()));
		// contract.setInitialNotificationLetterDate(new java.sql.Timestamp());
		contract.setPayableId(1);
		contract.setUri("99999");
		// contract.setDirectAttriGroupID("");
		Map<String, String> referenceFields = new HashMap<String, String>();
		referenceFields.put("18", "12");
		contract.setReferenceFields(referenceFields);
		contract.setCustomerSourceSystemCode("SCIMS");
		contract.setObligationConfirmationNumber(new BigInteger("1234567890"));

		Collection customers = new ArrayList<CustomerInfo>();
		CustomerInfo customerInfo = new CustomerInfo("1533454", true);
		customers.add(customerInfo);
		contract.setCustomers(customers);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testIsHealthy_NullAgencyToken() throws Exception {
		INRRSReceivableBS nrrsReceivableBS = ServiceAgentFacade.getInstance().getReceivableBusinessService();
		nrrsReceivableBS.isHealthy(null);
	}
}
