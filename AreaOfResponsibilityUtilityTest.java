package gov.usda.fsa.fcao.flp.security.userprofile.eas;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceCenterFLPCodeManager;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.StateAbbrFromStateFLPLookUp;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;
import gov.usda.fsa.fcao.flp.security.userprofile.eas.AreaOfResponsibilityUtility.NormalizedAOREnvelope;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaOfResponsibilityUtilityTest {
	private AreaOfResponsibilityUtility areaOfResponsibilityUtility;
	private List<String> source;
	private ServiceCenterFLPCodeManager mockServiceCenterFLPCodeManager;
	private StateAbbrFromStateFLPLookUp mockStateAbbrFromStateFLPLookUp;
	@BeforeClass
	public static void classScopeSetUp() throws Exception{
		ServiceAgentFacade.setLAZYLOADING(true);
	}
	@Before
	public void setUp() throws Exception {
		areaOfResponsibilityUtility = new AreaOfResponsibilityUtilityImpl();
		source = new ArrayList<String>();
		
		ServiceAgentFacade serviceAgentFacade = ServiceAgentFacade.getInstance();
		mockServiceCenterFLPCodeManager = mock(ServiceCenterFLPCodeManager.class);
		mockStateAbbrFromStateFLPLookUp = mock(StateAbbrFromStateFLPLookUp.class);
		
		ReflectionUtility.setAttribute(serviceAgentFacade, mockServiceCenterFLPCodeManager, "serviceCenterFLPCodeManager", ServiceAgentFacade.class);
		ReflectionUtility.setAttribute(serviceAgentFacade, mockStateAbbrFromStateFLPLookUp, "stateAbbrFromStateFLPLookUp", ServiceAgentFacade.class);
	}

	@Test
	public void test_normalizeAreaOfResponsibility_emptySource() throws Exception {	
		NormalizedAOREnvelope normalizedAOREnvelope = areaOfResponsibilityUtility.normalizeAreaOfResponsibility(source);
		
		Assert.assertNotNull(normalizedAOREnvelope);
		Assert.assertNotNull(normalizedAOREnvelope.getNormalizedAreaOfResponsibility());
		Assert.assertSame(source,normalizedAOREnvelope.getSourceAreaOfResponsibility());
		Assert.assertNotNull(normalizedAOREnvelope.getWarningMessages());
	}
	@Test
	public void test_normalizeAreaOfResponsibility_oneItem_invalidOfficeCode() throws Exception {	
		source.add("02300");
		List<ServiceCenterFlpOfficeCodeBO> serviceCenterOfficeList = new ArrayList<ServiceCenterFlpOfficeCodeBO>();
		ServiceCenterFlpOfficeCodeBO item = new ServiceCenterFlpOfficeCodeBO("02209","virtual office");
		serviceCenterOfficeList.add(item);
		when(mockServiceCenterFLPCodeManager.retrieveServiceCenterFLPCodesByStateAbbr(eq("MO"))).thenReturn(serviceCenterOfficeList);
		when( mockStateAbbrFromStateFLPLookUp.getAbbreviation(eq("02"))).thenReturn("MO");
		
		NormalizedAOREnvelope normalizedAOREnvelope = areaOfResponsibilityUtility.normalizeAreaOfResponsibility(source);
		
		Assert.assertNotNull(normalizedAOREnvelope);
		Assert.assertNotNull(normalizedAOREnvelope.getNormalizedAreaOfResponsibility());
		
		Assert.assertSame(source,normalizedAOREnvelope.getSourceAreaOfResponsibility());
		Assert.assertNotNull(normalizedAOREnvelope.getWarningMessages());
		Assert.assertEquals(1,normalizedAOREnvelope.getNormalizedAreaOfResponsibility().size());
	}
	
	@Test
	public void test_normalizeAreaOfResponsibility_oneItem_validOfficeCode() throws Exception {	
		source.add("02300");
		List<ServiceCenterFlpOfficeCodeBO> serviceCenterOfficeList = new ArrayList<ServiceCenterFlpOfficeCodeBO>();
		ServiceCenterFlpOfficeCodeBO item = new ServiceCenterFlpOfficeCodeBO("02309","virtual office");
		serviceCenterOfficeList.add(item);
		when(mockServiceCenterFLPCodeManager.retrieveServiceCenterFLPCodesByStateAbbr(eq("MO"))).thenReturn(serviceCenterOfficeList);
		when( mockStateAbbrFromStateFLPLookUp.getAbbreviation(eq("02"))).thenReturn("MO");
		
		NormalizedAOREnvelope normalizedAOREnvelope = areaOfResponsibilityUtility.normalizeAreaOfResponsibility(source);
		
		Assert.assertNotNull(normalizedAOREnvelope);
		Assert.assertNotNull(normalizedAOREnvelope.getNormalizedAreaOfResponsibility());
		
		Assert.assertSame(source,normalizedAOREnvelope.getSourceAreaOfResponsibility());
		Assert.assertNotNull(normalizedAOREnvelope.getWarningMessages());
		Assert.assertFalse(normalizedAOREnvelope.getNormalizedAreaOfResponsibility().isEmpty());
	}
}
