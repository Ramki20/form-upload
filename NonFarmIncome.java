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

@Table(schema = "cash_flow", name = "nfarm_incm")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
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
	@Builder.Default
	private Set<NonFarmIncomeSupportingDocument> supportingDocumentReferenceSet = new HashSet<>();

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
				.map(NonFarmIncomeSupportingDocument::getSupportingDocumentIdentifier).collect(Collectors.toSet());
	}
	
	public void addSupportingDocument(SupportingDocument supportingDocument) {

		supportingDocuments.add(supportingDocument);
	}

	public void addSupportingDocumentToBeRemoved(SupportingDocument supportingDocument) {

		removedSupportingDocuments.add(supportingDocument);
	}

	@Transient
	private Set<SupportingDocument> supportingDocuments = new HashSet<>();

	@Transient
	private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();
	
}
