/*
 * Created on Mar 24, 2006
 */
package gov.usda.fsa.fcao.flp.flpids.common.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.ServiceCenterFlpOfficeCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.external.EmployeeData;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.IMRTProxyBS;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessServices.ServiceAgentFacade;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBusinessStopException;
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTInterfaceException;

/**
 * Responsible for validating that a given collection of String values are FLP
 * office codes; converting this collection of flp office codes (containing both
 * state office FLP codes as well as service center office FLP codes) and
 * returning a collection of service center office FLP codes. For a given state
 * office FLP code encountered in the source collection, the returned target
 * collection will include every service center FLP code for that state.
 *
 * @author douglas.a.clark
 */
public class NormalizeAreaOfResponsibility {
	private static final Logger logger = LogManager.getLogger(NormalizeAreaOfResponsibility.class);

	private ServiceAgentFacade serviceAgentFacade;

	public Collection<String> process(Collection<String> source, String eAuthID) throws DLSBusinessStopException {
		Set<ServiceCenterFlpOfficeCodeBO> serviceCenterList = new TreeSet<ServiceCenterFlpOfficeCodeBO>();
		return process(source, eAuthID, serviceCenterList);
	}

	public Collection<String> process(Collection<String> serviceCenterOfficeCodeSource, String eAuthID,
			Set<ServiceCenterFlpOfficeCodeBO> serviceCenterListTarget) throws DLSBusinessStopException {
		Set<String> target = new TreeSet<String>();
		String errorEAuthID = null != eAuthID ? eAuthID : "Not Provided";

		if (null == serviceCenterOfficeCodeSource || serviceCenterOfficeCodeSource.isEmpty()
				|| ValidationUtils.isListWithSingleEmptyString(serviceCenterOfficeCodeSource)) {
			return target;
		}
		List<String> localFlpOfficeCode = new ArrayList<String>();
		String infoMessage = "eAuthID = ";
		for (String serviceCenterFLPOfficeCode : serviceCenterOfficeCodeSource) {
			try {
				if (LocationUtils.isValidFLPOfficeCode(serviceCenterFLPOfficeCode)) {
					if (LocationUtils.isStateFLPOfficeCode(serviceCenterFLPOfficeCode)) {
						
                    	target.add( serviceCenterFLPOfficeCode );
                        localFlpOfficeCode.add(serviceCenterFLPOfficeCode);
						
						/*
						 * Obtain all service center FLP Office Codes for this state.
						 */
						String stateAbbr = ServiceAgentFacade.getInstance()
								.getAbbreviation(serviceCenterFLPOfficeCode.substring(0, 2));
						if (stateAbbr == null) {
							logger.warn(infoMessage + errorEAuthID
									+ " Invalid FLP Office Code, cannot parse State from it: "
									+ serviceCenterFLPOfficeCode);
						} else {
							try {
								List<ServiceCenterFlpOfficeCodeBO> serviceCenterFLPCodes = ServiceAgentFacade
										.getInstance().retrieveServiceCenterFLPCodesByStateAbbr(stateAbbr);
								if (serviceCenterFLPCodes != null && !serviceCenterFLPCodes.isEmpty()) {
									for (ServiceCenterFlpOfficeCodeBO office : serviceCenterFLPCodes) {
										if (LocationUtils.isValidFLPOfficeCode(office.getCode())) {
											target.add(office.getCode());
											serviceCenterListTarget.add(office);
										}
									}
								}
							} catch (Exception e) {
								logger.warn("Unable to normalize given state FLP office code = "
										+ serviceCenterFLPOfficeCode, e);
							}
						}
					} else {
						target.add(serviceCenterFLPOfficeCode);
						localFlpOfficeCode.add(serviceCenterFLPOfficeCode);
					}
				} else {
					logger.warn(
							infoMessage + errorEAuthID + " has invalid flpOfficeCode = " + serviceCenterFLPOfficeCode);
				}
			} catch (NumberFormatException e) {
				logger.warn(
						infoMessage + errorEAuthID + " has an invalid flpOfficeCode = " + serviceCenterFLPOfficeCode);
			} catch (MRTInterfaceException e) {
				String message = "Unable to interact with MRT!";
				logger.error(message);
			}
		}
		if (target.isEmpty()) {
			String message = "Major validation and processing errors encountered during the normalization of the area of responsibility for eAuthID = "
					+ errorEAuthID + ".";
			logger.error(message + "  Note the eAuthID related warnings preceeding this log entry.");
		}
		if (!localFlpOfficeCode.isEmpty()) {
			List<ServiceCenterFlpOfficeCodeBO> serviceCenterFLPCodes = ServiceAgentFacade.getInstance()
					.retrieveFlpServiceCentersByFlpOfficeCode(localFlpOfficeCode);
			serviceCenterListTarget.addAll(serviceCenterFLPCodes);
		}
		return target;
	}

	public Collection<String> getAllStateOfficeCode(Collection<String> source) {
		Collection<String> stateFLPOfficeList = new ArrayList<String>();
		for (String officeCode : source) {
			if (LocationUtils.isStateFLPOfficeCode(officeCode)) {
				stateFLPOfficeList.add(officeCode);
			}
		}
		return stateFLPOfficeList;
	}

	public EmployeeData loadeEployeeData(AgencyToken token, String userAgencyCode) {
		IMRTProxyBS mrtProxyBS = ServiceAgentFacade.getInstance().getMrtProxyBusinessService();
		return mrtProxyBS.retrieveEmployeeData(token, userAgencyCode);
	}

	public void setServiceAgentFacade(ServiceAgentFacade serviceAgentFacade) {
		this.serviceAgentFacade = serviceAgentFacade;
	}

	public ServiceAgentFacade getServiceAgentFacade() {
		return serviceAgentFacade;
	}

	private NormalizeAreaOfResponsibility() {

	}
}
