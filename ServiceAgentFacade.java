package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.usda.fsa.fcao.flp.flpids.common.ContractValidator;
import gov.usda.fsa.fcao.flp.flpids.common.auth.EASUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.auth.MockUserProfile;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTInterfaceException;
import gov.usda.fsa.fcao.flp.flpids.common.exceptions.ApplicationAgentException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.NormalizeAreaOfResponsibility;
import gov.usda.fsa.fcao.flp.interest.MartInterestRateBusinessService;
import gov.usda.fsa.fcao.flp.services.cmbs.ContentManagementBusinessServiceConnector;
import gov.usda.fsa.fcao.flp.user.UserInformationBusinessService;

/**
 * ServiceAgentFacade <br>
 * <br>
 * Single stop to load and retrieve all the External Service Beans.
 * 
 * @author partha.chowdhury
 * @version 02/13/2014 - individualize the external service calls for FSFL as we
 *          don't need all the services for FSFL in one place.
 * 
 */
public final class ServiceAgentFacade implements StateAbbrFromStateFLPLookUp, ServiceCenterFLPCodeManager {
	private static boolean LAZYLOADING = false;
	private static ServiceAgentFacade INSTANCE;
	private StateAbbrFromStateFLPLookUp stateAbbrFromStateFLPLookUp = null;
	private ServiceCenterFLPCodeManager serviceCenterFLPCodeManager = null;
	private IMRTProxyBS mrtProxyBusinessService;
	private MRTFacadeBusinessService mrtFacadeBusinessService;

	private IFBPProxyBS fbpProxyBusinessService;
	private ApplicationContext applicationContext;
	private ApplicationContext applicationContextScimsOnly;
	private ApplicationContext applicationContextFsfl;
	private ApplicationContext applicationContextFBP;

	private ApplicationContext applicationContextCmbs;

	private IScimsCustomerBS scimsBusinessService;
	private IFarmRecordBS farmRecordsBusinessService;
	private IDisbursementBS disbursementBusinessService;
	private INRRSReceivableBS receivableBusinessService;
	private INRRSCollectionBS collectionBusinessService;
	private IESCOAPTransactionBS legacyTransactionBusinessService;
	private ContentManagementBusinessServiceConnector cmbsConnector;
	/**
	 * moved from farmLoanServicesImpl
	 */
	private UserInformationBusinessService userInformationBusinessService;
	private gov.usda.fsa.fcao.flp.fbp.FarmBusinessPlanService farmBusinessPlanService;
	private MartInterestRateBusinessService martInterestRateBusinessService;

	private static String[] SPRING_CONFIGURATIONS = {
			"classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/common-external-service-spring-config.xml" };
	private static String[] SCIMS_SPRING_CONFIGURATIONS = {
			"classpath*:/gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/common-external-scims-only-spring-config.xml" };
	private static String[] FSFL_SPRING_CONFIG = {
			"classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/fsfl-external-service-spring-config.xml" };

	private static String[] FBP_SPRING_CONFIG = {
			"classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/fbp-service-spring-config.xml" };

	private static String[] CMBS_SPRING_CONFIG = {
			"classpath:gov/usda/fsa/fcao/flp/flpids/common/business/businessServices/cmbs-external-service-spring-config.xml" };

	private static final Logger logger = LogManager.getLogger(ServiceAgentFacade.class);

	public static ServiceAgentFacade getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ServiceAgentFacade();
		}
		return INSTANCE;
	}

	/**
	 * Added to support three services moved from farmLoanServicesImpl
	 */
	public UserInformationBusinessService getUserInformationBusinessService() {
		return userInformationBusinessService;
	}

	public MartInterestRateBusinessService getMartInterestRateBusinessService() {
		return martInterestRateBusinessService;
	}

	/**
	 * ended moved from farmLoanServicesImpl
	 */

	public String getAbbreviation(String stateFLPCode) throws MRTInterfaceException {
		return stateAbbrFromStateFLPLookUp.getAbbreviation(stateFLPCode);
	}

	public boolean isAbbreviation(String stateAbbr) throws MRTInterfaceException {
		return stateAbbrFromStateFLPLookUp.isAbbreviation(stateAbbr);
	}

	public List<StateBO> fetchStateList() throws MRTInterfaceException {
		return stateAbbrFromStateFLPLookUp.fetchStateList();
	}

	public List<ServiceCenterFlpOfficeCodeBO> retrieveServiceCenterFLPCodes(String stateAbbr)
			throws ApplicationAgentException, DLSBusinessStopException {
		return serviceCenterFLPCodeManager.retrieveServiceCenterFLPCodesByStateAbbr(stateAbbr);
	}

	@Override
	public List<ServiceCenterFlpOfficeCodeBO> retrieveServiceCenterFLPCodesByStateAbbr(String stateAbbr)
			throws ApplicationAgentException, DLSBusinessStopException {
		return retrieveServiceCenterFLPCodes(stateAbbr);
	}

	@Override
	public List<ServiceCenterFlpOfficeCodeBO> retrieveFlpServiceCentersByFlpOfficeCode(List<String> flpOfficeCodeIds)
			throws DLSBusinessStopException {
		return retrieveFlpServiceCentersByOIP(flpOfficeCodeIds);
	}

	public List<ServiceCenterFlpOfficeCodeBO> retrieveFlpServiceCentersByOIP(List<String> flpOfficeCodeIds)
			throws DLSBusinessStopException {
		return serviceCenterFLPCodeManager.retrieveFlpServiceCentersByFlpOfficeCode(flpOfficeCodeIds);
	}

	public ApplicationContext getApplicationContext() {
		if (applicationContext == null && LAZYLOADING) {
			this.initialize();
			setLAZYLOADING(true);
		}
		return applicationContext;
	}

	public IMRTProxyBS getMrtProxyBusinessService() {
		return mrtProxyBusinessService;
	}

	public MRTFacadeBusinessService getMrtFacadeBusinessService() {
		return mrtFacadeBusinessService;
	}

	public IScimsCustomerBS getScimsCustomerBS() {
		return getScimsBusinessService();
	}

	public IFBPProxyBS getFbpProxyBusinessService() {
		if (this.fbpProxyBusinessService == null) {
			initializeFBPDependencies();
		}
		return this.fbpProxyBusinessService;
	}

	public gov.usda.fsa.fcao.flp.fbp.FarmBusinessPlanService getFarmBusinessPlanService() {
		if (this.farmBusinessPlanService == null) {
			initializeFBPDependencies();
		}
		return farmBusinessPlanService;
	}

	public ContentManagementBusinessServiceConnector getCmbsConnector() {
		if (applicationContextCmbs == null) {
			initializeCMBSDependencies();
		}
		return this.cmbsConnector;
	}

	public IFarmRecordBS getFarmRecordsBusinessService() {
		if (this.farmRecordsBusinessService == null) {
			initializeFsflDependencies();
			this.farmRecordsBusinessService = (FarmRecordBS) getApplicationContextFsfl().getBean("farmRecordsProxy");
		}

		return farmRecordsBusinessService;
	}

	public IScimsCustomerBS getScimsBusinessService() {
		if (this.scimsBusinessService == null) {
			initializeScimsOnlyDependencies();
			applicationContextScimsOnly.getBean("scimsClientProxy");
			
			this.scimsBusinessService = (IScimsCustomerBS) applicationContextScimsOnly.getBean("scimsBusinessService");
		}
		return scimsBusinessService;
	}

	public IDisbursementBS getDisbursementBusinessService() {
		if (this.disbursementBusinessService == null) {
			initializeFsflDependencies();
			this.disbursementBusinessService = (DisbursementBS) getApplicationContextFsfl()
					.getBean("disbursementProxy");
		}
		return disbursementBusinessService;
	}

	public INRRSReceivableBS getReceivableBusinessService() {
		if (this.receivableBusinessService == null) {
			initializeFsflDependencies();
			this.receivableBusinessService = (INRRSReceivableBS) getApplicationContextFsfl()
					.getBean("nrrsReceivableBS");
		}
		return receivableBusinessService;
	}

	public INRRSCollectionBS getCollectionBusinessService() {
		if (this.collectionBusinessService == null) {
			initializeFsflDependencies();
			this.collectionBusinessService = (NRRSCollectionBS) getApplicationContextFsfl().getBean("nrrsCollectionBS");
		}
		return collectionBusinessService;
	}

	public IESCOAPTransactionBS getLegacyTransactionBusinessService() {
		if (this.legacyTransactionBusinessService == null) {
			initializeFsflDependencies();
			this.legacyTransactionBusinessService = (ESCOAPTransactionBS) getApplicationContextFsfl()
					.getBean("escoapTransactionBS");
		}
		return legacyTransactionBusinessService;
	}

	public ApplicationContext getApplicationContextFsfl() {
		return applicationContextFsfl;
	}

	private ServiceAgentFacade() {
		logger.info("Is Lazy Loading? " + LAZYLOADING);
		if (!LAZYLOADING) {
			initialize();
		}
	}

	private synchronized void initialize() {
		if (applicationContext == null) {
			logger.info("Spring configuration file:" + SPRING_CONFIGURATIONS);

			this.applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIGURATIONS);

			this.stateAbbrFromStateFLPLookUp = (StateAbbrFromStateFLPLookUp) applicationContext
					.getBean("mrtStateAbbrFromStateFLPLookUp");
			this.serviceCenterFLPCodeManager = (ServiceCenterFLPCodeManager) applicationContext
					.getBean("serviceCenterFLPCodeManager");
			this.mrtProxyBusinessService = (MRTProxyBS) applicationContext.getBean("mrtProxyBS");
			this.mrtFacadeBusinessService = (MRTFacadeBusinessService) applicationContext
					.getBean("mrtFacadeBusinessService");

			this.userInformationBusinessService = (UserInformationBusinessService) applicationContext
					.getBean("userInformationService");

			this.martInterestRateBusinessService = (MartInterestRateBusinessService) applicationContext
					.getBean("interestRateBusinessService");

			NormalizeAreaOfResponsibility normalizeAreaOfResponsibility = (NormalizeAreaOfResponsibility) applicationContext
					.getBean("normalizeAreaOfResponsibility");
			normalizeAreaOfResponsibility.setServiceAgentFacade(this);

			EASUserProfile easUserProfile = (EASUserProfile) applicationContext.getBean("easUserProfile");
			MockUserProfile mockUserProfile = (MockUserProfile) applicationContext.getBean("mockUserProfile");

			ContractValidator contractValidator = mockUserProfile.getContractValidator();
			if (logger.isDebugEnabled()) {
				logger.debug("ESAUserProfile check " + easUserProfile.getNormalizeAreaOfResponsibility());
				logger.debug("ESAUserProfile check "
						+ easUserProfile.getNormalizeAreaOfResponsibility().getServiceAgentFacade());

				logger.debug("MockUserProfile check " + mockUserProfile);
				logger.debug("MockUserProfile dependency (ContractValidator) check " + contractValidator);

			}
			logger.info("Spring configuration completed");
		}
	}

	private synchronized void initializeFsflDependencies() {
		if (getApplicationContextFsfl() == null) {
			logger.info("Spring configuration file:" + FSFL_SPRING_CONFIG);
			this.applicationContextFsfl = new ClassPathXmlApplicationContext(FSFL_SPRING_CONFIG);
			logger.info("Spring configuration for FSFL completed");
		}
	}

	private synchronized void initializeScimsOnlyDependencies() {
		if (getApplicationContextScimsOnly() == null) {
			logger.info("Spring configuration file:" + SCIMS_SPRING_CONFIGURATIONS);
			this.applicationContextScimsOnly = new ClassPathXmlApplicationContext(SCIMS_SPRING_CONFIGURATIONS);
			logger.info("Spring configuration for SCIMS completed");
		}
	}

	private synchronized void initializeCMBSDependencies() {
		if (applicationContextCmbs == null) {
			logger.info("Spring configuration for CBMS file:" + CMBS_SPRING_CONFIG);
			this.applicationContextCmbs = new ClassPathXmlApplicationContext(CMBS_SPRING_CONFIG);
			this.cmbsConnector = (ContentManagementBusinessServiceConnector) applicationContextCmbs
					.getBean("cmbsConnector");
			logger.info("Spring configuration for CMBS completed");
		}
	}

	private synchronized void initializeFBPDependencies() {
		if (getApplicationContextFBP() == null) {
			logger.info("Spring configuration for FBP file:" + FBP_SPRING_CONFIG);
			this.applicationContextFBP = new ClassPathXmlApplicationContext(FBP_SPRING_CONFIG);
			this.fbpProxyBusinessService = (IFBPProxyBS) getApplicationContextFBP().getBean("fbpProxyBS");
			this.farmBusinessPlanService = (gov.usda.fsa.fcao.flp.fbp.FarmBusinessPlanService) getApplicationContextFBP()
					.getBean("fbpBusinessService");
			if (logger.isDebugEnabled()) {
				logger.debug("FBPProxy object: " + fbpProxyBusinessService);
				logger.debug("FarmBusinessPlanService object: " + farmBusinessPlanService);
			}
			logger.info("Spring configuration for FBP completed");
		}
	}

	public static boolean isLAZYLOADING() {
		return LAZYLOADING;
	}

	public static void setLAZYLOADING(boolean lAZYLOADING) {
		if (INSTANCE == null) {
			LAZYLOADING = lAZYLOADING;
		}
	}

	public ApplicationContext getApplicationContextScimsOnly() {
		return applicationContextScimsOnly;
	}

	public void setApplicationContextScimsOnly(ApplicationContext applicationContextScimsOnly) {
		this.applicationContextScimsOnly = applicationContextScimsOnly;
	}

	public ApplicationContext getApplicationContextFBP() {
		return applicationContextFBP;
	}

}
