package gov.usda.fsa.fcao.flp.ola.core.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.ISCIMSCustomerService;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.OLAServiceException;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.SCIMSClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.DataOptions;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.SCIMSCustomer;

@Component
public class SCIMSCustomerServiceImpl implements ISCIMSCustomerService {

	protected static final Logger log = LogManager.getLogger(SCIMSCustomerServiceImpl.class);

	protected static final String MS = " milliseconds";
	
	@Autowired
	SCIMSClient scimsClient;

	public SCIMSCustomerServiceImpl() {}
	
	@Override
	public SCIMSCustomer getCustomer(AgencyToken agencyToken, Integer coreCustomerID)
	{
		
		return scimsClient.getCustomer(coreCustomerID.toString(), 
				OlaServiceUtil.getOlaAgencyToken(agencyToken), createDetailedDataOptions("OLA"));
		
	}
	
	@Override
	public List<SCIMSCustomer> getCustomers(AgencyToken agencyToken, List<Integer> coreCustIdList) {
		
		long startTime = System.currentTimeMillis();
		
		List<SCIMSCustomer> scimsCustomerList = null;
		try {
			 List<String> coreCustIdStringList = coreCustIdList.stream()
	                 .map(String::valueOf) // or .map(Object::toString) or .map(i -> Integer.toString(i))
	                 .collect(Collectors.toList());		
			
			 scimsCustomerList = scimsClient.getCustomers(coreCustIdStringList, 
					OlaServiceUtil.getOlaAgencyToken(agencyToken), createDetailedDataOptions("OLA"));

		} catch (Exception e) {
			log.error("Error while retrieving data from SCIMS. coreCustIdList:"+coreCustIdList, e);
			throw new OLAServiceException("Error while retrieving data from SCIMS Service");
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("SCIMS-getCustomers took {} {}", (endTime - startTime), MS);
		}
		
		return scimsCustomerList;
	}
	
    // Helper methods to create default options
    private DataOptions createDefaultDataOptions() {
        DataOptions options = new DataOptions();
        options.setCustomerStatus("BOTH");
        options.setAddress("ALL");
        options.setAddressID(false);
        options.setPhone("ALL");
        options.setEmail("ALL");
        options.setRace("ALL");
        options.setEthnicity(true);
        options.setProgramParticipation("NONE");
        options.setLegacyLink("ALL");
        options.setParameterType("CORE_ID");
        options.setDescriptionDisplay("SHORT");
        options.setCustomerAttributes(false);
        options.setPriorName(false);
        options.setInactiveLegacyLink(false);
        options.setStateOfficeLinks(false);
        options.setReturn100CharBusName(false);
        options.setLegacyLinkAddressRef(false);
        options.setReturnAllIdentifiers(false);
        options.setReturnAuditFields(false);
        options.setCustomerNotes(false);
        options.setUserID("FLP-User");
        options.setProcessingNode("FLP");
        options.setRequestHost("FLP");
        options.setPriorYearBusinessCode("NONE");
        options.setReturnSurrogateTaxId(false);
        options.setMergedIDList(false);
        options.setComplete(false);
        return options;
    }

    private DataOptions createDetailedDataOptions(String processingNode) {
        DataOptions options = createDefaultDataOptions();
        options.setCustomerAttributes(true);
        options.setPriorName(true);
        options.setPriorYearBusinessCode("ALL");
        options.setReturn100CharBusName(true);
        options.setReturnAllIdentifiers(true);
        options.setDisability(true);
        options.setReturnAuditFields(true);
        options.setCustomerNotes(true);
        options.setProcessingNode(processingNode);
        options.setMergedIDList(true);
        options.setComplete(true);
        return options;
    }

}
