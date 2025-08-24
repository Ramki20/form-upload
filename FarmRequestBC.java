/**
 * Created on Aug 14, 2013
 */
package gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts;

import gov.usda.fsa.common.base.AgencyBusinessContract;
import gov.usda.fsa.common.base.AgencyToken;

/**
 * 
 * Request BC for farm records.
 * 
 * @author kartik.dhingra
 *
 */
public class FarmRequestBC extends AgencyBusinessContract{

	private static final long serialVersionUID = -6589788364629797210L;
	
	private Long coreCustomerId;
	private Integer year;
	private Boolean includeCustomerWithResponse;
	private Boolean includeNonActiveCustomerWithResponse;
	private Boolean includeTractInfoWithResponse;
	private Boolean includeCropWithResponse;
	private String lowerBoundFarmNumber;
	
	public FarmRequestBC(AgencyToken token)
	{
		super(token);
		includeCustomerWithResponse=false;
		includeNonActiveCustomerWithResponse=false;
		includeTractInfoWithResponse=false;
		includeCropWithResponse=false;
	}
	/**
	 * @return the coreCustomerId
	 */
	public Long getCoreCustomerId() {
		return coreCustomerId;
	}
	/**
	 * @param coreCustomerId the coreCustomerId to set
	 */
	public void setCoreCustomerId(Long coreCustomerId) {
		this.coreCustomerId = coreCustomerId;
	}
	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
	/**
	 * @return the includeCustomerWithResponse
	 */
	public Boolean getIncludeCustomerWithResponse() {
		return includeCustomerWithResponse;
	}
	/**
	 * @param includeCustomerWithResponse the includeCustomerWithResponse to set
	 */
	public void setIncludeCustomerWithResponse(Boolean includeCustomerWithResponse) {
		this.includeCustomerWithResponse = includeCustomerWithResponse;
	}
	/**
	 * @return the includeNonActiveCustomerWithResponse
	 */
	public Boolean getIncludeNonActiveCustomerWithResponse() {
		return includeNonActiveCustomerWithResponse;
	}
	/**
	 * @param includeNonActiveCustomerWithResponse the includeNonActiveCustomerWithResponse to set
	 */
	public void setIncludeNonActiveCustomerWithResponse(
			Boolean includeNonActiveCustomerWithResponse) {
		this.includeNonActiveCustomerWithResponse = includeNonActiveCustomerWithResponse;
	}
	/**
	 * @return the includeTractInfoWithResponse
	 */
	public Boolean getIncludeTractInfoWithResponse() {
		return includeTractInfoWithResponse;
	}
	/**
	 * @param includeTractInfoWithResponse the includeTractInfoWithResponse to set
	 */
	public void setIncludeTractInfoWithResponse(Boolean includeTractInfoWithResponse) {
		this.includeTractInfoWithResponse = includeTractInfoWithResponse;
	}
	/**
	 * @return the includeCropWithResponse
	 */
	public Boolean getIncludeCropWithResponse() {
		return includeCropWithResponse;
	}
	/**
	 * @param includeCropWithResponse the includeCropWithResponse to set
	 */
	public void setIncludeCropWithResponse(Boolean includeCropWithResponse) {
		this.includeCropWithResponse = includeCropWithResponse;
	}
	/**
	 * @return the lowerBoundFarmNumber
	 */
	public String getLowerBoundFarmNumber() {
		return lowerBoundFarmNumber;
	}
	/**
	 * @param lowerBoundFarmNumber the lowerBoundFarmNumber to set
	 */
	public void setLowerBoundFarmNumber(String lowerBoundFarmNumber) {
		this.lowerBoundFarmNumber = lowerBoundFarmNumber;
	}
}
