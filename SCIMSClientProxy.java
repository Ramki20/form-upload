package gov.usda.fsa.fcao.flp.flpids.common.business.serviceBroker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gov.usda.fsa.common.base.AgencyApplicationException;
import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.RetrieveScimsCustomersByCoreCustomerIdBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.SCIMSProgramParticipationBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsAddressBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCodeTypeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsCustomerIDHistoryBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsEmailBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsLegacyLinkBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsMergedCustomerIDHistoryBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ScimsPhoneBO;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessFatalException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.SCIMSDataNotFoundException;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.JNDILookup;
import gov.usda.fsa.fcao.flp.flpids.common.utilities.StringUtil;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomer;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerAddress;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerEmail;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerEthnicity;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerIdHistory;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerLegacyLink;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerPhone;
import gov.usda.fsa.parmo.scims.businessobject.CoreCustomerRace;
import gov.usda.fsa.parmo.scims.businessobject.CoreProgramParticipant;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions.DescriptionDisplayOptions;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions.EmailOptions;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions.PhoneOptions;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions.ProgramParticipationOptions;
import gov.usda.fsa.parmo.scims.service.CustomerSearchOptions.SearchTypeOptions;
import gov.usda.fsa.parmo.scims.servicewrapper.SCIMSService;
import gov.usda.fsa.parmo.scims.servicewrapper.exception.SCIMSServiceException;
import gov.usda.fsa.parmo.scims.servicewrapper.factory.SCIMSServiceFactory;
import gov.usda.fsa.parmo.scims.servicewrapper.reply.SCIMSServiceResult;

public class SCIMSClientProxy {
	private static final Logger logger = LogManager.getLogger(SCIMSClientProxy.class);
	private static String UNKNOWN = "UnKnown";
	private static String YES = "Y";
	// Added this for contingency to revert/remedy quickly with JNDI Change - in case of severe errors in Production (due to DLS-9647 changes)
	private static String scimsDefaultValueUnknown = JNDILookup.commonFSADirectLookup("gov/usda/fsa/fcao/flp/scims_default_value_unknown");
	
	public SCIMSClientProxy() {
		logger.info("SCIMSClientProxy constructor is called...");		
	}
	public List<ScimsCustomerBO> getCustomerByCustomerIds(gov.usda.fsa.common.base.AgencyToken baseAgnecyToken,
			List<Integer> customerIds)
			throws SCIMSDataNotFoundException, SCIMSBusinessFatalException, SCIMSBusinessStopException {
		List<ScimsCustomerBO> scimsCustomerList = new ArrayList<ScimsCustomerBO>();

		try {

			SCIMSService service = getSCIMSService(baseAgnecyToken);

			CustomerSearchOptions options = createCustomerSearchOption(SearchTypeOptions.CORE_ID);

			SCIMSServiceResult result = service.getCustomers(baseAgnecyToken, options, (ArrayList) customerIds);

			Collection<CoreCustomer> customers = result.getCustomerList();

			if (customers != null && !customers.isEmpty()) {

				buildScimsCustomerList(baseAgnecyToken, scimsCustomerList, customers);

			} else {

				throw new SCIMSDataNotFoundException("Customer identifier " + customerIds + " not found in SCIMS");
			}

		} catch (SCIMSServiceException sse) {
			logger.error("SCIMS Service Exception caught at getCustomerByCustomerIds() call", sse);
			throw new SCIMSBusinessFatalException("SCIMS Service Exception caught at getCustomerByCustomerIds() call");

		} catch (AgencyApplicationException aae) {
			logger.error("Exception caught at getCustomerByCustomerIds() call", aae);
			throw new SCIMSBusinessStopException("Exception caught at getCustomerByCustomerIds() call");
		}
		return scimsCustomerList;
	}
	
	public List<ScimsCustomerBO> getCustomerByCustomerIdsLite(RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC)
			throws SCIMSDataNotFoundException, SCIMSBusinessFatalException, SCIMSBusinessStopException {
		List<ScimsCustomerBO> scimsCustomerList = new ArrayList<ScimsCustomerBO>();

		try {

			SCIMSService service = getSCIMSService(retrieveBC.getAgencyToken());

			CustomerSearchOptions options = createCustomerSearchOptionLite(retrieveBC);

			SCIMSServiceResult result = service.getCustomers(retrieveBC.getAgencyToken(), options, (ArrayList) retrieveBC.getCoreCustomerIds());

			Collection<CoreCustomer> customers = result.getCustomerList();

			if (customers != null && !customers.isEmpty()) {

				buildScimsCustomerList(retrieveBC.getAgencyToken(), scimsCustomerList, customers);

			} else {

				throw new SCIMSDataNotFoundException("Customer identifier " + retrieveBC.getCoreCustomerIds() + " not found in SCIMS");
			}

		} catch (SCIMSServiceException sse) {
			logger.error("SCIMS Service Exception caught at getCustomerByCustomerIdsLite call", sse);
			throw new SCIMSBusinessFatalException("SCIMS Service Exception caught at getCustomerByCustomerIds() call");

		} catch (AgencyApplicationException aae) {
			logger.error("Exception caught at getCustomerByCustomerIdsLite call", aae);
			throw new SCIMSBusinessStopException("Exception caught at getCustomerByCustomerIds() call");
		} 
		return scimsCustomerList;
	}
	

	public List<ScimsCustomerBO> getCustomerByTaxIds(gov.usda.fsa.common.base.AgencyToken baseAgnecyToken,
			List<String> taxIdentifierAndTypes)
			throws SCIMSDataNotFoundException, SCIMSBusinessFatalException, SCIMSBusinessStopException {
		List<ScimsCustomerBO> scimsCustomerList = new ArrayList<ScimsCustomerBO>();
		Collection<CoreCustomer> customers = new ArrayList<CoreCustomer>();
		try {
			SCIMSService service = getSCIMSService(baseAgnecyToken);

			CustomerSearchOptions options = createCustomerSearchOption(SearchTypeOptions.TAX_ID);

			SCIMSServiceResult result = service.getCustomers(baseAgnecyToken, options,
					(ArrayList) taxIdentifierAndTypes);

			customers = result.getCustomerList();

			if (customers != null && !customers.isEmpty()) {

				buildScimsCustomerList(baseAgnecyToken, scimsCustomerList, customers);

			} else {

				throw new SCIMSDataNotFoundException("Tax Id and type is not found in SCIMS");
			}

		} catch (SCIMSServiceException sse) {
			logger.error("SCIMS Service Exception caught at getCustomerByTaxIds() call", sse);
			throw new SCIMSBusinessFatalException("SCIMS Service Exception caught at getCustomerByTaxIds() call");
		} catch (AgencyApplicationException aae) {
			logger.error("Exception caught at getCustomerByTaxIds() call", aae);
			throw new SCIMSBusinessStopException("Exception caught at getCustomerByTaxIds() call");

		}
		return scimsCustomerList;
	}

	protected SCIMSService getSCIMSService(gov.usda.fsa.common.base.AgencyToken baseAgnecyToken)
			throws SCIMSServiceException {

		return SCIMSServiceFactory.createService(baseAgnecyToken);
	}

	private void buildScimsCustomerList(gov.usda.fsa.common.base.AgencyToken appAgencyToken,
			List<ScimsCustomerBO> scimsCustomerList, Collection<CoreCustomer> customers) {
		if (customers != null && !customers.isEmpty()) {
			for (CoreCustomer coreCustomer : customers) {
				ScimsCustomerBO customer = mapToScimsCustomter(appAgencyToken, coreCustomer);
				scimsCustomerList.add(customer);
			}
		}
	}

	private CustomerSearchOptions createCustomerSearchOption(SearchTypeOptions type) throws AgencyApplicationException {
		CustomerSearchOptions searchOptions = new CustomerSearchOptions();

		searchOptions.setSearchTypeOptions(type);
		searchOptions.setComplete(Boolean.TRUE);
		searchOptions.setLegacyLinkOptions(CustomerSearchOptions.LegacyLinkOptions.ALL);
		searchOptions.setInactiveLegacyLinkOption(Boolean.TRUE);
		searchOptions.setMergedIDListOption(Boolean.TRUE);
		searchOptions.setCustomerStatusOptions(CustomerSearchOptions.CustomerStatusOptions.ACTIVE);
		searchOptions.setAddressOptions(CustomerSearchOptions.AddressOptions.ALL);
		searchOptions.setEthnicityOption(Boolean.TRUE);
		searchOptions.setRaceOption(Boolean.TRUE);
		searchOptions.setEmailOptions(EmailOptions.ALL);
		searchOptions.setPhoneOptions(PhoneOptions.ALL);
		searchOptions.setDescriptionDisplayOptions(DescriptionDisplayOptions.SHORT);
		searchOptions.setDisabilityOption(Boolean.TRUE);
		searchOptions.setProgramParticipationOptions(ProgramParticipationOptions.ALL);
		searchOptions.setCustomerDetailsOption(Boolean.TRUE);
		searchOptions.setCustomerNotesOption(Boolean.TRUE);
		searchOptions.setReturnAuditFields(Boolean.TRUE);

		return searchOptions;
	}

	private CustomerSearchOptions createCustomerSearchOptionLite(RetrieveScimsCustomersByCoreCustomerIdBC retrieveBC) throws AgencyApplicationException {
		CustomerSearchOptions searchOptions = new CustomerSearchOptions();

		searchOptions.setSearchTypeOptions(retrieveBC.getSearchTypeOptions()==null?SearchTypeOptions.CORE_ID:retrieveBC.getSearchTypeOptions());
		searchOptions.setComplete(false);
		searchOptions.setLegacyLinkOptions(retrieveBC.getLegacyLinkOptions()==null?CustomerSearchOptions.LegacyLinkOptions.NONE:retrieveBC.getLegacyLinkOptions());
		searchOptions.setInactiveLegacyLinkOption(retrieveBC.isInactiveLegacyLinkOption());
		searchOptions.setMergedIDListOption(retrieveBC.isMergedIDList());
		searchOptions.setCustomerStatusOptions(retrieveBC.getCustomerStatusOptions()==null?CustomerSearchOptions.CustomerStatusOptions.ACTIVE:retrieveBC.getCustomerStatusOptions());
		searchOptions.setAddressOptions(retrieveBC.getAddressOptions()==null?CustomerSearchOptions.AddressOptions.ALL:retrieveBC.getAddressOptions());
		searchOptions.setEthnicityOption(retrieveBC.isEthnicityOption());
		searchOptions.setRaceOption(retrieveBC.isRaceOption());
		searchOptions.setEmailOptions(retrieveBC.getEmailOptions()==null?EmailOptions.NONE:retrieveBC.getEmailOptions());
		searchOptions.setPhoneOptions(retrieveBC.getPhoneOptions()==null?PhoneOptions.NONE:retrieveBC.getPhoneOptions());
		searchOptions.setDescriptionDisplayOptions(retrieveBC.getDescriptionDisplayOptions()==null?DescriptionDisplayOptions.SHORT:retrieveBC.getDescriptionDisplayOptions());
		searchOptions.setDisabilityOption(retrieveBC.isDisabilityOption());
		searchOptions.setProgramParticipationOptions(retrieveBC.getProgramParticipationOptions()==null?ProgramParticipationOptions.NONE:retrieveBC.getProgramParticipationOptions());
		searchOptions.setCustomerDetailsOption(retrieveBC.isCustomerDetailsOption());
		searchOptions.setCustomerNotesOption(retrieveBC.isCustomerNotesOption());
		searchOptions.setReturnAuditFields(retrieveBC.isReturnAuditFieldsOption());
		
		return searchOptions;
	}
	
	/**
	 * The implementation has not been finished. (April 11,2011)
	 * 
	 * @param appAgencyToken
	 * @param coreCustomer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ScimsCustomerBO mapToScimsCustomter(gov.usda.fsa.common.base.AgencyToken appAgencyToken,
			CoreCustomer coreCustomer) {
		ScimsCustomerBO customer = new ScimsCustomerBO(appAgencyToken);

		customer.setCustomerID(coreCustomer.getCoreCustomerId().toString());
		customer.setTaxID(coreCustomer.getTaxIdentification());
		
		customer.setCreationDate(coreCustomer.getCreationDateTime());
		customer.setLastChangeDate(coreCustomer.getLastChangeDateTime());

		ScimsCodeTypeBO scimsCodeTypeBO = parsingTaxCodeType(appAgencyToken, coreCustomer);
		customer.setTaxIDType(scimsCodeTypeBO);
		customer.setCommonName(coreCustomer.getCommonCustomerName());

		if (coreCustomer.getInactiveCustomerType() != null
				&& coreCustomer.getInactiveCustomerType().getInactiveCustomerCode() != null
				&& coreCustomer.getInactiveCustomerType().getInactiveCustomerCode().toString().equalsIgnoreCase("I")) {

			customer.setActiveIndicator(Boolean.FALSE);

		} else {

			customer.setActiveIndicator(Boolean.TRUE);
		}

		String customerName = "";
		if (coreCustomer.getFirstName().isEmpty() && coreCustomer.getLastName().isEmpty()) {

			customer.setCustomerName(coreCustomer.getBusinessName());

		} else {

			customer.setFirstName(coreCustomer.getFirstName());

			if (coreCustomer.getNameSuffix().isEmpty()) {

				customer.setLastName(coreCustomer.getLastName());

			} else {

				customer.setLastName(coreCustomer.getLastName() + " " + coreCustomer.getNameSuffix());
			}

			customer.setMiddleName(coreCustomer.getMiddleName());
			customer.setPrefix(coreCustomer.getNamePrefix());
			customer.setSuffix(coreCustomer.getNameSuffix());
			customerName = StringUtil.formatNameForScimCustomer(coreCustomer.getFirstName(), coreCustomer.getLastName(),
					coreCustomer.getMiddleName());
			customer.setCustomerName(customerName);

		}

		customer.setBusinessName(coreCustomer.getBusinessName());

		List<ScimsMergedCustomerIDHistoryBO> coreMergedIdHistoryList = new ArrayList<ScimsMergedCustomerIDHistoryBO>();
		
		if (coreCustomer.getMergedIdList() != null && !coreCustomer.getMergedIdList().isEmpty()) {

			ScimsMergedCustomerIDHistoryBO scimsMergedCustomerIDHistoryBO = null;
			for (CoreCustomerIdHistory coreCustomerIdHistory : (Set<CoreCustomerIdHistory>) coreCustomer
					.getMergedIdList()) {

				scimsMergedCustomerIDHistoryBO = new ScimsMergedCustomerIDHistoryBO(appAgencyToken);

				if (coreCustomerIdHistory.getNewCustomerIdentifier() != null) {
					scimsMergedCustomerIDHistoryBO
							.setNewCustomerId(coreCustomerIdHistory.getNewCustomerIdentifier().toString());
					
				}
				if (coreCustomerIdHistory.getOldCustomerIdentifier() != null) {
					scimsMergedCustomerIDHistoryBO
							.setOldCustomerId(coreCustomerIdHistory.getOldCustomerIdentifier().toString());
				}

				if (coreCustomerIdHistory.getCreationDateTime() != null) {
					scimsMergedCustomerIDHistoryBO.setIdChangeDate(coreCustomerIdHistory.getCreationDateTime().toString());
				}

				coreMergedIdHistoryList.add(scimsMergedCustomerIDHistoryBO);
			}

		}

		customer.setMergedCustomerIdHistorySet(coreMergedIdHistoryList);
		
		List<ScimsCustomerIDHistoryBO> coreIdHistoryList = new ArrayList<ScimsCustomerIDHistoryBO>();

		if (coreCustomer.getCoreIdHistoryList() != null && !coreCustomer.getCoreIdHistoryList().isEmpty()) {

			ScimsCustomerIDHistoryBO scimsCustomerIDHistoryBO = null;
			for (CoreCustomerIdHistory coreCustomerIdHistory : (List<CoreCustomerIdHistory>) coreCustomer
					.getCoreIdHistoryList()) {

				scimsCustomerIDHistoryBO = new ScimsCustomerIDHistoryBO(appAgencyToken);

				if (coreCustomerIdHistory.getNewCustomerIdentifier() != null) {
					scimsCustomerIDHistoryBO
							.setNewCustomerId(coreCustomerIdHistory.getNewCustomerIdentifier().toString());
					
				}
				if (coreCustomerIdHistory.getOldCustomerIdentifier() != null) {
					scimsCustomerIDHistoryBO
							.setOldCustomerId(coreCustomerIdHistory.getOldCustomerIdentifier().toString());
				}

				if (coreCustomerIdHistory.getCreationDateTime() != null) {
					scimsCustomerIDHistoryBO.setIdChangeDate(coreCustomerIdHistory.getCreationDateTime().toString());
				}

				coreIdHistoryList.add(scimsCustomerIDHistoryBO);
			}

		}

		customer.setCustomerIdHistorySet(coreIdHistoryList);

		ScimsCodeTypeBO customerTypeCode = new ScimsCodeTypeBO(appAgencyToken);
		customerTypeCode.setCode(coreCustomer.getCustomerType().getCustomerTypeCode().toString());
		customerTypeCode.setDescription(coreCustomer.getCustomerType().getShortDescription());

		customer.setCustomerType(customerTypeCode);

		ScimsCodeTypeBO employeeRelationType = parsingEmployeeCode(coreCustomer, customer);
		customer.setEmployeeRelationType(employeeRelationType);

		if (coreCustomer.getBirthDateTime() != null) {
			customer.setBirthDate(coreCustomer.getBirthDateTime().toString());
		}

		ScimsCodeTypeBO genderCode = parsingGenderCode(coreCustomer, customer);
		customer.setGender(genderCode);

		ScimsCodeTypeBO maritalCodeType = parsingMaritalCode(coreCustomer, customer);

		customer.setMaritalStatus(maritalCodeType);

		ScimsCodeTypeBO businessType = new ScimsCodeTypeBO(customer.getAgencyToken());
		businessType.setCode(coreCustomer.getBusinessTypeCode().getBusinessTypeCode());
		businessType.setDescription(coreCustomer.getBusinessTypeCode().getShortDescription());
		customer.setBusinessType(businessType);

		if (coreCustomer.getVeteranCode() != null) {
			customer.setVeteranStatus(coreCustomer.getVeteranCode().toString());
		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {
			customer.setVeteranStatus(UNKNOWN);
		}		

		if (coreCustomer.getResidentAlienCode() != null) {
			customer.setResidentAlien(coreCustomer.getResidentAlienCode().toString());
		}
		customer.setCitizenshipCountryCode(parsingCitizenCode(coreCustomer, customer));

		customer.setOriginatingCountryCode(parsingOriginationCountryCode(coreCustomer, customer));

		List<ScimsCodeTypeBO> raceTypes = parsingRaceType(coreCustomer, customer);
		customer.setRaceType(raceTypes);

		ScimsCodeTypeBO ethnicityType = parsingEnthnicityCode(coreCustomer, customer);
		customer.setEthnicityType(ethnicityType);
		
		List<ScimsAddressBO> addressSet = parsingAddress(coreCustomer, customer);
		customer.setAddressSet(addressSet);

		List<ScimsEmailBO> emailList = new ArrayList<ScimsEmailBO>();

		parsingEMailList(coreCustomer, customer, emailList);
		customer.setEmailSet(emailList);

		List<ScimsPhoneBO> phoneList = parsingPhoneList(coreCustomer, customer);
		customer.setPhoneSet(phoneList);

		customer.setLegacyLinkSet(parseLegacyLink(coreCustomer, customer));

		customer.setProgramParticipationList(parseScimsProgramParticipationList(coreCustomer, customer));

		return customer;
	}

	private List<SCIMSProgramParticipationBO> parseScimsProgramParticipationList(CoreCustomer coreCustomer,
			ScimsCustomerBO customer) {

		List<SCIMSProgramParticipationBO> programParticipationList = new ArrayList<SCIMSProgramParticipationBO>();

		SCIMSProgramParticipationBO scimsProgramParticipationBO = null;
		ScimsCodeTypeBO scimsCodeTypeCode = null;
		ScimsCodeTypeBO servicingCodeTypeCode = null;
		if (coreCustomer.getProgramParticipationList() != null) {

			Iterator<CoreProgramParticipant> participantIterator = coreCustomer.getProgramParticipationList()
					.iterator();

			while (participantIterator.hasNext()) {

				CoreProgramParticipant programParticipant = participantIterator.next();

				scimsProgramParticipationBO = new SCIMSProgramParticipationBO(customer.getAgencyToken());
				scimsCodeTypeCode = new ScimsCodeTypeBO(customer.getAgencyToken());
				scimsCodeTypeCode.setCode(programParticipant.getProgramName().getProgramNameCode());
				scimsCodeTypeCode.setDescription(programParticipant.getProgramName().getShortDescription());
				scimsProgramParticipationBO.setProgram(scimsCodeTypeCode);

				servicingCodeTypeCode = new ScimsCodeTypeBO(customer.getAgencyToken());

				if (programParticipant.getServicingOrganizationalUnit() != null
						&& programParticipant.getServicingOrganizationalUnit().getOrganizationalUnitId() != null) {

					servicingCodeTypeCode.setCode(
							programParticipant.getServicingOrganizationalUnit().getOrganizationalUnitId().toString());
				}
				servicingCodeTypeCode.setDescription(
						programParticipant.getServicingOrganizationalUnit().getOrganizationalUnitName());
				scimsProgramParticipationBO.setServicingOrgUnit(servicingCodeTypeCode);
				if (programParticipant.getGeneralProgramInterestCode() != null) {

					scimsProgramParticipationBO
							.setGeneralProgramInterest(programParticipant.getGeneralProgramInterestCode().toString());
				}

				scimsProgramParticipationBO.setAgreementNumber(programParticipant.getAgreementNumber());

				if (programParticipant.getCurrentParticipantCode() != null) {
					scimsProgramParticipationBO
							.setCurrentParticipant(programParticipant.getCurrentParticipantCode().toString());
				}

				if (programParticipant.getEverParticipatedIndicator() != null) {
					scimsProgramParticipationBO
							.setEverParticipated(programParticipant.getEverParticipatedIndicator().toString());
				}

				programParticipationList.add(scimsProgramParticipationBO);

			}

		}

		return programParticipationList;
	}

	private List<ScimsLegacyLinkBO> parseLegacyLink(CoreCustomer coreCustomer, ScimsCustomerBO customer) {

		List<ScimsLegacyLinkBO> lgcyLinkList = new ArrayList<ScimsLegacyLinkBO>();

		ScimsLegacyLinkBO lgcyLink = null;
		ScimsCodeTypeBO scimsCodeTypeCode = null;
		ScimsCodeTypeBO systemCodeTypeCode = null;

		if (coreCustomer.getLegacyLinkList() != null) {
			for (CoreCustomerLegacyLink legacyLinkBO : coreCustomer.getLegacyLinkList()) {

				lgcyLink = new ScimsLegacyLinkBO(customer.getAgencyToken());
				lgcyLink.setStateCode(legacyLinkBO.getLegacyStateCode());
				lgcyLink.setCountyCode(legacyLinkBO.getLegacyCountyCode());

				scimsCodeTypeCode = getDefaultValues(customer.getAgencyToken());
				if(legacyLinkBO.getServicingOrganizationalUnit()!=null)
				{
					if (legacyLinkBO.getServicingOrganizationalUnit().getOrganizationalUnitId() != null) {
						scimsCodeTypeCode.setCode(
								legacyLinkBO.getServicingOrganizationalUnit().getOrganizationalUnitId().toString());
					}
					scimsCodeTypeCode
							.setDescription(legacyLinkBO.getServicingOrganizationalUnit().getOrganizationalUnitName());
				}
				lgcyLink.setServicingOrganizationUnit(scimsCodeTypeCode);

				systemCodeTypeCode = getDefaultValues(customer.getAgencyToken());
				systemCodeTypeCode.setCode(legacyLinkBO.getLegacySystemName().getLegacySystemName());
				systemCodeTypeCode.setDescription(legacyLinkBO.getLegacySystemName().getShortDescription());
				lgcyLink.setSystem(systemCodeTypeCode);

				lgcyLinkList.add(lgcyLink);

			}
		}
		return lgcyLinkList;

	}

	private ScimsCodeTypeBO parsingEmployeeCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		ScimsCodeTypeBO employeeRelationType = new ScimsCodeTypeBO(customer.getAgencyToken());
		if (coreCustomer.getEmployeeTypeCode() != null) {
			employeeRelationType.setCode(coreCustomer.getEmployeeTypeCode().getEmployeeTypeCode());
			employeeRelationType.setDescription(coreCustomer.getEmployeeTypeCode().getShortDescription());
		}
		return employeeRelationType;
	}

	@SuppressWarnings("unchecked")
	private ScimsCodeTypeBO parsingEnthnicityCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		Set<CoreCustomerEthnicity> ethnicityTypes = coreCustomer.getEthnicityList();

		ScimsCodeTypeBO ethnicityType = new ScimsCodeTypeBO(customer.getAgencyToken());

		if (!ethnicityTypes.isEmpty()) {

			for (CoreCustomerEthnicity coreCustomerEthnicity : ethnicityTypes) {

				ethnicityType.setCode(coreCustomerEthnicity.getEthnicityType().getEthnicityTypeCode().toString());
				ethnicityType.setDescription(coreCustomerEthnicity.getEthnicityType().getShortDescription());
				ethnicityType.setDeterminationCode(coreCustomerEthnicity.getDeterminationType().getDeterminationTypeCode().toString());
				ethnicityType.setDeterminationDescription(coreCustomerEthnicity.getDeterminationType().getShortDescription());
				ethnicityType.setLastChangeDate(coreCustomerEthnicity.getLastChangeDateTime());
				break;
			} 
		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {

			 ethnicityType = getDefaultValues(customer.getAgencyToken());
						
		} 
		return ethnicityType;
	}
	
	@SuppressWarnings("unchecked")
	private void parsingEMailList(CoreCustomer coreCustomer, ScimsCustomerBO customer, List<ScimsEmailBO> emailList) {
		Set<CoreCustomerEmail> emailSet = coreCustomer.getEmailList();
		for (CoreCustomerEmail coreCustomerEmail : emailSet) {
			ScimsEmailBO email = new ScimsEmailBO(customer.getAgencyToken());
			email.setAddress(coreCustomerEmail.getEmailAddress());
			if (coreCustomerEmail.getPrimaryIndicator() != null
					&& coreCustomerEmail.getPrimaryIndicator().toString().equalsIgnoreCase("Y")) {
				email.setPrimary(Boolean.TRUE);

			} else {

				email.setPrimary(Boolean.FALSE);
			}
			emailList.add(email);

			ScimsCodeTypeBO emailType = new ScimsCodeTypeBO(customer.getAgencyToken());
			email.setType(emailType);
			emailType.setCode(coreCustomerEmail.getEmailAddressType().getEmailAddressTypeCode());
			emailType.setDescription(coreCustomerEmail.getEmailAddressType().getShortDescription());

		}
	}

	@SuppressWarnings("unchecked")
	private List<ScimsCodeTypeBO> parsingRaceType(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		List<ScimsCodeTypeBO> raceTypes = new ArrayList<ScimsCodeTypeBO>();
		Set<CoreCustomerRace> raceList = coreCustomer.getRaceList();

		if (!raceList.isEmpty()) {
			for (CoreCustomerRace race : raceList) {
				ScimsCodeTypeBO raceType = new ScimsCodeTypeBO(customer.getAgencyToken());
				raceType.setCode(race.getRaceType().getRaceTypeCode().toString());
				raceType.setDescription(race.getRaceType().getShortDescription());
				raceType.setDeterminationCode(race.getDeterminationType().getDeterminationTypeCode().toString());
				raceType.setDeterminationDescription(race.getDeterminationType().getShortDescription());
				raceType.setLastChangeDate(race.getLastChangeDateTime());
				raceTypes.add(raceType);
			}
		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {
			raceTypes.add(getDefaultValues(customer.getAgencyToken()));
			logger.info("coreCustomerID="+customer.getCustomerID()+" raceTypes="+raceTypes);
		}
		return raceTypes;
	}
	
	private ScimsCodeTypeBO parsingGenderCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		ScimsCodeTypeBO genderCode = new ScimsCodeTypeBO(customer.getAgencyToken());
		if (coreCustomer.getGenderCode() != null && (!StringUtil.isEmptyString(coreCustomer.getGenderCode().getGenderCode())) ) {
			genderCode.setCode(coreCustomer.getGenderCode().getGenderCode());
			genderCode.setDescription(coreCustomer.getGenderCode().getShortDescription());
		}
		return genderCode;
	}

	private ScimsCodeTypeBO parsingCitizenCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		ScimsCodeTypeBO citizenCode = new ScimsCodeTypeBO(customer.getAgencyToken());
		if (coreCustomer.getCitizenshipCountryCode() != null) {
			citizenCode.setCode(coreCustomer.getCitizenshipCountryCode());

		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {
			citizenCode = getDefaultValues(customer.getAgencyToken());
		}
		return citizenCode;
	}

	private ScimsCodeTypeBO parsingOriginationCountryCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		ScimsCodeTypeBO originationCountryCode = new ScimsCodeTypeBO(customer.getAgencyToken());
		if (coreCustomer.getOriginatingCountryCode() != null) {
			originationCountryCode.setCode(coreCustomer.getOriginatingCountryCode());

		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {
			originationCountryCode = getDefaultValues(customer.getAgencyToken());
		}
		return originationCountryCode;
	}

	@SuppressWarnings("unchecked")
	private List<ScimsAddressBO> parsingAddress(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		List<ScimsAddressBO> addressSet = new ArrayList<ScimsAddressBO>();
		Set<CoreCustomerAddress> addressList = coreCustomer.getAddressList();

		for (CoreCustomerAddress coreCustomerAddress : addressList) {

			ScimsAddressBO scimsAddress = new ScimsAddressBO(customer.getAgencyToken());

			if (coreCustomerAddress.getBeginDateTime() != null) {
				scimsAddress.setBeginDate(coreCustomerAddress.getBeginDateTime().toString());
			}
			scimsAddress.setCity(coreCustomerAddress.getCity());

			scimsAddress.setCurrentAddress(isValidAddressIndicator(coreCustomerAddress.getCurrentAddressIndicator()));

			scimsAddress.setDeliveryLine(coreCustomerAddress.getDeliveryAddressLine());
			if (coreCustomerAddress.getEndDateTime() != null) {
				scimsAddress.setEndDate(coreCustomerAddress.getEndDateTime().toString());
			}
			scimsAddress.setInformationLine(coreCustomerAddress.getInformationLine());
			scimsAddress.setMailingAddress(isValidAddressIndicator(coreCustomerAddress.getMailingAddressIndicator()));
			scimsAddress.setShippingAddress(isValidAddressIndicator(coreCustomerAddress.getShippingAddressIndicator()));
			scimsAddress.setStateAbbrevation(coreCustomerAddress.getStateAbbreviation());
			scimsAddress.setStreetAddress(isValidAddressIndicator(coreCustomerAddress.getStreetAddressIndicator()));
			scimsAddress.setSupplementalLine_1(coreCustomerAddress.getSupplementalAddressLine1());
			scimsAddress.setSupplementalLine_2(coreCustomerAddress.getSupplementalAddressLine2());
			scimsAddress.setZip_4(coreCustomerAddress.getZipCodePlus4());
			scimsAddress.setZipCode(coreCustomerAddress.getZipCode());
			scimsAddress.setCity(coreCustomerAddress.getCity());
			scimsAddress.setForeignAddressLine(coreCustomerAddress.getForeignAddressLine());
			
			scimsAddress.setLastChangeDate(coreCustomerAddress.getLastChangeDateTime());
			scimsAddress.setLinkLastChangeDate(coreCustomerAddress.getLinkLastChangeDateTime());

			if (scimsAddress.getCurrentAddress()) {
				addressSet.add(scimsAddress);
			}

		}
		return addressSet;
	}

	private Boolean isValidAddressIndicator(Character addressIndicator) {

		if (addressIndicator != null && addressIndicator.toString().equalsIgnoreCase("Y")) {

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	private ScimsCodeTypeBO parsingMaritalCode(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		ScimsCodeTypeBO maritalCodeType = new ScimsCodeTypeBO(customer.getAgencyToken());
		if (coreCustomer.getMaritalStatusCode() != null) {
			maritalCodeType.setCode(coreCustomer.getMaritalStatusCode().getMaritalStatusCode());
			maritalCodeType.setDescription(coreCustomer.getMaritalStatusCode().getShortDescription());
		} else if (YES.equalsIgnoreCase(scimsDefaultValueUnknown)) {
			maritalCodeType = getDefaultValues(customer.getAgencyToken());
		}
		return maritalCodeType;
	}

	private ScimsCodeTypeBO getDefaultValues(AgencyToken token) {
		ScimsCodeTypeBO scType = new ScimsCodeTypeBO(token);
		scType.setCode(UNKNOWN);
		scType.setDescription(UNKNOWN);

		return scType;
	}

	@SuppressWarnings("unchecked")
	private List<ScimsPhoneBO> parsingPhoneList(CoreCustomer coreCustomer, ScimsCustomerBO customer) {
		List<ScimsPhoneBO> phoneList = new ArrayList<ScimsPhoneBO>();
		Set<CoreCustomerPhone> phoneSet = coreCustomer.getPhoneList();
		for (CoreCustomerPhone phone : phoneSet) {
			ScimsPhoneBO scimsPhone = new ScimsPhoneBO(customer.getAgencyToken());
			phoneList.add(scimsPhone);

			scimsPhone.setNumber(phone.getPhoneNumber());
			ScimsCodeTypeBO phoneCodeTypeBO = new ScimsCodeTypeBO(customer.getAgencyToken());
			phoneCodeTypeBO.setCode(phone.getPhoneType().getPhoneTypeCode());
			phoneCodeTypeBO.setDescription(phone.getPhoneType().getShortDescription());

			scimsPhone.setType(phoneCodeTypeBO);
			scimsPhone.setPrimary(!phone.getPhonePrimaryIndicator().equals('N'));
			scimsPhone.setExtension(phone.getPhoneExtensionNumber());
		}
		return phoneList;
	}

	private ScimsCodeTypeBO parsingTaxCodeType(gov.usda.fsa.common.base.AgencyToken appAgencyToken,
			CoreCustomer coreCustomer) {
		ScimsCodeTypeBO scimsCodeTypeBO = new ScimsCodeTypeBO(appAgencyToken);
		scimsCodeTypeBO.setCode(coreCustomer.getTaxIdentificationType().getTaxIdentificationTypeCode().toString());
		scimsCodeTypeBO.setDescription(coreCustomer.getTaxIdentificationType().getShortDescription());

		return scimsCodeTypeBO;
	}

}
