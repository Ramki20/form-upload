package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.List;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.dao.IFBPProxyDao;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;

public interface IFBPProxyBS {

	public List<FBPProxyBO> retrieveCreditActionForCustomer(
			gov.usda.fsa.common.base.AgencyToken token, Integer coreCustomerId)
			throws DLSBCInvalidDataStopException,FBPServiceBrokerException;
	
	public List<FarmBusinessPlanBO> retrieveDALRDataForCustomer(
			gov.usda.fsa.common.base.AgencyToken token, Integer coreCustomerId)
					throws DLSBCInvalidDataStopException,FBPServiceBrokerException;

	public void setFbpProxyDao(IFBPProxyDao fbpProxyDao);

	public void setAgencyEncryption(AgencyEncryption agencyEncryption);

/**
 * Added to support ls-dashboard
 */
	 public  List<CreditActionBO> getCreditActions(int customerId) throws FBPServiceBrokerException;
	 
	 public List<CreditActionBO> getYEACreditActions(int customerId) throws FBPServiceBrokerException;
	 
	 public boolean isHealthy(AgencyToken token);
}