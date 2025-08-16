package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "qstn_ans", name = "ola_ans")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
public class OlaAnswerWithDocument extends AuditedEntity {

	@Id
	@Column("ola_ans_id")
	private Integer olaAnswerIdentifier;

	@NotNull(message = "Application borrower identifier is mandatory.")
	@Column("app_borr_id")
	private Integer applicationBorrowerIdentifier;

	@NotBlank(message = "ola question type code is mandatory.")
	@Column("ola_qstn_type_cd")
	private String olaQuestionTypeCode;

	@Column("qstn_type_ans_ind")
	private String questionTypeAnswerIndicator;

	@Column("ans_txt")
	private String answerText;

	@NotBlank(message = "Data status code is mandatory.")
	@Column("data_stat_cd")
	private String dataStatusCode;

	@NotNull(message = "Creation date is mandatory.")
	@Column("cre_dt")
	private Date creationDate;

	@NotBlank(message = "Creation user name is mandatory.")
	@Column("cre_user_nm")
	private String creationUserName;

	@NotNull(message = "Last change date is mandatory.")
	@Column("last_chg_dt")
	private Date lastChangeDate;

	@NotBlank(message = "Last change user name is mandatory.")
	@Column("last_chg_user_nm")
	private String lastChangeUserName;

	@MappedCollection(idColumn = "ola_ans_id", keyColumn = "sprt_doc_id")
	@Builder.Default
	private Set<SupportingDocumentReference> supportingDocumentReferenceSet = new HashSet<>();

	public void createSupportingDocument(SupportingDocument supportingDocument) {

		supportingDocumentReferenceSet.add(SupportingDocumentReference.builder()
				.supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
				.lastChangeDate(supportingDocument.getLastChangeDate())
				.lastChangeUserName(supportingDocument.getLastChangeUserName())
				.creationDate(supportingDocument.getCreationDate())
				.creationUserName(supportingDocument.getCreationUserName())
				.dataStatusCode(supportingDocument.getDataStatusCode()).build());

	}

	public Set<Integer> getAllSupportingDocumentIdentifiers() {

		return this.supportingDocumentReferenceSet.stream()
				.map(SupportingDocumentReference::getSupportingDocumentIdentifier).collect(Collectors.toSet());
	}

	@Transient
	private Set<SupportingDocument> olaAnswerSupportingDocuments = new HashSet<>();

	@Transient
	private Set<SupportingDocument> olaAnswerRemovedSupportingDocuments = new HashSet<>();

	@Transient
	private Set<CreditHistoryEligibility> creditHistoryEligibilitySet = new HashSet<>();

	@Transient
	private Set<FarmingExperienceEligibility> farmingExperienceEligibilitySet = new HashSet<>();
	
	@Transient
	private Set<ParticipatingLender> participatingLenderSet = new HashSet<>();

	public void addSupportingDocument(SupportingDocument supportingDocument) {

		olaAnswerSupportingDocuments.add(supportingDocument);
	}

	public void addSupportingDocumentToBeRemoved(SupportingDocument supportingDocument) {

		olaAnswerRemovedSupportingDocuments.add(supportingDocument);
	}

	public void addCreditHistoryEligibility(CreditHistoryEligibility creditHistoryEligibility) {

		creditHistoryEligibilitySet.add(creditHistoryEligibility);
	}
	
	public void addParticipatingLender(ParticipatingLender participatingLender) {

		participatingLenderSet.add(participatingLender);
	}

	public void addFarmingExperienceEligibility(FarmingExperienceEligibility farmingExperienceEligibility) {

		farmingExperienceEligibilitySet.add(farmingExperienceEligibility);
	}

}
