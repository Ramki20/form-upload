
package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfLenderStaff complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfLenderStaff">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LenderStaff" type="{http://schemas.eci-equity.com/2005/FBPService/}LenderStaff" maxOccurs="unbounded" minOccurs="0"/>
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
    "lenderStaff"
})
public class ArrayOfLenderStaff {

    // @XmlElementWrapper(name="LenderStaffList")
    @XmlElement(name = "LenderStaff", namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, nillable = true)
    protected List<LenderStaff> lenderStaff;

    /**
     * Gets the value of the lenderStaff property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lenderStaff property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLenderStaff().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LenderStaff }
     * 
     * 
     */
    public List<LenderStaff> getLenderStaff() {
        if (lenderStaff == null) {
            lenderStaff = new ArrayList<LenderStaff>();
        }
        return this.lenderStaff;
    }

	@Override
	public String toString() {
		return "ArrayOfLenderStaff [lenderStaff=" + lenderStaff + "]";
	}
    
}
