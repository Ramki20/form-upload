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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "app_base", name = "app_loan_prps")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)

public class ApplicationLoanPurposePlain extends AuditedEntity {

	@Id
	@Column("app_loan_prps_id")
	private Integer applicationLoanPurposeIdentifier;

	@NotNull(message = "Application identifier is mandatory.")
	@Column("app_id")
	private Integer applicationIdentifier;

	@NotBlank(message = "Loan purpose type code is mandatory.")
	@Column("loan_prps_type_cd")
	private String loanPurposeTypeCode;

	@Column("loan_prps_type_ot_desc")
	private String loanPurposeTypeOtherDescription;

	@NotBlank(message = "Data status code is mandatory.")
	@Column("data_stat_cd")
	private String dataStatusCode;

	@NotNull(message = "Loan request amount is mandatory.")
	@Column("loan_rqst_amt")
	private BigDecimal loanRequestAmount;

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

	@NotBlank(message = "Loan purpose category code is mandatory.")
	@Column("loan_prps_cat_cd")
	private String loanPurposeCategoryCode;

	@NotBlank(message = "Loan purpose sub-type text is mandatory.")
	@Column("loan_prps_sub_type_txt")
	private String loanPurposeSubTypeText;

	@Column("cr_nm_txt")
	private String creditorNameText;

	@Column("lvstk_head_ct")
	private Integer livestockHeadCount;

	@MappedCollection(idColumn = "app_loan_prps_id", keyColumn = "sprt_doc_id")
	@Builder.Default
	private Set<SupportingDocumentLoanPurposeReference> supportingDocumentReferenceSet = new HashSet<>();

	public void createSupportingDocument(SupportingDocument supportingDocument) {

		supportingDocumentReferenceSet.add(SupportingDocumentLoanPurposeReference.builder()
				.supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
				.lastChangeDate(supportingDocument.getLastChangeDate())
				.lastChangeUserName(supportingDocument.getLastChangeUserName())
				.creationDate(supportingDocument.getCreationDate())
				.creationUserName(supportingDocument.getCreationUserName())
				.dataStatusCode(supportingDocument.getDataStatusCode()).build());

	}

	public Set<Integer> getAllSupportingDocumentIdentifiers() {

		return this.supportingDocumentReferenceSet.stream()
				.map(SupportingDocumentLoanPurposeReference::getSupportingDocumentIdentifier).collect(Collectors.toSet());
	}

	@Transient
	private Set<SupportingDocument> supportingDocuments = new HashSet<>();

	@Transient
	private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

	public void addSupportingDocument(SupportingDocument supportingDocument) {

		supportingDocuments.add(supportingDocument);
	}

	public void addSupportingDocumentToBeRemoved(SupportingDocument supportingDocument) {

		removedSupportingDocuments.add(supportingDocument);
	}

}
