package gov.usda.fsa.fcao.flp.flpids.common.business.common.utilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

@RunWith(MockitoJUnitRunner.class)
public class NormalizeAreaOfResponsibility_UT {
    
    private NormalizeAreaOfResponsibility tester;
    
    @Mock
    private ServiceAgentFacade mockServiceAgentFacade;

    @Before
    public void setUp() throws Exception {
        tester = (NormalizeAreaOfResponsibility) ReflectionUtility.createObject(NormalizeAreaOfResponsibility.class);
        
        // Set the mocked ServiceAgentFacade on the tester
        tester.setServiceAgentFacade(mockServiceAgentFacade);
    }

    @Test
    public void testProcessWithEmptySource() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        String eAuthID = "123232";
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        
        // Verify no interactions with ServiceAgentFacade since source is empty
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test
    public void testProcessWithNullSource() throws Exception {
        String eAuthID = "123232";
        
        Collection<String> result = tester.process(null, eAuthID);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        
        // Verify no interactions with ServiceAgentFacade since source is null
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test
    public void testProcessWithSingleEmptySource() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        source.add("");
        String eAuthID = "123232";
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        
        // Verify no interactions with ServiceAgentFacade since source contains only empty string
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test
    public void testProcessWithWrongLengthOfOfficeCode() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        source.add("12323241"); // Wrong length (8 characters instead of 5)
        String eAuthID = "123232";
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertTrue(result.isEmpty());
        
        // Verify no interactions with ServiceAgentFacade since office code is invalid
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test 
    public void testProcessWithWrongStateCode() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        source.add("99300"); // Invalid state code - but has valid format
        String eAuthID = "123232";
        
        try (MockedStatic<ServiceAgentFacade> mockedStatic = mockStatic(ServiceAgentFacade.class)) {
            // Mock ServiceAgentFacade.getInstance()
            mockedStatic.when(ServiceAgentFacade::getInstance).thenReturn(mockServiceAgentFacade);
            
            // Mock the state abbreviation lookup to return null for invalid state
            when(mockServiceAgentFacade.getAbbreviation("99")).thenReturn(null);
            
            // Mock service center lookup for individual codes (should be called for the state code)
            List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
            when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
                .thenReturn(mockServiceCenters);
            
            Collection<String> result = tester.process(source, eAuthID);
            
            // The invalid state code will still be added to result initially, 
            // but won't have service centers expanded
            assertNotNull(result);
            assertTrue(result.contains("99300"));
            
            verify(mockServiceAgentFacade).getAbbreviation("99");
            verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
        }
    }

    @Test 
    public void testProcessWithCodeNotInteger() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        source.add("afaerer"); // Non-numeric characters
        String eAuthID = "123232";
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertTrue(result.isEmpty());
        
        // Verify no interactions with ServiceAgentFacade since office code is invalid
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test 
    public void testProcessWithSingleValidServiceCenterCode() throws Exception {
        Collection<String> source = new ArrayList<String>(); 
        source.add("01339"); // Valid service center code (ends with 39, not 00)
        String eAuthID = "123232";
        
        // Mock the service center lookup for individual codes
        List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
        ServiceCenterFlpOfficeCodeBO mockServiceCenter = new ServiceCenterFlpOfficeCodeBO("01339", "Test Service Center");
        mockServiceCenters.add(mockServiceCenter);
        
        when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
            .thenReturn(mockServiceCenters);
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("01339"));
        
        // Verify interaction with ServiceAgentFacade for service center lookup
        verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
    }

    @Test 
    public void testProcessWithSingleValidStateCode() throws Exception {
        try (MockedStatic<ServiceAgentFacade> mockedStatic = mockStatic(ServiceAgentFacade.class)) {
            Collection<String> source = new ArrayList<String>(); 
            source.add("01300"); // Valid state code (ends with 00)
            String eAuthID = "123232";
            
            // Mock ServiceAgentFacade.getInstance()
            mockedStatic.when(ServiceAgentFacade::getInstance).thenReturn(mockServiceAgentFacade);
            
            // Mock the state abbreviation lookup
            when(mockServiceAgentFacade.getAbbreviation("01")).thenReturn("AL");
            
            // Mock service center lookup for the state
            List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
            ServiceCenterFlpOfficeCodeBO mockServiceCenter1 = new ServiceCenterFlpOfficeCodeBO("01339", "Test Service Center 1");
            ServiceCenterFlpOfficeCodeBO mockServiceCenter2 = new ServiceCenterFlpOfficeCodeBO("01349", "Test Service Center 2");
            mockServiceCenters.add(mockServiceCenter1);
            mockServiceCenters.add(mockServiceCenter2);
            
            when(mockServiceAgentFacade.retrieveServiceCenterFLPCodesByStateAbbr("AL"))
                .thenReturn(mockServiceCenters);
            when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
                .thenReturn(mockServiceCenters);
            
            Collection<String> result = tester.process(source, eAuthID);
            
            assertNotNull(result);
            // Should contain the original state code plus the service centers for that state
            assertTrue(result.size() >= 1);
            assertTrue(result.contains("01300")); // Original state code
            assertTrue(result.contains("01339")); // Service center 1
            assertTrue(result.contains("01349")); // Service center 2
            
            // Verify interactions
            verify(mockServiceAgentFacade).getAbbreviation("01");
            verify(mockServiceAgentFacade).retrieveServiceCenterFLPCodesByStateAbbr("AL");
            verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
        }
    }

    @Test
    public void testProcessWithMixedValidCodes() throws Exception {
        try (MockedStatic<ServiceAgentFacade> mockedStatic = mockStatic(ServiceAgentFacade.class)) {
            Collection<String> source = new ArrayList<String>();
            source.add("01300"); // State code
            source.add("02339"); // Service center code
            source.add("invalid"); // Invalid code
            String eAuthID = "123232";
            
            // Mock ServiceAgentFacade.getInstance()
            mockedStatic.when(ServiceAgentFacade::getInstance).thenReturn(mockServiceAgentFacade);
            
            // Mock the state abbreviation lookup
            when(mockServiceAgentFacade.getAbbreviation("01")).thenReturn("AL");
            
            // Mock service center lookup for the state
            List<ServiceCenterFlpOfficeCodeBO> mockServiceCentersForState = new ArrayList<>();
            ServiceCenterFlpOfficeCodeBO mockServiceCenter1 = new ServiceCenterFlpOfficeCodeBO("01339", "Alabama Service Center");
            mockServiceCentersForState.add(mockServiceCenter1);
            
            when(mockServiceAgentFacade.retrieveServiceCenterFLPCodesByStateAbbr("AL"))
                .thenReturn(mockServiceCentersForState);
            
            // Mock service center lookup for individual codes
            List<ServiceCenterFlpOfficeCodeBO> mockServiceCentersForCodes = new ArrayList<>();
            ServiceCenterFlpOfficeCodeBO mockServiceCenter2 = new ServiceCenterFlpOfficeCodeBO("02339", "Individual Service Center");
            ServiceCenterFlpOfficeCodeBO mockServiceCenter3 = new ServiceCenterFlpOfficeCodeBO("01300", "State Office");
            mockServiceCentersForCodes.add(mockServiceCenter2);
            mockServiceCentersForCodes.add(mockServiceCenter3);
            
            when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
                .thenReturn(mockServiceCentersForCodes);
            
            Collection<String> result = tester.process(source, eAuthID);
            
            assertNotNull(result);
            assertTrue(result.size() >= 3); // At least the valid codes
            assertTrue(result.contains("01300")); // Original state code
            assertTrue(result.contains("02339")); // Original service center code
            assertTrue(result.contains("01339")); // Service center from state lookup
            assertFalse(result.contains("invalid")); // Invalid code should not be in result
        }
    }

    @Test
    public void testProcessWithSetParameter() throws Exception {
        Collection<String> source = new ArrayList<String>();
        source.add("01339"); // Valid service center code
        String eAuthID = "123232";
        Set<ServiceCenterFlpOfficeCodeBO> serviceCenterListTarget = new TreeSet<>();
        
        // Mock the service center lookup
        List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
        ServiceCenterFlpOfficeCodeBO mockServiceCenter = new ServiceCenterFlpOfficeCodeBO("01339", "Test Service Center");
        mockServiceCenters.add(mockServiceCenter);
        
        when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
            .thenReturn(mockServiceCenters);
        
        Collection<String> result = tester.process(source, eAuthID, serviceCenterListTarget);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("01339"));
        
        // Verify that the service center was added to the target set
        assertEquals(1, serviceCenterListTarget.size());
        // Find the service center in the set and verify it has the correct code
        boolean foundServiceCenter = false;
        for (ServiceCenterFlpOfficeCodeBO sc : serviceCenterListTarget) {
            if ("01339".equals(sc.getCode())) {
                foundServiceCenter = true;
                break;
            }
        }
        assertTrue("Service center should be in the target set", foundServiceCenter);
        
        verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
    }

    @Test
    public void testGetAllStateOfficeCode() throws Exception {
        Collection<String> source = new ArrayList<String>();
        source.add("01300"); // State office code
        source.add("01339"); // Service center code
        source.add("02300"); // Another state office code
        source.add("invalid"); // Invalid code
        
        Collection<String> result = tester.getAllStateOfficeCode(source);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("01300"));
        assertTrue(result.contains("02300"));
        assertFalse(result.contains("01339")); // Service center, not state office
        assertFalse(result.contains("invalid")); // Invalid code
        
        // This method doesn't interact with ServiceAgentFacade
        verifyNoInteractions(mockServiceAgentFacade);
    }

    @Test
    public void testProcessWithNullEAuthID() throws Exception {
        Collection<String> source = new ArrayList<String>();
        source.add("01339"); // Valid service center code
        String eAuthID = null; // Null eAuthID should not cause issues
        
        // Mock the service center lookup
        List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
        ServiceCenterFlpOfficeCodeBO mockServiceCenter = new ServiceCenterFlpOfficeCodeBO("01339", "Test Service Center");
        mockServiceCenters.add(mockServiceCenter);
        
        when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
            .thenReturn(mockServiceCenters);
        
        Collection<String> result = tester.process(source, eAuthID);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("01339"));
        
        verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
    }

    @Test
    public void testProcessWithServiceLookupException() throws Exception {
        try (MockedStatic<ServiceAgentFacade> mockedStatic = mockStatic(ServiceAgentFacade.class)) {
            Collection<String> source = new ArrayList<String>();
            source.add("01300"); // State code that will cause lookup exception
            String eAuthID = "123232";
            
            // Mock ServiceAgentFacade.getInstance()
            mockedStatic.when(ServiceAgentFacade::getInstance).thenReturn(mockServiceAgentFacade);
            
            // Mock the state abbreviation lookup to throw exception
            when(mockServiceAgentFacade.getAbbreviation("01")).thenThrow(new RuntimeException("Service unavailable"));
            
            // Mock service center lookup for individual codes - should still be called
            List<ServiceCenterFlpOfficeCodeBO> mockServiceCenters = new ArrayList<>();
            ServiceCenterFlpOfficeCodeBO mockServiceCenter = new ServiceCenterFlpOfficeCodeBO("01300", "State Office");
            mockServiceCenters.add(mockServiceCenter);
            when(mockServiceAgentFacade.retrieveFlpServiceCentersByFlpOfficeCode(anyList()))
                .thenReturn(mockServiceCenters);
            
            Collection<String> result = tester.process(source, eAuthID);
            
            assertNotNull(result);
            // Should still contain the original state code even if service lookup fails
            assertTrue(result.contains("01300"));
            
            // Verify the exception was handled gracefully
            verify(mockServiceAgentFacade).getAbbreviation("01");
            verify(mockServiceAgentFacade).retrieveFlpServiceCentersByFlpOfficeCode(anyList());
        }
    }
}