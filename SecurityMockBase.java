package gov.usda.fsa.fcao.flp.security.utils;

import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.eas.auth.AuthorizationManagerImpl;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.mockito.Mockito;

public class SecurityMockBase {
	protected HttpServletRequest mockHttpServletRequest;
	protected NormalizeAreaOfResponsibility mockNormalizeAreaOfResponsibility;
	protected  Map<String,String> eauthMap;
	protected List<String> userRoles;
	
	@Before
	public void setUp() throws Exception {
		mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
		mockNormalizeAreaOfResponsibility = Mockito.mock(NormalizeAreaOfResponsibility.class);
		
		Object[] args = new Object[0];
		EASUserProfile easUserProfile = (EASUserProfile)ReflectionUtility.createObject(EASUserProfile.class, args);
		easUserProfile.setNormalizeAreaOfResponsibility(mockNormalizeAreaOfResponsibility);
		
		eauthMap = new HashMap<String,String>();
		eauthMap.put("usda_eauth_id", "2323232323232");
		
		mockAuthorizationManager();
	}
	
	private void mockAuthorizationManager() throws Exception {
		AuthorizationManagerImpl authManager = Mockito.mock(AuthorizationManagerImpl.class);
		Object[] args = new Object[0];
		AuthorizationManager authorizationManager = 
			(AuthorizationManager)ReflectionUtility.createObject(AuthorizationManager.class, args);
		ReflectionUtility.setAttribute(authorizationManager, authManager, "authManager", AuthorizationManager.class);
		
		//Mockito.when(authManager.getMapAttribute( "eauth.attributes.map" )).thenReturn(eauthMap);
		
		//Mockito.when(authManager.getAttribute("eas.user.found")).thenReturn("true");
		//Mockito.when(authManager.getCurrentUser()).thenReturn("testing user");
		
		
		userRoles = new ArrayList<String>();
		userRoles.add(Permission.EAS_KEY_VIEW);
		//Mockito.when(authManager.getUserRoles()).thenReturn(userRoles);
	}
}
