package gov.usda.fsa.fcao.flp.flpids.util;

import static org.mockito.Mockito.when;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.OfficeInfoCacheManager;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
public class OfficeInfoCacheManager_UT extends ExternalDependenciesMockBase{

		@Before
		public void setUp() throws Exception{
			super.setUp();	
			when(mocMrtFacadeBusinessService.getStatesList()).thenReturn(populateStateList());
			ServiceAgentFacade.setLAZYLOADING(true);			
			ServiceAgentFacade instance = ServiceAgentFacade.getInstance();
			ReflectionUtility.setAttribute(instance, mocMrtFacadeBusinessService, "mrtFacadeBusinessService");
			
			Assert.assertNotNull(OfficeInfoCacheManager.getStateMap());
		}
		
		@Test
		public void test_mapPopulation() throws Exception{
			Assert.assertTrue(OfficeInfoCacheManager.getStateMap().size() > 0);
		}
		
		@Test
		public void test_isUserInJurisdiction_differentStateFlpOfficeCode() throws Exception{
			Assert.assertFalse(OfficeInfoCacheManager.isUserInJurisdiction("20345", "31300"));
		}
		
		@Test
		public void test_isUserInJurisdiction_differentStateFlpOfficeCodeSameState() throws Exception{
			Assert.assertTrue(OfficeInfoCacheManager.isUserInJurisdiction("20345", "21300"));
		}
		
		@Test
		public void test_isUserInJurisdiction_sameStateFlpOfficeCode() throws Exception{
			Assert.assertTrue(OfficeInfoCacheManager.isUserInJurisdiction("20345", "20300"));
		}
		
		@Test
		public void test_getStateAbbr_exist() throws Exception{
			Assert.assertNotNull(OfficeInfoCacheManager.getStateAbbr("20345"));
		}
		
		@Test
		public void test_getStateAbbr_notExist() throws Exception{
			Assert.assertNull(OfficeInfoCacheManager.getStateAbbr("99345"));
		}
}
