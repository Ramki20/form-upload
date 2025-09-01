package gov.usda.fsa.fcao.flp.flpids.common.business.businessObjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * NRRSReceivableResponseBO
 * <br><br>
 * Encapsulates all the properties for NRRSReceivableResponseBO
 * 
 * @author Naresh.Gotoor
 * @version 12/10/13
 */
public class NRRSReceivableResponseBO implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5890588231893258126L;
	private List<BigDecimal> receivableIds;

	public NRRSReceivableResponseBO()
	{
		
	}
	
	/**
	 * @return the receivableIds
	 */
	public List<BigDecimal> getReceivableIds()
	{
		return receivableIds;
	}

	/**
	 * @param receivableIds the receivableIds to set
	 */
	public void setReceivableIds(List<BigDecimal> receivableIds)
	{
		this.receivableIds = receivableIds;
	}

}
