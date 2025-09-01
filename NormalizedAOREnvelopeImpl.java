package gov.usda.fsa.fcao.flp.security.userprofile.eas;

import gov.usda.fsa.fcao.flp.security.userprofile.eas.AreaOfResponsibilityUtility.NormalizedAOREnvelope;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

class NormalizedAOREnvelopeImpl implements NormalizedAOREnvelope
{
    private Collection <String> sourceAOR;
    private Collection <String> normalizedAOR;
    private TreeMap <String, String>warningMessages;
    
    /**
     * 
     */
    NormalizedAOREnvelopeImpl( Collection <String> source, Collection<String> normalized, TreeMap<String ,String> warnings )
    {
        super();
        sourceAOR = source;
        normalizedAOR = normalized;
        warningMessages = warnings;
    }

    public Collection <String>getSourceAreaOfResponsibility() { return sourceAOR; }
    public Collection <String> getNormalizedAreaOfResponsibility() { return normalizedAOR; }
    public boolean hasWarnings() { return !warningMessages.isEmpty(); }
    public Map <String, String> getWarningMessages() { 
    	Map <String, String> clone = new TreeMap<String, String>();
    	clone.putAll(warningMessages);
    	return clone; 
    }
}
