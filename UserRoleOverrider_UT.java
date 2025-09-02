package gov.usda.fsa.fcao.flp.security;

import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class UserRoleOverrider_UT {
	private static SimpleNamingContextBuilder contextBuilder;
	@BeforeClass
	public static void classScopeSetUp() throws Exception{
		contextBuilder = SimpleNamingContextBuilder
				.emptyActivatedContextBuilder();
		contextBuilder.bind("java:comp/env/name_space_root", "cell/persistent");
		contextBuilder.bind("java:comp/env/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles", 
				"Y");
		contextBuilder.bind("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles", 
				"Y");
	}
	
	private EASUserProfile userProfile;
	
	@Before
	public void setUp() throws Exception{
		contextBuilder.activate();
		
		userProfile = (EASUserProfile)ReflectionUtility.createObject(EASUserProfile.class);
	}
	@After
	public void cleanUp () throws Exception{
		contextBuilder.deactivate();
	}
	@Test
	public void test_overrider_serviceCenter01() throws Exception{
		String eAuthID = "EMP0012192";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021026474");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("01300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		
	}
	@Test
	public void test_overrider_serviceCenter58() throws Exception{
		String eAuthID = "EMP0022236";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021016296");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("58300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
	}
	@Test
	public void test_overrider_serviceCenter10() throws Exception{
		String eAuthID = "emp0001920";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021027271");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("10300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
	}
	
	@Test
	public void test_overrider_stateOffice02() throws Exception{
		String eAuthID = "EMP0022320";
		userProfile.getAgencyToken().setUserIdentifier("28200406309030022320");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("02300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	//@Test
	public void test_overrider_stateOffice_Texas() throws Exception{
		String eAuthID = "EMP0007965";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021026877");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("48300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("49300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("50300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_stateOffice_Missouri() throws Exception{
		String eAuthID = "EMP0012302";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021000430");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("01300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("10300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("11300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrideDistrictOffice() throws Exception{
		String eAuthID = "EMP0000281";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021041898");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isDistrictOfficeRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("01300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("10300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("11300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
		Assert.assertFalse(userProfile.isStateRole());
	}
	
	@Test
	public void test_overrider_National_Office() throws Exception{
		String eAuthID = "Emp0022402";
		userProfile.getAgencyToken().setUserIdentifier("28200406309030022402");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertFalse(userProfile.isNationalOnlyRole());
		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_National_Office_fsfl() throws Exception{
		String eAuthID = "EMP0075522";
		userProfile.getAgencyToken().setUserIdentifier("28200406309030075522");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_National_Office_with_StateRole() throws Exception{
		String eAuthID = "EMP0012541";
		userProfile.getAgencyToken().setUserIdentifier("28200406309030012541");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertFalse(userProfile.isNationalOnlyRole());
		Assert.assertTrue(userProfile.isNationalRole());
		Assert.assertTrue(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("01300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("10300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("11300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_Finace_Office() throws Exception{
		String eAuthID = "EMP0021508";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021017578");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isFinanceOnlyRole());
		Assert.assertTrue(userProfile.isFinanceRole());
		Assert.assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_Finace_Office_with_StateRole() throws Exception{
		String eAuthID = "EMP0947151";
		userProfile.getAgencyToken().setUserIdentifier("28200501179040163149");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertFalse(userProfile.isFinanceOnlyRole());
		Assert.assertTrue(userProfile.isFinanceRole());
		Assert.assertTrue(userProfile.isStateRole());
		Assert.assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertFalse(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("01300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("10300"));
		Assert.assertTrue(userProfile.getOfficeAssignments().contains("11300"));
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_IT_Role() throws Exception{
		String eAuthID = "EMP0015529";
		userProfile.getAgencyToken().setUserIdentifier("28200310169021003879");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isITMaintenanceRole());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
	
	@Test
	public void test_overrider_another_IT_FLP() throws Exception{
		String eAuthID = "EMP0020220";
		userProfile.getAgencyToken().setUserIdentifier("28562014021108595421176");
		ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");
		
		UserRoleOverrider.override_user_profile_for_testing_only(userProfile);
		
		Assert.assertTrue(userProfile.isITMaintenanceRole());
		Assert.assertFalse(userProfile.isFinanceOnlyRole());
		Assert.assertFalse(userProfile.isFinanceRole());
		Assert.assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
		Assert.assertTrue(userProfile.getOfficeAssignments().isEmpty());
		Assert.assertEquals(eAuthID, userProfile.getEAuthID());
		Assert.assertFalse(userProfile.isNationalRole());
		Assert.assertFalse(userProfile.isServiceCenterRole());
	}
}
