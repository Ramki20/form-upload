package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;

@Table(schema = "qstn_ans", name = "oper_prfl_cat")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
public class OperationProfileCategoryPlain extends AuditedEntity {

	@Id
	@Column("oper_prfl_cat_id")
	private Integer operationProfileCategoryIdentifier;

	@NotNull(message = "Application Borrower Identifier is mandatory.")
	@Column("app_borr_id")
	private Integer applicationBorrowerIdentifier;

	@NotBlank(message = "Data status code is mandatory.")
	@Column("data_stat_cd")
	private String dataStatusCode;

	@NotBlank(message = "Enterprise type code is mandatory.")
	@Column("entp_type_cd")
	private String enterpriseTypeCode;

	@NotBlank(message = "Operation category code is mandatory.")
	@Column("oper_cat_cd")
	private String operationCategoryCode;

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
	private Set<OperationProfileSubCategory> operationProfileSubCategorySet = new HashSet<>();

	public void addOperationProfileSubCategory(OperationProfileSubCategory operationProfileSubCategory) {

		operationProfileSubCategorySet.add(operationProfileSubCategory);
	}

}