package gov.usda.fsa.fcao.flp.flpids.mock.impl;

import gov.usda.fsa.common.base.AgencyException;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalCancelReceivableContract;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalCreateReceivableContract;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalReinstateReceivableContract;
import gov.usda.fsa.nrrs.biz.contract.impl.recv.ExternalRetrieveReceivableContract;
import gov.usda.fsa.nrrs.services.client.NRRSServiceProxy;
import gov.usda.fsa.nrrs.vo.recv.ExternalCancelReceivableResponse;
import gov.usda.fsa.nrrs.vo.recv.ExternalReinstateReceivableResponse;
import gov.usda.fsa.nrrs.vo.recv.ExternalRetrieveReceivableResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NRRSServiceImpl implements NRRSServiceProxy
{

	public List<BigDecimal> createReceivables(List<ExternalCreateReceivableContract> contracts) throws AgencyException
	{
		List<BigDecimal> receivableIds = new ArrayList<BigDecimal>();
		receivableIds.add(new BigDecimal(12345));
		return receivableIds;
	}

	public boolean isHealthy()
	{

		return true;
	}

	@Override
	public ExternalCancelReceivableResponse cancelReceivable(
			ExternalCancelReceivableContract contract) throws AgencyException {
		ExternalCancelReceivableResponse response = new ExternalCancelReceivableResponse();
		return response;
	}
	
	@Override
	public ExternalRetrieveReceivableResponse retrieveReceivable(
			ExternalRetrieveReceivableContract contract) throws AgencyException {
		ExternalRetrieveReceivableResponse response = new ExternalRetrieveReceivableResponse();
		return response;
	}
	
	@Override
    public ExternalReinstateReceivableResponse reinstateReceivable(
    		ExternalReinstateReceivableContract contract) throws AgencyException {
		
		ExternalReinstateReceivableResponse response = new ExternalReinstateReceivableResponse();
		return response;
	}

}
