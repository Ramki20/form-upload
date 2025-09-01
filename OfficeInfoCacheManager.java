package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.MRTFacadeBusinessService;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.dao.FBPProxyDao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author chris.caruthers
 *
 */
public class OfficeInfoCacheManager implements Serializable {
	private static final long serialVersionUID = 6070526356152940291L;
	private static final Logger LOG = LogManager.getLogger(FBPProxyDao.class);
	private static Map<String, String> STATE_CODE_STATE_ABBR_MAP = new HashMap<String, String>();
	static {
		try {
			if (STATE_CODE_STATE_ABBR_MAP.isEmpty()) {
				populateStateMap();
			}
		} catch (Exception ex) {
			LOG.error("Failed to load State list from MrtFacadeBusinessService.getStateList()..." + ex.getMessage());
		}
	}

	private static void populateStateMap() throws Exception {
		List<StateBO> statesList = new ArrayList<StateBO>();
		MRTFacadeBusinessService mrtFacadeBusinessService = ServiceAgentFacade.getInstance()
				.getMrtFacadeBusinessService();
		statesList = mrtFacadeBusinessService.getStatesList();
		populateStateList(statesList);
	}

	public static List<StateBO> populateStateList(List<StateBO> statesList) {
		if (STATE_CODE_STATE_ABBR_MAP.isEmpty()) {
			for (StateBO sb : statesList) {
				STATE_CODE_STATE_ABBR_MAP.put(sb.getStateCode(), sb.getStateAbbr());
			}
		}
		return statesList;
	}

	public static Map<String, String> getStateMap() {
		return STATE_CODE_STATE_ABBR_MAP;
	}

	public static boolean isUserInJurisdiction(String mailCode, String flpOfficeCode) {
		if (StringUtil.isEmptyString(mailCode) || StringUtil.isEmptyString(flpOfficeCode)) {
			return false;
		}
		String stateAbbrFromMailCode = getStateAbbr(mailCode.substring(0, 2));
		String stateAbbrFromOfficeCode = getStateAbbr(flpOfficeCode.substring(0, 2));

		return stateAbbrFromMailCode.equals(stateAbbrFromOfficeCode);
	}

	public static String getStateAbbr(String stateCode) {
		if (StringUtil.isEmptyString(stateCode)) {
			return "";
		}
		stateCode = stateCode.trim();
		if (stateCode.length() > 2) {
			stateCode = stateCode.substring(0, 2);
		}
		return STATE_CODE_STATE_ABBR_MAP.get(stateCode);
	}
}
