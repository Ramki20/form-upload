package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import gov.usda.fsa.common.base.AgencyToken;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.mockejb.MockContainer;
import org.mockejb.jndi.MockContextFactory;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

public class JndiMockTestBase {
	protected Context context;
	protected MockContainer mockContainer; 
	private static final Logger logger = LogManager
			.getLogger(JndiMockTestBase.class);

	private static JndiMockTestBase INSTANCE;

	@BeforeClass
	public static void classSetUp() throws Exception {
		INSTANCE = new JndiMockTestBase();
		INSTANCE.initializeMockEjb();
	}

	@AfterClass
	public static void classCleanUp() throws Exception {
		INSTANCE.clearContext(INSTANCE.getContext());
		INSTANCE.resetContext();
		MockContextFactory.revertSetAsInitial();
	}

	private void initializeMockEjb() throws Exception {
		MockContextFactory.setAsInitial();
		context = new InitialContext();
		initializeJndiSettings();		
	}

	protected void initializeJndiSettings() throws Exception {
		try {
			// MockContextFactory.setAsInitial();
			// InitialContext context = new InitialContext();
			 context.createSubcontext("java:comp");
			 context.createSubcontext("env");
			context.rebind("java:comp/env/name_space_root", "cell/persistent");

			

			context
					.rebind(
							"cell/persistent/gov/usda/fsa/fcao/flpids/DLS/batch/ejb/endPointUrl",
							"https://gls.sc.egov.usda.gov/");

			context.rebind(
					"cell/persistent/gov/usda/fsa/fcao/flpids/DLS/logLocation",
					"C:\\ClientEarLocation\\logs\\dlsBatchClient.log");

			context
					.rebind(
							"cell/persistent/gov/usda/fsa/fcao/flpids/DLS/batch/ejb/clientLocation",
							"C:\\ClientEarLocation");

			context.rebind("cell/persistent/gov/usda/common/environment",
					"local");

			context
					.rebind(
							"cell/persistent/gov/usda/fsa/fcao/flpids/DLS/batch/clientFileName",
							"LoanServicingBatchClientEAR.ear");

			context
					.rebind(
							"cell/persistent/gov/usda/fsa/fcao/flpids/DLS/batch/launchClientScriptLocation",
							"C:\\USDADEV\\IBM\\SDP70\\runtimes\\base_v61\\bin\\launchClient.bat");

			// Ejb
			context
					.rebind(
							"ejb/gov/usda/fsa/fcao/flp/ls/beans/ejb/AutomationService",
							"ejb/gov/usda/fsa/fcao/flp/ls/beans/ejb/AutomationServiceLocal");
			// datasource
			SQLServerDataSource datasource = new SQLServerDataSource(); 
			context
					.rebind("java:comp/env/jdbc/OZ.MSSQL.FLPIDS_DLS",
							datasource);

			// test
			String location = (String) context
					.lookup("cell/persistent/gov/usda/fsa/fcao/flpids/DLS/batch/launchClientScriptLocation");
			Assert.assertNotNull(location);

		} catch (NamingException ex) {
			//LogManager.getLogger(JndiMockTestBase.class.getName()).log(Level.DEBUG,
					//null, ex);
			
			logger.error(ex);
			ex.printStackTrace();
		}
	}

	public JndiMockTestBase() {
		initialization();
	}

	private void initialization() {
		if (INSTANCE != null) {
			this.context = INSTANCE.context;
			this.mockContainer = INSTANCE.mockContainer;
		}
	}

	protected AgencyToken getAgencyToken() {
		AgencyToken token = new AgencyToken();
		token.setApplicationIdentifier("FLP/LS_Batch");
		token.setUserIdentifier("LS-Batch TEST USER");
		try {
			token.setProcessingNode(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			token.setProcessingNode("NodeNameNotAvailable");
		}
		token.setRequestHost("localhost");
		token.setReadOnly();
		return token;
	}

	@SuppressWarnings("rawtypes")
	protected void clearContext(Context context) throws NamingException {
		if (context != null) {
			for (NamingEnumeration e = context.listBindings(""); e
					.hasMoreElements();) {
				Binding binding = (Binding) e.nextElement();
				if (binding.getObject() instanceof Context) {
					clearContext((Context) binding.getObject());
				}
				context.unbind(binding.getName());
			}
		}
	}

	protected Context getContext() {
		return this.context;
	}

	protected void resetContext() {
		this.context = null;
	}
}
