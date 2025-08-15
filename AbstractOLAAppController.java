package gov.usda.fsa.fcao.flp.ola.core.web.controller;

import java.util.ArrayList;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usda.fsa.ao.rest.common.security.EASAuthority;
import gov.usda.fsa.ao.rest.common.security.FSAUserDetails;
import gov.usda.fsa.ao.rest.common.security.IFSASecurityService;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.ola.core.api.IApplicationAPI;
import gov.usda.fsa.fcao.flp.ola.core.api.IAuthorizationAPI;
import gov.usda.fsa.fcao.flp.ola.core.api.model.OLACoreUserAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.OlaCoreAPIException;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.web.security.OLAUserDetailsService;

/**
 * Base Controller
 *
 */
@RestController
@RequestMapping("/api")
public abstract class AbstractOLAAppController {

	protected static final Logger log = LogManager.getLogger(AbstractOLAAppController.class);

	protected static final String NO_RESULTS_FOUND = "No results found.";
	protected static final String UNAUTHORIZED = "Not Authorized";
	protected static final String SUCESS = "Sucess";
	protected static final String MS = " milliseconds";
	public static final String USDASCIMS = "usdascims";
	public static final String USDAIAL = "usdaial";
	public static final String APP_ID = "OLA";// May Change
	public static final String NA = "NA";

	@Autowired
	//@Qualifier("agencyToken")
	protected AgencyToken agencyToken;
	
	@Autowired
	private IAuthorizationAPI authorizationAPI;
	
	@Autowired
	private IApplicationAPI applicationAPI;
	
	public AgencyToken getAgencyToken(HttpServletRequest request) {

		if (agencyToken != null && getEauthId(request) != null) {

			agencyToken.setUserIdentifier(getEauthId(request));
		}
		return agencyToken;
	}

	public void setAgencyToken(AgencyToken agencyToken) {
		this.agencyToken = agencyToken;
	}

	@Autowired
	protected OlaAgencyToken olaAgencyToken;

	public OlaAgencyToken getOlaAgencyToken(HttpServletRequest request) {


		if (olaAgencyToken != null) {

			olaAgencyToken.setApplicationIdentifier(APP_ID);
			olaAgencyToken.setProcessingNode(APP_ID);
			olaAgencyToken.setRequestHost(APP_ID);
		}

		String eAuthIdentifier = getEauthId(request);

		if (olaAgencyToken != null && eAuthIdentifier != null) {

			olaAgencyToken.setUserIdentifier(eAuthIdentifier);
		}else {
			
			UserDetails user=getCurrentUser( request);
			if(user!=null && user.getUsername()!=null) {
			olaAgencyToken.setUserIdentifier(user.getUsername());
			}else {
				olaAgencyToken.setUserIdentifier("NA");
			}
			
		}

		return olaAgencyToken;
	}

	public String getEauthId(HttpServletRequest request) {

		if (getFSAUserDetails(request) != null) {

			return getFSAUserDetails(request).getEAuthId();
		}
		return null;

	}

	public void setOlaAgencyToken(OlaAgencyToken olaAgencyToken) {

		this.olaAgencyToken = olaAgencyToken;
	}
	

	public void validateHeaders(Integer passedCCIDFromClient, Integer applicationId) {

		boolean validAccess = false;
		
		boolean isApplication = applicationAPI.doesApplicationExist(passedCCIDFromClient, applicationId);
			
		if(isApplication == true) {
			validAccess = true;
		}
		
		if(!validAccess)
		{
			String msg = "Unauthorized API access."
					+ " passedCCIDFromClient:"+passedCCIDFromClient
					+ " passedAppIdFromClient:"+applicationId; 
		    log.warn(msg);
			throw new OlaCoreAPIException("Unauthorized API access.");
		}
	}
	
	public void validateHeaders(HttpServletRequest request, Integer passedCCIDFromClient, Integer applicationId) {

		Integer eAuthCCID = getCurrentCoreCustomerIdentifier(request);
		String eAuthId = getEauthId(request);
		
		OLACoreUserAPIModel olaCoreUserModel = authorizationAPI.findOLACoreUserByEauth(eAuthId);
		boolean validAccess = false;
		
		if(applicationId == null) {

			// Customer can only access his/her data (Not another customer's data)
			if(olaCoreUserModel.getEasRoleList().isEmpty() && passedCCIDFromClient.equals(eAuthCCID))
			{
				validAccess = true;
			}
			else if(olaCoreUserModel.isServiceCenterRole() || olaCoreUserModel.isStateRole() 
			     || olaCoreUserModel.isNationalRole() || olaCoreUserModel.isItRole())
			{
				validAccess = true;
			}
		}
		else if(applicationId != null && passedCCIDFromClient == null){
			boolean isApplication = applicationAPI.doesApplicationExist(eAuthCCID, applicationId);
			
			if(isApplication == true) {
				validAccess = true;
			}
			
		}
		
		// below if makes all requests valid, bypassing this method. later, remove and fix method logic.
		if(request != null) {
			  validAccess = true;
		}
		
		if(!validAccess)
		{
			String msg = "Unauthorized API access."
					+ " passedCCIDFromClient:"+passedCCIDFromClient
					+ " passedAppIdFromClient:"+applicationId
					+" eAuthCCID:"+eAuthCCID
					+" eAuthCCID:"+eAuthCCID
					+" userRoles:"+olaCoreUserModel.getEasRoleList()
					+ " eAuthId:"+eAuthId; 
		    log.warn(msg);
			throw new OlaCoreAPIException("Unauthorized API access.");
		}
	}

	@Autowired
	public IFSASecurityService securityService;
	
	public UserDetails getCurrentUser(HttpServletRequest request) {
		UserDetails userDetails = securityService.getCurrentUser();
		if(userDetails!=null) {
		    userDetails =new FSAUserDetails(request.getHeader(OLAUserDetailsService.EAUTH_COMMON_NAME_ATTR), 
		    		request.getHeader(OLAUserDetailsService.EAUTH_FIRST_NAME_ATTR), 
		    		request.getHeader(OLAUserDetailsService.EAUTH_LAST_NAME_ATTR), userDetails.getUsername(), new ArrayList<EASAuthority>())	;	
			log.info("OIDC Authentication FSAUserDetails= {}", userDetails);
		}

		return userDetails;
		
	}

	public FSAUserDetails getFSAUserDetails(HttpServletRequest request) {

		UserDetails userDetails = getCurrentUser(request);

		if (userDetails instanceof FSAUserDetails) {

			return (FSAUserDetails) userDetails;
		}
		return null;
	}

	protected Integer getCurrentCoreCustomerIdentifier(HttpServletRequest request) {

		Integer coreCustomerIdentifier = null;

		FSAUserDetails fsaUserDetails = getFSAUserDetails(request);

		if (fsaUserDetails != null) {
		
			String ccid = request.getHeader(OLAUserDetailsService.EAUTH_SCIMS_ID_ATTR);
			log.info("getCurrentCoreCustomerIdentifier ccid: {}",ccid);
			if (!StringUtil.isEmptyString(ccid)) {
	
				coreCustomerIdentifier = Integer.valueOf(ccid);
			}

		}
		return coreCustomerIdentifier;
	}
	
	protected String getCurrentCoreCustomerEmailAddress(HttpServletRequest request) {

		String emailAddress = null;

		FSAUserDetails fsaUserDetails = getFSAUserDetails(request);

		if (fsaUserDetails != null) {
		
			String ccid = request.getHeader(OLAUserDetailsService.EAUTH_EMAIL_ATTR);
			if (!StringUtil.isEmptyString(ccid)) {
	
				emailAddress = ccid;
			}

		}
		return emailAddress;
	}
	

	protected String getEAuthAssuranceLevel(HttpServletRequest request) {

		return request.getHeader(OLAUserDetailsService.EAUTH_ASSURANCE_LEVEL_ATTR);
		
	}

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {// delegates to a JSR-303 provider for performing method-level validation on annotated methods.
		return new MethodValidationPostProcessor();
	}
	
}