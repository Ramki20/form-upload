package gov.usda.fsa.fcao.flp.security.userprofile.eas;

import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.fcao.flp.flpids.common.auth.UserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.security.userprofile.Exceptions.AuthorizationException;
import gov.usda.fsa.fcao.flp.security.userprofile.UserSecurityProfile;
import gov.usda.fsa.fcao.flp.security.userprofile.eas.AreaOfResponsibilityUtility.NormalizedAOREnvelope;
import gov.usda.fsa.fcao.flp.security.utils.SecurityMockBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author gowtham.nalluri
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = { AuthorizationManager.class,
		AreaOfResponsibilityUtilityImpl.class })
@PowerMockIgnore( { "org.w3c.*", "javax.xml.*", "org.apache.logging.log4j.*",
		"org.xml.*" })
public class EASUserSecurityProfileFactoryTest extends SecurityMockBase{
	private static final String EAUTH_MAP_KEY = "eauth.attributes.map";
	private static final String EAUTH_ID_KEY_NEW = "usdaeauthid";
	private static final String EAS_USER_FOUND_KEY = "eas.user.found";
	private static final String EAS_OFFICE_ASSIGNMENT_KEY = "employee.office_id_assignments.list";

	private static final String DLS_GENERAL_AREA_OF_RESPONSIBILITY = "app.fsa.flp.office";
	private static final String DLS_1A_SUBMISSION_AREA_OF_RESPONSIBILITY = "app.fsa.flp.1a.office";

	private EASUserSecurityProfileFactory easSecurityProfileFactory;
	private AreaOfResponsibilityUtilityImpl aorUtility;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		aorUtility = PowerMockito.mock(AreaOfResponsibilityUtilityImpl.class);
		PowerMockito.whenNew(AreaOfResponsibilityUtilityImpl.class)
				.withNoArguments().thenReturn(aorUtility);

		PowerMockito.mockStatic(AuthorizationManager.class);

		easSecurityProfileFactory = EASUserSecurityProfileFactory.INSTANCE;
		
	}

	@Test
	public void testGetUserSecurityProfile() throws Exception {
		PowerMockito
				.when(AuthorizationManager.getAttribute(EAS_USER_FOUND_KEY))
				.thenReturn("true");

		Map<String, String> mapAttrib = new HashMap<String, String>();
		mapAttrib.put(EAUTH_ID_KEY_NEW, "2323232323232");
		mapAttrib.put("usda_eauth_id", "2323232323232");
		

		PowerMockito.when(AuthorizationManager.getMapAttribute(EAUTH_MAP_KEY))
				.thenReturn(mapAttrib);

		List<String> roleNames = new ArrayList<String>();
		roleNames.add(Role.EAS_KEY_SERVICE_CENTER_EMPLOYEE);
		roleNames.add(Role.EAS_KEY_DISTRICT_OFFICE_EMPLOYEE);
		roleNames.add(Role.EAS_KEY_STATE_OFFICE_EMPLOYEE);
		roleNames.add(Role.EAS_KEY_FINANCIAL_OFFICE_EMPLOYEE);
		roleNames.add(Role.EAS_KEY_NATIONAL_OFFICE_EMPLOYEE);
		roleNames.add(Role.DLSAPP_VIEW.toString());
		roleNames.add(Permission.EAS_KEY_1C);
		roleNames.add(Permission.EAS_KEY_1D);
		roleNames.add(Permission.EAS_KEY_1F);
		roleNames.add(Permission.EAS_KEY_VIEW);
		PowerMockito.when(AuthorizationManager.getUserRoles()).thenReturn(
				roleNames);

		PowerMockito.when(AuthorizationManager.getCurrentUser()).thenReturn(
				"testing User");

		
		List<String> offices = new ArrayList<String>();
		offices.add("23232");
		offices.add("23433");
		PowerMockito.when(
				AuthorizationManager
						.getListAttribute(EAS_OFFICE_ASSIGNMENT_KEY))
				.thenReturn(offices);

		List<String> aorAttributeList = new ArrayList<String>();
		aorAttributeList.add("23232");
		PowerMockito.when(
				AuthorizationManager
						.getListAttribute(DLS_GENERAL_AREA_OF_RESPONSIBILITY))
				.thenReturn(aorAttributeList);
		NormalizedAOREnvelope aorEnvelope = new NormalizedAOREnvelopeImpl(
				new ArrayList<String>(), new TreeSet<String>(),
				new TreeMap<String, String>());
		PowerMockito.doReturn(aorEnvelope).when(aorUtility)
				.normalizeAreaOfResponsibility(aorAttributeList);

		PowerMockito
				.when(
						AuthorizationManager
								.getListAttribute(DLS_1A_SUBMISSION_AREA_OF_RESPONSIBILITY))
				.thenReturn(aorAttributeList);
		PowerMockito.doReturn(aorEnvelope).when(aorUtility)
				.normalizeAreaOfResponsibility(aorAttributeList);

		UserSecurityProfile profile = easSecurityProfileFactory
				.getUserSecurityProfile(mockHttpServletRequest);
		Assert.assertNotNull(profile);
		Assert.assertEquals("2323232323232", profile.getEAuthID());
	}

	@Test(expected = AuthorizationException.class)
	public void testGetUserSecurityProfileNotFound() throws Exception {
		PowerMockito
				.when(AuthorizationManager.getAttribute(EAS_USER_FOUND_KEY))
				.thenReturn("false");

		easSecurityProfileFactory.getUserSecurityProfile();
	}

	@Test //(expected = AuthorizationException.class)
	public void testGetUserSecurityProfileAuthorizationException()
			throws Exception {
		PowerMockito
				.when(AuthorizationManager.getAttribute(EAS_USER_FOUND_KEY))
				.thenReturn("true");

		Map<String, String> mapAttrib = new HashMap<String, String>();
		mapAttrib.put(EAUTH_ID_KEY_NEW, "2323232323232");

		PowerMockito.when(AuthorizationManager.getMapAttribute(EAUTH_MAP_KEY))
				.thenThrow(new gov.usda.fsa.eas.auth.AuthorizationException());
		UserProfile userProfile = easSecurityProfileFactory.getUserSecurityProfile();
		
		Assert.assertTrue(userProfile instanceof UserSecurityProfile);
	}
}
