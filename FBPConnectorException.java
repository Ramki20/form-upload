package gov.usda.fsa.fcao.flpids.fbpservice.jaxws;

/**
 * @author FCAO
 *
 */
public class FBPConnectorException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public FBPConnectorException() {
		super();
	}

	public FBPConnectorException(String message) {
		super(message);
	}

	public FBPConnectorException(String message, Throwable e) {
		super(message,e);
	}
	
}
