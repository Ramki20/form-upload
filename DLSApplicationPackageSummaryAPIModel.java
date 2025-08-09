package gov.usda.fsa.fcao.flp.ola.core.api.model;

import java.util.Date;
import java.util.List;

import gov.usda.fsa.fcao.flp.ola.core.api.sub.model.DLSLoanRequestDetails;

public class DLSApplicationPackageSummaryAPIModel implements IRestAPIModel {

	private Integer applicationPackageIdentifier;
	private Date applicationPackageReceiveDate;
	private Integer onlineApplicationNumber;
	private String errorMessage;
	
	private List<DLSLoanRequestDetails> dlsLoanRequestDetailsList;

	public Integer getApplicationPackageIdentifier() {
		return applicationPackageIdentifier;
	}

	public void setApplicationPackageIdentifier(Integer applicationPackageIdentifier) {
		this.applicationPackageIdentifier = applicationPackageIdentifier;
	}

	public Date getApplicationPackageReceiveDate() {
		return applicationPackageReceiveDate;
	}

	public void setApplicationPackageReceiveDate(Date applicationPackageReceiveDate) {
		this.applicationPackageReceiveDate = applicationPackageReceiveDate;
	}
	
	public Integer getOnlineApplicationNumber() {
		return onlineApplicationNumber;
	}

	public void setOnlineApplicationNumber(Integer onlineApplicationNumber) {
		this.onlineApplicationNumber = onlineApplicationNumber;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public List<DLSLoanRequestDetails> getDlsLoanRequestDetailsList() {
		return dlsLoanRequestDetailsList;
	}

	public void setDlsLoanRequestDetailsList(List<DLSLoanRequestDetails> dlsLoanRequestDetailsList) {
		this.dlsLoanRequestDetailsList = dlsLoanRequestDetailsList;
	}

	@Override
	public String toString() {
		return "DLSApplicationPackageSummaryAPIModel [applicationPackageIdentifier=" + applicationPackageIdentifier
				+ ", applicationPackageReceiveDate=" + applicationPackageReceiveDate + ", onlineApplicationNumber="
				+ onlineApplicationNumber + ", errorMessage=" + errorMessage + ", dlsLoanRequestDetailsList="
				+ dlsLoanRequestDetailsList + "]";
	}
	
}
