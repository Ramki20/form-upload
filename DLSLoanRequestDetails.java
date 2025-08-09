package gov.usda.fsa.fcao.flp.ola.core.api.sub.model;

import java.math.BigDecimal;
import java.util.Date;

public class DLSLoanRequestDetails {

	private Integer directLoanRequestIdentifier;
	private String directLoanRequestTypeCode;
	private BigDecimal loanRequestAmount;
	private BigDecimal loanApprovedAmount;
	private Date firstIncompleteApplicationLetterSendDate;
	private Date secondIncompleteApplicationLetterSendDate;
	private BigDecimal obligationAmount;
	private String flpAssistanceTypeCode;
	private String directLoanProgramCode;
	private String loanRequestStatusCode;
	private Date loanRequestStatusDate;
	private String loanNumber;
	private String requestAppealStatusCode;
	
	public Integer getDirectLoanRequestIdentifier() {
		return directLoanRequestIdentifier;
	}
	public void setDirectLoanRequestIdentifier(Integer directLoanRequestIdentifier) {
		this.directLoanRequestIdentifier = directLoanRequestIdentifier;
	}
	public String getDirectLoanRequestTypeCode() {
		return directLoanRequestTypeCode;
	}
	public void setDirectLoanRequestTypeCode(String directLoanRequestTypeCode) {
		this.directLoanRequestTypeCode = directLoanRequestTypeCode;
	}
	public BigDecimal getLoanRequestAmount() {
		return loanRequestAmount;
	}
	public void setLoanRequestAmount(BigDecimal loanRequestAmount) {
		this.loanRequestAmount = loanRequestAmount;
	}
	public BigDecimal getLoanApprovedAmount() {
		return loanApprovedAmount;
	}
	public void setLoanApprovedAmount(BigDecimal loanApprovedAmount) {
		this.loanApprovedAmount = loanApprovedAmount;
	}
	public Date getFirstIncompleteApplicationLetterSendDate() {
		return firstIncompleteApplicationLetterSendDate;
	}
	public void setFirstIncompleteApplicationLetterSendDate(Date firstIncompleteApplicationLetterSendDate) {
		this.firstIncompleteApplicationLetterSendDate = firstIncompleteApplicationLetterSendDate;
	}
	public Date getSecondIncompleteApplicationLetterSendDate() {
		return secondIncompleteApplicationLetterSendDate;
	}
	public void setSecondIncompleteApplicationLetterSendDate(Date secondIncompleteApplicationLetterSendDate) {
		this.secondIncompleteApplicationLetterSendDate = secondIncompleteApplicationLetterSendDate;
	}
	public BigDecimal getObligationAmount() {
		return obligationAmount;
	}
	public void setObligationAmount(BigDecimal obligationAmount) {
		this.obligationAmount = obligationAmount;
	}
	public String getFlpAssistanceTypeCode() {
		return flpAssistanceTypeCode;
	}
	public void setFlpAssistanceTypeCode(String flpAssistanceTypeCode) {
		this.flpAssistanceTypeCode = flpAssistanceTypeCode;
	}
	public String getDirectLoanProgramCode() {
		return directLoanProgramCode;
	}
	public void setDirectLoanProgramCode(String directLoanProgramCode) {
		this.directLoanProgramCode = directLoanProgramCode;
	}
	public String getLoanRequestStatusCode() {
		return loanRequestStatusCode;
	}
	public void setLoanRequestStatusCode(String loanRequestStatusCode) {
		this.loanRequestStatusCode = loanRequestStatusCode;
	}
	public Date getLoanRequestStatusDate() {
		return loanRequestStatusDate;
	}
	public void setLoanRequestStatusDate(Date loanRequestStatusDate) {
		this.loanRequestStatusDate = loanRequestStatusDate;
	}
	public String getLoanNumber() {
		return loanNumber;
	}
	public void setLoanNumber(String loanNumber) {
		this.loanNumber = loanNumber;
	}
	public String getRequestAppealStatusCode() {
		return requestAppealStatusCode;
	}
	public void setRequestAppealStatusCode(String requestAppealStatusCode) {
		this.requestAppealStatusCode = requestAppealStatusCode;
	}
	@Override
	public String toString() {
		return "LoanRequestDetails [directLoanRequestIdentifier=" + directLoanRequestIdentifier
				+ ", directLoanRequestTypeCode=" + directLoanRequestTypeCode + ", loanRequestAmount="
				+ loanRequestAmount + ", loanApprovedAmount=" + loanApprovedAmount
				+ ", firstIncompleteApplicationLetterSendDate=" + firstIncompleteApplicationLetterSendDate
				+ ", secondIncompleteApplicationLetterSendDate=" + secondIncompleteApplicationLetterSendDate
				+ ", obligationAmount=" + obligationAmount + ", flpAssistanceTypeCode=" + flpAssistanceTypeCode
				+ ", directLoanProgramCode=" + directLoanProgramCode + ", loanRequestStatusCode="
				+ loanRequestStatusCode + ", loanRequestStatusDate=" + loanRequestStatusDate + ", loanNumber="
				+ loanNumber + ", requestAppealStatusCode=" + requestAppealStatusCode + "]";
	}
	
	
}
