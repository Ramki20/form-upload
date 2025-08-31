
package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfVendorClient complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfVendorClient">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="VendorClient" type="{http://schemas.eci-equity.com/2005/FBPService/}VendorClient" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "",  propOrder = {
    "vendorClient"
})
public class ArrayOfVendorClient {

    //@XmlElementWrapper(name="VendorClientList")
    @XmlElement(name = "VendorClient", namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, nillable = true)
    protected List<VendorClient> vendorClient;

    /**
     * Gets the value of the vendorClient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vendorClient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVendorClient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VendorClient }
     * 
     * 
     */
    public List<VendorClient> getVendorClient() {
        if (vendorClient == null) {
            vendorClient = new ArrayList<VendorClient>();
        }
        return this.vendorClient;
    }

	@Override
	public String toString() {
		return "ArrayOfVendorClient [vendorClient=" + vendorClient + "]";
	}
    
}
