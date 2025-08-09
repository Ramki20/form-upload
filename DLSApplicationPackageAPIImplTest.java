package gov.usda.fsa.fcao.flp.ola.core.api.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gov.usda.fsa.fcao.flp.ola.core.api.converter.IDLSApplicationPackageConverter;
import gov.usda.fsa.fcao.flp.ola.core.api.model.DLSApplicationPackageSummaryAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.service.exception.OlaCoreAPIException;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaAgencyToken;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.FLPGatewayClient;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApplicationPackageSummary;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.LoanRequestDetails;

@ExtendWith(MockitoExtension.class)
class DLSApplicationPackageAPIImplTest {

    @Mock
    private IDLSApplicationPackageConverter dlsApplicationPackageConverter;

    @Mock
    private FLPGatewayClient flpGatewayClient;

    @Mock
    private OlaAgencyToken olaAgencyToken;

    @InjectMocks
    private DLSApplicationPackageAPIImpl dlsApplicationPackageAPI;

    private ApplicationPackageSummary mockApplicationPackageSummary;
    private DLSApplicationPackageSummaryAPIModel mockAPIModel;
    private List<ApplicationPackageSummary> mockApplicationPackageSummaryList;
    private List<DLSApplicationPackageSummaryAPIModel> mockAPIModelList;

    @BeforeEach
    void setUp() {
        setupMockData();
    }

    private void setupMockData() {
        // Setup mock ApplicationPackageSummary
        mockApplicationPackageSummary = new ApplicationPackageSummary();
        mockApplicationPackageSummary.setApplicationPackageIdentifier("22051");
        mockApplicationPackageSummary.setApplicationPackageReceiveDate("2007-04-06T00:00:00-05:00");
        mockApplicationPackageSummary.setOnlineApplicationNumber("6961");
        mockApplicationPackageSummary.setActiveIndicator(true);
        mockApplicationPackageSummary.setDirty(false);
        mockApplicationPackageSummary.setLightWeightInd(false);

        // Setup loan request details
        LoanRequestDetails loanRequest1 = createLoanRequestDetails("22391", "FO", "41", "036", 
            new BigDecimal("40000.00"), new BigDecimal("40000.00"), "07", "CPR", 
            "2008-10-22T00:00:00-05:00", "2007-04-16T00:00:00-05:00", null);

        LoanRequestDetails loanRequest2 = createLoanRequestDetails("22392", "OL-A", "44", "112", 
            new BigDecimal("30000.00"), new BigDecimal("30000.00"), "03", "CPR", 
            "2007-09-12T00:00:00-05:00", "2007-04-16T00:00:00-05:00", null);

        mockApplicationPackageSummary.setLoanRequestDetailsList(Arrays.asList(loanRequest1, loanRequest2));

        // Setup mock API model
        mockAPIModel = new DLSApplicationPackageSummaryAPIModel();
        mockAPIModel.setApplicationPackageIdentifier(22051);
        mockAPIModel.setOnlineApplicationNumber(6961);

        // Setup list data
        ApplicationPackageSummary summary2 = new ApplicationPackageSummary();
        summary2.setApplicationPackageIdentifier("551885");
        summary2.setApplicationPackageReceiveDate("2023-10-25T00:00:00-05:00");
        summary2.setOnlineApplicationNumber("6970");
        
        LoanRequestDetails loanRequest3 = createLoanRequestDetails("618874", "FO", "41", null, 
            new BigDecimal("1000.00"), null, null, "APL", 
            "2024-01-29T00:00:00-06:00", null, "1");
        
        summary2.setLoanRequestDetailsList(Arrays.asList(loanRequest3));

        mockApplicationPackageSummaryList = Arrays.asList(mockApplicationPackageSummary, summary2);

        DLSApplicationPackageSummaryAPIModel apiModel2 = new DLSApplicationPackageSummaryAPIModel();
        apiModel2.setApplicationPackageIdentifier(551885);
        apiModel2.setOnlineApplicationNumber(6970);

        mockAPIModelList = Arrays.asList(mockAPIModel, apiModel2);
    }

    private LoanRequestDetails createLoanRequestDetails(String identifier, String typeCode, 
            String programCode, String assistanceTypeCode, BigDecimal requestAmount, 
            BigDecimal approvedAmount, String loanNumber, String statusCode, String statusDate, 
            String firstIncompleteDate, String appealStatusCode) {
        
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
        details.setRequestAppealStatusCode(appealStatusCode);
        details.setObligationAmount(approvedAmount);
        details.setActiveIndicator(true);
        details.setDirty(false);
        details.setLightWeightInd(false);
        
        return details;
    }

    @Test
    void testGetApplicationPackageSummary_Success() {
        // Given
        Integer onlineApplicationNumber = 6961;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("6961"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummary);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummary))
            .thenReturn(mockAPIModel);

        // When
        DLSApplicationPackageSummaryAPIModel result = dlsApplicationPackageAPI
            .getApplicationPackageSummary(onlineApplicationNumber);

        // Then
        assertNotNull(result);
        assertEquals(mockAPIModel, result);
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("6961", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummary);
    }

    @Test
    void testGetApplicationPackageSummary_NullInput() {
        // Given
        Integer onlineApplicationNumber = null;

        // When & Then
        OlaCoreAPIException exception = assertThrows(OlaCoreAPIException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummary(onlineApplicationNumber);
        });

        assertEquals("Application Nnmber is Mandatory.", exception.getMessage());
        verifyNoInteractions(flpGatewayClient);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummary_NoDataFound() {
        // Given
        Integer onlineApplicationNumber = 9999;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("9999"), eq(olaAgencyToken))).thenReturn(null);

        // When
        DLSApplicationPackageSummaryAPIModel result = dlsApplicationPackageAPI
            .getApplicationPackageSummary(onlineApplicationNumber);

        // Then
        assertNull(result);
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("9999", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummary_GatewayClientThrowsException() {
        // Given
        Integer onlineApplicationNumber = 6961;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("6961"), eq(olaAgencyToken))).thenThrow(new RuntimeException("Gateway error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummary(onlineApplicationNumber);
        });

        assertEquals("Gateway error", exception.getMessage());
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("6961", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_Success() {
        // Given
        Integer coreCustomerIdentifier = 7495988;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("7495988"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummaryList);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummaryList))
            .thenReturn(mockAPIModelList);

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = dlsApplicationPackageAPI
            .getApplicationPackageSummaryList(coreCustomerIdentifier);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockAPIModelList, result);
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("7495988", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummaryList);
    }

    @Test
    void testGetApplicationPackageSummaryList_NullInput() {
        // Given
        Integer coreCustomerIdentifier = null;

        // When & Then
        OlaCoreAPIException exception = assertThrows(OlaCoreAPIException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummaryList(coreCustomerIdentifier);
        });

        assertEquals("SCIMS Customer Identifier is Mandatory.", exception.getMessage());
        verifyNoInteractions(flpGatewayClient);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_EmptyList() {
        // Given
        Integer coreCustomerIdentifier = 7495988;
        List<ApplicationPackageSummary> emptyList = new ArrayList<>();
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("7495988"), eq(olaAgencyToken))).thenReturn(emptyList);

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = dlsApplicationPackageAPI
            .getApplicationPackageSummaryList(coreCustomerIdentifier);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("7495988", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_NullList() {
        // Given
        Integer coreCustomerIdentifier = 7495988;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("7495988"), eq(olaAgencyToken))).thenReturn(null);

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = dlsApplicationPackageAPI
            .getApplicationPackageSummaryList(coreCustomerIdentifier);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("7495988", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_GatewayClientThrowsException() {
        // Given
        Integer coreCustomerIdentifier = 7495988;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("7495988"), eq(olaAgencyToken))).thenThrow(new RuntimeException("Gateway error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummaryList(coreCustomerIdentifier);
        });

        assertEquals("Gateway error", exception.getMessage());
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("7495988", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_ConverterThrowsException() {
        // Given
        Integer coreCustomerIdentifier = 7495988;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("7495988"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummaryList);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummaryList))
            .thenThrow(new RuntimeException("Converter error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummaryList(coreCustomerIdentifier);
        });

        assertEquals("Converter error", exception.getMessage());
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("7495988", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummaryList);
    }

    @Test
    void testGetApplicationPackageSummary_ConverterThrowsException() {
        // Given
        Integer onlineApplicationNumber = 6961;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("6961"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummary);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummary))
            .thenThrow(new RuntimeException("Converter error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dlsApplicationPackageAPI.getApplicationPackageSummary(onlineApplicationNumber);
        });

        assertEquals("Converter error", exception.getMessage());
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("6961", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummary);
    }

    @Test
    void testGetApplicationPackageSummary_WithZeroInput() {
        // Given
        Integer onlineApplicationNumber = 0;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("0"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummary);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummary))
            .thenReturn(mockAPIModel);

        // When
        DLSApplicationPackageSummaryAPIModel result = dlsApplicationPackageAPI
            .getApplicationPackageSummary(onlineApplicationNumber);

        // Then
        assertNotNull(result);
        assertEquals(mockAPIModel, result);
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("0", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummary);
    }

    @Test
    void testGetApplicationPackageSummaryList_WithZeroInput() {
        // Given
        Integer coreCustomerIdentifier = 0;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("0"), eq(olaAgencyToken))).thenReturn(mockApplicationPackageSummaryList);
        when(dlsApplicationPackageConverter.convert(mockApplicationPackageSummaryList))
            .thenReturn(mockAPIModelList);

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = dlsApplicationPackageAPI
            .getApplicationPackageSummaryList(coreCustomerIdentifier);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockAPIModelList, result);
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("0", olaAgencyToken);
        verify(dlsApplicationPackageConverter).convert(mockApplicationPackageSummaryList);
    }

    @Test
    void testGetApplicationPackageSummary_WithNegativeInput() {
        // Given
        Integer onlineApplicationNumber = -1;
        
        when(flpGatewayClient.getApplicationPackageSummaryByOnlineApplicationNumber(
            eq("-1"), eq(olaAgencyToken))).thenReturn(null);

        // When
        DLSApplicationPackageSummaryAPIModel result = dlsApplicationPackageAPI
            .getApplicationPackageSummary(onlineApplicationNumber);

        // Then
        assertNull(result);
        verify(flpGatewayClient).getApplicationPackageSummaryByOnlineApplicationNumber("-1", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }

    @Test
    void testGetApplicationPackageSummaryList_WithNegativeInput() {
        // Given
        Integer coreCustomerIdentifier = -1;
        
        when(flpGatewayClient.getApplicationPackageSummaryListByCoreCustomerIdentifier(
            eq("-1"), eq(olaAgencyToken))).thenReturn(new ArrayList<>());

        // When
        List<DLSApplicationPackageSummaryAPIModel> result = dlsApplicationPackageAPI
            .getApplicationPackageSummaryList(coreCustomerIdentifier);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(flpGatewayClient).getApplicationPackageSummaryListByCoreCustomerIdentifier("-1", olaAgencyToken);
        verifyNoInteractions(dlsApplicationPackageConverter);
    }
}