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

@Table(schema = "bal_sht", name = "bal_sht_due_lia")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
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
	@Builder.Default
	@Valid
	private Set<SupportingDocDueLiabilityReference> supportingDocumentReferenceSet = new HashSet<>();

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
				.map(SupportingDocDueLiabilityReference::getSupportingDocumentIdentifier).collect(Collectors.toSet());
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
