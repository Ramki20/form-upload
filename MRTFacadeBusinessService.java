package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.InvalidBusinessContractDataException;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.StateLocationAreaCodeBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.CountyBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.MailCodeBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.StateBO;
import gov.usda.fsa.fcao.flp.flpids.common.exception.MRTNoDataException;

import java.util.List;

import javax.naming.ServiceUnavailableException;

/**
 * 
 * @author ramesh.ponugoti  
 * 
 */


public interface MRTFacadeBusinessService {
	
	
	List<StateBO> getStatesList() throws  ServiceUnavailableException,InvalidBusinessContractDataException;
	List<CountyBO> getCountiesList(StateBC contract) throws  ServiceUnavailableException,InvalidBusinessContractDataException;
	List<MailCodeBO> getMailCodesList(StateBC contract) throws  ServiceUnavailableException,InvalidBusinessContractDataException;
	String getMailCode(StateLocationAreaCodeBC contract) throws  ServiceUnavailableException,
		InvalidBusinessContractDataException,MRTNoDataException;

}
