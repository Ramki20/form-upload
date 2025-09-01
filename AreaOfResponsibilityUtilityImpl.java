package gov.usda.fsa.fcao.flp.security.userprofile.eas;


import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSApplicationAgentAssertionException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;

import java.util.Collection;
import java.util.TreeMap;

class AreaOfResponsibilityUtilityImpl implements AreaOfResponsibilityUtility{	 
	public NormalizedAOREnvelope normalizeAreaOfResponsibility( Collection <String> source )
    	throws DLSApplicationAgentAssertionException ,DLSBusinessStopException{
		Object [] args = new Object[0];
		 TreeMap <String, String> warningMessages = new TreeMap<String, String>();
		 Collection<String> noramalizedAOR   = null;
		try{
			NormalizeAreaOfResponsibility normalizeAreaOfResponsibility =	(NormalizeAreaOfResponsibility)ReflectionUtility.createObject(NormalizeAreaOfResponsibility.class,args);
			noramalizedAOR = normalizeAreaOfResponsibility.process(source, null);
		}catch(Exception e){
			warningMessages.put("noramalizedAOR", e.getMessage());
		}
		 return new NormalizedAOREnvelopeImpl( source, noramalizedAOR, warningMessages );
	}
}
