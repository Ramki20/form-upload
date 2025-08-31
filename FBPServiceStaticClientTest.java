
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FBPServiceStaticClientTest {

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

    @Mock
    private JAXBElement<DALRData> mockDALRJaxbElement;

    @Mock
    private JAXBElement<DLMData> mockDLMJaxbElement;

    @Mock
    private JAXBElement<FLPRALoanServicingData> mockFLPRAJaxbElement;

    private FBPServiceStaticClient client;
    private final String TEST_ENDPOINT_URL = "https://wem.dev.sc.egov.usda.gov/gateway/fbpservice.asmx";
    private final String TEST_SITENAME = "test1.onlinequity.com";
    private final String TEST_USERNAME = "FBP_FLPIDS_SOAP";
    private final String TEST_PASSWORD_DIGEST = "a9rD7tE8vsKhk";
    private final Integer TEST_CORE_CUSTOMER_ID = 5470400;

    @BeforeEach
    void setUp() {
        client = new FBPServiceStaticClient();
    }

    @Test
    void testConstructor_InitializesJNDIValues() {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_SITENAME_JNDI))
                    .thenReturn(TEST_SITENAME);
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_USER_NAME_JNDI))
                    .thenReturn(TEST_USERNAME);
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI))
                    .thenReturn(TEST_PASSWORD_DIGEST);

            FBPServiceStaticClient newClient = new FBPServiceStaticClient();
            assertNotNull(newClient);
        }
    }

    @Test
    void testGetDALRData_Success() throws Exception {
        DALRData expectedData = createMockDALRData();
        List<Object> contentList = Arrays.asList(mockDALRJaxbElement);
        
        when(mockDALRJaxbElement.getValue()).thenReturn(expectedData);
        when(mockDALRDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDALRDataResult);
            
            DALRData result = spyClient.getDALRData(TEST_CORE_CUSTOMER_ID);
            
            assertNotNull(result);
            assertEquals(expectedData, result);
            verify(mockFBPServiceSoap).getDALRData(anyString(), anyString(), anyString(), anyString(), eq(TEST_CORE_CUSTOMER_ID));
        }
    }

    @Test
    void testGetDALRData_NullResult() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(null);
            
            DALRData result = spyClient.getDALRData(TEST_CORE_CUSTOMER_ID);
            
            assertNull(result);
        }
    }

    @Test
    void testGetDALRData_WebServiceException_RecordsNotFound() throws Exception {
        WebServiceException exception = new WebServiceException("Cannot find child element");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenThrow(exception);
            
            DALRData result = spyClient.getDALRData(TEST_CORE_CUSTOMER_ID);
            
            assertNull(result);
        }
    }

    @Test
    void testGetDALRData_SOAPFaultException_RecordsNotFound() throws Exception {
        SOAPFaultException exception = new SOAPFaultException(null) {
            @Override
            public String getMessage() {
                return "Response message did not contain proper response data";
            }
        };
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenThrow(exception);
            
            DALRData result = spyClient.getDALRData(TEST_CORE_CUSTOMER_ID);
            
            assertNull(result);
        }
    }

    @Test
    void testGetDALRData_UnexpectedException() throws Exception {
        RuntimeException exception = new RuntimeException("Unexpected error");
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenThrow(exception);
            
            assertThrows(FBPConnectorException.class, 
                    () -> spyClient.getDALRData(TEST_CORE_CUSTOMER_ID));
        }
    }

    @Test
    void testGetDLMData_Success() throws Exception {
        DLMData expectedData = createMockDLMData();
        List<Object> contentList = Arrays.asList(mockDLMJaxbElement);
        
        when(mockDLMJaxbElement.getValue()).thenReturn(expectedData);
        when(mockDLMDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDLMDataResult);
            
            DLMData result = spyClient.getDLMData(TEST_CORE_CUSTOMER_ID);
            
            assertNotNull(result);
            assertEquals(expectedData, result);
        }
    }

    @Test
    void testGetDLMYEAData_Success() throws Exception {
        DLMData expectedData = createMockDLMData();
        List<Object> contentList = Arrays.asList(mockDLMJaxbElement);
        
        when(mockDLMJaxbElement.getValue()).thenReturn(expectedData);
        when(mockDLMYEADataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMYEAData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDLMYEADataResult);
            
            DLMData result = spyClient.getDLMYEAData(TEST_CORE_CUSTOMER_ID);
            
            assertNotNull(result);
            assertEquals(expectedData, result);
        }
    }

    @Test
    void testGetLenderStaffData_Success() throws Exception {
        List<Integer> coreCustomerIds = Arrays.asList(5470400, 7495988);
        LenderStaffData expectedData = createMockLenderStaffData();
        
        ArrayOfVendorClient arrayOfVendorClient = createMockArrayOfVendorClient();
        when(mockLenderStaffDataResult.getVendorClientList()).thenReturn(arrayOfVendorClient);
        when(mockLenderStaffDataResult.getWarning()).thenReturn(null);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), anyString(), any(ArrayOfInt.class)))
                    .thenReturn(mockLenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(coreCustomerIds);
            
            assertNotNull(result);
            assertNull(result.getErrorMessage());
        }
    }

    @Test
    void testGetLenderStaffData_EmptyInputList() throws Exception {
        List<Integer> emptyList = new ArrayList<>();
        
        LenderStaffData result = client.getLenderStaffData(emptyList);
        
        assertNotNull(result);
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("inputCoreCustomerIDList is null or empty"));
    }

    @Test
    void testGetLenderStaffData_NullInputList() throws Exception {
        LenderStaffData result = client.getLenderStaffData(null);
        
        assertNotNull(result);
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("inputCoreCustomerIDList is null or empty"));
    }

    @Test
    void testGetFLPRALoanServicingData_Success() throws Exception {
        FLPRALoanServicingData expectedData = createMockFLPRAData();
        List<Object> contentList = Arrays.asList(mockFLPRAJaxbElement);
        
        when(mockFLPRAJaxbElement.getValue()).thenReturn(expectedData);
        when(mockFLPRALoanServicingDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getFLPRALoanServicingData(anyString(), anyString(), anyString()))
                    .thenReturn(mockFLPRALoanServicingDataResult);
            
            FLPRALoanServicingData result = spyClient.getFLPRALoanServicingData();
            
            assertNotNull(result);
            assertEquals(expectedData, result);
        }
    }

    @Test
    void testIsHealthy_True() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            boolean result = spyClient.isHealthy();
            
            assertTrue(result);
        }
    }

    @Test
    void testIsHealthy_False() throws Exception {
        FBPServiceStaticClient spyClient = spy(client);
        doThrow(new FBPConnectorException("Connection failed")).when(spyClient).createFBPServiceInstance();
        
        boolean result = spyClient.isHealthy();
        
        assertFalse(result);
    }

    @Test
    void testGetDALRData_WithAllParameters_Success() throws Exception {
        DALRData expectedData = createMockDALRData();
        List<Object> contentList = Arrays.asList(mockDALRJaxbElement);
        
        when(mockDALRJaxbElement.getValue()).thenReturn(expectedData);
        when(mockDALRDataResult.getContent()).thenReturn(contentList);
        
        FBPServiceStaticClient spyClient = spy(client);
        doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
        
        when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
        when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                .thenReturn(mockDALRDataResult);
        
        DALRData result = spyClient.getDALRData(TEST_ENDPOINT_URL, TEST_SITENAME, TEST_USERNAME, "", TEST_PASSWORD_DIGEST, TEST_CORE_CUSTOMER_ID);
        
        assertNotNull(result);
        assertEquals(expectedData, result);
    }

    @Test
    void testGetDALRData_JAXBException() throws Exception {
        List<Object> contentList = Arrays.asList("InvalidObject");
        
        when(mockDALRDataResult.getContent()).thenReturn(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(anyString(), anyString(), anyString(), anyString(), any(Integer.class)))
                    .thenReturn(mockDALRDataResult);
            
            assertThrows(FBPConnectorException.class, 
                    () -> spyClient.getDALRData(TEST_CORE_CUSTOMER_ID));
        }
    }

    // Helper methods for creating mock data
    private void setupJNDIMocks(MockedStatic<JNDIUtil> jndiUtilMock) {
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                .thenReturn(TEST_ENDPOINT_URL);
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_SITENAME_JNDI))
                .thenReturn(TEST_SITENAME);
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_USER_NAME_JNDI))
                .thenReturn(TEST_USERNAME);
        jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI))
                .thenReturn(TEST_PASSWORD_DIGEST);
    }

    private DALRData createMockDALRData() {
        DALRData data = new DALRData();
        // Set up mock data as needed
        return data;
    }

    private DLMData createMockDLMData() {
        DLMData data = new DLMData();
        // Set up mock data as needed
        return data;
    }

    private LenderStaffData createMockLenderStaffData() {
        LenderStaffData data = new LenderStaffData();
        // Set up mock data as needed
        return data;
    }

    private FLPRALoanServicingData createMockFLPRAData() {
        FLPRALoanServicingData data = new FLPRALoanServicingData();
        // Set up mock data as needed
        return data;
    }

    private ArrayOfVendorClient createMockArrayOfVendorClient() {
        ArrayOfVendorClient arrayOfVendorClient = new ArrayOfVendorClient();
        List<VendorClient> vendorClients = new ArrayList<>();
        
        VendorClient vendorClient1 = new VendorClient();
        vendorClient1.setCoreCustomerID(5470400);
        vendorClient1.setClientID(188723);
        
        ArrayOfLenderStaff arrayOfLenderStaff1 = new ArrayOfLenderStaff();
        List<LenderStaff> staffList1 = new ArrayList<>();
        
        LenderStaff staff1 = new LenderStaff();
        staff1.setStaffMember("Chantal Haun");
        staff1.setTitle("Farm Loan Manager");
        staff1.setEmail("chantal.haun@ca.usda.gov");
        staff1.setRole("Lender");
        staffList1.add(staff1);
        
        arrayOfLenderStaff1.setLenderStaff(staffList1);
        vendorClient1.setLenderStaffList(arrayOfLenderStaff1);
        vendorClients.add(vendorClient1);
        
        VendorClient vendorClient2 = new VendorClient();
        vendorClient2.setCoreCustomerID(7495988);
        vendorClient2.setClientID(301891);
        
        ArrayOfLenderStaff arrayOfLenderStaff2 = new ArrayOfLenderStaff();
        List<LenderStaff> staffList2 = new ArrayList<>();
        
        LenderStaff staff2 = new LenderStaff();
        staff2.setStaffMember("Tammy Phelps");
        staff2.setTitle("Program Specialist");
        staff2.setEmail("");
        staff2.setRole("Lender");
        staffList2.add(staff2);
        
        arrayOfLenderStaff2.setLenderStaff(staffList2);
        vendorClient2.setLenderStaffList(arrayOfLenderStaff2);
        vendorClients.add(vendorClient2);
        
        arrayOfVendorClient.setVendorClient(vendorClients);
        return arrayOfVendorClient;
    }
}