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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "bal_sht", name = "bal_sht_ot_lia")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
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
	@Builder.Default
	@Valid
	private Set<SupportingDocOtherLiabilityReference> supportingDocumentReferenceSet = new HashSet<>();

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
				.map(SupportingDocOtherLiabilityReference::getSupportingDocumentIdentifier).collect(Collectors.toSet());
	}

	@Transient
	private Set<SupportingDocument> supportingDocuments = new HashSet<>();

	@Transient
	private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

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
