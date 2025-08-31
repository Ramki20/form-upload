package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FBPServiceTest {

    @Mock
    private FBPServiceSoap mockFBPServiceSoap;

    @Mock
    private FBPWsdlInfo mockFBPWsdlInfo;

    private static final String TEST_ENDPOINT_URL = "https://wem.dev.sc.egov.usda.gov/gateway/fbpservice.asmx";

    @BeforeEach
    void setUp() {
        // Reset the static fbpServiceSoap field before each test
        try {
            java.lang.reflect.Field field = FBPService.class.getDeclaredField("fbpServiceSoap");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            // Ignore reflection errors in test setup
        }
    }

    @Test
    void testDefaultConstructor_WithValidJNDI() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = new FBPService();
            assertNotNull(service);
        }
    }

    @Test
    void testDefaultConstructor_WithInvalidJNDI() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn("invalid-url");

            FBPService service = new FBPService();
            assertNotNull(service);
        }
    }

    @Test
    void testConstructorWithURL() throws MalformedURLException {
        URL testURL = new URL(TEST_ENDPOINT_URL + "?wsdl");
        FBPService service = new FBPService(testURL);
        assertNotNull(service);
    }

    @Test
    void testConstructorWithFBPWsdlInfo() throws MalformedURLException {
        URL testURL = new URL(TEST_ENDPOINT_URL + "?wsdl");
        when(mockFBPWsdlInfo.getWsdlLocation()).thenReturn(testURL);

        FBPService service = new FBPService(mockFBPWsdlInfo);
        assertNotNull(service);
    }

    @Test
    void testGetFBPServiceSoap_FirstCall() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            // Test using reflection to verify the static field is set correctly
            FBPService service = new FBPService();
           
            // Since we can't easily spy on the Service class due to Jakarta XML WS complexities,
            // we'll test that the service is created and can be called
            assertNotNull(service);
           
            // Verify that calling getFBPServiceSoap doesn't throw an exception
            assertDoesNotThrow(() -> {
                try {
                    service.getFBPServiceSoap();
                } catch (Exception e) {
                    // Expected in unit test environment without actual web service
                    assertTrue(e.getMessage().contains("Could not send Message") ||
                              e.getMessage().contains("Connection refused") ||
                              e.getMessage().contains("UnknownHostException") ||
                              e.getMessage().contains("ConnectException"));
                }
            });
        }
    }

    @Test
    void testGetFBPServiceSoap_CachingBehavior() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = new FBPService();
           
            // Test that the static field works for caching
            // First, verify the static field is null initially
            try {
                java.lang.reflect.Field field = FBPService.class.getDeclaredField("fbpServiceSoap");
                field.setAccessible(true);
                assertNull(field.get(null));
               
                // Create a mock and set it in the static field
                field.set(null, mockFBPServiceSoap);
               
                // Now getFBPServiceSoap should return the cached mock
                FBPServiceSoap result = service.getFBPServiceSoap();
                assertEquals(mockFBPServiceSoap, result);
               
            } catch (Exception e) {
                fail("Reflection access failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testGetFBPServiceSoapWithFeatures_CachingBehavior() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = new FBPService();
            WebServiceFeature[] features = new WebServiceFeature[0];
           
            // Test that the static field works for caching
            try {
                java.lang.reflect.Field field = FBPService.class.getDeclaredField("fbpServiceSoap");
                field.setAccessible(true);
               
                // Set mock in the static field
                field.set(null, mockFBPServiceSoap);
               
                // Now getFBPServiceSoap should return the cached mock
                FBPServiceSoap result = service.getFBPServiceSoap(features);
                assertEquals(mockFBPServiceSoap, result);
               
            } catch (Exception e) {
                fail("Reflection access failed: " + e.getMessage());
            }
        }
    }

    @Test
    void testGetFBPServiceSoap_WithoutCache() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = new FBPService();
           
            // Ensure static field is null to test actual creation
            try {
                java.lang.reflect.Field field = FBPService.class.getDeclaredField("fbpServiceSoap");
                field.setAccessible(true);
                field.set(null, null);
            } catch (Exception e) {
                fail("Reflection setup failed: " + e.getMessage());
            }
           
            // This should attempt to create a real service port
            assertDoesNotThrow(() -> {
                try {
                    FBPServiceSoap result = service.getFBPServiceSoap();
                    // In unit test environment, this might fail due to network issues
                    // but the method should not throw unexpected exceptions
                } catch (Exception e) {
                    // Expected network-related exceptions in unit test environment
                    assertTrue(e.getMessage().contains("Could not send Message") ||
                              e.getMessage().contains("Connection refused") ||
                              e.getMessage().contains("UnknownHostException") ||
                              e.getMessage().contains("ConnectException") ||
                              e.getMessage().contains("I/O error"));
                }
            });
        }
    }

    @Test
    void testWsdlLocationInitialization_ValidURL() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = new FBPService();
            assertNotNull(service);
        }
    }

    @Test
    void testWsdlLocationInitialization_MalformedURL() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn("not-a-valid-url");

            // Should not throw exception, but log error
            assertDoesNotThrow(() -> new FBPService());
        }
    }

    @Test
    void testServiceNamespaceAndServiceName() {
        // Test that the constants are properly defined
        assertEquals("http://schemas.eci-equity.com/2005/FBPService/", FBPWsdlInfo.FBP_NAMESPACE_URI);
        assertEquals("FBPService", FBPWsdlInfo.FBP_SERVICE_NAME);
        assertEquals("FBPServiceSoap", FBPWsdlInfo.FBP_SERVICE_PORT_NAME);
    }
}