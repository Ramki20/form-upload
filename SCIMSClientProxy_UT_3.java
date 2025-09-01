package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.scims.base.JNDISpringMockBase;
import gov.usda.fsa.fcao.flp.flpids.util.JNDIMockBase;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomer;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerAddress;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerIdHistory;
import gov.usda.fsa.parmo.scims.businessobject.InactiveCustomerType;
import gov.usda.fsa.parmo.scims.businessobject.TaxIdentificationType;
import gov.usda.fsa.parmo.scims.businessobject.CustomerType;
import gov.usda.fsa.parmo.scims.businessobject.BusinessTypeCode;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions;
import gov.usda.fsa.parmo.scims.servicewrapper.SCIMSService;
import gov.usda.fsa.parmo.scims.servicewrapper.reply.SCIMSServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class SCIMSClientProxy_UT extends JNDISpringMockBase {

    @Mock
    private SCIMSService mockScimsService;
    
    @Mock
    private SCIMSServiceResult mockServiceResult;
    
    @InjectMocks
    private SCIMSClientProxy scimsClientProxy;
    
    private AgencyToken testAgencyToken;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        // Create test agency token
        testAgencyToken = createTestAgencyToken();
        
        // Override the getSCIMSService method behavior through mocking
        scimsClientProxy = new SCIMSClientProxy() {
            @Override
            protected SCIMSService getSCIMSService(AgencyToken baseAgencyToken) {
                return mockScimsService;
            }
        };
    }
    
    private AgencyToken createTestAgencyToken() {
        AgencyToken token = new AgencyToken();
        token.setApplicationIdentifier("SCIMS_TEST");
        token.setUserIdentifier("Scims Shared jar Test");
        token.setRequestHost("localhost");
        token.setProcessingNode("**n/a**");
        token.setReadOnly(Boolean.TRUE);
        return token;
    }

    @Test
    public void testJNDISettings() throws Exception {
        Context context = new InitialContext();

        String value = getJNDIStringValue(context, JNDIMockBase.SERVICE_CONFIG_JNDI_NAMESPACE_ROOT_ENV_ENTRY_KEY);
        assertEquals(JNDIMockBase.SERVICE_CONFIG_JNDI_NAMESPACE_ROOT_ENV_ENTRY_VALUE, value);
        
        value = getJNDIStringValue(context, JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_KEY);
        assertEquals(JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_VALUE, value);
    }

    @Test
    public void testGetCustomerByCustomerIds() throws Exception {
        // Arrange
        Integer customerId = 1432871;
        String expectedTaxId = "400352979";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(expectedTaxId, customerList.get(0).getTaxID());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());
    }
    
    @Test
    public void testGetCustomerByCustomerIdsLite() throws Exception {
        // Arrange
        Integer customerId = 1432871;
        String expectedTaxId = "400352979";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(expectedTaxId, customerList.get(0).getTaxID());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());
    }
    
    @Test
    public void testGetCustomerByCustomerIdsLiteMergedList() throws Exception {
        // Arrange
        Integer customerId = 2579305;
        
        List<CoreCustomer> mockCustomers = createMockCustomerListWithMergedHistory(customerId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertTrue(!customerList.get(0).getMergedCustomerIdHistorySet().isEmpty());
        assertEquals(2, customerList.get(0).getMergedCustomerIdHistorySet().size());
    }
    
    @Test
    public void testGetCustomerByCustomerIdsLiteMergedListEmpty() throws Exception {
        // Arrange
        Integer customerId = 1432871;
        String expectedTaxId = "400352979";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertTrue(customerList.get(0).getMergedCustomerIdHistorySet().isEmpty());
    }
    
    @Test
    public void testCurrentAddress() throws Exception {
        // Arrange
        Integer customerId = 5144273;
        String expectedTaxId = "400880298";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(expectedTaxId, customerList.get(0).getTaxID());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());
    }
    
    @Test
    public void testCurrentAddressAndMailingAdress() throws Exception {
        // Arrange
        Integer customerId = 5144273;
        String expectedTaxId = "400880298";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(expectedTaxId, customerList.get(0).getTaxID());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());
    }
    
    @Test
    public void testTaxId() throws Exception {
        // Arrange
        String taxId = "400352979";
        Integer customerId = 1432871;
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, taxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), anyList()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByTaxIds(taxId);
        
        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(taxId, customerList.get(0).getTaxID());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
        assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());
    }

    private List<CoreCustomer> createMockCustomerList(Integer customerId, String taxId) {
        List<CoreCustomer> mockCustomers = new ArrayList<CoreCustomer>();
        CoreCustomer mockCustomer = new CoreCustomer();
        mockCustomer.setCoreCustomerId(BigInteger.valueOf(customerId));
        mockCustomer.setTaxIdentification(taxId);
        mockCustomer.setAddressList(createMockAddressList());
        mockCustomers.add(mockCustomer);
        return mockCustomers;
    }
    
    private List<CoreCustomer> createMockCustomerListWithMergedHistory(Integer customerId) {
        List<CoreCustomer> mockCustomers = new ArrayList<CoreCustomer>();
        CoreCustomer mockCustomer = new CoreCustomer();
        mockCustomer.setCoreCustomerId(BigInteger.valueOf(customerId));
        Set<CoreCustomerIdHistory> historySet = new HashSet<CoreCustomerIdHistory>();
        historySet.add(new CoreCustomerIdHistory());
        historySet.add(new CoreCustomerIdHistory());
        mockCustomer.setMergedCustomerIdHistorySet(historySet);
        mockCustomers.add(mockCustomer);
        return mockCustomers;
    }
    
    private Set<CoreCustomerAddress> createMockAddressList() {
        Set<CoreCustomerAddress> addressList = new HashSet<CoreCustomerAddress>();
        CoreCustomerAddress address = new CoreCustomerAddress();
        address.setCurrentAddress('Y');
        address.setMailingAddress('Y');
        address.setShippingAddress('N');
        address.setStreetAddress('N');
        addressList.add(address);
        return addressList;
    }

    private List<ScimsCustomerBO> getCustomerByCustomerIds(Integer customerId) throws Exception {
        List<Integer> customerIds = new ArrayList<Integer>();
        customerIds.add(customerId);

        List<ScimsCustomerBO> customerList = scimsClientProxy.getCustomerByCustomerIds(testAgencyToken, customerIds);
        return customerList;
    }

    private List<ScimsCustomerBO> getCustomerByCustomerIdsLite(Integer customerId) throws Exception {
        List<Integer> customerIds = new ArrayList<Integer>();
        customerIds.add(customerId);
        
        RetrieveScimsCustomersByCoreCustomerIdBC bc = new RetrieveScimsCustomersByCoreCustomerIdBC(testAgencyToken, customerIds, false);
        bc.setMergedIDList(true);

        List<ScimsCustomerBO> customerList = scimsClientProxy.getCustomerByCustomerIdsLite(bc);
        return customerList;
    }

    private List<ScimsCustomerBO> getCustomerByTaxIds(String taxId) throws Exception {
        List<String> taxIds = new ArrayList<String>();
        taxIds.add(taxId);

        List<ScimsCustomerBO> customerList = scimsClientProxy.getCustomerByTaxIds(testAgencyToken, taxIds);
        return customerList;
    }
}