package gov.usda.fsa.fcao.flp.ola.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import gov.usda.fpac.bcss.notification.sharedservice.client.domain.Email;
import gov.usda.fpac.bcss.notification.sharedservice.client.service.NotificationService;
import gov.usda.fpac.bcss.notification.sharedservice.client.service.RequestToken;
import gov.usda.fpac.bcss.notification.sharedservice.client.service.SendEmailContract;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.DateUtil;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLACounty;
import gov.usda.fsa.fcao.flp.ola.core.bo.OLAState;
import gov.usda.fsa.fcao.flp.ola.core.config.OLACoreConfig;
import gov.usda.fsa.fcao.flp.ola.core.config.OLACoreServiceConfig;
import gov.usda.fsa.fcao.flp.ola.core.entity.Application;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerPlainAndSectionStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationHistory;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationLoanPurpose;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationLoanPurposePlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationSelectionStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationWithBorrower;
import gov.usda.fsa.fcao.flp.ola.core.entity.Borrower;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationStatus.BorrowerApplicationStatusBuilder;
import gov.usda.fsa.fcao.flp.ola.core.entity.CreditReportFee;
import gov.usda.fsa.fcao.flp.ola.core.entity.CreditReportFeeHistory;
import gov.usda.fsa.fcao.flp.ola.core.entity.DisclosureCertification;
import gov.usda.fsa.fcao.flp.ola.core.entity.DomainValue;
import gov.usda.fsa.fcao.flp.ola.core.entity.NewApplication;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerWithDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.OperationProfileCategory;
import gov.usda.fsa.fcao.flp.ola.core.entity.OperationProfileSubCategory;
import gov.usda.fsa.fcao.flp.ola.core.entity.ParticipatingLender;
import gov.usda.fsa.fcao.flp.ola.core.entity.ReleaseAuthorization;
import gov.usda.fsa.fcao.flp.ola.core.entity.SupportingDocument;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationSectionCode;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationSectionStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.CropCategoryType;
import gov.usda.fsa.fcao.flp.ola.core.enums.LivestockMarketCategoryType;
import gov.usda.fsa.fcao.flp.ola.core.enums.LivestockProductCategoryType;
import gov.usda.fsa.fcao.flp.ola.core.enums.LoanApplicantType;
import gov.usda.fsa.fcao.flp.ola.core.enums.LoanRelationShipTypeCode;
import gov.usda.fsa.fcao.flp.ola.core.service.IApplicationService;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationBorrowerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationHistoryContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationIdentificationContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationStorageAddressContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.CreditReportFeeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.CreditReportFeeHistoryContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.LoanPurposeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.OperationProfileContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.SupportingDocumentContract;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.ServiceUnavailableException;
import gov.usda.fsa.fcao.flp.ola.core.service.factory.ApplicationFactory;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.LocationServiceClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.PersonServiceClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Office;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Person;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.SCIMSCustomer;
/**
 * Application Information will be added/updated/deleted.
 * 
 * @author Matt T
 *
 */
@Component
public class ApplicationServiceImpl extends OlaApplicationService implements IApplicationService {

	private static final Logger LOGGER = LogManager.getLogger(ApplicationServiceImpl.class);

	public static final String FLP = "FLP";
	public static final String FSA = "FSA";
	public static final String NA = "NA";
	
	@Value("${itkdls_Url}")
	private String itkDlsUrl;
	
	@Autowired
	RestTemplate restTemplate;
    
	@Lazy	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private JavaMailSender javaMailSender;
	 
	@Value("${emailNotificationGroup}")
	private String emailNotificationGroup;
	
	@Value("${useTestEmailAddress}")
	private boolean useTestEmailAddress;

	@Value("${borrowerEmailNotificationEnabled}")
	private boolean borrowerEmailNotificationEnabled;
	
	@Value("${flpOfficialEmailNotificationEnabled}")
	private boolean flpOfficialEmailNotificationEnabled;

	@Value("${flpOfficialEmailNotificationBccEnabled}")
	private boolean flpOfficialEmailNotificationBccEnabled;
	
	@Value("${coreApiEnvironment}")
	private String environment;

	@Autowired
	private LocationServiceClient locationServiceClient;
	
	@Autowired
	private PersonServiceClient personServiceClient;
	
	public ApplicationServiceImpl () {}

	@Override
	public Set<NewApplication> findAllApplications(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier) {

		return newApplicationRepository.findAllApplications(coreCustomerIdentifier);

	}
	
	@Override
	public Set<NewApplication> findCoBorrowerApplication(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier, String emailAddress, String lastname) {
		
		Set<NewApplication>  existing = new HashSet<NewApplication>();
		Set<ApplicationBorrowerPlain> existingCoBorrower = applicationBorrowerPlainRepository.findCoApplicationBorrowerByEmailAddress(emailAddress, lastname);
		if (!existingCoBorrower.isEmpty()) {	
			ApplicationBorrowerPlain borrowerApplication = existingCoBorrower.iterator().next();
			if(borrowerApplication.getConfirmationEmailAddress() != null && borrowerApplication.getConfirmationEmailAddress().length() > 0 && borrowerApplication.getEmailAddress().equals(borrowerApplication.getConfirmationEmailAddress())) {
				Optional<ApplicationBorrowerPlain> primaryApplicant = applicationBorrowerPlainRepository.findPrimaryApplicationBorrowerByApplicationID(borrowerApplication.getApplicationIdentifier());
				if(primaryApplicant.isPresent()) {
					ApplicationBorrowerPlain applicant =  primaryApplicant.get();
					NewApplication application = NewApplication.builder().applicationIdentifier(borrowerApplication.getApplicationIdentifier())
							.loanApplicantTypeCode(LoanApplicantType.JOINT.getCode())
							.stateLocationAreaFlpCode(null)
							.officeFlpCode(null)
							.assignedOfficeFlpCode(null)
							.lastChangeDate(DateUtil.getCurrentDateFromCalendar()).lastChangeUserName("" + borrowerApplication.getApplicationBorrowerIdentifier())
							.creationDate(DateUtil.getCurrentDateFromCalendar()).creationUserName("" + applicant.getCoreCustomerIdentifier())
							.dataStatusCode(ACTIVE).build();
					existing.add(application);
				}
			}
		}

		return existing;
	}
	
	@Override
	public boolean processCoApplications(OlaAgencyToken agencyToken, Integer coreCustomerIdentifier, Integer applicationBorrowerIdentifier, Boolean coApplicantRequest) {
		
		Optional<ApplicationBorrowerPlain> existingCoBorrower = applicationBorrowerPlainRepository.findById(applicationBorrowerIdentifier);
		
		if (existingCoBorrower.isPresent() && coApplicantRequest != null) {	
			if(coApplicantRequest == true) {
				// accept CoApplication
				
				Integer borrowerIdentifier = null;
				
				Optional<Borrower> borrower = borrowerRepository.findByCoreIdentifier(coreCustomerIdentifier);

				if (borrower.isPresent()) {
					borrowerIdentifier = borrower.get().getBorrowerIdentifier();
				}
				else {
					//No borrower entry found. This happens when a user with no applications accepts a member request
					Borrower borrowerToSave = Borrower.builder().coreCustomerIdentifier(coreCustomerIdentifier)
							.creationDate(DateUtil.getCurrentDateFromCalendar())
							.creationUserName(agencyToken.getUserIdentifier())
							.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
							.lastChangeUserName(agencyToken.getUserIdentifier()).dataStatusCode(ACTIVE).build();

					borrowerToSave = borrowerRepository.save(borrowerToSave);
					borrowerIdentifier = borrowerToSave.getBorrowerIdentifier();
				}

				applicationBorrowerPlainRepository.updateCoBorrowerData(applicationBorrowerIdentifier,borrowerIdentifier, coreCustomerIdentifier);
				//SEND EMAIL 	
				Optional<ApplicationBorrowerPlain> primaryApplicant = applicationBorrowerPlainRepository.findPrimaryApplicationBorrowerByApplicationID(existingCoBorrower.get().getApplicationIdentifier());
				
				if(primaryApplicant.isPresent()) {
					
					ApplicationBorrowerPlain applicant =  primaryApplicant.get();
						
					Integer primaryCCID = applicant.getCoreCustomerIdentifier();
					sendEmailNotificationAcceptRejectCoApplicant(coreCustomerIdentifier, primaryCCID, coApplicantRequest);
				} 
			}
			else {
				// Remove coApplication
				
				Set<ApplicationSelectionStatus> sectionOptional = applicationSelectionStatusRepository.findAllActiveSelectionStatuses(applicationBorrowerIdentifier);

				if (!sectionOptional.isEmpty()) {

					applicationSelectionStatusRepository.deleteApplicationSelectionStatus(applicationBorrowerIdentifier);
				}
				applicationBorrowerPlainRepository.deleteById(applicationBorrowerIdentifier);
				
				
				Optional<ApplicationBorrowerPlain> primaryApplicant = applicationBorrowerPlainRepository.findPrimaryApplicationBorrowerByApplicationID(existingCoBorrower.get().getApplicationIdentifier());
				
				if(primaryApplicant.isPresent()) {
					
					ApplicationBorrowerPlain applicant =  primaryApplicant.get();
						
					applicationSelectionStatusService.saveSectionStatus(agencyToken, applicant.getApplicationBorrowerIdentifier(),
							ApplicationSectionCode.HOW_WILL_APPLY.getCode(),
							ApplicationSectionStatus.PENDING.getCode());
					
					Integer primaryCCID = applicant.getCoreCustomerIdentifier();
					sendEmailNotificationAcceptRejectCoApplicant(coreCustomerIdentifier, primaryCCID, coApplicantRequest);
										
				} 
				
			}
		}

		return Boolean.TRUE;
	}

	@Override
	public NewApplication create(OlaAgencyToken olaAgencyToken, Borrower borrower) {

		NewApplication application = null;

		if (borrower != null) {

			// Check to see if the Borrower has any Loan is in progress

			Set<NewApplication> existingApplications = newApplicationRepository
					.findAllActiveApplications(borrower.getCoreCustomerIdentifier());

//			if (!isEmpty(existingApplications)) {
//
//				for (NewApplication newApplication : existingApplications) {
//
//					if (newApplication.getBorrowerApplicationStatusSet().size() == 1) {
//
//						throw new OLAServiceException(
//								"Duplicate application(s) are identified. Please contact the technical team.");
//					}
//
//				}
//
//			}

			ApplicationFactory factory = new ApplicationFactory();
			application = factory.createNewApplicationInstance(olaAgencyToken, borrower);
			
			// populate application number with FLEX service
			
			JSONObject flexAppJsonObject = new JSONObject();
			flexAppJsonObject.put("auditUser", olaAgencyToken.getUserIdentifier());
			
			LOGGER.info("FLEX application-number request: `{}`", flexAppJsonObject);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> httpEntityRequest = new HttpEntity<>(flexAppJsonObject.toString(), headers);     
			
			ResponseEntity<String> flexResponse = restTemplate.postForEntity(itkDlsUrl + "application-number", httpEntityRequest, String.class);
			LOGGER.info("FLEX application-number response: `{}`", flexResponse);	
			
			if(flexResponse.getStatusCode().is2xxSuccessful()) {
				
				JSONObject responseBody = new JSONObject(flexResponse.getBody());
				
				application = application.toBuilder()
						.applicationNumber(responseBody.getJSONObject("data").getInt("nextApplicationNumber"))
						.build();
				
				application = newApplicationRepository.save(application);
				
				LOGGER.debug("Application created successfully for the Borrower with Core ID: {} {} {}",
						borrower.getCoreCustomerIdentifier(), " Application ID: ",
						application.getApplicationIdentifier());

			} else {
				
				LOGGER.error("Error retrieving application number from application-number FLEX service. Request to FLEX returned status other than 2xx.");
				throw new ServiceUnavailableException("Request to retrieve application number from FLEX unsuccessful");
				
			}
			
		}

		return application;
	}

	@Override
	public boolean delete(OlaAgencyToken agencyToken, Integer applicationIdentifier) {

		Optional<BorrowerApplicationStatus> borrowerApplicationStatusOptional = borrowerApplicationStatusRepository
				.findBorrowerApplicationStatus(applicationIdentifier, ApplicationStatus.STARTED.getCode());

		if (borrowerApplicationStatusOptional.isPresent()) {

			BorrowerApplicationStatus borrowerApplicationStatus = borrowerApplicationStatusOptional.get();

			BorrowerApplicationStatusBuilder builder = borrowerApplicationStatus.toBuilder();

			borrowerApplicationStatus = builder.borrowerApplicationStatusCode(ApplicationStatus.DELETED.getCode())
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
					.lastChangeUserName(agencyToken.getUserIdentifier()).build();

			borrowerApplicationStatusRepository.save(borrowerApplicationStatus);
			
			// Remove exisiting coApplication

			 Set<ApplicationBorrowerPlain> applicationBorrowerSet = applicationBorrowerPlainRepository.findCoApplicationBorrowerByApplicationID(applicationIdentifier); 
		 	 
			 if(!applicationBorrowerSet.isEmpty()) {
				 ApplicationBorrowerPlain coBorrower = applicationBorrowerSet.iterator().next();
				 Set<ApplicationSelectionStatus> sectionOptional = applicationSelectionStatusRepository.findAllActiveSelectionStatuses(coBorrower.getApplicationBorrowerIdentifier());
	
				 if (!sectionOptional.isEmpty()) {	
					applicationSelectionStatusRepository.deleteApplicationSelectionStatus(coBorrower.getApplicationBorrowerIdentifier());
				 }
				 
				applicationBorrowerPlainRepository.deleteById(coBorrower.getApplicationBorrowerIdentifier());
		 	}	

		}

		return Boolean.TRUE;
	}

	@Override
	public Application findApplication(OlaAgencyToken agencyToken, Integer applicationIdentifier) {

		Optional<Application> optionalApplication = applicationRepository.findById(applicationIdentifier);

		if (optionalApplication.isPresent()) {

			return optionalApplication.get();
		}
		return null;
	}
	
	@Override
	public ApplicationPlain findPlainApplication(OlaAgencyToken agencyToken, Integer applicationIdentifier) {

		Optional<ApplicationPlain> optionalApplication = applicationPlainRepository.findById(applicationIdentifier);

		if (optionalApplication.isPresent()) {

			return optionalApplication.get();
		}
		return null;
	}
	

	@Override
	public NewApplication updateIdentification(OlaAgencyToken agencyToken, ApplicationIdentificationContract contract) {

		applicationIdentificationValidator.validate(contract);
		
		boolean primaryAndCoborrowerEmailsMatch = false;
		boolean coborrowerEmailConfirmed = false;
		
		ApplicationBorrowerPlain applicationBorrower = applicationBorrowerService.findActiveApplicationBorrower(agencyToken, contract.getCoreCustomerIdentifier(), contract.getApplicationIdentifier());

		if(applicationBorrower != null && applicationBorrower.getEmailAddress() != null && applicationBorrower.getEmailAddress().equalsIgnoreCase(contract.getCoApplicantEmail())) {
			
			LOGGER.error("Primary and Member email are the same for the application number: {}", contract.getApplicationIdentifier());
			primaryAndCoborrowerEmailsMatch = true;
			
		}
		
		if(contract.getCoApplicantConfirmationEmail() != null && contract.getCoApplicantConfirmationEmail().length() > 0 && contract.getCoApplicantConfirmationEmail().equals(contract.getCoApplicantEmail())) {
			
			LOGGER.error("Member emails do not match for the application number: {}", contract.getApplicationIdentifier());
			coborrowerEmailConfirmed = true;
			
		}
///////////////////////////////////////////////////////////////////		
//		applicationPlainRepository.updateIdentification(contract.getLoanApplicantTypeCode(),
//				contract.getApplicationIdentifier(), agencyToken.getUserIdentifier());

		applicationPlainRepository.updateIdentification(contract.getLoanApplicantTypeCode(),
														contract.getApplicationIdentifier(),
														agencyToken.getUserIdentifier(),
														contract.getApplicantSubtypeCode(),
														contract.getLegalEntityRegistrationNumber(),
														contract.getEmbeddedEntityIndicator(),
														contract.getOtherLegalEntityDescription());
		
		
		applicationBorrowerPlainRepository.updateMaritalStatus(contract.getMaritalStatusCode(),
				contract.getApplicationBorrowerIdentifier(), agencyToken.getUserIdentifier());
                
		applicationBorrowerPlainRepository.updateAppNickname(contract.getAppNickname(),
				contract.getApplicationIdentifier(), agencyToken.getUserIdentifier());
                
		LOGGER.debug("Application updated successfully for the application number: {}",
				contract.getApplicationIdentifier());
		
		// Add or update CoApplicant if applicant type == INE 
		if("INE".equalsIgnoreCase(contract.getLoanApplicantTypeCode().trim()) && !primaryAndCoborrowerEmailsMatch) {
			
			Set<ApplicationBorrowerPlain> applicationCoborrowerSet = applicationBorrowerService.findAllCoApplicationBorrowerByApplicationID(agencyToken, contract.getApplicationIdentifier()); 
						
			if(applicationCoborrowerSet != null && !applicationCoborrowerSet.isEmpty()) {
				
				// if a coborrower exists for this app, update CB record with user-entered values only if CCID is null (meaning CB hasn't accepted invite)
				
				ApplicationBorrowerPlain applicationCoborrower = applicationCoborrowerSet.iterator().next();
				
				if(applicationCoborrower.getCoreCustomerIdentifier() == null) {
					
					applicationCoborrower = applicationCoborrower.toBuilder()
							.emailAddress(contract.getCoApplicantEmail())
							.confirmationEmailAddress(contract.getCoApplicantConfirmationEmail())
							.applicantVerificationText(contract.getCoApplicantLastName())
							.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
							.build();
					
					applicationBorrowerPlainRepository.save(applicationCoborrower);
					
					LOGGER.info("successfully updated co-application borrower for the application number {} ", contract.getApplicationIdentifier());
					
				}
				
			} else {
				
				// if no coborrower exists for this app, create one
				
				if(contract.getCoApplicantLastName()!=null) {
					
					contract.setCoApplicantLastName(contract.getCoApplicantLastName().toUpperCase());
				}
					
				ApplicationBorrowerPlain borrower = ApplicationBorrowerPlain.builder().dataStatusCode("A")
							.applicationIdentifier(contract.getApplicationIdentifier())
							.maritalStatusCode(contract.getMaritalStatusCode())
							.emailAddress(contract.getCoApplicantEmail())
							.confirmationEmailAddress(contract.getCoApplicantConfirmationEmail())
							.applicantVerificationText(contract.getCoApplicantLastName())
							.lastChangeDate(DateUtil.getCurrentDateFromCalendar()).lastChangeUserName(agencyToken.getUserIdentifier())
							.creationDate(DateUtil.getCurrentDateFromCalendar()).creationUserName(agencyToken.getUserIdentifier())
							.loanRelationshipTypeCode(LoanRelationShipTypeCode.COBORROWER.getCode()).build();
					
				applicationBorrowerPlainRepository.save(borrower);
					
				LOGGER.info("successfully saved co-application borrower for the application number {} ", contract.getApplicationIdentifier());
				
			}
		}

		
		boolean isCompleted = Boolean.TRUE;

		if (StringUtil.isEmptyString(contract.getMaritalStatusCode()) && contract.getLoanApplicantTypeCode().equalsIgnoreCase(LoanApplicantType.INDIVIDUAL.getCode())) {
			isCompleted = Boolean.FALSE;
		}else {
			if (contract.getLoanApplicantTypeCode().equalsIgnoreCase(LoanApplicantType.INFORMAL_ENTITY.getCode())) {
				
				// has coborrower accepted invitation?
				
				if (StringUtil.isEmptyString(contract.getMaritalStatusCode())){
					isCompleted = Boolean.FALSE;
				}
				
				if(contract.getCoApplicantLastName() == null|| contract.getCoApplicantLastName().isEmpty()) {
					isCompleted = Boolean.FALSE;
				}
				
				if(contract.getCoApplicantEmail() == null || contract.getCoApplicantEmail().isEmpty() || primaryAndCoborrowerEmailsMatch) {
					isCompleted = Boolean.FALSE;
				}
				
				if(contract.getCoApplicantConfirmationEmail() == null || contract.getCoApplicantConfirmationEmail().isEmpty() || !coborrowerEmailConfirmed) {
					isCompleted = Boolean.FALSE;
				}
			}
		}

		applicationSelectionStatusService.saveSectionStatus(agencyToken, contract.getApplicationBorrowerIdentifier(),
				ApplicationSectionCode.HOW_WILL_APPLY.getCode(),
				isCompleted ? ApplicationSectionStatus.COMPLETED.getCode() : ApplicationSectionStatus.PENDING.getCode());

		Optional<NewApplication> optionalApplication = newApplicationRepository.findById(contract.getApplicationIdentifier());

		if (optionalApplication.isPresent()) {

			return optionalApplication.get();

		}

		return null;
	}
	
	@Override
	public NewApplication unlinkCoApplications(OlaAgencyToken olaAgencyToken, Integer applicationIdentifier, Set<ApplicationBorrowerPlain> applicationBorrowers) {
		
		 for(ApplicationBorrowerPlain coBorrower : applicationBorrowers) {
			 Integer applicationBorrowerIdentifier = coBorrower.getApplicationBorrowerIdentifier(); 			 
			
			// Soft delete CoApplication			
			applicationBorrowerPlainRepository.unlinkCoBorrower(applicationBorrowerIdentifier);
		 }
		 
		 Optional<ApplicationBorrowerPlain> applicationBorrowerOptional = applicationBorrowerPlainRepository.findPrimaryApplicationBorrowerByApplicationID(applicationIdentifier);
		 System.out.println("applicationBorrowerOptional: " + applicationBorrowerOptional);
		 if(applicationBorrowerOptional.isPresent()) {
			 System.out.println("RESETTING HWA to pending: " + applicationBorrowerOptional.get().getApplicationBorrowerIdentifier());
				applicationSelectionStatusService.saveSectionStatus(olaAgencyToken, applicationBorrowerOptional.get().getApplicationBorrowerIdentifier(),
				ApplicationSectionCode.HOW_WILL_APPLY.getCode(),
				ApplicationSectionStatus.PENDING.getCode());
		}

		Optional<NewApplication> optionalApplication = newApplicationRepository
				.findById(applicationIdentifier);


		if (optionalApplication.isPresent()) {

			return optionalApplication.get();

		}

		return null;
	}

	@Override
	public NewApplication saveOperationProfile(OperationProfileContract contract) {

		operationProfileValidator.validate(contract);
		
		// Reject is data has changed in database since last retrieval
		
		OperationProfileCategory operationProfileToSave = contract.getOperationProfileCategory();
		
		OperationProfileCategory existingOperationProfileCategory = 
				findPrimaryOperationPofileCategory(contract.getApplicationBorrowerIdentifier());
		
		if(operationProfileToSave != null && existingOperationProfileCategory != null) {
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, 12, 25, 59, 59, 59);

			if (operationProfileToSave.getLastChangeDate() != null && existingOperationProfileCategory.getLastChangeDate().compareTo(operationProfileToSave.getLastChangeDate()) > 0) {
				LOGGER.info("Last change date has changed");
				return null;
			}
			
			if (operationProfileToSave.getLastChangeDate().compareTo(calendar.getTime()) == 0  && existingOperationProfileCategory.getLastChangeDate() != null) {
				LOGGER.info("Last change date has changed");
				return null;
			}
		}

		String stateFlpCode = contract.getStateFLPCode();
		String countyCode = contract.getCountyFLPCode();


		String locationAreaFlpCode = stateFlpCode.concat(countyCode);
		
		String officeFLPCode = contract.getOfficeFlpCode();

		String message = "";

		if (officeFLPCode != null) {
			message = locationAreaFlpCode.concat("/").concat(officeFLPCode);
			LOGGER.info("Updating Office FLP Code:{}", message);
		} else {
			message = locationAreaFlpCode;
			LOGGER.error("Error updating Office FLP Code:{}", message);
		}

		// default setting - assign office_flp_code to assigned_office_flp_code 
		String assignedOfficeFLPCode =  officeFLPCode;
				
		applicationPlainRepository.updateStateAndOfficeCode(locationAreaFlpCode, officeFLPCode,
				assignedOfficeFLPCode, contract.getApplicationIdentifier());

		saveOperationProfileData(contract);

		return findNewApplication(contract.getApplicationIdentifier());

	}

	private void saveOperationProfileData(OperationProfileContract contract) {

		Integer applicationBorrowerIdentifier = contract.getApplicationBorrowerIdentifier();

		OperationProfileCategory operationProfileCategory = contract.getOperationProfileCategory();
		
		OlaAgencyToken token = contract.getOlaToken();

		OperationProfileCategory existingOperationProfileCategory = 
				findPrimaryOperationPofileCategory(applicationBorrowerIdentifier);

		Integer existingIdentifier = existingOperationProfileCategory != null ?
										existingOperationProfileCategory.getOperationProfileCategoryIdentifier() :
										null;
		
		Boolean subcategoryComplete = false;
		String operationCategoryCode = "";

		
		if (existingIdentifier != null && operationProfileCategory == null) {
			// If there is an existing profile, and the new one is null, delete the old.
			operationProfileCategoryRepository.deleteById(existingIdentifier);
		}

		if (operationProfileCategory != null) {
			if (existingIdentifier != null) {
				// If there is an existing profile, update it with the new information
				Date existingDate = existingOperationProfileCategory != null ?
						existingOperationProfileCategory.getCreationDate() : operationProfileCategory.getCreationDate();
				operationProfileSubCategoryRepository.deleteByParentIdentifer(existingIdentifier);
				operationProfileCategory = operationProfileCategory.toBuilder()
					.operationProfileCategoryIdentifier(existingIdentifier).creationDate(existingDate).lastChangeDate(DateUtil.getCurrentDateFromCalendar()).build();
			}
			else {
				operationProfileCategory = operationProfileCategory.toBuilder().lastChangeDate(DateUtil.getCurrentDateFromCalendar()).build();
			}
			operationProfileCategoryRepository.save(operationProfileCategory);
			
			
			
			operationCategoryCode = operationProfileCategory.getOperationCategoryCode();	
			
			for (OperationProfileSubCategory subcategory : operationProfileCategory.getOperationProfileSubCategorySet()) {
				
				String subcategoryCode = subcategory.getOperationSubcategoryCode();
				String subcategoryItemCode = subcategory.getOperationProfileSubcategoryItemCode();
				
				subcategoryComplete = subcategoryCode != null && !subcategoryCode.isEmpty();
				
				if(!operationCategoryCode.equals("CS")) {
					
					subcategoryComplete = subcategoryItemCode != null && !subcategoryItemCode.isEmpty();

				}
				
				if((subcategoryCode != null && !subcategoryCode.isEmpty()) && subcategoryCode.equals("OTH")) {
					
					subcategoryComplete = subcategory.getOperationProfileSubcategoryOtherDescription() != null &&
										  !subcategory.getOperationProfileSubcategoryOtherDescription().isEmpty(); 
				
				} 
						
				if(subcategoryComplete) {
					
					if (CropCategoryType.getCodesOther().contains(subcategoryItemCode) ||
								LivestockMarketCategoryType.getCodesOther().contains(subcategoryItemCode) ||
								LivestockProductCategoryType.getCodesOther().contains(subcategoryItemCode)) {
						
						if(!subcategoryCode.equals("GRC")) {
							subcategoryComplete = subcategory.getOperationProfileSubcategoryOtherItemDescription() != null &&
											  !subcategory.getOperationProfileSubcategoryOtherItemDescription().isEmpty();
						}
						
					} else if(subcategoryCode.equals(LivestockMarketCategoryType.BEEF_CATTLE.getCode()) && subcategoryItemCode.equals("SSK")) {
						
						subcategoryComplete = subcategory.getBreedName() != null &&
											  !subcategory.getBreedName().isEmpty();
						
					} else if(subcategoryCode.equals("DMP") && LivestockProductCategoryType.getCodesRequiringBreedName().contains(subcategoryItemCode)) {
						
						subcategoryComplete = subcategory.getBreedName() != null &&
											  !subcategory.getBreedName().isEmpty();
						
					}
				}
				
			}
			
		}		
		
		
		Boolean sectionComplete = contract.getStateFLPCode() != null && !contract.getStateFLPCode().isEmpty() &&
								  contract.getCountyFLPCode() != null && !contract.getCountyFLPCode().isEmpty() &&
								  contract.getOperationProfileCategory() != null &&
								  operationCategoryCode != null && !operationCategoryCode.isEmpty() &&
								  subcategoryComplete;
								  
		
		applicationSelectionStatusService.saveSectionStatus(
				token,
				applicationBorrowerIdentifier,
				ApplicationSectionCode.OPERATION_PROFILE.getCode(),
				sectionComplete ? ApplicationSectionStatus.COMPLETED.getCode() :
								  ApplicationSectionStatus.PENDING.getCode());

	}
	
	@Override
	public OperationProfileCategory findPrimaryOperationPofileCategory(Integer applicationBorrowerIdentifier) {
		
		return operationProfileCategoryRepository.findPrimaryOperationPofileCategory(applicationBorrowerIdentifier);
		
	}
	
	@Override
	public OperationProfileSubCategory findOperationPofileSubCategory(Integer operationProfileCategoryIdentifier) {
		
		return operationProfileSubCategoryRepository.findOperationProfileSubCategoryByParent(operationProfileCategoryIdentifier);
		
	}
	
	@Override
	public NewApplication saveSupportingDocuments(ApplicationBorrowerContract contract) {
		
		applicationBorrowerValidator.validate(contract);
		
		ApplicationBorrowerPlain applicationBorrowerToSave = contract.getApplicationBorrowerSet().iterator().next();
		Optional<ApplicationBorrowerPlain> existingApplicationBorrower = applicationBorrowerPlainRepository.findActiveApplicationBorrower(contract.getApplicationIdentifier(), contract.getCoreCustomerIdentifier());
		
		if(existingApplicationBorrower.isPresent()) {
			ApplicationBorrowerPlain firstRow = existingApplicationBorrower.get();
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, 12, 25, 59, 59, 59);
			if (applicationBorrowerToSave.getLastChangeDate() != null && firstRow.getLastChangeDate().compareTo(applicationBorrowerToSave.getLastChangeDate()) > 0) {
				LOGGER.info("Last change date has changed");
				return null;
			}
			if (applicationBorrowerToSave.getLastChangeDate().compareTo(calendar.getTime()) == 0  && firstRow.getLastChangeDate() != null) {
				LOGGER.info("Last change date has changed");
				return null;
			}
		}
		
		//applicationBorrowerSupportingDocumentRepository.deleteApplicationBorrowerSupportingDocuments(applicationBorrowerToSave.getApplicationBorrowerIdentifier());
		
		if (!isEmpty(contract.getApplicationBorrowerSet())) {

			saveSupportingDocumentsData(contract.getApplicationIdentifier(), contract.getBorrowerIdentifier(),
					contract.getApplicationBorrowerSet());
		}
		
		return findApplicationBorrowerNewApplication(contract.getApplicationIdentifier());
		
	}

	@Override
	public NewApplication saveLoanPurposes(LoanPurposeContract contract) {

		loanPurposeValidator.validate(contract);
		
		// Reject is data has changed in database since last retrieval
		
		ApplicationLoanPurposePlain loanPurposeToSave = contract.getApplicationLoanPurposeSet().iterator().next();
		List<ApplicationLoanPurposePlain>  existingLoanPurposes = applicationLoanPurposeRepository
				.findLoanPurposesByApplicationIdentifier(contract.getApplicationIdentifier());

		if(!existingLoanPurposes.isEmpty()) {
		ApplicationLoanPurposePlain firstRow = existingLoanPurposes.iterator().next();
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, 12, 25, 59, 59, 59);
			if (loanPurposeToSave.getLastChangeDate() != null && firstRow.getLastChangeDate().compareTo(loanPurposeToSave.getLastChangeDate()) > 0) {
				LOGGER.info("Last change date has changed");
				return null;
			}
			if (loanPurposeToSave.getLastChangeDate().compareTo(calendar.getTime()) == 0  && firstRow.getLastChangeDate() != null) {
				LOGGER.info("Last change date has changed");
				return null;
			}
		}

		applicationLoanPurposeRepository.deleteLoanPurposes(contract.getApplicationIdentifier());

		if (!isEmpty(contract.getApplicationLoanPurposeSet())) {

			saveLoanPurposesData(contract.getApplicationIdentifier(), contract.getBorrowerIdentifier(),
					contract.getApplicationLoanPurposeSet());
		}

		applicationSelectionStatusService.saveSectionStatus(
				contract.getOlaToken(),
				contract.getApplicationBorrowerIdentifier(),
				ApplicationSectionCode.LOAN_PURPOSE.getCode(),
				!contract.getApplicationLoanPurposeSet().isEmpty() ?
						ApplicationSectionStatus.COMPLETED.getCode() : 
						ApplicationSectionStatus.PENDING.getCode());
		
		return findNewApplication(contract.getApplicationIdentifier());

	}

	private void saveSupportingDocumentsData(Integer applicationIdentifier, Integer borrowerIdentifier,
			Set<ApplicationBorrowerPlain> applicationBorrowerSet) {
		
		for (ApplicationBorrowerPlain applicationBorrower : applicationBorrowerSet) {

			applicationBorrower = saveApplicationBorrowerSupportingDocument(applicationBorrower, borrowerIdentifier, applicationIdentifier);

			applicationBorrowerPlainRepository.save(applicationBorrower);

		}
	}
	
	private ApplicationBorrowerPlain saveApplicationBorrowerSupportingDocument(ApplicationBorrowerPlain applicationBorrower,
			Integer borrowerIdentifier, Integer applicationIdentifier) {

		SupportingDocumentContract contract = SupportingDocumentContract.builder()
				.applicationBorrowerIdentifier(applicationBorrower.getApplicationBorrowerIdentifier())
				.borrowerIdentifier(borrowerIdentifier).supportingDocuments(applicationBorrower.getSupportingDocuments())
				.removedSupportingDocuments(applicationBorrower.getRemovedSupportingDocuments())
				.build();

		if (!isEmpty(applicationBorrower.getSupportingDocuments())) {
			Set<SupportingDocument> documents = supportingDocumentService.save(contract);
			
			if(!isEmpty(applicationBorrower.getRemovedSupportingDocuments())) {
				documents.removeIf(doc -> doc.getStorageAddressText().equals(contract.getRemovedSupportingDocuments().iterator().next().getStorageAddressText()));
			}
			
			Set<SupportingDocument> inactiveDocuments = supportingDocumentRepository.findAllInactiveByApplicationBorrowerIdentifier(contract.getApplicationBorrowerIdentifier());
			documents.addAll(inactiveDocuments);

			for (SupportingDocument document : documents) {
				applicationBorrower.createSupportingDocument(document);
			}

		}

		Optional<ApplicationBorrowerPlain> existingApplicationBorrowerOptional = applicationBorrowerPlainRepository
				.findActiveApplicationBorrower(applicationIdentifier, applicationBorrower.getCoreCustomerIdentifier());

		Set<SupportingDocument> supportingDocuments = applicationBorrower.getSupportingDocuments();
		if (existingApplicationBorrowerOptional.isPresent()) {
			
			ApplicationBorrowerPlain existingApplicationBorrowerPlain = existingApplicationBorrowerOptional.get();
			applicationBorrower = applicationBorrower.toBuilder()
					.creationDate(existingApplicationBorrowerPlain.getCreationDate())
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
					.dataStatusCode(ACTIVE).build();
			
			
				
		}
		else {
			applicationBorrower = applicationBorrower.toBuilder()
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
					.dataStatusCode(ACTIVE).build();
		}
		for(SupportingDocument document : supportingDocuments) {
			applicationBorrower.addSupportingDocument(document);
		}
		
		return applicationBorrower;

	}
	
	private void saveLoanPurposesData(Integer applicationIdentifier, Integer borrowerIdentifier,
			Set<ApplicationLoanPurposePlain> applicationLoanPurposeSet) {
		
		for (ApplicationLoanPurposePlain loanPurpose : applicationLoanPurposeSet) {

			loanPurpose = saveLoanPurposeDocuments(loanPurpose, borrowerIdentifier, applicationIdentifier);

			applicationLoanPurposeRepository.save(loanPurpose);

		}
	}

	private ApplicationLoanPurposePlain saveLoanPurposeDocuments(ApplicationLoanPurposePlain loanPurpose,
			Integer borrowerIdentifier, Integer applicationIdentifier) {

		SupportingDocumentContract contract = SupportingDocumentContract.builder()
				.borrowerIdentifier(borrowerIdentifier).supportingDocuments(loanPurpose.getSupportingDocuments())
				.removedSupportingDocuments(loanPurpose.getRemovedSupportingDocuments()).build();

		if (!isEmpty(loanPurpose.getSupportingDocuments())) {

			Set<SupportingDocument> documents = supportingDocumentService.save(contract);

			for (SupportingDocument document : documents) {

				loanPurpose.createSupportingDocument(document);
			}

		}

		Set<ApplicationLoanPurposePlain> existingLoanPurposes = applicationLoanPurposeRepository
				.existingLoanPurposes(applicationIdentifier, loanPurpose.getLoanPurposeTypeCode().trim());

		if (!isEmpty(existingLoanPurposes)) {

			for (ApplicationLoanPurposePlain applicationLoanPurposePlain : existingLoanPurposes) {

				if (applicationLoanPurposePlain.getLoanPurposeTypeCode().trim()
						.equalsIgnoreCase(loanPurpose.getLoanPurposeTypeCode().trim())) {

					loanPurpose = loanPurpose.toBuilder()
							.applicationLoanPurposeIdentifier(
									applicationLoanPurposePlain.getApplicationLoanPurposeIdentifier())
							.creationDate(applicationLoanPurposePlain.getCreationDate())
							.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
							.dataStatusCode(ACTIVE).build();
				}
			}

		}
		else {
			loanPurpose = loanPurpose.toBuilder()
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar())
					.dataStatusCode(ACTIVE).build();
		}
		return loanPurpose;

	}

	@Override
	public Set<ApplicationPlain> findAllActiveApplications(Integer coreCustomerIdentifier) {

		return applicationPlainRepository.findAllActiveApplications(coreCustomerIdentifier);

	}

	@Override
	public ApplicationPlain findActiveApplication(OlaAgencyToken agencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) {

		Set<ApplicationPlain> allActiveApplications = findAllActiveApplications(coreCustomerIdentifier);

		if (!isEmpty(allActiveApplications)) {

			Optional<ApplicationPlain> activeApplication = allActiveApplications.stream()
					.filter(a -> a.getApplicationIdentifier().equals(applicationIdentifier)).findFirst();

			if (activeApplication.isPresent()) {

				return activeApplication.get();
			}

		}

		return null;
	}

	@Override
	public NewApplication findBaseApplication(OlaAgencyToken agencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) {

		Optional<NewApplication> applicaitonOptional = newApplicationRepository
				.findBaseApplication(coreCustomerIdentifier, applicationIdentifier);
		NewApplication newApplication = null;
		if (applicaitonOptional.isPresent()) {

			newApplication = applicaitonOptional.get();

			return addLoanPurposeDocuments(newApplication);

		}
		return newApplication;
	}

	public NewApplication findNewApplication(Integer applicationIdentifier) {

		Optional<NewApplication> applicaitonOptional = newApplicationRepository
				.findNewApplication(applicationIdentifier);
		NewApplication newApplication = null;
		if (applicaitonOptional.isPresent()) {

			newApplication = applicaitonOptional.get();

			return addLoanPurposeDocuments(newApplication);

		}
		return newApplication;
	}
	
	public NewApplication findApplicationBorrowerNewApplication(Integer applicationIdentifier) {

		Optional<NewApplication> applicaitonOptional = newApplicationRepository
				.findNewApplication(applicationIdentifier);
		NewApplication newApplication = null;
		if (applicaitonOptional.isPresent()) {

			newApplication = applicaitonOptional.get();

			return addApplicationBorrowerDocuments(newApplication);

		}
		return newApplication;
	}
	
	private NewApplication addApplicationBorrowerDocuments(NewApplication newApplication) {

		if (!isEmpty(newApplication.getApplicationBorrowerAllChildsSet())) {

			for (ApplicationBorrowerPlainAndSectionStatus applicationBorrowerPlainAndSectionStatus : newApplication.getApplicationBorrowerAllChildsSet()) { 

				Set<Integer> supportingDocumentIdentifiers = applicationBorrowerPlainAndSectionStatus.getAllSupportingDocumentIdentifiers();

				if (!isEmpty(supportingDocumentIdentifiers)) {

					Iterator<SupportingDocument> documents = supportingDocumentRepository
							.findAllById(supportingDocumentIdentifiers).iterator();

					while (documents.hasNext()) {
						
						SupportingDocument document = documents.next();

						applicationBorrowerPlainAndSectionStatus.createSupportingDocument(document);
						applicationBorrowerPlainAndSectionStatus.addSupportingDocument(document);
					}
				}

			}
		}
		return newApplication;
	}

	private NewApplication addLoanPurposeDocuments(NewApplication newApplication) {

		if (!isEmpty(newApplication.getApplicationLoanPurposeSet())) {

			for (ApplicationLoanPurpose loanPurpose : newApplication.getApplicationLoanPurposeSet()) {

				Set<Integer> supportingDocumentIdentifiers = loanPurpose.getAllSupportingDocumentIdentifiers();

				if (!isEmpty(supportingDocumentIdentifiers)) {

					Iterator<SupportingDocument> documents = supportingDocumentRepository
							.findAllById(supportingDocumentIdentifiers).iterator();

					while (documents.hasNext()) {

						loanPurpose.addSupportingDocument(documents.next());
					}
				}

			}
		}
		return newApplication;
	}

	@Override
	public Application findApplicationAndBorrowerData(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) {

		Optional<Application> applicaitonOptional = applicationRepository.findById(applicationIdentifier);

		if (applicaitonOptional.isPresent()) {

			return applicaitonOptional.get();
		}
		return null;
	}

	@Override
	public ApplicationWithBorrower findApplicationWithBorrower(OlaAgencyToken agencyToken,
			Integer applicationIdentifier) {

		Optional<ApplicationWithBorrower> applicaitonOptional = applicationWithBorrowerRepository
				.findById(applicationIdentifier);

		if (applicaitonOptional.isPresent()) {

			return applicaitonOptional.get();
		}
		return null;
	}

	@Override
	public CreditReportFee saveCreditReportFee(CreditReportFeeContract contract) {

		creditReportFeeValidator.validate(contract);

		CreditReportFee creditReportFee = contract.getCreditReportFee();

		CreditReportFee existingCreditReportFee = creditReportFeeRepository
				.findByApplication(contract.getApplicationIdentifier());

		if (existingCreditReportFee != null) {

			creditReportFee = creditReportFee.toBuilder()
					.creditReportFeeIdentifier(existingCreditReportFee.getCreditReportFeeIdentifier())
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar()).creationDate(existingCreditReportFee.getCreationDate()).build();

		}

		creditReportFee = creditReportFeeRepository.save(creditReportFee);
		
		LOGGER.info("paymentTypeCode="+creditReportFee.getCreditReportFeePaymentTypeCode()+" remittanceID="+creditReportFee.getCreditReportFeeRemittanceIdentifier());
		
		if(OLACoreServiceConfig.CREDIT_REPORT_FEE_PAY_BY_CHECK.equalsIgnoreCase(creditReportFee.getCreditReportFeePaymentTypeCode()))
		{
			BorrowerApplicationStatus borrowerApplicationStatus = OlaServiceUtil.getBorrowerApplicationStatus(contract);

			borrowerApplicationStatusRepository.save(borrowerApplicationStatus);

			// Send Email notification
			sendEmailNotification(contract, borrowerApplicationStatus.getCreationDate());
			
			applicationSelectionStatusService.saveSectionStatus(contract.getOlaToken(),
					creditReportFee.getApplicationBorrowerIdentifier(), ApplicationSectionCode.CREDIT_REPORT_FEE.getCode(),
					ApplicationSectionStatus.COMPLETED.getCode());
		}

		return creditReportFee;
	}
	
	
	
	@Override
	public CreditReportFee completeCreditReportFeeForElectronicPayment(CreditReportFeeContract contract) {
		
		creditReportFeeValidator.validate(contract);

		CreditReportFee creditReportFee = contract.getCreditReportFee();

		CreditReportFee existingCreditReportFee = creditReportFeeRepository
				.findByApplication(contract.getApplicationIdentifier());

		if (existingCreditReportFee != null) {

			creditReportFee = creditReportFee.toBuilder()
					.creditReportFeeIdentifier(existingCreditReportFee.getCreditReportFeeIdentifier())
					.lastChangeDate(DateUtil.getCurrentDateFromCalendar()).creationDate(existingCreditReportFee.getCreationDate()).build();
		}
		
		LOGGER.info("paymentTypeCode="+creditReportFee.getCreditReportFeePaymentTypeCode()+" remittanceID="+creditReportFee.getCreditReportFeeRemittanceIdentifier());
		
		BorrowerApplicationStatus borrowerApplicationStatus = OlaServiceUtil.getBorrowerApplicationStatus(contract);

		borrowerApplicationStatusRepository.save(borrowerApplicationStatus);

		// Send Email notification
		sendEmailNotification(contract, borrowerApplicationStatus.getCreationDate());
		applicationSelectionStatusService.saveSectionStatus(contract.getOlaToken(),
				creditReportFee.getApplicationBorrowerIdentifier(), ApplicationSectionCode.CREDIT_REPORT_FEE.getCode(),
				ApplicationSectionStatus.COMPLETED.getCode());

		return creditReportFee;
	}
	
	@Override
	public CreditReportFeeHistory saveCreditReportFeeHistory(CreditReportFeeHistoryContract contract) {

		creditReportFeeHistoryValidator.validate(contract);
		
		CreditReportFeeHistory creditReportFeeHistory = contract.getCreditReportFeeHistory();

		creditReportFeeHistory = creditReportFeeHistoryRepository.save(creditReportFeeHistory);
		
		return creditReportFeeHistory;
	}

	@Override
	public CreditReportFee findCreditReportFee(Integer applicationIdentifier) {
		
		return creditReportFeeRepository
				.findByApplication(applicationIdentifier);
		
	}
	
	@Override
	public Set<CreditReportFeeHistory> findCreditReportFeeHistoryList(Integer creditReportFeeIdentifier) {

		return creditReportFeeHistoryRepository
				.findByCreditReportFeeIdentifier(creditReportFeeIdentifier);
		
	}
	
	
	private void sendEmailNotificationToServiceCenter(CreditReportFeeContract contract, Date submissionDate) {

		//  Get office Id by using State and County Office id
		NewApplication application = findBaseApplication(contract.getOlaToken(), contract.getCoreCustomerIdentifier(),
				contract.getApplicationIdentifier());
		if(null != application) {
			String officeFLPCode = application.getOfficeFlpCode();
			List<Office> offices = stateCountyService.findFLPOffices(officeFLPCode);
			if(CollectionUtils.isNotEmpty(offices)) {
				String operationHQName = getOperationHQName(application.getStateLocationAreaFlpCode());
				//String assignedOfficeFLPCode = application.getAssignedOfficeFlpCode();
				String stateName = getStateName(application.getStateLocationAreaFlpCode());
				String assignedServiceCenter = stateCountyService.getServiceCenterOfficeInfo(officeFLPCode, true);
				String fullName = getFullName(contract);
				Optional<Office> optionalObject = offices.stream().findFirst();
				if(optionalObject.isPresent()) {
					Office office = optionalObject.get();
					String svcofficeId = String.valueOf(office.getOfficeId());
					LOGGER.info("Service Center office Id: {}", svcofficeId);
					// Prepare email content
					Set<DomainValue> categorySet = domainService.findAllTypesByCategory(LOAN_PURPOSE_DOMAIN_CODE);
					String emailBody = OlaServiceUtil.getEmailBodyToSendServiceCenter(contract.getApplicationIdentifier(), application, categorySet,
							submissionDate, fullName, stateName, operationHQName, assignedServiceCenter);
					SimpleMailMessage message = new SimpleMailMessage(); 
					if(!useTestEmailAddress) {
						//List<String>  flpOfficialEmailNotificationRoles = getFlpOfficialEmailNotificationRoles();
						List<String> svcRole = new ArrayList<>(Arrays.asList("app.fsa.flp.dls.sc"));						
						List<String> serviceCenterEAuthIds = authorizationService.findUsersByCriteria(
													svcofficeId, svcRole);
						LOGGER.info("serviceCenterEAuthIds: {}", serviceCenterEAuthIds);
						
						Set<String> recipients = new HashSet<String>();
						if(CollectionUtils.isNotEmpty(serviceCenterEAuthIds)) {	
							recipients = getEmailListByEAuthId(serviceCenterEAuthIds);
							LOGGER.info("Email Svc List {}", recipients);
						} else {
							LOGGER.error("Unable to find eas users in the selected office name: {}",office.getOfficeName());
						}
						
						//#####
						String stateCode = application.getStateLocationAreaFlpCode().substring(0, 2);
						LOGGER.info("stateCode: {}", stateCode);
//						List<Office> actual = locationService.findOffices("FLP", "FLP", stateCode);
						List<Office> actual = locationServiceClient.getOfficesByStateAndStandard(stateCode, "FLP").getData();
						Long stateOfficeId = null; 
						if(!actual.isEmpty()) {
							stateOfficeId = actual.get(0).getOfficeId();
							LOGGER.info("stateOfficeId: {}", stateOfficeId);
							List<String> soRole = new ArrayList<>(Arrays.asList("app.fsa.flp.dls.so"));						
							List<String> stateServiceCenterEAuthIds = authorizationService.findUsersByCriteria(
						        String.valueOf(stateOfficeId), soRole);
							LOGGER.info("stateServiceCenterEAuthIds: {}", stateServiceCenterEAuthIds);
						        
						    if(CollectionUtils.isNotEmpty(stateServiceCenterEAuthIds)) {
						        Set<String> stateRecipients = getEmailListByEAuthId(stateServiceCenterEAuthIds);
								LOGGER.info("Email state List {}", stateRecipients);
								recipients.addAll(stateRecipients);
						    }else {
								LOGGER.info("Unable to find eas users in the selected state code: {}",stateCode);
							}
			        	}
										        
				        if(!recipients.isEmpty()) {
							message.setTo(recipients.stream().toArray(String[]::new));
							if(flpOfficialEmailNotificationBccEnabled)
							{
								message.setBcc(emailNotificationGroup);
							}
				        }
				        else {
							message.setTo(emailNotificationGroup);
						}
					} else {
						message.setTo(emailNotificationGroup);
					}
					message.setFrom(emailNotificationGroup);
					if(!OLACoreConfig.AWS_PRODUCTION_ENVIRONMENT.equals(environment)) {
						message.setSubject("Notification of FSA Online Loan Application Submission  ("+environment+")");
					} else {
						message.setSubject("Notification of FSA Online Loan Application Submission");
					}
					message.setText(emailBody);
					
					// Send out notification
					long startTime = System.currentTimeMillis();
					javaMailSender.send(message);
					long endTime = System.currentTimeMillis();
					LOGGER.info("javaMailSender.send took {} {} ", (endTime - startTime), " milliseconds");

				} else {
					LOGGER.error("Unable to find office Id from locaiton service for the flp office: {}",officeFLPCode);
					// need to implement code to let user know that office is null.
				}
			} else {
				LOGGER.error("Unable to find office Id from locaiton service for the flp office: {}",officeFLPCode);
				// need to implement code to let user know that office is null.
			} 
		} else {
			LOGGER.error("Unable to retrive the applicant's application from data base, application identifier:  {}",contract.getApplicationIdentifier());
		}
		
	}
	

	private String getOperationHQName(String statelocationAreaCode) {

		String operationHQName = NA;

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
	
	private String getStateName(String statelocationAreaCode) {
		String stateName = NA;
		if (!StringUtil.isEmptyString(statelocationAreaCode) && statelocationAreaCode.length() == 5) {
			String stateLocationCode = statelocationAreaCode.substring(0, 2);
			OLAState state = findOLAState(stateLocationCode);
			return state.getStateName();
		}
		return stateName;

	}

	private OLACounty findOLACounty(String stateCode, String countyCode) {
		//flpOfficesByFlpCodeList
		return stateCountyService.findCounty(stateCode, countyCode);
	}

	private OLAState findOLAState(String stateCode) {

		return stateCountyService.findState(stateCode);
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
				personsInformation.addAll(personServiceClient.getPersonsByEauthIds(String.join(", ", eAuthIdList)).getData());
			}
		} else if (CollectionUtils.isNotEmpty(eAuthIds)) {
			 personsInformation = personServiceClient.getPersonsByEauthIds(String.join(", ", eAuthIds)).getData();
		}
		
		if(CollectionUtils.isNotEmpty(personsInformation)){
			List<String> names = personsInformation.stream()
					  .map(Person::getEmail)
					  .collect(Collectors.toList());
			
			names.removeIf(name -> name == null || "".equals(name.trim())); // to remove null and empty emails, which cause no emails to be sent

			eMailList = new HashSet<>(names); // to remove duplicate
		}
		
		return eMailList;
	}

	private void sendEmailNotification(CreditReportFeeContract contract, Date submissionDate) {
		try {
			if(borrowerEmailNotificationEnabled)
			{
				sendEmailNotificationToBorrower(contract, submissionDate);
			}
		} catch (Exception e) {
			// Do not throw exception in case of email notification failures. Log the error and continue with the submission
			LOGGER.error("Error sending email to Borrower for app# {}, ccid {}",
					contract.getApplicationIdentifier(), contract.getCoreCustomerIdentifier(), e);
		}
		try {
			if(flpOfficialEmailNotificationEnabled)
			{
				// Notifications goes to all FLP employees within assigned Service Center based on EAS Roles
				sendEmailNotificationToServiceCenter(contract, submissionDate);
			}
		} catch (Exception e) {
			// Do not throw exception in case of email notification failures. Log the error and continue with the submission
			LOGGER.error("Error sending email to FLP Officials for app# {}, ccid {}",
					contract.getApplicationIdentifier(), contract.getCoreCustomerIdentifier(), e);
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private void sendEmailNotificationToBorrower(CreditReportFeeContract contract, Date submissionDate) {

		String fullName = getFullName(contract);

		NewApplication application = findBaseApplication(contract.getOlaToken(), contract.getCoreCustomerIdentifier(),
				contract.getApplicationIdentifier());

		RequestToken requestToken = new RequestToken(APPLICATION_IDENTIFIER, "IP-ADDRESS"); // email body limit is 1024
																							// //IP ADDRESS IMPLEMENT
																							// LATER IF NEEDED
		List<String> coreCustomerIdentifiers = new ArrayList<>();
		coreCustomerIdentifiers.add(String.valueOf(contract.getCoreCustomerIdentifier()));


		Set<DomainValue> categorySet = domainService.findAllTypesByCategory(LOAN_PURPOSE_DOMAIN_CODE);

		String emailBody = OlaServiceUtil.getEmailBody(contract.getApplicationIdentifier(), application, categorySet,
				submissionDate, fullName);

		Email email = new Email(APPLICATION_IDENTIFIER, emailBody, SUBJECT);
		email.setReturnEmailAddress(emailNotificationGroup);

		SendEmailContract emailContract = new SendEmailContract(email, requestToken);
		emailContract.setEventName(EVENT_NAME);
		emailContract.setCoreCustomerId(coreCustomerIdentifiers);
		
		notificationService.sendEmailNotification(emailContract);

	}

	@SuppressWarnings("deprecation")
	private void sendEmailNotificationAcceptRejectCoApplicant(Integer coreCustomerIdentifier, Integer primaryCoreCustomerIdentifier, Boolean coApplicantRequest) {

		String subject = "";
		String fullName = getFirstLastName(coreCustomerIdentifier);
		
		if (coApplicantRequest.booleanValue()) {
			subject = "Member Accepted Joint Loan Application Request";
		}
		else {
			subject = "Member Rejected Joint Loan Application Request";
		}

		RequestToken requestToken = new RequestToken(APPLICATION_IDENTIFIER, "IP-ADDRESS"); // email body limit is 1024
																							// //IP ADDRESS IMPLEMENT
																							// LATER IF NEEDED
		List<String> coreCustomerIdentifiers = new ArrayList<>();
		coreCustomerIdentifiers.add(String.valueOf(primaryCoreCustomerIdentifier));

		String emailBody = OlaServiceUtil.getCoApplicantConfirmationBody(coApplicantRequest, fullName);
		
		Email email = new Email(APPLICATION_IDENTIFIER, emailBody, subject);
		email.setReturnEmailAddress(emailNotificationGroup);

		SendEmailContract emailContract = new SendEmailContract(email, requestToken);
		emailContract.setEventName(EVENT_NAME);
		emailContract.setCoreCustomerId(coreCustomerIdentifiers);
		
		notificationService.sendEmailNotification(emailContract);

	}

	private String getFullName(CreditReportFeeContract contract) {

		String fullName = "NA";

		SCIMSCustomer scimsCustomerBO = scimsCustomerService.getCustomer(agencyToken,
				contract.getCoreCustomerIdentifier());
		if (scimsCustomerBO != null) {
			fullName = scimsCustomerBO.getCommonName();
		}

		return fullName;

	}

	private String getFirstLastName(Integer coreCustomerID) {

		String fullName = "NA";

		SCIMSCustomer scimsCustomerBO = scimsCustomerService.getCustomer(agencyToken,
				coreCustomerID);
		if (scimsCustomerBO != null) {
			fullName = scimsCustomerBO.getCommonName();
		}

		return fullName;

	}
	@Override
	public DisclosureCertification saveDisclosureCertificationData(OlaAgencyToken agencyToken, DisclosureCertification disclosureCertification) {

		disclosureCertificationValidator.validate(disclosureCertification);
		
		disclosureCertificationRepository.delete(disclosureCertification.getApplicationBorrowerIdentifier());

		DisclosureCertification disclosureCertificationObject = disclosureCertificationRepository
				.findDisclosureCertification(disclosureCertification.getApplicationBorrowerIdentifier());

		if (disclosureCertificationObject != null) {
			
			disclosureCertification = disclosureCertification.toBuilder().disclosureCertificationIdentifier(disclosureCertificationObject.getDisclosureCertificationIdentifier())
					.creationDate(disclosureCertificationObject.getCreationDate()).build();
			
		}
			disclosureCertification = disclosureCertificationRepository.save(disclosureCertification);

			applicationSelectionStatusService.saveSectionStatus(agencyToken, disclosureCertification.getApplicationBorrowerIdentifier(),
					ApplicationSectionCode.NOTIFICATIONS_DISCLOSURES.getCode(), ApplicationSectionStatus.COMPLETED.getCode());
			
			return disclosureCertification;
		}
		

	@Override
	public ReleaseAuthorization saveReleaseAuthorizationData(OlaAgencyToken agencyToken,ReleaseAuthorization releaseAuthorization) {
		
		releaseAuthorizationValidator.validate(releaseAuthorization);
		
		releaseAuthorizationRepository.delete(releaseAuthorization.getApplicationBorrowerIdentifier());
		
		ReleaseAuthorization releaseAuthorizationObject = releaseAuthorizationRepository
				.findReleaseAuthorization(releaseAuthorization.getApplicationBorrowerIdentifier());

		if (releaseAuthorizationObject != null) { 
			
			releaseAuthorization = releaseAuthorization.toBuilder().releaseAuthorizationIdentifier(releaseAuthorizationObject.getReleaseAuthorizationIdentifier())
					.creationDate(releaseAuthorizationObject.getCreationDate()).build();
		}

			releaseAuthorization = releaseAuthorizationRepository.save(releaseAuthorization);
			
		
		applicationSelectionStatusService.saveSectionStatus(agencyToken, releaseAuthorization.getApplicationBorrowerIdentifier(),
				ApplicationSectionCode.AUTHORIZATIONS_SIGNATURES.getCode(), ApplicationSectionStatus.COMPLETED.getCode());
		
		return releaseAuthorization;
	}

	@Override
	public Date findApplicationSubmissionDate(Integer applicationIdentifier) {

		Optional<BorrowerApplicationStatus> borrowerApplicationStatus = borrowerApplicationStatusRepository
				.findBorrowerApplicationStatus(applicationIdentifier, ApplicationStatus.SUBMITTED.getCode());

		Date submissionDate = null;

		if (borrowerApplicationStatus.isPresent()) {

			submissionDate = borrowerApplicationStatus.get().getCreationDate();

		}

		return submissionDate;
	}

	@Override
	public ApplicationHistory saveApplicationHistory(ApplicationHistoryContract contract) {

		applicationHistoryValidator.validate(contract);

		ApplicationHistory applicationHistory = contract.getApplicationHistory();

		ApplicationHistory savedApplicationHistory = applicationHistoryRepository.save(applicationHistory);

		return savedApplicationHistory;
	}
	
	@Override
	public Set<OlaAnswerWithDocument> findAllActiveOlaAnswersWithDocuments(Integer applicationBorrowerIdentifier, Set<String> typeCodes) {

		Set<OlaAnswerWithDocument> olaAnswers = olaAnswerWithDocumentRepository.findAllActiveOlaAnswersByCodes(applicationBorrowerIdentifier, typeCodes);

		return olaAnswers;
	}
	
	@Override
	public Set<DisclosureCertification> findAllDisclosureCertification( Integer applicationBorrowerIdentifier){
		return disclosureCertificationRepository.findAllDisclosureCertification(applicationBorrowerIdentifier);
	}
	
	@Override
	public Set<ReleaseAuthorization> findAllReleaseAuthorization( Integer applicationBorrowerIdentifier){
		return releaseAuthorizationRepository.findAllReleaseAuthorizationIdentifier(applicationBorrowerIdentifier);
	}


	@Override
	public void saveApplicationStorageAddress(ApplicationStorageAddressContract contract) {

		applicationStorageAddressValidator.validate(contract);

		applicationPlainRepository.updateApplicationStorageAddress(
				contract.getApplicationIdentifier(), contract.getApplicationStorageAddress());

	}


	@Override
	public boolean findApplicationSignaturesLock(Integer applicationBorrowerIdentifier, Integer coApplicationBorrowerIdentifier) {
		boolean answer = releaseAuthorizationRepository.findAllReleaseAuthorizationIdentifier(applicationBorrowerIdentifier).isEmpty();
		if(!answer) {
			return true;
		}
		
		boolean coAnswer = releaseAuthorizationRepository.findAllReleaseAuthorizationIdentifier(coApplicationBorrowerIdentifier).isEmpty();
		if(!coAnswer) {
			return true;
		}
		
		return false; 
	}


	@Override
	public boolean findApplicationSignaturesUnLock(Integer applicationBorrowerIdentifier,
			Integer coApplicationBorrowerIdentifier, OlaAgencyToken olaAgencyToken) {
		releaseAuthorizationRepository.deleteAnyAuthorization(applicationBorrowerIdentifier);
		releaseAuthorizationRepository.deleteAnyAuthorization(coApplicationBorrowerIdentifier);	

		applicationSelectionStatusService.saveSectionStatus(olaAgencyToken, applicationBorrowerIdentifier,
				ApplicationSectionCode.AUTHORIZATIONS_SIGNATURES.getCode(),
				ApplicationSectionStatus.INCOMPLETE.getCode());
		
		applicationSelectionStatusService.saveSectionStatus(olaAgencyToken, coApplicationBorrowerIdentifier,
				ApplicationSectionCode.AUTHORIZATIONS_SIGNATURES.getCode(),
				ApplicationSectionStatus.INCOMPLETE.getCode());
		
		return Boolean.TRUE; 
	}

	@Override
	public Boolean AnswerWithParticipatingLenderCheck(Set<OlaAnswerWithDocument> checkAnswers,
			Integer applicationBorrowerIdentifier, OlaAgencyToken token) {
			boolean isUpdated = true;  
			Map<String, Date> lastChangeDate= new HashMap<>();
			Set<Integer> ids = new HashSet<>();
			boolean skip = true; 
			for (OlaAnswerWithDocument olaAnswerWithDocument : checkAnswers ) {
				skip = true; 
				if (olaAnswerWithDocument.getOlaQuestionTypeCode().trim().equalsIgnoreCase("PLEN")) {
					skip = false;
				} else if (olaAnswerWithDocument.getOlaQuestionTypeCode().trim().equalsIgnoreCase("PCCD")) {
					skip = false; 
				} else if (olaAnswerWithDocument.getOlaQuestionTypeCode().trim().equalsIgnoreCase("PFRE")) {
					skip = false;  
				} else if (olaAnswerWithDocument.getOlaQuestionTypeCode().trim().equalsIgnoreCase("OTH")) {
					skip = false;  
				}
				if(skip) {
					continue; 
				}
				for(ParticipatingLender model : olaAnswerWithDocument.getParticipatingLenderSet()) {
					if(model.getParticipatingLenderIdentifier()!=null) {
						ids.add(model.getParticipatingLenderIdentifier());
						lastChangeDate.put(model.getParticipatingLenderIdentifier().toString().trim(), olaAnswerWithDocument.getLastChangeDate());
					}
				}
				
			}
			Iterable<ParticipatingLender> existingParticipants = participatingLenderRepository.findAllById(ids);
		
			
			for(ParticipatingLender lender : existingParticipants) {
				
				Calendar calendar = Calendar.getInstance();
				calendar.set(1900, 12, 25, 59, 59, 59);
				if(lastChangeDate.containsKey(lender.getParticipatingLenderIdentifier().toString().trim())) {
					String key = lender.getParticipatingLenderIdentifier().toString().trim();
					if (lastChangeDate.get(key) != null && lender.getLastChangeDate().compareTo(lastChangeDate.get(key)) > 0) {
					LOGGER.info("Last change date has changed");
					return false;
					}
					
					if (lender.getLastChangeDate() != null && lastChangeDate.get(key).compareTo(calendar.getTime()) == 0) {
					LOGGER.info("Last change date has changed");
					return false;
					}
				}
			}
			
			return isUpdated;
	}


	@Override
	public Set<ParticipatingLender> getParticipatingLenderSetFromApplicationLenderId(Set<Integer> ids) {
		List<ParticipatingLender> results = participatingLenderRepository.findAllParticipatingLendersInSet(ids);
		return new HashSet<ParticipatingLender>(results);
		
	}

}