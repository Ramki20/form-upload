package gov.usda.fsa.parmo.frs.ejb.client.contract;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.usda.fsa.common.base.AgencyToken;


/**
 * &lt;p&gt;Java class for retrieveFarmListRequest complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="retrieveFarmListRequest"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="adminCountyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="adminStateCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="contractVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="coreCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="farmNumberList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="includeClu" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeCrop" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeCustomer" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeElection" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeHip" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeNonActiveCustomer" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeNonActiveFarm" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="includeTract" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="lowerBoundCountyCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="lowerBoundFarmNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="lowerBoundStateCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="maxPrecision" type="{http://www.w3.org/2001/XMLSchema}string"/&amp;gt;
 *         &amp;lt;element name="serviceOfficeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="tractNumberList" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="userIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="year" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "retrieveFarmListRequest", propOrder = {
    "year",
    "adminCountyCode",
    "adminStateCode",
    "contractVersion",
    "coreCustomerId",
    "farmNumberList",
    "includeClu",
    "includeCrop",
    "includeCustomer",
    "includeElection",
    "includeHip",
    "includeNonActiveCustomer",
    "includeNonActiveFarm",
    "includeTract",
    "lowerBoundCountyCode",
    "lowerBoundFarmNumber",
    "lowerBoundStateCode",
    "maxPrecision",
    "serviceOfficeId",
    "tractNumberList",
    "userIdentifier"
})
public class RetrieveFarmListRequest {

    protected Short year;
    protected String adminCountyCode;
    protected String adminStateCode;
    protected String contractVersion;
    protected Integer coreCustomerId;
    @XmlElement(nillable = true)
    protected List<String> farmNumberList;
    @XmlElement(required = true)
    protected boolean includeClu;
    @XmlElement(required = true)
    protected boolean includeCrop;
    @XmlElement(required = true)
    protected boolean includeCustomer;
    @XmlElement(required = true)
    protected boolean includeElection;
    @XmlElement(required = true)
    protected boolean includeHip;
    @XmlElement(required = true)
    protected boolean includeNonActiveCustomer;
    @XmlElement(required = true)
    protected boolean includeNonActiveFarm;
    @XmlElement(required = true)
    protected boolean includeTract;
    protected String lowerBoundCountyCode;
    protected String lowerBoundFarmNumber;
    protected String lowerBoundStateCode;
    @XmlElement(required = true)
    protected boolean maxPrecision;
    protected String serviceOfficeId;
    @XmlElement(nillable = true)
    protected List<String> tractNumberList;
    protected String userIdentifier; //MIDAS uses the userIdentifer as the application identifier

    public RetrieveFarmListRequest() { 
    	//Default Constructor 
    }
    
    /**
     * @deprecated
     * @param token
     */
    @Deprecated
    public RetrieveFarmListRequest(AgencyToken token)
    {
    	//There is no "Application Identifier in the contract but MIDAS uses the userIdentifer as the application identifier
    	if( token != null ) {
    		this.userIdentifier = token.getApplicationIdentifier();
    	} else {
    		this.userIdentifier = "UNKWN";
    	}
    }
    
    /**
     * @deprecated
     */
    @Deprecated
	public AgencyToken getAgencyToken()
	{
		AgencyToken token = new AgencyToken();
		token.setUserIdentifier(this.userIdentifier);
		token.setRequestHost(this.userIdentifier);
		return token;
	}
    
    /**
     * @deprecated
     * @param token
     */
    @Deprecated
	public void setAgencyToken(AgencyToken agencyToken) {
		this.userIdentifier = agencyToken.getUserIdentifier();
	}

    /**
     * Gets the value of the adminCountyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdminCountyCode() {
        return adminCountyCode;
    }

    /**
     * Sets the value of the adminCountyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdminCountyCode(String value) {
        this.adminCountyCode = value;
    }

    /**
     * Gets the value of the adminStateCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdminStateCode() {
        return adminStateCode;
    }

    /**
     * Sets the value of the adminStateCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdminStateCode(String value) {
        this.adminStateCode = value;
    }

    /**
     * Gets the value of the contractVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractVersion() {
        return contractVersion;
    }

    /**
     * Sets the value of the contractVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractVersion(String value) {
        this.contractVersion = value;
    }

    /**
     * Gets the value of the coreCustomerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Integer getCustomerId() {
        return coreCustomerId;
    }

    /**
     * Sets the value of the coreCustomerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerId(Integer value) {
        this.coreCustomerId = value;
    }

    /**
     * Gets the value of the farmNumberList property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the farmNumberList property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getFarmNumberList().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFarmNumberList() {
        if (farmNumberList == null) {
            farmNumberList = new ArrayList<>();
        }
        return this.farmNumberList;
    }

    public void setFarmNumberList(List<String> farmNumberList)
    {
    	this.farmNumberList = farmNumberList;
    }
    
    /**
     * Gets the value of the includeClu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeClu() {
        return includeClu;
    }

    /**
     * Sets the value of the includeClu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeClu(boolean value) {
        this.includeClu = value;
    }

    /**
     * Gets the value of the includeCrop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeCrop() {
        return includeCrop;
    }

    /**
     * Sets the value of the includeCrop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeCrop(boolean value) {
        this.includeCrop = value;
    }

    /**
     * Gets the value of the includeCustomer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeCustomer() {
        return includeCustomer;
    }

    /**
     * Sets the value of the includeCustomer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeCustomer(boolean value) {
        this.includeCustomer = value;
    }

    /**
     * Gets the value of the includeElection property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeElection() {
        return includeElection;
    }

    /**
     * Sets the value of the includeElection property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeElection(boolean value) {
        this.includeElection = value;
    }

    /**
     * Gets the value of the includeHip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeHip() {
        return includeHip;
    }

    /**
     * Sets the value of the includeHip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeHip(boolean value) {
        this.includeHip = value;
    }

    /**
     * Gets the value of the includeNonActiveCustomer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeNonActiveCustomer() {
        return includeNonActiveCustomer;
    }

    /**
     * Sets the value of the includeNonActiveCustomer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeNonActiveCustomer(boolean value) {
        this.includeNonActiveCustomer = value;
    }

    /**
     * Gets the value of the includeNonActiveFarm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeNonActiveFarm() {
        return includeNonActiveFarm;
    }

    /**
     * Sets the value of the includeNonActiveFarm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeNonActiveFarm(boolean value) {
        this.includeNonActiveFarm = value;
    }

    /**
     * Gets the value of the includeTract property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getIncludeTract() {
        return includeTract;
    }

    /**
     * Sets the value of the includeTract property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeTract(boolean value) {
        this.includeTract = value;
    }

    /**
     * Gets the value of the lowerBoundCountyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowerBoundCountyCode() {
        return lowerBoundCountyCode;
    }

    /**
     * Sets the value of the lowerBoundCountyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowerBoundCountyCode(String value) {
        this.lowerBoundCountyCode = value;
    }

    /**
     * Gets the value of the lowerBoundFarmNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowerBoundFarmNumber() {
        return lowerBoundFarmNumber;
    }

    /**
     * Sets the value of the lowerBoundFarmNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowerBoundFarmNumber(String value) {
        this.lowerBoundFarmNumber = value;
    }

    /**
     * Gets the value of the lowerBoundStateCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowerBoundStateCode() {
        return lowerBoundStateCode;
    }

    /**
     * Sets the value of the lowerBoundStateCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowerBoundStateCode(String value) {
        this.lowerBoundStateCode = value;
    }

    /**
     * Gets the value of the maxPrecision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public boolean getMaxPrecision() {
        return maxPrecision;
    }

    /**
     * Sets the value of the maxPrecision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxPrecision(boolean value) {
        this.maxPrecision = value;
    }

    /**
     * Gets the value of the serviceOfficeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceOfficeId() {
        return serviceOfficeId;
    }

    /**
     * Sets the value of the serviceOfficeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceOfficeId(String value) {
        this.serviceOfficeId = value;
    }

    /**
     * Gets the value of the tractNumberList property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the tractNumberList property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getTractNumberList().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getTractNumberList() {
        if (tractNumberList == null) {
            tractNumberList = new ArrayList<>();
        }
        return this.tractNumberList;
    }

    public void setTractNumberList(List<String> tractNumberList)
    {
    	this.tractNumberList = tractNumberList;
    }
    
    /**
     * Gets the value of the userIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Sets the value of the userIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserIdentifier(String value) {
        this.userIdentifier = value;
    }

    /**
     * Gets the value of the year property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Short getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYear(Short value) {
        this.year = value;
    }

    /**
     * @deprecated
     */
	@Deprecated
	public boolean getRetrieveListByTract()
	{
		return false;
	}

    /**
     * @deprecated
     */
	@Deprecated
	public void setRetrieveListByTract(boolean retrieveListByTract)
	{
		//Do Nothing
	}
    
	@Override
	public String toString() {
		return "RetrieveFarmListRequest [year=" + year + ", adminCountyCode=" + adminCountyCode + ", adminStateCode="
				+ adminStateCode + ", contractVersion=" + contractVersion + ", coreCustomerId=" + coreCustomerId
				+ ", farmNumberList=" + farmNumberList + ", includeClu=" + includeClu + ", includeCrop=" + includeCrop
				+ ", includeCustomer=" + includeCustomer + ", includeElection=" + includeElection + ", includeHip="
				+ includeHip + ", includeNonActiveCustomer=" + includeNonActiveCustomer + ", includeNonActiveFarm="
				+ includeNonActiveFarm + ", includeTract=" + includeTract + ", lowerBoundCountyCode="
				+ lowerBoundCountyCode + ", lowerBoundFarmNumber=" + lowerBoundFarmNumber + ", lowerBoundStateCode="
				+ lowerBoundStateCode + ", maxPrecision=" + maxPrecision + ", serviceOfficeId=" + serviceOfficeId
				+ ", tractNumberList=" + tractNumberList + ", userIdentifier=" + userIdentifier + "]";
	}
}