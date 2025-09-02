/*
 * Created on Mar 29, 2006
 */
package gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects;

import java.io.Serializable;

/**
 * @author douglas.a.clark
 * @author nathan.wang DLS-5041/DLS-5702 adding more attributes so that web Apps
 *         can use them (not just for office IDs)
 */
public class ServiceCenterFlpOfficeCodeBO implements Serializable, Comparable<ServiceCenterFlpOfficeCodeBO> {
	private static final long serialVersionUID = -2956875444223004206L;
	private String countyFipsCode;
	private String locCityName;
	private String locStateAbbrev;

	private String serviceCenterCode;
	private String description;

	public ServiceCenterFlpOfficeCodeBO(String serviceCenterCode, String desc) {
		this.serviceCenterCode = serviceCenterCode;
		description = desc;
	}

	public String getServiceCenterCode() {
		return serviceCenterCode;
	}

	public String getCode() {
		return getServiceCenterCode();
	}

	public String getDescription() {
		return description;
	}

	public String getLocCityName() {
		return locCityName;
	}

	public void setLocCityName(String locCityName) {
		this.locCityName = locCityName;
	}

	public String getLocStateAbbrev() {
		return locStateAbbrev;
	}

	public void setLocStateAbbrev(String locStateAbbrev) {
		this.locStateAbbrev = locStateAbbrev;
	}

	public String getCountyFipsCode() {
		return countyFipsCode;
	}

	public void setCountyFipsCode(String countyFipsCode) {
		this.countyFipsCode = countyFipsCode;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean thesame =  super.equals(obj);
		if(thesame && obj instanceof ServiceCenterFlpOfficeCodeBO ) {
			ServiceCenterFlpOfficeCodeBO input = 
					(ServiceCenterFlpOfficeCodeBO)obj;
			return this.getCode().equals(input.getCode());	
		}
		return thesame;
	}

	@Override
	public int compareTo(ServiceCenterFlpOfficeCodeBO theBO) {
		if(theBO == null) {
			return 0;
		}
		return this.getCode().compareTo(theBO.getCode());
	}
}
