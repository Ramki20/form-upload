package gov.usda.fsa.fcao.flp.ola.core.fsa.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.usda.fsa.fcao.flp.ola.core.bo.OLACoreUser;
import gov.usda.fsa.fcao.flp.ola.core.cache.CacheEntry;
import gov.usda.fsa.fcao.flp.ola.core.fsa.service.IAuthorizationService;
import gov.usda.fsa.fcao.flp.ola.core.service.util.OlaServiceUtil;
import gov.usda.fsa.fcao.flp.ola.core.shared.service.EASClient;

@Component
public class AuthorizationServiceImpl implements IAuthorizationService {

	private static final Logger LOGGER = LogManager.getLogger(AuthorizationServiceImpl.class);

	public static final String OLA_CORE_USER = "OLA_CORE_USER";
	
	private static Map<String, CacheEntry<Map<String, OLACoreUser>>> cacheOLACoreUser = new HashMap<>();

	@Autowired
	private EASClient easClient;

	@Override
	public void initializeMapCache() {
		LOGGER.info("Initialize olaCoreUserMap Caches...");
		Map<String, OLACoreUser> olaCoreUserMap = new HashMap<String, OLACoreUser>();
		CacheEntry<Map<String, OLACoreUser>> olaCoreUserEntry = new CacheEntry<>(olaCoreUserMap);
		cacheOLACoreUser.put(OLA_CORE_USER, olaCoreUserEntry);
		LOGGER.info("Initialized olaCoreUserMap Caches...");
	}

	@Override
	public void clearCache() {
		cacheOLACoreUser.clear();
	}
	
	@Override
	public Map<String, OLACoreUser> getOLACoreUserMap() {

		CacheEntry<Map<String, OLACoreUser>> entry = cacheOLACoreUser.get(OLA_CORE_USER);
		return entry.getContext();
	}
	
	@Override
	public OLACoreUser findOLACoreUser(String eAUthIdentifier) {

		OLACoreUser olaCoreUser =  getOLACoreUserMap().get(eAUthIdentifier);
		if(olaCoreUser==null)
		{
			LOGGER.info("Instantiate OLACoreUser for eAUthIdentifier:{}",eAUthIdentifier);
			olaCoreUser = new OLACoreUser();
			List<String> listUserRoles = getUserRoles(eAUthIdentifier);
			List<String> officeIdentifiers = findAllOfficeIdentifiers(eAUthIdentifier);
			olaCoreUser.setEasRoleList(listUserRoles);
			olaCoreUser.setIdentifier(eAUthIdentifier);
			olaCoreUser.setOfficeIdList(officeIdentifiers);
			olaCoreUser.setNationalRole(OlaServiceUtil.isNationalRole(listUserRoles));
			olaCoreUser.setStateRole(OlaServiceUtil.isStateRole(listUserRoles));
			olaCoreUser.setItRole(OlaServiceUtil.isITRole(listUserRoles));
			olaCoreUser.setServiceCenterRole(OlaServiceUtil.isServiceCenterRole(listUserRoles));
			
			getOLACoreUserMap().put(eAUthIdentifier, olaCoreUser);
		}

		return olaCoreUser;
	}

	@Override
	public List<String> getUserRoles(@NotNull String eAUthIdentifier) {

		return easClient.getUserRoles(eAUthIdentifier);
	}


	public List<String> findAllOfficeIdentifiers(@NotNull String eAuthId) {
		
		return easClient.findOfficesByEauthId(eAuthId);

	}

	@Override
	public List<String> findUsersByCriteria(String officeId, List<String> easRoles){

		List<String> easUsers = new ArrayList<>();
		easRoles.stream().forEach(easRole -> {
			easUsers.addAll(easClient.findUsersByCriteria(Integer.parseInt(officeId), easRole));
		});
		
		return easUsers;
	}

}
