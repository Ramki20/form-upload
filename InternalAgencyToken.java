package gov.usda.fsa.fcao.flp.flpids.common.utilities;

/**
 * This will generate agency token for other services.This can be replaced with AgencyTocken factory builder form
 * commons.
 * 
 * @author sarita.gupta
 */

import gov.usda.fsa.common.base.AgencyToken;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InternalAgencyToken
{
	// This will create a agency token
	public AgencyToken getAgencyToken()
	{
		InetAddress addr = null;
		String userIdentifier = "flp_web_user";
		try
		{
			addr = InetAddress.getLocalHost();
		}
		catch (UnknownHostException exception)
		{
			throw new RuntimeException("Unable to retrieve local host" + ": " + exception.getMessage(), exception);

		}

		AgencyToken agencyToken = new AgencyToken();
		agencyToken.setUserIdentifier(userIdentifier);
		agencyToken.setApplicationIdentifier("DLS");
		agencyToken.setRequestHost(addr.getHostName());
		agencyToken.setProcessingNode(addr.getHostAddress());
		return agencyToken;
	}

}
