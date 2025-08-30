package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.JAXBElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for FBPServiceStaticClient that test more complex scenarios
 * and edge cases that might occur in real-world usage.
 */
@ExtendWith(MockitoExtension.class)
class FBPServiceIntegrationTest {

    @Mock
    private FBPService mockFBPService;

    @Mock
    private FBPServiceSoap mockFBPServiceSoap;

    private FBPServiceStaticClient client;
    private final String TEST_ENDPOINT_URL = "https://wem.dev.sc.egov.usda.gov/gateway/fbpservice.asmx";
    private final String TEST_SITENAME = "test1.onlinequity.com";
    private final String TEST_USERNAME = "FBP_FLPIDS_SOAP";
    private final String TEST_PASSWORD_DIGEST = "a9rD7tE8vsKhk";

    @BeforeEach
    void setUp() {
        client = new FBPServiceStaticClient();
    }

    @Test
    void testEndToEndDALRDataFlow() throws Exception {
        // Create realistic test data based on the mock data provided
        DALRData expectedData = createRealisticDALRData();
        GetDALRDataResult dalrDataResult = new GetDALRDataResult();
        
        JAXBElement<DALRData> jaxbElement = mock(JAXBElement.class);
        when(jaxbElement.getValue()).thenReturn(expectedData);
        
        List<Object> contentList = Arrays.asList(jaxbElement);
        dalrDataResult.getContent().addAll(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDALRData(eq(TEST_SITENAME), eq(TEST_USERNAME), eq(""), 
                    eq(TEST_PASSWORD_DIGEST), eq(5470400)))
                    .thenReturn(dalrDataResult);
            
            DALRData result = spyClient.getDALRData(5470400);
            
            assertNotNull(result);
            assertEquals(expectedData, result);
            
            // Verify the service was called with correct parameters
            verify(mockFBPServiceSoap).getDALRData(TEST_SITENAME, TEST_USERNAME, "", 
                    TEST_PASSWORD_DIGEST, 5470400);
        }
    }

    @Test
    void testEndToEndDLMDataFlow() throws Exception {
        DLMData expectedData = createRealisticDLMData();
        GetDLMDataResult dlmDataResult = new GetDLMDataResult();
        
        JAXBElement<DLMData> jaxbElement = mock(JAXBElement.class);
        when(jaxbElement.getValue()).thenReturn(expectedData);
        
        List<Object> contentList = Arrays.asList(jaxbElement);
        dlmDataResult.getContent().addAll(contentList);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getDLMData(anyString(), anyString(), anyString(), 
                    anyString(), any(Integer.class)))
                    .thenReturn(dlmDataResult);
            
            DLMData result = spyClient.getDLMData(6166876);
            
            assertNotNull(result);
            assertEquals(expectedData, result);
        }
    }

    @Test
    void testLenderStaffDataWithMultipleCustomers() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400, 7495988, 1234567);
        
        GetLenderStaffDataResult lenderStaffDataResult = new GetLenderStaffDataResult();
        ArrayOfVendorClient arrayOfVendorClient = createRealisticVendorClientArray();
        lenderStaffDataResult.setVendorClientList(arrayOfVendorClient);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), 
                    anyString(), any(ArrayOfInt.class)))
                    .thenReturn(lenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNull(result.getErrorMessage());
            assertNotNull(result.getMapOfLenderStaffRecords());
            
            // Verify that the ArrayOfInt contains unique values only
            verify(mockFBPServiceSoap).getLenderStaffData(eq(TEST_SITENAME), eq(TEST_USERNAME), 
                    eq(""), eq(TEST_PASSWORD_DIGEST), argThat(arrayOfInt -> 
                        arrayOfInt.getInt().size() == 3 && 
                        arrayOfInt.getInt().contains(5470400) &&
                        arrayOfInt.getInt().contains(7495988) &&
                        arrayOfInt.getInt().contains(1234567)
                    ));
        }
    }

    @Test
    void testLenderStaffDataWithDuplicateCustomerIds() throws Exception {
        List<Integer> customerIdsWithDuplicates = Arrays.asList(5470400, 7495988, 5470400, 7495988);
        
        GetLenderStaffDataResult lenderStaffDataResult = new GetLenderStaffDataResult();
        ArrayOfVendorClient arrayOfVendorClient = createRealisticVendorClientArray();
        lenderStaffDataResult.setVendorClientList(arrayOfVendorClient);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), 
                    anyString(), any(ArrayOfInt.class)))
                    .thenReturn(lenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIdsWithDuplicates);
            
            assertNotNull(result);
            
            // Verify that duplicates are filtered out
            verify(mockFBPServiceSoap).getLenderStaffData(eq(TEST_SITENAME), eq(TEST_USERNAME), 
                    eq(""), eq(TEST_PASSWORD_DIGEST), argThat(arrayOfInt -> 
                        arrayOfInt.getInt().size() == 2 && // Only 2 unique values
                        arrayOfInt.getInt().contains(5470400) &&
                        arrayOfInt.getInt().contains(7495988)
                    ));
        }
    }

    @Test
    void testLenderStaffDataWithWarning() throws Exception {
        List<Integer> customerIds = Arrays.asList(5470400);
        
        GetLenderStaffDataResult lenderStaffDataResult = new GetLenderStaffDataResult();
        lenderStaffDataResult.setWarning("Warning: Some data may be incomplete");
        
        ArrayOfVendorClient arrayOfVendorClient = createRealisticVendorClientArray();
        lenderStaffDataResult.setVendorClientList(arrayOfVendorClient);
        
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            setupJNDIMocks(jndiUtilMock);
            
            FBPServiceStaticClient spyClient = spy(client);
            doReturn(mockFBPService).when(spyClient).createFBPServiceInstance();
            
            when(mockFBPService.getFBPServiceSoap()).thenReturn(mockFBPServiceSoap);
            when(mockFBPServiceSoap.getLenderStaffData(anyString(), anyString(), anyString(), 
                    anyString(), any(ArrayOfInt.class)))
                    .thenReturn(lenderStaffDataResult);
            
            LenderStaffData result = spyClient.getLenderStaffData(customerIds);
            
            assertNotNull(result);
            assertNull(result.getErrorMessage()); // Should not have error message with valid vendor client list
        }
    }

    @Test
    void testCreateFBPServiceInstance_MalformedURL() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn("not-a-valid-url");
            
            FBPServiceStaticClient spyClient = spy(client);
            
            assertThrows(FBPConnectorException.class, spyClient::createFBPServiceInstance);
        }
    }

    @Test
    void testWsdlURLGeneration() throws Exception {
        try (MockedStatic<JNDIUtil> jndiUtilMock = mockStatic(JNDIUtil.class)) {
            // Test URL without ?wsdl
            jndiUtilMock.when(() -> JNDIUtil.lookUp(FBPWsdlInfo.FBP_INTERFACE_CONTEXT_JNDI))
                    .thenReturn(TEST_ENDPOINT_URL);
            
            FBPServiceStaticClient spyClient = spy(client);
            
            // Use reflection to access the private getWsdlURL method
            java.lang.reflect.Method getWsdlURLMethod = FBPServiceStaticClient.class
                    .getDeclaredMethod("getWsdlURL", String.class);
            getWsdlURLMethod.setAccessible(true);
            
            URL result = (URL) getWsdlURLMethod.invoke(spyClient, TEST_ENDPOINT_URL);
            
            assertEquals(TEST_ENDPOINT_URL + "?wsdl", result.toString());
        }
    }

    @Test
    void testWsdlURLGeneration_AlreadyHasWsdl() throws Exception {
        String urlWithWsdl = TEST_ENDPOINT_URL + "?wsdl";
        
        FBPServiceStaticClient spyClient = spy(client);
        
        // Use reflection to access the private getWsdlURL method
        java.lang.reflect.Method getWsdlURLMethod = FBPServiceStaticClient.class
                .getDeclaredMethod("getWsdlURL", String.class);
        getWsdlURLMethod.setAccessible(true);
        
        URL result = (URL) getWsdlURLMethod.invoke(spyClient, urlWithWsdl);
        
        assertEquals(urlWithWsdl, result.toString());
    }

    // Helper methods
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

    private DALRData createRealisticDALRData() {
        DALRData data = new DALRData();
        
        DALRRecord record1 = new DALRRecord();
        record1.setCoreCustomerID(5470400);
        record1.setCADescr("2011 Servicing");
        record1.setScenario("2011 Servicing - Typical");
        record1.setFarmOperExp(217821);
        record1.setFarmOperIntExp(12521);
        record1.setOwnrWdrw(71698);
        record1.setBalAvl(28296);
        record1.setNonAgcyDebtTax(0);
        
        DALRRecord record2 = new DALRRecord();
        record2.setCoreCustomerID(5470400);
        record2.setCADescr("2011 Servicing");
        record2.setScenario("2011 Servicing");
        record2.setFarmOperExp(223322);
        record2.setFarmOperIntExp(13354);
        record2.setOwnrWdrw(80627);
        record2.setBalAvl(111261);
        record2.setNonAgcyDebtTax(88813);
        
        data.getDALRRecord().add(record1);
        data.getDALRRecord().add(record2);
        
        return data;
    }

    private DLMData createRealisticDLMData() {
        DLMData data = new DLMData();
        
        DLMRecord record1 = new DLMRecord();
        record1.setCreditActionID(981081);
        record1.setCreditActionDescr("Loan Making shelly");
        record1.setLoanApprovalOfficial("EMP0007965 Integration");
        record1.setLoanApprovalTitle("Integration Test User");
        record1.setCommetsAndRequirements("Shelly test");
        
        DLMRecord record2 = new DLMRecord();
        record2.setCreditActionID(981082);
        record2.setCreditActionDescr("Shelly test");
        record2.setLoanApprovalOfficial("Tammy Phelps");
        record2.setLoanApprovalTitle("Program Specialist");
        record2.setCommetsAndRequirements("");
        
        data.getDLMRecord().add(record1);
        data.getDLMRecord().add(record2);
        
        return data;
    }

    private ArrayOfVendorClient createRealisticVendorClientArray() {
        ArrayOfVendorClient arrayOfVendorClient = new ArrayOfVendorClient();
        List<VendorClient> vendorClients = arrayOfVendorClient.getVendorClient();
        
        // First vendor client
        VendorClient vendorClient1 = new VendorClient();
        vendorClient1.setCoreCustomerID(5470400);
        vendorClient1.setClientID(188723);
        
        ArrayOfLenderStaff arrayOfLenderStaff1 = new ArrayOfLenderStaff();
        List<LenderStaff> staffList1 = arrayOfLenderStaff1.getLenderStaff();
        
        LenderStaff staff1 = new LenderStaff();
        staff1.setStaffMember("Chantal Haun");
        staff1.setTitle("Farm Loan Manager");
        staff1.setEmail("chantal.haun@ca.usda.gov");
        staff1.setRole("Lender");
        staffList1.add(staff1);
        
        vendorClient1.setLenderStaffList(arrayOfLenderStaff1);
        vendorClients.add(vendorClient1);
        
        // Second vendor client
        VendorClient vendorClient2 = new VendorClient();
        vendorClient2.setCoreCustomerID(7495988);
        vendorClient2.setClientID(301891);
        
        ArrayOfLenderStaff arrayOfLenderStaff2 = new ArrayOfLenderStaff();
        List<LenderStaff> staffList2 = arrayOfLenderStaff2.getLenderStaff();
        
        LenderStaff staff2 = new LenderStaff();
        staff2.setStaffMember("Tammy Phelps");
        staff2.setTitle("Program Specialist");
        staff2.setEmail("");
        staff2.setRole("Lender");
        staffList2.add(staff2);
        
        vendorClient2.setLenderStaffList(arrayOfLenderStaff2);
        vendorClients.add(vendorClient2);
        
        return arrayOfVendorClient;
    }
}