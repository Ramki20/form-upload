package gov.usda.fsa.fcao.flp.security.userprofile;

import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;

import javax.servlet.http.HttpServletRequest;

public class UserProfileImpl extends EASUserProfile implements 
	gov.usda.fsa.fcao.flp.security.userprofile.UserProfile {
	private static final long serialVersionUID = 4909584880935819069L;

	public UserProfileImpl(HttpServletRequest request) {
		super(request);
	}

	public UserProfileImpl() {
		super();
	}

	static interface Name {
		public String getFirstName();

		public String getMiddleName();

		public String getLastName();
	}

	private Name name;
	public void setName( Name name){
		this.name = name;
	}
	public String getEAuthId() {
		return this.getEAuthID();
	}

	public String getFirstName() {
		return name.getFirstName();
	}

	public String getMiddleName() {
		return name.getMiddleName();
	}

	public String getLastName() {
		return name.getLastName();
	}

	public boolean hasConflictOfInterest(Integer coreCustomerID) {
		return this.hasConflictOfInterest(coreCustomerID.toString());
	}

	public boolean hasRole(Role role) {
		return this.isUserInRole(role);
	}

	public boolean isAssignedOffice(Integer oipOfficeID) {
		return this.isAssignedOffice(oipOfficeID.toString());
	}

	public int compareTo(Object obj) {
		if (null == obj)
			throw new NullPointerException("Target comparison object is null.");

		if (!(obj instanceof UserProfile))
			throw new ClassCastException(
					"Expected UserIdentifier; encountered "
							+ obj.getClass().getName());

		return this.getEAuthID().compareTo(
				((UserProfile) obj).getEAuthID());
	}

}
