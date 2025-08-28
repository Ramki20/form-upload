package gov.usda.fsa.eas.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import gov.usda.fsa.fcao.flp.constants.OfficeTypeConstants;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASNatsUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.auth.UserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.MRTFacadeBusinessService;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceCenterFLPCodeManager;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.StateAbbrFromStateFLPLookUp;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.OfficeInfoCacheManager;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.flpids.util.ExternalDependenciesMockBase;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class EASUserProfile_UT extends ExternalDependenciesMockBase {

	protected HttpServletRequest mockRequest;
	protected AuthorizationManagerImpl mockAuthManager;
	protected String currentUser = "OfficeManager";
	protected NormalizeAreaOfResponsibility mockProcessor;
	protected String privateMethodName = "setNormalizeAreaOfResponsibility";
	protected List<String> userRoles;
	protected List<String> easListOfAttribute;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		mockRequest = mock(HttpServletRequest.class);
		mockAuthManager = mock(AuthorizationManagerImpl.class);
		easListOfAttribute = new ArrayList<String>();
		when(mockAuthManager.getListAttribute(EASUserProfile.DLS_GENERAL_AREA_OF_RESPONSIBILITY))
				.thenReturn(easListOfAttribute);
		when(mockAuthManager.getListAttribute(EASUserProfile.NATS_GENERAL_AREA_OF_RESPONSIBILITY))
				.thenReturn(easListOfAttribute);
		when(mockAuthManager.getListAttribute(EASUserProfile.DLS_1A_SUBMISSION_AREA_OF_RESPONSIBILITY))
				.thenReturn(easListOfAttribute);
		AuthorizationManager.setAuthorizationManagerImpl(mockAuthManager);
		mockProcessor = mock(NormalizeAreaOfResponsibility.class);

		injectMockProcessor();
	}

	@Test
	public void test_EASUserProfile_NationalOnlyUserWithNATSAdmin() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addNationalOfficeRole();
		addNatsAdminNationalRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isNationalOnlyRole());
	}

	@Test
	public void test_EASUserProfile_NationalOnlyUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addNationalOfficeRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.isNationalOnlyRole());
	}

	@Test
	public void test_EASUserProfile_NationalAndStateUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addNationalOfficeRole();
		this.addStateRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isNationalOnlyRole());
	}

	@Test
	public void test_EASUserProfile_NationalAndServiceCenterUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addNationalOfficeRole();
		this.addServiceCenterRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isNationalOnlyRole());
	}

	@Test
	public void test_EASUserProfile_FinanceOnlyUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addFinanceRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isFinanceRole());
		Assert.assertTrue(userProfile.isFinanceOnlyRole());
	}

	@Test
	public void test_EASUserProfile_FinanceAndStateUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addFinanceRole();
		addStateRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isFinanceOnlyRole());
	}

	@Test
	public void test_EASUserProfile_FinanceAndServiceCenterUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		addFinanceRole();
		this.addServiceCenterRole();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isFinanceOnlyRole());
	}

	@Test
	public void testEASUserProfileNotAuthorized() throws Exception {
		EASUserProfile instance = new EASUserProfile(mockRequest);
		DLSAgencyToken agencyToken = (DLSAgencyToken) instance.getAgencyToken();
		Assert.assertNotNull(agencyToken);
		Assert.assertTrue(agencyToken instanceof DLSAgencyToken);

		Assert.assertEquals(OfficeTypeConstants.TRANSACTION_SERVICE_CENTER, agencyToken.getFlpOfficeTypeCode());
		Assert.assertTrue(instance.getEASRoles().isEmpty());

	}

	@Test
	public void testEASUserProfileNoPermission() throws Exception {
		mockUserAuthenticated();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(StringUtil.isEmptyString(userProfile.getEAuthID()));
	}

	@Test
	public void testEASUserProfileNullCurrentUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertFalse(StringUtil.isEmptyString(userProfile.getEAuthID()));
	}

	@Test
	public void testEASUserProfileEmptyCurrentUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		when(mockAuthManager.getCurrentUser()).thenReturn("");

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertTrue(StringUtil.isEmptyString(((EASUserProfile) userProfile).getCurrentUser()));
	}

	@Test
	public void testEASUserProfileWithCurrentUser() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();

		UserProfile userProfile = new EASUserProfile(mockRequest);

		Assert.assertNotNull(userProfile);

		Assert.assertFalse(userProfile.isAssignedOffice("01300"));
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.isStateRole());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedJurisdiction().isEmpty());
		Assert.assertTrue(userProfile.getJurisdiction().isEmpty());

		Assert.assertFalse(userProfile.isNatsNationalAdminRole());
		Assert.assertFalse(userProfile.isNatsStateAdminRole());
		Assert.assertFalse(userProfile.isInUserAreaOfResponsiblity("01300"));

		Assert.assertFalse(userProfile.isInUserCombinedJurisdiction("01300"));
		Assert.assertFalse(userProfile.isInUserJurisdiction("01300"));

	}

	@Test
	public void test_EASNATSUserProfileWithCurrentUser_invalid_officeCode() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectNatsMockProcessor();
		addNatsAdminNationalRole();
		easListOfAttribute.add("00000");

		UserProfile userProfile = new EASNatsUserProfile(mockRequest);

		Assert.assertNotNull(userProfile);

		Assert.assertFalse(userProfile.isAssignedOffice("01300"));
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.isStateRole());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedJurisdiction().isEmpty());
		Assert.assertTrue(userProfile.getJurisdiction().isEmpty());

		Assert.assertTrue(userProfile.isNatsNationalAdminRole());
		Assert.assertFalse(userProfile.isNatsStateAdminRole());
		Assert.assertFalse(userProfile.isInUserAreaOfResponsiblity("01300"));

		Assert.assertFalse(userProfile.isInUserCombinedJurisdiction("01300"));
		Assert.assertFalse(userProfile.isInUserJurisdiction("01300"));

	}

	@Test
	public void test_EASNATSUserProfileWithCurrentUser_Valid_officeCode() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectNatsMockProcessor();
		addNatsAdminNationalRole();
		easListOfAttribute.add("00000");
		easListOfAttribute.add("01300");
		easListOfAttribute.add("01310");

		UserProfile userProfile = new EASNatsUserProfile(mockRequest);

		Assert.assertNotNull(userProfile);
		Assert.assertNotNull(userProfile.getEmployeeData());
		Assert.assertFalse(userProfile.isAssignedOffice("01300"));
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedJurisdiction().isEmpty());
		Assert.assertFalse(userProfile.getJurisdiction().isEmpty());

		Assert.assertTrue(userProfile.isInUserCombinedJurisdiction("01300"));
		Assert.assertTrue(userProfile.isInUserJurisdiction("01300"));

		Assert.assertTrue(userProfile.isNatsNationalAdminRole());
		Assert.assertFalse(userProfile.isNatsStateAdminRole());
		Assert.assertTrue(userProfile.isInUserAreaOfResponsiblity("01310"));

		Assert.assertTrue(userProfile.isInUserCombinedJurisdiction("01310"));
		Assert.assertTrue(userProfile.isInUserJurisdiction("01310"));

		Assert.assertFalse(userProfile.isInUserJurisdiction("01320"));

	}

	@Test
	public void testEASUserProfileWithStateRole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addStateRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);

		Assert.assertNotNull(eASUserProfile);
	}

	@Test
	public void testEASUserProfileWithOfficeRole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addFinanceRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
	}

	@Test
	public void testEASUserProfileWithFLOORole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addFinanceRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();
		DLSAgencyToken token = (DLSAgencyToken) eASUserProfile.getAgencyToken();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
		Assert.assertEquals(OfficeTypeConstants.TRANSACTION_OWNER_FO, token.getFlpOfficeTypeCode());

		Assert.assertTrue(eASUserProfile.getCombinedAreaOfResponsibility().isEmpty());
	}

	@Test
	public void testEASUserProfileWithNORole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addNationalOfficeRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();
		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);

		Assert.assertTrue(eASUserProfile.getCombinedAreaOfResponsibility().isEmpty());
	}

	@Test
	public void testEASUserProfileWithNO() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addNationalOfficeRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();
		DLSAgencyToken token = (DLSAgencyToken) eASUserProfile.getAgencyToken();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
		Assert.assertEquals(OfficeTypeConstants.TRANSACTION_NATIONAL, token.getFlpOfficeTypeCode());
	}

	@Test
	public void testEASUserProfileWithSORole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addStateRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();
		DLSAgencyToken token = (DLSAgencyToken) eASUserProfile.getAgencyToken();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
		Assert.assertEquals(OfficeTypeConstants.TRANSACTION_STATE, token.getFlpOfficeTypeCode());
	}

	@Test
	public void testEASUserProfileWithSCRole() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addServiceCenterRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest);
		String eAuthStr = eASUserProfile.getEauthMapString();
		DLSAgencyToken token = (DLSAgencyToken) eASUserProfile.getAgencyToken();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
		Assert.assertEquals(OfficeTypeConstants.TRANSACTION_SERVICE_CENTER, token.getFlpOfficeTypeCode());
	}

	protected void mockUserAuthenticated() throws AuthorizationException {
		when(mockAuthManager.getAttribute(eq("eas.user.found"))).thenReturn("true");
		when(mockAuthManager.getCurrentUser()).thenReturn(currentUser);
	}

	protected void addStateRole() {
		userRoles.add(Role.STATE_OFFICE.getKey());
	}

	protected void addFinanceRole() {
		userRoles.add(Role.FINANCIAL_OFFICE.getKey());
	}

	protected void addNationalOfficeRole() {
		userRoles.add(Role.NATIONAL_OFFICE.getKey());
	}

	protected void addServiceCenterRole() {
		userRoles.add(Role.SERVICE_CENTER.getKey());
	}

	protected void addNatsAdminNationalRole() {
		userRoles.add(Role.NATS_ADMIN_NO.getKey());
	}

	protected void mockUserRoles() throws AuthorizationException {
		if (userRoles == null) {
			userRoles = new ArrayList<String>();
		} else {
			userRoles.clear();
		}
		userRoles.add(Role.DLSAPP_VIEW.getKey());
		when(mockAuthManager.getUserRoles()).thenReturn(userRoles);
		Map<String, String> eAuthMap = new HashMap<String, String>();
		eAuthMap.put("usda_eauth_id", "Tester");
		when(mockAuthManager.getMapAttribute(eq("eauth.attributes.map"))).thenReturn(eAuthMap);
	}

	private void injectNatsMockProcessor() throws Exception {
		UserProfile eASUserProfileInstance = null;
		Constructor<?> constructor = ReflectionUtility.getDefaultConstructor(EASNatsUserProfile.class);

		eASUserProfileInstance = (EASNatsUserProfile) constructor.newInstance();
		ServiceAgentFacade.setLAZYLOADING(true);
		ServiceAgentFacade serviceAgentFacade = ServiceAgentFacade.getInstance();
		StateAbbrFromStateFLPLookUp mockStateAbbrFromStateFLPLookUp = mock(StateAbbrFromStateFLPLookUp.class);
		ReflectionUtility.setAttribute(serviceAgentFacade, mockStateAbbrFromStateFLPLookUp,
				"stateAbbrFromStateFLPLookUp");
		ServiceCenterFLPCodeManager mockServiceCenterFLPCodeManager = mock(ServiceCenterFLPCodeManager.class);
		ReflectionUtility.setAttribute(serviceAgentFacade, mockServiceCenterFLPCodeManager,
				"serviceCenterFLPCodeManager");

		NormalizeAreaOfResponsibility normalizeAreaOfResponsibility = (NormalizeAreaOfResponsibility) ReflectionUtility
				.createObject(NormalizeAreaOfResponsibility.class);
		Method method = ReflectionUtility.getPrivateMethod(privateMethodName, EASUserProfile.class);
		method.invoke(eASUserProfileInstance, normalizeAreaOfResponsibility);

		mocMrtFacadeBusinessService = mock(MRTFacadeBusinessService.class);
		when(mocMrtFacadeBusinessService.getStatesList()).thenReturn(populateStateList());
		ReflectionUtility.setAttribute(serviceAgentFacade, mocMrtFacadeBusinessService, "mrtFacadeBusinessService");

		IMRTProxyBS mockIMRTProxyBS = mock(IMRTProxyBS.class);
		ReflectionUtility.setAttribute(serviceAgentFacade, mockIMRTProxyBS, "mrtProxyBusinessService");
	}

	protected void injectMockProcessor() throws Exception {
		EASUserProfile eASUserProfileInstance = null;
		Constructor<?> constructor = ReflectionUtility.getDefaultConstructor(EASUserProfile.class);

		eASUserProfileInstance = (EASUserProfile) constructor.newInstance();

		Method method = ReflectionUtility.getPrivateMethod(privateMethodName, EASUserProfile.class);
		method.invoke(eASUserProfileInstance, mockProcessor);
	}

	@Test
	public void testEASUserProfile_WithAgencyToken() throws Exception {
		EASUserProfile instance = new EASUserProfile(mockRequest, new DLSAgencyToken());

		Assert.assertTrue(instance.getAgencyToken() instanceof DLSAgencyToken);
		Assert.assertTrue(instance.getEASRoles().isEmpty());

	}

	// @Test(expected = AuthorizationException.class)
	public void testEASUserProfileNoPermission_WithAgencyToken() throws Exception {
		mockUserAuthenticated();
		UserProfile userProfile = new EASUserProfile(mockRequest, new DLSAgencyToken());

		Assert.assertTrue(StringUtil.isEmptyString(userProfile.getEAuthID()));

	}

//	@Test (expected=AuthorizationException.class)
	public void testEASUserProfileNullCurrentUser_WithAgencyToken() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();

		UserProfile userProfile = new EASUserProfile(mockRequest, new DLSAgencyToken());

		Assert.assertFalse(StringUtil.isEmptyString(userProfile.getEAuthID()));
	}

	@Test
	public void testEASUserProfileWithCurrentUser_WithAgencyToken() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest, new DLSAgencyToken());

		Assert.assertNotNull(eASUserProfile);
	}

	@Test
	public void testEASUserProfileWithStateRole_WithAgencyToken() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addStateRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest, new DLSAgencyToken());

		Assert.assertNotNull(eASUserProfile);
	}

	@Test
	public void testEASUserProfileWithOfficeRole_WithAgencyToken() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		addFinanceRole();
		injectMockProcessor();

		EASUserProfile eASUserProfile = new EASUserProfile(mockRequest, new DLSAgencyToken());
		String eAuthStr = eASUserProfile.getEauthMapString();

		Assert.assertNotNull(eASUserProfile);
		Assert.assertNotNull(eAuthStr);
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_invalidState() throws Exception {
		mockUserAuthenticated();
		mockUserRoles();
		injectMockProcessor();
		
		UserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.STATE_OFFICE);
		Collection<String> jurisdictions = new HashSet<String>();
		jurisdictions.add("01301");
		jurisdictions.add("03305");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

		boolean value = userProfile.isInUserAreaOfResponsiblity("01300");

		Assert.assertFalse(value);
		Assert.assertNotNull(userProfile.getEmployeeData());

		if (userProfile.getEmployeeData().getPartyBO() != null) {
			Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
		}
		Assert.assertNull(userProfile.getEmployeeData().getEmail());
		Assert.assertNull(userProfile.getEmployeeData().getOfficeName());
		Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		if (userProfile.getEmployeeData().getOfficeAddressBO() != null) {
			Assert.assertNotNull(userProfile.getEmployeeData().getOfficeAddressBO());
		}
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_validState() throws Exception {
		try (MockedStatic<OfficeInfoCacheManager> mockedOfficeInfo = mockStatic(OfficeInfoCacheManager.class)) {
			mockUserAuthenticated();
			mockUserRoles();
			injectMockProcessor();
			
			UserProfile userProfile = new EASUserProfile(mockRequest);

			userProfile.getRoles().add(Role.STATE_OFFICE);
			Collection<String> jurisdictions = new HashSet<String>();
			jurisdictions.add("01301");
			jurisdictions.add("02305");
			ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
			ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

			userProfile.getObligationSubmissionAreaOfResponsibility().add("02305");

			Map<String, String> mapAttrib = new HashMap<String, String>();
			mapAttrib.put("02", "02");

			mockedOfficeInfo.when(OfficeInfoCacheManager::getStateMap).thenReturn(mapAttrib);

			boolean value = userProfile.isInUserAreaOfResponsiblity("02300");

			Assert.assertTrue(value);
			Assert.assertNotNull(userProfile.getEmployeeData());

			if (userProfile.getEmployeeData().getPartyBO() != null) {
				Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
			}
			Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		}
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_invalidMailCode() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.STATE_OFFICE);
		Collection<String> jurisdictions = new HashSet<String>();
		jurisdictions.add("21301");
		jurisdictions.add("31305");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

		boolean value = userProfile.isInUserAreaOfResponsiblity("99301");

		Assert.assertFalse(value);
		Assert.assertNotNull(userProfile.getEmployeeData());
		Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_FO_invalidMailCode() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.FINANCIAL_OFFICE);
		Collection<String> jurisdictions = new HashSet<String>();
		jurisdictions.add("21301");
		jurisdictions.add("31305");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

		boolean value = userProfile.isInUserAreaOfResponsiblity("99301");

		Assert.assertFalse(value);
		Assert.assertNotNull(userProfile.getEmployeeData());
		Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_NO_invalidMailCode() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.NATIONAL_OFFICE);
		Collection<String> jurisdictions = new HashSet<String>();
		jurisdictions.add("21301");
		jurisdictions.add("31305");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

		boolean value = userProfile.isInUserAreaOfResponsiblity("99301");

		Assert.assertFalse(value);
		Assert.assertNotNull(userProfile.getEmployeeData());
		Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_IT_invalidMailCode() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.FLP_IT_USER);
		Collection<String> jurisdictions = new HashSet<String>();
		jurisdictions.add("21301");
		jurisdictions.add("31305");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "generalAreaOfResponsibility");
		ReflectionUtility.setAttribute(userProfile, jurisdictions, "combinedAreaOfResponsibility");

		boolean value = userProfile.isInUserAreaOfResponsiblity("99301");

		Assert.assertFalse(value);
		Assert.assertNotNull(userProfile.getEmployeeData());
		Assert.assertNull(userProfile.getEmployeeData().getEmployeeId());
		Assert.assertNotNull(userProfile.getEmployeeData().getPartyBO());
	}

	@Test
	public void test_isInUserAreaOfResponsiblity_mail_code_null() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		boolean value = userProfile.isInUserAreaOfResponsiblity(null);

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_null() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity(null);

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_invalid() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("99345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_noRole() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("21345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_SCRole_noSubmissionPermission()
			throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);
		userProfile.getRoles().add(Role.SERVICE_CENTER);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("21345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_SCRole_withSubmissionPermission()
			throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);
		userProfile.getRoles().add(Role.SERVICE_CENTER);
		userProfile.getObligationSubmissionAreaOfResponsibility().add("21345");

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("21345");

		Assert.assertTrue(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_SO_Role() throws Exception {
		try (MockedStatic<OfficeInfoCacheManager> mockedOfficeInfo = mockStatic(OfficeInfoCacheManager.class)) {
			UserProfile userProfile = new EASUserProfile(mockRequest);
			userProfile.getRoles().add(Role.STATE_OFFICE);
			userProfile.getObligationSubmissionAreaOfResponsibility().add("21345");
			userProfile.getObligationSubmissionAreaOfResponsibility().add("51345");

			Map<String, String> mapAttrib = new HashMap<String, String>();
			mapAttrib.put("21", "21");

			mockedOfficeInfo.when(OfficeInfoCacheManager::getStateMap).thenReturn(mapAttrib);

			boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("21345");

			Assert.assertTrue(value);
		}
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_SO_Role_multiStateCode()
			throws Exception {
		try (MockedStatic<OfficeInfoCacheManager> mockedOfficeInfo = mockStatic(OfficeInfoCacheManager.class)) {
			UserProfile userProfile = new EASUserProfile(mockRequest);
			userProfile.getRoles().add(Role.STATE_OFFICE);
			userProfile.getObligationSubmissionAreaOfResponsibility().add("49300");
			userProfile.getObligationSubmissionAreaOfResponsibility().add("51345");

			Map<String, String> mapAttrib = new HashMap<String, String>();
			mapAttrib.put("51", "51");

			mockedOfficeInfo.when(OfficeInfoCacheManager::getStateMap).thenReturn(mapAttrib);

			boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("51345");

			Assert.assertTrue(value);
		}
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_IT_Role() throws Exception {
		try (MockedStatic<OfficeInfoCacheManager> mockedOfficeInfo = mockStatic(OfficeInfoCacheManager.class)) {
			UserProfile userProfile = new EASUserProfile(mockRequest);
			userProfile.getRoles().add(Role.FLP_IT_USER);

			userProfile.getObligationSubmissionAreaOfResponsibility().add("51345");

			Map<String, String> mapAttrib = new HashMap<String, String>();
			mapAttrib.put("51", "51");

			mockedOfficeInfo.when(OfficeInfoCacheManager::getStateMap).thenReturn(mapAttrib);

			boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("51345");

			Assert.assertTrue(value);
		}
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_invalid_IT_Role() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);
		userProfile.getRoles().add(Role.FLP_IT_USER);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("99345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_FO_Role() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);
		userProfile.getRoles().add(Role.FINANCIAL_OFFICE);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("99345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isInUserObligationSubmissionAreaOfResponsiblity_mail_code_valid_NO_Role() throws Exception {
		UserProfile userProfile = new EASUserProfile(mockRequest);
		userProfile.getRoles().add(Role.FINANCIAL_OFFICE);

		boolean value = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("99345");

		Assert.assertFalse(value);
	}

	@Test
	public void test_isNatsRole_forStateOffice() throws Exception {
		EASUserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.STATE_OFFICE);

		Assert.assertTrue(userProfile.getNATSRoles().isEmpty());
	}

	@Test
	public void test_isNatsRole_ForNATSAdmin() throws Exception {
		EASUserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.NATS_ADMIN);

		Assert.assertFalse(userProfile.getNATSRoles().isEmpty());
	}

	@Test
	public void test_getAgencyCode() throws Exception {
		EASUserProfile userProfile = new EASUserProfile(mockRequest);

		userProfile.getRoles().add(Role.NATS_ADMIN);

		Assert.assertEquals(EASUserProfile.AGENCY_CODE, userProfile.getUserAgencyCode());
	}

	@Test
	public void test_getUserEASServiceCenterList() throws Exception {
		EASUserProfile userProfile = new EASUserProfile(mockRequest);

		List<ServiceCenterFlpOfficeCodeBO> sclist = userProfile.getUserEASServiceCenterList();

		Assert.assertNotNull(sclist);
		Assert.assertTrue(sclist.isEmpty());
	}
}