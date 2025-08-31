package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.FBPProxyBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.fcao.flp.flpids.common.dao.IFBPProxyDao;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.AgencyEncryption;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;

public class FBPProxyBS implements IFBPProxyBS {
	private static final Logger logger = LogManager.getLogger(FBPProxyBS.class);
	private IFBPProxyDao fbpProxyDao = null;
	private AgencyEncryption agencyEncryption;
	private static final String defaultPassword = "";

	public List<FarmBusinessPlanBO> retrieveDALRDataForCustomer(gov.usda.fsa.common.base.AgencyToken token,
			Integer coreCustomerId) throws DLSBCInvalidDataStopException, FBPServiceBrokerException {
		try {
			/** validates the Contract */
			FBPProxyBCValidator.validate(coreCustomerId);
		} catch (Exception e) {
			throw new DLSBCInvalidDataStopException(
					"Exception when validating input data from FBPProxyBS.retrieveCreditActionForCustomer() : "
							+ e.getMessage());
		}
		fbpProxyDao.setAgencyToken(token);
		Collection<FarmBusinessPlanBO> result = fbpProxyDao.retrieveDALRData(token, getDecodedFBPPasscode(),
				coreCustomerId);
		List<FarmBusinessPlanBO> returnResult = new ArrayList<FarmBusinessPlanBO>();
		returnResult.addAll(result);
		return (returnResult);
	}

	public List<FBPProxyBO> retrieveCreditActionForCustomer(gov.usda.fsa.common.base.AgencyToken token,
			Integer coreCustomerId) throws DLSBCInvalidDataStopException, FBPServiceBrokerException {
		try {
			/** validates the Contract */
			FBPProxyBCValidator.validate(coreCustomerId);
		} catch (Exception e) {
			throw new DLSBCInvalidDataStopException(
					"Exception when validating input data from FBPProxyBS.retrieveCreditActionForCustomer() : "
							+ e.getMessage());
		}
		return retrieveDLMData(token, getDecodedFBPPasscode(), coreCustomerId);
	}

	/**
	 * Two methods for ls-dashboard
	 */
	@Override
	public List<CreditActionBO> getCreditActions(int customerId) throws FBPServiceBrokerException {
		return getYEACreditActions(customerId);
	}

	public List<CreditActionBO> getYEACreditActions(int customerId) throws FBPServiceBrokerException {
		return fbpProxyDao.getYEACreditActions(getDecodedFBPPasscode(), customerId);
	}

	public void setFbpProxyDao(IFBPProxyDao fbpProxyDao) {
		this.fbpProxyDao = fbpProxyDao;
	}

	public void setAgencyEncryption(AgencyEncryption agencyEncryption) {
		this.agencyEncryption = agencyEncryption;
	}

	public boolean isHealthy(AgencyToken token) {
		return (token.isReadOnly() && this.agencyEncryption != null && fbpProxyDao != null && fbpProxyDao.isHealthy());
	}

	private FBPProxyBS() {
	}

	private List<FBPProxyBO> retrieveDLMData(gov.usda.fsa.common.base.AgencyToken token, String passwdtext,
			Integer corecustomerid) throws FBPServiceBrokerException {
		fbpProxyDao.setAgencyToken(token);
		return fbpProxyDao.retrieveDLMData(passwdtext, corecustomerid);
	}

	private String getDecodedFBPPasscode() {

		return defaultPassword;
	}
}
