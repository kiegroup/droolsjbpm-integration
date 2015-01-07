package org.kie.provider.impl;

import static org.kie.provider.impl.SafeIdentityProvider.UNKNOWN_USER_IDENTITY;

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
import org.uberfire.rpc.SessionInfo;
import java.io.Serializable;

@SessionScoped
public class SafeSessionInfoProvider implements SessionInfoProvider, Serializable {

    @Inject
    private Instance<SessionInfo> delegate;

    @Inject 
    private IdentityProvider identityProvider; 
  
    private static String UNKNOWN_SESSION_ID = "--";
    
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
            List<String> roleStrList = identityProvider.getRoles();
            List<Role> roles = new ArrayList<Role>(roleStrList.size());
            for( String roleStr : roleStrList ) { 
                roles.add( new RoleImpl(roleStr));
            }
            return new UserImpl( identityProvider.getName(), roles );
        } 
       
        // default
        try {
            return delegate.get().getIdentity(); 
        } catch ( Exception e ) {
            return new UserImpl( UNKNOWN_USER_IDENTITY );
        }
    }
}
