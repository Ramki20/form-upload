package gov.usda.fsa.fcao.flp.flpids.common.dao;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors.FBPProxySBConnector;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMRecord;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPConnector;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;
import junit.framework.Assert;

public class FBPProxyDao_UT extends DLSExternalCommonTestMockBase {

	private FBPProxyDao fbpProxyDao;
	private ServiceAgentFacade serviceAgentFacade;

	@Mock
	private FBPConnector fbpConnector = null;

	@Mock
	private FBPProxySBConnector mockFbpProxySBConnector;

	private static final Logger logger = LogManager.getLogger(FBPProxyDao_UT.class);

	@Before
	public void setUp() throws Exception {
		super.setUp();

		MockitoAnnotations.initMocks(this);
		populateMockObjects();
	}

	protected void populateMockObjects() {

		ServiceAgentFacade.setLAZYLOADING(true);
		serviceAgentFacade = ServiceAgentFacade.getInstance();
		ServiceAgentFacade.getInstance().getFbpProxyBusinessService();
		ApplicationContext context = serviceAgentFacade.getApplicationContextFBP();

		fbpProxyDao = (FBPProxyDao) context.getBean("fbpProxyDao");

		ReflectionTestUtils.setField(mockFbpProxySBConnector, "fbpConnector", fbpConnector);
		ReflectionTestUtils.setField(fbpProxyDao, "fbpProxySBConnector", mockFbpProxySBConnector);

		ServiceAgentFacade.setLAZYLOADING(false);
	}

	public void testJndiEnvironment() throws NamingException {
		Context initialCtx = null;
		try {
			if (NamingManager.hasInitialContextFactoryBuilder()) {
				logger.debug("NamingManager.hasInitialContextFactoryBuilder() = true");
				Hashtable<String, Object> env = new Hashtable<String, Object>();
				initialCtx = NamingManager.getInitialContext(env);
			} else {
				logger.debug("NamingManager.hasInitialContextFactoryBuilder() = false");
			}
			Assert.assertNotNull(initialCtx);
			logger.debug("InitialContext object class = " + initialCtx.getClass().getName());
			String nameSpaceRoot = (String) initialCtx.lookup("java:comp/env/name_space_root");
			Assert.assertNotNull(nameSpaceRoot);
			logger.debug("nameSpaceRoot = " + nameSpaceRoot);
			Assert.assertEquals(nameSpaceRoot, "cell/persistent");
			Context subContext = (Context) initialCtx.lookup(nameSpaceRoot);
			Assert.assertNotNull(subContext);
			logger.debug("subContext object class = " + subContext.getClass().getName());
			String sharedservice_specifier = (String) subContext
					.lookup("gov/usda/fsa/common/citso/cbs/sharedservice_specifier");
			Assert.assertNotNull(sharedservice_specifier);
			logger.debug("sharedservice_specifier = " + sharedservice_specifier);
			Assert.assertEquals(sharedservice_specifier, "WS");

		} catch (NamingException e) {
			logger.debug("NamingException thrown in testJndiEnvironment()", e);
			throw e;
		}
	}

	@Test(expected = FBPServiceBrokerException.class)
	public void testRetrieveDLMData_WithoutData() throws Exception {

		testJndiEnvironment();

		ApplicationContext context = serviceAgentFacade.getApplicationContextFBP();
		fbpProxyDao = (FBPProxyDao) context.getBean("fbpProxyDao");
		FBPConnector fbpConnector = (FBPConnector) context.getBean("fbpConnector");
		FBPProxySBConnector fbpProxySBConnector = (FBPProxySBConnector) context.getBean("fbpProxySBConnector");
		fbpProxySBConnector.setFbpConnector(fbpConnector);
		fbpProxyDao.setFbpProxySBConnector(fbpProxySBConnector);

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> result = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);

		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testRetrieveDLMDataWithNullResult() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);
		when(mockFBPProxySBConnector.invokeDLMData(anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(null);

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> results = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);

		Assert.assertNotNull(results);
		Assert.assertTrue(results.isEmpty());
	}

	@Test
	public void testRetrieveDLMDataWithEmptyResult() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);

		DLMData dlmData = new DLMData();
		when(mockFBPProxySBConnector.invokeDLMData(anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> results = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);

		Assert.assertTrue(results.isEmpty());
	}

	@Test
	public void testRetrieveDLMDataWithMockResult() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);

		mockInvokeDLMData(mockFBPProxySBConnector, "2005-03-17T11:39:02.353-06:00", "2005-03-17T11:39:02");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> results = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);
		FBPProxyBO fbpProxBO = results.get(0);

		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertNull(fbpProxBO.getCommentsandReq());
		Assert.assertNull(fbpProxBO.getCreditActionDesc());
		Assert.assertNull(fbpProxBO.getLoanApprovalOffical());
		Assert.assertNull(fbpProxBO.getLoanApprovalTitle());
		Assert.assertNotNull(fbpProxBO.getCreditActionCreationDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionID());

	}

	@Test
	public void testRetrieveDLMDataWithMockResult_02() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);

		mockInvokeDLMData(mockFBPProxySBConnector, "2005-03-17T15:04:51-06:00", "Mar 17 2005  2:57PM");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> results = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);
		FBPProxyBO fbpProxBO = results.get(0);

		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertNull(fbpProxBO.getCommentsandReq());
		Assert.assertNull(fbpProxBO.getCreditActionDesc());
		Assert.assertNull(fbpProxBO.getLoanApprovalOffical());
		Assert.assertNull(fbpProxBO.getLoanApprovalTitle());
		Assert.assertNotNull(fbpProxBO.getCreditActionCreationDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionID());

	}

	@Test
	public void testRetrieveDLMDataWithMockResult_03() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);

		mockInvokeDLMData(mockFBPProxySBConnector, "Jul 27 2005  4:39PM", "2005-03-17T11:39:02.353-06:00");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<FBPProxyBO> results = fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);
		FBPProxyBO fbpProxBO = results.get(0);

		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertNull(fbpProxBO.getCommentsandReq());
		Assert.assertNull(fbpProxBO.getCreditActionDesc());
		Assert.assertNull(fbpProxBO.getLoanApprovalOffical());
		Assert.assertNull(fbpProxBO.getLoanApprovalTitle());
		Assert.assertNotNull(fbpProxBO.getCreditActionCreationDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionDate());
		Assert.assertNotNull(fbpProxBO.getCreditActionID());

	}

	@Test
	public void test_getYEACreditActions_format_01() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);

		mockGetDLMYEAMessageElement(mockFBPProxySBConnector, "2005-03-17T11:39:02", "Mar 17 2005  2:57PM",
				"2005-03-17T11:39:02.353-06:00");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<CreditActionBO> results = fbpProxyDao.getYEACreditActions(passwdtext, corecustomerid);

		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());

		CreditActionBO creditActionBO = results.get(0);

		Assert.assertNotNull(creditActionBO.getCreditActionDate());
		Assert.assertNotNull(creditActionBO.getCreditActionCreationDate());
	}

	@Test
	public void test_getYEACreditActions() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);
		mockGetDLMYEAMessageElement(mockFBPProxySBConnector, "2005-03-17T11:39:02", "2005-03-17T11:39:02.353-06:00",
				"Mar 17 2005  2:57PM");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<CreditActionBO> results = fbpProxyDao.getYEACreditActions(passwdtext, corecustomerid);

		Assert.assertNotNull(results);
		Assert.assertFalse(results.isEmpty());

		CreditActionBO creditActionBO = results.get(0);

		Assert.assertNotNull(creditActionBO.getCreditActionDate());
		Assert.assertNotNull(creditActionBO.getCreditActionCreationDate());
		Assert.assertNotNull(creditActionBO.getScoreDate());

	}

	@Test
	public void test_getYEACreditActions_invalidFormat() throws Exception {
		testJndiEnvironment();
		FBPProxySBConnector mockFBPProxySBConnector = mock(FBPProxySBConnector.class);
		fbpProxyDao.setFbpProxySBConnector(mockFBPProxySBConnector);
		mockGetDLMYEAMessageElement(mockFBPProxySBConnector, "T2011-0500-1500 12:23:38", "T2011-0500-1500 12:23:38",
				"T2011-0500-1500 12:23:38");

		String passwdtext = "";
		Integer corecustomerid = 1;

		List<CreditActionBO> results = fbpProxyDao.getYEACreditActions(passwdtext, corecustomerid);

		Assert.assertTrue(results.get(0).getCreditActionCreationDate()==null);
	}

	@Test
	public void test_isHealthy_succeeded() throws Exception {
		testJndiEnvironment();

		when(mockFbpProxySBConnector.isHealthy()).thenReturn(Boolean.TRUE);

		Assert.assertTrue(fbpProxyDao.isHealthy());
	}

	@Test
	public void test_isHealthy_failed() throws Exception {
		testJndiEnvironment();
		fbpProxyDao.setFbpProxySBConnector(null);
		fbpProxyDao.setAgencyToken(createAgencyToken());

		Assert.assertFalse(fbpProxyDao.isHealthy());
	}

	private void mockInvokeDLMData(FBPProxySBConnector mockFBPProxySBConnector, String createActionDate,
			String createActionCreatedDate) throws Exception {
		DLMData dlmData = new DLMData();
		DLMRecord dlmRecord = new DLMRecord();
		dlmRecord.setCreditActionID("12345");
		dlmRecord.setCreditActionDate(createActionDate);
		dlmRecord.setCreditActionCreationDate(createActionCreatedDate);
		List<DLMRecord> listOfDLMRecords = new ArrayList<DLMRecord>();
		listOfDLMRecords.add(dlmRecord);
		dlmData.setListOfDLMRecords(listOfDLMRecords);

		when(mockFBPProxySBConnector.invokeDLMData(anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

	}

	private void mockGetDLMYEAMessageElement(FBPProxySBConnector mockFBPProxySBConnector, String createActionDate,
			String createActionCreatedDate, String scoredDate) throws FBPServiceBrokerException {
		DLMData dlmData = new DLMData();
		DLMRecord dlmRecord = new DLMRecord();
		dlmRecord.setCreditActionID("12345");
		dlmRecord.setCreditActionDate(createActionDate);
		dlmRecord.setCreditActionCreationDate(createActionCreatedDate);
		dlmRecord.setScoreDate(scoredDate);
		List<DLMRecord> listOfDLMRecords = new ArrayList<DLMRecord>();
		listOfDLMRecords.add(dlmRecord);
		dlmData.setListOfDLMRecords(listOfDLMRecords);

		when(mockFBPProxySBConnector.getDLMYEAMessageElement(anyString(), anyString(), anyString(), anyString(),
				anyInt())).thenReturn(dlmData);

	}
}
