package gov.usda.fsa.fcao.flp.security.userprofile.eas;

import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.DLSApplicationAgentAssertionException;

import java.util.Collection;
import java.util.Map;

/**
 * Responsible for expanding a given set of EAS "Area of Responsibility" attribute
 * values.  In EAS, the set of values that represents a user's Area of Responsibility
 * is compacted.  This means that if a user has state-wide access for a given state,
 * only the state FLP office code is stored within EAS.  At runtime, the set of
 * service center FLP office codes for the state must be retrieved and added to the
 * set.
 * <p>
 * In addition, any invalid values (non-state or service center FLP office codes) that
 * are encountered should be reported.
 * 
 */
public interface AreaOfResponsibilityUtility{
    
    public interface NormalizedAOREnvelope{
        public Collection<String> getSourceAreaOfResponsibility();
        public Collection<String> getNormalizedAreaOfResponsibility();
        
        public boolean hasWarnings();
        /**
         * Returns the set of warnings as a Map< String, String >.  A key String is either
         * an empty string (indicating a problem with the source collection) or the actual
         * String value encountered in the source collection that is not a valid state or
         * service center FLP office code.  The value String is a human readable warning
         * message related to the key String.
         * 
         * @return the Map of warnings (the Map may be empty).
         */
        public Map <String ,String> getWarningMessages();
    }
    
    /*
     * Responsible for validating that a given collection of String values are FLP office codes; converting this collection
 * of flp office codes (containing both state office FLP codes as well as service center office FLP codes) and returning
 * a collection of service center office FLP codes.  For a given state office FLP code encountered in the source collection,
 * the returned target collection will include every service center FLP code for that state.
     * 
     * May be given an empty or null source Collection
     * May encounter one or more invalid FLP Codes
     * May be unable to normalzie given state FLP Code
     * 
     * MRT may not be available
     */
    /**
     * Responsible for normalizing the stored form of an Area of Responsibility
     * list.  This list may have both service center FLP codes and state office FLP
     * codes.  This utility method will convert the stored form into its corresponding
     * expanded form such that each state office FLP code in the
     * stored form is replaced by a set of FLP codes that corresponds to its
     * related service center subset.
     * <p>
     * The implementation is thread-safe.
     * <p>
     * @param	aor The succinct, stored form of an Area of Responsibility.
     * @return The normalized form of the given AOR along with any warnings.
     * @throws ContractViolationException if the given list contains anything other than String objects.
     * @throws DLSBusinessStopException
     * 		Indicates that the MRT service upon which this utility relies is not
     * 		available.
     */
    public NormalizedAOREnvelope normalizeAreaOfResponsibility( Collection <String> aor )
    	throws DLSApplicationAgentAssertionException ,DLSBusinessStopException;
}
