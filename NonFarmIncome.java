package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.math.BigDecimal;
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

@Table(schema = "cash_flow", name = "nfarm_incm")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class NonFarmIncome extends AuditedEntity {

    @Id
    @Column("nfarm_incm_id")
    private Integer nonfarmIncomeIdentifier;

    @NotNull(message = "borrower cash flow identifier is mandatory.")
    @Column("borr_cash_flow_id")
    private Integer borrowerCashFlowIdentifier;

    @NotBlank(message = "Data status code is mandatory.")
    @Column("data_stat_cd")
    private String dataStatusCode;

    @NotBlank(message = "Nonfarm income code is mandatory.")
    @Column("nfarm_incm_cd")
    private String nonfarmIncomeCode;

    @NotNull(message = "income amount is mandatory.")
    @Column("incm_amt")
    private BigDecimal incomeAmount;

    @NotBlank(message = "Nonfarm income other description is mandatory.")
    @Column("nfarm_incm_ot_desc")
    private String nonfarmIncomeOtherDescription;

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

    @MappedCollection(idColumn = "nfarm_incm_id", keyColumn = "sprt_doc_id")
    private Set<NonFarmIncomeSupportingDocument> supportingDocumentReferenceSet = new HashSet<>();

    // Transient fields
    @Transient
    private Set<SupportingDocument> supportingDocuments = new HashSet<>();

    @Transient
    private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public NonFarmIncome() {
        this.supportingDocumentReferenceSet = new HashSet<>();
        this.supportingDocuments = new HashSet<>();
        this.removedSupportingDocuments = new HashSet<>();
    }

    // Builder pattern implementation
    public static NonFarmIncomeBuilder builder() {
        return new NonFarmIncomeBuilder();
    }

    public NonFarmIncomeBuilder toBuilder() {
        return new NonFarmIncomeBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class NonFarmIncomeBuilder {
        private NonFarmIncome instance;

        public NonFarmIncomeBuilder() {
            this.instance = new NonFarmIncome();
        }

        public NonFarmIncomeBuilder(NonFarmIncome existing) {
            this.instance = new NonFarmIncome();
            // Copy persistent fields from existing instance
            this.instance.setNonfarmIncomeIdentifier(existing.getNonfarmIncomeIdentifier());
            this.instance.setBorrowerCashFlowIdentifier(existing.getBorrowerCashFlowIdentifier());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setNonfarmIncomeCode(existing.getNonfarmIncomeCode());
            this.instance.setIncomeAmount(existing.getIncomeAmount());
            this.instance.setNonfarmIncomeOtherDescription(existing.getNonfarmIncomeOtherDescription());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
            // Copy collections
            this.instance.setSupportingDocumentReferenceSet(new HashSet<>(existing.getSupportingDocumentReferenceSet()));
        }

        // Basic field builders
        public NonFarmIncomeBuilder nonfarmIncomeIdentifier(Integer nonfarmIncomeIdentifier) {
            instance.setNonfarmIncomeIdentifier(nonfarmIncomeIdentifier);
            return this;
        }

        public NonFarmIncomeBuilder borrowerCashFlowIdentifier(Integer borrowerCashFlowIdentifier) {
            instance.setBorrowerCashFlowIdentifier(borrowerCashFlowIdentifier);
            return this;
        }

        public NonFarmIncomeBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public NonFarmIncomeBuilder nonfarmIncomeCode(String nonfarmIncomeCode) {
            instance.setNonfarmIncomeCode(nonfarmIncomeCode);
            return this;
        }

        public NonFarmIncomeBuilder incomeAmount(BigDecimal incomeAmount) {
            instance.setIncomeAmount(incomeAmount);
            return this;
        }

        public NonFarmIncomeBuilder nonfarmIncomeOtherDescription(String nonfarmIncomeOtherDescription) {
            instance.setNonfarmIncomeOtherDescription(nonfarmIncomeOtherDescription);
            return this;
        }

        public NonFarmIncomeBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public NonFarmIncomeBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public NonFarmIncomeBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public NonFarmIncomeBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Collection builder
        public NonFarmIncomeBuilder supportingDocumentReferenceSet(Set<NonFarmIncomeSupportingDocument> supportingDocumentReferenceSet) {
            instance.setSupportingDocumentReferenceSet(supportingDocumentReferenceSet != null ? supportingDocumentReferenceSet : new HashSet<>());
            return this;
        }

        // Build method
        public NonFarmIncome build() {
            return instance;
        }
    }

    // Keep your existing methods
    public void createSupportingDocument(SupportingDocument supportingDocument) {
        supportingDocumentReferenceSet.add(NonFarmIncomeSupportingDocument.builder()
                .supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
                .lastChangeDate(supportingDocument.getLastChangeDate())
                .lastChangeUserName(supportingDocument.getLastChangeUserName())
                .creationDate(supportingDocument.getCreationDate())
                .creationUserName(supportingDocument.getCreationUserName())
                .dataStatusCode(supportingDocument.getDataStatusCode()).build());
    }

    public Set<Integer> getAllSupportingDocumentIdentifiers() {
        return this.supportingDocumentReferenceSet.stream()
                .map(NonFarmIncomeSupportingDocument::getSupportingDocumentIdentifier)
                .collect(Collectors.toSet());
    }

    public void addSupportingDocument(SupportingDocument supportingDocument) {
        if (this.supportingDocuments == null) {
            this.supportingDocuments = new HashSet<>();
        }
        supportingDocuments.add(supportingDocument);
    }

    public void addSupportingDocumentToBeRemoved(SupportingDocument supportingDocument) {
        if (this.removedSupportingDocuments == null) {
            this.removedSupportingDocuments = new HashSet<>();
        }
        removedSupportingDocuments.add(supportingDocument);
    }
}