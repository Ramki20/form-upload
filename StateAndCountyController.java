package gov.usda.fsa.fcao.flp.ola.core.web.controller;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import gov.usda.fsa.ao.rest.common.response.RestResponse;
import gov.usda.fsa.ao.rest.common.security.FSAUserDetails;
import gov.usda.fsa.fcao.flp.ola.core.api.IStateCountyAPI;
import gov.usda.fsa.fcao.flp.ola.core.api.comparator.OlaStateComparator;
import gov.usda.fsa.fcao.flp.ola.core.api.model.OfficeUserAPIModel;
import gov.usda.fsa.fcao.flp.ola.core.api.model.StateAPIModel;



@RestController
public class StateAndCountyController extends AbstractOLAAppController {

	private static final Logger LOGGER = LogManager.getLogger(StateAndCountyController.class);
	private static final String SUCCESS_RETRIEVAL = "State and counties are retrieved sucessfully 1:{} ({})";
	private static final String SUCCESS_OFFICE_RETRIEVAL = "Offices are retrieved sucessfully 1:{} ({})";

	private static final String ERROR_OFFICE = "No Offices are retrieved.";
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${fwepWS_LocationUrl}")
	private String locationUrl;
	
	@Value("${itkdls_Url}")
	private String itkDlsUrl;

	@Autowired
	private IStateCountyAPI stateCountyAPI;


	@GetMapping(value = "/all/states/counties")
	public RestResponse findAllStatesConties(HttpServletRequest request) {


		List<StateAPIModel> statesAndCounties = stateCountyAPI.findAllStatesAndCounties();

		if (!statesAndCounties.isEmpty() && LOGGER.isDebugEnabled()) {

			LOGGER.info(SUCCESS_RETRIEVAL, statesAndCounties,
					!statesAndCounties.isEmpty());

		}


		Collections.sort(statesAndCounties, new OlaStateComparator());
		return RestResponse.success(statesAndCounties, statesAndCounties.size()).message(SUCESS);

	}

	@GetMapping(value = "/profile/states/counties")
	public RestResponse findUniqueStatesConties(HttpServletRequest request) {

		List<StateAPIModel> statesAndCounties = stateCountyAPI.findUniqueStatesAndCounties();

		if (!statesAndCounties.isEmpty() && LOGGER.isDebugEnabled()) {

			LOGGER.info(SUCCESS_RETRIEVAL, statesAndCounties,
					!statesAndCounties.isEmpty());

		}

		Collections.sort(statesAndCounties, new OlaStateComparator());
		return RestResponse.success(statesAndCounties, statesAndCounties.size()).message(SUCESS);

	}

	@GetMapping(value = "/flp/states/counties")
	public RestResponse findFLPStatesConties(HttpServletRequest request) {

		FSAUserDetails user = getFSAUserDetails(request);

		List<StateAPIModel> statesAndCounties = stateCountyAPI.findAllAuthorizedStateCounties(user.getEAuthId());

		if (!statesAndCounties.isEmpty() && LOGGER.isDebugEnabled()) {

			LOGGER.info(SUCCESS_RETRIEVAL, statesAndCounties,
					!statesAndCounties.isEmpty());

		}
		Collections.sort(statesAndCounties, new OlaStateComparator());
		return RestResponse.success(statesAndCounties, statesAndCounties.size()).message(SUCESS);

	}

	@GetMapping(value = "/flp/offices")
	public RestResponse findAllAuthorizedOffices(HttpServletRequest request) {

		FSAUserDetails user = getFSAUserDetails(request);

		OfficeUserAPIModel offices = stateCountyAPI.findAllAuthorizedOffices(user.getEAuthId());

		if (offices != null) {

			LOGGER.info(SUCCESS_RETRIEVAL, offices);

			return RestResponse.success(offices, 1).message(SUCCESS_OFFICE_RETRIEVAL);

		}
	
		return RestResponse.error(null).message(ERROR_OFFICE.concat(" EAuth ID:").concat(user.getEAuthId()));
		

	}

	@GetMapping(value = "/ansi/states/counties")
	public RestResponse findAnsiStatesAndCounties(HttpServletRequest request) {

		List<StateAPIModel> statesAndCounties = stateCountyAPI.findAllAnsiStateAndCounties();

		if (!statesAndCounties.isEmpty() && LOGGER.isDebugEnabled()) {

			LOGGER.info(SUCCESS_RETRIEVAL, statesAndCounties,
					!statesAndCounties.isEmpty());

		}

		return RestResponse.success(statesAndCounties, statesAndCounties.size()).message(SUCESS);

	}



}
