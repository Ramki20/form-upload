package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
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

@Table(schema = "bal_sht", name = "bal_sht_due_lia")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class BalanceSheetDueLiability extends AuditedEntity {

    @Id
    @Column("bal_sht_due_lia_id")
    private Integer balanceSheetDueLiabilityIdentifier;

    @NotNull(message = "Borrower balancesheet identifier is mandatory.")
    @Column("borr_app_bal_sht_id")
    private Integer borrowerApplicationBalanceSheetIdentifier;

    @NotBlank(message = "Due liability type code is mandatory.")
    @Column("due_lia_type_cd")
    private String dueLiabilityTypeCode;

    @Column("incm_tax_type_cd")
    private String incomeTaxTypeCode;

    @NotBlank(message = "Due liability description is mandatory.")
    @Column("due_lia_desc")
    private String dueLiabilityDescription;

    @NotNull(message = "Liability amount is mandatory.")
    @Column("lia_amt")
    private BigDecimal liabilityAmount;

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

    @MappedCollection(idColumn = "bal_sht_due_lia_id", keyColumn = "sprt_doc_id")
    @Valid
    private Set<SupportingDocDueLiabilityReference> supportingDocumentReferenceSet = new HashSet<>();

    // Transient fields
    @Transient
    private Set<SupportingDocument> supportingDocuments = new HashSet<>();

    @Transient
    private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public BalanceSheetDueLiability() {
        this.supportingDocumentReferenceSet = new HashSet<>();
        this.supportingDocuments = new HashSet<>();
        this.removedSupportingDocuments = new HashSet<>();
    }

    // Builder pattern implementation
    public static BalanceSheetDueLiabilityBuilder builder() {
        return new BalanceSheetDueLiabilityBuilder();
    }

    public BalanceSheetDueLiabilityBuilder toBuilder() {
        return new BalanceSheetDueLiabilityBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class BalanceSheetDueLiabilityBuilder {
        private BalanceSheetDueLiability instance;

        public BalanceSheetDueLiabilityBuilder() {
            this.instance = new BalanceSheetDueLiability();
        }

        public BalanceSheetDueLiabilityBuilder(BalanceSheetDueLiability existing) {
            this.instance = new BalanceSheetDueLiability();
            // Copy persistent fields from existing instance
            this.instance.setBalanceSheetDueLiabilityIdentifier(existing.getBalanceSheetDueLiabilityIdentifier());
            this.instance.setBorrowerApplicationBalanceSheetIdentifier(existing.getBorrowerApplicationBalanceSheetIdentifier());
            this.instance.setDueLiabilityTypeCode(existing.getDueLiabilityTypeCode());
            this.instance.setIncomeTaxTypeCode(existing.getIncomeTaxTypeCode());
            this.instance.setDueLiabilityDescription(existing.getDueLiabilityDescription());
            this.instance.setLiabilityAmount(existing.getLiabilityAmount());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
            // Copy collections
            this.instance.setSupportingDocumentReferenceSet(new HashSet<>(existing.getSupportingDocumentReferenceSet()));
        }

        // Basic field builders
        public BalanceSheetDueLiabilityBuilder balanceSheetDueLiabilityIdentifier(Integer balanceSheetDueLiabilityIdentifier) {
            instance.setBalanceSheetDueLiabilityIdentifier(balanceSheetDueLiabilityIdentifier);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder borrowerApplicationBalanceSheetIdentifier(Integer borrowerApplicationBalanceSheetIdentifier) {
            instance.setBorrowerApplicationBalanceSheetIdentifier(borrowerApplicationBalanceSheetIdentifier);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder dueLiabilityTypeCode(String dueLiabilityTypeCode) {
            instance.setDueLiabilityTypeCode(dueLiabilityTypeCode);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder incomeTaxTypeCode(String incomeTaxTypeCode) {
            instance.setIncomeTaxTypeCode(incomeTaxTypeCode);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder dueLiabilityDescription(String dueLiabilityDescription) {
            instance.setDueLiabilityDescription(dueLiabilityDescription);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder liabilityAmount(BigDecimal liabilityAmount) {
            instance.setLiabilityAmount(liabilityAmount);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public BalanceSheetDueLiabilityBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Collection builder
        public BalanceSheetDueLiabilityBuilder supportingDocumentReferenceSet(Set<SupportingDocDueLiabilityReference> supportingDocumentReferenceSet) {
            instance.setSupportingDocumentReferenceSet(supportingDocumentReferenceSet != null ? supportingDocumentReferenceSet : new HashSet<>());
            return this;
        }

        // Build method
        public BalanceSheetDueLiability build() {
            return instance;
        }
    }

    // Keep your existing methods
    public void createSupportingDocument(@Valid SupportingDocument supportingDocument) {
        supportingDocumentReferenceSet.add(SupportingDocDueLiabilityReference.builder()
                .supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
                .lastChangeDate(supportingDocument.getLastChangeDate())
                .lastChangeUserName(supportingDocument.getLastChangeUserName())
                .creationDate(supportingDocument.getCreationDate())
                .creationUserName(supportingDocument.getCreationUserName())
                .dataStatusCode(supportingDocument.getDataStatusCode()).build());
    }

    public Set<Integer> getAllSupportingDocumentIdentifiers() {
        return this.supportingDocumentReferenceSet.stream()
                .map(SupportingDocDueLiabilityReference::getSupportingDocumentIdentifier)
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