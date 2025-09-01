package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
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
import gov.usda.fsa.parmo.scims.businessobject.BusinessType;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomer;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerAddress;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerIdHistory;
import gov.usda.fsa.parmo.scims.businessobject.CustomerType;
import gov.usda.fsa.parmo.scims.businessobject.TaxIdentificationType;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions;
import gov.usda.fsa.parmo.scims.servicewrapper.SCIMSService;
import gov.usda.fsa.parmo.scims.servicewrapper.reply.SCIMSServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class SCIMSClientProxy_UT  {

    @Mock
    private SCIMSService mockScimsService;
    
    @Mock
    private SCIMSServiceResult mockServiceResult;
    
    @InjectMocks
    private SCIMSClientProxy scimsClientProxy;
    
    private AgencyToken testAgencyToken;

    @Before
    public void setUp() throws Exception {
        
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
    public void testGetCustomerByCustomerIds() throws Exception {
        // Arrange
        Integer customerId = 1432871;
        String expectedTaxId = "400352979";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
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
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
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
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
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
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
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
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
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
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);

        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
    }

    @Test
    public void testCurrentAddressAndNOMailingAdress() throws Exception {
        // Arrange
        Integer customerId = 10649299;
        String expectedTaxId = "123456789";
        
        List<CoreCustomer> mockCustomers = createMockCustomerList(customerId, expectedTaxId);
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);

        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
        assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
    }

    @Test(expected = SCIMSDataNotFoundException.class)
    public void testBadCustomerByCustomerIds() throws Exception {
        // Arrange
        Integer customerId = 0;
        
        when(mockServiceResult.getCustomerList()).thenReturn(new ArrayList<>());
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        getCustomerByCustomerIds(customerId);
    }

    @Test(expected = SCIMSDataNotFoundException.class)
    public void testBadTaxIds() throws Exception {
        // Arrange
        String taxIandType = "400352979M";
        
        when(mockServiceResult.getCustomerList()).thenReturn(new ArrayList<>());
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        getCustomerByTaxIds(taxIandType);
    }

    @Test
    public void testGoodTaxIdAndTypes() throws Exception {
        // Arrange
        String taxIandType = "400352979S";
        String expectedTaxId = "400352979";
        
        List<CoreCustomer> mockCustomers = createMockCustomerListForTaxId(expectedTaxId, "S");
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByTaxIds(taxIandType);

        // Assert
        assertNotNull(customerList);
        assertEquals(expectedTaxId, customerList.get(0).getTaxID());
        assertEquals("S", customerList.get(0).getTaxIDType().getCode());
    }

    @Test
    public void testGetCustomerByTaxIds() throws Exception {
        // Arrange
        String taxId = "400352979";
        Integer expectedCustomerId = 1432871;
        
        List<CoreCustomer> mockCustomers = createMockCustomerListForTaxId(taxId, "S");
        mockCustomers.get(0).setCoreCustomerId(Integer.valueOf(expectedCustomerId));
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerList = getCustomerByTaxIds(taxId);

        // Assert
        assertNotNull(customerList);
        assertEquals(1, customerList.size());
        assertEquals(expectedCustomerId.toString(), customerList.get(0).getCustomerID());
    }

    @Test
    public void testGetCustomerCompareResults() throws Exception {
        // Arrange
        String taxId = "400352979";
        Integer customerId = 1432871;
        
        List<CoreCustomer> mockCustomers = createMockCustomerListForTaxId(taxId, "S");
        mockCustomers.get(0).setCoreCustomerId(Integer.valueOf(customerId));
        
        when(mockServiceResult.getCustomerList()).thenReturn(mockCustomers);
        when(mockScimsService.getCustomers(eq(testAgencyToken), any(CustomerSearchOptions.class), any()))
            .thenReturn(mockServiceResult);
        
        // Act
        List<ScimsCustomerBO> customerListByTaxId = getCustomerByTaxIds(taxId);
        List<ScimsCustomerBO> customerListByCustomerId = getCustomerByCustomerIds(customerId);
        
        // Assert
        ScimsCustomerBO customerSource = null;
        ScimsCustomerBO customerToCompare = null;
        if (customerListByTaxId != null && !customerListByTaxId.isEmpty()) {
            customerSource = customerListByTaxId.get(0);
        }
        if (customerListByCustomerId != null && !customerListByCustomerId.isEmpty()) {
            customerToCompare = customerListByCustomerId.get(0);
        }
        
        if (customerSource != null && customerToCompare != null) {
            verifyResults(customerSource, customerToCompare);
        } else {
            Assert.assertNotNull("Error: getCustomerByTaxIds(" + taxId + ")  returned an empty List", customerSource);
            Assert.assertNotNull("Error: getCustomerByCustomerIds(" + customerId + ")  returned an empty List", customerToCompare);
        }
    }

    // Helper methods to create mock data
    private List<CoreCustomer> createMockCustomerList(Integer customerId, String taxId) {
        List<CoreCustomer> customers = new ArrayList<>();
        CoreCustomer customer = createBasicMockCustomer(customerId, taxId, "S");
        customers.add(customer);
        return customers;
    }
    
    private List<CoreCustomer> createMockCustomerListWithMergedHistory(Integer customerId) {
        List<CoreCustomer> customers = new ArrayList<>();
        CoreCustomer customer = createBasicMockCustomer(customerId, "123456789", "S");
        
        // Add merged ID history
        Set<CoreCustomerIdHistory> mergedHistory = new HashSet<>();
        mergedHistory.add(createMockIdHistory(Integer.valueOf(2579305), Integer.valueOf(2579306)));
        mergedHistory.add(createMockIdHistory(Integer.valueOf(2579307), Integer.valueOf(2579308)));
        customer.setMergedIdList(mergedHistory);
        
        customers.add(customer);
        return customers;
    }
    
    private List<CoreCustomer> createMockCustomerListForTaxId(String taxId, String taxType) {
        List<CoreCustomer> customers = new ArrayList<>();
        CoreCustomer customer = createBasicMockCustomer(1432871, taxId, taxType);
        customers.add(customer);
        return customers;
    }
    
    private CoreCustomer createBasicMockCustomer(Integer customerId, String taxId, String taxType) {
        CoreCustomer customer = new CoreCustomer();
        customer.setCoreCustomerId(Integer.valueOf(customerId));
        customer.setTaxIdentification(taxId);
        
        // Mock tax identification type
        TaxIdentificationType taxIdType = new TaxIdentificationType();
        taxIdType.setTaxIdentificationTypeCode(taxType.charAt(0));
        taxIdType.setShortDescription("Social Security Number");
        customer.setTaxIdentificationType(taxIdType);
        
        // Mock customer type
        CustomerType customerType = new CustomerType();
        customerType.setCustomerTypeCode('I');
        customerType.setShortDescription("Individual");
        customer.setCustomerType(customerType);
        
        // Mock business type code
        BusinessType businessType = new BusinessType();
        businessType.setBusinessTypeCode("INDIVIDUAL");
        businessType.setShortDescription("Individual");
        customer.setBusinessTypeCode(businessType);
        
        // Set basic customer info
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setBusinessName("John Doe");
        customer.setCommonCustomerName("John Doe");
        customer.setMiddleName("");
        customer.setNamePrefix("");
        customer.setNameSuffix("");
        
        // Mock address
        Set<CoreCustomerAddress> addresses = new HashSet<>();
        CoreCustomerAddress address = new CoreCustomerAddress();
        address.setCurrentAddressIndicator('Y');
        address.setMailingAddressIndicator('Y');
        address.setShippingAddressIndicator('N');
        address.setStreetAddressIndicator('N');
        address.setCity("Test City");
        address.setStateAbbreviation("IA");
        address.setZipCode("50001");
        addresses.add(address);
        customer.setAddressList(addresses);
        
        // Initialize empty collections to avoid null pointer exceptions
        customer.setEmailList(new HashSet<>());
        customer.setPhoneList(new HashSet<>());
        customer.setLegacyLinkList(new HashSet<>());
        customer.setProgramParticipationList(new HashSet<>());
        customer.setRaceList(new HashSet<>());
        customer.setEthnicityList(new HashSet<>());
        customer.setCoreIdHistoryList(new ArrayList<>());
        customer.setMergedIdList(new HashSet<>());
        
        return customer;
    }
    
    private CoreCustomerIdHistory createMockIdHistory(Integer oldId, Integer newId) {
        CoreCustomerIdHistory history = new CoreCustomerIdHistory();
        history.setOldCustomerIdentifier(oldId);
        history.setNewCustomerIdentifier(newId);
        return history;
    }

    // Helper methods from original test
    private void verifyResults(ScimsCustomerBO customerSource, ScimsCustomerBO customerToCompare) {
        assertEquals(customerSource.getAddressSet().size(), customerToCompare.getAddressSet().size());
        assertEquals(customerSource.getPhoneSet().size(), customerToCompare.getPhoneSet().size());
        assertEquals(customerSource.getEmailSet().size(), customerToCompare.getEmailSet().size());
        assertEquals(customerSource.getBirthDate(), customerToCompare.getBirthDate());
        assertEquals(customerSource.getBusinessName(), customerToCompare.getBusinessName());
        assertEquals(customerSource.getBusinessType().getDescription(), customerToCompare.getBusinessType().getDescription());
        assertEquals(customerSource.getCommonName(), customerToCompare.getCommonName());
        assertEquals(customerSource.getCustomerName(), customerToCompare.getCustomerName());
        assertEquals(customerSource.getFirstName(), customerToCompare.getFirstName());
        assertEquals(customerSource.getLastName(), customerToCompare.getLastName());
        assertEquals(customerSource.getLegacyLinkSet().size(), customerToCompare.getLegacyLinkSet().size());
        assertEquals(customerSource.getCustomerIdHistorySet(), customerToCompare.getCustomerIdHistorySet());
        assertEquals(customerSource.getProgramParticipationList().size(), customerToCompare.getProgramParticipationList().size());
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

    public SCIMSClientProxy getScimsClientProxy() {
        return scimsClientProxy;
    }

    public void setScimsClientProxy(SCIMSClientProxy scimsClientProxy) {
        this.scimsClientProxy = scimsClientProxy;
    }
}