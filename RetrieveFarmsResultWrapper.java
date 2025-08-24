package gov.usda.fsa.parmo.frs.ejb.client.reply;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import gov.usda.fsa.parmo.farmrecords.dto.FarmResultDTO;


/**
 * &lt;p&gt;Java class for retrieveFarmsResultWrapper complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="retrieveFarmsResultWrapper"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="result" type="{urn://midas.usda.gov/FR/I-026/CRMFRSharedService}FarmResultDTO" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "retrieveFarmsResultWrapper", propOrder = {
    "result"
})
public class RetrieveFarmsResultWrapper {

    protected FarmResultDTO result;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link FarmResultDTO }
     *     
     */
    public FarmResultDTO getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link FarmResultDTO }
     *     
     */
    public void setResult(FarmResultDTO value) {
        this.result = value;
    }

	@Override
	public String toString() {
		return "RetrieveFarmsResultWrapper [result=" + result + "]";
	}

}
