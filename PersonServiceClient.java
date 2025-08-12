package gov.usda.fsa.fcao.flp.ola.core.shared.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.ApiResponse;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.HealthCheck;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.model.Person;

@Service
public class PersonServiceClient {

	private static final Logger LOGGER = LogManager.getLogger(PersonServiceClient.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${fwepWS_PersonUrl}")
	private String personBaseUrl;

    // health check
    public boolean isHealthy() {
        try {
            String url = personBaseUrl + "/isHealthy";
            ResponseEntity<HealthCheck> response = restTemplate.getForEntity(url, HealthCheck.class);
            if(response!=null && response.getBody()!=null)
            {
                return response.getBody().getData();
            }
            return false;
        } catch (HttpClientErrorException e) {
        	LOGGER.error("Unexpected error:",e);
        	throw e;
        }
    }
	
    // Get multiple persons by eAuth IDs (comma-separated)
    public ApiResponse<Person> getPersonsByEauthIds(String usdaEauthIds) {
        try {
            String url = personBaseUrl + "/persons?usdaeauthids=" + usdaEauthIds;
            ParameterizedTypeReference<ApiResponse<Person>> responseType =
                new ParameterizedTypeReference<ApiResponse<Person>>() {};
           
            ResponseEntity<ApiResponse<Person>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType);
            return response.getBody();
        } catch (HttpClientErrorException e) {
        	LOGGER.error("Unexpected error:",e);
        	throw e;
        }
    }
	
}
