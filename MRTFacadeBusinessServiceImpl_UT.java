package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.mockito.Mockito.mock;
import gov.usda.fsa.citso.cbs.client.InterestRateDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy;
import gov.usda.fsa.citso.cbs.client.StateDataServiceProxy;
import gov.usda.fsa.citso.cbs.dto.LocationArea;
import gov.usda.fsa.citso.cbs.dto.Office;
import gov.usda.fsa.citso.cbs.dto.State;
import gov.usda.fsa.common.base.InvalidBusinessContractDataException;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateLocationAreaCodeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CountyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MailCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTNoDataException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MRTFacadeBusinessServiceImpl_UT.class, MRTFacadeBusinessService.class, 
	OfficeDataServiceProxy.class, 
	StateDataServiceProxy.class, InterestRateDataServiceProxy.class, 
	LocationAreaDataServiceProxy.class})
public class MRTFacadeBusinessServiceImpl_UT extends DLSExternalCommonTestMockBase{
	private MRTFacadeBusinessService service;
	
	private gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy mockOfficeDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.StateDataServiceProxy mockStateDataServiceProxy;
	private gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy mockLocationAreaDataServiceProxy;
	StateLocationAreaCodeBC contract;
	
	@Before
	public void setUp() throws Exception{
		super.setUp();
		mockOfficeDataServiceProxy = mock (gov.usda.fsa.citso.cbs.client.OfficeDataServiceProxy.class);
		mockStateDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.StateDataServiceProxy.class);
		mockLocationAreaDataServiceProxy = mock(gov.usda.fsa.citso.cbs.client.LocationAreaDataServiceProxy.class);
		
		service = ServiceAgentFacade.getInstance().getMrtFacadeBusinessService();
		
		Assert.assertTrue(service instanceof MRTFacadeBusinessServiceImpl);
		MRTFacadeBusinessServiceImpl mrtFacadeBSImpl = (MRTFacadeBusinessServiceImpl)service;
		mrtFacadeBSImpl.setOfficeBusinessService(mockOfficeDataServiceProxy);
		mrtFacadeBSImpl.setStateBusinessService(mockStateDataServiceProxy);
		mrtFacadeBSImpl.setLocationBusinessService(mockLocationAreaDataServiceProxy);
		
		contract = 	new StateLocationAreaCodeBC(this.createAgencyToken(),"12345");
	}
	@Test
	public void testGetgetStatesList() throws Exception{		
		
		// mock service
		List<State> stateReadFacades = new ArrayList<State>();
		
		State st = new State();
		st.setCode("61");
		st.setAbbreviation("HI");
		st.setName("Hawaii");

		State st1 = new State();
		st1.setCode("62");
		st1.setAbbreviation("FM");
		st1.setName("Federated States of Micronesia");
		
		stateReadFacades.add(st);
		stateReadFacades.add(st1);

		Mockito.when(mockStateDataServiceProxy.allFlp()).thenReturn(stateReadFacades);
		
		// call to service
		List<StateBO> states = service.getStatesList();
		
		Assert.assertNotNull(states);
		Assert.assertTrue(states.size() > 0);	
		
	}
	
	@Test
	public void testGetCountriesListWrongStateCode() throws Exception{
		StateBC contract = new StateBC(this.createAgencyToken(),"123","MO");
		
		// mock the service
		List<LocationArea> countyReadFacades = new ArrayList<LocationArea>();
		Mockito.when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
		
		// call service
		List<CountyBO> countries = service.getCountiesList(contract);
		
		Assert.assertNotNull(countries);
		Assert.assertEquals(0, countries.size());			
	}
	
	@Test
	public void testGetCountriesListWithCorrectCode() throws Exception{
		StateBC contract = new StateBC(this.createAgencyToken(),"30","MO");
		
		// mock service
		List<LocationArea> countyReadFacades = new ArrayList<LocationArea>();
		LocationArea county = new LocationArea();
		county.setStateCode("30");
		county.setCode("010");
		county.setName("Johnson");
		countyReadFacades.add(county);
		
		Mockito.when(mockLocationAreaDataServiceProxy.flpByStateAbbr("MO")).thenReturn(countyReadFacades);
		
		// call service
		List<CountyBO> countries = service.getCountiesList(contract);
		CountyBO countyBo = countries.get(0);
		
		Assert.assertNotNull(countries);
		Assert.assertTrue( countries.size() > 0);
		Assert.assertNotNull(countyBo);
		Assert.assertNotNull(countyBo.getCountyCode());
		Assert.assertNotNull(countyBo.getCountyName());
	}
	
	@Test (expected=MRTNoDataException.class)
	public void testGetMailCodeNoResult() throws Exception{
		
		service.getMailCode(contract);		
	}
	
	@Test 
	public void testGetMailCodeWithResult() throws Exception{
		contract.setStateLocationAreaCode("58062");
		
		// mock service
		List<Office> serviceCenterReadFacades = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("01305");
		serviceCenterReadFacades.add(office);
		
		String[] stateLocationAreaCodesArray = {"58062"};
		Mockito.when(mockOfficeDataServiceProxy.
				flpServiceCenterOfficesByFlpStateAndLocAreas(stateLocationAreaCodesArray)).
					thenReturn(serviceCenterReadFacades);
		
		// call service
		String mailCode = service.getMailCode(contract);
		Assert.assertNotNull(mailCode);			
	}
	
	@Test (expected=InvalidBusinessContractDataException.class)
	public void testGetMailCodeInvalidContract() throws Exception{
		contract.setStateLocationAreaCode(null);
		
		service.getMailCode(contract);	
	}
	
	@Test
	public void testGetMailCodeListWithCorrectCode() throws Exception{
		StateBC contract = new StateBC(this.createAgencyToken(),"30");
		contract.setStateAbbr("MO");
		
		// mock service
		List<Office> serviceCenterReadFacades = new ArrayList<Office>();
		Office office = new Office();
		office.setOfficeCode("01305");
		office.setCountyName("Jackson");
		office.setLocCityName("Kansas City");
		
		serviceCenterReadFacades.add(office);
		
		Mockito.when(mockOfficeDataServiceProxy
			.fsaFlpServiceCenterOfficesByStateAbbr("MO")).thenReturn(serviceCenterReadFacades);
		
		// call service
		List<MailCodeBO> mailCodes = service.getMailCodesList(contract);
		MailCodeBO mailCodeBO = mailCodes.get(0);
		
		Assert.assertNotNull(mailCodes);
		Assert.assertTrue( mailCodes.size() > 0);	
		Assert.assertNotNull(mailCodeBO);
		Assert.assertNotNull(mailCodeBO.getCityName());
		Assert.assertNotNull(mailCodeBO.getCountyName());
		Assert.assertNotNull(mailCodeBO.getMailCode());
	}
}
