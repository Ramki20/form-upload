/**
 * Created on Aug 29, 2013
 */
package gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects;

import java.io.Serializable;

/**
 * 
 * ResponseBO for the Farm Records.
 * 
 * @author kartik.dhingra
 *
 */
public class FarmResponseBO  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4883858358130705024L;
	private String farmNumber;
	private String adminStateCode;
	private String adminCountyCode;
	private String errorMessage;
	/**
	 * @return the farmNumber
	 */
	public String getFarmNumber() {
		return farmNumber;
	}
	/**
	 * @param farmNumber the farmNumber to set
	 */
	public void setFarmNumber(String farmNumber) {
		this.farmNumber = farmNumber;
	}
	/**
	 * @return the adminStateCode
	 */
	public String getAdminStateCode() {
		return adminStateCode;
	}
	/**
	 * @param adminStateCode the adminStateCode to set
	 */
	public void setAdminStateCode(String adminStateCode) {
		this.adminStateCode = adminStateCode;
	}
	/**
	 * @return the adminCountyCode
	 */
	public String getAdminCountyCode() {
		return adminCountyCode;
	}
	/**
	 * @param adminCountyCode the adminCountyCode to set
	 */
	public void setAdminCountyCode(String adminCountyCode) {
		this.adminCountyCode = adminCountyCode;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
