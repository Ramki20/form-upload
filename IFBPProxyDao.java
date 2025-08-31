package gov.usda.fsa.fcao.flp.flpids.common.dao;

import java.util.Collection;
import java.util.List;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;

public interface IFBPProxyDao {

	public abstract List<FBPProxyBO> retrieveDLMData(String passwdtext, Integer corecustomerid)
			throws FBPServiceBrokerException;

	public abstract void setAgencyToken(gov.usda.fsa.common.base.AgencyToken agencyToken);

	public abstract List<CreditActionBO> getCreditActions(String passwdtext, Integer customerId)
			throws FBPServiceBrokerException;

	public abstract List<CreditActionBO> getYEACreditActions(String passwdtext, Integer customerId)
			throws FBPServiceBrokerException;

	public Collection<FarmBusinessPlanBO> retrieveDALRData(AgencyToken token, String passwdtext, Integer corecustomerid)
			throws FBPServiceBrokerException;

	public abstract boolean isHealthy();

}