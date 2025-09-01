package gov.usda.fsa.fcao.flp.flpids.common;

import org.junit.Before;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.scims.base.JNDISpringMockBase;

public class ScimsTestBase extends JNDISpringMockBase{
	private static AgencyToken sgencyToken;
	protected ServiceAgentFacade serviceAgentFacade;
	

	
	@Before
	public void setUp() throws Exception{
		super.setUp();
		ServiceAgentFacade.setLAZYLOADING(true);
		serviceAgentFacade = ServiceAgentFacade.getInstance();
	}
	protected AgencyToken getAgencyToken() {
		if (sgencyToken == null) {
			createAgencyToken();
		}
		return sgencyToken;
	}

	private AgencyToken createAgencyToken() {
		sgencyToken = new AgencyToken();
		sgencyToken.setApplicationIdentifier("SCIMS_TEST");
		sgencyToken.setUserIdentifier("Scims Shared jar Test");
		sgencyToken.setRequestHost("localhost");
		sgencyToken.setProcessingNode("**n/a**");
		sgencyToken.setReadOnly(Boolean.TRUE);
		return sgencyToken;
	}
	

}
