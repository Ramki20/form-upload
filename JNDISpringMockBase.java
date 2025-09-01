package gov.usda.fsa.fcao.flp.flpids.scims.base;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockejb.jndi.MockContextFactory;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import gov.usda.fsa.eas.auth.AuthorizationManagerImpl;
import gov.usda.fsa.fcao.flp.flpids.util.JNDIMockBase;
import junit.framework.Assert;

public class JNDISpringMockBase {
	protected static SimpleNamingContextBuilder contextBuilder;
	protected static final String PARMO_SCIMS_SOAP_WSDL_URL_VALUE =	"http://int1-internal-services.fsa.usda.gov/SCIMS/services/ScimsWebService";
	
	
	protected AuthorizationManagerImpl mockAuthorizationManager;
	
	UserTransaction mockUserTransaction = null;
//
//	@BeforeClass
//	public static void classScopeSetup() throws Exception {
//		initializeScimsJNDI();
//	}

	@Before
	public void setUp() throws Exception {
		MockContextFactory.setAsInitial();
		Context context = new InitialContext();
		context.createSubcontext("java:comp");
		context.createSubcontext("java:comp/env");
		context.createSubcontext("java:comp/env/cell");
		context.createSubcontext("cell");
		
		Context subContext =context.createSubcontext("cell/persistent");
		subContext.createSubcontext("java:comp");
		subContext.createSubcontext("java:comp/env");
		
		context.rebind("java:comp/env/name_space_root","cell/persistent");
		context.rebind("java:comp/env/scims_soap_url","gov/usda/common/scims_soap_wsdl_url");
		subContext.createSubcontext("gov");
		subContext.createSubcontext("gov/usda");
		subContext.createSubcontext("gov/usda/common");
		subContext.rebind("gov/usda/common/scims_soap_wsdl_url",PARMO_SCIMS_SOAP_WSDL_URL_VALUE);
		subContext.rebind("java:comp/env/name_space_root","cell/persistent");
		
		
		context.rebind(JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_KEY, 
				JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_VALUE);
		
		context.rebind("cell/persistent/gov/usda/common/scims_client_log_file","scimsLog.log");
		context.rebind("cell/persistent/gov/usda/common/scims_client_log_level","ERROR");
		context.rebind("cell/persistent/gov/usda/common/scims_service_customer_id_limit","3");
		
		context.rebind("cell/persistent/gov/usda/common/scims_service_specifier", "WS");
		context.rebind("cell/persistent/" + JNDIMockBase.PARMO_SCIMS_URL_KEY, PARMO_SCIMS_SOAP_WSDL_URL_VALUE);
		context.rebind("cell/persistent/gov/usda/common/SCIMS_Shared_service_rpt","http://int1-internal-services.fsa.usda.gov/SCIMSRPT/services/ScimsRptWebService");
		
		context.rebind("cell/persistent/gov/usda/common/SCIMS_Shared_service","http://int1-internal-services.fsa.usda.gov/SCIMS/services/ScimsWebService");
		context.rebind("cell/persistent/gov/usda/fsa/common/citso/cbs/sharedservice_specifier", "WS");
		context.bind("cell/persistent/gov/usda/fsa/common/citso/cbs-surrogate/sharedservice_specifier", "WS");
		

		context.bind("cell/persistent/gov/usda/fsa/common/scims-services_type_specifier", "WS");
		context.bind("cell/persistent/gov/usda/fsa/common/scims-services_ws_endpoint_url",
				"https://int1fcp-internal-services-fsa.fpac.usda.gov/scims-services/services/SCIMSService");
		
		context.bind("gov/usda/fsa/fcao/flp/scims_default_value_unknown", "Y");		
		context.bind("cell/persistent/gov/usda/fsa/fcao/flp/scims_default_value_unknown", "Y");		
	}

	@After
	public void cleanUp() throws Exception {
		MockContextFactory.revertSetAsInitial();
	}

	@Test
	public void testJNDICfg () throws Exception{
		Context context = new InitialContext();
		String root = (String) context.lookup("java:comp/env/name_space_root");
		Assert.assertNotNull(root);
		Assert.assertEquals("cell/persistent", root);

		String scimsWsdlUrlJNDI = (String) context.lookup("java:comp/env/scims_soap_url");
		Assert.assertNotNull(scimsWsdlUrlJNDI);
		Assert.assertEquals("gov/usda/common/scims_soap_wsdl_url", scimsWsdlUrlJNDI);
		
		Context globalNames = (Context) context.lookup(root);
		String scimsWsdlUrl = (String)globalNames.lookup(scimsWsdlUrlJNDI);
		
		Assert.assertNotNull(scimsWsdlUrl);
		Assert.assertEquals(PARMO_SCIMS_SOAP_WSDL_URL_VALUE, scimsWsdlUrl);
		
	}
//	private static void initializeScimsJNDI() throws Exception {
//		contextBuilder = SimpleNamingContextBuilder
//				.emptyActivatedContextBuilder();
//		contextBuilder.bind("java:comp/env/name_space_root", "cell/persistent");
//		contextBuilder.bind("java:comp/env/scims_soap_url",
//				"gov/usda/common/scims_soap_wsdl_url");
//		contextBuilder
//				.bind("cell/persistent/gov/usda/common/scims_soap_wsdl_url",
//						PARMO_SCIMS_SOAP_WSDL_URL_VALUE);
//
//		contextBuilder.bind(JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_KEY, 
//				JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_VALUE);
//		
//		contextBuilder.bind("cell/persistent/gov/usda/common/scims_client_log_file","scimsLog.log");
//		contextBuilder.bind("cell/persistent/gov/usda/common/scims_client_log_level","ERROR");
//		contextBuilder.bind("cell/persistent/gov/usda/common/scims_service_customer_id_limit","3");
//		
//		contextBuilder.bind("cell/persistent/gov/usda/common/scims_service_specifier", "WS");
//		contextBuilder.bind("cell/persistent/" + JNDIMockBase.PARMO_SCIMS_URL_KEY, PARMO_SCIMS_SOAP_WSDL_URL_VALUE);
//		contextBuilder.bind("cell/persistent/gov/usda/common/SCIMS_Shared_service_rpt","http://int1-internal-services.fsa.usda.gov/SCIMS/services/ScimsRptWebService");
//		
//		
//		Context context = new InitialContext();
//		String root = (String) context.lookup("java:comp/env/name_space_root");
//		Assert.assertNotNull(root);
//		Assert.assertEquals("cell/persistent", root);
//
//		String scimsWsdlUrlJNDI = (String) context.lookup("java:comp/env/scims_soap_url");
//		Assert.assertNotNull(scimsWsdlUrlJNDI);
//		Assert.assertEquals("gov/usda/common/scims_soap_wsdl_url", scimsWsdlUrlJNDI);
//		
//		Context globalNames = (Context) context.lookup(root);
//		String scimsWsdlUrl = (String)globalNames.lookup(scimsWsdlUrlJNDI);
//		
//		Assert.assertNotNull(scimsWsdlUrl);
//		Assert.assertEquals(PARMO_SCIMS_SOAP_WSDL_URL_VALUE, scimsWsdlUrl);
//	}
	protected String getJNDIStringValue(Context context, String key){
		String value = null;
		try{
			value = (String)context.lookup(key);
		}catch(Exception e){
			
		}
		return value;
	}
}
