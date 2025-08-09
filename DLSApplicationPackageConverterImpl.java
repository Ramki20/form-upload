package gov.usda.fsa.fcao.flp.ola.core.api.converter.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.usda.fsa.fcao.flp.ola.core.api.converter.IDLSApplicationPackageConverter;
import gov.usda.fsa.fcao.flp.ola.core.api.model.DLSApplicationPackageSummaryAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.api.sub.model.DLSLoanRequestDetails;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApplicationPackageSummary;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.LoanRequestDetails;

@Component
public class DLSApplicationPackageConverterImpl implements IDLSApplicationPackageConverter {

	@Override
	public DLSApplicationPackageSummaryAPIModel convert(ApplicationPackageSummary applicationPackageSummary) {
		
		DLSApplicationPackageSummaryAPIModel appPkgSummaryAPIModel = new DLSApplicationPackageSummaryAPIModel();
		appPkgSummaryAPIModel.setApplicationPackageIdentifier(Integer.parseInt(applicationPackageSummary.getApplicationPackageIdentifier()));
		appPkgSummaryAPIModel.setApplicationPackageReceiveDate(OlaServiceUtil.getDate(applicationPackageSummary.getApplicationPackageReceiveDate()));
		appPkgSummaryAPIModel.setOnlineApplicationNumber(Integer.parseInt(applicationPackageSummary.getOnlineApplicationNumber()));
		
		List<DLSLoanRequestDetails> loanRequestDetailsList = new ArrayList<>();
		
		for(LoanRequestDetails loanReqDetails : applicationPackageSummary.getLoanRequestDetailsList())
		{
			DLSLoanRequestDetails loanRequestDetails = new DLSLoanRequestDetails();
			loanRequestDetails.setDirectLoanProgramCode(loanReqDetails.getDirectLoanProgramCode());
			loanRequestDetails.setDirectLoanRequestIdentifier(Integer.parseInt(loanReqDetails.getDirectLoanRequestIdentifier()));
			loanRequestDetails.setDirectLoanRequestTypeCode(loanReqDetails.getDirectLoanRequestTypeCode());
			loanRequestDetails.setFirstIncompleteApplicationLetterSendDate(OlaServiceUtil.getDate(loanReqDetails.getFirstIncompleteApplicationLetterSendDate()));
			loanRequestDetails.setFlpAssistanceTypeCode(loanReqDetails.getFlpAssistanceTypeCode());
			loanRequestDetails.setLoanApprovedAmount(loanReqDetails.getLoanApprovedAmount());
			loanRequestDetails.setLoanRequestAmount(loanReqDetails.getLoanRequestAmount());
			loanRequestDetails.setLoanNumber(loanReqDetails.getLoanNumber());
			loanRequestDetails.setLoanRequestStatusCode(loanReqDetails.getLoanRequestStatusCode());
			loanRequestDetails.setLoanRequestStatusDate(OlaServiceUtil.getDate(loanReqDetails.getLoanRequestStatusDate()));
			loanRequestDetails.setObligationAmount(loanReqDetails.getObligationAmount());
			loanRequestDetails.setRequestAppealStatusCode(loanReqDetails.getRequestAppealStatusCode());
			loanRequestDetails.setSecondIncompleteApplicationLetterSendDate(OlaServiceUtil.getDate(loanReqDetails.getSecondIncompleteApplicationLetterSendDate()));
			loanRequestDetailsList.add(loanRequestDetails);
		}
		appPkgSummaryAPIModel.setDlsLoanRequestDetailsList(loanRequestDetailsList);
		
		return appPkgSummaryAPIModel;
	}

	@Override
	public List<DLSApplicationPackageSummaryAPIModel> convert(
			List<ApplicationPackageSummary> applicationPackageSummaryList) {
		
		List<DLSApplicationPackageSummaryAPIModel> appPkgSummaryAPIModelList = new ArrayList<>();
		
		for(ApplicationPackageSummary appPkgSummary: applicationPackageSummaryList)
		{
			appPkgSummaryAPIModelList.add(convert(appPkgSummary));
		}

		return appPkgSummaryAPIModelList;
	}

}
