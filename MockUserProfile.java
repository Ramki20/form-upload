/*
 * Created on Sep 26, 2005
 */
package gov.usda.fsa.fcao.flp.flpids.common.auth;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.ContractValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.ApplicationAgentException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSAgentValidationException;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author douglas.a.clark
 */
public final class MockUserProfile extends DLSUserProfile implements
gov.usda.fsa.fcao.flp.flpids.common.auth.UserProfile {
	private static final Logger logger = LogManager.getLogger(MockUserProfile.class);
	private static final long serialVersionUID = 1L;
	private static NormalizeAreaOfResponsibility normalizeAreaOfResponsibility;
	private static ContractValidator contractValidator;

	private static InetAddress addr;
	private static String hostname;

	private void initialize() {
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			logger.error("Exception thrown in intialize in MockUserProfile:", e);
		}
		hostname = addr.getHostName();
	}

	private static final Set<Permission> stateRolePermissions;
	private static final Set<Permission> serviceCenterRolePermissions;
	private static final Set<Permission> financeRolePermissions;
	private static final Set<Permission> viewOnlyPermissions;
	private static final Set<String> arbitraryAreaOfResponsibility;
	private static final Set<Permission> nationalRolePermissions;
	static {
		stateRolePermissions = new TreeSet<Permission>();
		stateRolePermissions.add(Permission.PLAS_1A_MANUSCRIPT_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_1A_SUBMISSION_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_1C_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_1D_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_1F_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_1M_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_4A_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_4D_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_5F_PERMISSION);
		stateRolePermissions.add(Permission.PLAS_8R_PERMISSION);
		stateRolePermissions.add(Permission.LOAN_MAKING_PERMISSION);
		stateRolePermissions.add(Permission.LOAN_SERVICING_PERMISSION);
		stateRolePermissions.add(Permission.DLS_VIEW_PERMISSION);
		stateRolePermissions.add(Permission.DLS_REPORTS_PERMISSION);
		stateRolePermissions.add(Permission.CHECKLIST_MAINT_PERMISSION);

		serviceCenterRolePermissions = new TreeSet<Permission>();
		serviceCenterRolePermissions
				.add(Permission.PLAS_1A_MANUSCRIPT_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_1C_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_1D_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_1F_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_1M_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_4A_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_4D_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_5F_PERMISSION);
		serviceCenterRolePermissions.add(Permission.PLAS_8R_PERMISSION);
		serviceCenterRolePermissions.add(Permission.LOAN_MAKING_PERMISSION);
		serviceCenterRolePermissions.add(Permission.LOAN_SERVICING_PERMISSION);
		serviceCenterRolePermissions.add(Permission.DLS_VIEW_PERMISSION);
		serviceCenterRolePermissions.add(Permission.DLS_REPORTS_PERMISSION);

		financeRolePermissions = new TreeSet<Permission>();
		financeRolePermissions.add(Permission.PLAS_1A_MANUSCRIPT_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_1A_SUBMISSION_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_1C_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_1D_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_1F_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_1M_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_4A_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_4D_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_5F_PERMISSION);
		financeRolePermissions.add(Permission.PLAS_8R_PERMISSION);
		financeRolePermissions.add(Permission.LOAN_MAKING_PERMISSION);
		financeRolePermissions.add(Permission.LOAN_SERVICING_PERMISSION);
		financeRolePermissions.add(Permission.DLS_VIEW_PERMISSION);
		financeRolePermissions.add(Permission.DLS_REPORTS_PERMISSION);

		viewOnlyPermissions = new TreeSet<Permission>();
		viewOnlyPermissions.add(Permission.DLS_VIEW_PERMISSION);
		viewOnlyPermissions.add(Permission.DLS_REPORTS_PERMISSION);

		arbitraryAreaOfResponsibility = new TreeSet<String>();
		// Existing in database...
		arbitraryAreaOfResponsibility.add("64330");
		arbitraryAreaOfResponsibility.add("01305");
		arbitraryAreaOfResponsibility.add("01306");
		arbitraryAreaOfResponsibility.add("01308");
		arbitraryAreaOfResponsibility.add("01309");
		arbitraryAreaOfResponsibility.add("01311");
		arbitraryAreaOfResponsibility.add("01317");
		arbitraryAreaOfResponsibility.add("01323");
		arbitraryAreaOfResponsibility.add("18330");
		arbitraryAreaOfResponsibility.add("30308");
		arbitraryAreaOfResponsibility.add("30309");
		nationalRolePermissions = new TreeSet<Permission>();
		nationalRolePermissions.add(Permission.CHECKLIST_MAINT_PERMISSION);
		nationalRolePermissions.add(Permission.DLS_VIEW_PERMISSION);
		nationalRolePermissions.add(Permission.DLS_REPORTS_PERMISSION);

		// Kentucky service centers...
		arbitraryAreaOfResponsibility.add("20301");
		arbitraryAreaOfResponsibility.add("20305");
		arbitraryAreaOfResponsibility.add("20314");
		arbitraryAreaOfResponsibility.add("20324");
		arbitraryAreaOfResponsibility.add("20330");
		arbitraryAreaOfResponsibility.add("20335");
		arbitraryAreaOfResponsibility.add("20342");
		arbitraryAreaOfResponsibility.add("20349");
		arbitraryAreaOfResponsibility.add("20352");
		arbitraryAreaOfResponsibility.add("20369");
		arbitraryAreaOfResponsibility.add("20371");
		arbitraryAreaOfResponsibility.add("20387");
		arbitraryAreaOfResponsibility.add("20388");
		arbitraryAreaOfResponsibility.add("21306");
		arbitraryAreaOfResponsibility.add("21314");
		arbitraryAreaOfResponsibility.add("21315");
		arbitraryAreaOfResponsibility.add("21316");
		arbitraryAreaOfResponsibility.add("21321");
		arbitraryAreaOfResponsibility.add("09340");
		arbitraryAreaOfResponsibility.add("32345");
		arbitraryAreaOfResponsibility.add("47325");

	}

	private Set<Permission> getPermissions(Role role) {
		if (Role.SERVICE_CENTER.equals(role)) {
			return serviceCenterRolePermissions;
		} else if (Role.STATE_OFFICE.equals(role)) {
			return stateRolePermissions;
		} else if (Role.FINANCIAL_OFFICE.equals(role)) {
			return financeRolePermissions;
		} else if (Role.NATIONAL_OFFICE.equals(role)) {
			return nationalRolePermissions;
		} else {
			return viewOnlyPermissions;
		}
	}

	private String eAuthID;

	private Set<Role> roles = new TreeSet<Role>();
	private Set<Permission> permissions = new TreeSet<Permission>();
	private Set<String> officeAssignments = new TreeSet<String>();
	private Set<String> conflictsOfInterest = new TreeSet<String>();

	private Set<String> generalAreaOfResponsibility = new TreeSet<String>(); // Set<String
																				// flpOfficeID>
	private Set<String> obligationSubmissionAreaOfResponsibility = new TreeSet<String>(); // Set<String
																							// flpOfficeID>
	private Set<String> combinedAreaOfResponsibility = new TreeSet<String>(); // Set<String
																				// flpOfficeID>

	private AgencyToken agencyToken;

	// Automated Test Tool ctor...
	public MockUserProfile(String eAuthIdValue, 
							Collection<Role> rolesList,
							Collection<Permission> permissionsList, 
							Collection<String> aor,
							Collection<String> aor1a, 
							Collection<String> conflictsList)
			throws ApplicationAgentException {
		initialize();

		contractValidator.assertPrecondition(null != eAuthIdValue,
				"UserProfile constructed with null eAuthID.",
				"error.Security.InvalidUserProfileInformation");

		eAuthID = eAuthIdValue;

		if (null != rolesList) {
			roles.addAll(rolesList);
		}

		if (null != conflictsList) {
			conflictsOfInterest.addAll(conflictsList);
		}

		if (null != permissionsList) {
			permissions.addAll(permissionsList);
		}

		try {
			if (null != aor) {
				generalAreaOfResponsibility
						.addAll(normalizeAreaOfResponsibility.process(aor,
								eAuthID));
			}

			if (null != aor1a) {
				obligationSubmissionAreaOfResponsibility
						.addAll(normalizeAreaOfResponsibility.process(aor1a,
								eAuthID));
			}
		} catch (Throwable e) {
			throw new DLSAgentValidationException(
					"UserProfile constructed with bad service area codes.",
					"error.Security.InvalidUserProfileInformation");
		}

		combinedAreaOfResponsibility.addAll(generalAreaOfResponsibility);
		combinedAreaOfResponsibility
				.addAll(obligationSubmissionAreaOfResponsibility);

		createAgencyToken(eAuthID, hostname);
	}

	public MockUserProfile(String eAuthIdValue, 
							Role role,
							Collection<Permission> permissionsList, 
							Collection<String> offices,
							Collection<String> conflictsList)
			throws DLSAgentValidationException {
		initialize();

		eAuthID = eAuthIdValue;
		roles.add(role);


		if (null != offices) {
			Collection<String> normalizedOffices = null;

			try {
				normalizedOffices = normalizeAreaOfResponsibility.process(
						offices, eAuthID);
			} catch (Throwable e) {
				throw new DLSAgentValidationException(
						"UserProfile constructed with bad service area codes.",
						"error.Security.InvalidUserProfileInformation");
			}

			generalAreaOfResponsibility.addAll(normalizedOffices);
			obligationSubmissionAreaOfResponsibility.addAll(normalizedOffices);
			combinedAreaOfResponsibility.addAll(normalizedOffices);
		}

		if (null != conflictsList) {
			conflictsOfInterest.addAll(conflictsList);
		}

		if (null != permissionsList) {
			permissions.addAll(permissionsList);
		}
		createAgencyToken(eAuthID, hostname);
	}

	public MockUserProfile(String eAuthIdValue, Role role,
							Collection<String> offices, 
							Collection<String> conflictsList)
			throws ApplicationAgentException {
		initialize();

		eAuthID = eAuthIdValue;

		roles.add(role);


		if (null != offices) {
			Collection<String> normalizedOffices = null;

			try {
				normalizedOffices = normalizeAreaOfResponsibility.process(
						offices, eAuthID);
			} catch (Throwable e) {
				throw new ApplicationAgentException(
						"UserProfile constructed with bad service area codes. Error.Security.InvalidUserProfileInformation");
			}

			generalAreaOfResponsibility.addAll(normalizedOffices);
			obligationSubmissionAreaOfResponsibility.addAll(normalizedOffices);
			combinedAreaOfResponsibility.addAll(normalizedOffices);
		} else {
			generalAreaOfResponsibility.addAll(arbitraryAreaOfResponsibility);
			obligationSubmissionAreaOfResponsibility
					.addAll(arbitraryAreaOfResponsibility);
			combinedAreaOfResponsibility.addAll(arbitraryAreaOfResponsibility);
		}

		if (null != conflictsList) {
			conflictsOfInterest.addAll(conflictsList);
		}

		permissions.addAll(getPermissions(role));

		createAgencyToken(eAuthID, hostname);
	}

	public String getEAuthID() {
		return eAuthID;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

	public boolean isUserInRole(Role role) {
		return roles.contains(role);
	}

	public boolean isStateRole() {
		return roles.contains(Role.STATE_OFFICE);
	}

	public boolean isServiceCenterRole() {
		return roles.contains(Role.SERVICE_CENTER);
	}

	public boolean isNationalRole() {
		return roles.contains(Role.NATIONAL_OFFICE);
	}

	public boolean isFinanceRole() {
		return roles.contains(Role.FINANCIAL_OFFICE);
	}

	public boolean isMaintanenceRole() {
		return ((!isStateRole() || isServiceCenterRole()) && roles
				.contains(Role.NON_FLP_FSA));
	}

	public boolean isNONFLPUserRole() {
		return ((roles.contains(Role.NON_FLP_FSA)));
	}

	public Collection<String> getOfficeAssignments() {
		return officeAssignments;
	}

	public boolean isAssignedOffice(String oipOfficeID) {
		return officeAssignments.contains(oipOfficeID);
	}

	public Collection<Permission> getPermissions() {
		return permissions;
	}

	public boolean hasPermission(Permission permission) {
		return permissions.contains(permission);
	}

	public Collection<String> getGeneralAreaOfResponsibility() {
		return generalAreaOfResponsibility;
	}// .clone(); }

	public Collection<String> getObligationSubmissionAreaOfResponsibility() {
		return obligationSubmissionAreaOfResponsibility;
	}// .clone(); }

	public Collection<String> getCombinedAreaOfResponsibility() {
		return combinedAreaOfResponsibility;
	}// .clone(); }

	public Collection<String> getConflictsOfInterest() {
		return conflictsOfInterest;
	}

	public boolean hasConflictOfInterest(String coreCustomerID) {
		return conflictsOfInterest.contains(coreCustomerID);
	}

	public AgencyToken getAgencyToken() {
		return agencyToken;
	}

	public void setNormalizeAreaOfResponsibility(
			NormalizeAreaOfResponsibility normalizeAreaOfResponsibility) {
		MockUserProfile.normalizeAreaOfResponsibility = normalizeAreaOfResponsibility;
	}

	public void setContractValidator(ContractValidator contractValidator) {
		MockUserProfile.contractValidator = contractValidator;
	}

	public ContractValidator getContractValidator() {
		return MockUserProfile.contractValidator;
	}

	private void createAgencyToken(String userId, String hostname) {
		this.agencyToken = new AgencyToken();
		agencyToken.setUserIdentifier(userId);
		agencyToken.setRequestHost(hostname);
	}

	@SuppressWarnings("unused")
	private MockUserProfile() {
		initialize();
		createAgencyToken("unknown user", "LocalHost");
	}

	@Override
	public boolean hasRole(Role role) {
		return this.isUserInRole(role);
	}

	@Override
	public boolean isFSFLRole() {
		return this.getPermissions().contains(Permission.FSFL_PERMISSION);
	}

	@Override
	public boolean isNatsNationalAdminRole() {
		return this.getPermissions().contains(Permission.EAS_KEY_NATS_ADMIN_NO_PERMISSION);
	}

	@Override
	public boolean isNatsStateAdminRole() {
		return this.getPermissions().contains(Permission.EAS_KEY_NATS_ADMIN_SO_PERMISSION);
	}

	@Override
	public boolean hasNatsRole() {
		return !this.getRoles().isEmpty();
	}

	@Override
	public Collection<String> getJurisdiction() {
		return getGeneralAreaOfResponsibility();
	}

	@Override
	public Collection<String> getCombinedJurisdiction() {
		return getCombinedAreaOfResponsibility();
	}

	@Override
	public boolean isInUserJurisdiction(String flpOfficeCode) {
		return isInUserCombinedJurisdiction(flpOfficeCode);
	}

	@Override
	public boolean isInUserCombinedJurisdiction(String flpOfficeCode) {
		return getCombinedAreaOfResponsibility().contains(flpOfficeCode);
	}

	@Override
	public boolean isInUserAreaOfResponsiblity(String flpOfficeCode) {
		return generalAreaOfResponsibility.contains(flpOfficeCode);
	}
	
	@Override
	public boolean isNationalOnlyRole(){
		 return (roles.contains(Role.NATIONAL_OFFICE) && 
				 (!roles.contains(Role.STATE_OFFICE) &&
				  !roles.contains(Role.SERVICE_CENTER) && 
				  !roles.contains(Role.FINANCIAL_OFFICE)));
	 }
	@Override
	 public boolean isFinanceOnlyRole(){
		 return (roles.contains(Role.FINANCIAL_OFFICE) && 
				 (!roles.contains(Role.STATE_OFFICE) &&
				  !roles.contains(Role.SERVICE_CENTER) && 
				  !roles.contains(Role.NATIONAL_OFFICE)));
	 }

	@Override
	public boolean isEdalrsExpertRole() {
		return permissions.contains(Permission.EAS_EDALRS_PERMISSION);
	}

	@Override
	public boolean isEdalrsAdminRole() {
		return permissions.contains(Permission.EAS_EDALRS_ADMIN_PERMISSION);
	}

	@Override
	public String getUserInformation() {
		return eAuthID;
	}

	@Override
	public String getCurrentUser() {
		return eAuthID;
	}

	@Override
	public Collection<String> getEASRoles() {
		return this.getEASRoles();
	}

	@Override
	public boolean hasRole(String role) {
		return this.getEASRoles().contains(role);
	}

	@Override
	public boolean isDistrictOfficeRole() {
		return roles.contains(Role.DISTRICT_OFFICE);
	}

	@Override
	public boolean isITMaintenanceRole() {
		return roles.contains(Role.FLP_IT_USER);
	}

	@Override
	public boolean isHelpDeskUserRole() {
		return roles.contains(Role.HELPDESK_USER);
	}

	@Override
	public boolean isInUserObligationSubmissionAreaOfResponsiblity(
			String flpOfficeCode) {
		return obligationSubmissionAreaOfResponsibility.contains(flpOfficeCode);
	}

	@Override
	public EmployeeData getEmployeeData() {
		return new EmployeeData();
	}

	@Override
	public List<String> getNATSRoles() {
		return new ArrayList<String>();
	}

	@Override
	public String getUserAgencyCode() {
		return EASUserProfile.AGENCY_CODE;
	}	
	
}
