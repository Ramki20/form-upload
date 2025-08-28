package gov.usda.fsa.fcao.flp.flpids.common.auth;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.eas.auth.AuthorizationException;
import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.fcao.flp.constants.OfficeTypeConstants;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.AddressBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.PartyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.DLSAccessPermissionException;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.OfficeInfoCacheManager;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ValidationUtils;
import gov.usda.fsa.fcao.flp.security.UserRoleOverrider;

public class EASUserProfile extends DLSUserProfile implements UserProfile {
	private static final long serialVersionUID = -8432704250474461540L;
	protected static final Logger LOGGER = LogManager.getLogger(EASUserProfile.class);
	protected static final String EAUTH_MAP_KEY = "eauth.attributes.map";
	public static final String EAUTH_ID_KEY = "usda_eauth_id";
	protected static final String EAS_USER_FOUND_KEY = "eas.user.found";
	protected static final String EAS_OFFICE_ASSIGNMENT_KEY = "employee.office_id_assignments.list";
	public static final String DLS_GENERAL_AREA_OF_RESPONSIBILITY = "app.fsa.flp.office";

	public static final String NATS_GENERAL_AREA_OF_RESPONSIBILITY = "app.fsa.flp.nats.jurisdiction";

	public static final String DLS_1A_SUBMISSION_AREA_OF_RESPONSIBILITY = "app.fsa.flp.1a.office";
	public static final String EAUTH_PRIMARY_OFFICE_ID = "USDALOCATION";
	protected static InetAddress addr = null;
	protected static String hostname = "localhost";
	protected static NormalizeAreaOfResponsibility normalizeAreaOfResponsibility;
	protected String currentUser = "Anonymous";
	protected String userDiscription = "";
	protected Map<String, String> eauthMap = null;
	protected static final String headName_usdaeauthid = "usdaeauthid";
	protected static final String headName_usdaeauthid2 = "usda_eauth_id";

	protected Collection<String> userEASRoles = new HashSet<String>();

	public static final List<String> EAS_ROLE_OVERRIDE_CANDIDATES = new ArrayList<String>();

	public static final String AGENCY_CODE = "FA";
	public static final String AGENCY_CODE_CE = "CE";
	private String userAgencyCode = AGENCY_CODE;

	private List<ServiceCenterFlpOfficeCodeBO> userEASServiceCenterList = new ArrayList<ServiceCenterFlpOfficeCodeBO>();

	static {
		EAS_ROLE_OVERRIDE_CANDIDATES.add("28682016081603511481257");
	}

	protected void initialize() {
		try {
			addr = InetAddress.getLocalHost();
		} catch (java.net.UnknownHostException e) {
			LOGGER.error("Initialization error", e);
		}
		hostname = addr.getHostName();
	}

	private String eAuthID = "";
	private AgencyToken agencyToken;
	protected Set<Role> userAssignedRoles = new TreeSet<Role>();
	protected Set<Permission> userAssignedPermissions = new TreeSet<Permission>();
	private Set<String> officeAssignments = new TreeSet<String>();
	private Set<String> generalAreaOfResponsibility = new TreeSet<String>();

	protected Set<String> jurisdictionFlpOfficeCodeCollection = new TreeSet<String>();

	private Set<String> obligationSubmissionAreaOfResponsibility = new TreeSet<String>();
	private Set<String> combinedAreaOfResponsibility = new TreeSet<String>();
	private Set<String> combinedJurisdictionFlpOfficeCode = new HashSet<String>(getJurisdiction());
	private Set<String> conflictsOfInterest = new HashSet<String>();
	protected EmployeeData employeeData;

	public EASUserProfile(HttpServletRequest request) {
		try {
			if (userFoundInEAS(request)) {
				initialize();
				intializeUserEASProfile();
			} else { // this should never happen
				LOGGER.error("Invalid User obtained from EAS... failed with an empty EASUserProfile instance...");
			}
		} catch (AuthorizationException ex) {
			String errorMsg = "Exception thrown while Retrieving EAS User Profile";
			LOGGER.error(errorMsg, ex);
		} catch (Exception ex) {
			LOGGER.error("Unknown Exception from EASUserProfile constructor in intializeUserEASProfile() call..", ex);
		}
		try {
			createAgencyToken(request, eAuthID, hostname + "#" + getCookies(request));
			userAgencyCode = obtainUserAgencyCode(request);
			UserRoleOverrider.override_user_profile_for_testing_only(this);
			employeeData = getNormalizeAreaOfResponsibility().loadeEployeeData(agencyToken, userAgencyCode);
		} catch (Exception ex) {
			LOGGER.error("Unknown Exception from EASUserProfile constructor in createAgencyToken() call...", ex);
		} finally {
			createDefaultEmployeeData();
		}
	}

	public EASUserProfile(HttpServletRequest request, DLSAgencyToken token) {
		try {
			if (userFoundInEAS(request)) {
				initialize();
				intializeUserEASProfile();
			} else { // this should never happen
				LOGGER.error("Invalid User obtained from EAS... failed with an empty EASUserProfile instance...");
			}

			// set the token
			this.agencyToken = token;
			userAgencyCode = obtainUserAgencyCode(request);
			token.setFlpOfficeTypeCode(getFlpOfficeTypeCode());
			UserRoleOverrider.override_user_profile_for_testing_only(this);
			employeeData = getNormalizeAreaOfResponsibility().loadeEployeeData(agencyToken, userAgencyCode);
		} catch (AuthorizationException ex) {
			String errorMsg = "Exception thrown while Retrieving EAS User Profile";
			LOGGER.error(errorMsg, ex);
		} catch (Exception ex) {
			LOGGER.error("Unknown Exception from EASUserProfile constructor in intializeUserEASProfile() call..", ex);
		} finally {
			createDefaultEmployeeData();
		}
	}

	public static String getEAuthIdFromRequestHeader(HttpServletRequest request, AgencyToken token) {
		String headerValue = "";
		if (request == null || (token == null || token.isReadOnly())) {
			return headerValue;
		}
		Enumeration<?> headernames = request.getHeaderNames();

		String headerName;
		while (headernames != null && headernames.hasMoreElements()) {
			headerName = headernames.nextElement().toString();
			if (headName_usdaeauthid.equalsIgnoreCase(headerName)
					|| headName_usdaeauthid2.equalsIgnoreCase(headerName)) {
				headerValue = retrieveHeaderValueFromRequest(request, headerName);
				LOGGER.info("requestHeader name: value.." + headerName + "&&" + headerValue);
				if (EAS_ROLE_OVERRIDE_CANDIDATES.contains(headerValue)) {
					token.setUserIdentifier(headerValue);
					break;
				}
			}
		}
		return headerValue;
	}

	private boolean userFoundInEAS(HttpServletRequest request) throws AuthorizationException {
		boolean result = false;
		String found = AuthorizationManager.getAttribute(EAS_USER_FOUND_KEY);

		if ("true".equals(found)) {
			result = true;
		} else {
			String eAuthId = getEAuthIdFromRequestHeader(request, null);
			result = !StringUtil.isEmptyString(eAuthId);
		}
		return result;
	}

	private void checkDLMPermission() throws DLSAccessPermissionException {
		if (!hasNatsRole() && !getEASRoles().contains(Permission.EAS_KEY_VIEW)) {
			throw new DLSAccessPermissionException("This EAS User does not have access to DLS Application");
		}
	}

	/**
	 * only for SO and SC FO and NO donot required this
	 */
	@SuppressWarnings("unchecked")
	protected void obtainAreasOfResponsibility() throws AuthorizationException, DLSBusinessStopException {
		if (this.isFinanceOnlyRole() || this.isNationalOnlyRole()) {
			return;
		}
		try {
			Set<ServiceCenterFlpOfficeCodeBO> userEASServiceCenterSet = new TreeSet<ServiceCenterFlpOfficeCodeBO>();

			Collection<String> userAssignedStateOfficeCodes = AuthorizationManager
					.getListAttribute(DLS_GENERAL_AREA_OF_RESPONSIBILITY);
			Collection<String> areaOfResponsibility = getNormalizeAreaOfResponsibility()
					.process(userAssignedStateOfficeCodes, eAuthID, userEASServiceCenterSet);

			if (areaOfResponsibility != null && !areaOfResponsibility.isEmpty()) {
				generalAreaOfResponsibility.addAll(areaOfResponsibility);
			}

			Collection<String> additionalAOR = obtainAditonalAreasOfResponsibility();
			Collection<String> processedAdditionalAOR = getNormalizeAreaOfResponsibility().process(additionalAOR,
					eAuthID);

			if (processedAdditionalAOR != null && !processedAdditionalAOR.isEmpty()) {
				jurisdictionFlpOfficeCodeCollection.clear();
				jurisdictionFlpOfficeCodeCollection.addAll(processedAdditionalAOR);
				combinedJurisdictionFlpOfficeCode.addAll(processedAdditionalAOR);
			}
			additionalAOR = getNormalizeAreaOfResponsibility().getAllStateOfficeCode(additionalAOR);
			if (!additionalAOR.isEmpty()) {
				jurisdictionFlpOfficeCodeCollection.addAll(additionalAOR);
				combinedJurisdictionFlpOfficeCode.addAll(additionalAOR);
			}

			Collection<String> stateOfficeCodes = getNormalizeAreaOfResponsibility()
					.getAllStateOfficeCode(userAssignedStateOfficeCodes);
			if (!stateOfficeCodes.isEmpty()) {
				jurisdictionFlpOfficeCodeCollection.addAll(stateOfficeCodes);
				combinedJurisdictionFlpOfficeCode.addAll(stateOfficeCodes);
			}

			areaOfResponsibility = getNormalizeAreaOfResponsibility()
					.process(AuthorizationManager.getListAttribute(DLS_1A_SUBMISSION_AREA_OF_RESPONSIBILITY), eAuthID);

			if (areaOfResponsibility != null && !areaOfResponsibility.isEmpty()) {
				obligationSubmissionAreaOfResponsibility.addAll(areaOfResponsibility);
			}

			combinedAreaOfResponsibility.addAll(generalAreaOfResponsibility);
			combinedAreaOfResponsibility.addAll(obligationSubmissionAreaOfResponsibility);
			combinedJurisdictionFlpOfficeCode.addAll(generalAreaOfResponsibility);
			userEASServiceCenterList.clear();
			userEASServiceCenterList.addAll(userEASServiceCenterSet);
		} catch (AuthorizationException ex) {
			String errorMsg = "Exception thrown while Retrieving obtainAreasOfResponsibility";
			LOGGER.error(errorMsg, ex);
		}
	}

	protected Collection<String> obtainAditonalAreasOfResponsibility() {
		return jurisdictionFlpOfficeCodeCollection;
	}

	public boolean hasNatsRole() {
		return (isNatsNationalAdminRole() || isNatsStateAdminRole()
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_AGENCY_POINT_OF_CONTACT_PERMISSION)
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_FUND_CONTROL_PERMISSION)
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_APPRAISER_PERMISSION)
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_STAFF_APPRAISER_PERMISSION));
	}

	@SuppressWarnings("unchecked")
	private void intializeUserEASProfile()
			throws AuthorizationException, DLSAccessPermissionException, DLSBusinessStopException {
		eauthMap = AuthorizationManager.getMapAttribute(EAUTH_MAP_KEY);
		eAuthID = eauthMap.get(EAUTH_ID_KEY);
		currentUser = AuthorizationManager.getCurrentUser();
		if (StringUtil.isEmptyString(currentUser)) {
			throw new AuthorizationException("Exception thrown while Retrieving EAS User Profile");
		}

		userDiscription = eauthMap.get("user_description");
		userEASRoles = new ArrayList<String>();
		userEASRoles.addAll(AuthorizationManager.getUserRoles());
		obtainRolesAndPermissions();

		checkDLMPermission();

		if (isStateRole() || isITMaintenanceRole()) {
			userAssignedPermissions.add(Permission.lookup(Permission.EAS_KEY_CM));
		}
		// added FO Role.
		if (isFinanceRole() || isITMaintenanceRole()) {
			userAssignedPermissions.add(Permission.lookup(Permission.EAS_KEY_1C));
			userAssignedPermissions.add(Permission.lookup(Permission.EAS_KEY_1D));
			userAssignedPermissions.add(Permission.lookup(Permission.EAS_KEY_1F));
		}
		obtainOfficeAssignments();
		obtainAreasOfResponsibility();
		obtainConflictsOfInterest();
	}

	private void obtainConflictsOfInterest() {
		// List<String> conflicts = new ArrayList<String>();
		// conflictsOfInterest.addAll(conflicts);
	}

	private void obtainRolesAndPermissions() {
		for (String assignedRole : this.userEASRoles) {
			Role role = Role.lookup(assignedRole);
			if (null != role) {
				this.userAssignedRoles.add(role);
			}
			Permission permission = Permission.lookup(assignedRole);

			if (null != permission) {
				this.userAssignedPermissions.add(permission);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void obtainOfficeAssignments() throws AuthorizationException {
		try {
			officeAssignments = new TreeSet<String>();
			List<String> offices = AuthorizationManager.getListAttribute(EAS_OFFICE_ASSIGNMENT_KEY);
			for (String item : offices) {
				officeAssignments.add(item);
			}
		} catch (AuthorizationException ex) {
			String errorMsg = "Exception thrown while Retrieving obtainOfficeAssignments";
			LOGGER.error(errorMsg, ex);
		}
	}

	public final String getEAuthID() {
		return eAuthID;
	}

	public final Collection<Role> getRoles() {
		return userAssignedRoles;
	}

	public boolean isUserInRole(Role role) {
		return userAssignedRoles.contains(role);
	}

	public boolean isStateRole() {
		return (userAssignedRoles.contains(Role.STATE_OFFICE));
	}

	public boolean isFSFLRole() {
		return (this.userAssignedPermissions.contains(Permission.FSFL_PERMISSION)
				|| this.getEASRoles().contains(Role.EAS_KEY_FSFL_SUPPORT));
	}

	public boolean isNationalRole() {
		return userAssignedRoles.contains(Role.NATIONAL_OFFICE);
	}

	public boolean isNationalOnlyRole() {
		return (userAssignedRoles.contains(Role.NATIONAL_OFFICE)
				&& (!userAssignedRoles.contains(Role.STATE_OFFICE) && !userAssignedRoles.contains(Role.SERVICE_CENTER)
						&& !userAssignedRoles.contains(Role.FINANCIAL_OFFICE) && !isNatsNationalAdminRole()
						&& !isNatsStateAdminRole() && !hasNatsRole()));
	}

	public boolean isFinanceOnlyRole() {
		return (userAssignedRoles.contains(Role.FINANCIAL_OFFICE)
				&& (!userAssignedRoles.contains(Role.STATE_OFFICE) && !userAssignedRoles.contains(Role.SERVICE_CENTER)
						&& !userAssignedRoles.contains(Role.NATIONAL_OFFICE)));
	}

	public boolean isFinanceRole() {
		return userAssignedRoles.contains(Role.FINANCIAL_OFFICE);
	}

	public boolean isMaintanenceRole() {
		return (userAssignedRoles.contains(Role.STATE_OFFICE) || (userAssignedRoles.contains(Role.SERVICE_CENTER)));
	}

	public boolean isNONFLPUserRole() {
		return (userAssignedRoles.contains(Role.NON_FLP_FSA));
	}

	public boolean isServiceCenterRole() {
		return userAssignedRoles.contains(Role.SERVICE_CENTER);
	}

	public boolean isNatsNationalAdminRole() {
		return ((userAssignedRoles.contains(Role.NATS_ADMIN) || userAssignedRoles.contains(Role.NATS_ADMIN_NO)
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_ADMIN_NO_PERMISSION))
				|| (getNATSRoles().contains(Role.EAS_KEY_NATS_ADMIN_NO))
				|| (getNATSRoles().contains(Role.EAS_KEY_NATS_ADMIN)));
	}

	public boolean isNatsStateAdminRole() {
		return (userAssignedRoles.contains(Role.NATS_ADMIN_SO)
				|| userAssignedPermissions.contains(Permission.EAS_KEY_NATS_ADMIN_SO_PERMISSION)
				|| getNATSRoles().contains(Role.EAS_KEY_NATS_ADMIN_SO));
	}

	public boolean isEdalrsExpertRole() {
		return (userAssignedPermissions.contains(Permission.EAS_EDALRS_PERMISSION));
	}

	public boolean isEdalrsAdminRole() {
		return (userAssignedPermissions.contains(Permission.EAS_EDALRS_ADMIN_PERMISSION));
	}

	public boolean isDistrictOfficeRole() {
		return (userAssignedRoles.contains(Role.DISTRICT_OFFICE));
	}

	public boolean isITMaintenanceRole() {
		return (userAssignedRoles.contains(Role.FLP_IT_USER));
	}

	public boolean isHelpDeskUserRole() {
		return (userAssignedRoles.contains(Role.HELPDESK_USER));
	}

	public String getUserInformation() {
		return userDiscription;
	}

	public Collection<String> getEASRoles() {
		return this.userEASRoles;
	}

	public final Collection<String> getOfficeAssignments() {
		return officeAssignments;
	}

	public boolean isAssignedOffice(String oipOfficeID) {
		return officeAssignments.contains(oipOfficeID);
	}

	public final Collection<Permission> getPermissions() {
		return userAssignedPermissions;
	}

	public final boolean hasPermission(Permission permission) {
		return userAssignedPermissions.contains(permission);
	}

	public final Collection<String> getGeneralAreaOfResponsibility() {
		return generalAreaOfResponsibility;
	}

	public Collection<String> getObligationSubmissionAreaOfResponsibility() {
		return obligationSubmissionAreaOfResponsibility;
	}

	public final Collection<String> getCombinedAreaOfResponsibility() {
		return combinedAreaOfResponsibility;
	}

	public final Collection<String> getConflictsOfInterest() {
		return conflictsOfInterest;
	}

	public final boolean hasConflictOfInterest(String coreCustomerID) {
		return (getConflictsOfInterest().isEmpty() || (StringUtil.isEmptyString(coreCustomerID)) ? false
				: getConflictsOfInterest().contains(coreCustomerID));
	}

	public AgencyToken getAgencyToken() {
		return agencyToken;
	}

	public Collection<String> getJurisdiction() {
		return jurisdictionFlpOfficeCodeCollection;
	}

	/**
	 * This logic only apply for FLP users IT role have full access
	 */
	public boolean isInUserObligationSubmissionAreaOfResponsiblity(String flpOfficeCode) {
		if (StringUtil.isEmptyString(flpOfficeCode)) {
			return false;
		}
		if (isITMaintenanceRole()) {
			return checkValidMailCode(flpOfficeCode);
		}
		if (this.isStateRole()) {
			return checkJurisdictionForStateRole(flpOfficeCode, getObligationSubmissionAreaOfResponsibility());
		}
		return getObligationSubmissionAreaOfResponsibility().contains(flpOfficeCode);
	}

	/**
	 * This logic applies to both FLP and FSFL The hierarchy order is FLOO, NO, IT,
	 * SO, SC and others
	 */
	public boolean isInUserAreaOfResponsiblity(String flpOfficeCode) {
		if (StringUtil.isEmptyString(flpOfficeCode)) {
			return false;
		}
		if (isSuperUser()) {
			return checkValidMailCode(flpOfficeCode);
		}
		if (isActingAsStateUser()) {
			return checkJurisdictionForStateRole(flpOfficeCode, getGeneralAreaOfResponsibility());
		}
		return getGeneralAreaOfResponsibility().contains(flpOfficeCode);
	}

	/**
	 * This logic should apply to NATS user, but can be used for FLP and FSFL users
	 * too The hierarchy order is FLOO, NO, IT, SO, SC and others
	 */
	public boolean isInUserJurisdiction(String flpOfficeCode) {
		if (StringUtil.isEmptyString(flpOfficeCode)) {
			return false;
		}
		if (isSuperUser()) {
			return checkValidMailCode(flpOfficeCode);
		}
		if (isActingAsStateUser()) {
			return checkJurisdictionForStateRole(flpOfficeCode, getJurisdiction());
		}
		return getJurisdiction().contains(flpOfficeCode);
	}

	/**
	 * This logic should apply to NATS user, but can be used for FLP and FSFL users
	 * too The hierarchy order is FLOO, NO, IT, SO, SC and others
	 */
	public boolean isInUserCombinedJurisdiction(String flpOfficeCode) {
		if (StringUtil.isEmptyString(flpOfficeCode)) {
			return false;
		}
		if (isSuperUser()) {
			return checkValidMailCode(flpOfficeCode);
		}
		if (isActingAsStateUser()) {
			return checkJurisdictionForStateRole(flpOfficeCode, getCombinedJurisdiction());
		}
		return getCombinedJurisdiction().contains(flpOfficeCode);
	}

	/**
	 * @return
	 */
	public String getCurrentUser() {
		return currentUser;
	}

	public Collection<String> getCombinedJurisdiction() {
		return combinedJurisdictionFlpOfficeCode;
	}

	/*
	 * Srinivas prints the contents
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("UserId:" + getCurrentUser() + "::>>>" + getEAuthID() + "\r");
		sBuf.append("Office Assignments:");
		sBuf.append(printForIterator(getOfficeAssignments().iterator()));
		sBuf.append("Areas Of Responsibilites:");
		sBuf.append(printForIterator(getGeneralAreaOfResponsibility().iterator()));
		sBuf.append("Combined Area Of Responsibility:");
		sBuf.append(printForIterator(getCombinedAreaOfResponsibility().iterator()));
		sBuf.append("Obligation Submission Area Of Responsibility:");
		sBuf.append(printForIterator(getObligationSubmissionAreaOfResponsibility().iterator()));
		sBuf.append("Permissions:");
		sBuf.append(printForIterator(getPermissions().iterator()));
		sBuf.append("Roles:");
		sBuf.append(printForIterator(getRoles().iterator()));
		return sBuf.toString();
	}

	/*
	 * prints the contents using the iterator
	 */
	private String printForIterator(Iterator<?> it) {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("[");
		for (Iterator<?> itr = it; itr.hasNext();) {
			sBuf.append((Object) itr.next());
			sBuf.append(" , ");
		}
		sBuf.append("]\r");
		return sBuf.toString();
	}

	/**
	 * @return
	 */
	public Map<String, String> getEauthMap() {
		return eauthMap;
	}

	public EmployeeData getEmployeeData() {
		return employeeData;
	}

	public boolean hasEmployeeInfo() {
		return (isITMaintenanceRole() || (!StringUtil.isEmptyString(getEmployeeData().getEmployeeId())));
	}

	/**
	 * @return
	 */
	public String getEauthMapString() {
		StringBuilder sBuf = new StringBuilder();
		for (Iterator<Entry<String, String>> it = eauthMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			sBuf.append(key + "=" + value + "@@");
		}
		return sBuf.toString();
	}

	/**
	 * @return
	 */
	private String getCookies(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		StringBuilder sBuf = new StringBuilder();
		Cookie cookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				if (i > 0) {
					sBuf.append("; ");
				}

				sBuf.append(cookie.getName());
				sBuf.append("=");
				sBuf.append(cookie.getValue());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("\nCookies ===>" + cookie);
				}
			}
		}
		sBuf.append("; ");
		return sBuf.toString();
	}

	private void createAgencyToken(HttpServletRequest request, String userId, String hostname) {

		DLSAgencyToken token = new DLSAgencyToken();
		this.agencyToken = token;
		token.setUserName(currentUser);
		token.setUserIdentifier(userId);
		token.setRequestHost(hostname);
		token.setProcessingNode(hostname);
		token.setFlpOfficeTypeCode(getFlpOfficeTypeCode());
		String primaryOfficeId = (request == null) ? ""
				: retrieveHeaderValueFromRequest(request, EAUTH_PRIMARY_OFFICE_ID);

		Long oipOfficeLocationId = null;

		if (!ValidationUtils.isNullOrEmpty(primaryOfficeId)) {
			oipOfficeLocationId = new Long(primaryOfficeId);
		}
		token.setOipOfficeLocationIdentifier(oipOfficeLocationId);
	}

	public void setNormalizeAreaOfResponsibility(NormalizeAreaOfResponsibility normalizeAreaOfResponsibility) {
		EASUserProfile.normalizeAreaOfResponsibility = normalizeAreaOfResponsibility;
	}

	public final NormalizeAreaOfResponsibility getNormalizeAreaOfResponsibility() {
		return EASUserProfile.normalizeAreaOfResponsibility;
	}

	private String getFlpOfficeTypeCode() {
		String flpOfficeTypeCode = OfficeTypeConstants.TRANSACTION_SERVICE_CENTER;
		if (this.isFinanceRole()) {
			flpOfficeTypeCode = OfficeTypeConstants.TRANSACTION_OWNER_FO;
		} else if (this.isNationalRole()) {
			flpOfficeTypeCode = OfficeTypeConstants.TRANSACTION_NATIONAL;
		} else if (this.isStateRole()) {
			flpOfficeTypeCode = OfficeTypeConstants.TRANSACTION_STATE;
		} else if (this.isNatsNationalAdminRole()) {
			flpOfficeTypeCode = OfficeTypeConstants.NATS_ADMIN_NATIONAL;
		} else if (this.isNatsStateAdminRole()) {
			flpOfficeTypeCode = OfficeTypeConstants.NATS_ADMIN_STATE;
		}
		return flpOfficeTypeCode;
	}

	protected EASUserProfile() {
		this.agencyToken = new AgencyToken();
		agencyToken.setApplicationIdentifier("unknown");
		agencyToken.setUserIdentifier(this.currentUser);
		agencyToken.setRequestHost(hostname);
		agencyToken.setProcessingNode("unknown");

		userEASRoles.add(gov.usda.fsa.fcao.flp.flpids.common.security.Role.EAS_KEY_NATIONAL_OFFICE_EMPLOYEE);
		userEASRoles.add(gov.usda.fsa.fcao.flp.flpids.common.security.Role.EAS_KEY_FLP_IT_EMPLOYEE);
		userEASRoles.add(gov.usda.fsa.fcao.flp.flpids.common.security.Permission.EAS_KEY_EDALRS_EXPERT);
		userEASRoles.add(gov.usda.fsa.fcao.flp.flpids.common.security.Permission.EAS_KEY_EDALRS_ADMIN);

		createDefaultEmployeeData();
	}

	private void createDefaultEmployeeData() {
		if (this.employeeData == null) {
			this.employeeData = new EmployeeData();
			this.employeeData.setPartyBO(new PartyBO());
			this.employeeData.setOfficeAddressBO(new AddressBO());
		}
	}

	@Override
	public boolean hasRole(Role role) {
		return isUserInRole(role);
	}

	@Override
	public boolean hasRole(String role) {
		return userEASRoles.contains(role);
	}

	@Override
	public List<String> getNATSRoles() {
		List<String> roleCodes = new ArrayList<String>();
		Collection<Role> roles = getRoles();
		for (Role role : roles) {
			if (role.getKey().startsWith(Permission.EAS_KEY_NATS_BASE)) {
				roleCodes.add(role.getKey());
			}
		}
		Collection<Permission> permissions = getPermissions();
		for (Permission permission : permissions) {
			if (permission.getKey().startsWith(Permission.EAS_KEY_NATS_BASE)) {
				roleCodes.add(permission.getKey());
			}
		}
		return roleCodes;
	}

	@Override
	public String getUserAgencyCode() {
		return userAgencyCode;
	}

	public List<ServiceCenterFlpOfficeCodeBO> getUserEASServiceCenterList() {
		return userEASServiceCenterList;
	}

	private String obtainUserAgencyCode(HttpServletRequest request) {

		String agencyCode = retrieveHeaderValueFromRequest(request, "usdaagencycode");

		if (!StringUtil.isEmptyString(agencyCode)) {
			userAgencyCode = extractThe2DigitCodeOnly(agencyCode);
		} else {
			agencyCode = retrieveHeaderValueFromRequest(request, "agencycode");
			if (StringUtil.isEmptyString(agencyCode)) {
				agencyCode = retrieveHeaderValueFromRequest(request, "usdaotheragencycode");
			}
			if (!StringUtil.isEmptyString(agencyCode)) {
				userAgencyCode = agencyCode.trim();
			}
		}
		return userAgencyCode;
	}

	private String extractThe2DigitCodeOnly(String eAuth2AgencyCode) {
		String usdaAgencyCode = eAuth2AgencyCode;
		if (eAuth2AgencyCode != null && eAuth2AgencyCode.length() >= 2) {
			StringTokenizer st = new StringTokenizer(eAuth2AgencyCode, "^");
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (s.length() >= 2) {
					usdaAgencyCode = s.substring(s.length() - 2);
					break;
				}
			}
		}
		return usdaAgencyCode;
	}

	private boolean checkJurisdictionForStateRole(String flpOfficeCode, Collection<String> jurisdictionSet) {
		boolean inJurisdication = false;
		if (isActingAsStateUser() && !StringUtil.isEmptyString(flpOfficeCode) && flpOfficeCode.length() > 1) {

			String stateAbbrFromMailCode = OfficeInfoCacheManager.getStateMap().get(flpOfficeCode.substring(0, 2));
			if (StringUtil.isEmptyString(stateAbbrFromMailCode)) {
				return inJurisdication;
			}
			for (String mailCode : jurisdictionSet) {
				if (stateAbbrFromMailCode.equals(OfficeInfoCacheManager.getStateMap().get(mailCode.substring(0, 2)))) {
					inJurisdication = true;
					break;
				}
			}
		}
		return inJurisdication;
	}

	private boolean checkValidMailCode(String flpOfficeCode) {
		if (StringUtil.isEmptyString(flpOfficeCode) || flpOfficeCode.trim().length() < 2) {
			return false;
		}
		flpOfficeCode = flpOfficeCode.trim();
		String stateAbbrFromMailCode = OfficeInfoCacheManager.getStateMap().get(flpOfficeCode.substring(0, 2));
		return (!StringUtil.isEmptyString(stateAbbrFromMailCode));
	}

	private boolean isSuperUser() {
		return (isFinanceRole() || isNationalRole() || isITMaintenanceRole());
	}

	private boolean isActingAsStateUser() {
		return (isStateRole() || (isServiceCenterRole() && isFSFLRole()));
	}

	private static String retrieveHeaderValueFromRequest(HttpServletRequest request, String headerName) {
		String headerValue = request.getHeader(headerName);
		if(null != headerValue) {
			return headerValue.replaceAll("[\n|\r|\t]", "_");
		}
		return "";
	}
}
