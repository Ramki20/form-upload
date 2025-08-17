package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;

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

@Table(schema = "qstn_ans", name = "ptcp_lndr")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class ParticipatingLender extends AuditedEntity {

    @Id
    @Column("ptcp_lndr_id")
    private Integer participatingLenderIdentifier;

    @NotNull(message = "Ola Answer identifier is mandatory.")
    @Column("ola_ans_id")
    private Integer olaAnswerIdentifier;

    @NotNull(message = "Lender Name is mandatory.")
    @Column("lndr_nm")
    private String lenderName;

    @NotNull(message = "Lender Loan Amount identifier is mandatory.")
    @Column("lndr_loan_amt")
    private BigDecimal lenderLoanAmount;

    @NotNull(message = "application_loan_purpose_identifier is mandatory.")
    @Column("app_loan_prps_id")
    private Integer applicationLoanPurposeIdentifier;

    @NotNull(message = "Data status code is mandatory.")
    @Column("data_stat_cd")
    private String dataStatusCode;

    @NotNull(message = "Creation date is mandatory.")
    @Column("cre_dt")
    private Date creationDate;

    @NotNull(message = "Creation user name is mandatory.")
    @Column("cre_user_nm")
    private String creationUserName;

    @NotNull(message = "Last change date is mandatory.")
    @Column("last_chg_dt")
    private Date lastChangeDate;

    @NotNull(message = "Last change user name is mandatory.")
    @Column("last_chg_user_nm")
    private String lastChangeUserName;

    // Transient fields
    @Transient
    private Set<String> questionTypeSet = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public ParticipatingLender() {
        this.questionTypeSet = new HashSet<>();
    }

    // Builder pattern implementation
    public static ParticipatingLenderBuilder builder() {
        return new ParticipatingLenderBuilder();
    }

    public ParticipatingLenderBuilder toBuilder() {
        return new ParticipatingLenderBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class ParticipatingLenderBuilder {
        private ParticipatingLender instance;

        public ParticipatingLenderBuilder() {
            this.instance = new ParticipatingLender();
        }

        public ParticipatingLenderBuilder(ParticipatingLender existing) {
            this.instance = new ParticipatingLender();
            // Copy persistent fields from existing instance
            this.instance.setParticipatingLenderIdentifier(existing.getParticipatingLenderIdentifier());
            this.instance.setOlaAnswerIdentifier(existing.getOlaAnswerIdentifier());
            this.instance.setLenderName(existing.getLenderName());
            this.instance.setLenderLoanAmount(existing.getLenderLoanAmount());
            this.instance.setApplicationLoanPurposeIdentifier(existing.getApplicationLoanPurposeIdentifier());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
        }

        // Basic field builders
        public ParticipatingLenderBuilder participatingLenderIdentifier(Integer participatingLenderIdentifier) {
            instance.setParticipatingLenderIdentifier(participatingLenderIdentifier);
            return this;
        }

        public ParticipatingLenderBuilder olaAnswerIdentifier(Integer olaAnswerIdentifier) {
            instance.setOlaAnswerIdentifier(olaAnswerIdentifier);
            return this;
        }

        public ParticipatingLenderBuilder lenderName(String lenderName) {
            instance.setLenderName(lenderName);
            return this;
        }

        public ParticipatingLenderBuilder lenderLoanAmount(BigDecimal lenderLoanAmount) {
            instance.setLenderLoanAmount(lenderLoanAmount);
            return this;
        }

        public ParticipatingLenderBuilder applicationLoanPurposeIdentifier(Integer applicationLoanPurposeIdentifier) {
            instance.setApplicationLoanPurposeIdentifier(applicationLoanPurposeIdentifier);
            return this;
        }

        public ParticipatingLenderBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public ParticipatingLenderBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public ParticipatingLenderBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public ParticipatingLenderBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public ParticipatingLenderBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Build method
        public ParticipatingLender build() {
            return instance;
        }
    }

    // Keep your existing methods
    public void addQuestionSetType(String question) {
        if (this.questionTypeSet == null) {
            this.questionTypeSet = new HashSet<>();
        }
        questionTypeSet.add(question);
    }
}