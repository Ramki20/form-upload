package gov.usda.fsa.fcao.flp.ola.core.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usda.fsa.fcao.flp.ola.core.api.IDLSApplicationPackageAPI;
import gov.usda.fsa.fcao.flp.ola.core.api.converter.IDLSApplicationPackageConverter;
import gov.usda.fsa.fcao.flp.ola.core.api.model.DLSApplicationPackageSummaryAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.OlaCoreAPIException;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.FLPGatewayClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApplicationPackageSummary;

@Component
public class DLSApplicationPackageAPIImpl implements IDLSApplicationPackageAPI {
	
	protected static final Logger LOGGER = LogManager.getLogger(DLSApplicationPackageAPIImpl.class);
	
	@Autowired
	IDLSApplicationPackageConverter dlsApplicationPackageConverter;
	
	@Autowired
	FLPGatewayClient flpGatewayClient;
	
	@Autowired
	OlaAgencyToken olaAgencyToken;
	
	@Override
	public DLSApplicationPackageSummaryAPIModel getApplicationPackageSummary (Integer onlineApplicationNumber) {
		
		if(onlineApplicationNumber==null){
			
			LOGGER.error("Application Nnmber is Mandatory.");

			throw new OlaCoreAPIException("Application Nnmber is Mandatory.");
		}
		
		LOGGER.info("Processing DLS to OLA for the Application Nnmber: {}", onlineApplicationNumber);

		ApplicationPackageSummary applicationPackageSummary = flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(onlineApplicationNumber.toString(), olaAgencyToken);

		if(applicationPackageSummary!=null)
		{
			LOGGER.info("Processed DLS to OLA for the Application Nnmber. Result is: {}", applicationPackageSummary);
			return dlsApplicationPackageConverter.convert(applicationPackageSummary);
		} else {

			LOGGER.error("No Data returned from DLS to OLA for the Application Nnmber: {}", onlineApplicationNumber);
		}
        return null;		
	}
	
	@Override
	public List<DLSApplicationPackageSummaryAPIModel> getApplicationPackageSummaryList(
			Integer coreCustomerIdentifier) {
		
	     if(coreCustomerIdentifier==null){
				LOGGER.error("coreCustomerIdentifier is Mandatory.");
				throw new OlaCoreAPIException("SCIMS Customer Identifier is Mandatory.");
		}

		LOGGER.info("Processing DLS to OLA for the Core customer identifier: {}", coreCustomerIdentifier);
		
		List<ApplicationPackageSummary> applicationPackageSummaryList = 
				flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(coreCustomerIdentifier.toString(), olaAgencyToken);
		
		if(applicationPackageSummaryList!=null && !applicationPackageSummaryList.isEmpty())
		{
			LOGGER.info("Processed DLS to OLA for the Customer Identifier. Result is: {}",
					applicationPackageSummaryList);

			return dlsApplicationPackageConverter.convert(applicationPackageSummaryList);
		} else {

			LOGGER.error("No Data returned from DLS to OLA for the Customer Identifie: {}", coreCustomerIdentifier);
		}
	    return new ArrayList<>(); 	
	}

}
