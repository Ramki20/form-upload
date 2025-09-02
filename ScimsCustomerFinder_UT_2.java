package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.CustomerInformationBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;

@ExtendWith(MockitoExtension.class)
public class ScimsCustomerFinder_UT extends DLSExternalCommonTestMockBase {
    
    private CustomerInformationBC customer;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp(); // This calls testJndiConfig() from parent class
        customer = new CustomerInformationBC();
        customer.setAgencyToken(this.createAgencyToken());
    }
    
    @Test
    public void testCustomer_taxDiff_noProfile_expectFalse() throws Exception {
        customer.setCoreCustomerId(12);    
        customer.setTaxId("123456789");
        customer.setOpenLoanIndicator(null);
        ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
        scimsCustomer.setTaxID("987654321");
                
        assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
                scimsCustomer));
    }
    
    @Test
    public void testCustomer_taxSame_noProfile_expectFalse() throws Exception {
        customer.setCoreCustomerId(12);    
        customer.setTaxId("123456789");
        ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
        scimsCustomer.setTaxID("123456789");
                
        assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
                scimsCustomer));
    }
    
    @Test
    public void testCustomer_taxDiff_withoutValidOpenLoan_expectFalse() throws Exception {
        customer.setCoreCustomerId(12);    
        customer.setTaxId("123456789");
        customer.setOpenLoanIndicator("A");
        ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
        scimsCustomer.setTaxID("987654321");
                
        assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
                scimsCustomer));
    }
    
    @Test
    public void testCustomer_taxDiff_withValidOpenLoan_expectTrue() throws Exception {
        customer.setCoreCustomerId(12);    
        customer.setTaxId("123456789");
        customer.setOpenLoanIndicator("Y");
        ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
        scimsCustomer.setTaxID("987654321");
                
        assertTrue(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
                scimsCustomer));
    }
    
    @Test
    public void test_getScimsCustomerByCCID_retrieve() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
            customer.setCoreCustomerId(1);
            customer.setTaxId("123456789");
            customer.setTaxIdTypeCode("S");
            
            ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, true);
            
            assertNotNull(scimsCustomer);
        }
    }
    
    @Test
    public void test_getScimsCustomerByCCID_copyFromFLPCustomer() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
            customer.setCoreCustomerId(1);
            customer.setTaxId("123456789");
            customer.setTaxIdTypeCode("S");
            customer.setLastName("LastName Suffix");
            
            ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
            
            assertNotNull(scimsCustomer);
        }
    }
    
    @Test
    public void test_getScimsCustomerByCCID_notExit_copyFromFLPCustomer() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
            customer.setCoreCustomerId(49472391);
            customer.setTaxId("123456789");
            customer.setTaxIdTypeCode("S");
            
            ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
            
            assertNotNull(scimsCustomer);
        }
    }
    
    @Test
    public void test_getScimsCustomerByCCID_NotExist_NoCopy_expectCustomerId() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            setupJndiMockForTest(mockedStatic, "Y");
            
            customer.setCoreCustomerId(1432871);
            customer.setTaxId("123456789");
            customer.setTaxIdTypeCode("S");
            
            ScimsCustomerBO noDataFoundCopyingFromCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, true);
            
            assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCopyingFromCustomer.getCustomerID());
        }
    }
    
    @Test
    public void test_getScimsCustomerByCCID_supportDisabled_expectCustomerId() throws Exception {
        try (MockedStatic<InitialContext> mockedStatic = mockStatic(InitialContext.class)) {
            // Set support.create.scims.customer to "N" to disable creation
            setupJndiMockForTest(mockedStatic, "N");
            
            customer.setCoreCustomerId(1432871);
            customer.setTaxId("123456789");
            customer.setTaxIdTypeCode("S");
            
            ScimsCustomerBO noDataFoundCopyingFromCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
            
            assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCopyingFromCustomer.getCustomerID());
        }
    }
    
    private void setupJndiMockForTest(MockedStatic<InitialContext> mockedStatic, String supportCreateValue) throws NamingException {
        InitialContext mockInitialContext = mock(InitialContext.class);
        mockedStatic.when(() -> new InitialContext()).thenReturn(mockInitialContext);
        mockedStatic.when(() -> new InitialContext(any(Hashtable.class))).thenReturn(mockInitialContext);
        
        // Configure all the JNDI lookups that might be needed
        when(mockInitialContext.lookup("java:comp/env/name_space_root")).thenReturn("cell/persistent");
        when(mockInitialContext.lookup("cell/persistent")).thenReturn(mockSubContext);
        when(mockInitialContext.lookup("java:comp/env/application_identifier")).thenReturn("cbs-client");
        
        // Configure the key JNDI lookup for SCIMS customer support
        when(mockInitialContext.lookup("gov/usda/fsa/fcao/flp/dls/support.create.scims.customer")).thenReturn(supportCreateValue);
        when(mockInitialContext.lookup("cell/persistent/gov/usda/fsa/fcao/flp/dls/support_create_scims_customer")).thenReturn(supportCreateValue);
        when(mockInitialContext.lookup("gov/usda/fsa/fcao/flp/dls/support_create_scims_customer")).thenReturn(supportCreateValue);
        
        // Configure FRS service lookups
        when(mockInitialContext.lookup("cell/persistent/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockInitialContext.lookup("java:comp/env/gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("gov/usda/fsa/common/frs_service_specifier")).thenReturn("WS");
        
        // Configure CBS service lookups
        when(mockSubContext.lookup("gov/usda/fsa/common/citso/cbs/sharedservice_specifier")).thenReturn("WS");
        when(mockSubContext.lookup("gov/usda/fsa/common/citso/cbs/web_service_endpoint_url"))
            .thenReturn("http://int1-internal-services.fsa.usda.gov/cbs-ejb/services/CommonBusinessDataServicePort?wsdl");
    }
}