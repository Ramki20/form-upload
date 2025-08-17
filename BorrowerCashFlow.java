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

@Table(schema = "cash_flow", name = "borr_cash_flow")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
public class BorrowerCashFlow extends AuditedEntity {

	@Id
	@Column("borr_cash_flow_id")
	private Integer borrowerCashFlowIdentifier;

	@NotNull(message = "Application borrower identifier is mandatory.")
	@Column("app_borr_id")
	private Integer applicationBorrowerIdentifier;

	@NotBlank(message = "Cash flow source type code is mandatory.")
	@Column("cash_flow_src_type_cd")
	private String cashFlowSourceTypeCode;

	@NotNull(message = "Cash flow scenario name is mandatory.")
	@Column("cash_flow_scnr_nm")
	private String cashFlowScenarioName;

	@NotNull(message = "Cash flow submission method type code.")
	@Column("cash_flow_sbms_mthd_type_cd")
	private String cashFlowSubmissionMethodTypeCode;
	
	@Column("prod_cycl_strt_dt")
	private Date productionCycleStartDate;
	
	@Column("prod_cycl_end_dt")
	private Date productionCycleEndDate;

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

	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<NonFarmIncome> nonFarmIncomeSet = new HashSet<>();
	
	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowCrop> cropSet = new HashSet<>();

	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowLivestockPoultry> livestockPoultrySet = new HashSet<>();

	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowLivestockProduct> livestockProductSet = new HashSet<>();

	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowMilk> milkSet = new HashSet<>();
	
	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowMiscellaneousIncome> miscellaneousIncomeSet = new HashSet<>();
	
	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowNonFarmExpense> nonFarmExpenseSet = new HashSet<>();
	
	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowCapitalExpenditure> capitalExpenditureSet = new HashSet<>();

	
	@Builder.Default
	@MappedCollection(idColumn = "borr_cash_flow_id")
	private Set<CashFlowFarmOperatingExpense> farmOperatingExpenseSet = new HashSet<>();
	
	@MappedCollection(idColumn = "borr_cash_flow_id", keyColumn = "sprt_doc_id")
	@Builder.Default
	private Set<SupportingDocumentCashFlowReference> supportingDocumentReferenceSet = new HashSet<>();

	public void createSupportingDocument(SupportingDocument supportingDocument) {

		supportingDocumentReferenceSet.add(SupportingDocumentCashFlowReference.builder()
				.supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
				.lastChangeDate(supportingDocument.getLastChangeDate())
				.lastChangeUserName(supportingDocument.getLastChangeUserName())
				.creationDate(supportingDocument.getCreationDate())
				.creationUserName(supportingDocument.getCreationUserName())
				.dataStatusCode(supportingDocument.getDataStatusCode()).build());

	}

	public Set<Integer> getAllSupportingDocumentIdentifiers() {

		return this.supportingDocumentReferenceSet.stream()
				.map(SupportingDocumentCashFlowReference::getSupportingDocumentIdentifier).collect(Collectors.toSet());
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
