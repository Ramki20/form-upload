package gov.usda.fsa.fcao.flp.flpids.common.auth;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import gov.usda.fsa.eas.auth.AuthorizationException;
import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;

public class EASNatsUserProfile extends EASUserProfile {
	private static final long serialVersionUID = -9166654099019929225L;

	public EASNatsUserProfile(HttpServletRequest request, DLSAgencyToken token){
		super(request, token);
	}

	public EASNatsUserProfile(HttpServletRequest request) {
		super(request);
	}

	protected EASNatsUserProfile() {

	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<String> obtainAditonalAreasOfResponsibility()  {
		Collection<String> additionalAOR = new TreeSet<String>();
		Collection<String> stateCodeList;
		try {
			stateCodeList = AuthorizationManager
					.getListAttribute(NATS_GENERAL_AREA_OF_RESPONSIBILITY);
			if(stateCodeList != null && !stateCodeList.isEmpty()){
				additionalAOR.addAll(stateCodeList);
			}
		} catch (AuthorizationException e) {
			LOGGER.info("No NATS specific state code.." + e.getMessage());
		}
		
		return additionalAOR;
	}
	
	@Override
	public List<String> getNATSRoles(){
		List<String> natsRoles =  super.getNATSRoles();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Login User has Nats Role " + natsRoles.size());
		}
		if(natsRoles.isEmpty() && isITMaintenanceRole()){
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_ADMIN_NO_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_ADMIN_SO_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_BASE_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_ADMIN_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_AGENCY_POINT_OF_CONTACT_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_FUND_CONTROL_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_APPRAISER_PERMISSION);
			userAssignedPermissions.add(Permission.EAS_KEY_NATS_STAFF_APPRAISER_PERMISSION);
			
			userAssignedRoles.add(Role.NATS_ADMIN);
			userAssignedRoles.add(Role.NATS_ADMIN_NO);
			userAssignedRoles.add(Role.NATS_ADMIN_SO);
			natsRoles = super.getNATSRoles();
		}
		if(isITMaintenanceRole() && getEmployeeData() != null && StringUtil.isEmptyString(getEmployeeData().getEmployeeId()) ){
			getEmployeeData().setEmployeeId(this.getEAuthID());
		}
		return natsRoles;
	}
}
