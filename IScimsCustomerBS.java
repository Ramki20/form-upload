package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ParseScimsResultBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByTaxIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CustomerScimsVO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;

import java.util.List;
import java.util.Map;

public interface IScimsCustomerBS {
	public abstract List<ScimsCustomerBO> retrieveScimsCustomersByTaxId(
			RetrieveScimsCustomersByTaxIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException;


	public abstract List<ScimsCustomerBO> retrieveScimsCustomersByCoreCustomerId(
			RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException;
	
	public abstract Map<Integer, ScimsCustomerBO> retrieveScimsCustomersLiteMapByCoreCustomerId(
			RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException;	
	
	public  abstract ScimsCustomerBO parseScimsResultXMLForOneCustomer(
			ParseScimsResultBC contract) throws SCIMSBusinessStopException,
			SCIMSDataNotFoundException, SCIMSBusinessFatalException;
	
	public abstract CustomerScimsVO retrieveScimsCustomersByCoreCustomerId(
			final AgencyToken token, final Integer coreCustId	)
			throws SCIMSBusinessStopException, SCIMSBusinessFatalException,
			SCIMSDataNotFoundException;
	
}