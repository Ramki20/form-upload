package gov.usda.fsa.eas.auth;

import static org.mockito.Mockito.when;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASNatsUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.auth.UserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EASNatsUserProfile_UT extends EASUserProfile_UT {
	
	@Test
	public void test_EASNatsUserProfile() throws Exception {
		mockUserAuthenticated();
		when(mockAuthManager.getCurrentUser()).thenReturn(currentUser);
		mockUserRoles();
		injectMockProcessor();
		addNatsOfficeRole();
		addNatsAdminNationalRole();
		
		UserProfile userProfile = new EASNatsUserProfile(mockRequest);
		
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.getNATSRoles().isEmpty());
	}
	
	@Test
	public void test_EASNatsUserProfile_ITRole() throws Exception {
		mockUserAuthenticated();
		when(mockAuthManager.getCurrentUser()).thenReturn(currentUser);
		mockUserRoles();
		injectMockProcessor();
		addNatsOfficeRole();
		addNatsITRole();
		
		UserProfile userProfile = new EASNatsUserProfile(mockRequest);
		
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.isNatsNationalAdminRole());
		Assert.assertFalse(userProfile.isNatsStateAdminRole());
		Assert.assertFalse(userProfile.getNATSRoles().isEmpty());
	}
	
	@Test
	public void test_EASNatsUserProfile_ITRole_default() throws Exception {
		mockUserAuthenticated();
		when(mockAuthManager.getCurrentUser()).thenReturn(currentUser);
		mockUserRoles();
		injectMockProcessor();
		addNatsITRole();
		
		UserProfile userProfile = new EASNatsUserProfile(mockRequest);
		
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.isNatsNationalAdminRole());
		Assert.assertTrue(userProfile.isNatsStateAdminRole());
		
		Assert.assertFalse(userProfile.getNATSRoles().isEmpty());
		
		Assert.assertTrue(userProfile.isNatsNationalAdminRole());
		Assert.assertTrue(userProfile.isNatsStateAdminRole());
	}
	
	protected void addNatsOfficeRole(){
		userRoles.add(Role.NATS_ADMIN_NO.getKey());
		userRoles.add(Permission.EAS_KEY_NATS_APPRAISER);
	}
	
	protected void addNatsITRole(){
		userRoles.add(Role.FLP_IT_USER.getKey());
	}
	
	@Override
	protected void injectMockProcessor() throws Exception{
		EASNatsUserProfile eASUserProfileInstance = null;
		Constructor<?> constructor = ReflectionUtility.getDefaultConstructor(EASNatsUserProfile.class);
		
		eASUserProfileInstance = (EASNatsUserProfile)constructor.newInstance();
		
		Method method = ReflectionUtility.getPrivateMethod(privateMethodName,EASUserProfile.class);
		method.invoke(eASUserProfileInstance, mockProcessor);		
	}
}