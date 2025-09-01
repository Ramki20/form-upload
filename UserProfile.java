//Source file: C:\\dclark\\study\\DLM\\Rose Code Gen\\gov\\usda\\fsa\\fcao\\flp\\dlm\\applicationAgent\\serviceFacade\\securityServiceInterface\\UserProfile.java

package gov.usda.fsa.fcao.flp.flpids.common.auth;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Provides an internally facing interface to decouple the DLS system from the specific
 * authorization-based implementation.  Responsible for providing the users role,
 * permission, and area of responsibility assignments as well as any conflicts of interest.
 * 
 * @author douglas.a.clark
 */

public interface UserProfile extends Serializable 
{
	public String getEAuthID();
    
	public Collection<Role> getRoles();										// Collection<Role>
	public boolean isStateRole();
	public boolean isServiceCenterRole();
	public boolean isNationalRole();
	public boolean isFinanceRole();
	public boolean isNationalOnlyRole();
	public boolean isFinanceOnlyRole();
	public boolean isMaintanenceRole();
	public boolean isNONFLPUserRole();
	public boolean isFSFLRole();
	public boolean isNatsNationalAdminRole();
	public boolean isNatsStateAdminRole();
	
	public boolean isEdalrsExpertRole();
	public boolean isEdalrsAdminRole();
	public boolean isDistrictOfficeRole();
	public boolean isITMaintenanceRole();
	public boolean isHelpDeskUserRole();
	
	public String getUserInformation();
	public String getCurrentUser();
	
	
	public Collection<String> getOfficeAssignments();							// Collection<String oipOfficeID>
	public boolean isAssignedOffice( String oipOfficeID );
	
	public Collection<Permission> getPermissions();									// Collection<Permission>
	public boolean hasPermission( Permission permission );
	
	public Collection<String> getConflictsOfInterest();							// Collection<String coreCustID>
	public boolean hasConflictOfInterest( String coreCustomerID );
	
	public Collection<String> getGeneralAreaOfResponsibility();					// Collection<String serviceCenterFLPOfficeCode>
	public Collection<String> getObligationSubmissionAreaOfResponsibility();	// Collection<String serviceCenterFLPOfficeCode>
	public Collection<String> getCombinedAreaOfResponsibility();				// Collection<String serviceCenterFLPOfficeCode>
   
   public AgencyToken getAgencyToken();
   public boolean hasRole( Role role );
   public boolean hasRole( String role );
   
   public boolean hasNatsRole();
   public Collection<String> getJurisdiction();
   public Collection<String> getCombinedJurisdiction();
   public boolean isInUserJurisdiction(String flpOfficeCode);
   public boolean isInUserCombinedJurisdiction(String flpOfficeCode);
   
   public boolean isInUserAreaOfResponsiblity(String flpOfficeCode);
   public boolean isInUserObligationSubmissionAreaOfResponsiblity(String flpOfficeCode);
   public Collection<String> getEASRoles();	
   
   public EmployeeData getEmployeeData();
   public List<String> getNATSRoles();
   public String getUserAgencyCode();
   
}
