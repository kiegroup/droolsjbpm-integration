package org.kie.provider.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.kie.internal.identity.IdentityProvider;
import org.kie.provider.SessionInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.rpc.impl.SessionInfoImpl;

@SessionScoped
public class SafeSessionInfoProvider implements SessionInfoProvider, Serializable {

    /** Generated serial version UID */
    private static final long serialVersionUID = 8510219062936244657L;

    private static final Logger logger = LoggerFactory.getLogger( SafeIdentityProvider.class );
            
    @Inject
    private Instance<SessionInfo> delegate;

    @Inject 
    private IdentityProvider identityProvider; 
    
    @Override
    public String getId() {
        if( delegate.isUnsatisfied() ) { 
            return UNKNOWN_SESSION_ID;
        }
        
        // default
        try {
            return delegate.get().getId();
        } catch ( Exception e ) {
            return UNKNOWN_SESSION_ID;
        }
    }

    @Override
    public User getIdentity() {
        if( delegate.isUnsatisfied() ) { 
           return getUserFromIdentityProvider(); 
        } 
       
        // default
        try {
            return delegate.get().getIdentity(); 
        } catch ( Exception e ) {
            logger.debug("SessionInfo bean was available but could not return identity: " + e.getMessage(), e );
            return getUserFromIdentityProvider();
        }
    }
    
    private User getUserFromIdentityProvider() { 
        List<String> roleStrList = identityProvider.getRoles();
        List<Role> roles = new ArrayList<Role>(roleStrList.size());
        for( String roleStr : roleStrList ) { 
            roles.add( new RoleImpl(roleStr));
        }
        return new UserImpl( identityProvider.getName(), roles ); 
    }

    @Override
    public SessionInfo getSessionInfo() {
        if( delegate.isUnsatisfied() ) { 
            return createSessionInfo();
        }
        
        try { 
            SessionInfo sessionInfo = delegate.get();
            sessionInfo.getId();
            return sessionInfo;
        } catch( Exception e ) { 
            logger.debug("SessionInfo bean was available but could not be retrieved: " + e.getMessage(), e );
            return createSessionInfo();
        }
    }
        
    private SessionInfo createSessionInfo() { 
        String id = getId();
        User identity = getUserFromIdentityProvider();
        return new SessionInfoImpl(id, identity);
    }
}
