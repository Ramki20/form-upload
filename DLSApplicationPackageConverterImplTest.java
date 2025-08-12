package gov.usda.fsa.fcao.flp.ola.core.api.converter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.usda.fsa.fcao.flp.ola.core.api.model.DLSApplicationPackageSummaryAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.api.sub.model.DLSLoanRequestDetails;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApplicationPackageSummary;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.LoanRequestDetails;

@ExtendWith(MockitoExtension.class)
class DLSApplicationPackageConverterImplTest {

    private DLSApplicationPackageConverterImpl converter;
    
    private ApplicationPackageSummary mockApplicationPackageSummary;
    private Date mockDate1;
    private Date mockDate2;
    private Date mockDate3;
    private Date mockDate4;

    @BeforeEach
    void setUp() {
        converter = new DLSApplicationPackageConverterImpl();
        setupMockData();
    }

    private void setupMockData() {
        mockDate1 = new Date(1175835600000L); // 2007-04-06
        mockDate2 = new Date(1176700800000L); // 2007-04-16
        mockDate3 = new Date(1224648000000L); // 2008-10-22
        mockDate4 = new Date(1189598400000L); // 2007-09-12

        // Setup ApplicationPackageSummary with complete data
        mockApplicationPackageSummary = new ApplicationPackageSummary();
        mockApplicationPackageSummary.setApplicationPackageIdentifier("22051");
        mockApplicationPackageSummary.setApplicationPackageReceiveDate("2007-04-06T00:00:00-05:00");
        mockApplicationPackageSummary.setOnlineApplicationNumber("6961");
        mockApplicationPackageSummary.setActiveIndicator(true);
        mockApplicationPackageSummary.setDirty(false);
        mockApplicationPackageSummary.setLightWeightInd(false);

        // Setup loan request details with all fields
        LoanRequestDetails loanRequest1 = createLoanRequestDetails(
            "22391", "FO", "41", "036", 
            new BigDecimal("40000.00"), new BigDecimal("40000.00"), 
            "07", "CPR", "2008-10-22T00:00:00-05:00", 
            "2007-04-16T00:00:00-05:00", null, new BigDecimal("40000.00"), null
        );

        LoanRequestDetails loanRequest2 = createLoanRequestDetails(
            "22392", "OL-A", "44", "112", 
            new BigDecimal("30000.00"), new BigDecimal("30000.00"), 
            "03", "CPR", "2007-09-12T00:00:00-05:00", 
            "2007-04-16T00:00:00-05:00", null, new BigDecimal("30000.00"), null
        );

        LoanRequestDetails loanRequest3 = createLoanRequestDetails(
            "22393", "OL-T", "44", "051", 
            new BigDecimal("40000.00"), new BigDecimal("40000.00"), 
            "04", "CPR", "2007-05-30T00:00:00-05:00", 
            "2007-04-16T00:00:00-05:00", "2007-04-20T00:00:00-05:00", 
            new BigDecimal("40000.00"), "1"
        );

        mockApplicationPackageSummary.setLoanRequestDetailsList(
            Arrays.asList(loanRequest1, loanRequest2, loanRequest3)
        );
    }

    private LoanRequestDetails createLoanRequestDetails(String identifier, String typeCode, 
            String programCode, String assistanceTypeCode, BigDecimal requestAmount, 
            BigDecimal approvedAmount, String loanNumber, String statusCode, String statusDate, 
            String firstIncompleteDate, String secondIncompleteDate, BigDecimal obligationAmount, 
            String appealStatusCode) {
        
        LoanRequestDetails details = new LoanRequestDetails();
        details.setDirectLoanRequestIdentifier(identifier);
        details.setDirectLoanRequestTypeCode(typeCode);
        details.setDirectLoanProgramCode(programCode);
        details.setFlpAssistanceTypeCode(assistanceTypeCode);
        details.setLoanRequestAmount(requestAmount);
        details.setLoanApprovedAmount(approvedAmount);
        details.setLoanNumber(loanNumber);
        details.setLoanRequestStatusCode(statusCode);
        details.setLoanRequestStatusDate(statusDate);
        details.setFirstIncompleteApplicationLetterSendDate(firstIncompleteDate);
        details.setSecondIncompleteApplicationLetterSendDate(secondIncompleteDate);
        details.setObligationAmount(obligationAmount);
        details.setRequestAppealStatusCode(appealStatusCode);
        details.setActiveIndicator(true);
        details.setDirty(false);
        details.setLightWeightInd(false);
        
        return details;
    }

    @Test
    void testConvert_SingleObject_Success() {
        // Given
        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-06T00:00:00-05:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-16T00:00:00-05:00")).thenReturn(mockDate2);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2008-10-22T00:00:00-05:00")).thenReturn(mockDate3);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-09-12T00:00:00-05:00")).thenReturn(mockDate4);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-05-30T00:00:00-05:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-20T00:00:00-05:00")).thenReturn(mockDate2);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(mockApplicationPackageSummary);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(22051), result.getApplicationPackageIdentifier());
            assertEquals(mockDate1, result.getApplicationPackageReceiveDate());
            assertEquals(Integer.valueOf(6961), result.getOnlineApplicationNumber());
            
            assertNotNull(result.getDlsLoanRequestDetailsList());
            assertEquals(3, result.getDlsLoanRequestDetailsList().size());

            // Verify first loan request details
            DLSLoanRequestDetails loanDetail1 = result.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(22391), loanDetail1.getDirectLoanRequestIdentifier());
            assertEquals("FO", loanDetail1.getDirectLoanRequestTypeCode());
            assertEquals("41", loanDetail1.getDirectLoanProgramCode());
            assertEquals("036", loanDetail1.getFlpAssistanceTypeCode());
            assertEquals(new BigDecimal("40000.00"), loanDetail1.getLoanRequestAmount());
            assertEquals(new BigDecimal("40000.00"), loanDetail1.getLoanApprovedAmount());
            assertEquals("07", loanDetail1.getLoanNumber());
            assertEquals("CPR", loanDetail1.getLoanRequestStatusCode());
            assertEquals(mockDate3, loanDetail1.getLoanRequestStatusDate());
            assertEquals(mockDate2, loanDetail1.getFirstIncompleteApplicationLetterSendDate());
            assertEquals(new BigDecimal("40000.00"), loanDetail1.getObligationAmount());
            assertNull(loanDetail1.getRequestAppealStatusCode());
            assertNull(loanDetail1.getSecondIncompleteApplicationLetterSendDate());

            // Verify second loan request details
            DLSLoanRequestDetails loanDetail2 = result.getDlsLoanRequestDetailsList().get(1);
            assertEquals(Integer.valueOf(22392), loanDetail2.getDirectLoanRequestIdentifier());
            assertEquals("OL-A", loanDetail2.getDirectLoanRequestTypeCode());
            assertEquals("44", loanDetail2.getDirectLoanProgramCode());
            assertEquals("112", loanDetail2.getFlpAssistanceTypeCode());
            assertEquals(new BigDecimal("30000.00"), loanDetail2.getLoanRequestAmount());
            assertEquals(new BigDecimal("30000.00"), loanDetail2.getLoanApprovedAmount());
            assertEquals("03", loanDetail2.getLoanNumber());
            assertEquals("CPR", loanDetail2.getLoanRequestStatusCode());
            assertEquals(mockDate4, loanDetail2.getLoanRequestStatusDate());

            // Verify third loan request details (with second incomplete date and appeal status)
            DLSLoanRequestDetails loanDetail3 = result.getDlsLoanRequestDetailsList().get(2);
            assertEquals(Integer.valueOf(22393), loanDetail3.getDirectLoanRequestIdentifier());
            assertEquals("OL-T", loanDetail3.getDirectLoanRequestTypeCode());
            assertEquals("44", loanDetail3.getDirectLoanProgramCode());
            assertEquals("051", loanDetail3.getFlpAssistanceTypeCode());
            assertEquals(mockDate2, loanDetail3.getSecondIncompleteApplicationLetterSendDate());
            assertEquals("1", loanDetail3.getRequestAppealStatusCode());
        }
    }

    @Test
    void testConvert_SingleObject_WithNullValues() {
        // Given
        ApplicationPackageSummary summaryWithNulls = new ApplicationPackageSummary();
        summaryWithNulls.setApplicationPackageIdentifier("12345");
        summaryWithNulls.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithNulls.setOnlineApplicationNumber("9999");

        LoanRequestDetails loanWithNulls = new LoanRequestDetails();
        loanWithNulls.setDirectLoanRequestIdentifier("111");
        loanWithNulls.setDirectLoanRequestTypeCode("FO");
        loanWithNulls.setDirectLoanProgramCode("41");
        // Leave other fields null
        
        summaryWithNulls.setLoanRequestDetailsList(Arrays.asList(loanWithNulls));

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate(null)).thenReturn(null);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(summaryWithNulls);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(12345), result.getApplicationPackageIdentifier());
            assertEquals(mockDate1, result.getApplicationPackageReceiveDate());
            assertEquals(Integer.valueOf(9999), result.getOnlineApplicationNumber());
            
            assertNotNull(result.getDlsLoanRequestDetailsList());
            assertEquals(1, result.getDlsLoanRequestDetailsList().size());

            DLSLoanRequestDetails loanDetail = result.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(111), loanDetail.getDirectLoanRequestIdentifier());
            assertEquals("FO", loanDetail.getDirectLoanRequestTypeCode());
            assertEquals("41", loanDetail.getDirectLoanProgramCode());
            assertNull(loanDetail.getFlpAssistanceTypeCode());
            assertNull(loanDetail.getLoanRequestAmount());
            assertNull(loanDetail.getLoanApprovedAmount());
            assertNull(loanDetail.getLoanNumber());
            assertNull(loanDetail.getLoanRequestStatusCode());
            assertNull(loanDetail.getLoanRequestStatusDate());
            assertNull(loanDetail.getFirstIncompleteApplicationLetterSendDate());
            assertNull(loanDetail.getSecondIncompleteApplicationLetterSendDate());
            assertNull(loanDetail.getObligationAmount());
            assertNull(loanDetail.getRequestAppealStatusCode());
        }
    }

    @Test
    void testConvert_SingleObject_WithEmptyLoanRequestList() {
        // Given
        ApplicationPackageSummary summaryWithEmptyList = new ApplicationPackageSummary();
        summaryWithEmptyList.setApplicationPackageIdentifier("12345");
        summaryWithEmptyList.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithEmptyList.setOnlineApplicationNumber("9999");
        summaryWithEmptyList.setLoanRequestDetailsList(new ArrayList<>());

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(summaryWithEmptyList);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(12345), result.getApplicationPackageIdentifier());
            assertEquals(mockDate1, result.getApplicationPackageReceiveDate());
            assertEquals(Integer.valueOf(9999), result.getOnlineApplicationNumber());
            
            assertNotNull(result.getDlsLoanRequestDetailsList());
            assertTrue(result.getDlsLoanRequestDetailsList().isEmpty());
        }
    }

    @Test
    void testConvert_SingleObject_WithNullLoanRequestList() {
        // Given
        ApplicationPackageSummary summaryWithNullList = new ApplicationPackageSummary();
        summaryWithNullList.setApplicationPackageIdentifier("12345");
        summaryWithNullList.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithNullList.setOnlineApplicationNumber("9999");
        summaryWithNullList.setLoanRequestDetailsList(null);

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                converter.convert(summaryWithNullList);
            });
        }
    }

    @Test
    void testConvert_SingleObject_WithInvalidNumberFormat() {
        // Given
        ApplicationPackageSummary summaryWithInvalidNumber = new ApplicationPackageSummary();
        summaryWithInvalidNumber.setApplicationPackageIdentifier("invalid_number");
        summaryWithInvalidNumber.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithInvalidNumber.setOnlineApplicationNumber("9999");
        summaryWithInvalidNumber.setLoanRequestDetailsList(new ArrayList<>());

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);

            // When & Then
            assertThrows(NumberFormatException.class, () -> {
                converter.convert(summaryWithInvalidNumber);
            });
        }
    }

    @Test
    void testConvert_List_Success() {
        // Given
        ApplicationPackageSummary summary2 = new ApplicationPackageSummary();
        summary2.setApplicationPackageIdentifier("551885");
        summary2.setApplicationPackageReceiveDate("2023-10-25T00:00:00-05:00");
        summary2.setOnlineApplicationNumber("6970");
        
        LoanRequestDetails loanRequest = createLoanRequestDetails(
            "618874", "FO", "41", null, 
            new BigDecimal("1000.00"), null, 
            null, "APL", "2024-01-29T00:00:00-06:00", 
            null, null, null, "1"
        );
        
        summary2.setLoanRequestDetailsList(Arrays.asList(loanRequest));

        List<ApplicationPackageSummary> summaryList = Arrays.asList(mockApplicationPackageSummary, summary2);

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            // Mock all the date conversions
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-06T00:00:00-05:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-16T00:00:00-05:00")).thenReturn(mockDate2);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2008-10-22T00:00:00-05:00")).thenReturn(mockDate3);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-09-12T00:00:00-05:00")).thenReturn(mockDate4);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-05-30T00:00:00-05:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2007-04-20T00:00:00-05:00")).thenReturn(mockDate2);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-10-25T00:00:00-05:00")).thenReturn(mockDate2);
            mockedUtil.when(() -> OlaServiceUtil.getDate("2024-01-29T00:00:00-06:00")).thenReturn(mockDate3);
            mockedUtil.when(() -> OlaServiceUtil.getDate(null)).thenReturn(null);

            // When
            List<DLSApplicationPackageSummaryAPIModel> result = converter.convert(summaryList);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            
            // Verify first item
            DLSApplicationPackageSummaryAPIModel firstResult = result.get(0);
            assertEquals(Integer.valueOf(22051), firstResult.getApplicationPackageIdentifier());
            assertEquals(Integer.valueOf(6961), firstResult.getOnlineApplicationNumber());
            assertEquals(3, firstResult.getDlsLoanRequestDetailsList().size());

            // Verify second item
            DLSApplicationPackageSummaryAPIModel secondResult = result.get(1);
            assertEquals(Integer.valueOf(551885), secondResult.getApplicationPackageIdentifier());
            assertEquals(Integer.valueOf(6970), secondResult.getOnlineApplicationNumber());
            assertEquals(1, secondResult.getDlsLoanRequestDetailsList().size());
            
            DLSLoanRequestDetails secondLoanDetail = secondResult.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(618874), secondLoanDetail.getDirectLoanRequestIdentifier());
            assertEquals("FO", secondLoanDetail.getDirectLoanRequestTypeCode());
            assertEquals("41", secondLoanDetail.getDirectLoanProgramCode());
            assertEquals(new BigDecimal("1000.00"), secondLoanDetail.getLoanRequestAmount());
            assertEquals("APL", secondLoanDetail.getLoanRequestStatusCode());
            assertEquals("1", secondLoanDetail.getRequestAppealStatusCode());
            assertNull(secondLoanDetail.getFlpAssistanceTypeCode());
            assertNull(secondLoanDetail.getLoanApprovedAmount());
            assertNull(secondLoanDetail.getLoanNumber());
        }
    }

    @Test
    void testConvert_List_EmptyList() {
        // Given
        List<ApplicationPackageSummary> emptyList = new ArrayList<>();

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = converter.convert(emptyList);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvert_List_NullList() {
        // Given
        List<ApplicationPackageSummary> nullList = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            converter.convert(nullList);
        });
    }

    @Test
    void testConvert_List_WithNullElements() {
        // Given
        List<ApplicationPackageSummary> listWithNulls = Arrays.asList(mockApplicationPackageSummary, null);

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate(anyString())).thenReturn(mockDate1);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                converter.convert(listWithNulls);
            });
        }
    }

    @Test
    void testConvert_SingleObject_WithZeroValues() {
        // Given
        ApplicationPackageSummary summaryWithZeros = new ApplicationPackageSummary();
        summaryWithZeros.setApplicationPackageIdentifier("0");
        summaryWithZeros.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithZeros.setOnlineApplicationNumber("0");

        LoanRequestDetails loanWithZeros = new LoanRequestDetails();
        loanWithZeros.setDirectLoanRequestIdentifier("0");
        loanWithZeros.setDirectLoanRequestTypeCode("FO");
        loanWithZeros.setDirectLoanProgramCode("41");
        loanWithZeros.setLoanRequestAmount(BigDecimal.ZERO);
        loanWithZeros.setLoanApprovedAmount(BigDecimal.ZERO);
        loanWithZeros.setObligationAmount(BigDecimal.ZERO);
        
        summaryWithZeros.setLoanRequestDetailsList(Arrays.asList(loanWithZeros));

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate(null)).thenReturn(null);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(summaryWithZeros);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(0), result.getApplicationPackageIdentifier());
            assertEquals(Integer.valueOf(0), result.getOnlineApplicationNumber());
            
            DLSLoanRequestDetails loanDetail = result.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(0), loanDetail.getDirectLoanRequestIdentifier());
            assertEquals(BigDecimal.ZERO, loanDetail.getLoanRequestAmount());
            assertEquals(BigDecimal.ZERO, loanDetail.getLoanApprovedAmount());
            assertEquals(BigDecimal.ZERO, loanDetail.getObligationAmount());
        }
    }

    @Test
    void testConvert_SingleObject_WithNegativeNumbers() {
        // Given
        ApplicationPackageSummary summaryWithNegatives = new ApplicationPackageSummary();
        summaryWithNegatives.setApplicationPackageIdentifier("-1");
        summaryWithNegatives.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithNegatives.setOnlineApplicationNumber("-1");

        LoanRequestDetails loanWithNegatives = new LoanRequestDetails();
        loanWithNegatives.setDirectLoanRequestIdentifier("-1");
        loanWithNegatives.setDirectLoanRequestTypeCode("FO");
        loanWithNegatives.setDirectLoanProgramCode("41");
        loanWithNegatives.setLoanRequestAmount(new BigDecimal("-1000.00"));
        
        summaryWithNegatives.setLoanRequestDetailsList(Arrays.asList(loanWithNegatives));

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate(null)).thenReturn(null);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(summaryWithNegatives);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(-1), result.getApplicationPackageIdentifier());
            assertEquals(Integer.valueOf(-1), result.getOnlineApplicationNumber());
            
            DLSLoanRequestDetails loanDetail = result.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(-1), loanDetail.getDirectLoanRequestIdentifier());
            assertEquals(new BigDecimal("-1000.00"), loanDetail.getLoanRequestAmount());
        }
    }

    @Test
    void testConvert_SingleObject_WithLargeNumbers() {
        // Given
        ApplicationPackageSummary summaryWithLargeNumbers = new ApplicationPackageSummary();
        summaryWithLargeNumbers.setApplicationPackageIdentifier("2147483647"); // Integer.MAX_VALUE
        summaryWithLargeNumbers.setApplicationPackageReceiveDate("2023-01-01T00:00:00-06:00");
        summaryWithLargeNumbers.setOnlineApplicationNumber("2147483647");

        LoanRequestDetails loanWithLargeNumbers = new LoanRequestDetails();
        loanWithLargeNumbers.setDirectLoanRequestIdentifier("2147483647");
        loanWithLargeNumbers.setDirectLoanRequestTypeCode("FO");
        loanWithLargeNumbers.setDirectLoanProgramCode("41");
        loanWithLargeNumbers.setLoanRequestAmount(new BigDecimal("999999999999.99"));
        
        summaryWithLargeNumbers.setLoanRequestDetailsList(Arrays.asList(loanWithLargeNumbers));

        try (MockedStatic<OlaServiceUtil> mockedUtil = mockStatic(OlaServiceUtil.class)) {
            mockedUtil.when(() -> OlaServiceUtil.getDate("2023-01-01T00:00:00-06:00")).thenReturn(mockDate1);
            mockedUtil.when(() -> OlaServiceUtil.getDate(null)).thenReturn(null);

            // When
            DLSApplicationPackageSummaryAPIModel result = converter.convert(summaryWithLargeNumbers);

            // Then
            assertNotNull(result);
            assertEquals(Integer.valueOf(Integer.MAX_VALUE), result.getApplicationPackageIdentifier());
            assertEquals(Integer.valueOf(Integer.MAX_VALUE), result.getOnlineApplicationNumber());
            
            DLSLoanRequestDetails loanDetail = result.getDlsLoanRequestDetailsList().get(0);
            assertEquals(Integer.valueOf(Integer.MAX_VALUE), loanDetail.getDirectLoanRequestIdentifier());
            assertEquals(new BigDecimal("999999999999.99"), loanDetail.getLoanRequestAmount());
        }
    }
}