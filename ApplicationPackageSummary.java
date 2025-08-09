package gov.usda.fsa.fcao.flp.ola.core.shared.service.model;

import java.util.List;

/**
 * Application Package Summary entity from Farm Loan Gateway Service
 */
public class ApplicationPackageSummary {
    private Boolean dirty;
    private Boolean activeIndicator;
    private Boolean lightWeightInd;
    private String applicationPackageIdentifier;
    private String applicationPackageReceiveDate;
    private String onlineApplicationNumber;
    private List<LoanRequestDetails> loanRequestDetailsList;

    // Constructors
    public ApplicationPackageSummary() {}

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

    public String getApplicationPackageIdentifier() {
        return applicationPackageIdentifier;
    }

    public void setApplicationPackageIdentifier(String applicationPackageIdentifier) {
        this.applicationPackageIdentifier = applicationPackageIdentifier;
    }

    public String getApplicationPackageReceiveDate() {
        return applicationPackageReceiveDate;
    }

    public void setApplicationPackageReceiveDate(String applicationPackageReceiveDate) {
        this.applicationPackageReceiveDate = applicationPackageReceiveDate;
    }

    public String getOnlineApplicationNumber() {
        return onlineApplicationNumber;
    }

    public void setOnlineApplicationNumber(String onlineApplicationNumber) {
        this.onlineApplicationNumber = onlineApplicationNumber;
    }

    public List<LoanRequestDetails> getLoanRequestDetailsList() {
        return loanRequestDetailsList;
    }

    public void setLoanRequestDetailsList(List<LoanRequestDetails> loanRequestDetailsList) {
        this.loanRequestDetailsList = loanRequestDetailsList;
    }
}
