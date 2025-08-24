
package gov.usda.fsa.parmo.farmrecords.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for FarmDTO complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="FarmDTO"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="adminCountyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="adminStateCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="farmYearList" type="{urn://midas.usda.gov/FR/I-026/CRMFRSharedService}FarmYearDTO" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="identifier" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="reconPendingCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Farm", propOrder = {
    "adminCountyCode",
    "adminStateCode",
    "description",
    "farmYearList",
    "identifier",
    "number",
    "reconPendingCode"
})
public class FarmDTO
{
	private String adminCountyCode;
	private String adminStateCode;
	private String description;
    @XmlElement(nillable = true)
	private FarmYearDTO[]  farmYearList;
	private Long identifier;
	private String number;
	private String reconPendingCode;
	
	public FarmDTO() {
		//Default Constructor
	}
	
	public String getAdminCountyCode() {
		return adminCountyCode;
	}
	public void setAdminCountyCode(String adminCountyCode) {
		this.adminCountyCode = adminCountyCode;
	}
	public String getAdminStateCode() {
		return adminStateCode;
	}
	public void setAdminStateCode(String adminStateCode) {
		this.adminStateCode = adminStateCode;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public Long getIdentifier() {
		return identifier;
	}
	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}
	public FarmYearDTO[] getFarmYearList() {
		return farmYearList;
	}
	public void setFarmYearList(FarmYearDTO[] farmYearList) {
		this.farmYearList = farmYearList;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getReconPendingCode() {
		return reconPendingCode;
	}
	public void setReconPendingCode(String reconPendingCode) {
		this.reconPendingCode = reconPendingCode;
	}
}
