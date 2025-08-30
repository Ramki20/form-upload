
package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.usda.fsa.fcao.flpids.fbpservice2 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Sitename_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "sitename");
    private final static QName _PasswordText_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "passwordText");
    private final static QName _CoreCustomerID_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "coreCustomerID");
    private final static QName _CoreCustomerIDs_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "coreCustomerIDs");
    private final static QName _Username_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "username");
    private final static QName _PasswordDigest_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "passwordDigest");

    private final static QName _GetDLMDataResult_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "GetDLMDataResult");
    private final static QName _GetDLMYEADataResult_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "GetDLMYEADataResult");
    private final static QName _GetDALRDataResult_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "GetDALRDataResult");
    private final static QName _GetFLPRALoanServicingDataResult_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "GetFLPRALoanServicingDataResult");
    private final static QName _GetLenderStaffDataResult_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "GetLenderStaffDataResult");
    private final static QName _DLMData_QNAME = new QName("", "DLMData");
    private final static QName _DALRData_QNAME = new QName("", "DALRData");
    private final static QName _FLPRALoanServicingData_QNAME = new QName("", "FLPRALoanServicingData");
    // private final static QName _DLMData_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "DLMData");
    // private final static QName _DLMRecord_QNAME = new QName(FBPWsdlInfo.FBP_NAMESPACE_URI, "DLMRecord");
    
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.usda.fsa.fcao.flpids.fbpservice2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDALRDataResult }
     * 
     */
    public GetDALRDataResult createGetDALRDataResult() {
        return new GetDALRDataResult();
    }
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "GetDALRDataResult")
    public JAXBElement<GetDALRDataResult> createGetDLMDataResult(GetDALRDataResult value) {
        return new JAXBElement<GetDALRDataResult>(_GetDALRDataResult_QNAME, GetDALRDataResult.class, null, value);
    }

    /**
     * Create an instance of {@link GetDLMYEADataResult }
     * 
     */
    public GetDLMYEADataResult createGetDLMYEADataResult() {
        return new GetDLMYEADataResult();
    }
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "GetDLMYEADataResult")
    public JAXBElement<GetDLMYEADataResult> createGetDLMYEADataResult(GetDLMYEADataResult value) {
        return new JAXBElement<GetDLMYEADataResult>(_GetDLMYEADataResult_QNAME, GetDLMYEADataResult.class, null, value);
    }

    /**
     * Create an instance of {@link GetFLPRALoanServicingDataResult }
     * 
     */
    public GetFLPRALoanServicingDataResult createGetFLPRALoanServicingDataResult() {
        return new GetFLPRALoanServicingDataResult();
    }
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "GetFLPRALoanServicingDataResult")
    public JAXBElement<GetFLPRALoanServicingDataResult> createGetFLPRALoanServicingDataResult(GetFLPRALoanServicingDataResult value) {
        return new JAXBElement<GetFLPRALoanServicingDataResult>(_GetFLPRALoanServicingDataResult_QNAME, GetFLPRALoanServicingDataResult.class, null, value);
    }

    /**
     * Create an instance of {@link GetDLMDataResult }
     * 
     */
    public GetDLMDataResult createGetDLMDataResult() {
        return new GetDLMDataResult();
    }    
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "GetDLMDataResult")
    public JAXBElement<GetDLMDataResult> createGetDLMDataResult(GetDLMDataResult value) {
        return new JAXBElement<GetDLMDataResult>(_GetDLMDataResult_QNAME, GetDLMDataResult.class, null, value);
    }

    public GetLenderStaffDataResult createGetLenderStaffDataResult() {
        return new GetLenderStaffDataResult();
    }    
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "GetLenderStaffDataResult")
    public JAXBElement<GetLenderStaffDataResult> createGetLenderStaffDataResult(GetLenderStaffDataResult value) {
        return new JAXBElement<GetLenderStaffDataResult>(_GetLenderStaffDataResult_QNAME, GetLenderStaffDataResult.class, null, value);
    }
    
    @XmlElementDecl(namespace = "", name = "DLMData")
    public JAXBElement<DLMData> createDLMData(DLMData value) {
        return new JAXBElement<DLMData>(_DLMData_QNAME, DLMData.class, null, value);
    }
    
    @XmlElementDecl(namespace = "", name = "DALRData")
    public JAXBElement<DALRData> createDALRData(DALRData value) {
        return new JAXBElement<DALRData>(_DALRData_QNAME, DALRData.class, null, value);
    }
    
    @XmlElementDecl(namespace = "", name = "FLPRALoanServicingData")
    public JAXBElement<FLPRALoanServicingData> createFLPRALoanServicingData(FLPRALoanServicingData value) {
        return new JAXBElement<FLPRALoanServicingData>(_FLPRALoanServicingData_QNAME, FLPRALoanServicingData.class, null, value);
    }
    
//    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "DLMRecord")
//    public JAXBElement<DLMRecord> createDLMRecord(DLMRecord value) {
//        return new JAXBElement<DLMRecord>(_DLMRecord_QNAME, DLMRecord.class, null, value);
//    }
    
    /**
     * Create an instance of {@link ArrayOfInt }
     * 
     */
    public ArrayOfInt createArrayOfInt() {
        return new ArrayOfInt();
    }    
    
    /**
     * Create an instance of {@link ArrayOfLenderStaff }
     * 
     */
    public ArrayOfLenderStaff createArrayOfLenderStaff() {
        return new ArrayOfLenderStaff();
    }
    
    
    /**
     * Create an instance of {@link VendorClient }
     * 
     */
    public VendorClient createVendorClient() {
        return new VendorClient();
    }
    
    /**
     * Create an instance of {@link ArrayOfVendorClient }
     * 
     */
    public ArrayOfVendorClient createArrayOfVendorClient() {
        return new ArrayOfVendorClient();
    }

    /**
     * Create an instance of {@link LenderStaff }
     * 
     */
    public LenderStaff createLenderStaff() {
        return new LenderStaff();
    }

    /**
     * Create an instance of {@link WarningType }
     * 
     */
    public WarningType createWarningType() {
        return new WarningType();
    }

    /**
     * Create an instance of {@link ArrayOfWarningType }
     * 
     */
    public ArrayOfWarningType createArrayOfWarningType() {
        return new ArrayOfWarningType();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "sitename")
    public JAXBElement<String> createSitename(String value) {
        return new JAXBElement<String>(_Sitename_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "passwordText")
    public JAXBElement<String> createPasswordText(String value) {
        return new JAXBElement<String>(_PasswordText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "coreCustomerID")
    public JAXBElement<Integer> createCoreCustomerID(Integer value) {
        return new JAXBElement<Integer>(_CoreCustomerID_QNAME, Integer.class, null, value);
    }
    
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "coreCustomerIDs")
    public JAXBElement<ArrayOfInt> createCoreCustomerIDs(ArrayOfInt value) {
        return new JAXBElement<ArrayOfInt>(_CoreCustomerIDs_QNAME, ArrayOfInt.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "username")
    public JAXBElement<String> createUsername(String value) {
        return new JAXBElement<String>(_Username_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = FBPWsdlInfo.FBP_NAMESPACE_URI, name = "passwordDigest")
    public JAXBElement<String> createPasswordDigest(String value) {
        return new JAXBElement<String>(_PasswordDigest_QNAME, String.class, null, value);
    }
    
}
