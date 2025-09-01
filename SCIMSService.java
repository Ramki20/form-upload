package gov.usda.fsa.parmo.scims.servicewrapper;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerSystemYear;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions;
import gov.usda.fsa.parmo.scims.servicewrapper.contract.GetCustomersByOptionsContract;
import gov.usda.fsa.parmo.scims.servicewrapper.exception.SCIMSServiceException;
import gov.usda.fsa.parmo.scims.servicewrapper.reply.SCIMSServiceResult;

import java.util.ArrayList;



public interface SCIMSService
{
	
	  @deprecated
	  Original method for calling the service to look up customers.
	  getCustomers or getRptCustomers should be used instead of this method.
	  It only remains for passivity.  
	 
	public SCIMSServiceResult getCustomersByOptions(GetCustomersByOptionsContract contract ) throws SCIMSServiceException;
	
	
	  				Takes in a CustomerSearchOptions object which is what the EJB ultimately needs
	              to process. This saves time converting the data in the contract that was used in the old method. Also
	              this avoids using the customerOptionsLegacy object which is now deprecated.
	  
	  @param t
	             (AgencyToken)
	  @param options
	             (CustomerSearchOptions)
	  @param customerIds
	             (ArrayList)
	  @return SCIMSServiceResult
	  @throws SCIMSServiceException
	 

	public SCIMSServiceResult getCustomers(AgencyToken t, CustomerSearchOptions options, ArrayList customerIds)
				throws SCIMSServiceException;
	
	
	  			   Legacy method look up customers in the reporting data.This works exactly the same as getCustomers,
	              but uses the data in the reporting database which is up to 24 hrs old. Ideal for large data lookups
	              that do not need to be up to the minute.
	  
	  @param t
	             (AgencyToken)
	  @param options
	             (CustomerSearchOptions)
	  @param customerIds
	             (ArrayList)
	  @return SCIMSServiceResult
	  @throws SCIMSServiceException
	 

	public SCIMSServiceResult getRptCustomers(AgencyToken t, CustomerSearchOptions options, ArrayList customerIds)
				throws SCIMSServiceException;

	
	  	 			This returns current system year according to SCIMS. This can be used when
	              determining year offsets
	  
	  @param t
	             (AgencyToken)
	  @return CoreCustomerSystemYear
	  @throws SCIMSServiceException
	 
	public CoreCustomerSystemYear getCurrentYear(AgencyToken t) throws SCIMSServiceException;
	
	
	  			   This returns the current system year according to the Reporting version of
	              SCIMS. This will be identical to the transactional data 99.99% of the time. This can be used when
	              determining year offsets
	  
	  @param t
	             (AgencyToken)
	  @return CoreCustomerSystemYear
	  @throws SCIMSServiceException
	 
	public CoreCustomerSystemYear getRptCurrentYear(AgencyToken t) throws SCIMSServiceException;
}
