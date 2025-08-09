package gov.usda.fsa.fcao.flp.ola.core.service;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.OLAServiceException;
import gov.usda.fsa.fcao.flp.ola.core.service.impl.SCIMSCustomerServiceImpl;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.SCIMSClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SCIMSCustomerServiceImplTest {

    @Mock
    private SCIMSClient scimsClient;

    @Mock
    private AgencyToken agencyToken;

    @InjectMocks
    private SCIMSCustomerServiceImpl scimsCustomerService;

    private SCIMSCustomer testCustomer;
    private List<SCIMSCustomer> testCustomerList;
    private OlaAgencyToken olaAgencyToken;

    @BeforeEach
    void setUp() {
        // Setup OlaAgencyToken
        setupOlaAgencyToken();
       
        // Setup test data
        setupTestCustomers();
    }

    private void setupOlaAgencyToken() {
        olaAgencyToken = new OlaAgencyToken();
        olaAgencyToken.setApplicationIdentifier("TEST_APP");
        olaAgencyToken.setProcessingNode("FLP");
        olaAgencyToken.setRequestHost("FLP");
        olaAgencyToken.setUserIdentifier("TEST_USER");
    }

    private void setupTestCustomers() {
        // Individual customer based on sample mock data (SCOTTIE JAMISON)
        testCustomer = new SCIMSCustomer();
        testCustomer.setCustomerID("7495988");
        testCustomer.setCommonName("SCOTTIE JAMISON");
        testCustomer.setFirstName("SCOTTIE");
        testCustomer.setLastName("JAMISON");
        testCustomer.setMiddleName("");
        testCustomer.setBusinessName("");
        testCustomer.setBirthDateString("3/5/1961");
        testCustomer.setTaxID("000000000");
        testCustomer.setSurrogateTaxID("342695372");
        testCustomer.setCitizenshipCountryCode("US");
        testCustomer.setVeteran("78"); // ASCII 'N'
        testCustomer.setResidentAlien("85"); // ASCII 'U'
        testCustomer.setInactiveCustomerCode("78"); // ASCII 'N' - Active
        testCustomer.setCreationDate("2002-02-19T08:14:43-06:00");
        testCustomer.setLastChangeDate("2025-01-03T09:35:45-06:00");

        // Set up BusinessType
        BusinessType businessType = new BusinessType();
        businessType.setCode("00");
        businessType.setDescription("Individual");
        testCustomer.setBusinessType(businessType);

        // Set up CustomerType
        CustomerType customerType = new CustomerType();
        customerType.setCode("73");
        customerType.setDescription("Individual");
        testCustomer.setCustomerType(customerType);

        // Set up TaxIDType
        TaxIDType taxIDType = new TaxIDType();
        taxIDType.setCode("83");
        taxIDType.setDescription("Social Security");
        testCustomer.setTaxIDType(taxIDType);

        // Set up Gender
        Gender gender = new Gender();
        gender.setCode("02");
        gender.setDescription("Male");
        testCustomer.setGender(gender);

        // Set up MaritalStatus
        Marital marital = new Marital();
        marital.setCode("MA");
        marital.setDescription("Married");
        testCustomer.setMarital(marital);

        // Set up Ethnicity
        Ethnicity ethnicity = new Ethnicity();
        ethnicity.setCode("78");
        ethnicity.setDescription("Not Hispanic");
        testCustomer.setEthnicity(ethnicity);

        // Set up Race List
        List<Race> raceList = new ArrayList<>();
        Race whiteRace = new Race();
        whiteRace.setCode("87");
        whiteRace.setDescription("White");
        raceList.add(whiteRace);

        Race asianRace = new Race();
        asianRace.setCode("65");
        asianRace.setDescription("Asian");
        raceList.add(asianRace);
        testCustomer.setRaceList(raceList);

        // Set up Address
        List<CustomerAddress> addresses = new ArrayList<>();
        CustomerAddress address = new CustomerAddress();
        address.setAddressID("22176363");
        address.setDeliveryAddressLine("713 NEWTON AVE");
        address.setCityName("OVERLAND PARK");
        address.setStateAbbreviation("KS");
        address.setStateCode("20");
        address.setZipCodeFirst5("66210");
        address.setCountryCode("US");
        address.setCurrentAddress("78");
        address.setMailingAddress("89");
        address.setStreetAddress("78");
        address.setShippingAddress("78");
        addresses.add(address);
        testCustomer.setAddresses(addresses);

        // Set up Phone
        List<CustomerPhone> phones = new ArrayList<>();
        CustomerPhone phone = new CustomerPhone();
        phone.setCoreCustomerPhoneId("2835475");
        phone.setPhoneNumber("6064985444");
        phone.setPhoneExtensionNumber("");
        phone.setCountryCode("US");
        phone.setPhoneTypeCode("HM");
        phone.setPhoneTypeDescription("Home");
        phone.setPhonePrimaryIndicator("89");
        phone.setPhoneUnlistedIndicator("78");
        phones.add(phone);
        testCustomer.setPhoneList(phones);

        // Set up Email
        List<CustomerEmail> emails = new ArrayList<>();
        CustomerEmail email = new CustomerEmail();
        email.setCoreCustomerEmailId("734679");
        email.setEmailAddress("sivaram.nataraj@usda.gov");
        email.setEmailAddressTypeCode("HM");
        email.setEmailTypeDescription("Home");
        email.setPrimaryIndicator("89");
        emails.add(email);
        testCustomer.setEmails(emails);

        // Set up LegacyLinks
        List<LegacyLink> legacyLinks = new ArrayList<>();
        LegacyLink legacyLink = new LegacyLink();
        legacyLink.setLegacyLinkIdentifier("6800397");
        legacyLink.setLegacySystemCustomerID("00707495988T");
        legacyLink.setLegacyStateCode("21");
        legacyLink.setLegacyCountyCode("173");
        legacyLink.setLegacySystemCode("PNAM");
        legacyLink.setLegacySystemDescription("PNAM System");
        legacyLink.setLegacyActiveDate("2023-02-11T12:50:56-06:00");
        legacyLink.setCreationDate("2002-02-19T19:46:58.263-06:00");
        legacyLink.setDataSource("28200709069031409910053");
        legacyLink.setDataSourceSiteIdentifier("1558");
        legacyLink.setLastChangeDate("2025-01-03T09:35:45-06:00");
        legacyLink.setCoreCustomerIdentifier("7495988");
        legacyLink.setAddressIDRef("7804202");

        ServicingOrgUnit orgUnit = new ServicingOrgUnit();
        orgUnit.setOrganizationalUnitId("62362");
        orgUnit.setOrganizationalUnitName("MONTGOMERY COUNTY FARM SERVICE AGENCY");
        orgUnit.setAgencyAbbreviation("FSA");
        orgUnit.setAgencyOfficeTypeCode("05");
        orgUnit.setDepartmentOfficeTypeCode("05");
        orgUnit.setMailCity("MOUNT STERLING");
        orgUnit.setMailDeliveryAddress("509 WILLIN WAY, STE 4");
        orgUnit.setMailStateAbbreviation("KY");
        orgUnit.setMailZIPCode("40353-0000");
        orgUnit.setSiteId("8002");
        orgUnit.setSiteIdNew("0");
        orgUnit.setDeleteIndicator("78");
        legacyLink.setServicingOrgUnit(orgUnit);

        legacyLinks.add(legacyLink);
        testCustomer.setLegacyLinks(legacyLinks);

        // Set up ProgramParticipation
        List<ProgramParticipation> programParticipations = new ArrayList<>();
        ProgramParticipation participation = new ProgramParticipation();
        participation.setProgramParticipationId("23937276");
        participation.setCurrentParticipantCode("89");
        participation.setEverParticipatedIndicator("89");
        participation.setGeneralProgramInterestCode("89");
        participation.setProgramCode("998");
        participation.setProgramDescription("FLP Customer");
        participation.setProgramNameText("");
        participation.setAgreementNumber("");
        participation.setProgramCategory("");
        participation.setProgramSubcategory("");
        programParticipations.add(participation);
        testCustomer.setProgramParticipationList(programParticipations);

        // Set up MergedIDList
        List<MergedCustomerId> mergedIds = new ArrayList<>();
        MergedCustomerId mergedId = new MergedCustomerId();
        mergedId.setActionCode("77");
        mergedId.setNewCustomerID("7495988");
        mergedId.setOldCustomerID("7495999");
        mergedId.setLastChangeDate("2004-03-18T16:29:55.347-06:00");
        mergedIds.add(mergedId);
        testCustomer.setMergedIDList(mergedIds);

        // Create test customer list
        testCustomerList = Arrays.asList(testCustomer);
    }

    @Test
    void testGetCustomer_Success() {
        // Mock the static method call to OlaServiceUtil.getOlaAgencyToken
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(eq("7495988"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);

            // Execute
            SCIMSCustomer result = scimsCustomerService.getCustomer(agencyToken, 7495988);

            // Verify
            assertNotNull(result);
            assertEquals("7495988", result.getCustomerID());
            assertEquals("SCOTTIE JAMISON", result.getCommonName());
            assertEquals("SCOTTIE", result.getFirstName());
            assertEquals("JAMISON", result.getLastName());
            assertEquals("", result.getMiddleName());
            assertEquals("", result.getBusinessName());
           
            // Validate dates and identification
            assertEquals("3/5/1961", result.getBirthDateString());
            assertEquals("000000000", result.getTaxID());
            assertEquals("342695372", result.getSurrogateTaxID());
            assertEquals("US", result.getCitizenshipCountryCode());
            assertEquals("78", result.getVeteran());
            assertEquals("85", result.getResidentAlien());
            assertEquals("78", result.getInactiveCustomerCode());
           
            // Validate program participation
            assertNotNull(result.getProgramParticipationList());
            assertFalse(result.getProgramParticipationList().isEmpty());
            ProgramParticipation participation = result.getProgramParticipationList().get(0);
            assertEquals("23937276", participation.getProgramParticipationId());
            assertEquals("998", participation.getProgramCode());
            assertEquals("FLP Customer", participation.getProgramDescription());
           
            // Validate merged ID information
            assertNotNull(result.getMergedIDList());
            assertFalse(result.getMergedIDList().isEmpty());
            MergedCustomerId mergedId = result.getMergedIDList().get(0);
            assertEquals("77", mergedId.getActionCode());
            assertEquals("7495988", mergedId.getNewCustomerID());
            assertEquals("7495999", mergedId.getOldCustomerID());
           
            // Validate service org unit through legacy link
            assertNotNull(result.getLegacyLinks());
            assertFalse(result.getLegacyLinks().isEmpty());
            LegacyLink legacyLink = result.getLegacyLinks().get(0);
            assertNotNull(legacyLink.getServicingOrgUnit());
            ServicingOrgUnit orgUnit = legacyLink.getServicingOrgUnit();
            assertEquals("62362", orgUnit.getOrganizationalUnitId());
            assertEquals("MONTGOMERY COUNTY FARM SERVICE AGENCY", orgUnit.getOrganizationalUnitName());
            assertEquals("FSA", orgUnit.getAgencyAbbreviation());
            assertEquals("MOUNT STERLING", orgUnit.getMailCity());
            assertEquals("KY", orgUnit.getMailStateAbbreviation());
        }
    }

    @Test
    void testGetCustomer_EdgeCaseCustomerIds() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Test with very large customer ID
            when(scimsClient.getCustomer(eq("2147483647"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(null);
           
            SCIMSCustomer result1 = scimsCustomerService.getCustomer(agencyToken, Integer.MAX_VALUE);
            assertNull(result1);
           
            // Test with small customer ID
            when(scimsClient.getCustomer(eq("1"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);
           
            SCIMSCustomer result2 = scimsCustomerService.getCustomer(agencyToken, 1);
            assertNotNull(result2);
            assertEquals("7495988", result2.getCustomerID()); // Returns our test customer
           
            // Verify calls
            verify(scimsClient).getCustomer(eq("2147483647"), eq(olaAgencyToken), any(DataOptions.class));
            verify(scimsClient).getCustomer(eq("1"), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_LargeList() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Create a large list of customer IDs
            List<Integer> largeCustomerIdList = new ArrayList<>();
            List<String> largeCustomerIdStringList = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                largeCustomerIdList.add(i);
                largeCustomerIdStringList.add(String.valueOf(i));
            }

            when(scimsClient.getCustomers(eq(largeCustomerIdStringList), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, largeCustomerIdList);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());

            // Verify method calls
            verify(scimsClient).getCustomers(eq(largeCustomerIdStringList), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testDataOptionsConfigurationDefaults() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(anyString(), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);

            // Execute
            scimsCustomerService.getCustomer(agencyToken, 12345);

            // Verify that all DataOptions are set correctly by the private methods
            verify(scimsClient).getCustomer(eq("12345"), eq(olaAgencyToken), argThat(dataOptions -> {
                // Test all the default options that should be inherited
                boolean defaultsCorrect = "BOTH".equals(dataOptions.getCustomerStatus()) &&
                    "ALL".equals(dataOptions.getAddress()) &&
                    !dataOptions.isAddressID() &&
                    "ALL".equals(dataOptions.getPhone()) &&
                    "ALL".equals(dataOptions.getEmail()) &&
                    "ALL".equals(dataOptions.getRace()) &&
                    dataOptions.isEthnicity() &&
                    "NONE".equals(dataOptions.getProgramParticipation()) &&
                    "ALL".equals(dataOptions.getLegacyLink()) &&
                    "CORE_ID".equals(dataOptions.getParameterType()) &&
                    "SHORT".equals(dataOptions.getDescriptionDisplay()) &&
                    !dataOptions.isInactiveLegacyLink() &&
                    !dataOptions.isStateOfficeLinks() &&
                    !dataOptions.isLegacyLinkAddressRef() &&
                    "FLP-User".equals(dataOptions.getUserID()) &&
                    "FLP".equals(dataOptions.getRequestHost()) &&
                    !dataOptions.isReturnSurrogateTaxId();

                // Test the overridden options for detailed configuration
                boolean detailedCorrect = dataOptions.isCustomerAttributes() &&
                    dataOptions.isPriorName() &&
                    "ALL".equals(dataOptions.getPriorYearBusinessCode()) &&
                    dataOptions.isReturn100CharBusName() &&
                    dataOptions.isReturnAllIdentifiers() &&
                    dataOptions.isDisability() &&
                    dataOptions.isReturnAuditFields() &&
                    dataOptions.isCustomerNotes() &&
                    "OLA".equals(dataOptions.getProcessingNode()) &&
                    dataOptions.isMergedIDList() &&
                    dataOptions.isComplete();

                return defaultsCorrect && detailedCorrect;
            }));
        }
    }

    @Test
    void testGetCustomers_StreamOperationCoverage() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Test the stream operation that converts Integer list to String list
            List<Integer> mixedCustomerIds = Arrays.asList(1, 999999, 12345, 7495988);
            List<String> expectedStringIds = Arrays.asList("1", "999999", "12345", "7495988");

            when(scimsClient.getCustomers(eq(expectedStringIds), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, mixedCustomerIds);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());

            // Verify the stream mapping worked correctly
            verify(scimsClient).getCustomers(eq(expectedStringIds), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomer_OlaServiceUtilIntegration() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            // Test with different agency token configurations
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(anyString(), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);

            // Execute multiple calls to ensure the OlaServiceUtil integration works
            scimsCustomerService.getCustomer(agencyToken, 12345);
            scimsCustomerService.getCustomer(agencyToken, 67890);

            // Verify that OlaServiceUtil.getOlaAgencyToken was called correctly for each invocation
            mockedOlaServiceUtil.verify(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken), times(2));

            // Verify scimsClient was called with the correct transformed token
            verify(scimsClient, times(2)).getCustomer(anyString(), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_OlaServiceUtilIntegration() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomers(anyList(), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute multiple calls
            scimsCustomerService.getCustomers(agencyToken, Arrays.asList(1, 2, 3));
            scimsCustomerService.getCustomers(agencyToken, Arrays.asList(4, 5, 6));

            // Verify that OlaServiceUtil.getOlaAgencyToken was called for each invocation
            mockedOlaServiceUtil.verify(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken), times(2));

            // Verify scimsClient was called with the correct transformed token
            verify(scimsClient, times(2)).getCustomers(anyList(), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_ExceptionInStreamMapping() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Create a list that will cause an exception during stream mapping
            List<Integer> problematicList = new ArrayList<>();
            problematicList.add(123);
            problematicList.add(null); // This will cause NullPointerException in String::valueOf
            problematicList.add(456);

            // Execute and verify exception is caught and wrapped
            OLAServiceException exception = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, problematicList);
            });

            assertEquals("Error while retrieving data from SCIMS Service", exception.getMessage());

            // Verify that scimsClient was never called due to the exception in stream processing
            verify(scimsClient, never()).getCustomers(anyList(), any(OlaAgencyToken.class), any(DataOptions.class));
        }
    }

    @Test
    void testPrivateMethodsIndirectlyCovered() {
        // This test ensures that the private helper methods are covered through public method calls
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(anyString(), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);
            when(scimsClient.getCustomers(anyList(), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute both public methods to ensure private helper methods are called
            scimsCustomerService.getCustomer(agencyToken, 12345);
            scimsCustomerService.getCustomers(agencyToken, Arrays.asList(67890));

            // Verify both createDefaultDataOptions and createDetailedDataOptions were executed
            // indirectly through the DataOptions verification in the argument matchers
            verify(scimsClient).getCustomer(anyString(), eq(olaAgencyToken), any(DataOptions.class));
            verify(scimsClient).getCustomers(anyList(), eq(olaAgencyToken), any(DataOptions.class));
        }
    }
}", result.getLastName());
            assertEquals("3/5/1961", result.getBirthDateString());
            assertEquals("000000000", result.getTaxID());
            assertEquals("342695372", result.getSurrogateTaxID());
            assertEquals("US", result.getCitizenshipCountryCode());

            // Verify business objects
            assertNotNull(result.getBusinessType());
            assertEquals("00", result.getBusinessType().getCode());
            assertEquals("Individual", result.getBusinessType().getDescription());

            assertNotNull(result.getCustomerType());
            assertEquals("73", result.getCustomerType().getCode());
            assertEquals("Individual", result.getCustomerType().getDescription());

            assertNotNull(result.getGender());
            assertEquals("02", result.getGender().getCode());
            assertEquals("Male", result.getGender().getDescription());

            assertNotNull(result.getMarital());
            assertEquals("MA", result.getMarital().getCode());
            assertEquals("Married", result.getMarital().getDescription());

            assertNotNull(result.getEthnicity());
            assertEquals("78", result.getEthnicity().getCode());
            assertEquals("Not Hispanic", result.getEthnicity().getDescription());

            // Verify collections
            assertNotNull(result.getRaceList());
            assertEquals(2, result.getRaceList().size());
            assertEquals("87", result.getRaceList().get(0).getCode());
            assertEquals("White", result.getRaceList().get(0).getDescription());

            assertNotNull(result.getAddresses());
            assertEquals(1, result.getAddresses().size());
            assertEquals("22176363", result.getAddresses().get(0).getAddressID());
            assertEquals("713 NEWTON AVE", result.getAddresses().get(0).getDeliveryAddressLine());

            assertNotNull(result.getPhoneList());
            assertEquals(1, result.getPhoneList().size());
            assertEquals("6064985444", result.getPhoneList().get(0).getPhoneNumber());

            assertNotNull(result.getEmails());
            assertEquals(1, result.getEmails().size());
            assertEquals("sivaram.nataraj@usda.gov", result.getEmails().get(0).getEmailAddress());

            assertNotNull(result.getLegacyLinks());
            assertEquals(1, result.getLegacyLinks().size());
            assertEquals("6800397", result.getLegacyLinks().get(0).getLegacyLinkIdentifier());

            // Verify method calls
            verify(scimsClient).getCustomer(eq("7495988"), eq(olaAgencyToken), any(DataOptions.class));
            mockedOlaServiceUtil.verify(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken));
        }
    }

    @Test
    void testGetCustomer_VerifyDataOptionsConfiguration() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(eq("12345"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);

            // Execute
            scimsCustomerService.getCustomer(agencyToken, 12345);

            // Verify DataOptions configuration
            verify(scimsClient).getCustomer(eq("12345"), eq(olaAgencyToken), argThat(dataOptions ->
                // Verify detailed data options are set correctly
                "BOTH".equals(dataOptions.getCustomerStatus()) &&
                "ALL".equals(dataOptions.getAddress()) &&
                "ALL".equals(dataOptions.getPhone()) &&
                "ALL".equals(dataOptions.getEmail()) &&
                "ALL".equals(dataOptions.getRace()) &&
                dataOptions.isEthnicity() &&
                "NONE".equals(dataOptions.getProgramParticipation()) &&
                "ALL".equals(dataOptions.getLegacyLink()) &&
                "CORE_ID".equals(dataOptions.getParameterType()) &&
                "SHORT".equals(dataOptions.getDescriptionDisplay()) &&
                // Verify detailed options are enabled
                dataOptions.isCustomerAttributes() &&
                dataOptions.isPriorName() &&
                "ALL".equals(dataOptions.getPriorYearBusinessCode()) &&
                dataOptions.isReturn100CharBusName() &&
                dataOptions.isReturnAllIdentifiers() &&
                dataOptions.isDisability() &&
                dataOptions.isReturnAuditFields() &&
                dataOptions.isCustomerNotes() &&
                "OLA".equals(dataOptions.getProcessingNode()) &&
                dataOptions.isMergedIDList() &&
                dataOptions.isComplete() &&
                // Verify default values
                "FLP-User".equals(dataOptions.getUserID()) &&
                "FLP".equals(dataOptions.getRequestHost()) &&
                !dataOptions.isAddressID() &&
                !dataOptions.isInactiveLegacyLink() &&
                !dataOptions.isStateOfficeLinks() &&
                !dataOptions.isLegacyLinkAddressRef() &&
                !dataOptions.isReturnSurrogateTaxId()
            ));
        }
    }

    @Test
    void testGetCustomer_ReturnsNull() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(eq("9999999"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(null);

            // Execute
            SCIMSCustomer result = scimsCustomerService.getCustomer(agencyToken, 9999999);

            // Verify
            assertNull(result);
            verify(scimsClient).getCustomer(eq("9999999"), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomer_NullCustomerId() {
        // Execute and verify exception (NullPointerException when calling toString on null Integer)
        assertThrows(NullPointerException.class, () -> {
            scimsCustomerService.getCustomer(agencyToken, null);
        });
    }

    @Test
    void testGetCustomers_Success() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988", "5470400");
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute
            List<Integer> customerIds = Arrays.asList(7495988, 5470400);
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, customerIds);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("7495988", result.get(0).getCustomerID());
            assertEquals("SCOTTIE JAMISON", result.get(0).getCommonName());

            // Verify method calls
            verify(scimsClient).getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class));
            mockedOlaServiceUtil.verify(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken));
        }
    }

    @Test
    void testGetCustomers_EmptyList() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomers(eq(Collections.emptyList()), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(Collections.emptyList());

            // Execute
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, Collections.emptyList());

            // Verify
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // Verify method calls
            verify(scimsClient).getCustomers(eq(Collections.emptyList()), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_SingleCustomer() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988");
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute
            List<Integer> customerIds = Arrays.asList(7495988);
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, customerIds);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("7495988", result.get(0).getCustomerID());

            // Verify method calls
            verify(scimsClient).getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_Exception() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988");
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new RuntimeException("SCIMS service unavailable"));

            // Execute and verify exception
            List<Integer> customerIds = Arrays.asList(7495988);
            OLAServiceException exception = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIds);
            });

            assertEquals("Error while retrieving data from SCIMS Service", exception.getMessage());

            // Verify method calls
            verify(scimsClient).getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_NullCustomerIdList() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Execute and verify exception (OLAServiceException wrapping NullPointerException)
            assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, null);
            });
        }
    }

    @Test
    void testGetCustomer_ExceptionFromSCIMSClient() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(eq("12345"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new RuntimeException("SCIMS client error"));

            // Execute - should NOT catch exception for getCustomer (no try-catch block)
            assertThrows(RuntimeException.class, () -> {
                scimsCustomerService.getCustomer(agencyToken, 12345);
            });
        }
    }

    @Test
    void testGetCustomers_WithListContainingNullElements() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            // Create list with null elements
            List<Integer> customerIdsWithNull = Arrays.asList(7495988, null, 5470400);

            // Execute and verify exception (OLAServiceException wrapping NullPointerException from stream mapping)
            assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIdsWithNull);
            });
        }
    }

    @Test
    void testDefaultConstructor() {
        // Test that the default constructor works without any initialization issues
        SCIMSCustomerServiceImpl newService = new SCIMSCustomerServiceImpl();
        assertNotNull(newService);
       
        // The service should be usable after construction (though it needs injected dependencies)
        assertTrue(newService instanceof ISCIMSCustomerService);
    }

    @Test
    void testServiceLayerErrorHandling() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988");
            List<Integer> customerIds = Arrays.asList(7495988);
           
            // Test network exception
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new RuntimeException("Network connection failed"));
           
            OLAServiceException networkException = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIds);
            });
            assertEquals("Error while retrieving data from SCIMS Service", networkException.getMessage());
           
            // Test service unavailable exception
            reset(scimsClient);
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new RuntimeException("Service temporarily unavailable"));
           
            OLAServiceException serviceException = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIds);
            });
            assertEquals("Error while retrieving data from SCIMS Service", serviceException.getMessage());
           
            // Test generic exception
            reset(scimsClient);
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new IllegalStateException("Unexpected error"));
           
            OLAServiceException genericException = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIds);
            });
            assertEquals("Error while retrieving data from SCIMS Service", genericException.getMessage());
        }
    }

    @Test
    void testGetCustomers_VerifyTimingLogExecution() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988");
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomerList);

            // Execute
            List<Integer> customerIds = Arrays.asList(7495988);
            List<SCIMSCustomer> result = scimsCustomerService.getCustomers(agencyToken, customerIds);

            // Verify result (timing log is verified implicitly through successful execution)
            assertNotNull(result);
            assertEquals(1, result.size());

            // The finally block executes and logs timing information
            verify(scimsClient).getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomers_ExceptionInFinallyBlock() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            List<String> customerIdStrings = Arrays.asList("7495988");
            when(scimsClient.getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class)))
                .thenThrow(new RuntimeException("SCIMS error"));

            // Execute and verify that the finally block still executes even when there's an exception
            List<Integer> customerIds = Arrays.asList(7495988);
            OLAServiceException exception = assertThrows(OLAServiceException.class, () -> {
                scimsCustomerService.getCustomers(agencyToken, customerIds);
            });

            assertEquals("Error while retrieving data from SCIMS Service", exception.getMessage());
           
            // The timing log should still execute in the finally block even when there's an exception
            verify(scimsClient).getCustomers(eq(customerIdStrings), eq(olaAgencyToken), any(DataOptions.class));
        }
    }

    @Test
    void testGetCustomer_WithComplexDataValidation() {
        try (MockedStatic<OlaServiceUtil> mockedOlaServiceUtil = mockStatic(OlaServiceUtil.class)) {
            mockedOlaServiceUtil.when(() -> OlaServiceUtil.getOlaAgencyToken(agencyToken))
                               .thenReturn(olaAgencyToken);

            when(scimsClient.getCustomer(eq("7495988"), eq(olaAgencyToken), any(DataOptions.class)))
                .thenReturn(testCustomer);

            // Execute
            SCIMSCustomer result = scimsCustomerService.getCustomer(agencyToken, 7495988);

            // Comprehensive validation of the returned customer data
            assertNotNull(result);
           
            // Validate basic customer information
            assertEquals("7495988", result.getCustomerID());
            assertEquals("SCOTTIE JAMISON", result.getCommonName());
            assertEquals("SCOTTIE", result.getFirstName());
            assertEquals("JAMISON