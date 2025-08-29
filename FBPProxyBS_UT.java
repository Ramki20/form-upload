package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors.FBPProxySBConnector;
import gov.usda.fsa.fcao.flp.flpids.common.dao.FBPProxyDao;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DALRData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DALRRecord;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPWsdlInfo;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FBPProxyDao.class, FBPProxySBConnector.class })
public class FBPProxyBS_UT extends DLSExternalCommonTestMockBase {

	protected IFBPProxyBS fbpProxyBusinessService;
	private AgencyEncryption agencyEncryption;
	private FBPProxyDao mockFbpProxyDao;
	private FBPProxySBConnector mockFbpProxySBConnector;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		ServiceAgentFacade serviceAgentFacade = ServiceAgentFacade.getInstance();
		fbpProxyBusinessService = ServiceAgentFacade.getInstance().getFbpProxyBusinessService();
		ApplicationContext context = serviceAgentFacade.getApplicationContextFBP();
		mockFbpProxyDao = mock(FBPProxyDao.class);
		mockFbpProxySBConnector = mock(FBPProxySBConnector.class);
		mockFbpProxyDao.setFbpProxySBConnector(mockFbpProxySBConnector);
		fbpProxyBusinessService.setFbpProxyDao(mockFbpProxyDao);
		agencyEncryption = (AgencyEncryption) context.getBean("agencyEncryption");
		fbpProxyBusinessService.setAgencyEncryption(agencyEncryption);
		mockFbpProxyDao.setAgencyToken(this.createAgencyToken());
	}

	@Test
	public void test_encode_decode() throws Exception {
		String originalString = "this is my test string";
		String encodedString = agencyEncryption.encode(originalString);
		String decodedString = agencyEncryption.decode(encodedString);

		Assert.assertEquals(originalString, decodedString);

	}

	@SuppressWarnings("unused")
	@Test
	public void testDecodingPassword() throws Exception {
		String encodedPassword =
				// "Vm0xMFQyUXlWblJWYTJocFUwWmFhRnBYTVdwUFVUMDk=";
				"Vm14V2IxTnRUbGRqUld4V1ltdEtjRlJYY0ZKTlFUMDk=";
		String serviceURL = "https://wem6.cert.sc.egov.usda.gov/gateway/FBPService.asmx";
		String password = FBPWsdlInfo.FBP_PASSWORD_DIGEST;

		String plainTextPassword = agencyEncryption.decode(password);

		plainTextPassword = agencyEncryption.decode("Vm14V2IxTnRUbGRqUld4V1ltdEtjRlJYY0ZKTlFUMDk=");

		plainTextPassword = agencyEncryption.decode("Vm0xMFQyUXlWblJWYTJocFUwWmFhRnBYTVdwUFVUMDk=");

		String passcodeDcoded2 = agencyEncryption.decode("Vm14V2IxTnRUbGRqUld4V1ltdEtjRlJYY0ZKTlFUMDk");

		Assert.assertNotNull(passcodeDcoded2);

	}

	@Test
	public void testRetrieveCreditActionForCustomer() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = 5094279;
		List<FBPProxyBO> fbpProxyBOList = new ArrayList<FBPProxyBO>();
		FBPProxyBO fBPProxyBO = new FBPProxyBO(this.createAgencyToken());
		fbpProxyBOList.add(fBPProxyBO);
		Mockito.when(mockFbpProxyDao.retrieveDLMData(Mockito.any(String.class), Mockito.any(Integer.class)))
				.thenReturn(fbpProxyBOList);

		List<FBPProxyBO> results = fbpProxyBusinessService.retrieveCreditActionForCustomer(token, coreCustomerId);

		Assert.assertFalse(results.isEmpty());

	}

	@Test
	public void testretrieveDALRDataForCustomer() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = 5094279;
		List<FarmBusinessPlanBO> fbpProxyBOList = new ArrayList<FarmBusinessPlanBO>();
		FarmBusinessPlanBO fBPProxyBO = new FarmBusinessPlanBO();
		fbpProxyBOList.add(fBPProxyBO);
		Mockito.when(mockFbpProxyDao.retrieveDALRData(Mockito.any(AgencyToken.class), Mockito.any(String.class),
				Mockito.any(Integer.class))).thenReturn(fbpProxyBOList);

		Collection<FarmBusinessPlanBO> results = fbpProxyBusinessService.retrieveDALRDataForCustomer(token,
				coreCustomerId);

		Assert.assertFalse(results.isEmpty());

	}

	@Test
	public void testretrieveDALRDataForCustomer_verify_dateparsing() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = 5094279;

		DALRData mockedDALRData = mockgetDLARDataTest();
		mockedDALRData.getListOfDALRRecords().get(0).setBegDate("08/01/19");
		mockedDALRData.getListOfDALRRecords().get(0).setEndDate("08-01-19");

		Collection<FarmBusinessPlanBO> results = fbpProxyBusinessService.retrieveDALRDataForCustomer(token,
				coreCustomerId);

		Assert.assertFalse(results.isEmpty());
	}

	@Test
	public void testretrieveDALRDataForCustomer_verify_dateparsing2() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = 5094279;

		DALRData mockedDALRData = mockgetDLARDataTest();
		mockedDALRData.getListOfDALRRecords().get(0).setBegDate("2019/08/01");
		mockedDALRData.getListOfDALRRecords().get(0).setEndDate("08/01/2019");

		Collection<FarmBusinessPlanBO> results = fbpProxyBusinessService.retrieveDALRDataForCustomer(token,
				coreCustomerId);

		Assert.assertFalse(results.isEmpty());
	}

	private DALRData mockgetDLARDataTest() throws FBPServiceBrokerException {
		List<FarmBusinessPlanBO> fbpProxyBOList = new ArrayList<FarmBusinessPlanBO>();
		FarmBusinessPlanBO fBPProxyBO = new FarmBusinessPlanBO();
		fbpProxyBOList.add(fBPProxyBO);
		DALRData daLRData = new DALRData();

		List<DALRRecord> records = new ArrayList<DALRRecord>();
		daLRData.setListOfDALRRecords(records);
		DALRRecord record = new DALRRecord();
		records.add(record);

		record.setFarmOperExp("12");
		record.setFarmOperIntExp("12");
		record.setFarmOperIntExp("12");
		record.setBalAvl("12");
		record.setNonAgcyDebtTax("12");

		record.setScenario("test");
		record.setCaDescr("test");

		record.setBegDate("2019/08/01");
		record.setEndDate("08-01-2019");

		Mockito.when(mockFbpProxySBConnector.getDALRData(Mockito.any(String.class), Mockito.any(String.class),
				Mockito.any(String.class), Mockito.any(String.class), Mockito.any(Integer.class))).thenReturn(daLRData);

		FBPProxyDao fbpProxyDao = (FBPProxyDao) ServiceAgentFacade.getInstance().getApplicationContextFBP()
				.getBean("fbpProxyDao");
		fbpProxyDao.setFbpProxySBConnector(mockFbpProxySBConnector);
		fbpProxyBusinessService.setFbpProxyDao(fbpProxyDao);

		return daLRData;
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveCreditActionForCustomer_Exception() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = -5094279;
		List<FBPProxyBO> fbpProxyBOList = new ArrayList<FBPProxyBO>();
		FBPProxyBO fBPProxyBO = new FBPProxyBO(this.createAgencyToken());
		fbpProxyBOList.add(fBPProxyBO);

		fbpProxyBusinessService.retrieveCreditActionForCustomer(token, coreCustomerId);
	}

	@Test(expected = DLSBCInvalidDataStopException.class)
	public void testRetrieveCreditActionForCustomerInvalidCustomerId() throws Exception {
		AgencyToken token = createAgencyToken();
		Integer coreCustomerId = null;

		fbpProxyBusinessService.retrieveCreditActionForCustomer(token, coreCustomerId);

	}

	@Test
	public void test_getYEACreditActions() throws Exception {
		Integer coreCustomerId = 5094279;

		List<CreditActionBO> listCreditActionBO = new ArrayList<CreditActionBO>();
		CreditActionBO creditActionBO = new CreditActionBO();
		listCreditActionBO.add(creditActionBO);
		Mockito.when(mockFbpProxyDao.getYEACreditActions(Mockito.any(String.class), Mockito.any(Integer.class)))
				.thenReturn(listCreditActionBO);

		List<CreditActionBO> results = fbpProxyBusinessService.getYEACreditActions(coreCustomerId);

		Assert.assertFalse(results.isEmpty());

	}

	@Test
	public void test_getYEACreditActions_noResult() throws Exception {
		List<CreditActionBO> results = fbpProxyBusinessService.getYEACreditActions(-1);

		Assert.assertTrue(results.isEmpty());

	}

	@Test
	public void test_isHealthy() throws Exception {
		Mockito.when(mockFbpProxyDao.isHealthy()).thenReturn(true);
		Assert.assertTrue(fbpProxyBusinessService.isHealthy(createAgencyToken()));
	}
}
