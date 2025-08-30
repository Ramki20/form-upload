package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

/**
 * @author FCAO
 *
 */
public class FBPConnectorFactory 
{
	private static FBPConnectorFactory INSTANCE;
	
	private static FBPConnector fbpConnector = null;

	public static FBPConnectorFactory getInstance() 
	{
		return INSTANCE;
	}

	private FBPConnectorFactory() 
	{
	}
	
	public static FBPConnector getConnector() 
	{
		if(fbpConnector == null)
		{
			fbpConnector = new FBPServiceStaticClient();
		}
		return fbpConnector;
	}
	
	
}
