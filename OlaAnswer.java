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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "qstn_ans", name = "ola_ans")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class OlaAnswer extends AuditedEntity {

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
    private Set<SupportingDocumentReference> supportingDocumentReferenceSet = new HashSet<>();

    @MappedCollection(idColumn = "ola_ans_id")
    private Set<FarmingExperienceEligibility> farmingExperienceEligibilitySet = new HashSet<>();

    @MappedCollection(idColumn = "ola_ans_id")
    private Set<CreditHistoryEligibility> creditHistoryEligibilitySet = new HashSet<>();

    @MappedCollection(idColumn = "ola_ans_id")
    private Set<SociallyDisadvantagedApplicantEligibility> sociallyDisadvantagedApplicantEligibilitySet = new HashSet<>();

    @MappedCollection(idColumn = "ola_ans_id")
    private Set<ParticipatingLender> participatingLenderSet = new HashSet<>();

    // Transient fields - Note: Changed from static to instance field
    @Transient
    private Set<SupportingDocument> olaAnswerSupportingDocuments = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public OlaAnswer() {
        this.supportingDocumentReferenceSet = new HashSet<>();
        this.farmingExperienceEligibilitySet = new HashSet<>();
        this.creditHistoryEligibilitySet = new HashSet<>();
        this.sociallyDisadvantagedApplicantEligibilitySet = new HashSet<>();
        this.participatingLenderSet = new HashSet<>();
        this.olaAnswerSupportingDocuments = new HashSet<>();
    }

    // Builder pattern implementation
    public static OlaAnswerBuilder builder() {
        return new OlaAnswerBuilder();
    }

    public OlaAnswerBuilder toBuilder() {
        return new OlaAnswerBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class OlaAnswerBuilder {
        private OlaAnswer instance;

        public OlaAnswerBuilder() {
            this.instance = new OlaAnswer();
        }

        public OlaAnswerBuilder(OlaAnswer existing) {
            this.instance = new OlaAnswer();
            // Copy persistent fields from existing instance
            this.instance.setOlaAnswerIdentifier(existing.getOlaAnswerIdentifier());
            this.instance.setApplicationBorrowerIdentifier(existing.getApplicationBorrowerIdentifier());
            this.instance.setOlaQuestionTypeCode(existing.getOlaQuestionTypeCode());
            this.instance.setQuestionTypeAnswerIndicator(existing.getQuestionTypeAnswerIndicator());
            this.instance.setAnswerText(existing.getAnswerText());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
            // Copy collections
            this.instance.setSupportingDocumentReferenceSet(new HashSet<>(existing.getSupportingDocumentReferenceSet()));
            this.instance.setFarmingExperienceEligibilitySet(new HashSet<>(existing.getFarmingExperienceEligibilitySet()));
            this.instance.setCreditHistoryEligibilitySet(new HashSet<>(existing.getCreditHistoryEligibilitySet()));
            this.instance.setSociallyDisadvantagedApplicantEligibilitySet(new HashSet<>(existing.getSociallyDisadvantagedApplicantEligibilitySet()));
            this.instance.setParticipatingLenderSet(new HashSet<>(existing.getParticipatingLenderSet()));
        }

        // Basic field builders
        public OlaAnswerBuilder olaAnswerIdentifier(Integer olaAnswerIdentifier) {
            instance.setOlaAnswerIdentifier(olaAnswerIdentifier);
            return this;
        }

        public OlaAnswerBuilder applicationBorrowerIdentifier(Integer applicationBorrowerIdentifier) {
            instance.setApplicationBorrowerIdentifier(applicationBorrowerIdentifier);
            return this;
        }

        public OlaAnswerBuilder olaQuestionTypeCode(String olaQuestionTypeCode) {
            instance.setOlaQuestionTypeCode(olaQuestionTypeCode);
            return this;
        }

        public OlaAnswerBuilder questionTypeAnswerIndicator(String questionTypeAnswerIndicator) {
            instance.setQuestionTypeAnswerIndicator(questionTypeAnswerIndicator);
            return this;
        }

        public OlaAnswerBuilder answerText(String answerText) {
            instance.setAnswerText(answerText);
            return this;
        }

        public OlaAnswerBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public OlaAnswerBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public OlaAnswerBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public OlaAnswerBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public OlaAnswerBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Collection builders
        public OlaAnswerBuilder supportingDocumentReferenceSet(Set<SupportingDocumentReference> supportingDocumentReferenceSet) {
            instance.setSupportingDocumentReferenceSet(supportingDocumentReferenceSet != null ? supportingDocumentReferenceSet : new HashSet<>());
            return this;
        }

        public OlaAnswerBuilder farmingExperienceEligibilitySet(Set<FarmingExperienceEligibility> farmingExperienceEligibilitySet) {
            instance.setFarmingExperienceEligibilitySet(farmingExperienceEligibilitySet != null ? farmingExperienceEligibilitySet : new HashSet<>());
            return this;
        }

        public OlaAnswerBuilder creditHistoryEligibilitySet(Set<CreditHistoryEligibility> creditHistoryEligibilitySet) {
            instance.setCreditHistoryEligibilitySet(creditHistoryEligibilitySet != null ? creditHistoryEligibilitySet : new HashSet<>());
            return this;
        }

        public OlaAnswerBuilder sociallyDisadvantagedApplicantEligibilitySet(Set<SociallyDisadvantagedApplicantEligibility> sociallyDisadvantagedApplicantEligibilitySet) {
            instance.setSociallyDisadvantagedApplicantEligibilitySet(sociallyDisadvantagedApplicantEligibilitySet != null ? sociallyDisadvantagedApplicantEligibilitySet : new HashSet<>());
            return this;
        }

        public OlaAnswerBuilder participatingLenderSet(Set<ParticipatingLender> participatingLenderSet) {
            instance.setParticipatingLenderSet(participatingLenderSet != null ? participatingLenderSet : new HashSet<>());
            return this;
        }

        // Build method
        public OlaAnswer build() {
            return instance;
        }
    }

    // Keep your existing methods
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
                .map(SupportingDocumentReference::getSupportingDocumentIdentifier)
                .collect(Collectors.toSet());
    }

    public void addSupportingDocument(SupportingDocument supportingDocument) {
        if (this.olaAnswerSupportingDocuments == null) {
            this.olaAnswerSupportingDocuments = new HashSet<>();
        }
        olaAnswerSupportingDocuments.add(supportingDocument);
    }
}	