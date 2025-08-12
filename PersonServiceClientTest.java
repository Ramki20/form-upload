package gov.usda.fsa.fcao.flp.ola.core.shared.service;

import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApiResponse;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Employment;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.HealthCheck;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PersonServiceClient personServiceClient;

    private String personBaseUrl;

    @BeforeEach
    void setUp() {
        personBaseUrl = "https://test-person-service.com/api";
        ReflectionTestUtils.setField(personServiceClient, "personBaseUrl", personBaseUrl);
    }

    // Tests for isHealthy() method
    @Test
    void testIsHealthy_Success() {
        // Arrange
        HealthCheck expectedHealthCheck = new HealthCheck();
        expectedHealthCheck.setMessage("Service is healthy");
        expectedHealthCheck.setData(true);
        expectedHealthCheck.setTotalElements(1);
        expectedHealthCheck.setStatus("SUCCESS");

        String expectedUrl = personBaseUrl + "/isHealthy";
        ResponseEntity<HealthCheck> responseEntity = new ResponseEntity<>(expectedHealthCheck, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedUrl, HealthCheck.class)).thenReturn(responseEntity);

        // Act
        boolean result = personServiceClient.isHealthy();

        // Assert
        assertEquals(Boolean.TRUE, result);

        verify(restTemplate).getForEntity(expectedUrl, HealthCheck.class);
    }

    @Test
    void testIsHealthy_Unhealthy() {
        // Arrange
        HealthCheck expectedHealthCheck = new HealthCheck();
        expectedHealthCheck.setMessage("Service is down");
        expectedHealthCheck.setData(false);
        expectedHealthCheck.setTotalElements(0);
        expectedHealthCheck.setStatus("ERROR");

        String expectedUrl = personBaseUrl + "/isHealthy";
        ResponseEntity<HealthCheck> responseEntity = new ResponseEntity<>(expectedHealthCheck, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedUrl, HealthCheck.class)).thenReturn(responseEntity);

        // Act
        boolean result = personServiceClient.isHealthy();

        // Assert
        assertNotNull(result);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    void testIsHealthy_HttpClientErrorException() {
        // Arrange
        String expectedUrl = personBaseUrl + "/isHealthy";
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(expectedUrl, HealthCheck.class)).thenThrow(exception);

        // Act & Assert
        HttpClientErrorException thrownException = assertThrows(HttpClientErrorException.class, () -> {
            personServiceClient.isHealthy();
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrownException.getStatusCode());
        verify(restTemplate).getForEntity(expectedUrl, HealthCheck.class);
    }

    @Test
    void testIsHealthy_NullResponse() {
        // Arrange
        String expectedUrl = personBaseUrl + "/isHealthy";
        ResponseEntity<HealthCheck> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedUrl, HealthCheck.class)).thenReturn(responseEntity);

        // Act
        boolean result = personServiceClient.isHealthy();

        // Assert
        assertEquals(Boolean.FALSE, result);
        verify(restTemplate).getForEntity(expectedUrl, HealthCheck.class);
    }
	
	@Test
    void testIsHealthy_ResponseNotNullButBodyNull() {
        // Arrange
        String expectedUrl = personBaseUrl + "/isHealthy";
        // Create ResponseEntity with non-null response but null body
        ResponseEntity<HealthCheck> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedUrl, HealthCheck.class)).thenReturn(responseEntity);

        // Act
        boolean result = personServiceClient.isHealthy();

        // Assert
        assertEquals(Boolean.FALSE, result);
        verify(restTemplate).getForEntity(expectedUrl, HealthCheck.class);
    }	

    // Tests for getPersonsByEauthIds() method
    @Test
    void testGetPersonsByEauthIds_Success_SinglePerson() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        // Create Employment objects
        Employment employment1 = new Employment();
        employment1.setEmployeeId("180500");
        employment1.setEmployeeType("Employee");
        employment1.setEmployeeStatus("T");
        employment1.setAgencyCode("CE");
        employment1.setAgencyAbbreviation("FSACO");
        employment1.setOrganizationCode("CE0413009100000000");
        employment1.setSupervisorEauthId("");

        Employment employment2 = new Employment();
        employment2.setEmployeeId("180279");
        employment2.setEmployeeType("Employee");
        employment2.setEmployeeStatus("A");
        employment2.setAgencyCode("FA");
        employment2.setAgencyAbbreviation("FSA");
        employment2.setOrganizationCode("FA0518001202000000");
        employment2.setSupervisorEauthId("28200310169021039002");

        // Create Person object
        Person person = new Person();
        person.setUsdaEauthId("28032009029507000000279");
        person.setFirstName("JOHN");
        person.setLastName("DOE");
        person.setMiddleName("N");
        person.setTitle("Dir");
        person.setActive("Y");
        person.setEmail("john.doe@example.com");
        person.setEmploymentList(Arrays.asList(employment1, employment2));

        // Create ApiResponse
        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Found 1 person(s).");
        expectedResponse.setData(Collections.singletonList(person));
        expectedResponse.setTotalElements(1);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("Found 1 person(s).", result.getMessage());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(Integer.valueOf(1), result.getTotalElements());
       
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
       
        Person resultPerson = result.getData().get(0);
        assertEquals("28032009029507000000279", resultPerson.getUsdaEauthId());
        assertEquals("JOHN", resultPerson.getFirstName());
        assertEquals("DOE", resultPerson.getLastName());
        assertEquals("N", resultPerson.getMiddleName());
        assertEquals("Dir", resultPerson.getTitle());
        assertEquals("Y", resultPerson.getActive());
        assertEquals("john.doe@example.com", resultPerson.getEmail());
       
        // Verify employment list
        assertNotNull(resultPerson.getEmploymentList());
        assertEquals(2, resultPerson.getEmploymentList().size());
       
        Employment resultEmployment1 = resultPerson.getEmploymentList().get(0);
        assertEquals("180500", resultEmployment1.getEmployeeId());
        assertEquals("Employee", resultEmployment1.getEmployeeType());
        assertEquals("T", resultEmployment1.getEmployeeStatus());
        assertEquals("CE", resultEmployment1.getAgencyCode());
        assertEquals("FSACO", resultEmployment1.getAgencyAbbreviation());
        assertEquals("CE0413009100000000", resultEmployment1.getOrganizationCode());
        assertEquals("", resultEmployment1.getSupervisorEauthId());
       
        Employment resultEmployment2 = resultPerson.getEmploymentList().get(1);
        assertEquals("180279", resultEmployment2.getEmployeeId());
        assertEquals("Employee", resultEmployment2.getEmployeeType());
        assertEquals("A", resultEmployment2.getEmployeeStatus());
        assertEquals("FA", resultEmployment2.getAgencyCode());
        assertEquals("FSA", resultEmployment2.getAgencyAbbreviation());
        assertEquals("FA0518001202000000", resultEmployment2.getOrganizationCode());
        assertEquals("28200310169021039002", resultEmployment2.getSupervisorEauthId());

        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void testGetPersonsByEauthIds_Success_MultiplePeople() {
        // Arrange
        String eauthIds = "28032009029507000000279,28032009029507000000280";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        // Create first person
        Person person1 = new Person();
        person1.setUsdaEauthId("28032009029507000000279");
        person1.setFirstName("JOHN");
        person1.setLastName("DOE");
        person1.setEmail("john.doe@example.com");
        person1.setEmploymentList(Collections.emptyList());

        // Create second person
        Person person2 = new Person();
        person2.setUsdaEauthId("28032009029507000000280");
        person2.setFirstName("JANE");
        person2.setLastName("SMITH");
        person2.setEmail("jane.smith@example.com");
        person2.setEmploymentList(Collections.emptyList());

        // Create ApiResponse
        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Found 2 person(s).");
        expectedResponse.setData(Arrays.asList(person1, person2));
        expectedResponse.setTotalElements(2);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("Found 2 person(s).", result.getMessage());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(Integer.valueOf(2), result.getTotalElements());
       
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
       
        assertEquals("JOHN", result.getData().get(0).getFirstName());
        assertEquals("JANE", result.getData().get(1).getFirstName());
    }

    @Test
    void testGetPersonsByEauthIds_NoRecordsFound() {
        // Arrange
        String eauthIds = "280320090295070000002717";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        // Create ApiResponse for no records found
        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("No results found.");
        expectedResponse.setData(null);
        expectedResponse.setTotalElements(0);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("No results found.", result.getMessage());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(Integer.valueOf(0), result.getTotalElements());
        assertNull(result.getData());

        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void testGetPersonsByEauthIds_EmptyList() {
        // Arrange
        String eauthIds = "invalid-id";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        // Create ApiResponse with empty list
        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("No results found.");
        expectedResponse.setData(Collections.emptyList());
        expectedResponse.setTotalElements(0);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("No results found.", result.getMessage());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(Integer.valueOf(0), result.getTotalElements());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testGetPersonsByEauthIds_HttpClientErrorException_NotFound() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenThrow(exception);

        // Act & Assert
        HttpClientErrorException thrownException = assertThrows(HttpClientErrorException.class, () -> {
            personServiceClient.getPersonsByEauthIds(eauthIds);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrownException.getStatusCode());
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void testGetPersonsByEauthIds_HttpClientErrorException_InternalServerError() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenThrow(exception);

        // Act & Assert
        HttpClientErrorException thrownException = assertThrows(HttpClientErrorException.class, () -> {
            personServiceClient.getPersonsByEauthIds(eauthIds);
        });

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrownException.getStatusCode());
    }

    @Test
    void testGetPersonsByEauthIds_NullResponse() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;
        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNull(result);
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class));
    }

    // Edge cases and validation tests
    @Test
    void testGetPersonsByEauthIds_EmptyString() {
        // Arrange
        String eauthIds = "";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=";

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("No results found.");
        expectedResponse.setData(null);
        expectedResponse.setTotalElements(0);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("No results found.", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testGetPersonsByEauthIds_NullInput() {
        // Arrange
        String eauthIds = null;
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=null";

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("No results found.");
        expectedResponse.setData(null);
        expectedResponse.setTotalElements(0);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("No results found.", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testGetPersonsByEauthIds_SpecialCharacters() {
        // Arrange
        String eauthIds = "test@123,test#456";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Invalid eAuth IDs format.");
        expectedResponse.setData(null);
        expectedResponse.setTotalElements(0);
        expectedResponse.setStatus("ERROR");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals("Invalid eAuth IDs format.", result.getMessage());
        assertEquals("ERROR", result.getStatus());
        assertNull(result.getData());
    }

    // Test URL building
    @Test
    void testGetPersonsByEauthIds_UrlConstruction() {
        // Arrange
        String eauthIds = "123,456,789";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=123,456,789";

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Found 3 person(s).");
        expectedResponse.setData(Collections.emptyList());
        expectedResponse.setTotalElements(3);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert - Verify the exact URL was called
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class));
    }

    // Test person with minimal data
    @Test
    void testGetPersonsByEauthIds_PersonWithMinimalData() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        // Create Person object with minimal data
        Person person = new Person();
        person.setUsdaEauthId("28032009029507000000279");
        person.setFirstName("JOHN");
        person.setLastName("DOE");
        // Other fields are null/empty

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Found 1 person(s).");
        expectedResponse.setData(Collections.singletonList(person));
        expectedResponse.setTotalElements(1);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
       
        Person resultPerson = result.getData().get(0);
        assertEquals("28032009029507000000279", resultPerson.getUsdaEauthId());
        assertEquals("JOHN", resultPerson.getFirstName());
        assertEquals("DOE", resultPerson.getLastName());
        assertNull(resultPerson.getMiddleName());
        assertNull(resultPerson.getTitle());
        assertNull(resultPerson.getActive());
        assertNull(resultPerson.getEmail());
        assertNull(resultPerson.getEmploymentList());
    }

    // Test person with empty employment list
    @Test
    void testGetPersonsByEauthIds_PersonWithEmptyEmploymentList() {
        // Arrange
        String eauthIds = "28032009029507000000279";
        String expectedUrl = personBaseUrl + "/persons?usdaeauthids=" + eauthIds;

        Person person = new Person();
        person.setUsdaEauthId("28032009029507000000279");
        person.setFirstName("JOHN");
        person.setLastName("DOE");
        person.setEmploymentList(Collections.emptyList());

        ApiResponse<Person> expectedResponse = new ApiResponse<>();
        expectedResponse.setMessage("Found 1 person(s).");
        expectedResponse.setData(Collections.singletonList(person));
        expectedResponse.setTotalElements(1);
        expectedResponse.setStatus("SUCCESS");

        ResponseEntity<ApiResponse<Person>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // Act
        ApiResponse<Person> result = personServiceClient.getPersonsByEauthIds(eauthIds);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
       
        Person resultPerson = result.getData().get(0);
        assertNotNull(resultPerson.getEmploymentList());
        assertTrue(resultPerson.getEmploymentList().isEmpty());
    }
}