package gov.usda.fsa.fcao.flp.security;

import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import javax.naming.NamingException;

public class UserRoleOverrider_UT extends DLSExternalCommonTestMockBase {
    
    private EASUserProfile userProfile;

    @BeforeClass
    public static void classScopeSetUp() throws Exception {
        // Call parent's JNDI setup - this sets up all the basic JNDI infrastructure
        jndiSetup();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp(); // This calls test_jndiconfig() from parent class
        
        // Set up EAS role override support for all tests
        setupEASRoleOverrideMocks("Y");
        
        userProfile = (EASUserProfile) ReflectionUtility.createObject(EASUserProfile.class);
    }

    @Test
    public void test_overrider_serviceCenter01() throws Exception {
        String eAuthID = "EMP0012192";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021026474");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isServiceCenterRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("01300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
    }

    @Test
    public void test_overrider_serviceCenter58() throws Exception {
        String eAuthID = "EMP0022236";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021016296");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isServiceCenterRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("58300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
    }

    @Test
    public void test_overrider_serviceCenter10() throws Exception {
        String eAuthID = "emp0001920";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021027271");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isServiceCenterRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("10300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
    }

    @Test
    public void test_overrider_stateOffice02() throws Exception {
        String eAuthID = "EMP0022320";
        userProfile.getAgencyToken().setUserIdentifier("28200406309030022320");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isStateRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("02300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    // @Test - Commented out in original, keeping the same
    public void test_overrider_stateOffice_Texas() throws Exception {
        String eAuthID = "EMP0007965";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021026877");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isStateRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("48300"));
        assertTrue(userProfile.getOfficeAssignments().contains("49300"));
        assertTrue(userProfile.getOfficeAssignments().contains("50300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_stateOffice_Missouri() throws Exception {
        String eAuthID = "EMP0012302";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021000430");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isStateRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("01300"));
        assertTrue(userProfile.getOfficeAssignments().contains("10300"));
        assertTrue(userProfile.getOfficeAssignments().contains("11300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrideDistrictOffice() throws Exception {
        String eAuthID = "EMP0000281";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021041898");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isDistrictOfficeRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("01300"));
        assertTrue(userProfile.getOfficeAssignments().contains("10300"));
        assertTrue(userProfile.getOfficeAssignments().contains("11300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
        assertFalse(userProfile.isStateRole());
    }

    @Test
    public void test_overrider_National_Office() throws Exception {
        String eAuthID = "Emp0022402";
        userProfile.getAgencyToken().setUserIdentifier("28200406309030022402");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertFalse(userProfile.isNationalOnlyRole());
        assertTrue(userProfile.isNationalRole());
        assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().isEmpty());
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_National_Office_fsfl() throws Exception {
        String eAuthID = "EMP0075522";
        userProfile.getAgencyToken().setUserIdentifier("28200406309030075522");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isNationalRole());
        assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().isEmpty());
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_National_Office_with_StateRole() throws Exception {
        String eAuthID = "EMP0012541";
        userProfile.getAgencyToken().setUserIdentifier("28200406309030012541");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertFalse(userProfile.isNationalOnlyRole());
        assertTrue(userProfile.isNationalRole());
        assertTrue(userProfile.isStateRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("01300"));
        assertTrue(userProfile.getOfficeAssignments().contains("10300"));
        assertTrue(userProfile.getOfficeAssignments().contains("11300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isFinanceRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_Finance_Office() throws Exception {
        String eAuthID = "EMP0021508";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021017578");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isFinanceOnlyRole());
        assertTrue(userProfile.isFinanceRole());
        assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().isEmpty());
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isNationalRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_Finance_Office_with_StateRole() throws Exception {
        String eAuthID = "EMP0947151";
        userProfile.getAgencyToken().setUserIdentifier("28200501179040163149");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertFalse(userProfile.isFinanceOnlyRole());
        assertTrue(userProfile.isFinanceRole());
        assertTrue(userProfile.isStateRole());
        assertFalse(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertFalse(userProfile.getOfficeAssignments().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().contains("01300"));
        assertTrue(userProfile.getOfficeAssignments().contains("10300"));
        assertTrue(userProfile.getOfficeAssignments().contains("11300"));
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isNationalRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_IT_Role() throws Exception {
        String eAuthID = "EMP0015529";
        userProfile.getAgencyToken().setUserIdentifier("28200310169021003879");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isITMaintenanceRole());
        assertFalse(userProfile.isFinanceRole());
        assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().isEmpty());
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isNationalRole());
        assertFalse(userProfile.isServiceCenterRole());
    }

    @Test
    public void test_overrider_another_IT_FLP() throws Exception {
        String eAuthID = "EMP0020220";
        userProfile.getAgencyToken().setUserIdentifier("28562014021108595421176");
        ReflectionUtility.setAttribute(userProfile, eAuthID, "eAuthID");

        UserRoleOverrider.override_user_profile_for_testing_only(userProfile);

        assertTrue(userProfile.isITMaintenanceRole());
        assertFalse(userProfile.isFinanceOnlyRole());
        assertFalse(userProfile.isFinanceRole());
        assertTrue(userProfile.getGeneralAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getCombinedAreaOfResponsibility().isEmpty());
        assertTrue(userProfile.getOfficeAssignments().isEmpty());
        assertEquals(eAuthID, userProfile.getEAuthID());
        assertFalse(userProfile.isNationalRole());
        assertFalse(userProfile.isServiceCenterRole());
    }
}