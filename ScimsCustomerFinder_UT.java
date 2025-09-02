package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.support;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.CustomerInformationBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;

public class ScimsCustomerFinder_UT extends DLSExternalCommonTestMockBase{
	
	private CustomerInformationBC customer;
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
		customer = new CustomerInformationBC();
		customer.setAgencyToken(this.createAgencyToken());
	}
	
	@Test
	public void testCustomer_taxDiff_noProfile_expectFalse() throws Exception{
		customer.setCoreCustomerId(12);	
		customer.setTaxId("123456789");
		customer.setOpenLoanIndicator(null);
		ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
		scimsCustomer.setTaxID("987654321");
				
		Assert.assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
				scimsCustomer));
	}
	
	@Test
	public void testCustomer_taxSame_noProfile_expectFalse() throws Exception{
		customer.setCoreCustomerId(12);	
		customer.setTaxId("123456789");
		ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
		scimsCustomer.setTaxID("123456789");
				
		Assert.assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
				scimsCustomer));
	}
	
	
	@Test
	public void testCustomer_taxDiff_withoutValidOpenLoan_expectFalse() throws Exception{
		customer.setCoreCustomerId(12);	
		customer.setTaxId("123456789");
		customer.setOpenLoanIndicator("A");
		ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
		scimsCustomer.setTaxID("987654321");
				
		Assert.assertFalse(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
				scimsCustomer));
	}
	
	@Test
	public void testCustomer_taxDiff_withValidOpenLoan_expectFalse() throws Exception{
		customer.setCoreCustomerId(12);	
		customer.setTaxId("123456789");
		customer.setOpenLoanIndicator("Y");
		ScimsCustomerBO scimsCustomer = new ScimsCustomerBO(this.createAgencyToken());
		scimsCustomer.setTaxID("987654321");
				
		Assert.assertTrue(FLPCustomerSCIMSCustomerSynchService.isTaxInfoChangedForFLPCustomerWithOpenLoans(customer, 
				scimsCustomer));
	}
	@Test
	public void test_getScimsCustomerByCCID_retrieve() throws Exception{
		customer.setCoreCustomerId(1);
		customer.setTaxId("123456789");
		customer.setTaxIdTypeCode("S");
		
		
		ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer,true);
		
		Assert.assertNotNull(scimsCustomer);
	}
	@Test
	public void test_getScimsCustomerByCCID_copyFromFLPCustomer() throws Exception{
		customer.setCoreCustomerId(1);
		customer.setTaxId("123456789");
		customer.setTaxIdTypeCode("S");
		customer.setLastName("LastName Suffix");
		
		ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer,false);
		
		Assert.assertNotNull(scimsCustomer);
	}
	@Test
	public void test_getScimsCustomerByCCID_notExit_copyFromFLPCustomer() throws Exception{
		customer.setCoreCustomerId(49472391);
		customer.setTaxId("123456789");
		customer.setTaxIdTypeCode("S");
		
		ScimsCustomerBO scimsCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer,false);
		
		Assert.assertNotNull(scimsCustomer);
	}
	
	@Test //(expected=SCIMSDataNotFoundException.class)
	public void test_getScimsCustomerByCCID_NotExist_NoCopy_exception() throws Exception{
		customer.setCoreCustomerId(1432871);
		customer.setTaxId("123456789");
		customer.setTaxIdTypeCode("S");
		
		ScimsCustomerBO noDataFoundCoyingFromCustomer = ScimsCustomerFinder.getScimsCustomerByCCID(customer,true);
		
		Assert.assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCoyingFromCustomer.getCustomerID());
	}
	
	@Test //(expected=SCIMSDataNotFoundException.class)
	public void test_getScimsCustomerByCCID_exception() throws Exception{
		builder.bind("gov/usda/fsa/fcao/flp/dls/support.create.scims.customer","N");
		customer.setCoreCustomerId(1432871);
		customer.setTaxId("123456789");
		customer.setTaxIdTypeCode("S");
		
		ScimsCustomerBO noDataFoundCoyingFromCustomer =  ScimsCustomerFinder.getScimsCustomerByCCID(customer,false);
		
		Assert.assertEquals(customer.getCoreCustomerId().toString(), noDataFoundCoyingFromCustomer.getCustomerID());
	}
}
