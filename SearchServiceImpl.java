package gov.usda.fsa.fcao.flp.ola.core.fsa.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import gov.usda.fsa.fcao.flp.flpids.common.utilities.DateUtil;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLACoreUser;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLACounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLAState;
import gov.usda.fsa.fcao.flp.ola.core.config.OLACoreServiceConfig;
import gov.usda.fsa.fcao.flp.ola.core.entity.Application;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerFBPDataSubmissionStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerWithOpeationProfile;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationHistory;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationLoanPurposePlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerCashFlowPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.CreditReportFee;
import gov.usda.fsa.fcao.flp.ola.core.entity.DomainValue;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswer;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerWithDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.OperationProfileCategory;
import gov.usda.fsa.fcao.flp.ola.core.entity.OperationProfileSubCategory;
import gov.usda.fsa.fcao.flp.ola.core.entity.SearchApplication;
import gov.usda.fsa.fcao.flp.ola.core.entity.SupportingDocument;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.CreditElseWhereQuestionType;
import gov.usda.fsa.fcao.flp.ola.core.enums.DomainCategoryTypeCode;
import gov.usda.fsa.fcao.flp.ola.core.enums.ExperienceQuestionType;
import gov.usda.fsa.fcao.flp.ola.core.enums.FBPDataSubmissionStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.OperationProfileCategoryType;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.ReAssignedLocationResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.SearchDetailResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.SearchLoanDetailResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.SearchOperationProfileDetailResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.SearchResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.model.StatusChangeResponse;
import gov.usda.fsa.fcao.flp.ola.core.fsa.service.ISearchService;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationHistoryContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ReassignLocationContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.SearchContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.StatusChangeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.WorkListContract;
import gov.usda.fsa.fcao.flp.ola.core.service.impl.OlaRepositoryService;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.PersonServiceClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Office;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Person;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.SCIMSCustomer;

@Component
public class SearchServiceImpl extends OlaRepositoryService implements ISearchService {

	private static final Logger LOGGER = LogManager.getLogger(SearchServiceImpl.class);
	
	public static final int SIZE_OF_APPLICATION_BORROWER_SET_WITH_PR_AND_CB = 2;

	public static final String FLP = "FLP";
	public static final String LOAN_OFFICER_ASSISTANCE_REQUESTED = "A";
	public static final String NA = "NA";
	protected static final String LOAN_PURPOSE_DOMAIN_CODE = "LPT";
	protected static final String MS = " milliseconds";
	
	protected static final String ASCENDING = "asc";
	protected static final String DESCENDING = "desc";
	
	@Autowired
	private JavaMailSender javaMailSender;
	 
	@Autowired
	private PersonServiceClient personServiceClient;
	
	@Value("${emailNotificationGroup}")
	private String emailNotificationGroup;
	
	@Value("${useTestEmailAddress}")
	private boolean useTestEmailAddress;

	@Value("${flpOfficialEmailNotificationEnabled}")
	private boolean flpOfficialEmailNotificationEnabled;
	
	@Value("${flpOfficialEmailNotificationBccEnabled}")
	private boolean flpOfficialEmailNotificationBccEnabled;
	
	@Value("${coreApiEnvironment}")
	private String environment;
	
	public SearchServiceImpl() {}
	
	/***
	 * Server Side Pagination Support - Total applications Count
	 */
	@Override
	public Integer getApplicationsTotalCount(OlaAgencyToken agencyToken, WorkListContract contract) {
		
		if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0 &&
			contract.getStateOfficeFlpCodes()!=null && contract.getSvcOfficeFlpCodes()==null) // For a specific customer with SO role
		{
			return searchRepository.getApplicationsTotalCountForCustomerAndState(contract.getApplicationStatusCodes(), 
					contract.getCoreCustomerIdentifier(), contract.getStateOfficeFlpCodes()); 
		}
		else if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0 &&
			contract.getSvcOfficeFlpCodes()!=null && contract.getStateOfficeFlpCodes()==null) // For a specific customer with SC role
		{
			return searchRepository.getApplicationsTotalCountForCustomerAndSvc(contract.getApplicationStatusCodes(), 
					contract.getCoreCustomerIdentifier(), contract.getSvcOfficeFlpCodes()); 
		}
		if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0) // For a specific customer (National)
		{
			return searchRepository.getApplicationsTotalCountForCustomer(contract.getApplicationStatusCodes(), contract.getCoreCustomerIdentifier()); 
		}
		else if(contract.getSvcOfficeFlpCodes()==null && contract.getStateOfficeFlpCodes()==null) //National
		{
			return searchRepository.getApplicationsTotalCount(contract.getApplicationStatusCodes()); 
		}
		else if(contract.getStateOfficeFlpCodes()!=null)  // State
		{
			return searchRepository.getApplicationsTotalCountForState(contract.getApplicationStatusCodes(), contract.getStateOfficeFlpCodes());
		}
		else  // Service Center
		{
			return searchRepository.getApplicationsTotalCountForSvc(contract.getApplicationStatusCodes(), contract.getSvcOfficeFlpCodes());
		}
	}
	
	/***
	 * Server Side Pagination Support - Data Retrieval
	 */
	@Override
	public Set<SearchResponse> search(OlaAgencyToken agencyToken, WorkListContract contract) {
		
		Integer offset = (contract.getPageNumber() - 1) * contract.getPageSize();
		
		Set<SearchApplication> applications = null;

		if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0 &&
				contract.getStateOfficeFlpCodes()!=null && contract.getSvcOfficeFlpCodes()==null) // For a specific customer with SO role
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsForCustomerAndStateOrderByAsc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), 
						contract.getApplicationStatusCodes(), contract.getStateOfficeFlpCodes()); 
			}
			else
			{
				applications = searchRepository.searchApplicationsForCustomerAndStateOrderByDesc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), 
						contract.getApplicationStatusCodes(), contract.getStateOfficeFlpCodes()); 
			}
		}
		else if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0 &&
				contract.getSvcOfficeFlpCodes()!=null && contract.getStateOfficeFlpCodes()==null) // For a specific customer with SC role
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsForCustomerAndSvcOrderByAsc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), 
						contract.getApplicationStatusCodes(), contract.getSvcOfficeFlpCodes()); 
			}
			else
			{
				applications = searchRepository.searchApplicationsForCustomerAndSvcOrderByDesc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), 
						contract.getApplicationStatusCodes(), contract.getSvcOfficeFlpCodes()); 
			}
		}
		else if(contract.getCoreCustomerIdentifier()!=null && contract.getCoreCustomerIdentifier() > 0) // For a specific customer (National)
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsForCustomerOrderByAsc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), contract.getApplicationStatusCodes()); 
			}
			else
			{
				applications = searchRepository.searchApplicationsForCustomerOrderByDesc(offset, 
						contract.getPageSize(), contract.getCoreCustomerIdentifier(), contract.getApplicationStatusCodes()); 
			}
		}
		else if(contract.getSvcOfficeFlpCodes()==null && contract.getStateOfficeFlpCodes()==null) //National
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsOrderByAsc(offset, contract.getPageSize(), contract.getApplicationStatusCodes()); 
			}
			else
			{
				applications = searchRepository.searchApplicationsOrderByDesc(offset, contract.getPageSize(), contract.getApplicationStatusCodes()); 
			}
		}
		else if(contract.getStateOfficeFlpCodes()!=null)  // State
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsForStateOrderByAsc(offset, contract.getPageSize(), 
						contract.getApplicationStatusCodes(), contract.getStateOfficeFlpCodes());
			}
			else
			{
				applications = searchRepository.searchApplicationsForStateOrderByDesc(offset, contract.getPageSize(), 
						contract.getApplicationStatusCodes(), contract.getStateOfficeFlpCodes());
			}
		}
		else  // Service Center
		{
			if(ASCENDING.equals(contract.getSortOrder()))
			{
				applications = searchRepository.searchApplicationsForSvcOrderByAsc(offset, contract.getPageSize(), 
						contract.getApplicationStatusCodes(), contract.getSvcOfficeFlpCodes());
			}
			else
			{
				applications = searchRepository.searchApplicationsForSvcOrderByDesc(offset, contract.getPageSize(), 
						contract.getApplicationStatusCodes(), contract.getSvcOfficeFlpCodes());
			}
		}
		
		Set<SearchResponse> searchResponseSet = new LinkedHashSet<>();
		
		if(applications==null || applications.isEmpty()) return searchResponseSet; 
		
		populateSearchResponseSet(applications, searchResponseSet);
		
		for(String applicationStatusCode: contract.getApplicationStatusCodes())
		{
			populateCorrectSubmissionDate(applicationStatusCode, searchResponseSet);
		}
	
		return searchResponseSet;
	}


	/**
	 * @param applications
	 * @param searchResponseSet
	 */
	private void populateSearchResponseSet(Set<SearchApplication> applications, Set<SearchResponse> searchResponseSet) {
		
		for (SearchApplication application : applications) {
			
			SearchResponse searchResponse = SearchResponse.builder().applicationIdentifier(application.getApplicationIdentifier()).
			coreCustomerIdentifier(Integer.valueOf(application.getApplicationBorrowerAllChildsSet().iterator().next().getCoreCustomerIdentifier())).
			borrowerApplicationStatusCode(application.getBorrowerApplicationStatusSet().iterator().next().getBorrowerApplicationStatusCode()).
			submissionDate(DateUtil.dateToString(application.getBorrowerApplicationStatusSet().iterator().next().getCreationDate())).
			assignedServiceCenter(application.getAssignedOfficeFlpCode()).countyName(application.getStateLocationAreaFlpCode()).build();
			
			searchResponseSet.add(searchResponse);
			
		}
	}

    /**
     *  Retrieve and populate/override with correct submission date if status code other than 'SUB' (i.e. RIP, RVD)
     *  - STA - won't have submission date since it's not submitted yet. 
     *  - DEL - won't have submission date since it's deleted before submission
     *  
	 * @param applicationStatusCode
	 * @param searchResponseSet
	 */
	private void populateCorrectSubmissionDate(String applicationStatusCode, Set<SearchResponse> searchResponseSet) {
		if(!ApplicationStatus.SUBMITTED.getCode().equals(applicationStatusCode) && 
			!ApplicationStatus.STARTED.getCode().equals(applicationStatusCode)  && 
			!ApplicationStatus.DELETED.getCode().equals(applicationStatusCode))
		{   
			List<Integer> applicationIdentifiersList = searchResponseSet.stream().map(searchResponse -> 
			                    searchResponse.getApplicationIdentifier()).collect(Collectors.toList());
			
			List<BorrowerApplicationStatus> borrowerApplicationStatusList = borrowerApplicationStatusRepository
					.findBorrowerApplicationStatus(applicationIdentifiersList, ApplicationStatus.SUBMITTED.getCode());
			
            for(BorrowerApplicationStatus status: borrowerApplicationStatusList)
            {
            	SearchResponse response = searchResponseSet.stream().filter(searchResponse-> 
            	   status.getApplicationIdentifier().equals(searchResponse.getApplicationIdentifier())).findFirst().orElse(null);
				if(response != null) {
					response.setSubmissionDate(DateUtil.dateToString(status.getCreationDate()));
            	}
            }
		}
	}
	
	@Override
	public Set<SearchResponse> search(OlaAgencyToken olaToken, SearchContract contract) {

		searchContractValidator.validate(contract);

		Set<SearchResponse> searchResponseSet = new HashSet<>();
		List<Integer> coreCustomerIdentifiers;

		String borrowerApplicationStatusCode = contract.getBorrowerApplicationStatusCode();

		Set<SearchApplication> applications = getSearchApplications(contract);
		
		if (!isEmpty(applications)) {

			coreCustomerIdentifiers = OlaServiceUtil.filterAllCoreCustomerIdentifiers(applications);

			List<SCIMSCustomer> scimsCustomers = scimsCustomerService.getCustomers(agencyToken,
					coreCustomerIdentifiers);

			List<SCIMSCustomer> searchedCustomers = OlaServiceUtil.findAllMatchingBorrowers(scimsCustomers, contract);
			
			Map<Integer, String> applicationServiceCenterInfoMap = getAssignedServiceCenterOfficeInfoMap(applications);

			if (!isEmpty(searchedCustomers)) {

				for (SCIMSCustomer scimsCustomerBO : searchedCustomers) {

					String coreCustomerIdentifier = scimsCustomerBO.getCustomerID();
					

					for (SearchApplication application : applications) {

						Integer coreIdentifier = OlaServiceUtil.filterCoreCustomerIdentifier(application);

						if (coreCustomerIdentifier.equalsIgnoreCase(String.valueOf(coreIdentifier))) {

							SearchResponse searchResponse = buildSearchResponse(scimsCustomerBO, application,
									borrowerApplicationStatusCode, applicationServiceCenterInfoMap);

							searchResponseSet.add(searchResponse);
						}

					}

				}

			}

		}

		return searchResponseSet;
	}

	private SearchResponse buildSearchResponse(SCIMSCustomer scimsCustomerBO, SearchApplication application,
			String borrowerApplicationStatusCode, Map<Integer, String> applicationServiceCenterInfoMap) {

		String coreCustomerIdentifier = scimsCustomerBO.getCustomerID();

		String fullName = OlaServiceUtil.getFullName(scimsCustomerBO.getFirstName(), scimsCustomerBO.getLastName());

		String taxID = OlaServiceUtil.getTaxID(scimsCustomerBO.getTaxID(), scimsCustomerBO.getTaxIDType().getCode());

		String statelocationAreaCode = application.getStateLocationAreaFlpCode();

		//String assignedServiceCenter = getServiceCenterOfficeInfo(application.getOfficeFlpCode(), false);

		//String assignedOfficeFLPCode = application.getAssignedOfficeFlpCode();

		//if (!StringUtil.isEmptyString(assignedOfficeFLPCode)
		//		&& !application.getOfficeFlpCode().equalsIgnoreCase(application.getAssignedOfficeFlpCode())) {

		//	assignedServiceCenter = getServiceCenterOfficeInfo(application.getAssignedOfficeFlpCode(), false);

		//}
		String assignedServiceCenter = applicationServiceCenterInfoMap.get(application.getApplicationIdentifier());
		
		// TODO: Fix this - perform this collectively (avoid doing this one at a time)
		String countyName = getBorrowerCountyName(statelocationAreaCode);

		String borrowerApplicationStatusValue = OlaServiceUtil.getBorrowerApplicationStatusValue(
				application.getBorrowerApplicationStatusSet(), borrowerApplicationStatusCode);

		String submissionDate = OlaServiceUtil.getDateByStatusCode(application.getBorrowerApplicationStatusSet(),
				ApplicationStatus.SUBMITTED.getCode());

		return SearchResponse.builder().applicationIdentifier(application.getApplicationIdentifier())
				.coreCustomerIdentifier(Integer.valueOf(coreCustomerIdentifier)).fullName(fullName).taxIdentifier(taxID)
				.borrowerApplicationStatusCode(borrowerApplicationStatusValue).submissionDate(submissionDate)
				.assignedServiceCenter(assignedServiceCenter).countyName(countyName).build();

	}
	
/*	
	private Map<Integer, String> getAssignedServiceCenterOfficeInfoMapOld(Set<SearchApplication> applications) {

		List<String> officeFLPCodes = OlaServiceUtil.filterAllOfficeFLPCodes(applications);  
		LOGGER.info("officeFLPCodes: {}",officeFLPCodes);
		
		List<gov.usda.fsa.citso.cbs.dto.Office> flpServiceCenters = mrtProxyBS.retrieveOfficesByFlpCodes(officeFLPCodes);
		Map<String, String> serviceCenterOfficeInfoMap = new HashMap<>();
		for(gov.usda.fsa.citso.cbs.dto.Office office: flpServiceCenters)
		{
			serviceCenterOfficeInfoMap.put(office.getOfficeCode(), 
					office.getLocStateAbbrev()+"/"+office.getLocCityName());
		}
		LOGGER.info("serviceCenterOfficeInfoMap: {}",serviceCenterOfficeInfoMap);
		
		Map<Integer, String> applicationServiceCenterInfoMap = new HashMap<>();
		
		for(SearchApplication application: applications)
		{
			String assignedServiceCenerInfo = "";
			if(!StringUtil.isEmptyString(application.getAssignedOfficeFlpCode()))
			{
				assignedServiceCenerInfo = serviceCenterOfficeInfoMap.get(application.getAssignedOfficeFlpCode());
			}
			else
			{
				assignedServiceCenerInfo = serviceCenterOfficeInfoMap.get(application.getOfficeFlpCode());
			}
			
			applicationServiceCenterInfoMap.put(application.getApplicationIdentifier(), assignedServiceCenerInfo);
		}
		
		LOGGER.debug("applicationServiceCenterInfoMap: {}",applicationServiceCenterInfoMap);
		
		return applicationServiceCenterInfoMap;
		
	}
*/	

	private Map<Integer, String> getAssignedServiceCenterOfficeInfoMap(Set<SearchApplication> applications) {

		Map<Integer, String> applicationServiceCenterInfoMap = new HashMap<>();
		Map<String, Office> officeFLPCodeOfficeMap = stateCountyService.getOfficeFLPCodeOfficeMap();
		
		for(SearchApplication application: applications)
		{
			String officeFLPCode = !StringUtil.isEmptyString(application.getAssignedOfficeFlpCode())?
							application.getAssignedOfficeFlpCode(): application.getOfficeFlpCode();

			Office office = officeFLPCodeOfficeMap.get(officeFLPCode); 
			
			if(office!=null)
			{
				applicationServiceCenterInfoMap.put(application.getApplicationIdentifier(), 
						office.getLocationStateAbbreviation()+"/"+office.getLocationCityName());
			}
			else
			{
				LOGGER.info("Office info not found in cache for officeFLPCode: {}. Making WS call to CBS.",officeFLPCode);
				applicationServiceCenterInfoMap.put(application.getApplicationIdentifier(), 
						stateCountyService.getServiceCenterOfficeInfo(officeFLPCode, false));
			}
		}
		
		LOGGER.debug("applicationServiceCenterInfoMap: {}",applicationServiceCenterInfoMap);
		
		return applicationServiceCenterInfoMap;
		
	}

	private String getBorrowerCountyName(String statelocationAreaCode) {

		String countyName = "";
		if (!StringUtil.isEmptyString(statelocationAreaCode) && statelocationAreaCode.length() == 5) {

			String stateLocationCode = statelocationAreaCode.substring(0, 2);
			String countyLocationCode = statelocationAreaCode.substring(2);

			OLACounty county = findOLACounty(stateLocationCode, countyLocationCode);

			if (county != null) {

				countyName = county.getCountyName();
			}
		}
		return countyName;
	}

	private Set<SearchApplication> getSearchApplications(SearchContract contract) {

		String serviceCenterStateCode = contract.getServiceCenterStateCode();

		String borrowerApplicationStatusCode = contract.getBorrowerApplicationStatusCode();
		String officeFLPCode = contract.getOfficeFlpCode();

		List<String> officeCodesSearch = new ArrayList<>();
		if (!StringUtil.isEmptyString(officeFLPCode)) {
			officeCodesSearch.add(officeFLPCode);
		}

		Set<SearchApplication> applications;
		OLACoreUser coreUser = authorizationService.findOLACoreUser(contract.getEAuthIdentifier());

		List<String> roles = coreUser.getEasRoleList();

		List<String> officeIdentifiers = coreUser.getOfficeIdList();

		Optional<String> stateLevelMatchOptional = officeIdentifiers.stream()
				.filter(o -> (o.substring(0, 2).equalsIgnoreCase(serviceCenterStateCode)
						&& o.substring(3).equalsIgnoreCase("00")))
				.findAny();

		boolean isServiceCenterRole = OlaServiceUtil.isServiceCenterRole(roles);
		
		LOGGER.info("First log for debugging getSearchApplications - isServiceCenterRole: {}, stateLevelMatchOptional.isPresent(): {}, officeCodesSearch: {}", isServiceCenterRole, stateLevelMatchOptional.isPresent(), officeCodesSearch);

		if (isServiceCenterRole && !stateLevelMatchOptional.isPresent() && officeCodesSearch.isEmpty()) {

			List<String> officeCodesAssociatedWithState = OlaServiceUtil
					.getServiceCenterCounties(serviceCenterStateCode, coreUser.getOfficeIdList());

			long startTime = System.currentTimeMillis();
			
			LOGGER.info("Second log for debugging getSearchApplications - serviceCenterStateCode: {}, coreUser.getOfficeIdList(): {}, officeCodesAssociatedWithState: {}, borrowerApplicationStatusCode: {}", serviceCenterStateCode, coreUser.getOfficeIdList(), officeCodesAssociatedWithState, borrowerApplicationStatusCode);
			
			applications = searchRepository.searchApplicationsForSeviceCenters(officeCodesAssociatedWithState,
					borrowerApplicationStatusCode);
			
			long endTime = System.currentTimeMillis();
			LOGGER.info("searchApplications - SQL Query execution time took {} {}", (endTime - startTime), MS);

		} else {
			long startTime = System.currentTimeMillis();
			
			if (!officeCodesSearch.isEmpty()) {
				
				LOGGER.info("Third log for debugging getSearchApplications - officeCodesSearch: {}, borrowerApplicationStatusCode: {}", officeCodesSearch, borrowerApplicationStatusCode);

				applications = searchRepository.searchApplicationsForSeviceCenters(officeCodesSearch,
						borrowerApplicationStatusCode);
			} else {
				applications = searchRepository.searchApplications(serviceCenterStateCode,
						borrowerApplicationStatusCode);
			}
			
			long endTime = System.currentTimeMillis();
			LOGGER.info("searchApplications - SQL Query execution time took {} {}", (endTime - startTime), MS);
			
		}

		return filterApplications(applications, borrowerApplicationStatusCode);
	}

	private Set<SearchApplication> filterApplications(Set<SearchApplication> applications,
			String borrowerApplicationStatusCode) {

		Set<SearchApplication> filteredApplications = new HashSet<>();

		if (StringUtil.isEmptyString(borrowerApplicationStatusCode)) {

			return applications;
		}

		if (!isEmpty(applications)) {

			for (SearchApplication application : applications) {

//				Set<BorrowerApplicationStatus> activeBorrowerApplicationStatusSet = application
//						.getBorrowerApplicationStatusSet().stream()
//						.filter(s -> s.getDataStatusCode().equalsIgnoreCase(ACTIVE)).collect(Collectors.toSet());

				BorrowerApplicationStatus submittedApplicationStatus = OlaServiceUtil
						.getBorrowerApplicationStatusByStatusCode(application.getBorrowerApplicationStatusSet(),
								ApplicationStatus.SUBMITTED.getCode());

				BorrowerApplicationStatus reviewedApplicationStatus = OlaServiceUtil
						.getBorrowerApplicationStatusByStatusCode(application.getBorrowerApplicationStatusSet(),
								ApplicationStatus.REVIEWED.getCode());

				BorrowerApplicationStatus reviewInProgressApplicationStatus = OlaServiceUtil
						.getBorrowerApplicationStatusByStatusCode(application.getBorrowerApplicationStatusSet(),
								ApplicationStatus.REVIEW_IN_PROGRESS.getCode());

				if ((borrowerApplicationStatusCode.equalsIgnoreCase(ApplicationStatus.SUBMITTED.getCode())
						&& reviewedApplicationStatus == null && reviewInProgressApplicationStatus == null
						&& submittedApplicationStatus != null)

						|| (borrowerApplicationStatusCode.equalsIgnoreCase(ApplicationStatus.REVIEWED.getCode())
								&& reviewedApplicationStatus != null)

						|| (borrowerApplicationStatusCode
								.equalsIgnoreCase(ApplicationStatus.REVIEW_IN_PROGRESS.getCode())
								&& reviewedApplicationStatus == null && reviewInProgressApplicationStatus != null))

				{

					filteredApplications.add(application);

				}

			}

		}

		return filteredApplications;
	}

	@Override
	public SearchApplication searchByApplication(Integer applicationIdentifier) {
		
		return searchRepository.findByIdentifier(applicationIdentifier).orElse(null);
		
	}
	
	@Override
	public SearchDetailResponse searchDetail(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) {

		SearchApplication application = searchByApplication(applicationIdentifier) ;

		SearchDetailResponse response = null;

		if (application != null) {

			Integer coreIdentifier = OlaServiceUtil.filterCoreCustomerIdentifier(application);

			if (coreCustomerIdentifier.equals(coreIdentifier)) {

				SCIMSCustomer scimsCustomer = scimsCustomerService.getCustomer(agencyToken, coreCustomerIdentifier);
				
				LOGGER.info("coreCustomerIdentifier: {}, coreIdentifier: {}", coreCustomerIdentifier, coreIdentifier);

				Integer applicationBorrowerIdentifier = OlaServiceUtil.filterApplicationBorrowerIdentifier(application,
						coreCustomerIdentifier);

				if (scimsCustomer != null) {

					String assignedServiceCenter = "NA";
					String firstName = scimsCustomer.getFirstName();
					String lastName = scimsCustomer.getLastName();
					String taxIdentifier = OlaServiceUtil.getTaxID(scimsCustomer.getTaxID(),
							scimsCustomer.getTaxIDType().getCode());
					String borrowerApplicationStatusValue = OlaServiceUtil
							.getBorrowerApplicationStatusValue(application.getBorrowerApplicationStatusSet(), null);
					String submissionDate = OlaServiceUtil.getDateByStatusCode(
							application.getBorrowerApplicationStatusSet(), ApplicationStatus.SUBMITTED.getCode());

					String operationHQName = getOperationHQName(application.getStateLocationAreaFlpCode());

					String officeFLPCode = application.getOfficeFlpCode();

					String assignedOfficeFLPCode = application.getAssignedOfficeFlpCode();

					String defaultServiceCenter = stateCountyService.getServiceCenterOfficeInfo(officeFLPCode, true); 

					if ((StringUtil.isEmptyString(assignedOfficeFLPCode))
							|| (!StringUtil.isEmptyString(assignedOfficeFLPCode)
									&& assignedOfficeFLPCode.equalsIgnoreCase(officeFLPCode))) {

						assignedServiceCenter = defaultServiceCenter;

					} else if (!StringUtil.isEmptyString(assignedOfficeFLPCode)) {

						assignedServiceCenter = stateCountyService.getServiceCenterOfficeInfo(assignedOfficeFLPCode, true); 
					}

					Set<SupportingDocument> supportingDocumentSet = findAllSupportingDocuments(application.getApplicationIdentifier(),
							applicationBorrowerIdentifier);

				    OperationProfileCategory opCategory = applicationService.findPrimaryOperationPofileCategory(
				    		                        applicationBorrowerIdentifier);
				    
					OperationProfileSubCategory opSubCategory =
							   applicationService.findOperationPofileSubCategory(opCategory.getOperationProfileCategoryIdentifier());
				    
					DomainValue domainValue = domainService.findDomainValueByCategoryAndDomainCode(
							opCategory.getOperationCategoryCode(), DomainCategoryTypeCode.OPERATION_PRIMARY_ENTERPRISE.getCode());
				    
					String primaryEnterPriseName = domainValue.getDomainValueName();
					LOGGER.info("primaryEnterPriseName="+primaryEnterPriseName);  

					LOGGER.info("opSubCatCode='"+opSubCategory.getOperationSubcategoryCode()+"' "
				    		+ "opCatCode='"+opCategory.getOperationCategoryCode()+"'");  
					
					domainValue = domainService.findDomainValueByCategoryAndDomainCode(
							opSubCategory.getOperationSubcategoryCode().trim(), opCategory.getOperationCategoryCode().trim());
					
					String typeOfOperation = domainValue.getDomainValueName();
					LOGGER.info("typeOfOperation="+typeOfOperation);  
				    
					LOGGER.info("subcategoryItemCode='"+opSubCategory.getOperationProfileSubcategoryItemCode()+"'");
					String type = null;
					String typeOtherCategoryDescription = null;
					String typeOtherDescription = null;
					
					String domainCode = OperationProfileCategoryType.CROP_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
							DomainCategoryTypeCode.OPERATION_CROP_TYPE.getCode():
							OperationProfileCategoryType.LIVESTOCK_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
							DomainCategoryTypeCode.OPERATION_LIVESTOCK_TYPE.getCode():
							OperationProfileCategoryType.LIVESTOCKPROD_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
							DomainCategoryTypeCode.OPERATION_LIVESTOCK_PRODUCT_TYPE.getCode():null;
					
				    if(DomainCategoryTypeCode.OTHER.getCode().equalsIgnoreCase(opSubCategory.getOperationSubcategoryCode().trim())) 
				    {
				    	typeOtherCategoryDescription = opSubCategory.getOperationProfileSubcategoryOtherDescription();
				    	LOGGER.info("typeOtherCategoryDescription='"+typeOtherCategoryDescription+"'");
				    }
				    else if(domainCode!=null) // this will be null for CSA
					{
						domainValue = domainService.findDomainValueByCategoryAndDomainCode(
							opSubCategory.getOperationProfileSubcategoryItemCode().trim(), domainCode);
						type = domainValue.getDomainValueName();
						typeOtherDescription = opSubCategory.getOperationProfileSubcategoryOtherItemDescription();
						LOGGER.info("type="+type);  
						LOGGER.info("typeOtherDescription="+typeOtherDescription);  
					}
				    
				    Set<OlaAnswerWithDocument> olaAnswers = olaAnswerWithDocumentRepository.findAllActiveOlaAnswersByCodes(
							applicationBorrowerIdentifier,ExperienceQuestionType.getTypeCodes());
				    
				    boolean teeAnswerAllNoneFlag = isNoneSelectedForAllTEESections(olaAnswers);
				    
				    Integer coApplicationBorrowerIdentifier = null;
				    
				    Optional<ApplicationBorrowerPlain> coBorrowerOptional = applicationBorrowerPlainRepository
				    		.findActiveApplicationBorrowerByCustomerIdentifier(applicationIdentifier, coreIdentifier);
				    
				    if(coBorrowerOptional.isPresent()) {
				    	ApplicationBorrowerPlain coBorrower = coBorrowerOptional.get();
				    	coApplicationBorrowerIdentifier = coBorrower.getApplicationBorrowerIdentifier();
				    }
				    
				    Set<OlaAnswerWithDocument> coOlaAnswers = olaAnswerWithDocumentRepository.findAllActiveOlaAnswersByCodes(
							coApplicationBorrowerIdentifier,ExperienceQuestionType.getTypeCodes());
					
				    boolean coTeeAnswerAllNoneFlag = isNoneSelectedForAllTEESections(coOlaAnswers);

/*				    
					String primaryEnterPriseName = getPrimaryEnterPrise(applicationBorrowerIdentifier);

					String typeOfOperation = findTypeOfOperationCategory(applicationBorrowerIdentifier);

					String type = findType(applicationBorrowerIdentifier);

					String typeOtherCategoryDescription = findOtherTypeCategoryDescription(
							applicationBorrowerIdentifier);

					String typeOtherDescription = findOtherTypeDescription(applicationBorrowerIdentifier);
*/					
					
					boolean isOnlinePayment = isOnlinePayment(applicationIdentifier);
					
					FBPDataSubmissionStatus fbpDataSubmissionStatus = null;
					if(ApplicationStatus.REVIEWED.getDescription().equalsIgnoreCase(borrowerApplicationStatusValue))
					{
						ApplicationBorrowerFBPDataSubmissionStatus status = 
								applicationBorrowerFBPDataSubmissionStatusRepository.getFBPDataSubmissionCurrentStatus(
								coreCustomerIdentifier, applicationIdentifier);
						if(status!=null)
						{
							fbpDataSubmissionStatus = FBPDataSubmissionStatus.fromCode(
									status.getFbpDataSubmissionStatusCode()).orElse(null);
						}
					}
					
					boolean loanOfficerAssistanceRequested = loanOfficerAssistanceRequested(applicationBorrowerIdentifier);
					
					List<SearchLoanDetailResponse> searchLoanDetailResponseList = buildSearchLoanDetailResponse(
							application);
					
					SearchOperationProfileDetailResponse searchOperationProfileDetailResponse = buildSearchOperationProfileDetailResponse(
							applicationBorrowerIdentifier);

					response = SearchDetailResponse.builder().coreCustomerIdentifier(coreIdentifier)
							.firstName(firstName).lastName(lastName).taxIdentifier(taxIdentifier)
							.submissionDate(submissionDate)
							.borrowerApplicationStatusCode(borrowerApplicationStatusValue)
							.fbpDataSubmissionStatusCode(fbpDataSubmissionStatus!=null?fbpDataSubmissionStatus.getDescription():null)
							.applicationIdentifier(application.getApplicationIdentifier())
							.operationHeadQuartersName(operationHQName).deafultServiceCenter(defaultServiceCenter)
							.assignedServiceCenter(assignedServiceCenter).supportingDocumentSet(supportingDocumentSet)
							.primaryEnterprise(primaryEnterPriseName).typeOfOperation(typeOfOperation).type(type)
							.typeOfOperationDescription(typeOtherCategoryDescription)
							.typeDescription(typeOtherDescription)
					        .onlinePayment(isOnlinePayment)
					        .teeAnswerAllNoneSelected(teeAnswerAllNoneFlag)
					        .coTeeAnswerAllNoneSelected(coTeeAnswerAllNoneFlag)
					        .loanOfficerAssistanceRequested(loanOfficerAssistanceRequested)
					        .searchLoanDetailResponseList(searchLoanDetailResponseList)
					        .applicationBorrowerIdentifier(applicationBorrowerIdentifier)
							.creditElseWhereAnswer(retrieveAnswer(applicationBorrowerIdentifier,
									CreditElseWhereQuestionType.DENIAL_Q.getCode()))
							.creditElseWhereLetterAnswer(retrieveAnswer(applicationBorrowerIdentifier,
									CreditElseWhereQuestionType.DENIAL_LETTER_Q.getCode()))
							.searchOperationProfileDetailResponse(searchOperationProfileDetailResponse)
					        .build();

				} else {

					LOGGER.error("SCIMS customer not identified for the core customer identifier: {} ",
							coreCustomerIdentifier);
				}

			}
		} else {

			LOGGER.error("Application not found for the application identifier: {} ", applicationIdentifier);
		}

		return response;
	}
	
	/***
	 * This just returns OLA data (i.e. This does not populate SCIMS, Location-Service data)
	 */
	@Override
	public SearchDetailResponse getApplicationDetails(Integer applicationIdentifier) {
		
		SearchApplication application = searchByApplication(applicationIdentifier) ;

		SearchDetailResponse response = null;

		if (application != null) {
			
			Integer applicationBorrowerIdentifier = application
				    .getApplicationBorrowerAllChildsSet().stream()
				    .filter(borrower -> "PR".equalsIgnoreCase(borrower.getLoanRelationshipTypeCode()))
				    .findFirst()
				    .orElseThrow(() -> new RuntimeException("No borrower with loan relationship type 'PR' found"))
				    .getApplicationBorrowerIdentifier();
			
			LOGGER.info("getApplicationDetails applicationBorrowerIdentifier: {}", applicationBorrowerIdentifier);  
			
			String operationHQName = application.getStateLocationAreaFlpCode();
			String defaultServiceCenter = application.getOfficeFlpCode();
			String assignedOfficeFLPCode = application.getAssignedOfficeFlpCode();
			
			String submissionDate = OlaServiceUtil.getDateByStatusCode(
					application.getBorrowerApplicationStatusSet(), ApplicationStatus.SUBMITTED.getCode());
			
			Set<SupportingDocument> supportingDocumentSet = findAllSupportingDocuments(application.getApplicationIdentifier(),
					applicationBorrowerIdentifier);

		    OperationProfileCategory opCategory = applicationService.findPrimaryOperationPofileCategory(
		    		                        applicationBorrowerIdentifier);
		    
			OperationProfileSubCategory opSubCategory =
					   applicationService.findOperationPofileSubCategory(opCategory.getOperationProfileCategoryIdentifier());
		    
			DomainValue domainValue = domainService.findDomainValueByCategoryAndDomainCode(
					opCategory.getOperationCategoryCode(), DomainCategoryTypeCode.OPERATION_PRIMARY_ENTERPRISE.getCode());
		    
			String primaryEnterPriseName = domainValue.getDomainValueName();
			LOGGER.info("primaryEnterPriseName="+primaryEnterPriseName);  

			LOGGER.info("opSubCatCode='"+opSubCategory.getOperationSubcategoryCode()+"' "
		    		+ "opCatCode='"+opCategory.getOperationCategoryCode()+"'");  
			
			domainValue = domainService.findDomainValueByCategoryAndDomainCode(
					opSubCategory.getOperationSubcategoryCode().trim(), opCategory.getOperationCategoryCode().trim());
			
			String typeOfOperation = domainValue.getDomainValueName();
			LOGGER.info("typeOfOperation="+typeOfOperation);  
		    
			LOGGER.info("subcategoryItemCode='"+opSubCategory.getOperationProfileSubcategoryItemCode()+"'");
			String type = null;
			String typeOtherCategoryDescription = null;
			String typeOtherDescription = null;
			
			String domainCode = OperationProfileCategoryType.CROP_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
					DomainCategoryTypeCode.OPERATION_CROP_TYPE.getCode():
					OperationProfileCategoryType.LIVESTOCK_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
					DomainCategoryTypeCode.OPERATION_LIVESTOCK_TYPE.getCode():
					OperationProfileCategoryType.LIVESTOCKPROD_CATEGORY.getCode().equalsIgnoreCase(opCategory.getOperationCategoryCode())?
					DomainCategoryTypeCode.OPERATION_LIVESTOCK_PRODUCT_TYPE.getCode():null;
			
		    if(DomainCategoryTypeCode.OTHER.getCode().equalsIgnoreCase(opSubCategory.getOperationSubcategoryCode().trim())) 
		    {
		    	typeOtherCategoryDescription = opSubCategory.getOperationProfileSubcategoryOtherDescription();
		    	LOGGER.info("typeOtherCategoryDescription='"+typeOtherCategoryDescription+"'");
		    }
		    else if(domainCode!=null) // this will be null for CSA
			{
				domainValue = domainService.findDomainValueByCategoryAndDomainCode(
					opSubCategory.getOperationProfileSubcategoryItemCode().trim(), domainCode);
				type = domainValue.getDomainValueName();
				typeOtherDescription = opSubCategory.getOperationProfileSubcategoryOtherItemDescription();
				LOGGER.info("type="+type);  
				LOGGER.info("typeOtherDescription="+typeOtherDescription);  
			}
		    
		    Set<OlaAnswerWithDocument> olaAnswers = olaAnswerWithDocumentRepository.findAllActiveOlaAnswersByCodes(
					applicationBorrowerIdentifier,ExperienceQuestionType.getTypeCodes());
		    
		    boolean teeAnswerAllNoneFlag = isNoneSelectedForAllTEESections(olaAnswers);
			
			boolean isOnlinePayment = isOnlinePayment(applicationIdentifier);
			
			String borrowerApplicationStatusValue = "";
			
			ApplicationStatus borrowerApplicationStatus = OlaServiceUtil.
					getCurrentApplicationStatus(application.getBorrowerApplicationStatusSet(), null);
			if (borrowerApplicationStatus != null) {
				borrowerApplicationStatusValue = borrowerApplicationStatus.getCode(); 
			}
			
			FBPDataSubmissionStatus fbpDataSubmissionStatus = null;
			if(ApplicationStatus.REVIEWED.getDescription().equalsIgnoreCase(borrowerApplicationStatusValue))
			{
				ApplicationBorrowerFBPDataSubmissionStatus status = 
					applicationBorrowerFBPDataSubmissionStatusRepository.getFBPDataSubmissionCurrentStatus(
					application.getApplicationBorrowerAllChildsSet().iterator().next().getCoreCustomerIdentifier(), applicationIdentifier);
				if(status!=null)
				{
					fbpDataSubmissionStatus = FBPDataSubmissionStatus.fromCode(
							status.getFbpDataSubmissionStatusCode()).orElse(null);
				}
			}
			
			boolean loanOfficerAssistanceRequested = loanOfficerAssistanceRequested(applicationBorrowerIdentifier);
			
			List<SearchLoanDetailResponse> searchLoanDetailResponseList = buildSearchLoanDetailResponse(
					application);
			
			SearchOperationProfileDetailResponse searchOperationProfileDetailResponse = buildSearchOperationProfileDetailResponse(
					applicationBorrowerIdentifier);
			
			Iterator<ApplicationBorrowerWithOpeationProfile> iter = application.getApplicationBorrowerAllChildsSet().iterator();
			
			Integer ccid = iter.next().getCoreCustomerIdentifier();
			
			// if application has coborrower, populate co-CCID in response
			Integer coCcid = null;
			if("INE".equals(application.getLoanApplicantTypeCode()) &&
					application.getApplicationBorrowerAllChildsSet().size() >= SIZE_OF_APPLICATION_BORROWER_SET_WITH_PR_AND_CB) {
				
				coCcid = iter.next().getCoreCustomerIdentifier();
			}
			
			response = SearchDetailResponse.builder().coreCustomerIdentifier(ccid)
					.coCoreCustomerIdentifier(coCcid)
					.submissionDate(submissionDate)
					.borrowerApplicationStatusCode(borrowerApplicationStatusValue)
					.fbpDataSubmissionStatusCode(fbpDataSubmissionStatus!=null?fbpDataSubmissionStatus.getDescription():null)
					.applicationIdentifier(application.getApplicationIdentifier())
					.operationHeadQuartersName(operationHQName).deafultServiceCenter(defaultServiceCenter)
					.assignedServiceCenter(assignedOfficeFLPCode).supportingDocumentSet(supportingDocumentSet)					
					.primaryEnterprise(primaryEnterPriseName).typeOfOperation(typeOfOperation).type(type)
					.typeOfOperationDescription(typeOtherCategoryDescription)
					.typeDescription(typeOtherDescription)
			        .onlinePayment(isOnlinePayment)
			        .teeAnswerAllNoneSelected(teeAnswerAllNoneFlag)
			        .loanOfficerAssistanceRequested(loanOfficerAssistanceRequested)
			        .searchLoanDetailResponseList(searchLoanDetailResponseList)
			        .applicationBorrowerIdentifier(applicationBorrowerIdentifier)
					.creditElseWhereAnswer(retrieveAnswer(applicationBorrowerIdentifier,
							CreditElseWhereQuestionType.DENIAL_Q.getCode()))
					.creditElseWhereLetterAnswer(retrieveAnswer(applicationBorrowerIdentifier,
							CreditElseWhereQuestionType.DENIAL_LETTER_Q.getCode()))
					.searchOperationProfileDetailResponse(searchOperationProfileDetailResponse)
					.fsa2001StorageAddressIdentifier(application.getApplicationStorageAddress())
					.applicantTypeCode(application.getLoanApplicantTypeCode())
					.maritalStatusCode(application.getApplicationBorrowerAllChildsSet().iterator().next().getMaritalStatusCode())
			        .build();
			
		} else {

			LOGGER.error("Application not found for the application identifier: {} ", applicationIdentifier);
		}
		
		return response;
	}
	
	
	List<SearchLoanDetailResponse> buildSearchLoanDetailResponse(SearchApplication application) {

		List<ApplicationLoanPurposePlain> applicationLoanPurposeList = applicationLoanPurposeRepository
				.findLoanPurposesByApplicationIdentifier(application.getApplicationIdentifier());

		List<SearchLoanDetailResponse> searchLoanDetailResponseList = new ArrayList<>();

		for (ApplicationLoanPurposePlain applicationLoanPurpose : applicationLoanPurposeList) {

			SearchLoanDetailResponse response = SearchLoanDetailResponse.builder()
					.loanPurposeTypeCode(applicationLoanPurpose.getLoanPurposeTypeCode())
					.loanPurposeTypeDescription(
							retrieveDomainValue(applicationLoanPurpose.getLoanPurposeTypeCode().trim(),
									applicationLoanPurpose.getLoanPurposeCategoryCode().trim()))
					.loanPurposeTypeOtherDescription(applicationLoanPurpose.getLoanPurposeTypeOtherDescription())
					.loanRequestAmount(applicationLoanPurpose.getLoanRequestAmount())
					.loanPurposeCategoryCode(applicationLoanPurpose.getLoanPurposeCategoryCode())
					.loanPurposeSubTypeDescription(applicationLoanPurpose.getLoanPurposeSubTypeText())
					.creditorName(applicationLoanPurpose.getCreditorNameText())
					.livestockHeadCount(applicationLoanPurpose.getLivestockHeadCount()).build();

			searchLoanDetailResponseList.add(response);
		}

		return searchLoanDetailResponseList;
	}

	String retrieveDomainValue(String domainValueCode, String domainCode) {

		DomainValue domainValue = domainService.findDomainValueByCategoryAndDomainCode(domainValueCode, domainCode);

		String domainValueName;

		if (domainValue != null) {
			domainValueName = domainValue.getDomainValueName();
		} else {
			domainValueName = "Unknown";
		}

		return domainValueName;
	}
	
	Boolean retrieveAnswer(Integer applicationBorrowerIdentifier, String olaQuestionTypeCode) {

		Boolean result = Boolean.FALSE;

		OlaAnswer answer = questionAnswerPlainRepository.findQuestionAnswerByCode(applicationBorrowerIdentifier,
				olaQuestionTypeCode);

		if (null != answer) {
			if (answer.getQuestionTypeAnswerIndicator().trim().equalsIgnoreCase("Y")) {
				result = Boolean.TRUE;
			}
		}

		return result;
	}	
	
	SearchOperationProfileDetailResponse buildSearchOperationProfileDetailResponse(
			Integer applicationBorrowerIdentifier) {

		SearchOperationProfileDetailResponse searchOperationProfileDetailResponse = SearchOperationProfileDetailResponse
				.builder().build();

		OperationProfileCategory operationProfileCategory = operationProfileCategoryRepository
				.findPrimaryOperationPofileCategory(applicationBorrowerIdentifier);

		if (null != operationProfileCategory) {

			searchOperationProfileDetailResponse
					.setEnterpriseTypeCode(operationProfileCategory.getEnterpriseTypeCode());
			searchOperationProfileDetailResponse
					.setOperationCategoryCode(operationProfileCategory.getOperationCategoryCode());

			OperationProfileSubCategory operationProfileSubCategory = operationProfileSubCategoryRepository
					.findOperationProfileSubCategoryByParent(
							operationProfileCategory.getOperationProfileCategoryIdentifier());

			if (null != operationProfileSubCategory) {

				searchOperationProfileDetailResponse
						.setOperationSubcategoryCode(operationProfileSubCategory.getOperationSubcategoryCode());
				searchOperationProfileDetailResponse.setOperationProfileSubcategoryOtherDescription(
						operationProfileSubCategory.getOperationProfileSubcategoryOtherDescription());
				searchOperationProfileDetailResponse.setOperationProfileSubcategoryItemCode(
						operationProfileSubCategory.getOperationProfileSubcategoryItemCode());
				searchOperationProfileDetailResponse.setOperationProfileSubcategoryOtherItemDescription(
						operationProfileSubCategory.getOperationProfileSubcategoryOtherItemDescription());
				searchOperationProfileDetailResponse.setBuyerName(operationProfileSubCategory.getBuyerName());
				searchOperationProfileDetailResponse.setBreedName(operationProfileSubCategory.getBreedName());
				searchOperationProfileDetailResponse
						.setOrganicIndicator(operationProfileSubCategory.getOrganicIndicator());
			}
		}

		return searchOperationProfileDetailResponse;
	}

/*	
	private String getPrimaryEnterPrise(Integer applicationBorrowerIdentifier) {

		return domainRepository.findPrimaryEnterprise(applicationBorrowerIdentifier);

	}

	private String findTypeOfOperationCategory(Integer applicationBorrowerIdentifier) {

		return domainRepository.findTypeOfOperationCategory(applicationBorrowerIdentifier);

	}

	private String findType(Integer applicationBorrowerIdentifier) {

		return domainRepository.findType(applicationBorrowerIdentifier);

	}

	private String findOtherTypeCategoryDescription(Integer applicationBorrowerIdentifier) {

		return domainRepository.findOtherTypeCategoryDescription(applicationBorrowerIdentifier);

	}

	private String findOtherTypeDescription(Integer applicationBorrowerIdentifier) {

		return domainRepository.findOtherTypeDescription(applicationBorrowerIdentifier);

	}
*/	
	
	private boolean isOnlinePayment(Integer applicationIdentifier) {

		CreditReportFee creditReportFee =  creditReportFeeRepository.findByApplication(applicationIdentifier);
		
		if(creditReportFee!=null && OLACoreServiceConfig.CREDIT_REPORT_FEE_PAY_ONLINE.equalsIgnoreCase(creditReportFee.getCreditReportFeePaymentTypeCode()) &&
			creditReportFee.getCreditReportFeeRemittanceIdentifier()!=null && creditReportFee.getCreditReportFeeRemittanceIdentifier() > 0)
		{
           return true; 			
		}
        return false;
	}

	private boolean loanOfficerAssistanceRequested(Integer applicationBorrowerIdentifier) {
		boolean loanOfficerAssistanceRequested = false;
		BorrowerCashFlowPlain cashFlowPlain = borrowerCashFlowPlainRepository.findActiveBorrowerCashFlow(applicationBorrowerIdentifier);
		if(cashFlowPlain!=null && LOAN_OFFICER_ASSISTANCE_REQUESTED.equalsIgnoreCase(cashFlowPlain.getCashFlowSubmissionMethodTypeCode()))
		{
			loanOfficerAssistanceRequested=true;
		}
		return loanOfficerAssistanceRequested;
	}
	
	private Set<SupportingDocument> findAllSupportingDocuments(Integer applicationIdentifier,
			Integer applicationBorrowerIdentifier) {

		Set<SupportingDocument> supportingDocuments = new HashSet<>();

		Set<SupportingDocument> documents = supportingDocumentRepository
				.findAllSupportingDocumentsForAnswers(applicationBorrowerIdentifier);

		if (!isEmpty(documents)) {

			supportingDocuments.addAll(documents);
		}

		Set<SupportingDocument> loanPurposeDocuments = supportingDocumentRepository
				.findAllSupportingDocumentsForLoanPurpose(applicationIdentifier);

		if (!isEmpty(loanPurposeDocuments)) {

			supportingDocuments.addAll(loanPurposeDocuments);
		}

		Set<SupportingDocument> dueLiabilityDocuments = supportingDocumentRepository
				.findAllSupportingDocumentsForDueLiability(applicationBorrowerIdentifier);

		if (!isEmpty(dueLiabilityDocuments)) {

			supportingDocuments.addAll(dueLiabilityDocuments);
		}

		Set<SupportingDocument> otherLiabilityDocuments = supportingDocumentRepository
				.findAllSupportingDocumentsForOtherLiability(applicationBorrowerIdentifier);

		if (!isEmpty(otherLiabilityDocuments)) {

			supportingDocuments.addAll(otherLiabilityDocuments);
		}
		
		Set<SupportingDocument> applicationBorrowerDocuments = supportingDocumentRepository
				.findAllByApplicationBorrowerIdentifier(applicationBorrowerIdentifier);
		
		if (!isEmpty(applicationBorrowerDocuments)) {
			
			supportingDocuments.addAll(applicationBorrowerDocuments);
		}
		
		Set<SupportingDocument> nonFarmIncomeDocuments = supportingDocumentRepository.findAllSupportingDocumentsForNonFarmIncome(applicationBorrowerIdentifier);
		
		if(!isEmpty(nonFarmIncomeDocuments)) {
			
			supportingDocuments.addAll(nonFarmIncomeDocuments);
		}
		
		Set<ApplicationBorrowerPlain> applicationCoBorrowerSet =  applicationBorrowerPlainRepository
				.findActiveCoApplicationBorrowerByApplicationID(applicationIdentifier);
		
		if (!applicationCoBorrowerSet.isEmpty()) {	
						
			for(ApplicationBorrowerPlain applicationCoBorrower : applicationCoBorrowerSet) {
				
				Set<SupportingDocument> coBorrowerAnswerDocuments = supportingDocumentRepository
						.findAllSupportingDocumentsForAnswers(applicationCoBorrower.getApplicationBorrowerIdentifier());

				if (!isEmpty(coBorrowerAnswerDocuments)) {

					supportingDocuments.addAll(coBorrowerAnswerDocuments);
				}
				
				Set<SupportingDocument> coBorrowerPostSubmissionDocuments = supportingDocumentRepository
						.findAllByApplicationBorrowerIdentifier(applicationCoBorrower.getApplicationBorrowerIdentifier());
				
				if (!isEmpty(coBorrowerPostSubmissionDocuments)) {
					
					supportingDocuments.addAll(coBorrowerPostSubmissionDocuments);
				}
			}
		}

		return supportingDocuments;
	}

	private String getOperationHQName(String statelocationAreaCode) {

		String operationHQName = "NA";

		if (!StringUtil.isEmptyString(statelocationAreaCode) && statelocationAreaCode.length() == 5) {

			String stateLocationCode = statelocationAreaCode.substring(0, 2);
			String countyLocationCode = statelocationAreaCode.substring(2);

			OLACounty county = findOLACounty(stateLocationCode, countyLocationCode);

			OLAState state = findOLAState(stateLocationCode);

			if (county != null) {

				operationHQName = OlaServiceUtil.getFormattedName(statelocationAreaCode, county, state);

			}
		}
		return operationHQName;

	}

	private OLACounty findOLACounty(String stateCode, String countyCode) {

		return stateCountyService.findCounty(stateCode, countyCode);
	}

	private OLAState findOLAState(String stateCode) {

		return stateCountyService.findState(stateCode);
	}

	@Override
	public ReAssignedLocationResponse reassignLocation(OlaAgencyToken agencyToken, ReassignLocationContract contract) {

		reassignLocationValidator.validate(contract);

		ReAssignedLocationResponse reAssignedLocationResponse = null;

		Optional<ApplicationPlain> applicationOptional = applicationPlainRepository
				.findById(contract.getApplicationIdentifier());
		ApplicationPlain application = null;
		if (applicationOptional.isPresent()) {

			application = applicationOptional.get();
			OLACounty olaCounty = findOLACounty(contract.getReassignOHQStateCode(), 
												 contract.getReassignOHQCountyCode());
			String initialOfficeFLPCode = application.getOfficeFlpCode(); //Original Office FLP Code
			String defaultOfficeFLPCode =  olaCounty.getFlpOfficeCode(); //This could be different from initialOfficeFLPCode 
			String reassignedOfficeFLPCode = contract.getReassignServiceCenter();
			boolean serviceCenterReassigned = serviceCenterReassigned(application, reassignedOfficeFLPCode);
			
			retainOriginalLocationInformation(agencyToken, application);
			
			LOGGER.info("application ID: {} defaultOfficeFLPCode: {} reassignedOfficeFLPCode: {}",
					contract.getApplicationIdentifier(), defaultOfficeFLPCode, reassignedOfficeFLPCode);

			application = application.toBuilder()
					.stateLocationAreaFlpCode(
							contract.getReassignOHQStateCode().concat(contract.getReassignOHQCountyCode()))
					.officeFlpCode(defaultOfficeFLPCode)
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
					.lastChangeUserName(agencyToken.getUserIdentifier())
					.assignedOfficeFlpCode(reassignedOfficeFLPCode).build();

			application = applicationPlainRepository.save(application);

			String reAssignedServiceCenter = stateCountyService.getServiceCenterOfficeInfo(application.getAssignedOfficeFlpCode(), true);

			String reassignedOperationHeadQuarter = getOperationHQName(application.getStateLocationAreaFlpCode());

			String defaultServiceCenter = "";
			if(application.getAssignedOfficeFlpCode().equals(defaultOfficeFLPCode))
			{
				defaultServiceCenter = reAssignedServiceCenter;
			}
			else
			{
				defaultServiceCenter = stateCountyService.getServiceCenterOfficeInfo(defaultOfficeFLPCode, true);
			}
			
			reAssignedLocationResponse = ReAssignedLocationResponse.builder()
					.applicationIdentifier(application.getApplicationIdentifier())
					.coreCustomerIdentifier(contract.getCoreCustomerIdentifier())
					.reassignServiceCenter(reAssignedServiceCenter)
					.reassignedOperationHeadQuarter(reassignedOperationHeadQuarter)
					.defaultOfficeFLPCode(defaultOfficeFLPCode)
					.initialOfficeFLPCode(initialOfficeFLPCode)
					.defaultServiceCenter(defaultServiceCenter)
					.build();
			if(flpOfficialEmailNotificationEnabled && serviceCenterReassigned)
			{
				try {
					sendEmailNotificationToServiceCenter(agencyToken, reAssignedLocationResponse);
				} catch (Exception e) {
					// Do not throw exception in case of email notification failures. Log the error and continue with the submission
					LOGGER.error("Error sending email to FLP Officials for app# {}, ccid {}",
							contract.getApplicationIdentifier(), contract.getCoreCustomerIdentifier(), e);
				}
			}

		}

		return reAssignedLocationResponse;
	}

	Boolean serviceCenterReassigned(ApplicationPlain originalApplication, String assignedOfficeFLPCode) {

		Boolean stateChanged = Boolean.FALSE;
		String stateLocationAreaFLPCode = null;

		if (null != originalApplication) {

			if (!StringUtil.isEmptyString(originalApplication.getAssignedOfficeFlpCode())) {
				if (!StringUtil.isEmptyString(assignedOfficeFLPCode)) {
					if (!(originalApplication.getAssignedOfficeFlpCode().equals(assignedOfficeFLPCode))) {
						stateChanged = Boolean.TRUE;
					}
				}
			} else {
				if (!StringUtil.isEmptyString(assignedOfficeFLPCode)) {
					stateChanged = Boolean.TRUE;
				}
			}
			
			LOGGER.info("Application ID: {}, Old stateLocationAreaFLPCode: {}, New stateLocationAreaFLPCode: {}, "
					+ "Old assignedOfficeFLPCode: {}, New assignedOfficeFLPCode: {}" ,
					originalApplication.getApplicationIdentifier(),
					originalApplication.getStateLocationAreaFlpCode(), stateLocationAreaFLPCode,
					originalApplication.getAssignedOfficeFlpCode(), assignedOfficeFLPCode);
		}
		
		

		return stateChanged;
	}
	
	void retainOriginalLocationInformation(OlaAgencyToken olaAgencyToken, ApplicationPlain originalApplication) {

		ApplicationHistory applicationHistory = getApplicationHistory(olaAgencyToken,originalApplication);

		ApplicationHistoryContract applicationHistoryContract = ApplicationHistoryContract.builder()
				.olaToken(olaAgencyToken).applicationHistory(applicationHistory).build();
		
		applicationService.saveApplicationHistory(applicationHistoryContract);

	}
	
	ApplicationHistory getApplicationHistory(OlaAgencyToken agencyToken, 
				ApplicationPlain originalApplication) {

		return ApplicationHistory.builder()
				.applicationIdentifier(originalApplication.getApplicationIdentifier())
				.stateLocationAreaFlpCode(originalApplication.getStateLocationAreaFlpCode())
				.officeFlpCode(originalApplication.getOfficeFlpCode())
				.assignedOfficeFlpCode(originalApplication.getAssignedOfficeFlpCode())
				.dataStatusCode(ACTIVE)
				.creationDate(DateUtil.getCurrentDateFromCalendar())
				.creationUserName(agencyToken.getUserIdentifier())
				.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
				.lastChangeUserName(agencyToken.getUserIdentifier()).build();
	}
	
	@Override
	public StatusChangeResponse changeStatus(OlaAgencyToken agencyToken, StatusChangeContract contract) {

		changeStatusValidator.validate(contract);

		StatusChangeResponse statusChangeResponse = null;
		BorrowerApplicationStatus borrowserAppStatus = null;
		Optional<BorrowerApplicationStatus> applicationStatusOptional = borrowerApplicationStatusRepository
				.findBorrowerApplicationStatus(contract.getApplicationIdentifier(),
						contract.getBorrowerApplicationStatusCode());

		if (!applicationStatusOptional.isPresent()) {

			borrowserAppStatus = borrowerApplicationStatusRepository
					.save(OlaServiceUtil.getBorrowerApplicationStatus(contract));

		} else {

			borrowserAppStatus = applicationStatusOptional.get();
		}

		Optional<ApplicationStatus> statusOptional = ApplicationStatus
				.fromCode(borrowserAppStatus.getBorrowerApplicationStatusCode());
		String borrowerApplicationStatusValue = "";
		if (statusOptional.isPresent()) {

			borrowerApplicationStatusValue = statusOptional.get().getDescription();

		}

		statusChangeResponse = StatusChangeResponse.builder()
				.applicationIdentifier(borrowserAppStatus.getApplicationIdentifier())
				.coreCustomerIdentifier(contract.getCoreCustomerIdentifier())
				.borrowerApplicationStatusCode(borrowerApplicationStatusValue).build();

		return statusChangeResponse;

	}
	
private void sendEmailNotificationToServiceCenter(OlaAgencyToken olaAgencyToken, ReAssignedLocationResponse reAssignedLocationResponse) {

		//  Get office Id by using State and County Office id
		Optional<Application> optionalApplication = applicationRepository.findById(reAssignedLocationResponse.getApplicationIdentifier());
		Application application = null;
		if (optionalApplication.isPresent()) {
			application =  optionalApplication.get();
			Date dateReceived = applicationService.findApplicationSubmissionDate(application.getApplicationIdentifier());
			Date dateReAssigned = application.getLastChangeDate();
			String reAssignedOfficeFLPCode = application.getAssignedOfficeFlpCode();
			List<Office> reAssignedOffices = stateCountyService.findFLPOffices(reAssignedOfficeFLPCode);
			if(CollectionUtils.isNotEmpty(reAssignedOffices)) {
				String operationHQName = getOperationHQName(application.getStateLocationAreaFlpCode());
				//String assignedOfficeFLPCode = application.getAssignedOfficeFlpCode();
				String stateName = getStateName(application.getStateLocationAreaFlpCode());
				String reAssignedServiceCenter = reAssignedLocationResponse.getReassignServiceCenter();
				String defaultOfficeFLPCode = reAssignedLocationResponse.getDefaultOfficeFLPCode();
				String initialServiceCenter = "";
				if(defaultOfficeFLPCode.equals(reAssignedLocationResponse.getInitialOfficeFLPCode()))
				{
					initialServiceCenter = reAssignedLocationResponse.getDefaultServiceCenter();
				}
				else
				{
					initialServiceCenter = stateCountyService.getServiceCenterOfficeInfo(reAssignedLocationResponse.getInitialOfficeFLPCode(), true);
				}
				String fullName = getFullName(reAssignedLocationResponse.getCoreCustomerIdentifier());
				Optional<Office> optionalObjectForReAssignedOffices = reAssignedOffices.stream().findFirst();
				if(optionalObjectForReAssignedOffices.isPresent()) {
					Office reAssignedOffice = optionalObjectForReAssignedOffices.get();
					String reAssignedOfficeId = String.valueOf(reAssignedOffice.getOfficeId());
					// Prepare email content
					Set<DomainValue> categorySet = domainService.findAllTypesByCategory(LOAN_PURPOSE_DOMAIN_CODE);
					String emailBody = OlaServiceUtil.getEmailBodyToSendServiceCenterOnReSubmission(categorySet,
							dateReceived, dateReAssigned, fullName, stateName, operationHQName, initialServiceCenter,  reAssignedServiceCenter, application);
					SimpleMailMessage message = new SimpleMailMessage();
					if(!useTestEmailAddress) {
						// Get EAS (app.fsa.flp.dls.sc), (app.fsa.flp.dls.so)  users by office Id and Role
						List<String>  flpOfficialEmailNotificationRoles = getFlpOfficialEmailNotificationRoles();
						List<String> serviceCenterEAuthIds = authorizationService.findUsersByCriteria(
											reAssignedOfficeId, flpOfficialEmailNotificationRoles);
						if(CollectionUtils.isNotEmpty(serviceCenterEAuthIds)) {	
							Set<String> recipients = getEmailListByEAuthId(serviceCenterEAuthIds);
							LOGGER.info("Email List {}", recipients);
							message.setTo(recipients.stream().toArray(String[]::new));
							if(flpOfficialEmailNotificationBccEnabled)
							{
								message.setBcc(emailNotificationGroup);
							}
						} else {
							LOGGER.error("Unable to find eas users in the selected office name: {}",reAssignedOffice.getOfficeName());
						}
					} else {
						message.setTo(emailNotificationGroup);
					}
					message.setFrom(emailNotificationGroup);
					if(!"Production".equals(environment)) {
						message.setSubject("Notification of FSA Online Loan Application Reassignment ("+environment+")");
					} else {
						message.setSubject("Notification of FSA Online Loan Application Reassignment");
					}
					message.setText(emailBody);
					
					// Send out notification
					javaMailSender.send(message);

				} else {
					LOGGER.error("Unable to find office Id from location service for the flp office: {}",reAssignedOfficeFLPCode);
					// need to implement code to let user know that office is null.
				}
			} else {
				LOGGER.error("Unable to find office Id from locaiton service for the flp office: {}",reAssignedOfficeFLPCode);
				// need to implement code to let user know that office is null.
			} 
		} else {
			LOGGER.error("Unable to retrive the applicant's application from data base, application identifier:  {}",reAssignedLocationResponse.getApplicationIdentifier());
		}
		
	}	
	
	private String getStateName(String statelocationAreaCode) {
		String stateName = NA;
		if (!StringUtil.isEmptyString(statelocationAreaCode) && statelocationAreaCode.length() == 5) {
			String stateLocationCode = statelocationAreaCode.substring(0, 2);
			OLAState state = findOLAState(stateLocationCode);
			return state.getStateName();
		}
		return stateName;

	}

	private String getFullName(Integer ccid) {

		String fullName = NA;

		SCIMSCustomer scimsCustomerBO = scimsCustomerService.getCustomer(agencyToken, ccid);
		if (scimsCustomerBO != null) {
			fullName = scimsCustomerBO.getCommonName();
		}

		return fullName;

	}
	
	private Set<String> getEmailListByEAuthId(List<String> eAuthIds){
		Set<String> eMailList = Collections.emptySet();
		int chunkSize = 10;
		//IPersonService service = PersonServiceFactory.getService();
		AtomicInteger counter = new AtomicInteger();
		LOGGER.info("E-AuthId size: {}",eAuthIds);
		List<Person> personsInformation = new ArrayList<Person>();
		if(CollectionUtils.isNotEmpty(eAuthIds) 
				&&  chunkSize < eAuthIds.size()) { // this is if the eAuthId list size is greater than 10, as the Person Service has limitation on size.
			Collection<List<String>> eAuthIdsList = eAuthIds.stream()
				    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
				    .values();
			for(List<String> eAuthIdList: eAuthIdsList) {
				//personsInformation.addAll(personService.findPersons(eAuthIdList));
				personsInformation.addAll(personServiceClient.getPersonsByEauthIds(String.join(", ", eAuthIdList)).getData());
			}
		} else if (CollectionUtils.isNotEmpty(eAuthIds)) {
			 //personsInformation = personService.findPersons(eAuthIds);
			 personsInformation = personServiceClient.getPersonsByEauthIds(String.join(", ", eAuthIds)).getData();
		}
		
		if(CollectionUtils.isNotEmpty(personsInformation)){
			List<String> names = personsInformation.stream()
					  .map(Person::getEmail)
					  .collect(Collectors.toList());
			eMailList = new HashSet<>(names); // to remove duplicate
		}
		
		return eMailList;
	}
	
	private boolean isNoneSelectedForAllTEESections(Set<OlaAnswerWithDocument> answers){	
		
		boolean nonesSelected = true;
		
		List<String> q1Codes = Arrays.asList(
				ExperienceQuestionType.OPERATED_OWN_FARM.getCode(),
				ExperienceQuestionType.MANAGED_FARM.getCode(),
				ExperienceQuestionType.FAMILY_FARM.getCode(),
				ExperienceQuestionType.FARM_LABOR.getCode()
				);
		List<String> q2Codes = Arrays.asList(
				ExperienceQuestionType.FOUR_YEAR_DEGREE.getCode(),
				ExperienceQuestionType.TWO_YEAR_DEGREE.getCode(),
				ExperienceQuestionType.FARM_MANAGEMENT_CURRICULUM.getCode(),
				ExperienceQuestionType.OTHER_EDUCATION.getCode()
				);
		List<String> q3Codes = Arrays.asList(
				ExperienceQuestionType.FFA_4H_TRIBAL.getCode(),
				ExperienceQuestionType.FARM_WORKSHOP_PROGRAMS.getCode(),
				ExperienceQuestionType.AGREE_PROGRAMS.getCode(),
				ExperienceQuestionType.INTERNSHIP_PROGRAM.getCode(),
				ExperienceQuestionType.URBAN_PROGRAM.getCode(),
				ExperienceQuestionType.APPRENTICE_PROGRAM.getCode(),
				ExperienceQuestionType.SCORE_PROGRAM.getCode()
				);		
		List<String> q4Codes = Arrays.asList(
				ExperienceQuestionType.NON_FARM_EXPERIENCE.getCode(),
				ExperienceQuestionType.ARMED_FORCES_DISCHARGED.getCode()
				);
			
		for (OlaAnswerWithDocument answer : answers) {
			if ((q1Codes.contains(answer.getOlaQuestionTypeCode().trim().toString()) || q2Codes.contains(answer.getOlaQuestionTypeCode().trim().toString()) || q3Codes.contains(answer.getOlaQuestionTypeCode().trim().toString()) || q4Codes.contains(answer.getOlaQuestionTypeCode().trim().toString())) &&
				answer.getQuestionTypeAnswerIndicator() != null &&
				answer.getQuestionTypeAnswerIndicator().equalsIgnoreCase("Y")) {
				nonesSelected = false;
				return false;
			}		
				
		}

		return nonesSelected;
	}

}
