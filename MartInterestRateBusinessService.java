package gov.usda.fsa.fcao.flp.interest;

import java.util.Date;
import java.util.List;

import gov.usda.fsa.common.base.AgencyBusinessService;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.InterestRateRef;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.ServiceBrokerException;

/**
 * The <code>InterestRateBusinessService</code> is used to obtain interest rates
 * for Farm Loans from various sources as needed.
 * 
 * @author tvs.murthy
 */
public interface MartInterestRateBusinessService extends AgencyBusinessService {

	public InterestRateRef getInterestRate(String interestRateTypeId, Date effectiveDate, AgencyToken token)
			throws ServiceBrokerException;

	public List<InterestRateRef> getInterestRates(List<String> rateTypeIds, Date rateEffectiveDate, AgencyToken token)
			throws ServiceBrokerException;

}
