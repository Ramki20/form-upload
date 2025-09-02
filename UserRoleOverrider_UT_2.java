package gov.usda.fsa.fcao.flp.security;

import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

@ExtendWith(MockitoExtension.class)
public class UserRoleOverrider_UT extends DLSExternalCommonTestMockBase {
    
    private EASUserProfile userProfile;

    @BeforeAll
    public static void classScopeSetUp() throws Exception {
        // Call parent's JNDI setup
        jndiSetup();
        
        // Add specific JNDI bindings for UserRoleOverrider
        configureUserRoleOverriderBindings();
    }
    
    private static void configureUserRoleOverriderBindings() throws NamingException {
        // Configure EAS role override support
        when(mockContext.lookup("java:comp/env/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn("Y");
        when(mockContext.lookup("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn("Y");
        when(mockSubContext.lookup("gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn("Y");
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp(); // This calls testJndiConfig() from parent class
        
        userProfile = (EASUserProfile) ReflectionUtility.createObject(EASUserProfile.class);
    }

    @Test
    public void test_overrider_serviceCenter01() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_serviceCenter58() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_serviceCenter10() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_stateOffice02() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    // @Test - Commented out in original, keeping the same
    public void test_overrider_stateOffice_Texas() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_stateOffice_Missouri() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrideDistrictOffice() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_National_Office() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_National_Office_fsfl() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_National_Office_with_StateRole() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_Finance_Office() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_Finance_Office_with_StateRole() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_IT_Role() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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
    }

    @Test
    public void test_overrider_another_IT_FLP() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
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

    /**
     * Helper method to set up JNDI mocking for individual tests
     * This handles the specific JNDI lookups needed by UserRoleOverrider
     */
    private void setupJndiMockForTest(MockedStatic<InitialContext> mockedStatic, String supportOverrideValue) throws NamingException {
        InitialContext mockInitialContext = mock(InitialContext.class);
        mockedStatic.when(() -> new InitialContext()).thenReturn(mockInitialContext);
        mockedStatic.when(() -> new InitialContext(any(Hashtable.class))).thenReturn(mockInitialContext);
        
        // Configure namespace root and basic structure
        when(mockInitialContext.lookup("java:comp/env/name_space_root")).thenReturn("cell/persistent");
        when(mockInitialContext.lookup("cell/persistent")).thenReturn(mockSubContext);
        
        // Configure the key JNDI lookup for EAS role override support
        when(mockInitialContext.lookup("java:comp/env/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn(supportOverrideValue);
        when(mockInitialContext.lookup("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn(supportOverrideValue);
        when(mockSubContext.lookup("gov/usda/fsa/fcao/flp/dls/support_overrideeas_roles")).thenReturn(supportOverrideValue);
        
        // Configure other standard JNDI lookups that might be needed
        when(mockInitialContext.lookup("java:comp/env/application_identifier")).thenReturn("cbs-client");
        when(mockInitialContext.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockInitialContext.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
    }
}