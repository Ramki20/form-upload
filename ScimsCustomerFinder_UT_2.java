package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.support;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import javax.naming.NamingException;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.CustomerInformationBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;

public class ScimsCustomerFinder_UT extends DLSExternalCommonTestMockBase {
    
    private CustomerInformationBC customer;

    @Before
    public void setUp() throws Exception {
        super.setUp(); // This calls test_jndiconfig() from parent class
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
        setupTestSpecificJndiMocks("Y");
        
        customer.setCoreCustomerId(1);
        customer.setTaxId("123456789");
        customer.setTaxIdTypeCode("S");
        
        ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, true);
        
        assertNotNull(scimsCustomer);
    }
    
    @Test
    public void test_getScimsCustomerByCCID_copyFromFLPCustomer() throws Exception {
        setupTestSpecificJndiMocks("Y");
        
        customer.setCoreCustomerId(1);
        customer.setTaxId("123456789");
        customer.setTaxIdTypeCode("S");
        customer.setLastName("LastName Suffix");
        
        ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
        
        assertNotNull(scimsCustomer);
    }
    
    @Test
    public void test_getScimsCustomerByCCID_notExit_copyFromFLPCustomer() throws Exception {
        setupTestSpecificJndiMocks("Y");
        
        customer.setCoreCustomerId(49472391);
        customer.setTaxId("123456789");
        customer.setTaxIdTypeCode("S");
        
        ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
        
        assertNotNull(scimsCustomer);
    }
    
    @Test
    public void test_getScimsCustomerByCCID_NotExist_NoCopy_expectCustomerId() throws Exception {
        setupTestSpecificJndiMocks("Y");
        
        customer.setCoreCustomerId(1432871);
        customer.setTaxId("123456789");
        customer.setTaxIdTypeCode("S");
        
        ScimsCustomerBO noDataFoundCopyingFromCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, true);
        
        assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCopyingFromCustomer.getCustomerID());
    }
    
    @Test
    public void test_getScimsCustomerByCCID_supportDisabled_expectCustomerId() throws Exception {
        // Set support.create.scims.customer to "N" to disable creation
        setupTestSpecificJndiMocks("N");
        
        customer.setCoreCustomerId(1432871);
        customer.setTaxId("123456789");
        customer.setTaxIdTypeCode("S");
        
        ScimsCustomerBO noDataFoundCopyingFromCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer, false);
        
        assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCopyingFromCustomer.getCustomerID());
    }
}