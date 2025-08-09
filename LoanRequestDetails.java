package gov.usda.fsa.fcao.flp.ola.core.shared.service.model;

import java.math.BigDecimal;

/**
 * Loan Request Details entity from Farm Loan Gateway Service
 */
public class LoanRequestDetails {
    private Boolean dirty;
    private Boolean activeIndicator;
    private Boolean lightWeightInd;
    private String directLoanProgramCode;
    private String directLoanRequestIdentifier;
    private String directLoanRequestTypeCode;
    private String firstIncompleteApplicationLetterSendDate;
    private String secondIncompleteApplicationLetterSendDate;
    private String flpAssistanceTypeCode;
    private BigDecimal loanApprovedAmount;
    private String loanNumber;
    private BigDecimal loanRequestAmount;
    private String loanRequestStatusCode;
    private String loanRequestStatusDate;
    private BigDecimal obligationAmount;
    private String requestAppealStatusCode;

    // Constructors
    public LoanRequestDetails() {}

    // Getters and setters
    public Boolean getDirty() {
        return dirty;
    }

    public void setDirty(Boolean dirty) {
        this.dirty = dirty;
    }

    public Boolean getActiveIndicator() {
        return activeIndicator;
    }

    public void setActiveIndicator(Boolean activeIndicator) {
        this.activeIndicator = activeIndicator;
    }

    public Boolean getLightWeightInd() {
        return lightWeightInd;
    }

    public void setLightWeightInd(Boolean lightWeightInd) {
        this.lightWeightInd = lightWeightInd;
    }

    public String getDirectLoanProgramCode() {
        return directLoanProgramCode;
    }

    public void setDirectLoanProgramCode(String directLoanProgramCode) {
        this.directLoanProgramCode = directLoanProgramCode;
    }

    public String getDirectLoanRequestIdentifier() {
        return directLoanRequestIdentifier;
    }

    public void setDirectLoanRequestIdentifier(String directLoanRequestIdentifier) {
        this.directLoanRequestIdentifier = directLoanRequestIdentifier;
    }

    public String getDirectLoanRequestTypeCode() {
        return directLoanRequestTypeCode;
    }

    public void setDirectLoanRequestTypeCode(String directLoanRequestTypeCode) {
        this.directLoanRequestTypeCode = directLoanRequestTypeCode;
    }

    public String getFirstIncompleteApplicationLetterSendDate() {
        return firstIncompleteApplicationLetterSendDate;
    }

    public void setFirstIncompleteApplicationLetterSendDate(String firstIncompleteApplicationLetterSendDate) {
        this.firstIncompleteApplicationLetterSendDate = firstIncompleteApplicationLetterSendDate;
    }
    
    public String getSecondIncompleteApplicationLetterSendDate() {
		return secondIncompleteApplicationLetterSendDate;
	}

	public void setSecondIncompleteApplicationLetterSendDate(String secondIncompleteApplicationLetterSendDate) {
		this.secondIncompleteApplicationLetterSendDate = secondIncompleteApplicationLetterSendDate;
	}

	public String getFlpAssistanceTypeCode() {
        return flpAssistanceTypeCode;
    }

    public void setFlpAssistanceTypeCode(String flpAssistanceTypeCode) {
        this.flpAssistanceTypeCode = flpAssistanceTypeCode;
    }

    public BigDecimal getLoanApprovedAmount() {
        return loanApprovedAmount;
    }

    public void setLoanApprovedAmount(BigDecimal loanApprovedAmount) {
        this.loanApprovedAmount = loanApprovedAmount;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public BigDecimal getLoanRequestAmount() {
        return loanRequestAmount;
    }

    public void setLoanRequestAmount(BigDecimal loanRequestAmount) {
        this.loanRequestAmount = loanRequestAmount;
    }

    public String getLoanRequestStatusCode() {
        return loanRequestStatusCode;
    }

    public void setLoanRequestStatusCode(String loanRequestStatusCode) {
        this.loanRequestStatusCode = loanRequestStatusCode;
    }

    public String getLoanRequestStatusDate() {
        return loanRequestStatusDate;
    }

    public void setLoanRequestStatusDate(String loanRequestStatusDate) {
        this.loanRequestStatusDate = loanRequestStatusDate;
    }

    public BigDecimal getObligationAmount() {
        return obligationAmount;
    }

    public void setObligationAmount(BigDecimal obligationAmount) {
        this.obligationAmount = obligationAmount;
    }

    public String getRequestAppealStatusCode() {
        return requestAppealStatusCode;
    }

    public void setRequestAppealStatusCode(String requestAppealStatusCode) {
        this.requestAppealStatusCode = requestAppealStatusCode;
    }
}