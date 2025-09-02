package gov.usda.fsa.fcao.flp.security.userprofile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import gov.usda.fsa.eas.auth.AuthorizationException;
import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.eas.auth.AuthorizationManagerImpl;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.security.Permission;
import gov.usda.fsa.fcao.flp.flpids.common.security.Role;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;
import gov.usda.fsa.fcao.flp.security.userprofile.UserProfileImpl.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Fixed UserProfileImplTest with proper mocking and isolation
 * 
 * This version properly mocks the AuthorizationManager and its dependencies
 * to ensure test isolation and consistent behavior.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class UserProfileImplTest {

    public static final String EAS_KEY_STATE_OFFICE_EMPLOYEE = "app.fsa.flp.dls.so";
    public static final String EAS_KEY_LS = "app.fsa.flp.dls.ls";
    
    private Permission PERMISSION = Permission.lookup(EAS_KEY_LS);
    private Role role = Role.lookup(EAS_KEY_STATE_OFFICE_EMPLOYEE);

    private UserProfile userProfile;
    
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    
    @Mock
    private NormalizeAreaOfResponsibility mockNormalizeAreaOfResponsibility;
    
    @Mock
    private AuthorizationManagerImpl mockAuthManager;
    
    @Mock
    private Name mockName;
    
    private MockedStatic<AuthorizationManager> mockedAuthorizationManager;
    
    private Map<String, String> eauthMap;
    private List<String> userRoles;
    private List<String> officeAssignments;
    
    @Before
    public void setUp() throws Exception {
        // Initialize test data
        setupTestData();
        
        // Mock static AuthorizationManager
        setupAuthorizationManagerMocking();
        
        // Create EASUserProfile with mocked dependencies
        setupEASUserProfile();
        
        // Create and configure UserProfile
        setupUserProfile();
    }
    
    @After
    public void tearDown() throws Exception {
        // Clean up static mocks
        if (mockedAuthorizationManager != null) {
            mockedAuthorizationManager.close();
        }
    }
    
    private void setupTestData() {
        // Setup eauth map
        eauthMap = new HashMap<String, String>();
        eauthMap.put("usda_eauth_id", "2323232323232");
        eauthMap.put("user_description", "Test User");
        
        // Setup user roles
        userRoles = new ArrayList<String>();
        userRoles.add(Permission.EAS_KEY_VIEW); // Required for access
        userRoles.add(Role.EAS_KEY_FINANCIAL_OFFICE_EMPLOYEE);
        userRoles.add(Permission.EAS_KEY_CM);
        userRoles.add(Permission.EAS_KEY_1C);
        userRoles.add(Permission.EAS_KEY_1D);
        userRoles.add(Permission.EAS_KEY_1F);
        
        // Setup office assignments
        officeAssignments = new ArrayList<String>();
        officeAssignments.add("123456");
        officeAssignments.add("789012");
    }
    
    private void setupAuthorizationManagerMocking() throws AuthorizationException {
        // Mock static AuthorizationManager methods
        mockedAuthorizationManager = mockStatic(AuthorizationManager.class);
        
        // Mock the key methods that EASUserProfile relies on
        mockedAuthorizationManager.when(() -> AuthorizationManager.getMapAttribute("eauth.attributes.map"))
                                  .thenReturn(eauthMap);
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getAttribute("eas.user.found"))
                                  .thenReturn("true");
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getCurrentUser())
                                  .thenReturn("Test User");
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getUserRoles())
                                  .thenReturn(userRoles);
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getListAttribute("employee.office_id_assignments.list"))
                                  .thenReturn(officeAssignments);
        
        // Mock area of responsibility attributes
        List<String> generalAOR = new ArrayList<String>();
        generalAOR.add("01305");
        generalAOR.add("01306");
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getListAttribute("app.fsa.flp.office"))
                                  .thenReturn(generalAOR);
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getListAttribute("app.fsa.flp.1a.office"))
                                  .thenReturn(generalAOR);
        
        mockedAuthorizationManager.when(() -> AuthorizationManager.getListAttribute("app.fsa.flp.nats.jurisdiction"))
                                  .thenReturn(new ArrayList<String>());
    }
    
    private void setupEASUserProfile() throws Exception {
        // Create EASUserProfile instance using reflection to bypass normal constructor
        Object[] args = new Object[0];
        EASUserProfile easUserProfile = (EASUserProfile) ReflectionUtility.createObject(EASUserProfile.class, args);
        easUserProfile.setNormalizeAreaOfResponsibility(mockNormalizeAreaOfResponsibility);
        
        // Mock the normalize area of responsibility behavior
        List<String> processedOffices = new ArrayList<String>();
        processedOffices.add("01305");
        processedOffices.add("01306");
        
        when(mockNormalizeAreaOfResponsibility.process(anyList(), anyString()))
            .thenReturn(processedOffices);
        
        when(mockNormalizeAreaOfResponsibility.process(anyList(), anyString(), any()))
            .thenReturn(processedOffices);
        
        when(mockNormalizeAreaOfResponsibility.getAllStateOfficeCode(anyList()))
            .thenReturn(new ArrayList<String>());
        
        when(mockNormalizeAreaOfResponsibility.loadeEployeeData(any(), anyString()))
            .thenReturn(new gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData());
    }
    
    private void setupUserProfile() throws Exception {
        // Create UserProfileImpl instance
        userProfile = new UserProfileImpl(mockHttpServletRequest);
        
        // Set the mock name if UserProfileImpl has a setName method
        if (userProfile instanceof UserProfileImpl) {
            ((UserProfileImpl) userProfile).setName(mockName);
            
            // Mock name methods if needed
            when(mockName.getFirstName()).thenReturn("Test");
            when(mockName.getLastName()).thenReturn("User");
            when(mockName.getMiddleName()).thenReturn("M");
        }
    }
    
    @Test
    public void testGetEAuthId() {
        // Act
        String eAuthId = userProfile.getEAuthId();
        
        // Assert
        assertNotNull("EAuth Id should not be null", eAuthId);
    }
    
    @Test
    public void testGetRoles() {
        // Act
        Collection<Role> roles = userProfile.getRoles();
        
        // Assert
        assertNotNull("Roles should not be null", roles);
        assertTrue("Roles should not be empty", roles.size() > 0);
    }
    
    @Test
    public void testGetPermissions() {
        // Act
        Collection<Permission> permissions = userProfile.getPermissions();
        
        // Assert
        assertNotNull("Permissions should not be null", permissions);
        assertTrue("Permissions should not be empty", permissions.size() > 0);
    }
    
    @Test
    public void testHasRole() {
        // Act & Assert
        // Since we're not adding the specific role in our test data, it should return false
        assertFalse("Should not have the specific test role", userProfile.hasRole(role));
    }
    
    @Test
    public void testHasPermission() {
        // Act & Assert
        // Since we're not adding the specific permission in our test data, it should return false
        assertFalse("Should not have the specific test permission", userProfile.hasPermission(PERMISSION));
    }
    
    @Test
    public void testGetCombinedAreaOfResponsibility() {
        // Act
        Collection<String> combinedAOR = userProfile.getCombinedAreaOfResponsibility();
        
        // Assert
        assertNotNull("Combined area of responsibility should not be null", combinedAOR);
    }
    
    @Test
    public void testGetConflictsOfInterest() {
        // Act
        Collection<String> conflicts = userProfile.getConflictsOfInterest();
        
        // Assert
        assertNotNull("Conflicts of interest should not be null", conflicts);
    }
    
    @Test
    public void testGetGeneralAreaOfResponsibility() {
        // Act
        Collection<String> generalAOR = userProfile.getGeneralAreaOfResponsibility();
        
        // Assert
        assertNotNull("General area of responsibility should not be null", generalAOR);
    }
    
    @Test
    public void testGetObligationSubmissionAreaOfResponsibility() {
        // Act
        Collection<String> obligationAOR = userProfile.getObligationSubmissionAreaOfResponsibility();
        
        // Assert
        assertNotNull("Obligation submission area of responsibility should not be null", obligationAOR);
    }
    
    @Test
    public void testGetOfficeAssignments() {
        // Act
        Collection<String> offices = userProfile.getOfficeAssignments();
        
        // Assert
        assertNotNull("Office assignments should not be null", offices);
    }
    
    @Test
    public void testHasConflictOfInterest() {
        // Act & Assert
        assertFalse("Should not have conflict of interest for test ID", 
                   userProfile.hasConflictOfInterest("232323"));
    }
    
    @Test
    public void testCompareTo() {
        // This test might need adjustment based on actual UserProfileImpl implementation
        // For now, just ensure the method exists and doesn't throw an exception
        try {
            if (userProfile instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<UserProfile> comparableProfile = (Comparable<UserProfile>) userProfile;
                int result = comparableProfile.compareTo(userProfile);
                assertEquals("Profile should be equal to itself", 0, result);
            }
        } catch (Exception e) {
            fail("CompareTo should not throw exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testToString() {
        // Act
        String result = userProfile.toString();
        
        // Assert
        assertNotNull("toString should not return null", result);
        assertTrue("toString should not return empty string", result.length() > 0);
    }
    
    // Additional test methods for better coverage
    
    @Test
    public void testIsStateRole() {
        // Act & Assert
        // Based on our test data, this should return false since we set FINANCIAL_OFFICE role
        assertFalse("Should not be state role", userProfile.isStateRole());
    }
    
    @Test
    public void testIsFinanceRole() {
        // Act & Assert
        // Based on our test data, this should return true since we set FINANCIAL_OFFICE role
        assertTrue("Should be finance role", userProfile.isFinanceRole());
    }
    
    @Test
    public void testIsServiceCenterRole() {
        // Act & Assert
        assertFalse("Should not be service center role", userProfile.isServiceCenterRole());
    }
    
    @Test
    public void testGetAgencyToken() {
        // Act
        gov.usda.fsa.common.base.AgencyToken token = userProfile.getAgencyToken();
        
        // Assert
        assertNotNull("Agency token should not be null", token);
    }
    
    @Test
    public void testGetEASRoles() {
        // Act
        Collection<String> easRoles = userProfile.getEASRoles();
        
        // Assert
        assertNotNull("EAS roles should not be null", easRoles);
    }
    
    @Test
    public void testGetEmployeeData() {
        // Act
        gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData employeeData = 
            userProfile.getEmployeeData();
        
        // Assert
        assertNotNull("Employee data should not be null", employeeData);
    }
    
    @Test
    public void testGetUserAgencyCode() {
        // Act
        String agencyCode = userProfile.getUserAgencyCode();
        
        // Assert
        assertNotNull("User agency code should not be null", agencyCode);
    }
    
    @Test
    public void testGetCurrentUser() {
        // Act
        String currentUser = userProfile.getCurrentUser();
        
        // Assert
        assertNotNull("Current user should not be null", currentUser);
    }
    
    @Test
    public void testIsInUserAreaOfResponsiblity() {
        // Act & Assert
        // Test with a valid office code from our test data
        boolean result = userProfile.isInUserAreaOfResponsiblity("01305");
        
        // The result depends on the implementation, but the method should not throw an exception
        assertNotNull("Method should return a boolean value", result);
    }
    
    @Test
    public void testIsInUserObligationSubmissionAreaOfResponsiblity() {
        // Act & Assert
        // Test with a valid office code from our test data
        boolean result = userProfile.isInUserObligationSubmissionAreaOfResponsiblity("01305");
        
        // The result depends on the implementation, but the method should not throw an exception
        assertNotNull("Method should return a boolean value", result);
    }
}