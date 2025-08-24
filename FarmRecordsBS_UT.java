package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FarmRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.common.DLSExternalCommonTestMockBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.parmo.frs.ejb.client.contract.RetrieveFarmsServiceContractWrapper;

import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.core.Assert;
import org.junit.Before;
import org.junit.Test;

public class FarmRecordsBS_UT extends DLSExternalCommonTestMockBase
{
	RetrieveFarmsServiceContractWrapper contract;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
	}

	@Test
	public void testRetrieveFarmRecordByCustomer() throws Exception
	{
		IFarmRecordBS farmRecordsExternalService = (IFarmRecordBS) ServiceAgentFacade.getInstance()
				.getFarmRecordsBusinessService();

		AgencyToken agencyToken = createAgencyToken();
		FarmRequestBC contract1 = new FarmRequestBC(agencyToken);

		contract1.setCoreCustomerId(Long.parseLong(String.valueOf(9680545)));
		contract1.setYear(2017);

		List<FarmResponseBO> farmResponseBO = farmRecordsExternalService.retrieveFarmRecordByCustomer(contract1);
		Assert.isNotNull(farmResponseBO);
	}

	@Test
	public void testRetrieveFarmRecordByCustomerUnhappyValues() throws Exception
	{
		IFarmRecordBS farmRecordsExternalService = (IFarmRecordBS) ServiceAgentFacade.getInstance()
				.getFarmRecordsBusinessService();

		AgencyToken agencyToken = createAgencyToken();
		FarmRequestBC contract1 = new FarmRequestBC(agencyToken);

		contract1.setCoreCustomerId(Long.parseLong(String.valueOf(968054)));
		contract1.setYear(2016);
		contract1.setIncludeCustomerWithResponse(null);
		contract1.setIncludeCropWithResponse(null);
		contract1.setIncludeNonActiveCustomerWithResponse(null);
		contract1.setIncludeTractInfoWithResponse(null);

		try
		{
			farmRecordsExternalService.retrieveFarmRecordByCustomer(contract1);

		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 4)
				Assert.isTrue(true);
		}
	}

	@Test
	public void testRetrieveFarmRecordByCustomerContractNull() throws Exception
	{
		IFarmRecordBS farmRecordsExternalService = (IFarmRecordBS) ServiceAgentFacade.getInstance()
				.getFarmRecordsBusinessService();

		try
		{
			farmRecordsExternalService.retrieveFarmRecordByCustomer(null);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 1)
				Assert.isTrue(true);
		}
	}

	@Test
	public void testRetrieveFarmRecordByCustomerNullValues() throws Exception
	{
		IFarmRecordBS farmRecordsExternalService = (IFarmRecordBS) ServiceAgentFacade.getInstance()
		.getFarmRecordsBusinessService();

		FarmRequestBC contract1 = new FarmRequestBC(null);

		contract1.setCoreCustomerId(null);
		contract1.setYear(null);

		try
		{
			farmRecordsExternalService.retrieveFarmRecordByCustomer(contract1);
		}
		catch (DLSBCInvalidDataStopException ex)
		{
			if (ex.getErrorMessageList().size() == 3)
				Assert.isTrue(true);
		}
	}

	protected AgencyToken createAgencyToken()
	{
		return createAgencyToken("DLMTest_User");
	}

	protected AgencyToken createAgencyToken(String inUserId)
	{
		AgencyToken agencyToken = new AgencyToken();
		agencyToken.setProcessingNode("DLM_jUnit_TEST");
		agencyToken.setApplicationIdentifier("DLM-Test");
		agencyToken.setRequestHost("localhost");
		agencyToken.setUserIdentifier(inUserId);
		agencyToken.setReadOnly(true);
		return agencyToken;
	}
}
