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

@Table(schema = "bal_sht", name = "bal_sht_ot_lia")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
// Remove @Builder annotation
public class BalanceSheetOtherLiability extends AuditedEntity {

    @Id
    @Column("bal_sht_ot_lia_id")
    private Integer balanceSheetOtherLiabilityIdentifier;

    @NotNull(message = "Borrower balancesheet identifier is mandatory.")
    @Column("borr_app_bal_sht_id")
    private Integer borrowerApplicationBalanceSheetIdentifier;

    @NotBlank(message = "Creditor name is mandatory.")
    @Column("cr_nm")
    private String creditorName;

    @NotBlank(message = "Liability type code is mandatory.")
    @Column("lia_type_cd")
    private String liabilityTypeCode;

    @NotBlank(message = "Loan purpose description is mandatory.")
    @Column("loan_prps_desc")
    private String loanPurposeDescription;

    @NotNull(message = "Principal balance amount is mandatory.")
    @Column("prn_bal_amt")
    private BigDecimal principalBalanceAmount;

    @Column("int_rt")
    private BigDecimal interestRate;

    @NotNull(message = "Loan payment amount is mandatory.")
    @Column("loan_pymt_amt")
    private BigDecimal loanPaymentAmount;

    @Column("anl_istl_pymt_ct")
    private Integer annualInstallmentPaymentCount;

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

    @MappedCollection(idColumn = "bal_sht_ot_lia_id", keyColumn = "sprt_doc_id")
    @Valid
    private Set<SupportingDocOtherLiabilityReference> supportingDocumentReferenceSet = new HashSet<>();

    // Transient fields
    @Transient
    private Set<SupportingDocument> supportingDocuments = new HashSet<>();

    @Transient
    private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

    // Default constructor for Spring Data JDBC
    public BalanceSheetOtherLiability() {
        this.supportingDocumentReferenceSet = new HashSet<>();
        this.supportingDocuments = new HashSet<>();
        this.removedSupportingDocuments = new HashSet<>();
    }

    // Builder pattern implementation
    public static BalanceSheetOtherLiabilityBuilder builder() {
        return new BalanceSheetOtherLiabilityBuilder();
    }

    public BalanceSheetOtherLiabilityBuilder toBuilder() {
        return new BalanceSheetOtherLiabilityBuilder(this);
    }

    // Custom builder class with only persistent fields
    public static class BalanceSheetOtherLiabilityBuilder {
        private BalanceSheetOtherLiability instance;

        public BalanceSheetOtherLiabilityBuilder() {
            this.instance = new BalanceSheetOtherLiability();
        }

        public BalanceSheetOtherLiabilityBuilder(BalanceSheetOtherLiability existing) {
            this.instance = new BalanceSheetOtherLiability();
            // Copy persistent fields from existing instance
            this.instance.setBalanceSheetOtherLiabilityIdentifier(existing.getBalanceSheetOtherLiabilityIdentifier());
            this.instance.setBorrowerApplicationBalanceSheetIdentifier(existing.getBorrowerApplicationBalanceSheetIdentifier());
            this.instance.setCreditorName(existing.getCreditorName());
            this.instance.setLiabilityTypeCode(existing.getLiabilityTypeCode());
            this.instance.setLoanPurposeDescription(existing.getLoanPurposeDescription());
            this.instance.setPrincipalBalanceAmount(existing.getPrincipalBalanceAmount());
            this.instance.setInterestRate(existing.getInterestRate());
            this.instance.setLoanPaymentAmount(existing.getLoanPaymentAmount());
            this.instance.setAnnualInstallmentPaymentCount(existing.getAnnualInstallmentPaymentCount());
            this.instance.setDataStatusCode(existing.getDataStatusCode());
            this.instance.setCreationDate(existing.getCreationDate());
            this.instance.setCreationUserName(existing.getCreationUserName());
            this.instance.setLastChangeDate(existing.getLastChangeDate());
            this.instance.setLastChangeUserName(existing.getLastChangeUserName());
            // Copy collections
            this.instance.setSupportingDocumentReferenceSet(new HashSet<>(existing.getSupportingDocumentReferenceSet()));
        }

        // Basic field builders (I'll include key ones - add more as needed)
        public BalanceSheetOtherLiabilityBuilder balanceSheetOtherLiabilityIdentifier(Integer balanceSheetOtherLiabilityIdentifier) {
            instance.setBalanceSheetOtherLiabilityIdentifier(balanceSheetOtherLiabilityIdentifier);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder borrowerApplicationBalanceSheetIdentifier(Integer borrowerApplicationBalanceSheetIdentifier) {
            instance.setBorrowerApplicationBalanceSheetIdentifier(borrowerApplicationBalanceSheetIdentifier);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder creditorName(String creditorName) {
            instance.setCreditorName(creditorName);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder liabilityTypeCode(String liabilityTypeCode) {
            instance.setLiabilityTypeCode(liabilityTypeCode);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder loanPurposeDescription(String loanPurposeDescription) {
            instance.setLoanPurposeDescription(loanPurposeDescription);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder principalBalanceAmount(BigDecimal principalBalanceAmount) {
            instance.setPrincipalBalanceAmount(principalBalanceAmount);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder interestRate(BigDecimal interestRate) {
            instance.setInterestRate(interestRate);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder loanPaymentAmount(BigDecimal loanPaymentAmount) {
            instance.setLoanPaymentAmount(loanPaymentAmount);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder annualInstallmentPaymentCount(Integer annualInstallmentPaymentCount) {
            instance.setAnnualInstallmentPaymentCount(annualInstallmentPaymentCount);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder dataStatusCode(String dataStatusCode) {
            instance.setDataStatusCode(dataStatusCode);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder creationDate(Date creationDate) {
            instance.setCreationDate(creationDate);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder creationUserName(String creationUserName) {
            instance.setCreationUserName(creationUserName);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder lastChangeDate(Date lastChangeDate) {
            instance.setLastChangeDate(lastChangeDate);
            return this;
        }

        public BalanceSheetOtherLiabilityBuilder lastChangeUserName(String lastChangeUserName) {
            instance.setLastChangeUserName(lastChangeUserName);
            return this;
        }

        // Collection builder
        public BalanceSheetOtherLiabilityBuilder supportingDocumentReferenceSet(Set<SupportingDocOtherLiabilityReference> supportingDocumentReferenceSet) {
            instance.setSupportingDocumentReferenceSet(supportingDocumentReferenceSet != null ? supportingDocumentReferenceSet : new HashSet<>());
            return this;
        }

        // Build method
        public BalanceSheetOtherLiability build() {
            return instance;
        }
    }

    // Keep your existing methods
    public void createSupportingDocument(@Valid SupportingDocument supportingDocument) {
        supportingDocumentReferenceSet.add(SupportingDocOtherLiabilityReference.builder()
                .supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
                .lastChangeDate(supportingDocument.getLastChangeDate())
                .lastChangeUserName(supportingDocument.getLastChangeUserName())
                .creationDate(supportingDocument.getCreationDate())
                .creationUserName(supportingDocument.getCreationUserName())
                .dataStatusCode(supportingDocument.getDataStatusCode()).build());
    }

    public Set<Integer> getAllSupportingDocumentIdentifiers() {
        return this.supportingDocumentReferenceSet.stream()
                .map(SupportingDocOtherLiabilityReference::getSupportingDocumentIdentifier)
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