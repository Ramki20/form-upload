package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author FCAO
 * 
 * Manually created class
 *
 */
public class FBPWsdlInfo {

	public static final String FBP_NAMESPACE_URI = "http://schemas.eci-equity.com/2005/FBPService/";
	public static final String FBP_NAMESPACE_PREFIX = "fbp";
	
	public static final String FBP_SERVICE_NAME = "FBPService";
	public static final String FBP_SERVICE_PORT_NAME = "FBPServiceSoap";
	
	public static final String FBP_SITENAME = "test1.onlinequity.com";
	public static final String FBP_SITENAME_JNDI ="gov/usda/fsa/fcao/flpids/fbp/ws/site_name";
	
	public static final String FBP_USER_NAME = "FBP_FLPIDS_SOAP";
	public static final String FBP_USER_NAME_JNDI = "gov/usda/fsa/fcao/flpids/fbp/ws/user";
		
	public static final String FBP_PASSWORD_TEXT_JNDI = "gov/usda/fsa/fcao/flpids/fbp/ws/password";
	public static final String FBP_PASSWORD_DIGEST_JNDI = "gov/usda/fsa/fcao/flpids/fbp/ws/pwd_digest";
	
	public static final String FBP_INTERFACE_CONTEXT_JNDI ="gov/usda/fsa/fcao/flpids/fbp/ws/service_endpoint_url";
	
	
	private static final Logger logger = LogManager.getLogger(FBPWsdlInfo.class);
	
    private URL wsdlLocation;
    
	public FBPWsdlInfo()  
	{
		
	}

	public FBPWsdlInfo(String fbpEndPointURL)  {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("FBPWsdlInfo - fbpEndPointURL="+fbpEndPointURL);
			}
			String wsdlURI = fbpEndPointURL;
			if(!fbpEndPointURL.endsWith("?wsdl")){
				wsdlURI = fbpEndPointURL + "?wsdl";
			}
			if(logger.isDebugEnabled()) {
				logger.debug("FBPWsdlInfo - wsdlURI="+wsdlURI);
			}
			wsdlLocation = new URL(wsdlURI);
		} 
		catch (Exception e) {
			logger.error("FBP WSDL location error:", e);
		}
	}

	public URL getWsdlLocation() {
		return wsdlLocation;
	}

	public void setWsdlLocation(URL wsdlLocation) {
		this.wsdlLocation = wsdlLocation;
	}
	
}
