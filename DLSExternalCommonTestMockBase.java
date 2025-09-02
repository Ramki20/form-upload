package gov.usda.fsa.fcao.flp.flpids.common.business.common;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;


public class DLSExternalCommonTestMockBase extends DLSExternalCommonTestAgency {
	protected static SimpleNamingContextBuilder builder;
	@Before
	public void setUp() throws Exception {
		test_jndiconfig();
	}
@BeforeClass
	public static void jndiSetup() throws Exception {
        builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        builder.bind("java:comp/env/name_space_root", "cell/persistent");
        builder.activate();
        InitialContext ctx = new InitialContext();
        builder.bind("cell/persistent", ctx);
        Context subContext = (Context)ctx.lookup("cell/persistent");

        subContext.bind("java:comp/env/application_identifier", "cbs-client");
        ctx.bind("java:comp/env/application_identifier", "cbs-client");
        
        subContext.bind("gov/usda/fsa/common/citso/cbs/sharedservice_specifier", "WS");
        subContext.bind("gov/usda/fsa/common/citso/cbs/web_service_endpoint_url ",
        		"http://int1-internal-services.fsa.usda.gov/cbs-ejb/services/CommonBusinessDataServicePort?wsdl");

        ctx.bind("cell/persistent/gov/usda/fsa/common/frs_service_specifier", "WS");
        subContext.bind("gov/usda/fsa/common/frs_service_specifier", "WS");
        ctx.bind("java:comp/env/gov/usda/fsa/common/frs_service_specifier", "WS");
        subContext.bind("java:comp/env/gov/fsa/usda/common/frs_service_specifier", "WS");

        builder.bind("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_create_scims_customer","Y");
        builder.bind("gov/usda/fsa/fcao/flp/dls/support_create_scims_customer","Y");
  }

	protected void test_jndiconfig() throws Exception {
		InitialContext ctx = new InitialContext();
		String contextRoot = (String) ctx
				.lookup("java:comp/env/name_space_root");
		Context subContext = (Context) ctx.lookup(contextRoot);
		Assert.assertNotNull(subContext);

		String value1 = (String) subContext
				.lookup("gov/usda/fsa/common/frs_service_specifier");
		String value2 = (String) ctx
				.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier");
		String value3 = (String) ctx
				.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier");

		Assert.assertNotNull(value1);
		Assert.assertNotNull(value2);
		Assert.assertNotNull(value3);
		Assert.assertTrue(value1.equals(value2));
		Assert.assertTrue(value2.equals(value3));
	}

}

