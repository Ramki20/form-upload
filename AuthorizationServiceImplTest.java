package gov.usda.fsa.fcao.flp.ola.core.fsa.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.usda.fsa.fcao.flp.ola.core.bo.OLACoreUser;
import gov.usda.fsa.fcao.flp.ola.core.enums.OLARolesType;
import gov.usda.fsa.fcao.flp.ola.core.fsa.service.IAuthorizationService;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.EASClient;


@SuppressWarnings("deprecation")
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

	@InjectMocks
	private AuthorizationServiceImpl authorizationServiceImpl = new AuthorizationServiceImpl();

	@Mock
	OlaAgencyToken olaToken;

	@Mock
	IAuthorizationService authorizationService;
	
	OLACoreUser olaCoreUser;
	
	@Mock
	EASClient easClient;
	
	@BeforeEach
	public void setup() {
		
		olaCoreUser = new OLACoreUser();
		olaCoreUser.setCoreCustomerID("100");
		olaCoreUser.setEasRoleList(new ArrayList<String>());
		olaCoreUser.getEasRoleList().add(OLARolesType.SERVICE_CENTER_ROLE.getCode());
		olaCoreUser.setOfficeIdList(new ArrayList<String>());
		olaCoreUser.getOfficeIdList().add("300");
 
	}
	
 	@Test
	public void testGetAuthorizationServiceNullOLACoreUserObject() {
 		
 		authorizationServiceImpl.initializeMapCache();
 		
		OLACoreUser actual = authorizationServiceImpl.findOLACoreUser("eAuthId");
		
		Assertions.assertNotNull(actual);
		Assertions.assertTrue(actual.getIdentifier().equals("eAuthId"));
		Assertions.assertNull(actual.getCoreCustomerID());
		
	}

 	@Test
	public void testGetAuthorizationService() {
 		
 		authorizationServiceImpl.initializeMapCache();
 		
 		Map<String, OLACoreUser> olaCoreUserMap = authorizationServiceImpl.getOLACoreUserMap();
 		OLACoreUser olaCoreUser = new OLACoreUser();
 		olaCoreUser.setIdentifier("eAuthId");
 		olaCoreUser.setCoreCustomerID("1001");
 		olaCoreUserMap.put("eAuthId", olaCoreUser);
 		
		OLACoreUser actual = authorizationServiceImpl.findOLACoreUser("eAuthId");
		
		Assertions.assertNotNull(actual);
		Assertions.assertTrue(actual.getIdentifier().equals("eAuthId"));
		Assertions.assertTrue(actual.getCoreCustomerID().equals("1001"));
		
	}
 	
	@Test
	public void testFindAllOfficeIdentifiers() {
		List<String> actual = authorizationServiceImpl.findAllOfficeIdentifiers("eId");		
 		Assertions.assertNotNull(actual.size());
	}
		

}
