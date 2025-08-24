
package gov.usda.fsa.parmo.frs.ejb.client.contract;

import gov.usda.fsa.common.base.AgencyToken;

/**
 * This class is used only to match the class signature of hte calling service.
 * the RetrieveFarmsServiceContractWrapper is what clients use to populate the values.
 * @author Taff.Andrews
 *
 */
public class RetrieveFarmsServiceContractWrapper extends RetrieveFarmListRequest
{
	public RetrieveFarmsServiceContractWrapper() {
		super();
	}
	
	public RetrieveFarmsServiceContractWrapper(AgencyToken token) {
		super(token);
	}
}
