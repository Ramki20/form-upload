package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import java.util.List;

/**
 * @author FCAO
 *
 */
public interface FBPConnector 
{

	public DLMData getDLMData(Integer corecustomerid) throws FBPConnectorException;
	
	public DLMData getDLMData(String fbpEndPointURL, String sitename,
			String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPConnectorException;

	public DLMData getDLMYEAData(Integer corecustomerid) throws FBPConnectorException;
	
	public DLMData getDLMYEAData(String fbpEndPointURL, String sitename,
			String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPConnectorException;

	public DALRData getDALRData(Integer corecustomerid) throws FBPConnectorException;
	
	public DALRData getDALRData(String fbpEndPointURL, String sitename,
			String username, String passwdtext, String passwddigest,
			Integer corecustomerid) throws FBPConnectorException;

	public LenderStaffData getLenderStaffData(List<Integer> inputCoreCustomerIDList) throws FBPConnectorException;
	
	public LenderStaffData getLenderStaffData(String fbpEndPointURL, String sitename,
			String username, String passwdtext, String passwddigest,
			List<Integer> inputCoreCustomerIDList) throws FBPConnectorException;

	public FLPRALoanServicingData getFLPRALoanServicingData() throws FBPConnectorException;
	
	public FLPRALoanServicingData getFLPRALoanServicingData(String fbpEndPointURL, 
			String username, String passwdtext, String passwddigest)
			throws FBPConnectorException;
	
	public boolean isHealthy();
}
