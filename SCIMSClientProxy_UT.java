package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.scims.base.ScimsTestCaseBase;
import gov.usda.fsa.fcao.flp.flpids.util.JNDIMockBase;

public class SCIMSClientProxy_UT extends ScimsTestCaseBase {

	@Autowired
	private SCIMSClientProxy scimsClientProxy;

	@Before
	public void setUp() throws Exception{
		super.setUp();

		if (scimsClientProxy == null) {

			scimsClientProxy = new SCIMSClientProxy();
		}
	}
	@Test
	public void testJNDISettings() throws Exception{
		Context context = new InitialContext();

		String value = getJNDIStringValue(context,JNDIMockBase.SERVICE_CONFIG_JNDI_NAMESPACE_ROOT_ENV_ENTRY_KEY);
		
		assertEquals(JNDIMockBase.SERVICE_CONFIG_JNDI_NAMESPACE_ROOT_ENV_ENTRY_VALUE, value);
		
		value = getJNDIStringValue(context,JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_KEY);
		assertEquals(JNDIMockBase.SERVICE_CONFIG_SPECIFIER_PATH_ENV_ENTRY_VALUE, value);
	}

	@Test
	public void testGetCustomerByCustomerIds() throws Exception {
		Integer customerId = 1432871;
		

		String taxId = "400352979";
		List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);
		
		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(taxId, customerList.get(0).getTaxID());
		assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());

	}
	
	@Test
	public void testGetCustomerByCustomerIdsLite() throws Exception {
		Integer customerId = 1432871;
		

		String taxId = "400352979";
		List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
		
		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(taxId, customerList.get(0).getTaxID());
		assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());

	}
	
	@Test
	public void testGetCustomerByCustomerIdsLiteMergedList() throws Exception {
		Integer customerId = 2579305;
		

		List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
		
		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(true, !customerList.get(0).getMergedCustomerIdHistorySet().isEmpty());
		assertEquals(2, customerList.get(0).getMergedCustomerIdHistorySet().size());

	}
	
	@Test
	public void testGetCustomerByCustomerIdsLiteMergedListEmpty() throws Exception {
		Integer customerId = 1432871;
		

		List<ScimsCustomerBO> customerList = getCustomerByCustomerIdsLite(customerId);
		
		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(true, customerList.get(0).getMergedCustomerIdHistorySet().isEmpty());

	}

	
	// Address set with ACTIVE current address indicator is ONLY be considered.
	// System will NOT add other address sets.
	@Test
	public void testCurrentAddress() throws Exception {
		Integer customerId = 5144273;

		String taxId = "400880298";
		List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);

		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(taxId, customerList.get(0).getTaxID());

		assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getShippingAddress());
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());
		assertEquals(false, customerList.get(0).getAddressSet().get(0).getStreetAddress());

	}

	@Test
	public void testCurrentAddressAndMailingAdress() throws Exception {
		Integer customerId = 5144273;

		List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);

		assertNotNull(customerList);
		assertEquals(1, customerList.size());

		assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());

	}

	@Test
	public void testCurrentAddressAndNOMailingAdress() throws Exception {
		Integer customerId = 10649299;

		List<ScimsCustomerBO> customerList = getCustomerByCustomerIds(customerId);

		assertNotNull(customerList);
		assertEquals(1, customerList.size());

		assertEquals(true, customerList.get(0).getAddressSet().size() == 1);
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getCurrentAddress());
		assertEquals(true, customerList.get(0).getAddressSet().get(0).getMailingAddress());

	}


	@Test(expected = SCIMSDataNotFoundException.class)
	public void testBadCustomerByCustomerIds() throws Exception {
		Integer customerId = 0;

		getCustomerByCustomerIds(customerId);


	}

	@Test(expected = SCIMSDataNotFoundException.class)
	public void testBadTaxIds() throws Exception
	{

		String taxIandType = "400352979M";

		getCustomerByTaxIds(taxIandType);

	}

	@Test
	public void testGoodTaxIdAndTypes() throws Exception {

		String taxIandType = "400352979S";

		List<ScimsCustomerBO> customerList = getCustomerByTaxIds(taxIandType);

		assertNotNull(customerList);
		assertEquals("400352979", customerList.get(0).getTaxID());
		assertEquals("S", customerList.get(0).getTaxIDType().getCode());
	}

	@Test
	public void testGetCustomerByTaxIds() throws Exception {	
		String taxId = "400352979";
		Integer customerId = 1432871;
		List<ScimsCustomerBO> customerList = getCustomerByTaxIds(taxId);

		assertNotNull(customerList);
		assertEquals(1, customerList.size());
		assertEquals(customerId.toString(), customerList.get(0).getCustomerID());

	}

	
	
	@Test
	public void testGetCustomerCompareResults() throws Exception{
		String taxId = "400352979";
		Integer customerId = 1432871;
		List<ScimsCustomerBO> customerListByTaxId = getCustomerByTaxIds(taxId);
		List<ScimsCustomerBO> customerListByCustomerId = getCustomerByCustomerIds(customerId);
		
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
			Assert.assertNotNull("Error: getCustomerByTaxIds(" + taxId + ")  returned an empty List",customerSource);
			Assert.assertNotNull("Error: getCustomerByCustomerIds(" + customerId + ")  returned an empty List",customerToCompare);
		}
	}


	
	private void verifyResults(ScimsCustomerBO customerSource,
			ScimsCustomerBO customerToCompare) {
		assertEquals(customerSource.getAddressSet().size(), customerToCompare.getAddressSet().size());
		assertEquals(customerSource.getPhoneSet().size(), customerToCompare.getPhoneSet().size());
		
		
		assertEquals(customerSource.getEmailSet().size(), customerToCompare.getEmailSet().size());
		assertEquals(customerSource.getBirthDate(), customerToCompare.getBirthDate());

		assertEquals(customerSource.getBusinessName(), customerToCompare.getBusinessName());
		assertEquals(customerSource.getBusinessType().getDescription(), customerToCompare.getBusinessType().getDescription());
		assertEquals(customerSource.getCommonName(), customerToCompare.getCommonName());
		
		assertEquals(customerSource.getCustomerName(), customerToCompare.getCustomerName());
		assertEquals(customerSource.getEthnicityType().getDescription(), customerToCompare.getEthnicityType().getDescription());
		assertEquals(customerSource.getFirstName(), customerToCompare.getFirstName());
		assertEquals(customerSource.getLastName(), customerToCompare.getLastName());
		assertEquals(customerSource.getLegacyLinkSet().size(), customerToCompare.getLegacyLinkSet().size());
		assertEquals(customerSource.getCustomerIdHistorySet(), customerToCompare.getCustomerIdHistorySet());
		assertEquals(customerSource.getProgramParticipationList().size(),
				customerToCompare.getProgramParticipationList().size());

	}
	
	
	

	private List<ScimsCustomerBO> getCustomerByCustomerIds(Integer customerId) throws Exception{
		
		

		List<Integer> customerIds = new ArrayList<Integer>();
		customerIds.add(customerId);//8579428);

		List<ScimsCustomerBO> customerList = scimsClientProxy
				.getCustomerByCustomerIds(getAgencyToken(),
						customerIds);
		return customerList;
	}

	private List<ScimsCustomerBO> getCustomerByCustomerIdsLite(Integer customerId) throws Exception{
		
		

		List<Integer> customerIds = new ArrayList<Integer>();
		customerIds.add(customerId);//8579428);
		
		RetrieveScimsCustomersByCoreCustomerIdBC bc = new RetrieveScimsCustomersByCoreCustomerIdBC(getAgencyToken(), customerIds, false);
		bc.setMergedIDList(true);

		List<ScimsCustomerBO> customerList = scimsClientProxy.getCustomerByCustomerIdsLite(bc);
		return customerList;
	}
	
	
	private List<ScimsCustomerBO> getCustomerByTaxIds(String taxId) throws Exception {

		List<String> taxIds = new ArrayList<String>();
		taxIds.add(taxId);

		List<ScimsCustomerBO> customerList = 
			scimsClientProxy.getCustomerByTaxIds(getAgencyToken(), 
						taxIds);
		return customerList;
	}
	


	public SCIMSClientProxy getScimsClientProxy() {
		return scimsClientProxy;
	}

	public void setScimsClientProxy(SCIMSClientProxy scimsClientProxy) {
		this.scimsClientProxy = scimsClientProxy;
	}
}
