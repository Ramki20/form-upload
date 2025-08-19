package gov.usda.fsa.fcao.flp.ola.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.fcao.flp.ola.core.bo.FileBO;
import gov.usda.fsa.fcao.flp.ola.core.entity.DisclosureCertification;
import gov.usda.fsa.fcao.flp.ola.core.entity.ReleaseAuthorization;
import gov.usda.fsa.fcao.flp.ola.core.fsa.service.IAuthorizationService;
import gov.usda.fsa.fcao.flp.ola.core.repository.BorrowerRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.DomainRepository;
import gov.usda.fsa.fcao.flp.ola.core.service.IApplicationService;
import gov.usda.fsa.fcao.flp.ola.core.service.IDomainService;
import gov.usda.fsa.fcao.flp.ola.core.service.ISCIMSCustomerService;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationBorrowerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationHistoryContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationIdentificationContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ApplicationStorageAddressContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.BorrowerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.CreditReportFeeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.CreditReportFeeHistoryContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.FBPDataSubmissionStatusContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.FindQuestionAnswerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.LoanPurposeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.OperationProfileContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.QuestionAnswerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.ReassignLocationContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.SearchContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.StatusChangeContract;
import gov.usda.fsa.fcao.flp.ola.core.service.external.IStateCountyService;
import gov.usda.fsa.fcao.flp.ola.core.service.validator.OlaValidator;

/**
 * 
 * @author Matt T
 *
 */

public class OlaBaseService {

	protected static final String INACTIVE = "I";
	protected static final String ACTIVE = "A";

	protected static final String NO = "N";
	protected static final String BALANCE_SHEET_UPLOAD_QUESTION_CODE = "QCBS";

	@Autowired
	protected AgencyToken agencyToken;

	@Autowired
	@Valid
	protected OlaValidator<QuestionAnswerContract> questionAnswerValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<ApplicationBorrowerContract> applicationBorrowerValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<LoanPurposeContract> loanPurposeValidator;

	@Autowired
	@Valid
	protected OlaValidator<OperationProfileContract> operationProfileValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<ApplicationIdentificationContract> applicationIdentificationValidator;

	@Autowired
	@Valid
	protected OlaValidator<CreditReportFeeContract> creditReportFeeValidator;

	@Autowired
	@Valid
	protected OlaValidator<CreditReportFeeHistoryContract> creditReportFeeHistoryValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<FindQuestionAnswerContract> findQuestionAnswerValidator;

	@Autowired
	protected DomainRepository domainRepository;

	@Autowired
	protected IDomainService domainService;
	@Autowired
	protected IAuthorizationService authorizationService;

	@Autowired
	protected BorrowerRepository borrowerRepository;

	@Autowired
	@Valid
	protected OlaValidator<BorrowerContract> borrowerContractValidator;

	@Autowired
	@Valid
	protected OlaValidator<Integer> coreIdValidator;

	@Autowired
	@Valid
	protected OlaValidator<String> storageAddressValidator;

	@Autowired
	@Valid
	protected OlaValidator<FileBO> fileValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<DisclosureCertification> disclosureCertificationValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<ReleaseAuthorization> releaseAuthorizationValidator;
	
	
	@Autowired
	@Valid
	protected OlaValidator<SearchContract> searchContractValidator;


	@Autowired
	protected ISCIMSCustomerService scimsCustomerService;

	@Autowired
	protected IStateCountyService stateCountyService;

	@Autowired
	protected IApplicationService applicationService;
	
	@Autowired
	@Valid
	protected OlaValidator<ReassignLocationContract> reassignLocationValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<StatusChangeContract> changeStatusValidator;

	@Autowired
	@Valid
	protected OlaValidator<FBPDataSubmissionStatusContract> fbpDataSubmissionStatusContractValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<ApplicationHistoryContract> applicationHistoryValidator;
	
	@Autowired
	@Valid
	protected OlaValidator<ApplicationStorageAddressContract> applicationStorageAddressValidator;
	
	@Value("${flpOfficialEmailNotificationRoles}")
	protected String flpOfficialEmailNotificationRoles;
	
	private static final Logger LOGGER = LogManager.getLogger(OlaBaseService.class);
	
	public static <T> boolean isEmpty(Set<T> sourceArray) {

		return (sourceArray == null || sourceArray.isEmpty());
	}

	public static <T> boolean isEmpty(List<T> sourceArray) {

		return (sourceArray == null || sourceArray.isEmpty());
	}

	public List<String> getFlpOfficialEmailNotificationRoles()
	{
		  if(!StringUtil.isEmptyString(flpOfficialEmailNotificationRoles))
		  {
			 List<String> flpOfficialEmailNotificationRolesList = new ArrayList<>(Arrays.asList(flpOfficialEmailNotificationRoles.split(",")));
			 LOGGER.info("flpOfficialEmailNotificationRolesList="+flpOfficialEmailNotificationRolesList);
			 return flpOfficialEmailNotificationRolesList.stream().map(s -> s.replaceAll("\\s", "")).collect(Collectors.toList());
		  }
		  else
		  {
			  LOGGER.info("flpOfficialEmailNotificationRoles is empty or not configured correctly");
	   	  }
		  return new ArrayList<String>();
	}
	
}
