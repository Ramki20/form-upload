package gov.usda.fsa.fcao.flp.security.userprofile.eas;


import gov.usda.fsa.eas.auth.AuthorizationManager;
import gov.usda.fsa.fcao.flp.security.userprofile.UserProfileImpl;
import gov.usda.fsa.fcao.flp.security.userprofile.UserSecurityProfile;
import gov.usda.fsa.fcao.flp.security.userprofile.UserSecurityProfileFactory;
import gov.usda.fsa.fcao.flp.security.userprofile.Exceptions.AuthorizationException;
import gov.usda.fsa.fcao.flp.security.userprofile.Exceptions.UnknownUserException;
import gov.usda.fsa.fcao.flp.security.userprofile.Exceptions.UserProfileException;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for engaging the EAS Authorization Manager service to construct
 * the UserSecurityProfile for the current user.
 * 
 */
public class EASUserSecurityProfileFactory
	implements UserSecurityProfileFactory
{
    /*
     * EAS attribute key names 
     */
    
    private static final String EAS_USER_FOUND_KEY = "eas.user.found";
    
    // Singleton reference
    public static final EASUserSecurityProfileFactory INSTANCE = new EASUserSecurityProfileFactory();
    
	public UserSecurityProfile getUserSecurityProfile(HttpServletRequest request)
    	throws UserProfileException{
    	return createUserSecurityProfile(request);
    }
	public UserSecurityProfile getUserSecurityProfile()
    	throws UserProfileException   {
    	return createUserSecurityProfile(null);
    }
	
	
	private UserSecurityProfile createUserSecurityProfile(HttpServletRequest request)
    	throws UserProfileException
    {
        try
        {
            if (!userFoundInEAS())
            {
                throw new UnknownUserException( "User's eAuth ID is unknown to EAS." );
            }
            
            UserSecurityProfile userProfile =  new UserProfileImpl(request);
            return userProfile;
        }
        catch (Exception e)
        {
            throw new AuthorizationException( e );
        }
    }
    
    
    /**
     * Not meant to be externally instantiated.
     */
    private EASUserSecurityProfileFactory()
    {
        super();
    }

    private boolean userFoundInEAS()
		throws gov.usda.fsa.eas.auth.AuthorizationException
	{
        boolean result = false;
        String found = AuthorizationManager.getAttribute( EAS_USER_FOUND_KEY );
        if ("true".equals( found ))
        {
            result = true;
        }
        return result;
	}

}
