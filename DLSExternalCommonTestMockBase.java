package gov.usda.fsa.fcao.flp.flpids.common.business.common;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

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

        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/service_endpoint_url", "https://wem.cert.sc.egov.usda.gov/gateway/FBPService.asmx");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/web_interface_url", "https://wem.cert.sc.egov.usda.gov/gateway/FBPGeneral.aspx");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/web_locator_interface_url", "https://wem.cert.sc.egov.usda.gov/fem_web_webcaaf.aspx");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/site_name", "test1.onlinequity.com");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/user", "FBP_FLPIDS_SOAP");

        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/password", "Vm0xMFQyUXlWblJWYTJocFUwWmFhRnBYTVdwUFVUMDk=");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/password_text", "T*sting8");
        subContext.bind("gov/usda/fsa/fcao/flpids/fbp/ws/pwd_digest", "0F78E6E616FF684D544387C0F5DC1DDB");



        subContext.bind("java:comp/env/application_identifier", "cbs-client");
        subContext.bind("gov/usda/fsa/common/citso/cbs-surrogate/web_service_endpoint_url",
        		"https://int1-internal-services.fsa.usda.gov/cbs-surrogate-ejb/services/CommonBusinessSurrogateServicePort?wsdl");
        subContext.bind("gov/usda/fsa/common/citso/cbs/web_service_endpoint_url ",
        		"http://int1-internal-services.fsa.usda.gov/cbs-ejb/services/CommonBusinessDataServicePort?wsdl");

        subContext.bind("gov/usda/fsa/common/citso/cbs/sharedservice_specifier", "WS");
        subContext.bind("gov/usda/fsa/common/citso/cbs-surrogate/sharedservice_specifier", "WS");

        subContext.bind("gov/usda/fsa/common/cmbs-shared-service_ws_endpoint_url", "http://int1-internal-services.fsa.usda.gov/cmbs-shared-service/services/ContentManagementBusinessService");
        subContext.bind("gov/usda/fsa/common/cmbs-shared-service_type_specifier", "WS");

        subContext.bind("gov/usda/fsa/common/nrrs-receivable/nrrs-shared-service_type_specifier", "WS");
//        subContext.bind("gov/usda/fsa/common/nrrs-receivable/nrrs-shared-service_ws_endpoint_url", "https://int1-internal-services.fsa.usda.gov/nrrs-shared-service/services/ReceivableService");

        ctx.bind("java:comp/env/application_identifier", "cbs-client");
        ctx.bind("cell/persistent/gov/usda/fsa/common/citso/cbs-surrogate/web_service_endpoint_url",
        		"http://int1-internal-services.fsa.usda.gov/cbs-surrogate-ejb/services/CommonBusinessSurrogateServicePort?wsdl");

        ctx.bind("cell/persistent/gov/usda/fsa/common/citso/cbs/web_service_endpoint_url",
        		"http://int1-internal-services.fsa.usda.gov/cbs-ejb/services/CommonBusinessDataServicePort?wsdl");
        ctx.bind("cell/persistent/gov/usda/fsa/common/citso/cbs/sharedservice_specifier", "WS");
        ctx.bind("cell/persistent/gov/usda/fsa/common/citso/cbs-surrogate/sharedservice_specifier", "WS");


        ctx.bind("cell/persistent/gov/usda/fsa/common/frs_service_specifier", "WS");
        subContext.bind("gov/usda/fsa/common/frs_service_specifier", "WS");
        ctx.bind("java:comp/env/gov/usda/fsa/common/frs_service_specifier", "WS");
        subContext.bind("java:comp/env/gov/fsa/usda/common/frs_service_specifier", "WS");

       // ctx.bind("cell/persistent/gov/usda/fpac/ciss/crm/fr/client/crm-fr-ws-url", "https://miintpi.fsa.usda.gov/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_CRMFRSHARED_SERVICE&receiverParty=&receiverService=&interface=SIIOS_CRMFR_SharedService&interfaceNamespace=urn://midas.usda.gov/FR/I-026/CRMFRSharedService");
        //subContext.bind("gov/usda/fpac/ciss/crm/fr/client/crm-fr-ws-url", "https://miintpi.fsa.usda.gov/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_CRMFRSHARED_SERVICE&receiverParty=&receiverService=&interface=SIIOS_CRMFR_SharedService&interfaceNamespace=urn://midas.usda.gov/FR/I-026/CRMFRSharedService");

        
        ctx.bind("cell/persistent/gov/usda/fsa/common/farm_Records_URL", "https://miintpi.fsa.usda.gov/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_CRMFRSHARED_SERVICE&receiverParty=&receiverService=&interface=SIIOS_CRMFR_SharedService&interfaceNamespace=urn://midas.usda.gov/FR/I-026/CRMFRSharedService");
        subContext.bind("gov/usda/fsa/common/farm_Records_URL", "https://miintpi.fsa.usda.gov/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_CRMFRSHARED_SERVICE&receiverParty=&receiverService=&interface=SIIOS_CRMFR_SharedService&interfaceNamespace=urn://midas.usda.gov/FR/I-026/CRMFRSharedService");
        
        //ctx.bind("gov/usda/common/nps_end_point_url", "https://int1-internal-services.fsa.usda.gov/Payments/WebService");
        ctx.bind("cell/persistent/gov/usda/common/nps_end_point_url", "https://int1-internal-services.fsa.usda.gov/Payments/WebService");

        // with cell/persistent
        // subContext.bind("gov/usda/dls-fsfl/nrrs_external_service_type", "WS");

        ctx.bind("cell/persistent/gov/usda/fsa/fcao/flpids/common/nrrs_external_service_type", "WS");
        ctx.bind("cell/persistent/gov/usda/fsa/fcao/flpids/common/nrrs_collection_service_type", "WS");

        ctx.bind("cell/persistent/gov/usda/fsa/nrrs/nrrscollectionservice_url", "http://int1-internal-services.fsa.usda.gov/NRRSService/services/NRRSCollectionService");
        ctx.bind("cell/persistent/gov/usda/fsa/common/escoapsharedservice_url", "http://int1-internal-services.fsa.usda.gov/escoap-shared-service-ws/services/ESCOAPTransactionWebService");

        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL("jdbc:sqlserver://165.221.206.238:1433;DatabaseName=mrtxdb_intg");
        ds.setUser("flpids_cp_readonly");
        ds.setPassword("FLP!cp$2005!");
        ds.setEncrypt(Boolean.FALSE);
        ctx.rebind("java:comp/env/jdbc/CBSDatasource", ds);

        builder.bind("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_create_scims_customer","Y");
        builder.bind("gov/usda/fsa/fcao/flp/dls/support_create_scims_customer","Y");
        //builder.bind("https.protocols", "TLSv1.2");
        /**
		 * fixing https security issue. Make sure run the UT with jdk 1.7
		 *
		 */
				//System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
		//	System.setProperty("https.protocols", "TLSv1.2");
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

