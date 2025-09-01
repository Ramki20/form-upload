package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usda.fsa.fcao.flp.flpids.common.ScimsTestBase;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.ParseScimsResultBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByTaxIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CustomerScimsVO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsMergedCustomerIDHistoryBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker.SCIMSClientProxy;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;

public class ScimsCustomerBS_UT extends ScimsTestBase {
	
	@Autowired
	private SCIMSClientProxy scimsClientProxy;

	@Before
	public void setUp () throws Exception{
		super.setUp();
			
	}
	@Test
	public void testRetrieveScimsCustomersByCoreCustomerId_noIds() throws Exception {
		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);
		List<ScimsCustomerBO> results = this.serviceAgentFacade.getScimsCustomerBS().retrieveScimsCustomersByCoreCustomerId(contract);
		assertTrue(results.size() == 0);
	}
	
	@Test
	public void testRetrieveScimsCustomersMapByCoreCustomerId_noIds() throws Exception {
		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);
		Map<Integer,ScimsCustomerBO> resultsMap = this.serviceAgentFacade.getScimsCustomerBS().retrieveScimsCustomersLiteMapByCoreCustomerId(contract);
		assertTrue(resultsMap.size() == 0);
	}
	
	@Test
	public void testRetrieveScimsCustomersByCoreCustomerId_oneId() throws Exception {
	
		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();

		
		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		coreCustomerIds.add(1432871);
		
		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);

		List<ScimsCustomerBO> results = scimsBusinessService.retrieveScimsCustomersByCoreCustomerId(contract);
		
		assertTrue(results.size() == 1);
		ScimsCustomerBO scimsCustomerBO = results.get(0);
		assertEquals("1432871",scimsCustomerBO.getCustomerID());
	}
	
	@Test
	public void testRetrieveScimsCustomersMapByCoreCustomerId_oneId() throws Exception {
	
		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();

		
		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		coreCustomerIds.add(1432871);
		
		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);

		Map<Integer,ScimsCustomerBO> resultsMap = scimsBusinessService.retrieveScimsCustomersLiteMapByCoreCustomerId(contract);
		
		assertTrue(resultsMap.size() == 1);
		ScimsCustomerBO scimsCustomerBO = resultsMap.values().iterator().next();
		assertEquals("1432871",scimsCustomerBO.getCustomerID());
		assertEquals("1432871", resultsMap.keySet().iterator().next().toString());
	}	
	
	@Test
	public void testRetrieveScimsCustomersByCoreCustomerId_foreignPersonInd() throws Exception {

		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();

		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		coreCustomerIds.add(1533454);

		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);

		List<ScimsCustomerBO> results = scimsBusinessService.retrieveScimsCustomersByCoreCustomerId(contract);

		assertTrue(results.size() == 1);
		ScimsCustomerBO scimsCustomerBO = results.get(0);
		assertEquals("1533454", scimsCustomerBO.getCustomerID());
		assertEquals("U", scimsCustomerBO.getResidentAlien());
		assertEquals("US", scimsCustomerBO.getCitizenshipCountryCode().getCode());
		assertEquals("", scimsCustomerBO.getOriginatingCountryCode().getCode());
	}

	@Test(expected = SCIMSDataNotFoundException.class)
	public void testRetrieveScimsCustomersByCoreCustomerId_wrongId() throws Exception {
		List<Integer> coreCustomerIds = new ArrayList<Integer>();
		coreCustomerIds.add(143287134);

		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();

		RetrieveScimsCustomersByCoreCustomerIdBC contract = new RetrieveScimsCustomersByCoreCustomerIdBC(
				getAgencyToken(), coreCustomerIds, true);

		scimsBusinessService.retrieveScimsCustomersByCoreCustomerId(contract);
	}

	@Test
	public void testRetrieveScimsCustomersByTaxId_noId() throws Exception {
		List<String> taxIds = new ArrayList<String>();
		List<String> taxIdTypes = new ArrayList<String>();

		RetrieveScimsCustomersByTaxIdBC contract = new RetrieveScimsCustomersByTaxIdBC(getAgencyToken(), taxIds,
				taxIdTypes, true);

		List<ScimsCustomerBO> results = this.serviceAgentFacade.getScimsCustomerBS()
				.retrieveScimsCustomersByTaxId(contract);

		assertTrue(results.size() == 0);
	}

	@Test
	public void testRetrieveScimsCustomersByTaxId_oneId() throws Exception {
		List<String> taxIds = new ArrayList<String>();
		taxIds.add("400352979");
		List<String> taxIdTypes = new ArrayList<String>();
		taxIdTypes.add("S");


		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();

		RetrieveScimsCustomersByTaxIdBC contract = new RetrieveScimsCustomersByTaxIdBC(getAgencyToken(), taxIds,
				taxIdTypes, true);

		List<ScimsCustomerBO> results = scimsBusinessService.retrieveScimsCustomersByTaxId(contract);

		assertTrue(results.size() == 1);
		ScimsCustomerBO scimsCustomerBO = results.get(0);
		assertEquals("400352979", scimsCustomerBO.getTaxID());
		assertEquals("S", scimsCustomerBO.getTaxIDType().getCode());
	}

	@Test(expected = SCIMSDataNotFoundException.class)
	public void testRetrieveScimsCustomersByTaxId_wrongId() throws Exception {
		List<String> taxIds = new ArrayList<String>();
		taxIds.add("4003529790");
		List<String> taxIdTypes = new ArrayList<String>();
		taxIdTypes.add("S");


		RetrieveScimsCustomersByTaxIdBC contract = new RetrieveScimsCustomersByTaxIdBC(getAgencyToken(), taxIds,
				taxIdTypes, true);

		this.serviceAgentFacade.getScimsCustomerBS().retrieveScimsCustomersByTaxId(contract);
	}

	@Test(expected = SCIMSDataNotFoundException.class)
	public void testRetrieveScimsCustomersByTaxId_wrongTaxIdType() throws Exception {
		List<String> taxIds = new ArrayList<String>();
		taxIds.add("400352979");
		List<String> taxIdTypes = new ArrayList<String>();
		taxIdTypes.add("M");
		
		RetrieveScimsCustomersByTaxIdBC contract = new RetrieveScimsCustomersByTaxIdBC(
				getAgencyToken(), taxIds,taxIdTypes, true);

		this.serviceAgentFacade.getScimsCustomerBS().retrieveScimsCustomersByTaxId(contract);
	}	
	
	@Test (expected=SCIMSDataNotFoundException.class)
	public void testParseScimsResultXMLForOneCustomer_emptyXmlString() throws Exception{
		ParseScimsResultBC contract = new ParseScimsResultBC(getAgencyToken(),"");
		
		this.serviceAgentFacade.getScimsCustomerBS().parseScimsResultXMLForOneCustomer(contract);
	}
	
	@Test (expected=SCIMSDataNotFoundException.class)
	public void testParseScimsResultXMLForOneCustomer_wrongXmlString() throws Exception{
		ParseScimsResultBC contract = new ParseScimsResultBC(getAgencyToken(),"<SCIMSQuery></SCIMSQuery>");
		
		this.serviceAgentFacade.getScimsCustomerBS().parseScimsResultXMLForOneCustomer(contract);		
	}
	
	@Test 
	public void testParseScimsResultXMLForOneCustomer_withValidXmlString() throws Exception{
		String resultXml = "<?xml version='1.0' ?><SCIMSCustomerList><NumberOfCustomersReturned>1</NumberOfCustomersReturned><SCIMSCustomer><CustomerData><CustomerID>1432871</CustomerID><CustomerType><Code>I</Code><Description>Individual</Description></CustomerType><CommonName>CRAIG ROGERS</CommonName><IndividualName><Last>ROGERS</Last><First>CRAIG</First><Middle /><Prefix>MR</Prefix><Suffix /></IndividualName><BusinessName /><LegalNameIndicator>N</LegalNameIndicator><BusinessType><BusinessTypeCurrentYear><Code>00</Code><Description>Individual</Description></BusinessTypeCurrentYear><BusinessTypePriorYear1><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear1><BusinessTypePriorYear2><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear2></BusinessType><TaxID>400352979</TaxID><TaxIDType><Code>S</Code><Description>Social Security</Description></TaxIDType><InactiveCustomer><Code>N</Code><Description>Active</Description></InactiveCustomer><InactiveCustomerDate /><DuplicateCustomer><Code>N</Code><Description>No duplicate</Description></DuplicateCustomer><CustomerAttributes><BirthDate>07/18/1974</BirthDate><BirthDateDetermination><Code>C </Code><Description>Customer Declared</Description></BirthDateDetermination><Gender><Code>02</Code><Description>Male</Description></Gender><GenderDetermination><Code>E</Code><Description>Employee Observed</Description></GenderDetermination><MaritalStatus><Code>MA</Code><Description>Married</Description></MaritalStatus><Veteran>N</Veteran><LimitedResourceProducer>N</LimitedResourceProducer><ResidentAlien>U</ResidentAlien><CitizenshipCountry><Code>US</Code></CitizenshipCountry><OriginatingCountry><Code /></OriginatingCountry><VotingDistrict>2106</VotingDistrict><LanguagePreference><Code>EN</Code><Description>English</Description></LanguagePreference><ReceiveMail>Y</ReceiveMail><ReceiveFsaMail>Y</ReceiveFsaMail><ReceiveNrcsMail>Y</ReceiveNrcsMail><ReceiveRdMail>N</ReceiveRdMail><EmployeeType><Code>00</Code><Description>Not an Employee</Description></EmployeeType></CustomerAttributes></CustomerData><Address><MailingAddress>Y</MailingAddress><ShippingAddress>N</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>02/09/2002</AddressBeginDate><AddressEndDate /><CurrentAddress>Y</CurrentAddress><SupplementalLine1 /><SupplementalLine2 /><InformationLine /><DeliveryAddressLine>640 CHANDALAND LN</DeliveryAddressLine><CityName>WINCHESTER</CityName><StateAbbreviation>KY</StateAbbreviation><ZipCodeFirst5>40391</ZipCodeFirst5><ZipCodePlus4>9691</ZipCodePlus4><CarrierRouteCode>R002</CarrierRouteCode><ForeignAddressLine /><DeliveryPointBarCode>404</DeliveryPointBarCode><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Phone><PhoneType><Code>HM</Code><Description>Home</Description></PhoneType><PhonePrimary>Y</PhonePrimary><PhoneNumber>6067446652</PhoneNumber><PhoneExtension /><PhoneUnlisted>N</PhoneUnlisted><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County></Phone><Race><RaceType><Code>W</Code><Description>White</Description></RaceType><RaceTypeDetermination><Code>E</Code><Description>Employee Observed</Description></RaceTypeDetermination></Race><Ethnicity><EthnicityType><Code>N</Code><Description>Not Hispanic</Description></EthnicityType><EthnicityTypeDetermination><Code>E</Code><Description>Employee Observed</Description></EthnicityTypeDetermination></Ethnicity><LegacyLink><LegacySystem><Code>PNAM</Code><Description>FSA Producer</Description></LegacySystem><LegacyState><Code>21</Code></LegacyState><LegacyCounty><Code>049</Code></LegacyCounty><LegacySystemCustomerID>00400352979S</LegacySystemCustomerID><ServicingOrgUnit><Code>62220</Code><Description>CLARK COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>02/09/2002</LegacyActiveDate><LegacyInactiveDate /></LegacyLink><LegacyLink><LegacySystem><Code>PNAM</Code><Description>FSA Producer</Description></LegacySystem><LegacyState><Code>21</Code></LegacyState><LegacyCounty><Code>173</Code></LegacyCounty><LegacySystemCustomerID>00400352979S</LegacySystemCustomerID><ServicingOrgUnit><Code>62362</Code><Description>MONTGOMERY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>02/19/2002</LegacyActiveDate><LegacyInactiveDate /></LegacyLink></SCIMSCustomer><ServerInfo><IPAddress>10.24.5.38</IPAddress><HostName>DrillerA007</HostName></ServerInfo></SCIMSCustomerList>";

		ParseScimsResultBC contract = new ParseScimsResultBC(getAgencyToken(),resultXml);
		
		ScimsCustomerBO scimsCustomerBO = 
				this.serviceAgentFacade.getScimsCustomerBS().parseScimsResultXMLForOneCustomer(contract);
		
		assertNotNull(scimsCustomerBO);
		assertEquals("1432871", scimsCustomerBO.getCustomerID());
	}
	
	@Test 
	public void testParseScimsResultXMLForOneCustomer_withMergedIDList() throws Exception{
		String resultXml = "<?xml version='1.0' ?><SCIMSCustomerList><NumberOfCustomersReturned>1</NumberOfCustomersReturned><SCIMSCustomer><CustomerData><CustomerID>10655139</CustomerID><CustomerType><Code>I</Code><Description>Individual</Description></CustomerType><CommonName>ALAN PARRISH</CommonName><IndividualName><Last>PARRISH</Last><First>ALAN</First><Middle></Middle><Prefix></Prefix><Suffix></Suffix></IndividualName><BusinessName></BusinessName><LegalNameIndicator>Y</LegalNameIndicator><BusinessType><BusinessTypeCurrentYear><Code>00</Code><Description>Individual</Description></BusinessTypeCurrentYear><BusinessTypePriorYear1><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear1><BusinessTypePriorYear2><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear2></BusinessType><TaxID>215478559</TaxID><SurrogateTaxID></SurrogateTaxID><TaxIDType><Code>S</Code><Description>Social Security</Description></TaxIDType><TINValidationResult><Code>0</Code><Description>Match</Description></TINValidationResult><InactiveCustomer><Code>N</Code><Description>Active</Description></InactiveCustomer><InactiveCustomerDate /><DuplicateCustomer><Code>R</Code><Description>Resolved</Description></DuplicateCustomer><CustomerAttributes><BirthDate /><BirthDateDetermination><Code /><Description /></BirthDateDetermination><DateOfDeath /><DeathConfirmed /><Gender><Code>02</Code><Description>Male</Description></Gender><GenderDetermination><Code>C</Code><Description>Customer Declared</Description></GenderDetermination><MaritalStatus><Code>UN</Code><Description>Unknown N/A</Description></MaritalStatus><Veteran>U</Veteran><LimitedResourceProducer>N</LimitedResourceProducer><ResidentAlien>U</ResidentAlien><CitizenshipCountry><Code>US</Code></CitizenshipCountry><OriginatingCountry><Code></Code></OriginatingCountry><VotingDistrict>0502</VotingDistrict><LanguagePreference><Code>EN</Code><Description>English</Description></LanguagePreference><ReceiveMail>Y</ReceiveMail><ReceiveFsaMail>Y</ReceiveFsaMail><ReceiveNrcsMail>Y</ReceiveNrcsMail><ReceiveRdMail>N</ReceiveRdMail><ReceiveElectronicMail>N</ReceiveElectronicMail><EmployeeType><Code>00</Code><Description>Not an Employee</Description></EmployeeType></CustomerAttributes></CustomerData><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>N</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8681 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4>2231</ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>N</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8687 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4></ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>Y</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8689 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4></ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Race><RaceType><Code>I</Code><Description>Amer Ind/Alaska</Description></RaceType><RaceTypeDetermination><Code>C</Code><Description>Customer Declared</Description></RaceTypeDetermination></Race><Race><RaceType><Code>W</Code><Description>White</Description></RaceType><RaceTypeDetermination><Code>C</Code><Description>Customer Declared</Description></RaceTypeDetermination></Race><Ethnicity><EthnicityType><Code>N</Code><Description>Not Hispanic</Description></EthnicityType><EthnicityTypeDetermination><Code>C</Code><Description>Customer Declared</Description></EthnicityTypeDetermination></Ethnicity><ProgramParticipation><Program><Code>998</Code><Description>FLP Customer</Description></Program><ProgramOther /><ServicingOrgUnit><Code>60237</Code><Description>CLAY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipant>Y</CurrentParticipant><EverParticipated>Y</EverParticipated><AgreementNumber></AgreementNumber></ProgramParticipation><ProgramParticipation><Program><Code>997</Code><Description>AG NRCS</Description></Program><ProgramOther /><ServicingOrgUnit><Code>60239</Code><Description>PIGGOTT SERVICE CENTER</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipant>Y</CurrentParticipant><EverParticipated>Y</EverParticipated><AgreementNumber></AgreementNumber></ProgramParticipation><LegacyLink><LegacySystem><Code>PNAM</Code><Description /></LegacySystem><LegacyState><Code>05</Code></LegacyState><LegacyCounty><Code>021</Code></LegacyCounty><LegacySystemCustomerID>00215478559S</LegacySystemCustomerID><ServicingOrgUnit><Code>60237</Code><Description>CLAY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>03/23/2021</LegacyActiveDate><LegacyInactiveDate /></LegacyLink><MergedIDList><MergedID>10655138</MergedID><MergedID>10655137</MergedID><MergedID>10655136</MergedID><MergedID>10655135</MergedID><MergedID>10655134</MergedID></MergedIDList></SCIMSCustomer><ServerInfo><IPAddress>10.203.49.99</IPAddress><HostName>fsafwep0257.edc.ds1.usda.gov</HostName></ServerInfo></SCIMSCustomerList>";

		ParseScimsResultBC contract = new ParseScimsResultBC(getAgencyToken(),resultXml);
		
		ScimsCustomerBO scimsCustomerBO = 
				this.serviceAgentFacade.getScimsCustomerBS().parseScimsResultXMLForOneCustomer(contract);
		
		assertNotNull(scimsCustomerBO);
		assertEquals("10655139", scimsCustomerBO.getCustomerID());
		
		List<ScimsMergedCustomerIDHistoryBO> mergedIDList = scimsCustomerBO.getMergedCustomerIdHistorySet();
		
		assertNotNull("Should not be null", mergedIDList);
	/*	assertTrue("Should not be empty", !mergedIDList.isEmpty());

		assertTrue("Size Should be 5", mergedIDList.size()==5);
		
		for(ScimsMergedCustomerIDHistoryBO bo: mergedIDList)
		{
			assertEquals("New CCID should be 10655139","10655139", bo.getNewCustomerId());
			assertNotSame("Old CCID should not be 10655139","10655139", bo.getOldCustomerId());
		}*/
	}
	
	@Test 
	public void testParseScimsResultXMLForOneCustomer_withMergedIDListIncludesNewCCID() throws Exception{
		String resultXml = "<?xml version='1.0' ?><SCIMSCustomerList><NumberOfCustomersReturned>1</NumberOfCustomersReturned><SCIMSCustomer><CustomerData><CustomerID>10655139</CustomerID><CustomerType><Code>I</Code><Description>Individual</Description></CustomerType><CommonName>ALAN PARRISH</CommonName><IndividualName><Last>PARRISH</Last><First>ALAN</First><Middle></Middle><Prefix></Prefix><Suffix></Suffix></IndividualName><BusinessName></BusinessName><LegalNameIndicator>Y</LegalNameIndicator><BusinessType><BusinessTypeCurrentYear><Code>00</Code><Description>Individual</Description></BusinessTypeCurrentYear><BusinessTypePriorYear1><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear1><BusinessTypePriorYear2><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear2></BusinessType><TaxID>215478559</TaxID><SurrogateTaxID></SurrogateTaxID><TaxIDType><Code>S</Code><Description>Social Security</Description></TaxIDType><TINValidationResult><Code>0</Code><Description>Match</Description></TINValidationResult><InactiveCustomer><Code>N</Code><Description>Active</Description></InactiveCustomer><InactiveCustomerDate /><DuplicateCustomer><Code>R</Code><Description>Resolved</Description></DuplicateCustomer><CustomerAttributes><BirthDate /><BirthDateDetermination><Code /><Description /></BirthDateDetermination><DateOfDeath /><DeathConfirmed /><Gender><Code>02</Code><Description>Male</Description></Gender><GenderDetermination><Code>C</Code><Description>Customer Declared</Description></GenderDetermination><MaritalStatus><Code>UN</Code><Description>Unknown N/A</Description></MaritalStatus><Veteran>U</Veteran><LimitedResourceProducer>N</LimitedResourceProducer><ResidentAlien>U</ResidentAlien><CitizenshipCountry><Code>US</Code></CitizenshipCountry><OriginatingCountry><Code></Code></OriginatingCountry><VotingDistrict>0502</VotingDistrict><LanguagePreference><Code>EN</Code><Description>English</Description></LanguagePreference><ReceiveMail>Y</ReceiveMail><ReceiveFsaMail>Y</ReceiveFsaMail><ReceiveNrcsMail>Y</ReceiveNrcsMail><ReceiveRdMail>N</ReceiveRdMail><ReceiveElectronicMail>N</ReceiveElectronicMail><EmployeeType><Code>00</Code><Description>Not an Employee</Description></EmployeeType></CustomerAttributes></CustomerData><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>N</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8681 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4>2231</ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>N</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8687 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4></ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Address><MailingAddress>Y</MailingAddress><ShippingAddress>Y</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>03/23/2021</AddressBeginDate><AddressEndDate /><CurrentAddress>Y</CurrentAddress><SupplementalLine1></SupplementalLine1><SupplementalLine2></SupplementalLine2><SupplementalLine3></SupplementalLine3><InformationLine></InformationLine><DeliveryAddressLine>8689 W MAIN ST</DeliveryAddressLine><CityName>PIGGOTT</CityName><StateAbbreviation>AR</StateAbbreviation><ZipCodeFirst5>72454</ZipCodeFirst5><ZipCodePlus4></ZipCodePlus4><CarrierRouteCode></CarrierRouteCode><ForeignAddressLine></ForeignAddressLine><DeliveryPointBarCode></DeliveryPointBarCode><ContactPersonName></ContactPersonName><ConsularCode></ConsularCode><ForeignRegionName></ForeignRegionName><ForeignRegionCode></ForeignRegionCode><PostalCode></PostalCode><Country><Code>US</Code></Country><State><Code></Code></State><County><Code></Code></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Race><RaceType><Code>I</Code><Description>Amer Ind/Alaska</Description></RaceType><RaceTypeDetermination><Code>C</Code><Description>Customer Declared</Description></RaceTypeDetermination></Race><Race><RaceType><Code>W</Code><Description>White</Description></RaceType><RaceTypeDetermination><Code>C</Code><Description>Customer Declared</Description></RaceTypeDetermination></Race><Ethnicity><EthnicityType><Code>N</Code><Description>Not Hispanic</Description></EthnicityType><EthnicityTypeDetermination><Code>C</Code><Description>Customer Declared</Description></EthnicityTypeDetermination></Ethnicity><ProgramParticipation><Program><Code>998</Code><Description>FLP Customer</Description></Program><ProgramOther /><ServicingOrgUnit><Code>60237</Code><Description>CLAY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipant>Y</CurrentParticipant><EverParticipated>Y</EverParticipated><AgreementNumber></AgreementNumber></ProgramParticipation><ProgramParticipation><Program><Code>997</Code><Description>AG NRCS</Description></Program><ProgramOther /><ServicingOrgUnit><Code>60239</Code><Description>PIGGOTT SERVICE CENTER</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipant>Y</CurrentParticipant><EverParticipated>Y</EverParticipated><AgreementNumber></AgreementNumber></ProgramParticipation><LegacyLink><LegacySystem><Code>PNAM</Code><Description /></LegacySystem><LegacyState><Code>05</Code></LegacyState><LegacyCounty><Code>021</Code></LegacyCounty><LegacySystemCustomerID>00215478559S</LegacySystemCustomerID><ServicingOrgUnit><Code>60237</Code><Description>CLAY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>03/23/2021</LegacyActiveDate><LegacyInactiveDate /></LegacyLink><MergedIDList><MergedID>10655139</MergedID><MergedID>10655138</MergedID><MergedID>10655137</MergedID><MergedID>10655136</MergedID><MergedID>10655135</MergedID><MergedID>10655134</MergedID></MergedIDList></SCIMSCustomer><ServerInfo><IPAddress>10.203.49.99</IPAddress><HostName>fsafwep0257.edc.ds1.usda.gov</HostName></ServerInfo></SCIMSCustomerList>";

		ParseScimsResultBC contract = new ParseScimsResultBC(getAgencyToken(),resultXml);
		
		ScimsCustomerBO scimsCustomerBO = 
				this.serviceAgentFacade.getScimsCustomerBS().parseScimsResultXMLForOneCustomer(contract);
		
		assertNotNull(scimsCustomerBO);
		assertEquals("10655139", scimsCustomerBO.getCustomerID());
		
		List<ScimsMergedCustomerIDHistoryBO> mergedIDList = scimsCustomerBO.getMergedCustomerIdHistorySet();
		
		assertNotNull("Should not be null", mergedIDList);
	/*	assertTrue("Should not be empty", !mergedIDList.isEmpty());

		assertTrue("Size Should be 5", mergedIDList.size()==5);
		
		for(ScimsMergedCustomerIDHistoryBO bo: mergedIDList)
		{
			assertEquals("New CCID should be 10655139","10655139", bo.getNewCustomerId());
			assertNotSame("Old CCID should not be 10655139","10655139", bo.getOldCustomerId());
		}*/
	}
	
	@Test
	public void testRetrieveScimsCustomersByCoreCustomerId() throws Exception{

		ScimsCustomerBS scimsBusinessService = (ScimsCustomerBS) this.serviceAgentFacade.getScimsCustomerBS();
		

		CustomerScimsVO result = scimsBusinessService.retrieveScimsCustomersByCoreCustomerId(getAgencyToken(),1432871);
		
		assertNotNull(result);
		assertEquals("1432871", result.getCustomerId());
	}
	
	protected String createXML() {
		String responseXml = "<?xml version='1.0' ?><SCIMSCustomerList><NumberOfCustomersReturned>1</NumberOfCustomersReturned><SCIMSCustomer><CustomerData><CustomerID>1432871</CustomerID><CustomerType><Code>I</Code><Description>Individual</Description> </CustomerType><CommonName>BUREAU OF LAND MANAGEMENT</CommonName><IndividualName><Last>BUREAU</Last> <First>OF LAND</First><Middle>MANAGEMENT</Middle><Prefix /><Suffix /></IndividualName><BusinessName /><LegalNameIndicator>N</LegalNameIndicator><BusinessType><BusinessTypeCurrentYear><Code>00</Code><Description>Unknown</Description></BusinessTypeCurrentYear><BusinessTypePriorYear1><Code>00</Code><Description>Unknown</Description></BusinessTypePriorYear1><BusinessTypePriorYear2><Code>00</Code><Description>Unknown</Description></BusinessTypePriorYear2></BusinessType><TaxID /><TaxIDType><Code>N</Code><Description>No Tax Id</Description></TaxIDType><InactiveCustomer><Code>N</Code><Description>No</Description></InactiveCustomer><DuplicateCustomer><Code>N</Code><Description>No duplicate</Description></DuplicateCustomer></CustomerData><Address><MailingAddress>Y</MailingAddress><ShippingAddress>N</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>2/9/2002 12:23:32 PM</AddressBeginDate><AddressEndDate /><CurrentAddress>Y</CurrentAddress><SupplementalLine1 /><SupplementalLine2 /><InformationLine /><DeliveryAddressLine /><CityName>VERNAL</CityName><StateAbbreviation>UT</StateAbbreviation><ZipCodeFirst5>84078</ZipCodeFirst5><ZipCodePlus4 /><ForeignAddressLine /><DeliveryPointBarCode /><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County><Latitude>0</Latitude><Longitude>0</Longitude><InactiveAddressDate /></Address><Phone><PhoneType><Code>HM</Code><Description>Home</Description></PhoneType><PhonePrimary>Y</PhonePrimary><PhoneNumber>8017891362</PhoneNumber><PhoneExtension /><PhoneUnlisted>N</PhoneUnlisted><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County></Phone><Race><RaceType><Code>W</Code><Description>White</Description></RaceType><RaceTypeDetermination><Code>E</Code><Description>Employee Observed</Description></RaceTypeDetermination></Race><ProgramParticipation><Program><Code>FSA</Code><Description>FSA Customer</Description></Program><ProgramOther /><ServicingOrgUnit><Code>100524</Code><Description>ROOSEVELT SERVICE CENTER-FSA</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipation>N</CurrentParticipation><EverParticipated>N</EverParticipated><AgreementNumber /></ProgramParticipation><ProgramParticipation><Program><Code>997</Code><Description>NRCS Customer</Description></Program><ProgramOther /><ServicingOrgUnit><Code>100525</Code><Description>ROOSEVELT SERVICE CENTER-NRCS</Description></ServicingOrgUnit><GeneralProgramInterest>Y</GeneralProgramInterest><CurrentParticipation>N</CurrentParticipation><EverParticipated>N</EverParticipated><AgreementNumber /></ProgramParticipation><LegacyLink><LegacySystem><Code>PNAM</Code><Description>FSA Producer</Description></LegacySystem><LegacyState><Code>49</Code></LegacyState><LegacyCounty><Code>013</Code></LegacyCounty><LegacySystemCustomerID>00490130764T</LegacySystemCustomerID><ServicingOrgUnit><Code>100524</Code><Description>ROOSEVELT SERVICE CENTER-FSA</Description></ServicingOrgUnit><LegacyActiveDate>2/9/2002</LegacyActiveDate><LegacyInactiveDate /></LegacyLink></SCIMSCustomer></SCIMSCustomerList>";
		return responseXml;
	}
	
	protected String createXML_ForeignPersonInd() {
		String responseXml = "<?xml version='1.0' ?><SCIMSCustomerList><NumberOfCustomersReturned>1</NumberOfCustomersReturned><SCIMSCustomer><CustomerData><CustomerID>1533454</CustomerID><CustomerType><Code>I</Code><Description>Individual</Description></CustomerType><CommonName>CRAIG ROGERS</CommonName><IndividualName><Last>ROGERS</Last><First>CRAIG</First><Middle /><Prefix>MR</Prefix><Suffix /></IndividualName><BusinessName /><LegalNameIndicator>N</LegalNameIndicator><BusinessType><BusinessTypeCurrentYear><Code>00</Code><Description>Individual</Description></BusinessTypeCurrentYear><BusinessTypePriorYear1><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear1><BusinessTypePriorYear2><Code>00</Code><Description>Individual</Description></BusinessTypePriorYear2></BusinessType><TaxID>400352979</TaxID><TaxIDType><Code>S</Code><Description>Social Security</Description></TaxIDType><InactiveCustomer><Code>N</Code><Description>Active</Description></InactiveCustomer><InactiveCustomerDate /><DuplicateCustomer><Code>N</Code><Description>No duplicate</Description></DuplicateCustomer><CustomerAttributes><BirthDate>07/18/1974</BirthDate><BirthDateDetermination><Code>C </Code><Description>Customer Declared</Description></BirthDateDetermination><Gender><Code>02</Code><Description>Male</Description></Gender><GenderDetermination><Code>E</Code><Description>Employee Observed</Description></GenderDetermination><MaritalStatus><Code>MA</Code><Description>Married</Description></MaritalStatus><Veteran>N</Veteran><LimitedResourceProducer>N</LimitedResourceProducer><ResidentAlien>U</ResidentAlien><CitizenshipCountry><Code>US</Code></CitizenshipCountry><OriginatingCountry><Code /></OriginatingCountry><VotingDistrict>2106</VotingDistrict><LanguagePreference><Code>EN</Code><Description>English</Description></LanguagePreference><ReceiveMail>Y</ReceiveMail><ReceiveFsaMail>Y</ReceiveFsaMail><ReceiveNrcsMail>Y</ReceiveNrcsMail><ReceiveRdMail>N</ReceiveRdMail><EmployeeType><Code>00</Code><Description>Not an Employee</Description></EmployeeType></CustomerAttributes></CustomerData><Address><MailingAddress>Y</MailingAddress><ShippingAddress>N</ShippingAddress><StreetAddress>N</StreetAddress><AddressBeginDate>02/09/2002</AddressBeginDate><AddressEndDate /><CurrentAddress>Y</CurrentAddress><SupplementalLine1 /><SupplementalLine2 /><InformationLine /><DeliveryAddressLine>640 CHANDALAND LN</DeliveryAddressLine><CityName>WINCHESTER</CityName><StateAbbreviation>KY</StateAbbreviation><ZipCodeFirst5>40391</ZipCodeFirst5><ZipCodePlus4>9691</ZipCodePlus4><CarrierRouteCode>R002</CarrierRouteCode><ForeignAddressLine /><DeliveryPointBarCode>404</DeliveryPointBarCode><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County><Latitude>0.00000000</Latitude><Longitude>0.00000000</Longitude><InactiveAddressDate /></Address><Phone><PhoneType><Code>HM</Code><Description>Home</Description></PhoneType><PhonePrimary>Y</PhonePrimary><PhoneNumber>6067446652</PhoneNumber><PhoneExtension /><PhoneUnlisted>N</PhoneUnlisted><Country><Code>US</Code></Country><State><Code /></State><County><Code /></County></Phone><Race><RaceType><Code>W</Code><Description>White</Description></RaceType><RaceTypeDetermination><Code>E</Code><Description>Employee Observed</Description></RaceTypeDetermination></Race><Ethnicity><EthnicityType><Code>N</Code><Description>Not Hispanic</Description></EthnicityType><EthnicityTypeDetermination><Code>E</Code><Description>Employee Observed</Description></EthnicityTypeDetermination></Ethnicity><LegacyLink><LegacySystem><Code>PNAM</Code><Description>FSA Producer</Description></LegacySystem><LegacyState><Code>21</Code></LegacyState><LegacyCounty><Code>049</Code></LegacyCounty><LegacySystemCustomerID>00400352979S</LegacySystemCustomerID><ServicingOrgUnit><Code>62220</Code><Description>CLARK COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>02/09/2002</LegacyActiveDate><LegacyInactiveDate /></LegacyLink><LegacyLink><LegacySystem><Code>PNAM</Code><Description>FSA Producer</Description></LegacySystem><LegacyState><Code>21</Code></LegacyState><LegacyCounty><Code>173</Code></LegacyCounty><LegacySystemCustomerID>00400352979S</LegacySystemCustomerID><ServicingOrgUnit><Code>62362</Code><Description>MONTGOMERY COUNTY FARM SERVICE AGENCY</Description></ServicingOrgUnit><LegacyActiveDate>02/19/2002</LegacyActiveDate><LegacyInactiveDate /></LegacyLink></SCIMSCustomer><ServerInfo><IPAddress>10.24.5.38</IPAddress><HostName>DrillerA007</HostName></ServerInfo></SCIMSCustomerList>";
		return responseXml;		
	}
	public SCIMSClientProxy getScimsClientProxy() {
		return scimsClientProxy;
	}
	public void setScimsClientProxy(SCIMSClientProxy scimsClientProxy) {
		this.scimsClientProxy = scimsClientProxy;
	}
}
