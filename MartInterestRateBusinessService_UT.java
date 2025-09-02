package gov.usda.fsa.fcao.flp.services.interest.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import gov.usda.fsa.citso.cbs.dto.InterestRate;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.InterestRateRef;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.ReflectionUtility;
import gov.usda.fsa.fcao.flp.interest.MartInterestRateBusinessService;

public class MartInterestRateBusinessService_UT extends DLSExternalCommonTestMockBase {
	private MartInterestRateBusinessService martInterestRateBusinessService;
	private IMRTProxyBS mockMataMartBusinessService;

	public void setUp() throws Exception {
		// super.setUp();
		mockMataMartBusinessService = Mockito.mock(IMRTProxyBS.class);

		martInterestRateBusinessService = ServiceAgentFacade.getInstance().getMartInterestRateBusinessService();

		ReflectionUtility.setAttribute(martInterestRateBusinessService, mockMataMartBusinessService,
				"dataMartBusinessService");
	}

	@Test
	public void retrieveInterestRates() throws Exception {

		Date effectiveDate = new Date();
		String interestRateTypeId = "02";

		List<gov.usda.fsa.citso.cbs.dto.InterestRate> mrtMatchingRates = new ArrayList<gov.usda.fsa.citso.cbs.dto.InterestRate>();
		InterestRate rate1 = new InterestRate();
		rate1.setId(1);
		rate1.setIntRate(new BigDecimal(0.25));
		rate1.setTypeName("mrtRate");

		mrtMatchingRates.add(rate1);

		Mockito.when(mockMataMartBusinessService.retrieveInterestRates(Mockito.any(Integer[].class),
				Mockito.any(Date.class))).thenReturn(mrtMatchingRates);

		InterestRateRef returnResult = martInterestRateBusinessService.getInterestRate(interestRateTypeId,
				effectiveDate, this.createAgencyToken());

		Assert.assertNotNull(returnResult.getInterestRate());
	}

}
