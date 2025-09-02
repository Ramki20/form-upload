package gov.usda.fsa.fcao.flp.services.interest.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.InterestRateRef;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.ServiceBrokerException;
import gov.usda.fsa.fcao.flp.interest.MartInterestRateBusinessService;

/**
 * Implements the MartInterestRateBusinessService.
 * 
 * @author tvs.murthy
 */
public class MartInterestRateBusinessServiceImpl implements MartInterestRateBusinessService {

	private IMRTProxyBS dataMartBusinessService;


	public InterestRateRef getInterestRate(String interestRateTypeId,
			Date effectiveDate, AgencyToken token) throws ServiceBrokerException {

		List<String> rateTypeIds = new LinkedList<String>();
		rateTypeIds.add(interestRateTypeId);

		List<InterestRateRef> rates = getInterestRates(rateTypeIds, effectiveDate,
				token);
		if (rates != null && !rates.isEmpty()) {
			return rates.get(0);
		} else {
			return null;
		}
	}

	
	public List<InterestRateRef> getInterestRates(
			List<String> rateTypeIds, Date effectiveDate, AgencyToken token)
			throws ServiceBrokerException {
		try {
			List<InterestRateRef> interestRates = new LinkedList<InterestRateRef>();

			Integer integerRateTypeIds[] = new Integer[rateTypeIds.size()];
			for (int index = 0; index < rateTypeIds.size(); index++) {
				integerRateTypeIds[index] = (Integer.valueOf(rateTypeIds
						.get(index)));
			}
			
			
			List<gov.usda.fsa.citso.cbs.dto.InterestRate> mrtMatchingRates = getDataMartBusinessService()
					.retrieveInterestRates(integerRateTypeIds, effectiveDate);

			for (gov.usda.fsa.citso.cbs.dto.InterestRate cbsFacade : mrtMatchingRates) {

				InterestRateRef interestRate = new InterestRateRef();
				String rateIdentifier = String.valueOf(cbsFacade.getId());
				interestRate.setRateIdentifier(rateIdentifier);
				interestRate.setInterestRate(cbsFacade.getIntRate());
				interestRate.setTypeName(cbsFacade.getTypeName());
				interestRates.add(interestRate);
			}

			return interestRates;		
		} catch (Exception e) {
			ServiceBrokerException ex = new ServiceBrokerException("error.unexpected",  e);
			ex.setReplacementParams(token.getUserIdentifier());
			throw ex;
		}
	}

	public IMRTProxyBS getDataMartBusinessService() {
		return dataMartBusinessService;
	}

	public void setDataMartBusinessService(IMRTProxyBS dataMartBusinessService) {
		this.dataMartBusinessService = dataMartBusinessService;
	}

}
