/*
 * Created on Dec 13, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.JNDILookup;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DALRData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPConnector;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPConnectorException;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPWsdlInfo;

/**
 * @author sivagurunathan.nagar
 * 
 *         Invokes the webservice through java proxy class
 */
public class FBPProxySBConnector {
	private static final Logger logger = LogManager.getLogger(FBPProxySBConnector.class);
	private FBPConnector fbpConnector = null;

	public DALRData getDALRData(String sitename, String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPServiceBrokerException {
		DALRData daLRData = null;
		String fbpEndPointURL = null;
		try {
			fbpEndPointURL = JNDILookup
					.commonFSADirectLookup(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI);
			daLRData = fbpConnector.getDALRData(fbpEndPointURL, sitename, username, passwdtext, passwddigest,
					corecustomerid);
		} catch (FBPConnectorException ex) {
			logger.error("FBPConnectorException for fbpEndPointURL=" + fbpEndPointURL + " sitename=" + sitename + " "
					+ "username=" + username + " corecustomerid=" + corecustomerid);
			logger.error("FBPConnectorException when calling the proxy class from FBPProxySBConnector.getDALRData()",
					ex);
			throw new FBPServiceBrokerException(
					"FBPConnectorException when calling the proxy class from FBPProxySBConnector.getDALRData() : "
							+ ex.getMessage());
		} catch (Exception e) {
			logger.error(
					"Unknown Exception when calling getDALRData() viae proxy FBPProxySBConnector.getDALRData()..for CCID: "
							+ corecustomerid,
					e);
		}
		return daLRData;
	}

	public DLMData invokeDLMData(String sitename, String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPServiceBrokerException {
		DLMData dlmData = null;
		try {
			/* sets the URL Endpoint from the property file */
			String fbpEndPointURL = JNDILookup
					.commonFSADirectLookup(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI);
			dlmData = fbpConnector.getDLMData(fbpEndPointURL, sitename, username, passwdtext, passwddigest,
					corecustomerid);
		} catch (FBPConnectorException ex) {
			logger.error("FBPConnectorException when calling the proxy class from FBPProxySBConnector.invokeDLMData()",
					ex);
			throw new FBPServiceBrokerException(
					"FBPConnectorException when calling the proxy class from FBPProxySBConnector.invokeDLMData() : "
							+ ex.getMessage());
		} catch (Exception e) {
			logger.error(
					"Unknown Exception when calling invokeDLMData() viae proxy FBPProxySBConnector.invokeDLMData() for CCID: "
							+ corecustomerid,
					e);
		}
		return dlmData;
	}

	public DLMData getDLMYEAMessageElement(String sitename, String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPServiceBrokerException {
		DLMData dlmData = null;
		try {
			String fbpEndPointURL = JNDILookup
					.commonFSADirectLookup(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI);
			dlmData = fbpConnector.getDLMYEAData(fbpEndPointURL, sitename, username, passwdtext, passwddigest,
					corecustomerid);
		} catch (FBPConnectorException ex) {
			logger.error("FBPConnectorException when calling the proxy class from FBPProxySBConnector.getDLMYEAData()",
					ex);
			throw new FBPServiceBrokerException(
					"FBPConnectorException when calling the proxy class from FBPProxySBConnector.getDLMYEAData() : "
							+ ex.getMessage());
		} catch (Exception e) {
			logger.error(
					"Unknown Exception when calling getDLMYEAMessageElement() via proxy FBPProxySBConnector.invokeDLMData() for CCID: "
							+ corecustomerid,
					e);
		}
		return dlmData;
	}

	public DLMData getCreditActionsMessageElement(String sitename, String username, String passwdtext,
			String passwddigest, Integer corecustomerid) throws FBPServiceBrokerException {
		return getDLMYEAMessageElement(sitename, username, passwdtext, passwddigest, corecustomerid);
	}

	protected DLMData invokeDLMMain() throws FBPServiceBrokerException {
		Integer a1 = null;
		String a2 = "8435018";
		a1 = Integer.valueOf(a2);
		DLMData dlmData = null;
		try {
			dlmData = invokeDLMData("test1.onlinequity.com", FBPWsdlInfo.FBP_USER_NAME,
					AgencyEncryption.getInstance()
							.decode5(JNDILookup
									.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_TEXT_JNDI)),
					"0F78E6E616FF684D544387C0F5DC1DDB", a1);
			logger.debug("Result" + dlmData);
		} catch (java.io.IOException e) {
			logger.debug("Failed to encrypt FBP Password");
		}
		return dlmData;
	}

	public void setFbpConnector(FBPConnector fbpConnector) {
		this.fbpConnector = fbpConnector;
	}

	public boolean isHealthy() {
		return (fbpConnector != null && fbpConnector.isHealthy());
	}
}
