package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.JNDILookup;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMRecord;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPConnector;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPWsdlInfo;
import junit.framework.Assert;

public class FBPProxySBConnector_UT extends DLSExternalCommonTestMockBase {
	private FBPProxySBConnector connector;
	private ServiceAgentFacade serviceAgentFacade;
	private FBPConnector fpbConnector = null;

	private AgencyEncryption agencyEncryption;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		ServiceAgentFacade.setLAZYLOADING(true);
		serviceAgentFacade = ServiceAgentFacade.getInstance();
		ServiceAgentFacade.getInstance().getFbpProxyBusinessService();
		ApplicationContext context = serviceAgentFacade.getApplicationContextFBP();
		connector = (FBPProxySBConnector) context.getBean("fbpProxySBConnector");
		fpbConnector = mock(FBPConnector.class);
		agencyEncryption = (AgencyEncryption) context.getBean("agencyEncryption");
		ServiceAgentFacade.setLAZYLOADING(false);
	}

	@Test
	public void test_Security_getProvider() throws Exception {
		Provider[] providers = Security.getProviders();
		for (Provider item : providers) {
			Set<String> properties = item.stringPropertyNames();
			System.out.println("*********************");
			System.out.println("All properteis for Provider: " + item.getName());
			for (String property : properties) {
				System.out.println("Provider property " + property);
			}
		}
	}

	@Test
	public void testInvokeDLMMain() throws Exception {

		DLMData dlmData = new DLMData();
		DLMRecord dlmRecord = new DLMRecord();
		dlmRecord.setCreditActionID("12345");
		dlmRecord.setCreditActionDate("T2011-05-15 12:23:38");
		dlmRecord.setCreditActionCreationDate("T2011-05-15 12:23:38");
		List<DLMRecord> listOfDLMRecords = new ArrayList<DLMRecord>();
		listOfDLMRecords.add(dlmRecord);
		dlmData.setListOfDLMRecords(listOfDLMRecords);

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

		connector.setFbpConnector(fpbConnector);

		Assert.assertNotNull(connector.invokeDLMMain());
	}

	@Test
	public void testInvokeDLMDataWithNullGetDLMDataResult_mockProxy() throws Exception {
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwdtext = agencyEncryption
				.decode5(JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_TEXT_JNDI));
		String passwddigest = JNDILookup
				.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);
		Integer corecustomerid = 1232;

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(null);

		connector.setFbpConnector(fpbConnector);

		DLMData dlimData = connector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		Assert.assertNull(dlimData);
	}

	@Test
	public void testInvokeDLMDataWithNullGetDLMDataResult() throws Exception {
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwdtext = agencyEncryption
				.decode5(JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_TEXT_JNDI));
		String passwddigest = JNDILookup
				.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);
		// String serviceEndURL =
		// JNDILookup.commonFSADirectLookup(DLSConstants.FBP_INTERFACE_CONTEXT_JNDI);

		Integer corecustomerid = 1232;
		// fbpSoapProxy = new FBPServiceSoapProxy(serviceEndURL);
		// connector.setFbpSoapProxy(fbpSoapProxy);

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(null);

		connector.setFbpConnector(fpbConnector);

		DLMData dlmData = connector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		Assert.assertNull(dlmData);
	}

	@Test
	public void testInvokeDLMData_withResult() throws Exception {
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwdtext = agencyEncryption
				.decode5(JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_TEXT_JNDI));
		String passwddigest = JNDILookup
				.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);
		// String serviceEndURL =
		// JNDILookup.commonFSADirectLookup(DLSConstants.FBP_INTERFACE_CONTEXT_JNDI);

		Integer corecustomerid = 8435018;
		// fbpSoapProxy = new FBPServiceSoapProxy(serviceEndURL);
		// connector.setFbpSoapProxy(fbpSoapProxy);

		DLMData dlmData = new DLMData();
		DLMRecord dlmRecord = new DLMRecord();
		dlmRecord.setCreditActionID("12345");
		dlmRecord.setCreditActionDate("T2011-05-15 12:23:38");
		dlmRecord.setCreditActionCreationDate("T2011-05-15 12:23:38");
		List<DLMRecord> listOfDLMRecords = new ArrayList<DLMRecord>();
		listOfDLMRecords.add(dlmRecord);
		dlmData.setListOfDLMRecords(listOfDLMRecords);

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

		connector.setFbpConnector(fpbConnector);

		dlmData = connector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		Assert.assertNotNull(dlmData);
	}

	@Test
	public void testInvokeDLMDataWithEmptyGetDLMDataResult() throws Exception {
		String sitename = "locahost";
		String username = "tester";
		String passwdtext = "password";
		String passwddigest = "312sfwerwer";
		Integer corecustomerid = 1232;

		DLMData dlmData = new DLMData();

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

		connector.setFbpConnector(fpbConnector);

		DLMData result = connector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		Assert.assertNull(result.getListOfDLMRecords());
	}

	@Test
	public void testInvokeDLMDataWithNoneEmptyGetDLMDataResult() throws Exception {
		String sitename = "locahost";
		String username = "tester";
		String passwdtext = "password";
		String passwddigest = "312sfwerwer";
		Integer corecustomerid = 1232;

		DLMData dlmData = new DLMData();
		DLMRecord dlmRecord = new DLMRecord();
		dlmRecord.setCreditActionID("12345");
		dlmRecord.setCreditActionDate("T2011-05-15 12:23:38");
		dlmRecord.setCreditActionCreationDate("T2011-05-15 12:23:38");
		List<DLMRecord> listOfDLMRecords = new ArrayList<DLMRecord>();
		listOfDLMRecords.add(dlmRecord);
		dlmData.setListOfDLMRecords(listOfDLMRecords);

		when(fpbConnector.getDLMData(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
				.thenReturn(dlmData);

		connector.setFbpConnector(fpbConnector);

		DLMData result = connector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		Assert.assertNotNull(result);
	}
}