package org.kie.provider;

import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.rpc.SessionInfo;

public interface SessionInfoProvider {
    
    public static final String UNKNOWN_SESSION_ID = "--";

    String getId();

    User getIdentity();
    
    SessionInfo getSessionInfo();
    
}
