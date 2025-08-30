package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import javax.naming.Context;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JNDIUtil {
    public static final String JNDI_NAME_SPACE = "java:comp/env/name_space_root";
    
	private static final Logger logger = LogManager.getLogger(JNDIUtil.class);
    

    public static String lookUp(String jndiURL) {
		String returnURL = "";
		if (isEmptyString(jndiURL)) {
			return "";
		}
		try {
			javax.naming.InitialContext ctx = new javax.naming.InitialContext();			
			String nameSpaceRoot = (String) ctx.lookup(JNDI_NAME_SPACE);
			Context globalNames = (Context) ctx.lookup(nameSpaceRoot);
			jndiURL = jndiURL.trim();
			returnURL = (String) globalNames.lookup(jndiURL);
		} catch (Exception e) {
			logger.warn("***lookUp():Error during JNDI lookup***:"+e.getMessage());
		}
		return returnURL;
    }
	
	private static boolean isEmptyString(String s)
	{
		return ((s == null) || "".equals(s.trim()));
	}
    
	
}
