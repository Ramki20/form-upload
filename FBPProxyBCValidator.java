/*
 * Created on Dec 13, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators;

import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;

public class FBPProxyBCValidator {

	public static void validate(Integer coreCustomerId) throws DLSBCInvalidDataStopException {
		if (coreCustomerId == null || coreCustomerId < 0) {
			throw new DLSBCInvalidDataStopException("Valid CoreCustomerID is Required... CCID: " + coreCustomerId);
		}
	}
	
	private FBPProxyBCValidator() {}
}
