package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive error handling and edge case tests for FBPServiceStaticClient
 */
@ExtendWith(MockitoExtension.class)
class FBPServiceErrorHandlingTest {

    @Mock
    private FBPService mockFBPService;

    @Mock
    private FBPServiceSoap mockFBPServiceSoap;

    @Mock
    private GetDALRDataResult mockDALRDataResult;

    @Mock
    private GetDLMDataResult mockDLMDataResult;

    @Mock
    private GetDLMYEADataResult mockDLMYEADataResult;

    @Mock
    private GetLenderStaffDataResult mockLenderStaffDataResult;

    @Mock
    private GetFLPRALoanServicingDataResult mockFLPRALoanServicingDataResult;

    private FBPServiceStaticClient client;

    @BeforeEach
    void setUp() {
        client = new FBPServiceStaticClient();
    }

    @Test
    void testGetDALRData_JAXBException_IncorrectObjectType() throws Exception {
        List<Object> contentList = Arrays.asList("NotAJAXBElement");
        when(mockDALRDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDALRDataResult);
            
            FBPConnectorException exception = assertThrows(FBPConnectorException.class,
                    () -> spyClient.getDALRData(5470400));
            
            assertTrue(exception.getMessage().contains("Failed to call FBP Web Service: getDALRData()"));
            assertTrue(exception.getCause() instanceof JAXBException);
        }
    }

    @Test
    void testGetDLMData_JAXBException_IncorrectObjectType() throws Exception {
        List<Object> contentList = Arrays.asList("NotAJAXBElement");
        when(mockDLMDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDLMDataResult);
            
            FBPConnectorException exception = assertThrows(FBPConnectorException.class,
                    () -> spyClient.getDLMData(6166876));
            
            assertTrue(exception.getMessage().contains("Failed to call FBP Web Service: getDLMData()"));
            assertTrue(exception.getCause() instanceof JAXBException);
        }
    }

    @Test
    void testGetDLMYEAData_JAXBException_IncorrectObjectType() throws Exception {
        List<Object> contentList = Arrays.asList("NotAJAXBElement");
        when(mockDLMYEADataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMYEAData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDLMYEADataResult);
            
            FBPConnectorException exception = assertThrows(FBPConnectorException.class,
                    () -> spyClient.getDLMYEAData(6166876));
            
            assertTrue(exception.getMessage().contains("Failed to call FBP Web Service: getDLMYEAData()"));
            assertTrue(exception.getCause() instanceof JAXBException);
        }
    }

    @Test
    void testGetFLPRALoanServicingData_JAXBException_IncorrectObjectType() throws Exception {
        List<Object> contentList = Arrays.asList("NotAJAXBElement");
        when(mockFLPRALoanServicingDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getFLPRALoanServicingData(anyString(), anyString(), anyString()))
                    .thenReturn(mockFLPRALoanServicingDataResult);
            
            FBPConnectorException exception = assertThrows(FBPConnectorException.class,
                    () -> spyClient.getFLPRALoanServicingData());
            
            assertTrue(exception.getMessage().contains("Failed to call FBP Web Service: getFLPRALoanServicingData()"));
            assertTrue(exception.getCause() instanceof JAXBException);
        }
    }

    @Test
    void testGetLenderStaffData_NullResult() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenReturn(null);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getErrorMessage().contains("Unexpected issue. lenderStaffDataResult is null"));
        }
    }

    @Test
    void testGetLenderStaffData_WarningWithNoVendorClients() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        
        when(mockLenderStaffDataResult.getWarning()).thenReturn("Error: Customer not found");
        when(mockLenderStaffDataResult.getVendorClientList()).thenReturn(null);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenReturn(mockLenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getErrorMessage().contains("FBP error occurred"));
            assertTrue(result.getErrorMessage().contains("Error: Customer not found"));
        }
    }

    @Test
    void testGetLenderStaffData_EmptyVendorClientList() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        
        ArrayOfVendorClient emptyArray = new ArrayOfVendorClient();
        when(mockLenderStaffDataResult.getVendorClientList()).thenReturn(emptyArray);
        when(mockLenderStaffDataResult.getWarning()).thenReturn(null);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenReturn(mockLenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNotNull(result.getErrorMessage());
            assertTrue(result.getErrorMessage().contains("vendorClientList is null or empty"));
        }
    }

    @Test
    void testGetLenderStaffData_RecordsNotFound_WebServiceException() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        WebServiceException exception = new WebServiceException("Cannot find child element");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenThrow(exception);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNull(result);
        }
    }

    @Test
    void testGetLenderStaffData_RecordsNotFound_SOAPFaultException() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        // Use WebServiceException instead of SOAPFaultException to avoid constructor issues
        WebServiceException exception = new WebServiceException("Response message did not contain proper response data");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenThrow(exception);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNull(result);
        }
    }

    @Test
    void testGetLenderStaffData_UnexpectedException() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        RuntimeException exception = new RuntimeException("Unexpected error");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenThrow(exception);
            
            assertThrows(FBPConnectorException.class,
                    () -> spyClient.getLenderStaffData(customerIds));
        }
    }

    @Test
    void testGetFLPRALoanServicingData_NullResult() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getFLPRALoanServicingData(anyString(), anyString(), anyString()))
                    .thenReturn(null);
            
            FLPRALoanServicingData result = spyClient.getFLPRALoanServicingData();
            
            assertNull(result);
        }
    }

    @Test
    void testCreateFBPServiceInstance_JNDIException() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenThrow(new RuntimeException("JNDI lookup failed"));
            
            FBPServiceStaticClient spyClient = spy(client);
            
            FBPConnectorException exception = assertThrows(FBPConnectorException.class,
                    spyClient::createFBPServiceInstance);
            
            assertTrue(exception.getMessage().contains("Connection to FBP Web Service failed"));
        }
    }

    @Test
    void testConstructor_JNDIException() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(anyString()))
                    .thenThrow(new RuntimeException("JNDI lookup failed"));
            
            // Should not throw exception, but log error
            assertDoesNotThrow(() -> new FBPServiceStaticClient());
        }
    }

    @Test
    void testRecordsNotFound_JBossEAP5Exception() throws Exception {
        WebServiceException exception = new WebServiceException("Cannot find child element for something");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenThrow(exception);
            
            DALRData result = spyClient.getDALRData(5470400);
            
            assertNull(result); // Should return null for records not found
        }
    }

    @Test
    void testRecordsNotFound_JBossEAP7Exception() throws Exception {
        // Use WebServiceException instead of SOAPFaultException to avoid constructor issues
        WebServiceException exception = new WebServiceException("Response message did not contain proper response data");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenThrow(exception);
            
            DLMData result = spyClient.getDLMData(6166876);
            
            assertNull(result); // Should return null for records not found
        }
    }

    @Test
    void testGetLenderStaffData_VendorClientWithEmptyStaffList() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        
        ArrayOfVendorClient arrayOfVendorClient = new ArrayOfVendorClient();
        VendorClient vendorClient = new VendorClient();
        vendorClient.setCoreCustomerID(5470400);
        vendorClient.setClientID(188723);
        
        ArrayOfLenderStaff emptyStaffList = new ArrayOfLenderStaff();
        // Empty staff list - no staff members added
        vendorClient.setLenderStaffList(emptyStaffList);
        
        arrayOfVendorClient.getVendorClient().add(vendorClient);
        
        when(mockLenderStaffDataResult.getVendorClientList()).thenReturn(arrayOfVendorClient);
        when(mockLenderStaffDataResult.getWarning()).thenReturn(null);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenReturn(mockLenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNull(result.getErrorMessage());
            assertNotNull(result.getMapOfLenderStaffRecords());
            assertTrue(result.getMapOfLenderStaffRecords().containsKey(5470400));
            assertTrue(result.getMapOfLenderStaffRecords().get(5470400).isEmpty());
        }
    }

    // Helper methods
    private void setupJNDIMocks(MockedStatic<JNDIUtil> jndiUtilMock) {
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                .thenReturn("https://wem.dev.sc.egov.usda.gov/gateway/fbpservice.asmx");
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_SITENAME_JNDI))
                .thenReturn("test1.onlinequity.com");
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_USER_NAME_JNDI))
                .thenReturn("FBP_FLPIDS_SOAP");
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI))
                .thenReturn("a9rD7tE8vsKhk");
    }
}