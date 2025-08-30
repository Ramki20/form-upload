package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FBPServiceTest {

    @Mock
    private FBPServiceSoap mockFBPServiceSoap;

    @Mock
    private FBPWsdlInfo mockFBPWsdlInfo;

    private FBPService fbpService;
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

            // Create a spy to partially mock the FBPService
            FBPService service = spy(new FBPService());
            
            // Mock the getPort method to return our mock
            when(service.getPort(any(QName.class), eq(FBPServiceSoap.class)))
                    .thenReturn(mockFBPServiceSoap);

            FBPServiceSoap result = service.getFBPServiceSoap();
            
            assertNotNull(result);
            assertEquals(mockFBPServiceSoap, result);
            verify(service, times(1)).getPort(any(QName.class), eq(FBPServiceSoap.class));
        }
    }

    @Test
    void testGetFBPServiceSoap_SecondCall_ReturnsCachedInstance() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = spy(new FBPService());
            
            when(service.getPort(any(QName.class), eq(FBPServiceSoap.class)))
                    .thenReturn(mockFBPServiceSoap);

            // First call
            FBPServiceSoap result1 = service.getFBPServiceSoap();
            // Second call
            FBPServiceSoap result2 = service.getFBPServiceSoap();
            
            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals(result1, result2);
            // Verify getPort is only called once (caching works)
            verify(service, times(1)).getPort(any(QName.class), eq(FBPServiceSoap.class));
        }
    }

    @Test
    void testGetFBPServiceSoapWithFeatures_FirstCall() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = spy(new FBPService());
            WebServiceFeature[] features = new WebServiceFeature[0];
            
            when(service.getPort(any(QName.class), eq(FBPServiceSoap.class), eq(features)))
                    .thenReturn(mockFBPServiceSoap);

            FBPServiceSoap result = service.getFBPServiceSoap(features);
            
            assertNotNull(result);
            assertEquals(mockFBPServiceSoap, result);
            verify(service, times(1)).getPort(any(QName.class), eq(FBPServiceSoap.class), eq(features));
        }
    }

    @Test
    void testGetFBPServiceSoapWithFeatures_SecondCall_ReturnsCachedInstance() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);

            FBPService service = spy(new FBPService());
            WebServiceFeature[] features = new WebServiceFeature[0];
            
            when(service.getPort(any(QName.class), eq(FBPServiceSoap.class), eq(features)))
                    .thenReturn(mockFBPServiceSoap);

            // First call
            FBPServiceSoap result1 = service.getFBPServiceSoap(features);
            // Second call
            FBPServiceSoap result2 = service.getFBPServiceSoap(features);
            
            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals(result1, result2);
            // Verify getPort is only called once (caching works)
            verify(service, times(1)).getPort(any(QName.class), eq(FBPServiceSoap.class), eq(features));
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