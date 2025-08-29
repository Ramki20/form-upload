package gov.usda.fsa.fcao.flp.flpids.common.business.businessServices;

import gov.usda.fsa.common.base.AgencyToken;
import gov.usda.fsa.fcao.flp.flpids.common.auth.DLSAgencyTokenFactory;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContractValidators.FarmRequestBCValidator;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessContracts.FarmRequestBC;
import gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects.FarmResponseBO;
import gov.usda.fsa.fcao.flp.flpids.common.business.exceptions.DLSBCInvalidDataStopException;
import gov.usda.fsa.parmo.farmrecords.dto.FarmDTO;
import gov.usda.fsa.parmo.farmrecords.dto.FarmResultDTO;
import gov.usda.fsa.parmo.frs.ejb.client.contract.RetrieveFarmsServiceContractWrapper;
import gov.usda.fsa.parmo.frs.ejb.client.reply.RetrieveFarmsResultWrapper;
import gov.usda.fsa.parmo.frs.ejb.service.FarmRecordsExternalService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.naming.ServiceUnavailableException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * FarmRecordBS
 * <br><br>
 * This class encapsulate the implementation to retrieve farm records.
 * 
 * @author sarita.gupta. 
 * @version 7/10/13
 * @version 9/6/13 Updated by kartik.dhingra
 * @version 11/45/13 	Updated by kartik.dhingra. 
 * 						Maintain coding standards. 
 * 						Removed unwanted exceptions. 
 * 						Renamed retrieveFarmReordByCustomer to retrieveFarmRecordByCustomer
 */
public class FarmRecordBS implements IFarmRecordBS
{
	private static final Logger			logger	= LogManager.getLogger( FarmRecordBS.class );
	private FarmRecordsExternalService	farmRecordsServiceBean;   

    //private final static String FRS_SERVICE_SPECIFIER = "gov/usda/common/frs_service_specifier";
  
	/**
	 * This method is just going to return a Farm Record Service Bean.
	 * @return FarmRecordsServiceBean
	 */
	public FarmRecordsExternalService getFarmRecordsServiceBean()
	{
		return farmRecordsServiceBean;
	}

	/**
	 * This method is just going to set a Farm Record Service Bean.
	 * @param FarmRecordsServiceBean
	 */
	public void setFarmRecordsServiceBean( FarmRecordsExternalService farmRecordsServiceBean )
	{
		this.farmRecordsServiceBean = farmRecordsServiceBean;
	}
	
	/**
	 * This method returns a list of FarmResponseBO on the basis of contract passed as an argument.
	 * @param contract
	 * @return
	 * @throws DLSBCInvalidDataStopException
	 */
	private List<FarmResponseBO> retrieveFarms(RetrieveFarmsServiceContractWrapper contract ) throws DLSBCInvalidDataStopException
	{
		RetrieveFarmsResultWrapper wrapper;
		Collection<FarmDTO> listFarmDTO = new ArrayList<FarmDTO>();

		boolean endOfListIndicator = false;
		try
		{
			
			while( !endOfListIndicator )
			{
				/**
				 * Making method call to External service - Farm record web service
				 */
				wrapper = farmRecordsServiceBean.retrieveFarms( contract );
				FarmResultDTO resultDTO = wrapper.getResult();
				
				if( !StringUtils.isBlank( resultDTO.getExceptionMessage() ) )
				{
					String msg =
						"Farm Records service returned error: exceptionMessage=" + resultDTO.getExceptionMessage()
							+ "; contract=" + contract.toString();
					logger.error( msg );
					throw new ServiceUnavailableException( "Exception Message from FarmService ::"
						+ resultDTO.getExceptionMessage() );
				}
				FarmDTO[] tmpFarmDTO = resultDTO.getFarmList();
				
				if( tmpFarmDTO != null && tmpFarmDTO.length > 0 )
				{
					listFarmDTO.addAll( Arrays.asList( tmpFarmDTO ) );
				}
				endOfListIndicator = resultDTO.isEndOfListIndicator();
				
				if( !endOfListIndicator )
				{
					contract.setLowerBoundFarmNumber( tmpFarmDTO[tmpFarmDTO.length - 1].getNumber() );
				}
			}
			List<FarmResponseBO> farmResponseBOList=new ArrayList<FarmResponseBO>();
			
			for(FarmDTO farmDTO: listFarmDTO){
				if(farmDTO != null){
					FarmResponseBO farmResponseBO=new FarmResponseBO();
					farmResponseBO.setAdminCountyCode(farmDTO.getAdminCountyCode());
					farmResponseBO.setAdminStateCode(farmDTO.getAdminStateCode());
					farmResponseBO.setFarmNumber(farmDTO.getNumber());
					farmResponseBOList.add(farmResponseBO);
				}
			}
			return farmResponseBOList;
		}
		catch( Throwable frExp )
		{
			String message = contract.toString();
			logger.error( "Error occured while calling FarmRecordBS.retrieveFarms() with contract input: " + message, frExp );
			throw new DLSBCInvalidDataStopException( "Invalid Data received from Farm Record Services when calling FarmRecordBS.retrieveFarms(). Message: "
					+ frExp.getMessage(), frExp );
		}
	}


	/**
	 * @param farmRequestBC Farm Records request business contract
	 * @return
	 * @throws DLSBCInvalidDataStopException 
	 */
	public List<FarmResponseBO> retrieveFarmRecordByCustomer(FarmRequestBC farmRequestBC) throws DLSBCInvalidDataStopException
	{
		FarmRequestBCValidator.validate(farmRequestBC);
		
		// get agency token from DLSAgencyTokenFactory
		AgencyToken token = DLSAgencyTokenFactory.createAgencyTokenFromDLSAgencyToken(farmRequestBC.getAgencyToken());
		
		RetrieveFarmsServiceContractWrapper contract=new RetrieveFarmsServiceContractWrapper(token);
		contract.setCustomerId(Integer.parseInt(String.valueOf(farmRequestBC.getCoreCustomerId())));
		contract.setYear(Short.parseShort(String.valueOf(farmRequestBC.getYear())));
		contract.setIncludeCustomer(farmRequestBC.getIncludeCustomerWithResponse());
		contract.setIncludeNonActiveCustomer(farmRequestBC.getIncludeNonActiveCustomerWithResponse());
		contract.setIncludeTract(farmRequestBC.getIncludeTractInfoWithResponse());
		contract.setIncludeCrop(farmRequestBC.getIncludeCropWithResponse());
		contract.setLowerBoundFarmNumber(farmRequestBC.getLowerBoundFarmNumber());
		List<FarmResponseBO> farmResponseBOList = retrieveFarms(contract);
		return farmResponseBOList;
	}
}
