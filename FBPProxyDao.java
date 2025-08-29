/*
 * Created on Dec 13, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.usda.fsa.fcao.flp.flpids.common.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FBPProxyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmBusinessPlanBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.connectors.FBPProxySBConnector;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPParserException;
import gov.usda.fsa.fcao.flp.flpids.common.dao.exceptions.FBPServiceBrokerException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.JNDILookup;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flpids.common.util.FBPCreditDateParser;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DALRData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DALRRecord;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMData;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.DLMRecord;
import gov.usda.fsa.fcao.flpids.fbpservice.jaxws.FBPWsdlInfo;
import gov.usda.fsa.flp.fbp.bo.CreditActionBO;

/**
 * @author sivagurunathan.nagar
 *
 *         This PO is designed for retrieving DLM Data by invoking the
 *         WebService
 */
public class FBPProxyDao implements IFBPProxyDao {
	private static final Logger logger = LogManager.getLogger(FBPProxyDao.class);
	private gov.usda.fsa.common.base.AgencyToken agencyToken;
	private FBPProxySBConnector fbpProxySBConnector;

	@Override
	public Collection<FarmBusinessPlanBO> retrieveDALRData(AgencyToken token, String passwdtext, Integer corecustomerid)
			throws FBPServiceBrokerException {
		Collection<FarmBusinessPlanBO> returnlist = null;
		DALRData daLRData = null;
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwddigest = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);

		daLRData = fbpProxySBConnector.getDALRData(sitename, username, passwdtext, passwddigest, corecustomerid);

		if (daLRData != null) {
			try {
				returnlist = createFBPList(token, daLRData);
			} catch (Exception e) {
				returnlist = new LinkedList<FarmBusinessPlanBO>();
				logger.error(
						"Exception when parsing the XMLString from FBPProxyPO.retrieveDALRData().createFBPList() for CCID: "
								+ corecustomerid,
						e);
			}
		} else {
			returnlist = new LinkedList<FarmBusinessPlanBO>();
		}
		return returnlist;
	}

	public List<FBPProxyBO> retrieveDLMData(String passwdtext, Integer corecustomerid)
			throws FBPServiceBrokerException {
		List<FBPProxyBO> returnlist = null;
		DLMData dlmData = null;
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwddigest = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);

		dlmData = fbpProxySBConnector.invokeDLMData(sitename, username, passwdtext, passwddigest, corecustomerid);

		if (dlmData != null) {
			try {
				returnlist = setFBPProxyObjects(dlmData);
			} catch (Exception e) {
				logger.error(
						"Exception when parsing the XMLString from FBPProxyPO.retrieveDLMData()/setFBPProxyObjects() for CCID: "
								+ corecustomerid,
						e);
			}
		} else {
			returnlist = new ArrayList<FBPProxyBO>();
		}
		return returnlist;

	}

	public List<CreditActionBO> getYEACreditActions(String passwdtext, Integer customerId)
			throws FBPServiceBrokerException {
		DLMData dlmData = null;
		String sitename = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_SITENAME_JNDI);
		String username = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_USER_NAME_JNDI);
		String passwddigest = JNDILookup.commonFSADirectLookup(FBPWsdlInfo.FBP_PASSWORD_DIGEST_JNDI);

		dlmData = fbpProxySBConnector.getDLMYEAMessageElement(sitename, username, passwdtext, passwddigest, customerId);
		List<CreditActionBO> creditActionList = new ArrayList<CreditActionBO>();
		if (dlmData != null) {
			processYEACreditActionsResult(creditActionList, dlmData);
		}
		return creditActionList;
	}

	public List<CreditActionBO> getCreditActions(String passwdtext, Integer customerId)
			throws FBPServiceBrokerException {
		return getYEACreditActions(passwdtext, customerId);
	}

	/**
	 * This method recieves the parsed elements in an ArrayList to convert it to a
	 * List of persistence objects
	 * 
	 * @param parsedelements
	 * @return List of FBPProxyPO
	 * @throws FBPParserException sivagurunathan.nagar
	 */
	private List<FBPProxyBO> setFBPProxyObjects(DLMData dlmData) throws FBPParserException {
		List<FBPProxyBO> fbpProxyPOList = new ArrayList<FBPProxyBO>();
		try {
			if (dlmData == null || dlmData.getListOfDLMRecords() == null || dlmData.getListOfDLMRecords().isEmpty()) {
				return fbpProxyPOList;
			}
			List<DLMRecord> listOfDLMRecords = dlmData.getListOfDLMRecords();

			for (DLMRecord dlmRecord : listOfDLMRecords) {
				FBPProxyBO fBPProxyBO = new FBPProxyBO(this.agencyToken);

				String creditid = dlmRecord.getCreditActionID();
				String creditdate = dlmRecord.getCreditActionDate();
				String createdate = dlmRecord.getCreditActionCreationDate();

				Date datecredit = FBPCreditDateParser.getDateValue(creditdate);
				Date datecreate = FBPCreditDateParser.getDateValue(createdate);
				fBPProxyBO.setCreditActionID(Integer.parseInt(creditid));
				fBPProxyBO.setCreditActionDesc(dlmRecord.getCreditActionDescr());
				fBPProxyBO.setCreditActionDate(datecredit);
				fBPProxyBO.setCreditActionCreationDate(datecreate);
				fBPProxyBO.setLoanApprovalOffical(dlmRecord.getLoanApprovalOffical());
				fBPProxyBO.setLoanApprovalTitle(dlmRecord.getLoanApprovalTitle());
				fBPProxyBO.setCommentsandReq(dlmRecord.getCommetsAndRequirements());

				fbpProxyPOList.add(fBPProxyBO);
				if (logger.isDebugEnabled()) {
					SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
					logger.debug("listOfDLMRecords.size()" + listOfDLMRecords.size());
					logger.debug("creditid" + creditid);
					logger.debug("CreditActionDescr" + dlmRecord.getCreditActionDescr());
					logger.debug("datecredit" + sdf.format(datecredit));
					logger.debug("datecreate" + datecreate);
					logger.debug("LoanApprovalOffical" + dlmRecord.getLoanApprovalOffical());
					logger.debug("LoanApprovalTitle" + dlmRecord.getLoanApprovalTitle());
					logger.debug("CommetsAndRequirements" + dlmRecord.getCommetsAndRequirements());
				}
			}
		} catch (Exception e) {
			logger.error("Exception when converting parsed XML datas to FBPProxyPO FBPProxyPO.setFBPProxyObjects", e);
			throw new FBPParserException(
					"Exception when converting parsed XML datas to FBPProxyPO FBPProxyPO.setFBPProxyObjects : "
							+ e.getMessage());
		}
		return fbpProxyPOList;
	}

	public void setAgencyToken(gov.usda.fsa.common.base.AgencyToken agencyToken) {
		this.agencyToken = agencyToken;
	}

	public void setFbpProxySBConnector(FBPProxySBConnector fbpProxySBConnector) {
		this.fbpProxySBConnector = fbpProxySBConnector;
	}

	public boolean isHealthy() {
		return (this.fbpProxySBConnector != null && fbpProxySBConnector.isHealthy());
	}

	private FBPProxyDao() {
	}

	private void processYEACreditActionsResult(List<CreditActionBO> creditActionList, DLMData dlmData) {
		try {
			List<DLMRecord> listOfDLMRecords = dlmData.getListOfDLMRecords();

			for (DLMRecord dlmRecord : listOfDLMRecords) {
				CreditActionBO creditAction = new CreditActionBO();
				creditAction.setCreditActionID(Long.parseLong(dlmRecord.getCreditActionID()));
				creditAction.setCreditActionDescr(dlmRecord.getCreditActionDescr());
				creditAction.setCreditActionModelDescr(dlmRecord.getCreditActionModelDescr());

				String creditdate = dlmRecord.getCreditActionDate();
				String createdate = dlmRecord.getCreditActionCreationDate();

				Date datecredit = FBPCreditDateParser.getDateValue(creditdate);
				Date datecreate = FBPCreditDateParser.getDateValue(createdate);

				creditAction.setCreditActionDate(datecredit);
				creditAction.setCreditActionCreationDate(datecreate);

				creditAction.setLoanApprovalOffical(dlmRecord.getLoanApprovalOffical());
				creditAction.setLoanApprovalTitle(dlmRecord.getLoanApprovalTitle());
				creditAction.setOverAllScore(dlmRecord.getOverAllScore());
				creditAction.setOverAllScoreDescr(dlmRecord.getOverAllScoreDescr());

				String scoredateString = dlmRecord.getScoreDate();
				Date scoredate = FBPCreditDateParser.getDateValue(scoredateString);
				creditAction.setScoreDate(scoredate);

				creditAction.setCommetsAndRequirements(dlmRecord.getCommetsAndRequirements());
				creditActionList.add(creditAction);
			}
		} catch (Exception e) {
			logger.error("Exception when converting parsed XML datas to FBPProxyPO FBPProxyPO.setFBPProxyObjects()", e);
		}
	}

	private Collection<FarmBusinessPlanBO> createFBPList(AgencyToken token, DALRData returnedResult) {
		Collection<FarmBusinessPlanBO> plans = new TreeSet<FarmBusinessPlanBO>();
		for (DALRRecord dALRRecord : returnedResult.getListOfDALRRecords()) {
			FarmBusinessPlanBO plan = new FarmBusinessPlanBO();
			plan.setAgencyToken(token);
			plan.setFarmLoanCustomerId(dALRRecord.getCoreCustomerID());
			plan.setCreditActionDescription(dALRRecord.getCaDescr());
			plan.setBeginningDate(dALRRecord.getBegDate());
			plan.setEndDate(dALRRecord.getEndDate());
			plan.setScenarioDescription(dALRRecord.getScenario());
			plan.setFarmOperatingExpenses(Double.parseDouble(dALRRecord.getFarmOperExp()));
			plan.setFarmOperatingInterestExpenses(Double.parseDouble(dALRRecord.getFarmOperIntExp()));
			if (!StringUtil.isEmptyString(dALRRecord.getOwnrWdrw())) {
				plan.setFamilyLivingExpenses(Double.parseDouble(dALRRecord.getOwnrWdrw()));
			}
			plan.setBalanceAvailable(Double.parseDouble(dALRRecord.getBalAvl()));
			plan.setNonAgencyDebtsAndTaxes(Double.parseDouble(dALRRecord.getNonAgcyDebtTax()));
			plans.add(plan);
		}
		return plans;
	}
}
