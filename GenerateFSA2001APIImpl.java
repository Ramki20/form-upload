package gov.usda.fsa.fcao.flp.ola.core.fsa.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import gov.usda.fsa.fcao.flp.flpids.common.utilities.DateUtil;
import gov.usda.fsa.fcao.flp.ola.core.api.IBaseAPI;
import gov.usda.fsa.fcao.flp.ola.core.api.model.CountyAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.api.model.CustomerAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartAOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartBOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartDOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartEOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartFOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartGOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartHOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartIOf2001;
import gov.usda.fsa.fcao.flp.ola.core.api.util.PartJOf2001;
import gov.usda.fsa.fcao.flp.ola.core.entity.Application;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrower;
import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationBorrowerPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerWithDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.ReleaseAuthorization;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.CitizenshipQuestionType;
import gov.usda.fsa.fcao.flp.ola.core.fsa.api.IGenerateFSA2001API;
import gov.usda.fsa.fcao.flp.ola.core.repository.ReleaseAuthorizationRepository;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.enums.LoanRelationShipTypeCode;

/**
 * @author Matt T
 * @apiNote Generates FSA-2001 Application.
 */
@Component
public class GenerateFSA2001APIImpl extends IBaseAPI implements IGenerateFSA2001API {

	@Value("${fsa2001FormUrl}")
	private String fsa2001FormUrl;
		
	public static final String SRC = "/pdf/FSA-2001.pdf";  // Latest
	public static final String SRC_01_13_23 = "/pdf/FSA-2001-R-01-13-23.pdf";
	
	private static final Logger LOGGER = LogManager.getLogger(GenerateFSA2001APIImpl.class);
	
	@Autowired
	private ReleaseAuthorizationRepository releaseAuthorizationRepository;
	
	@Override
	public ByteArrayInputStream generateFsa2001Report(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) {

		ByteArrayInputStream inputStream = null;

		PdfDocument pdfDoc = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Date dateReceived = applicationService.findApplicationSubmissionDate(applicationIdentifier);
		LOGGER.info("dateReceived: {}", dateReceived);
		Date date030524 = new GregorianCalendar(2024, Calendar.MARCH, 5).getTime();
		
//		String srcForm = SRC;   // Latest revised form
		String srcForm = fsa2001FormUrl;   //Use this for latest revised form (when using emso site)
		
		if(dateReceived!=null && dateReceived.before(date030524)) // override for the older applications 
		{
			srcForm = SRC_01_13_23;
		}
		LOGGER.info("srcForm: {}", srcForm);
				
		try (PdfReader reader = new PdfReader(srcForm);) {

			LOGGER.info("generateFsa2001Report reader="+reader);
			
			PdfWriter writer = new PdfWriter(out);
			
			pdfDoc = new PdfDocument(reader, writer);

			LOGGER.info("generateFsa2001Report pdfDoc="+pdfDoc);
			
			PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

			form.removeXfaForm();

			Map<String, PdfFormField> fields = form.getFormFields();

			Map<String, String> mappingfields = findAllMappingData(olaAgencyToken, coreCustomerIdentifier,
					applicationIdentifier);

			for (Map.Entry<String, String> mapData : mappingfields.entrySet()) {

				if (fields.get(mapData.getKey()) != null && mapData.getValue() != null) {

					fields.get(mapData.getKey()).setValue(mapData.getValue());

				}

			}
			form.flattenFields();

			pdfDoc.close();
			out.flush();
			out.close();
			inputStream = new ByteArrayInputStream(out.toByteArray());

		} catch (Exception ex) {
			LOGGER.error("Error generating FSA-2001 for the application number:"+applicationIdentifier+" coreCustomerIdentifier:"+coreCustomerIdentifier, ex);
		}
		return inputStream;

	}

	public Map<String, String> findAllMappingData(OlaAgencyToken olaAgencyToken, Integer coreCustomerIdentifier,
			Integer applicationIdentifier) throws IOException { 
		CustomerAPIModel memberModel = null; 
		Set<OlaAnswerWithDocument> OlaAnswersMember = null;
		Map<String, String> mapFields = new LinkedHashMap<>();
		
		Application application = applicationService.findApplication(olaAgencyToken, applicationIdentifier);

		String submittedDate = getSubmittedDateString(application);
		
		Optional<ApplicationBorrower> borrowerOptional = Optional.empty();
		Optional<ApplicationBorrower> memberOptional = Optional.empty();
		
		Set<ApplicationBorrower> activeApplicationBorrowerSet = application.getApplicationBorrowerAllChildsSet().stream()
				.filter(borrower -> borrower.getDataStatusCode().equals("A"))
				.filter(borrower -> borrower.getCoreCustomerIdentifier() != null)
				.collect(Collectors.toSet());
		
		if(activeApplicationBorrowerSet.size() >= 2 ) {
			for(ApplicationBorrower borrower : activeApplicationBorrowerSet) {
				
				//Normal Equals decided it was not going to work. 
				if (Objects.equals(borrower.getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.PRIMARY.getCode())) {
					borrowerOptional = Optional.of(borrower);
				} else if(Objects.equals(borrower.getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.COBORROWER.getCode())) {
					memberOptional = Optional.of(borrower); 
					//ApplicationBorrower member = memberOptional.get();
				}
				
				LOGGER.info("Mapping data to FSA-2001 for user with coreCustomerIdentifier: {}", borrower.getCoreCustomerIdentifier());
			}
			
			// Check if there is a one to one and that a primary and a co-borrower exists. 
			if(!borrowerOptional.isPresent() || !memberOptional.isPresent()) {
				throw new IOException();
			}
			if(!Objects.equals(borrowerOptional.get().getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.PRIMARY.getCode())
			|| !Objects.equals(memberOptional.get().getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.COBORROWER.getCode())){
				throw new IOException();
				
			}
		} else if(activeApplicationBorrowerSet.size() == 1) {
			for(ApplicationBorrower borrower : activeApplicationBorrowerSet) {	
				//Normal Equals decided it was not going to work. 
				if (Objects.equals(borrower.getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.PRIMARY.getCode())) {
					borrowerOptional = Optional.of(borrower);
				} 
				
				LOGGER.info("Mapping data to FSA-2001 for user with coreCustomerIdentifier: {}", borrower.getCoreCustomerIdentifier());
			}
			if(!borrowerOptional.isPresent()
			||!Objects.equals(borrowerOptional.get().getLoanRelationshipTypeCode(), LoanRelationShipTypeCode.PRIMARY.getCode())){
				throw new IOException();
			}
		}
		else {
			throw new IOException();
		}
		
		CustomerAPIModel customerModel = customerAPI.getCustomer(agencyToken, borrowerOptional.get().getCoreCustomerIdentifier());
		customerModel.setCountyOfOperationHqName(getCountyOfOperation(application));
		
		Set<OlaAnswerWithDocument> OlaAnswers =  applicationService.findAllActiveOlaAnswersWithDocuments(borrowerOptional.get().getApplicationBorrowerIdentifier(), CitizenshipQuestionType.getTypeCodes());
		
		String initial = customerModel.getInitial();
		
		Integer memberCoreCustomerIdentifier = null;
		if (memberOptional.isPresent()) {
			ApplicationBorrower coBorrower = memberOptional.get();
			memberCoreCustomerIdentifier = coBorrower.getCoreCustomerIdentifier();
			memberModel = customerAPI.getCustomer(agencyToken, memberCoreCustomerIdentifier );
			OlaAnswersMember =  applicationService.findAllActiveOlaAnswersWithDocuments(memberOptional.get().getApplicationBorrowerIdentifier(), CitizenshipQuestionType.getTypeCodes());
			initial = new StringBuilder().append(initial).append(", ").append(memberModel.getInitial()).toString();
		}

		// getting the primary applicant's signature, full_nm, from the app_base.rel_auth table 
		String primaryApplicantSignature = "";
		Set<ReleaseAuthorization> relAuthSetPrimary = releaseAuthorizationRepository.findAllReleaseAuthorizationIdentifier(borrowerOptional.get().getApplicationBorrowerIdentifier());
		Optional<ReleaseAuthorization> relAuthOptional = relAuthSetPrimary.stream().findFirst();
		if (relAuthOptional.isPresent()) {
			primaryApplicantSignature = relAuthOptional.get().getFullName();			
		}

		// secondary or co-applicant signature
		String secondaryApplicantSignature = "";
		if (memberOptional.isPresent()) {
			Set<ReleaseAuthorization> relAuthSetSecondary = releaseAuthorizationRepository.findAllReleaseAuthorizationIdentifier(memberOptional.get().getApplicationBorrowerIdentifier());
			Optional<ReleaseAuthorization> relAuthSetSecondaryOptional = relAuthSetSecondary.stream().findFirst();
			if (relAuthSetSecondaryOptional.isPresent()) {
				secondaryApplicantSignature = relAuthSetSecondaryOptional.get().getFullName();    
			}
		}
		
		PartAOf2001.fill(mapFields, customerModel, application);
		PartBOf2001.fill(mapFields, customerModel, borrowerOptional, initial, submittedDate, OlaAnswers);
		PartEOf2001.fill(mapFields, application);
		PartFOf2001.fill(mapFields, application, borrowerOptional);
		PartGOf2001.fill(mapFields, application, borrowerOptional, initial, submittedDate);
		PartHOf2001.fill(mapFields, borrowerOptional, customerModel.getFullLegalName());
		PartIOf2001.fill(mapFields, borrowerOptional);
		PartJOf2001.fill(mapFields, borrowerOptional, initial, submittedDate, customerModel, primaryApplicantSignature);

		
		if(memberModel!=null) {
			PartJOf2001.fillMemberSignature(mapFields, memberOptional, submittedDate, memberModel, secondaryApplicantSignature);
			PartDOf2001.fill(mapFields, memberModel, application, memberOptional, submittedDate, OlaAnswersMember);
		}
		return mapFields;
	}






	private String getSubmittedDateString(Application application) {

		Set<BorrowerApplicationStatus> activeBorrowerApplicationStatusSet = application
				.getBorrowerApplicationStatusSet().stream().filter(s -> s.getDataStatusCode().equalsIgnoreCase(ACTIVE))
				.collect(Collectors.toSet());

		BorrowerApplicationStatus submittedApplicationStatus = OlaServiceUtil.getBorrowerApplicationStatusByStatusCode(
				activeBorrowerApplicationStatusSet, ApplicationStatus.SUBMITTED.getCode());

		if (submittedApplicationStatus != null) {
			
			// get time from database, strip time zone, attach UTC, then convert to central time
			
			DateFormat noTimeZone = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			String dateNoTimeZone = noTimeZone.format(submittedApplicationStatus.getCreationDate());
						
			DateFormat attachUTC = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			attachUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date dateInUTC = Date.from(Instant.now());
			try {
				dateInUTC = attachUTC.parse(dateNoTimeZone);
			} catch (ParseException e) {
				LOGGER.error("ParseException: {0}", e);
			}

			DateFormat convertToStringInCentral = new SimpleDateFormat("MM/dd/yyyy");
			convertToStringInCentral.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
			return convertToStringInCentral.format(dateInUTC);

		}

		return "NA";
	}

	public String getCountyOfOperation(Application application) {
		String stateLocationAreaFlpCode = application.getStateLocationAreaFlpCode();

		String stateCode = stateLocationAreaFlpCode.substring(0, 2);
		String countyCode = stateLocationAreaFlpCode.substring(2);
		CountyAPIModel countyAPIModel = stateCountyAPI.findCounty(stateCode, countyCode);

		if (countyAPIModel != null) {

			return countyAPIModel.getCountyName();
		} else {
			LOGGER.error("Record not found for stateLocationAreaFlpCode:{} ", stateLocationAreaFlpCode);
		}

		return "NA";
	}


}