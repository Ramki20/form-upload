package gov.usda.fsa.fcao.flp.ola.core.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import gov.usda.fsa.fcao.flp.ola.core.entity.ApplicationLoanPurpose;
import gov.usda.fsa.fcao.flp.ola.core.entity.BorrowerDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.CreditHistoryEligibility;
import gov.usda.fsa.fcao.flp.ola.core.entity.FarmingExperienceEligibility;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerPlain;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerSupportingDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.OlaAnswerWithDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.ParticipatingLender;
import gov.usda.fsa.fcao.flp.ola.core.entity.SupportingDocument;
import gov.usda.fsa.fcao.flp.ola.core.entity.SupportingDocumentReference;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationSectionCode;
import gov.usda.fsa.fcao.flp.ola.core.enums.ApplicationSectionStatus;
import gov.usda.fsa.fcao.flp.ola.core.enums.CreditHistoryQuestionType;
import gov.usda.fsa.fcao.flp.ola.core.enums.ExperienceQuestionType;
import gov.usda.fsa.fcao.flp.ola.core.repository.BorrowerDocumentRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.CreditHistoryEligibilityRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.ParticipatingLenderRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.FarmingExperienceEligibilityRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.OlaAnswerSupportingDocumentRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.OlaAnswerWithDocumentRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.QuestionAnswerPlainRepository;
import gov.usda.fsa.fcao.flp.ola.core.repository.SupportingDocumentRepository;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.BorrowerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.FindQuestionAnswerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.contract.QuestionAnswerContract;
import gov.usda.fsa.fcao.flp.ola.core.service.impl.QuestionAnswerServiceImpl;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.service.validator.OlaValidator;

@ExtendWith(MockitoExtension.class)
public class QuestionAnswerServiceImplTest {

	@Mock
	private OlaAgencyToken token;

	@InjectMocks
	private QuestionAnswerServiceImpl questionAnswerServiceImpl = new QuestionAnswerServiceImpl();

	@Mock
	IApplicationSelectionStatusService applicationSelectionStatusService;

	@Mock
	private QuestionAnswerPlainRepository questionAnswerPlainRepository;

	@Mock
	private OlaAnswerWithDocumentRepository olaAnswerWithDocumentRepository;

	@Mock
	private SupportingDocumentRepository supportingDocumentRepository;

	@Mock
	private BorrowerDocumentRepository borrowerDocumentRepository;

	@Mock
	private OlaAnswerSupportingDocumentRepository olaAnswerSupportingDocumentRepository;
	
	@Mock
	private CreditHistoryEligibilityRepository creditHistoryEligibilityRepository;
	
	@Mock
	private FarmingExperienceEligibilityRepository farmingExperienceEligibilityRepository;

	@Mock
	private ParticipatingLenderRepository participatingLenderRepository;
	
	@Mock
	OlaServiceUtil olaServiceUtil;

	OlaAnswerWithDocument olaAnswerWithDocument;

	@Mock
	private OlaValidator<BorrowerContract> questionAnswerValidator;

	@Mock
	private OlaValidator<BorrowerContract> findQuestionAnswerValidator;

	@Mock
	private Set<OlaAnswerWithDocument> answersWithDocumentsToSave;

	private static final String COMPLETE = ApplicationSectionStatus.COMPLETED.getCode();
	private static final String PENDING = ApplicationSectionStatus.PENDING.getCode();
	private static final String OLA_TYPE_CODE = "T";
	
	QuestionAnswerContract contract;
	FindQuestionAnswerContract questionContract;
	SupportingDocument supportingDoc;
	CreditHistoryEligibility creditHistoryEligibility;
	FarmingExperienceEligibility farmingExperienceEligibility;
	OlaAnswerPlain olaAnswerPlain;
	BorrowerDocument borrowerDocument;
	
	Set<String> olaTypeCodes = new HashSet<>();
	Set<SupportingDocument> documentsSet = new HashSet<SupportingDocument>();
	Set<SupportingDocumentReference> supportingDocumenentReferences  = new HashSet<>();
	Set<OlaAnswerPlain> existingAnswers  = new HashSet<>(); 
	Set<OlaAnswerWithDocument> actualDocuments = new HashSet<>();
	
	@BeforeEach
	public void setUp() throws Exception {
		//Set<OlaAnswerPlain> existingAnswers = new HashSet<OlaAnswerPlain>();
		olaAnswerPlain = OlaAnswerPlain.builder()
							  		   .applicationBorrowerIdentifier(500)
							  		   .answerText("olaAnswerPlains")
							  		   .olaAnswerIdentifier(100)
							  		   .olaQuestionTypeCode(OLA_TYPE_CODE)
							  		   .lastChangeDate(new Date(1710185928752L))
							  		   .build();
		existingAnswers.add(olaAnswerPlain);
		
		borrowerDocument = BorrowerDocument.builder()
										   .borrowerDocumentIdentifier(6000)
										   .dataStatusCode("A")
										   .build();
		
		supportingDocumenentReferences = new HashSet<>();
		SupportingDocumentReference supportingDocumentRef =
				SupportingDocumentReference.builder()
										   .supportingDocumentIdentifier(6000)
										   .dataStatusCode("A")
										   .build();
		supportingDocumenentReferences.add(supportingDocumentRef);

		olaAnswerWithDocument = OlaAnswerWithDocument.builder()
													 .applicationBorrowerIdentifier(6653)
													 .dataStatusCode("A")
													 .olaQuestionTypeCode(OLA_TYPE_CODE)
													 .olaAnswerIdentifier(100)
													 .lastChangeDate(new Date(1710185928752L))
													 .supportingDocumentReferenceSet(supportingDocumenentReferences)
													 .build();
		supportingDoc = SupportingDocument.builder()
										  .supportingDocumentIdentifier(100)
										  .dataStatusCode("A")
										  .storageAddressText("Address")
										  .build();
		SupportingDocument supportingDoc2 = SupportingDocument.builder()
															  .supportingDocumentIdentifier(100)
															  .storageAddressText("Address")
															  .dataStatusCode("A")
															  .build();
		olaAnswerWithDocument.addSupportingDocument(supportingDoc);
		olaAnswerWithDocument.addSupportingDocument(supportingDoc2);
		documentsSet.add(supportingDoc);
		documentsSet.add(supportingDoc2);
		
		actualDocuments.add(olaAnswerWithDocument);
	
		olaTypeCodes.add(OLA_TYPE_CODE);
		
		creditHistoryEligibility = 
				CreditHistoryEligibility.builder()
										.creditHistoryEligibilityIdentifier(1)
										.build();
		
		farmingExperienceEligibility =
				FarmingExperienceEligibility.builder()
											.farmingExperienceEligibilityIdentifier(1)
											.build();
		
		contract = QuestionAnswerContract.builder()
										 .coreCustomerIdentifier(10039293)
										 .applicationIdentifier(100)
										 .applicationBorrowerIdentifier(500)
										 .olaTypeCodes(olaTypeCodes)
										 .olaToken(token)
										 .sectionCode("C")
										 .build();

		questionContract = FindQuestionAnswerContract.builder()
													 .coreCustomerIdentifier(10039293)
													 .applicationIdentifier(100)
													 .applicationBorrowerIdentifier(500)
													 .olaTypeCodes(olaTypeCodes)
													 .olaToken(token)
													 .build();
	}

	@Test
	public void deleteOlaAnswers() {
		contract.getOlaAnswersPlainSet().add(OlaAnswerPlain.builder()
														   .olaQuestionTypeCode(OLA_TYPE_CODE)
														   .build());
		questionAnswerServiceImpl.deleteOlaAnswers(contract);

		verify(questionAnswerPlainRepository, times(1)).deleteQuestionAnswers(500, olaTypeCodes);
	}

	@Test
	public void saveOlaAnswersPlain_simple() {
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(500, olaTypeCodes))
				.thenReturn(existingAnswers);

		questionAnswerServiceImpl.saveOlaAnswersPlain(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), PENDING);
	}
	
	@Test
	public void saveOlaAnswersPlain_OptimistLocking() {
		Set<OlaAnswerPlain> dataFromDb = new HashSet<>();
		dataFromDb.add(OlaAnswerPlain.builder()
										  		 .applicationBorrowerIdentifier(500)
										  		 .answerText("olaAnswerPlains")
										  		 .olaAnswerIdentifier(100)
										  		 .creationDate(new Date())
										  		 .lastChangeDate(new Date(1710185928752L))
										  		 .olaQuestionTypeCode(OLA_TYPE_CODE)
										  		 .build());

		contract.setOlaAnswersPlainSet(existingAnswers);
		
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(Mockito.anyInt(), Mockito.anySet()))
			.thenReturn(dataFromDb);

		boolean olaSet = questionAnswerServiceImpl.saveOlaAnswersPlain(contract);
		
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertTrue(olaSet);
	}
	
	@Test
	public void saveOlaAnswersPlain_OptimistLockingFail() {
		Set<OlaAnswerPlain> dataFromDb = new HashSet<>();
		dataFromDb.add(OlaAnswerPlain.builder()
										  		 .applicationBorrowerIdentifier(500)
										  		 .answerText("olaAnswerPlains")
										  		 .olaAnswerIdentifier(100)
										  		 .creationDate(new Date())
										  		 .lastChangeDate(new Date())
										  		 .olaQuestionTypeCode(OLA_TYPE_CODE)
										  		 .build());

		contract.setOlaAnswersPlainSet(existingAnswers);
		
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(Mockito.anyInt(), Mockito.anySet()))
		.thenReturn(dataFromDb);

		boolean olaSet = questionAnswerServiceImpl.saveOlaAnswersPlain(contract);

		Assertions.assertFalse(olaSet);

	}
	
	@Test
	public void saveOlaAnswersPlain_noExistingAnswers() {
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(500, olaTypeCodes))
				.thenReturn(new HashSet<OlaAnswerPlain>());

		questionAnswerServiceImpl.saveOlaAnswersPlain(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);
	}

	@Test
	public void saveOlaAnswersPlain_noExistingErrors() {
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(500, olaTypeCodes))
				.thenReturn(new HashSet<OlaAnswerPlain>());

		questionAnswerServiceImpl.saveOlaAnswersPlain(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);
	}

	@Test
	public void saveOlaAnswersPlain_updateExisting() {
		Set<OlaAnswerPlain> existingAnswers = new HashSet<OlaAnswerPlain>();
		Date today = new Date();
		existingAnswers.add(OlaAnswerPlain.builder()
						  				  .applicationBorrowerIdentifier(500)
						  				  .answerText("olaAnswerPlains")
						  				  .olaQuestionTypeCode("QT")
						  				  .olaAnswerIdentifier(100)
						  				  .creationDate(today)
						  				  .lastChangeDate(new Date())
						  				  .build());
		
		Set<OlaAnswerPlain> answersToSave = new HashSet<OlaAnswerPlain>();
		answersToSave.add(OlaAnswerPlain.builder()
				  						.applicationBorrowerIdentifier(500)
				  						.answerText("Answer to save")
				  						.olaQuestionTypeCode("QT")
						  				.olaAnswerIdentifier(101)
						  				.lastChangeDate(new Date())
				  						.build());
		
		QuestionAnswerContract contract = 
				QuestionAnswerContract.builder()
									  .coreCustomerIdentifier(10039293)
									  .applicationIdentifier(100)
									  .applicationBorrowerIdentifier(500)
									  .olaTypeCodes(olaTypeCodes)
									  .olaAnswersPlainSet(answersToSave)
									  .olaToken(token)
									  .sectionCode("C")
									  .sectionComplete(false)
									  .build();
	
		when(questionAnswerPlainRepository.findAllQuestionAnswersByCodes(500, olaTypeCodes))
				.thenReturn(existingAnswers);
	
		questionAnswerServiceImpl.saveOlaAnswersPlain(contract);
	
		verify(questionAnswerPlainRepository, times(1)).saveAll(Mockito.anyIterable());
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), PENDING);
	}
	
	@Test
	public void findAllByBorrowerAndTypeCodes() {
		Set<OlaAnswerWithDocument> olaAnswers = new HashSet<OlaAnswerWithDocument>();
		olaAnswers.add(olaAnswerWithDocument);
		
		OlaAnswerWithDocument bankruptcyAnswer =
				OlaAnswerWithDocument.builder()
									 .applicationBorrowerIdentifier(6653)
									 .dataStatusCode("A")
									 .olaAnswerIdentifier(2)
									 .olaQuestionTypeCode(CreditHistoryQuestionType.BANKRUPTCY.getCode())
									 .build(); 
		olaAnswers.add(bankruptcyAnswer);

		OlaAnswerWithDocument eligibilityAnswer =
				OlaAnswerWithDocument.builder()
									 .applicationBorrowerIdentifier(6653)
									 .dataStatusCode("A")
									 .olaAnswerIdentifier(3)
									 .olaQuestionTypeCode(ExperienceQuestionType.OPERATED_OWN_FARM.getCode())
									 .build(); 
		olaAnswers.add(eligibilityAnswer);
		
		OlaAnswerSupportingDocument olaAnswerSupportingDocument =
				OlaAnswerSupportingDocument.builder()
										   .documentYear(2023)
										   .build();
		
		Set<CreditHistoryEligibility> creditHistoryEligibilities = new HashSet<>();
		creditHistoryEligibilities.add(creditHistoryEligibility);
		Set<FarmingExperienceEligibility> farmingExperience = new HashSet<>();
		farmingExperience.add(farmingExperienceEligibility);
		
		when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(
				questionContract.getApplicationBorrowerIdentifier(), questionContract.getOlaTypeCodes()))
			.thenReturn(olaAnswers);
		when(supportingDocumentRepository.findAllById(Mockito.anyIterable()))
			.thenReturn(documentsSet);
		when(olaAnswerSupportingDocumentRepository.findOlaAnswerSupportingDocument(Mockito.anyInt(), Mockito.anyInt()))
			.thenReturn(olaAnswerSupportingDocument);
		when(creditHistoryEligibilityRepository.findAllCreditHistoryEligibilities(Mockito.anyInt()))
			.thenReturn(creditHistoryEligibilities);
		when(farmingExperienceEligibilityRepository.findAllFarmingExperienceEligibilities(Mockito.anyInt()))
			.thenReturn(farmingExperience);

		Set<OlaAnswerWithDocument> actualDocuments = questionAnswerServiceImpl.findAllOlaAnswersWithDocuments(questionContract);

		verify(olaAnswerWithDocumentRepository, times(1)).findAllOlaAnswersByCodes(
				questionContract.getApplicationBorrowerIdentifier(), questionContract.getOlaTypeCodes());

		Assertions.assertNotNull(actualDocuments);
		Assertions.assertTrue(actualDocuments.size() > 0);
	}

	@Test
	public void saveOlaAnswersPlainWithDocuments() {
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc);
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}

	@Test
	public void saveOlaAnswersPlainWithDocuments_removeSupportingDocument() {
		Set<OlaAnswerWithDocument> actualDocuments = new HashSet<>();
		olaAnswerWithDocument.addSupportingDocumentToBeRemoved(supportingDoc);
		actualDocuments.add(olaAnswerWithDocument);
		
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc);
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		Mockito.verify(supportingDocumentRepository).save(argThat(
				new ArgumentMatcher<SupportingDocument>() {
					@Override public boolean matches(SupportingDocument document) {
						return document.getDataStatusCode().equalsIgnoreCase("I");
					}
				}
			));
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}

	@Test
	public void saveOlaAnswersPlainWithDocuments_InactiveSupportingDocument() {
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc.toBuilder()
																			 .dataStatusCode("I")
																			 .build());
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);
		when(supportingDocumentRepository.save(Mockito.any(SupportingDocument.class)))
			.thenReturn(supportingDoc);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);
		
		Assertions.assertNotNull(olaSet);
	}

	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument() {
		Optional<SupportingDocument> documentsSet = Optional.empty();
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);
		
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);
		when(supportingDocumentRepository.save(Mockito.any(SupportingDocument.class)))
			.thenReturn(supportingDoc);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_DoesNotContainQCBS() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").questionTypeAnswerIndicator("N").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), COMPLETE);
		Assertions.assertNotNull(olaSet);
	}
		
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_DoesNotContainQCBSA() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), PENDING);
		Assertions.assertNotNull(olaSet);
	}
		
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_QCBSAQuestionTypeAnswerIndicatorNull() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), PENDING);
		Assertions.assertNotNull(olaSet);
	}
		
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_QCBSAAnswerY_QCBSBAnswerNull() {
		Optional<SupportingDocument> documentsSet = Optional.empty();
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);

		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);

		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").questionTypeAnswerIndicator("Y").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), PENDING);
		Assertions.assertNotNull(olaSet);
	}

	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_QCBSAAnswerYes_QCBSBAnswerY_SupportingDocumentsEmpty() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").questionTypeAnswerIndicator("Y").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").questionTypeAnswerIndicator("Y").supportingDocumentReferenceSet(new HashSet<>()).dataStatusCode("A").
				build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), PENDING);
		Assertions.assertNotNull(olaSet);
	}
		
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_QCBSAAnswerY_QCBSBAnswerN() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);

		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").questionTypeAnswerIndicator("Y").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").questionTypeAnswerIndicator("N").dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), COMPLETE);
		Assertions.assertNotNull(olaSet);
	}
		
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoSupportingDocument_QCBSAAnswerY_QCBSBAnswerY_SupportingDocumentsNotEmpty() {
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		contract.setSectionCode("CBS");
		contract.setSectionComplete(true);
		contract.getOlaTypeCodes().clear();
		olaTypeCodes.add("QCBS");
		olaTypeCodes.add("QCBSA");
		olaTypeCodes.add("QCBSB");
		contract.setOlaTypeCodes(olaTypeCodes);
		
		actualDocuments.clear();
		OlaAnswerWithDocument ans = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBS").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans);
		OlaAnswerWithDocument ans2 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSA").questionTypeAnswerIndicator("Y").dataStatusCode("Y").dataStatusCode("A").build();
		actualDocuments.add(ans2);
		OlaAnswerWithDocument ans3 = OlaAnswerWithDocument.builder().olaQuestionTypeCode("QCBSB").questionTypeAnswerIndicator("Y").supportingDocumentReferenceSet(supportingDocumenentReferences).
				dataStatusCode("A").build();
		actualDocuments.add(ans3);
		contract.setOlaAnswerAndDocumentsSet(actualDocuments);

		when(olaAnswerWithDocumentRepository.saveAll(Mockito.any())).thenReturn(actualDocuments);
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
																			  contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	@Test
	public void saveOlaAnswersPlainWithDocuments_NoBorrowerDocument() {
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc);
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.empty();

		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	
	@Test
	public void saveOlaAnswersPlainWithDocuments_existingAnswers() {
		Set<OlaAnswerWithDocument> existingAnswers = new HashSet<>();
		existingAnswers.add(OlaAnswerWithDocument.builder()
										  		 .applicationBorrowerIdentifier(500)
										  		 .answerText("olaAnswerPlains")
										  		 .olaAnswerIdentifier(100)
										  		 .creationDate(new Date())
										  		 .lastChangeDate(new Date(1710185928752L))
										  		 .olaQuestionTypeCode(OLA_TYPE_CODE)
										  		 .build());
		
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc);
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);

		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);
		when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.anySet()))
			.thenReturn(existingAnswers);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	@Test
	public void saveOlaAnswersPlainWithDocuments_OptimistLocking() {
		Set<OlaAnswerWithDocument> existingAnswers = new HashSet<>();
		existingAnswers.add(OlaAnswerWithDocument.builder()
										  		 .applicationBorrowerIdentifier(500)
										  		 .answerText("olaAnswerPlains")
										  		 .olaAnswerIdentifier(100)
										  		 .creationDate(new Date())
										  		 .lastChangeDate(new Date(1710185928752L))
										  		 .olaQuestionTypeCode(OLA_TYPE_CODE)
										  		 .build());
		
		Optional<SupportingDocument> documentsSet = Optional.of(supportingDoc);
		Optional<BorrowerDocument> borrowerDocumentOptional = Optional.of(borrowerDocument);

		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(supportingDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(documentsSet);
		when(borrowerDocumentRepository.findByStorageAddressText("Address"))
			.thenReturn(borrowerDocumentOptional);
		when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.anySet()))
			.thenReturn(existingAnswers);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);
		
		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	@Test
	public void saveOlaAnswersPlainWithDocuments_OptimistLockingFail() {
		Set<OlaAnswerWithDocument> existingAnswers = new HashSet<>();
		existingAnswers.add(OlaAnswerWithDocument.builder()
										  		 .applicationBorrowerIdentifier(500)
										  		 .answerText("olaAnswerPlains")
										  		 .olaAnswerIdentifier(100)
										  		 .creationDate(new Date())
										  		 .lastChangeDate(new Date())
										  		 .olaQuestionTypeCode(OLA_TYPE_CODE)
										  		 .build());

		contract.setOlaAnswerAndDocumentsSet(actualDocuments);
		
		when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.anySet()))
			.thenReturn(existingAnswers);

		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveOlaAnswerWithDocuments(contract);

		Assertions.assertNotNull(olaSet);
		Assertions.assertTrue(olaSet.isEmpty());
	}

	@Test
	public void saveTrainingExpEducation() {
		Set<OlaAnswerWithDocument> answersToSave = new HashSet<>();
		OlaAnswerWithDocument olaAnswerWithDocument =
			OlaAnswerWithDocument.builder()
								 .applicationBorrowerIdentifier(6653)
								 .olaAnswerIdentifier(100)
								 .dataStatusCode("A")
								 .olaQuestionTypeCode(ExperienceQuestionType.OPERATED_OWN_FARM.getCode())
								 .supportingDocumentReferenceSet(supportingDocumenentReferences)
								 .build();
		olaAnswerWithDocument.addFarmingExperienceEligibility(farmingExperienceEligibility);
		answersToSave.add(olaAnswerWithDocument);
		
		Set<FarmingExperienceEligibility> inactiveFarmingExperienceEligibilities = new HashSet<>();
		inactiveFarmingExperienceEligibilities.add(farmingExperienceEligibility);
		
		contract.setOlaAnswerAndDocumentsSet(answersToSave);
		
		when(olaAnswerWithDocumentRepository.saveAll(Mockito.anyIterable()))
			.thenReturn(answersToSave);
		when(farmingExperienceEligibilityRepository.findAllInactiveFarmingExperienceEligibilities(Mockito.anyInt()))
			.thenReturn(inactiveFarmingExperienceEligibilities);
		when(farmingExperienceEligibilityRepository.save(Mockito.any(FarmingExperienceEligibility.class)))
			.thenReturn(farmingExperienceEligibility);
		
		Set<OlaAnswerWithDocument> olaSet = questionAnswerServiceImpl.saveTrainingExpEducation(contract);

		verify(applicationSelectionStatusService, times(1)).saveSectionStatus(contract.getOlaToken(), 500,
				contract.getSectionCode(), COMPLETE);

		Assertions.assertNotNull(olaSet);
	}
	
	@Test
	public void saveCreditHistory() {
		Set<OlaAnswerWithDocument> olaAnswerWithDocuments = new HashSet<>();
		OlaAnswerWithDocument olaAnswerWithDocument =
				OlaAnswerWithDocument.builder()
				 					 .olaQuestionTypeCode(CreditHistoryQuestionType.BANKRUPTCY.getCode())
				 					 .olaAnswerIdentifier(100)
				 					 .build();
		
		olaAnswerWithDocument.addCreditHistoryEligibility(creditHistoryEligibility);
		olaAnswerWithDocuments.add(olaAnswerWithDocument);
		
		Set<CreditHistoryEligibility> inactiveCreditHistoryEligibilities = new HashSet<>();
		inactiveCreditHistoryEligibilities.add(creditHistoryEligibility);
		
		QuestionAnswerContract contract =
			QuestionAnswerContract.builder()
								  .olaAnswerAndDocumentsSet(olaAnswerWithDocuments)
								  .olaTypeCodes(olaTypeCodes)
								  .build();
		
		when(olaAnswerWithDocumentRepository.saveAll(Mockito.anyIterable()))
			.thenReturn(olaAnswerWithDocuments);
		when(creditHistoryEligibilityRepository.save(Mockito.any(CreditHistoryEligibility.class)))
			.thenReturn(creditHistoryEligibility);
		
		Set<OlaAnswerWithDocument> actual = questionAnswerServiceImpl.saveCreditHistory(contract);
		
		Assertions.assertNotNull(actual);
	}

	
	@Test
	public void sendParticipatingLender() {
		OlaAgencyToken pToken = new OlaAgencyToken();
		
		Set<String> olaQuestionTypeCode = new HashSet<>();
		olaQuestionTypeCode.add("PLEN");
		olaQuestionTypeCode.add("PFRE");
		olaQuestionTypeCode.add("PCCD");
		olaQuestionTypeCode.add("OTH");
		
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.set(1900, 12, 25, 59, 59, 59);
		
		Date loanDate = new Date(1721676509218L);
		Date existingDate = new Date(1721676509200L);
		Date creation = calendarNow.getTime();
		
		Set<OlaAnswerWithDocument> olaAnswerWithDocuments = new HashSet<>();
		ApplicationLoanPurpose appLoanPurpose = 
				ApplicationLoanPurpose.builder()
									.loanPurposeCategoryCode("OWLP")
									.loanPurposeTypeCode("PCCD")
									.loanPurposeSubTypeText("Purchase farm real estate")
									.loanRequestAmount(new BigDecimal(654))
									.loanPurposeTypeOtherDescription("")
									.dataStatusCode("A")
									.applicationIdentifier(100)
									.applicationLoanPurposeIdentifier(5)
									.lastChangeDate(loanDate)
									.build();
							
		OlaAnswerWithDocument olaAnswerWithDocument =
				OlaAnswerWithDocument.builder()
				 					 .olaQuestionTypeCode("PCCD")
				 					 .olaAnswerIdentifier(100)
				 					 .answerText("alskdfja;slkdfj")
				 					 .applicationBorrowerIdentifier(2)
				 					 .questionTypeAnswerIndicator("Y")
				 					 .dataStatusCode("A")
				 					 .creationDate(creation)
				 					 .lastChangeDate(loanDate)
				 					 .supportingDocumentReferenceSet(new HashSet<>())
				 					 .build();
		
		ParticipatingLender participatingLender = 
				ParticipatingLender.builder()
									.applicationLoanPurposeIdentifier(null)
									.lenderLoanAmount(BigDecimal.ONE)
									.lenderName("Tim")
									.dataStatusCode("A")
									.build();
		participatingLender.addQuestionSetType("PCCD");
		
		Map<String, ApplicationLoanPurpose> purposeMap = new HashMap<>();
		
		purposeMap.put("PCCD", appLoanPurpose);
		
		olaAnswerWithDocument.addParticipatingLender(participatingLender);
		olaAnswerWithDocuments.add(olaAnswerWithDocument);
		
		Set<ParticipatingLender> inactiveParticipatingLender = new HashSet<>();
		inactiveParticipatingLender.add(participatingLender);
		
		QuestionAnswerContract contract =
			QuestionAnswerContract.builder()
								  .olaAnswerAndDocumentsSet(olaAnswerWithDocuments)
								  .olaTypeCodes(olaQuestionTypeCode)
								  .applicationBorrowerIdentifier(2)
								  .applicationIdentifier(100)
								  .coreCustomerIdentifier(123)
								  .borrowerIdentifier(4)
								  .sectionCode(COMPLETE)
								  .olaToken(pToken)
								  .build();

		Set<OlaAnswerWithDocument> existingOlaAnswers = new HashSet<>();
		existingOlaAnswers.add(olaAnswerDocumentMaker("PCCD", "Y", "A", creation, existingDate, 100, participatingLender));	
		OlaAnswerWithDocument afterSavingAnswer = olaAnswerDocumentMaker("PCCD", "Y", "A", creation, existingDate, 100, null);
		
		Mockito.when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.any()))
				.thenReturn(existingOlaAnswers);
		Mockito.when(olaAnswerWithDocumentRepository.deleteQuestionAnswers(Mockito.anyInt(), Mockito.any())).thenReturn(true);
		Mockito.when(olaAnswerWithDocumentRepository.save(Mockito.any(OlaAnswerWithDocument.class))).thenReturn(afterSavingAnswer);
		Mockito.doNothing().when(applicationSelectionStatusService).saveSectionStatus(Mockito.any(OlaAgencyToken.class), Mockito.anyInt(), Mockito.any(), Mockito.any());
		Mockito.when(participatingLenderRepository.findAllParticipatingLenders(Mockito.anyInt())).thenReturn(new HashSet<>());;
		Mockito.when(participatingLenderRepository.saveAll(Mockito.anyIterable())).thenReturn(inactiveParticipatingLender);
		Boolean actual = questionAnswerServiceImpl.sendParticipatingLender(contract, purposeMap, pToken);
		Assertions.assertTrue(actual);
	}
	
	// Private methods introduced. 
	@Test
	public void sendParticipatingLender_Sets() {
		OlaAgencyToken pToken = new OlaAgencyToken();
		
		Set<String> olaQuestionTypeCode = new HashSet<>();
		olaQuestionTypeCode.add("PLEN");
		olaQuestionTypeCode.add("PFRE");
		olaQuestionTypeCode.add("PCCD");
		olaQuestionTypeCode.add("OTH");
		
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.set(1900, 12, 25, 59, 59, 59);
		
		Date loanDate = new Date(1721676509218L);
		Date existingDate = new Date(1721676509200L);
		Date creation = calendarNow.getTime();
		
		Set<OlaAnswerWithDocument> olaAnswerWithDocuments = new HashSet<>();
		Map<String, ApplicationLoanPurpose> purposeMap = new HashMap<>();
		
		purposeMap.put("PCCD", applicationLoanPurposeMaker("PCCD", 6, loanDate));
		purposeMap.put("PFRE", applicationLoanPurposeMaker("PFRE", 7, loanDate));
		purposeMap.put("OTH", applicationLoanPurposeMaker("OTH", 8, loanDate));
		purposeMap.put("PLEN", applicationLoanPurposeMaker("PLEN", 9, loanDate));
		
		ParticipatingLender participatingLender = 
				ParticipatingLender.builder()
									.applicationLoanPurposeIdentifier(null)
									.lenderLoanAmount(BigDecimal.ONE)
									.lenderName("Tim")
									.dataStatusCode("A")
									.build();
		participatingLender.addQuestionSetType("PCCD");
		
		lenderMaker(10, 6, "TIM", "PCCD", BigDecimal.valueOf(5.0), creation, loanDate);
			
		olaAnswerWithDocuments.add(olaAnswerDocumentMaker("PCCD", "Y", "A", creation, loanDate, 100, 
				lenderMaker(10, 6, "TIM", "PCCD", BigDecimal.valueOf(5.0), creation, loanDate)));
		olaAnswerWithDocuments.add(olaAnswerDocumentMaker("PFRE", "Y", "A", creation, loanDate, 100, 
				lenderMaker(10, 7, "TIM", "PFRE", BigDecimal.valueOf(5.0), creation, loanDate)));
		olaAnswerWithDocuments.add(olaAnswerDocumentMaker("OTH", "Y", "A", creation, loanDate, 100, 
				lenderMaker(10, 8, "TIM", "OTH", BigDecimal.valueOf(5.0), creation, loanDate)));
		olaAnswerWithDocuments.add(olaAnswerDocumentMaker("PLEN", "N", "A", creation, loanDate, 100, 
				lenderMaker(10, 9, "TIM", "PLEN", BigDecimal.valueOf(5.0), creation, loanDate)));
		
		Set<ParticipatingLender> inactiveParticipatingLender = new HashSet<>();
		inactiveParticipatingLender.add(participatingLender);
		
		QuestionAnswerContract contract =
			QuestionAnswerContract.builder()
								  .olaAnswerAndDocumentsSet(olaAnswerWithDocuments)
								  .olaTypeCodes(olaQuestionTypeCode)
								  .applicationBorrowerIdentifier(2)
								  .applicationIdentifier(100)
								  .coreCustomerIdentifier(123)
								  .borrowerIdentifier(4)
								  .sectionCode(COMPLETE)
								  .olaToken(pToken)
								  .build();

		Set<OlaAnswerWithDocument> existingOlaAnswers = new HashSet<>();
		existingOlaAnswers.add(olaAnswerDocumentMaker("PCCD", "Y", "A", creation, existingDate, 100, participatingLender));
		existingOlaAnswers.add(olaAnswerDocumentMaker("PFRE", "Y", "A", creation, existingDate, 100, participatingLender));
		existingOlaAnswers.add(olaAnswerDocumentMaker("OTH", "N", "A", creation, existingDate, 100, participatingLender));
		existingOlaAnswers.add(olaAnswerDocumentMaker("PLEN", "N", "A", creation, existingDate, 100, participatingLender));
		
		Optional<ParticipatingLender> LenderReturn1 = Optional.of(lenderMaker(10, 6, "TIM", "PCCD", BigDecimal.valueOf(5.0), creation, loanDate));
		
		Optional<OlaAnswerWithDocument> olaAnswerReturn = Optional.of(olaAnswerDocumentMaker("PFRE", "Y", "A", creation, existingDate, 100, null));
		
		Mockito.when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.any()))
				.thenReturn(existingOlaAnswers);
		Mockito.when(olaAnswerWithDocumentRepository.deleteQuestionAnswers(Mockito.anyInt(), Mockito.any())).thenReturn(true);
		Mockito.when(olaAnswerWithDocumentRepository.save(Mockito.any(OlaAnswerWithDocument.class)))
				.thenReturn(olaAnswerDocumentMaker("PCCD", "Y", "A", creation, existingDate, 100, null),
						olaAnswerDocumentMaker("PFRE", "Y", "A", creation, existingDate, 100, null), 
						olaAnswerDocumentMaker("OTH", "Y", "A", creation, existingDate, 100, null), 
						olaAnswerDocumentMaker("PLEN", "Y", "A", creation, existingDate, 100, null));
		

		Mockito.doNothing().when(applicationSelectionStatusService).saveSectionStatus(Mockito.any(OlaAgencyToken.class), Mockito.anyInt(), Mockito.any(), Mockito.any());

		Mockito.when(participatingLenderRepository.findById(Mockito.anyInt()))
			.thenReturn(LenderReturn1);
			//, LenderReturn2, LenderReturn3, LenderReturn4);
		Mockito.when(olaAnswerWithDocumentRepository.findById(Mockito.any())).thenReturn(olaAnswerReturn);
		//Mockito.when(participatingLenderRepository.delete(Mockito.anyInt())).thenReturn(true);
		
		Mockito.when(participatingLenderRepository.findAllParticipatingLenders(Mockito.anyInt())).thenReturn(new HashSet<>());;
		
		Mockito.when(participatingLenderRepository.saveAll(Mockito.anyIterable())).thenReturn(inactiveParticipatingLender);

		Boolean actual = questionAnswerServiceImpl.sendParticipatingLender(contract, purposeMap, pToken);
		
		Assertions.assertTrue(actual);
	}
	
	@Test
	public void sendParticipatingLender_Delete() {
		OlaAgencyToken pToken = new OlaAgencyToken();
		
		Set<String> olaQuestionTypeCode = new HashSet<>();
		olaQuestionTypeCode.add("PLEN");
		olaQuestionTypeCode.add("PFRE");
		olaQuestionTypeCode.add("PCCD");
		olaQuestionTypeCode.add("OTH");
		
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.set(1900, 12, 25, 59, 59, 59);
		
		Date loanDate = new Date(1721676509218L);
		Date existingDate = new Date(1721676509200L);
		Date creation = calendarNow.getTime();
		
		Set<OlaAnswerWithDocument> olaAnswerWithDocuments = new HashSet<>();
		Map<String, ApplicationLoanPurpose> purposeMap = new HashMap<>();
		
		purposeMap.put("PCCD", applicationLoanPurposeMaker("PCCD", 6, loanDate));
		
		ParticipatingLender participatingLender = 
				ParticipatingLender.builder()
									.applicationLoanPurposeIdentifier(null)
									.lenderLoanAmount(BigDecimal.ONE)
									.lenderName("Tim")
									.dataStatusCode("A")
									.build();
		participatingLender.addQuestionSetType("PCCD");
			
		olaAnswerWithDocuments.add(olaAnswerDocumentMaker("PCCD", "N", "A", creation, loanDate, 100, 
				lenderMaker(10, 6, "TIM", "PCCD", BigDecimal.valueOf(5.0), creation, loanDate)));
		
		Set<ParticipatingLender> inactiveParticipatingLender = new HashSet<>();
		inactiveParticipatingLender.add(participatingLender);
		
		QuestionAnswerContract contract =
			QuestionAnswerContract.builder()
								  .olaAnswerAndDocumentsSet(olaAnswerWithDocuments)
								  .olaTypeCodes(olaQuestionTypeCode)
								  .applicationBorrowerIdentifier(2)
								  .applicationIdentifier(100)
								  .coreCustomerIdentifier(123)
								  .borrowerIdentifier(4)
								  .sectionCode(COMPLETE)
								  .olaToken(pToken)
								  .build();

		Set<OlaAnswerWithDocument> existingOlaAnswers = new HashSet<>();
		existingOlaAnswers.add(olaAnswerDocumentMaker("PCCD", "N", "A", creation, existingDate, 100, participatingLender));
		
		Optional<ParticipatingLender> LenderReturn1 = Optional.of(lenderMaker(10, 6, "TIM", "PCCD", BigDecimal.valueOf(5.0), creation, loanDate));
		
		Optional<OlaAnswerWithDocument> olaAnswerReturn = Optional.of(olaAnswerDocumentMaker("PCCD", "N", "A", creation, existingDate, 100, null));
		
		Mockito.when(olaAnswerWithDocumentRepository.findAllOlaAnswersByCodes(Mockito.anyInt(), Mockito.any()))
				.thenReturn(existingOlaAnswers);
		Mockito.when(olaAnswerWithDocumentRepository.deleteQuestionAnswers(Mockito.anyInt(), Mockito.any())).thenReturn(true);
		Mockito.when(olaAnswerWithDocumentRepository.save(Mockito.any(OlaAnswerWithDocument.class)))
				.thenReturn(olaAnswerDocumentMaker("PCCD", "N", "A", creation, existingDate, 100, null));
		

		Mockito.doNothing().when(applicationSelectionStatusService).saveSectionStatus(Mockito.any(OlaAgencyToken.class), Mockito.anyInt(), Mockito.any(), Mockito.any());
		Mockito.when(participatingLenderRepository.findById(Mockito.anyInt())).thenReturn(LenderReturn1);
		Mockito.when(olaAnswerWithDocumentRepository.findById(Mockito.any())).thenReturn(olaAnswerReturn);
		Mockito.doNothing().when(participatingLenderRepository).delete(Mockito.any(ParticipatingLender.class));
		//Mockito.when(participatingLenderRepository.findAllParticipatingLenders(Mockito.anyInt())).thenReturn(new HashSet<>());;
		
		Mockito.when(participatingLenderRepository.saveAll(Mockito.anyIterable())).thenReturn(inactiveParticipatingLender);

		Boolean actual = questionAnswerServiceImpl.sendParticipatingLender(contract, purposeMap, pToken);
		
		Assertions.assertTrue(actual);
	}
	
	
	
	@Test
	public void sendParticipatingLender_Contract_Empty() {
		OlaAgencyToken pToken = new OlaAgencyToken();
		
		Set<OlaAnswerWithDocument> olaAnswerWithDocuments = new HashSet<>();
		Map<String, ApplicationLoanPurpose> purposeMap = new HashMap<>();
				
		QuestionAnswerContract contract =
			QuestionAnswerContract.builder()
								  .olaAnswerAndDocumentsSet(olaAnswerWithDocuments)
								  .olaTypeCodes(new HashSet<>())
								  .applicationBorrowerIdentifier(2)
								  .applicationIdentifier(100)
								  .coreCustomerIdentifier(123)
								  .borrowerIdentifier(4)
								  .sectionCode(COMPLETE)
								  .olaToken(pToken)
								  .build();


		Boolean actual = questionAnswerServiceImpl.sendParticipatingLender(contract, purposeMap, pToken);
		Assertions.assertTrue(actual);
	}
	
	private ParticipatingLender lenderMaker(Integer id, Integer appLoanPurpose, String name, String questionType, BigDecimal amount, Date creation, Date modify ) { 
		
		ParticipatingLender participatingLender = 
				ParticipatingLender.builder()
						.participatingLenderIdentifier(id)
						.applicationLoanPurposeIdentifier(appLoanPurpose)
						.lenderLoanAmount(amount)
						.lenderName(name)
						.dataStatusCode("A")
						.creationDate(creation)
						.lastChangeDate(modify)
						.build();
		participatingLender.addQuestionSetType(questionType);
		return participatingLender;
		
	}
	
	private OlaAnswerWithDocument olaAnswerDocumentMaker(String typeCode, String questionIndicator, String statusCode, Date creation, Date modify, Integer id, 
			ParticipatingLender lender) {
		
		OlaAnswerWithDocument result = OlaAnswerWithDocument.builder()
				 .olaQuestionTypeCode(typeCode)
				 .olaAnswerIdentifier(id)
				 .answerText("alskdfja;slkdfj")
				 .applicationBorrowerIdentifier(2)
				 .questionTypeAnswerIndicator(questionIndicator)
				 .dataStatusCode(statusCode)
				 .lastChangeDate(modify)
				 .creationDate(creation)
				 .supportingDocumentReferenceSet(supportingDocumenentReferences)
				 .build();
		if(lender!=null) {
			result.addParticipatingLender(lender);
		}
		return result;
		
	}
	
	private ApplicationLoanPurpose applicationLoanPurposeMaker(String typeCode, Integer loanPurposeId, Date lastChange) {
		return ApplicationLoanPurpose.builder()
				.loanPurposeCategoryCode("OWLP")
				.loanPurposeTypeCode(typeCode)
				.loanPurposeSubTypeText("Purchase farm real estate")
				.loanRequestAmount(new BigDecimal(654))
				.loanPurposeTypeOtherDescription("")
				.dataStatusCode("A")
				.applicationIdentifier(100)
				.applicationLoanPurposeIdentifier(loanPurposeId)
				.lastChangeDate(lastChange)
				.build();
	}
	
}