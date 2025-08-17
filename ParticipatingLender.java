package gov.usda.fsa.fcao.flp.ola.core.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;

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

@Table(schema = "qstn_ans", name = "ptcp_lndr")
@EqualsAndHashCode(callSuper = false)
@ToString(includeFieldNames = true)
@Data
@Builder(toBuilder = true)
public class ParticipatingLender extends AuditedEntity {
	
	@Id
	@Column("ptcp_lndr_id")
	private Integer participatingLenderIdentifier;

	@NotNull(message = "Ola Answer identifier is mandatory.")
	@Column("ola_ans_id")
	private Integer olaAnswerIdentifier;
	
	@NotNull(message = "Lender Name is mandatory.")
	@Column("lndr_nm")
	private String lenderName;
	
	@NotNull(message = "Lender Loan Amount identifier is mandatory.")
	@Column("lndr_loan_amt")
	private BigDecimal lenderLoanAmount;
	
	@NotNull(message = "application_loan_purpose_identifier is mandatory.")
	@Column("app_loan_prps_id")
	private Integer applicationLoanPurposeIdentifier;
	
	@NotNull(message = "Data status code is mandatory.")
	@Column("data_stat_cd")
	private String dataStatusCode;

	@NotNull(message = "Creation date is mandatory.")
	@Column("cre_dt")
	private Date creationDate;

	@NotNull(message = "Creation user name is mandatory.")
	@Column("cre_user_nm")
	private String creationUserName;

	@NotNull(message = "Last change date is mandatory.")
	@Column("last_chg_dt")
	private Date lastChangeDate;

	@NotNull(message = "Last change user name is mandatory.")
	@Column("last_chg_user_nm")
	private String lastChangeUserName;

	@Transient
	private Set<String> questionTypeSet = new HashSet<>();

	public void addQuestionSetType(String question) {

		questionTypeSet.add(question);
	}	
	
	
}
