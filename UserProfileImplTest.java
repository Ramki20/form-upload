package gov.usda.fsa.fcao.flp.security.userprofile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.security.userprofile.UserProfileImpl.Name;
import gov.usda.fsa.fcao.flp.security.utils.SecurityMockBase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author gowtham.nalluri
 * 
 */
public class UserProfileImplTest extends SecurityMockBase {

	public static final String EAS_KEY_STATE_OFFICE_EMPLOYEE = "app.fsa.flp.dls.so";
	public static final String EAS_KEY_LS = "app.fsa.flp.dls.ls";
	private Permission PERMISSION = Permission.lookup(EAS_KEY_LS);
	private Role role = Role.lookup(EAS_KEY_STATE_OFFICE_EMPLOYEE);

	private UserProfile userProfile;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		Name name = Mockito.mock(Name.class);

		userRoles.add(Role.EAS_KEY_FINANCIAL_OFFICE_EMPLOYEE);
		userRoles.add(Permission.EAS_KEY_CM);
		
		userProfile = new UserProfileImpl(mockHttpServletRequest);
		((UserProfileImpl)userProfile).setName(name);
		
	}

	@Test
	public void testGetEAuthId() {
		assertNotNull("EAuth Id should not be null", userProfile.getEAuthId());
	}
	@Test
	public void testGetRoles() {
		assertTrue("Roles Should Not be empty",
				userProfile.getRoles().size() > 0);
	}
	@Test
	public void testGetPermissions() {
		assertTrue("Permissions Should Not be empty", userProfile
				.getPermissions().size() > 0);
	}
	@Test
	public void testHasRole() {
		assertFalse(userProfile.hasRole(role));
	}
	@Test
	public void testHasPermission() {
		assertFalse(userProfile.hasPermission(PERMISSION));
	}
	@Test
	public void testGetCombinedAreaOfResponsibility() {
		assertNotNull(userProfile.getCombinedAreaOfResponsibility());
	}
	@Test
	public void testGetConflictsOfInterest() {
		assertNotNull(userProfile.getConflictsOfInterest());
	}
	@Test
	public void testGetGeneralAreaOfResponsibility() {
		assertNotNull(userProfile.getGeneralAreaOfResponsibility());
	}
	@Test
	public void testGetObligationSubmissionAreaOfResponsibility() {
		assertNotNull(userProfile.getObligationSubmissionAreaOfResponsibility());
	}
	@Test
	public void testGetOfficeAssignments() {
		assertNotNull(userProfile.getOfficeAssignments());
	}
	@Test
	public void testHasConflictOfInterest() {
		assertFalse(userProfile.hasConflictOfInterest(232323));
	}

	//
	// public void testIsAssignedOffice() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetFirstName() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetMiddleName() {
	// fail("Not yet implemented");
	// }
	//
	// public void testGetLastName() {
	// fail("Not yet implemented");
	// }
	@Test
	public void testCompareTo() {
		// assertEquals(0, userProfile.compareTo(userProfile1));
	}
	@Test
	public void testToString() {
		assertNotNull(userProfile.toString());
	}
	
}
