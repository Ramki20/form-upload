package gov.usda.fsa.fcao.flp.flpids.util;

import static org.mockito.Mockito.when;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.OfficeInfoCacheManager;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Fixed OfficeInfoCacheManager_UT with proper test isolation
 * 
 * This version handles the static initialization issues that occur when
 * tests run together by properly clearing and reinitializing static state.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class OfficeInfoCacheManager_UT extends ExternalDependenciesMockBase {

    private Map<String, String> originalStateMap;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Clear any existing singleton state to ensure clean test environment
        clearServiceAgentFacadeSingleton();
        
        // Clear the static state map before each test
        clearStaticStateMap();
        
        // Set up the mock behavior
        when(mocMrtFacadeBusinessService.getStatesList()).thenReturn(populateStateList());
        
        // Enable lazy loading to prevent automatic initialization
        ServiceAgentFacade.setLAZYLOADING(true);
        
        // Get a fresh instance and inject our mock
        ServiceAgentFacade instance = ServiceAgentFacade.getInstance();
        ReflectionUtility.setAttribute(instance, mocMrtFacadeBusinessService, "mrtFacadeBusinessService");
        
        // Manually populate the state map with our test data
        List<StateBO> testStateList = populateStateList();
        OfficeInfoCacheManager.populateStateList(testStateList);
        
        // Verify the setup worked
        Assert.assertNotNull("State map should be populated after setup", OfficeInfoCacheManager.getStateMap());
    }
    
    @After
    public void tearDown() throws Exception {
        // Clean up static state after each test
        clearStaticStateMap();
        clearServiceAgentFacadeSingleton();
        
        // Reset lazy loading flag
        ServiceAgentFacade.setLAZYLOADING(false);
    }
    
    /**
     * Clear the static state map in OfficeInfoCacheManager
     */
    private void clearStaticStateMap() throws Exception {
        try {
            Field stateMapField = OfficeInfoCacheManager.class.getDeclaredField("STATE_CODE_STATE_ABBR_MAP");
            stateMapField.setAccessible(true);
            Map<String, String> stateMap = (Map<String, String>) stateMapField.get(null);
            stateMap.clear();
        } catch (Exception e) {
            System.err.println("Warning: Could not clear static state map: " + e.getMessage());
            // Create a new empty map if we can't clear the existing one
            Field stateMapField = OfficeInfoCacheManager.class.getDeclaredField("STATE_CODE_STATE_ABBR_MAP");
            stateMapField.setAccessible(true);
            stateMapField.set(null, new HashMap<String, String>());
        }
    }
    
    /**
     * Clear the ServiceAgentFacade singleton instance
     */
    private void clearServiceAgentFacadeSingleton() throws Exception {
        try {
            Field instanceField = ServiceAgentFacade.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            System.err.println("Warning: Could not clear ServiceAgentFacade singleton: " + e.getMessage());
        }
    }
    
    @Test
    public void test_mapPopulation() throws Exception {
        // Act & Assert
        Map<String, String> stateMap = OfficeInfoCacheManager.getStateMap();
        Assert.assertNotNull("State map should not be null", stateMap);
        Assert.assertTrue("State map should not be empty", stateMap.size() > 0);
        
        // Verify some expected entries from our test data
        Assert.assertNotNull("Should have entry for state code 60", stateMap.get("60")); // Alaska
        Assert.assertEquals("Alaska should have abbreviation AK", "AK", stateMap.get("60"));
        Assert.assertNotNull("Should have entry for state code 30", stateMap.get("30")); // Missouri
        Assert.assertEquals("Missouri should have abbreviation MO", "MO", stateMap.get("30"));
    }
    
    @Test
    public void test_isUserInJurisdiction_differentStateFlpOfficeCode() throws Exception {
        // Arrange - Different states: 20 (Kentucky) vs 31 (Montana)
        String mailCode = "20345"; // Kentucky office
        String flpOfficeCode = "31300"; // Montana office
        
        // Act
        boolean result = OfficeInfoCacheManager.isUserInJurisdiction(mailCode, flpOfficeCode);
        
        // Assert
        Assert.assertFalse("Should return false for different states", result);
    }
    
    @Test
    public void test_isUserInJurisdiction_differentStateFlpOfficeCodeSameState() throws Exception {
        // Arrange - Same state: both 20 (Kentucky) but different office codes
        String mailCode = "20345"; // Kentucky office 
        String flpOfficeCode = "21300"; // Different Kentucky office (21 maps to KY too in our test data)
        
        // Act
        boolean result = OfficeInfoCacheManager.isUserInJurisdiction(mailCode, flpOfficeCode);
        
        // Assert
        Assert.assertTrue("Should return true for same state different offices", result);
    }
    
    @Test
    public void test_isUserInJurisdiction_sameStateFlpOfficeCode() throws Exception {
        // Arrange - Exactly same state: both 20 (Kentucky)
        String mailCode = "20345"; // Kentucky office
        String flpOfficeCode = "20300"; // Same Kentucky state office
        
        // Act
        boolean result = OfficeInfoCacheManager.isUserInJurisdiction(mailCode, flpOfficeCode);
        
        // Assert
        Assert.assertTrue("Should return true for same state offices", result);
    }
    
    @Test
    public void test_getStateAbbr_exist() throws Exception {
        // Arrange
        String stateCode = "20"; // Kentucky from our test data
        
        // Act
        String stateAbbr = OfficeInfoCacheManager.getStateAbbr(stateCode);
        
        // Assert
        Assert.assertNotNull("State abbreviation should not be null for existing state", stateAbbr);
        Assert.assertEquals("Should return correct abbreviation for Kentucky", "KY", stateAbbr);
    }
    
    @Test
    public void test_getStateAbbr_notExist() throws Exception {
        // Arrange
        String stateCode = "99"; // Non-existent state code
        
        // Act
        String stateAbbr = OfficeInfoCacheManager.getStateAbbr(stateCode);
        
        // Assert
        Assert.assertNull("State abbreviation should be null for non-existent state", stateAbbr);
    }
    
    @Test
    public void test_getStateAbbr_emptyInput() throws Exception {
        // Arrange
        String stateCode = "";
        
        // Act
        String stateAbbr = OfficeInfoCacheManager.getStateAbbr(stateCode);
        
        // Assert
        Assert.assertEquals("Should return empty string for empty input", "", stateAbbr);
    }
    
    @Test
    public void test_getStateAbbr_nullInput() throws Exception {
        // Arrange
        String stateCode = null;
        
        // Act
        String stateAbbr = OfficeInfoCacheManager.getStateAbbr(stateCode);
        
        // Assert
        Assert.assertEquals("Should return empty string for null input", "", stateAbbr);
    }
    
    @Test
    public void test_getStateAbbr_longInput() throws Exception {
        // Arrange - Input longer than 2 characters should be truncated
        String stateCode = "20345"; // Should use first 2 characters: "20"
        
        // Act
        String stateAbbr = OfficeInfoCacheManager.getStateAbbr(stateCode);
        
        // Assert
        Assert.assertNotNull("State abbreviation should not be null", stateAbbr);
        Assert.assertEquals("Should return abbreviation based on first 2 characters", "KY", stateAbbr);
    }
    
    @Test
    public void test_isUserInJurisdiction_emptyInputs() throws Exception {
        // Test empty mail code
        Assert.assertFalse("Should return false for empty mail code", 
                         OfficeInfoCacheManager.isUserInJurisdiction("", "20300"));
        
        // Test empty office code  
        Assert.assertFalse("Should return false for empty office code",
                         OfficeInfoCacheManager.isUserInJurisdiction("20345", ""));
        
        // Test both empty
        Assert.assertFalse("Should return false for both empty",
                         OfficeInfoCacheManager.isUserInJurisdiction("", ""));
    }
    
    @Test
    public void test_isUserInJurisdiction_nullInputs() throws Exception {
        // Test null mail code
        Assert.assertFalse("Should return false for null mail code",
                         OfficeInfoCacheManager.isUserInJurisdiction(null, "20300"));
        
        // Test null office code
        Assert.assertFalse("Should return false for null office code", 
                         OfficeInfoCacheManager.isUserInJurisdiction("20345", null));
        
        // Test both null
        Assert.assertFalse("Should return false for both null",
                         OfficeInfoCacheManager.isUserInJurisdiction(null, null));
    }
}