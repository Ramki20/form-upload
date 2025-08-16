package gov.usda.fsa.fcao.flp.ola.core.entity;

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

@Table(schema = "app_base", name = "app_borr")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
public class ApplicationBorrower extends AuditedEntity {

	@Id
	@Column("app_borr_id")
	private Integer applicationBorrowerIdentifier;

	@NotNull(message = "Application identifier is mandatory.")
	@Column("app_id")
	private Integer applicationIdentifier;

	@Column("borr_id")
	private Integer borrowerIdentifier;

	@Column("core_cust_id")
	private Integer coreCustomerIdentifier;

	@Column("loan_rlt_type_cd")
	private String loanRelationshipTypeCode;

//	@Column("fbp_data_submission_status_code")
//	private String fbpDataSubmissionStatusCode;

	@NotBlank(message = "Data status code is mandatory.")
	@Column("data_stat_cd")
	private String dataStatusCode;

	@Column("res_st_flp_cd")
	private String residentStateFlpCode;

	@Column("res_cnty_flp_cd")
	private String residentCountyFlpCode;

	@Column("email_adr")
	private String emailAddress;
	
	@Column("cnfrm_email_adr")
	private String confirmationEmailAddress;

	@Column("loan_app_mrtl_stat_cd")
	private String maritalStatusCode;

	@Column("bus_rgst_st_abr")
	private String businessRegistrationStateAbbreviation;

	@Column("bus_rgst_nbr")
	private String businessRegistrationNumber;

	@Column("bus_form_dt")
	private Date businessFormationDate;

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
	
	@Transient
	private Set<SupportingDocument> supportingDocuments = new HashSet<>();

	@Transient
	private Set<SupportingDocument> removedSupportingDocuments = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id", keyColumn = "sprt_doc_id")
	@Builder.Default
	private Set<SupportingDocumentReferenceApplicationBorrower> supportingDocumentReferenceSet = new HashSet<>();
	
	public void createSupportingDocument(SupportingDocument supportingDocument) {
		supportingDocumentReferenceSet.add(SupportingDocumentReferenceApplicationBorrower.builder()
				.supportingDocumentIdentifier(supportingDocument.getSupportingDocumentIdentifier())
				.lastChangeDate(supportingDocument.getLastChangeDate())
				.lastChangeUserName(supportingDocument.getLastChangeUserName())
				.creationDate(supportingDocument.getCreationDate())
				.creationUserName(supportingDocument.getCreationUserName())
				.dataStatusCode(supportingDocument.getDataStatusCode()).build());
	}
	
	public Set<Integer> getAllSupportingDocumentIdentifiers() {

		return this.supportingDocumentReferenceSet.stream()
				.map(SupportingDocumentReferenceApplicationBorrower::getSupportingDocumentIdentifier)
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
	
	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<OlaSignature> olaSignatureSet = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<ReleaseAuthorization> releaseAuthorizationSet = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<DisclosureCertification> disclosureCertificationSet = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<ApplicationSelectionStatus> applicationSelectionStatusSet = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<OlaAnswer> olaAnswerSet = new HashSet<>();

	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<BorrowerApplicationBalanceSheet> balanceSheetSet = new HashSet<>();

	public void addBorrowerApplicationBalanceSheet(BorrowerApplicationBalanceSheet borrowerApplicationBalanceSheet) {
		this.balanceSheetSet.add(borrowerApplicationBalanceSheet);

	}

	public void addOlaAnswer(OlaAnswer answer) {

		this.olaAnswerSet.add(answer);

	}

	public void addOlaSignature(OlaSignature signature) {
		this.olaSignatureSet.add(signature);

	}

	public void addReleaseAuthorization(ReleaseAuthorization releaseAuthorization) {
		this.releaseAuthorizationSet.add(releaseAuthorization);
		
	}

	public void addApplicationSelectionStatus(ApplicationSelectionStatus applicationSelectionStatus) {
		this.applicationSelectionStatusSet.add(applicationSelectionStatus);

	}
	
	@Valid
	@MappedCollection(idColumn = "app_borr_id")
	@Builder.Default
	private Set<OperationProfileCategory> operationProfileCategorySet = new HashSet<>();
	
	
	public void addOperationProfileCategory(OperationProfileCategory operationProfileCategory) {
		this.operationProfileCategorySet.add(operationProfileCategory);

	}

}
