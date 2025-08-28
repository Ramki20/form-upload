package gov.usda.fsa.fcao.flp.flpids.common.business.common;

import gov.usda.fsa.common.base.AgencyToken;


public class DLSExternalCommonTestAgency {
	protected AgencyToken createAgencyToken(){
		gov.usda.fsa.common.base.AgencyToken token = new AgencyToken();
		token.setRequestHost("FCAO");
		token.setApplicationIdentifier("FCAO");
		token.setUserIdentifier("FCAO");
		token.setProcessingNode("DLS_Common");
		token.setReadOnly();
		return token;
	}
}
