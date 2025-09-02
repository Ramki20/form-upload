package gov.usda.fsa.fcao.flp.services.interest.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.InterestRateRef;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.ServiceBrokerException;
import gov.usda.fsa.fcao.flp.interest.MartInterestRateBusinessService;

/**
 * Fixed MartInterestRateBusinessService_UT with proper test isolation
 * 
 * Unit tests for MartInterestRateBusinessServiceImpl using Mockito framework.
 * This version creates its own isolated Spring context for consistent testing
 * and uses proper mocking instead of relying on ServiceAgentFacade singleton.
 * 
 * @author FPAC-BC
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class MartInterestRateBusinessService_UT {
    
    private MartInterestRateBusinessService service;
    private MartInterestRateBusinessServiceImpl serviceImpl; // Reference to concrete impl for dependency injection
    private ApplicationContext testContext;
    
    @Mock
    private IMRTProxyBS mockMrtProxyBusinessService;
    
    @Before
    public void setUp() throws Exception {
        // Create isolated Spring context for this test
        createTestSpringContext();
        
        // Get service from our isolated context
        service = testContext.getBean("interestRateBusinessService", MartInterestRateBusinessService.class);
        
        // Get concrete implementation for dependency injection
        if (service instanceof MartInterestRateBusinessServiceImpl) {
            serviceImpl = (MartInterestRateBusinessServiceImpl) service;
        } else {
            // Handle proxy case - try to get the target object
            serviceImpl = extractTargetFromProxy(service);
        }
        
        if (serviceImpl == null) {
            fail("Could not obtain MartInterestRateBusinessServiceImpl instance for testing");
        }
        
        // Inject mocked dependency into the concrete implementation
        serviceImpl.setDataMartBusinessService(mockMrtProxyBusinessService);
    }
    
    @After
    public void tearDown() throws Exception {
        // Close test context if it exists
        if (testContext != null && testContext instanceof org.springframework.context.ConfigurableApplicationContext) {
            ((org.springframework.context.ConfigurableApplicationContext) testContext).close();
        }
    }
    
    private void createTestSpringContext() {
        // Create a fresh Spring context for each test to avoid interference
        String[] springConfigs = {
            "classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/common-external-service-spring-config.xml"
        };
        testContext = new ClassPathXmlApplicationContext(springConfigs);
    }
    
    /**
     * Extract the target object from a Spring proxy if needed
     */
    private MartInterestRateBusinessServiceImpl extractTargetFromProxy(Object proxy) {
        try {
            // Try AOP proxy extraction
            if (proxy.getClass().getName().contains("$Proxy") || 
                proxy.getClass().getName().contains("CGLIB")) {
                
                // Try to get target through AopUtils (if available)
                try {
                    Class<?> aopUtilsClass = Class.forName("org.springframework.aop.support.AopUtils");
                    Method getTargetMethod = aopUtilsClass.getMethod("getTargetObject", Object.class);
                    Object target = getTargetMethod.invoke(null, proxy);
                    if (target instanceof MartInterestRateBusinessServiceImpl) {
                        return (MartInterestRateBusinessServiceImpl) target;
                    }
                } catch (Exception e) {
                    // AopUtils not available or failed, try other approaches
                }
                
                // Try to access through advised interface
                try {
                    if (proxy instanceof org.springframework.aop.framework.Advised) {
                        org.springframework.aop.framework.Advised advised = (org.springframework.aop.framework.Advised) proxy;
                        Object target = advised.getTargetSource().getTarget();
                        if (target instanceof MartInterestRateBusinessServiceImpl) {
                            return (MartInterestRateBusinessServiceImpl) target;
                        }
                    }
                } catch (Exception e) {
                    // Advised interface not available or failed
                }
            }
            
            // If it's already the concrete class, return it
            if (proxy instanceof MartInterestRateBusinessServiceImpl) {
                return (MartInterestRateBusinessServiceImpl) proxy;
            }
            
        } catch (Exception e) {
            System.err.println("Warning: Could not extract target from proxy: " + e.getMessage());
        }
        
        return null;
    }
    
    private AgencyToken createAgencyToken() {
        AgencyToken token = new AgencyToken();
        token.setRequestHost("FCAO");
        token.setApplicationIdentifier("FCAO");
        token.setUserIdentifier("FCAO");
        token.setProcessingNode("DLS_Common");
        token.setReadOnly();
        return token;
    }
    
    @Test
    public void testGetInterestRate_SingleRate() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate1 = new InterestRate();
        rate1.setId(1);
        rate1.setIntRate(new BigDecimal("4.25"));
        rate1.setTypeName("Farm Loan Rate");
        mrtMatchingRates.add(rate1);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null", result);
        assertNotNull("Interest rate should not be null", result.getInterestRate());
        assertEquals("Interest rate should match", new BigDecimal("4.25"), result.getInterestRate());
        assertEquals("Rate identifier should match", "1", result.getRateIdentifier());
        assertEquals("Type name should match", "Farm Loan Rate", result.getTypeName());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }
    
    @Test
    public void testGetInterestRate_NoRatesFound() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "99999"; // Non-existent rate type

        List<InterestRate> emptyRatesList = new ArrayList<InterestRate>();
        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(emptyRatesList);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNull("Result should be null when no rates found", result);
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }
    
    @Test
    public void testGetInterestRate_NullRatesResponse() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(null);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNull("Result should be null when service returns null", result);
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_MultipleRates() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");
        rateTypeIds.add("50020");

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        
        InterestRate rate1 = new InterestRate();
        rate1.setId(1);
        rate1.setIntRate(new BigDecimal("4.25"));
        rate1.setTypeName("Farm Operating Loan Rate");
        
        InterestRate rate2 = new InterestRate();
        rate2.setId(2);
        rate2.setIntRate(new BigDecimal("3.75"));
        rate2.setTypeName("Second Rate");
        
        mrtMatchingRates.add(rate1);
        mrtMatchingRates.add(rate2);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return first rate when multiple available", new BigDecimal("4.25"), result.getInterestRate());
        assertEquals("Should return first rate identifier", "1", result.getRateIdentifier());
        assertEquals("Should return first rate type name", "First Rate", result.getTypeName());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_EmptyResultFromService() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");

        Integer[] expectedTypeIds = { 50010 };
        List<InterestRate> emptyMrtRates = new ArrayList<InterestRate>();
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(emptyMrtRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertTrue("Results should be empty when service returns empty list", results.isEmpty());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRate_ServiceExceptionWithUserContext() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";
        AgencyToken token = createAgencyToken();
        token.setUserIdentifier("testUser123");

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenThrow(new RuntimeException("MRT Service unavailable"));

        // Act & Assert
        try {
            service.getInterestRate(interestRateTypeId, effectiveDate, token);
            fail("Should throw ServiceBrokerException");
        } catch (ServiceBrokerException e) {
            // Verify the exception contains user context
            assertNotNull("Exception should not be null", e);
            assertTrue("Exception should contain error code", e.getMessage().contains("error.unexpected"));
        }
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_NegativeRateTypeId() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("-1"); // Negative rate type ID

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate = new InterestRate();
        rate.setId(-1);
        rate.setIntRate(new BigDecimal("0.00"));
        rate.setTypeName("Negative ID Rate");
        mrtMatchingRates.add(rate);

        Integer[] expectedTypeIds = { -1 };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should handle negative rate type ID", 1, results.size());
        assertEquals("Rate identifier should be negative", "-1", results.get(0).getRateIdentifier());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_DuplicateRateTypeIds() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");
        rateTypeIds.add("50010"); // Duplicate
        rateTypeIds.add("50020");

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate1 = new InterestRate();
        rate1.setId(1);
        rate1.setIntRate(new BigDecimal("4.25"));
        rate1.setTypeName("Rate 1");
        
        InterestRate rate2 = new InterestRate();
        rate2.setId(2);
        rate2.setIntRate(new BigDecimal("3.75"));
        rate2.setTypeName("Rate 2");
        
        mrtMatchingRates.add(rate1);
        mrtMatchingRates.add(rate2);

        Integer[] expectedTypeIds = { 50010, 50010, 50020 }; // Includes duplicate
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should return rates as returned by service", 2, results.size());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }
}.setTypeName("Farm Ownership Loan Rate");
        
        mrtMatchingRates.add(rate1);
        mrtMatchingRates.add(rate2);

        Integer[] expectedTypeIds = { 50010, 50020 };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should return two interest rates", 2, results.size());
        
        InterestRateRef result1 = results.get(0);
        assertEquals("First rate should match", new BigDecimal("4.25"), result1.getInterestRate());
        assertEquals("First rate identifier should match", "1", result1.getRateIdentifier());
        assertEquals("First type name should match", "Farm Operating Loan Rate", result1.getTypeName());
        
        InterestRateRef result2 = results.get(1);
        assertEquals("Second rate should match", new BigDecimal("3.75"), result2.getInterestRate());
        assertEquals("Second rate identifier should match", "2", result2.getRateIdentifier());
        assertEquals("Second type name should match", "Farm Ownership Loan Rate", result2.getTypeName());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_EmptyRateTypeIdsList() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> emptyRateTypeIds = new ArrayList<String>();

        Integer[] expectedTypeIds = new Integer[0];
        List<InterestRate> emptyMrtRates = new ArrayList<InterestRate>();
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(emptyMrtRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(emptyRateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertTrue("Results should be empty for empty input", results.isEmpty());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_NullRateTypeIdsList() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> nullRateTypeIds = null;

        // Act & Assert - Should handle null gracefully
        try {
            List<InterestRateRef> results = service.getInterestRates(nullRateTypeIds, effectiveDate, createAgencyToken());
            fail("Should throw exception for null rate type IDs");
        } catch (Exception e) {
            // Expected - the service should handle this appropriately
            assertTrue("Should be NullPointerException or ServiceBrokerException", 
                      e instanceof NullPointerException || e instanceof ServiceBrokerException);
        }
    }

    @Test(expected = ServiceBrokerException.class)
    public void testGetInterestRate_ServiceException() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenThrow(new RuntimeException("MRT Service unavailable"));

        // Act - should throw ServiceBrokerException
        service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());
    }

    @Test(expected = ServiceBrokerException.class)
    public void testGetInterestRates_ServiceException() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");

        Integer[] expectedTypeIds = { 50010 };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenThrow(new RuntimeException("MRT Service unavailable"));

        // Act - should throw ServiceBrokerException
        service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());
    }

    @Test
    public void testGetInterestRate_ZeroRate() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate zeroRate = new InterestRate();
        zeroRate.setId(1);
        zeroRate.setIntRate(BigDecimal.ZERO);
        zeroRate.setTypeName("Zero Interest Rate");
        mrtMatchingRates.add(zeroRate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null", result);
        assertNotNull("Interest rate should not be null", result.getInterestRate());
        assertEquals("Interest rate should be zero", BigDecimal.ZERO, result.getInterestRate());
        assertEquals("Rate identifier should match", "1", result.getRateIdentifier());
        assertEquals("Type name should match", "Zero Interest Rate", result.getTypeName());
    }

    @Test
    public void testGetInterestRate_HighPrecisionRate() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate precisionRate = new InterestRate();
        precisionRate.setId(1);
        precisionRate.setIntRate(new BigDecimal("4.123456789"));
        precisionRate.setTypeName("High Precision Rate");
        mrtMatchingRates.add(precisionRate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null", result);
        assertNotNull("Interest rate should not be null", result.getInterestRate());
        assertEquals("Interest rate should preserve precision", new BigDecimal("4.123456789"), result.getInterestRate());
    }

    @Test
    public void testGetInterestRates_SingleRateInList() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate = new InterestRate();
        rate.setId(1);
        rate.setIntRate(new BigDecimal("3.50"));
        rate.setTypeName("Single Rate");
        mrtMatchingRates.add(rate);

        Integer[] expectedTypeIds = { 50010 };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should return one interest rate", 1, results.size());
        
        InterestRateRef result = results.get(0);
        assertEquals("Rate should match", new BigDecimal("3.50"), result.getInterestRate());
        assertEquals("Rate identifier should match", "1", result.getRateIdentifier());
        assertEquals("Type name should match", "Single Rate", result.getTypeName());
    }

    @Test
    public void testGetInterestRates_NullEffectiveDate() throws Exception {
        // Arrange
        Date nullEffectiveDate = null;
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");

        Integer[] expectedTypeIds = { 50010 };
        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, nullEffectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, nullEffectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertTrue("Results should be empty when no rates returned", results.isEmpty());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, nullEffectiveDate);
    }

    @Test
    public void testGetInterestRate_InvalidRateTypeId() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String invalidRateTypeId = "INVALID";

        // Act & Assert
        try {
            service.getInterestRate(invalidRateTypeId, effectiveDate, createAgencyToken());
            fail("Should throw exception for invalid rate type ID");
        } catch (Exception e) {
            // Expected - either NumberFormatException wrapped in ServiceBrokerException or direct NumberFormatException
            assertTrue("Should handle invalid rate type ID", 
                      e instanceof ServiceBrokerException || e instanceof NumberFormatException);
        }
    }

    @Test
    public void testGetInterestRates_MixedValidInvalidRateTypeIds() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> mixedRateTypeIds = new ArrayList<String>();
        mixedRateTypeIds.add("50010"); // Valid
        mixedRateTypeIds.add("INVALID"); // Invalid

        // Act & Assert
        try {
            service.getInterestRates(mixedRateTypeIds, effectiveDate, createAgencyToken());
            fail("Should throw exception for invalid rate type ID in list");
        } catch (Exception e) {
            // Expected - either NumberFormatException wrapped in ServiceBrokerException or direct NumberFormatException
            assertTrue("Should handle invalid rate type ID in list", 
                      e instanceof ServiceBrokerException || e instanceof NumberFormatException);
        }
    }

    @Test
    public void testGetInterestRate_FutureEffectiveDate() throws Exception {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1); // Future date
        Date futureEffectiveDate = cal.getTime();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate futureRate = new InterestRate();
        futureRate.setId(1);
        futureRate.setIntRate(new BigDecimal("5.00"));
        futureRate.setTypeName("Future Rate");
        mrtMatchingRates.add(futureRate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, futureEffectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, futureEffectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null for future date", result);
        assertEquals("Future rate should match", new BigDecimal("5.00"), result.getInterestRate());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, futureEffectiveDate);
    }

    @Test
    public void testGetInterestRate_PastEffectiveDate() throws Exception {
        // Arrange
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1); // Past date
        Date pastEffectiveDate = cal.getTime();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate pastRate = new InterestRate();
        pastRate.setId(1);
        pastRate.setIntRate(new BigDecimal("2.50"));
        pastRate.setTypeName("Historical Rate");
        mrtMatchingRates.add(pastRate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, pastEffectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, pastEffectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null for past date", result);
        assertEquals("Past rate should match", new BigDecimal("2.50"), result.getInterestRate());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, pastEffectiveDate);
    }

    @Test
    public void testGetInterestRates_LargeNumberOfRates() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        List<Integer> expectedTypeIdsList = new ArrayList<Integer>();

        // Create 10 different rate types
        for (int i = 1; i <= 10; i++) {
            String rateTypeId = "5001" + i;
            rateTypeIds.add(rateTypeId);
            expectedTypeIdsList.add(Integer.valueOf(rateTypeId));
            
            InterestRate rate = new InterestRate();
            rate.setId(i);
            rate.setIntRate(new BigDecimal(String.valueOf(i + 2.0))); // 3.0, 4.0, 5.0, etc.
            rate.setTypeName("Rate Type " + i);
            mrtMatchingRates.add(rate);
        }

        Integer[] expectedTypeIds = expectedTypeIdsList.toArray(new Integer[0]);
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should return all 10 interest rates", 10, results.size());
        
        for (int i = 0; i < results.size(); i++) {
            InterestRateRef result = results.get(i);
            assertNotNull("Each result should not be null", result);
            assertNotNull("Each interest rate should not be null", result.getInterestRate());
            assertEquals("Rate identifier should match index", String.valueOf(i + 1), result.getRateIdentifier());
            assertEquals("Type name should match", "Rate Type " + (i + 1), result.getTypeName());
        }
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRate_NullTypeName() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate = new InterestRate();
        rate.setId(1);
        rate.setIntRate(new BigDecimal("4.25"));
        rate.setTypeName(null); // Null type name
        mrtMatchingRates.add(rate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Interest rate should match", new BigDecimal("4.25"), result.getInterestRate());
        assertEquals("Rate identifier should match", "1", result.getRateIdentifier());
        assertNull("Type name should be null", result.getTypeName());
    }

    @Test
    public void testGetInterestRate_NullAgencyToken() throws Exception {
        // Arrange
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        InterestRate rate = new InterestRate();
        rate.setId(1);
        rate.setIntRate(new BigDecimal("4.25"));
        rate.setTypeName("Test Rate");
        mrtMatchingRates.add(rate);

        Integer[] expectedTypeIds = { Integer.valueOf(interestRateTypeId) };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        InterestRateRef result = service.getInterestRate(interestRateTypeId, effectiveDate, null);

        // Assert
        assertNotNull("Result should not be null even with null token", result);
        assertEquals("Interest rate should match", new BigDecimal("4.25"), result.getInterestRate());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRates_PartialResults() throws Exception {
        // Arrange - Request 3 rates but only 2 are returned
        Date effectiveDate = new Date();
        List<String> rateTypeIds = new ArrayList<String>();
        rateTypeIds.add("50010");
        rateTypeIds.add("50020");
        rateTypeIds.add("50030");

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        // Only return 2 rates instead of 3
        InterestRate rate1 = new InterestRate();
        rate1.setId(1);
        rate1.setIntRate(new BigDecimal("4.25"));
        rate1.setTypeName("Rate 1");
        
        InterestRate rate2 = new InterestRate();
        rate2.setId(2);
        rate2.setIntRate(new BigDecimal("3.75"));
        rate2.setTypeName("Rate 2");
        
        mrtMatchingRates.add(rate1);
        mrtMatchingRates.add(rate2);

        Integer[] expectedTypeIds = { 50010, 50020, 50030 };
        when(mockMrtProxyBusinessService.retrieveInterestRates(expectedTypeIds, effectiveDate))
            .thenReturn(mrtMatchingRates);

        // Act
        List<InterestRateRef> results = service.getInterestRates(rateTypeIds, effectiveDate, createAgencyToken());

        // Assert
        assertNotNull("Results should not be null", results);
        assertEquals("Should return only available rates", 2, results.size());
        
        verify(mockMrtProxyBusinessService, times(1)).retrieveInterestRates(expectedTypeIds, effectiveDate);
    }

    @Test
    public void testGetInterestRate_ReturnsFirstRateWhenMultiple() throws Exception {
        // Arrange - Service returns multiple rates but getInterestRate should return first one
        Date effectiveDate = new Date();
        String interestRateTypeId = "50010";

        List<InterestRate> mrtMatchingRates = new ArrayList<InterestRate>();
        
        InterestRate rate1 = new InterestRate();
        rate1.setId(1);
        rate1.setIntRate(new BigDecimal("4.25"));
        rate1.setTypeName("First Rate");
        
        InterestRate rate2 = new InterestRate();
        rate2.setId(2);
        rate2.setIntRate(new BigDecimal("3.75"));
        rate2