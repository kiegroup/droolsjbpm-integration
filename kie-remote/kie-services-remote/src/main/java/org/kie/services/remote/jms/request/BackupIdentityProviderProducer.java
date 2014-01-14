package org.kie.services.remote.jms.request;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import org.jbpm.kie.services.api.RequestScopedBackupIdentityProvider;
import org.kie.services.remote.jms.RequestMessageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class BackupIdentityProviderProducer {

    private static final Logger logger = LoggerFactory.getLogger(RequestMessageBean.class);
    
    private RequestScopedBackupIdentityProvider backupIdentityProvider = null;
    
    public RequestScopedBackupIdentityProvider createBackupIdentityProvider(String commandUser) { 
        logger.debug( "Creating identity provider for user: {}", commandUser);
        if( commandUser == null ) { 
            commandUser = "unknown";
        }
        final String nameValue = commandUser;
        this.backupIdentityProvider =  new RequestScopedBackupIdentityProvider() {
            private String name = nameValue;
            @Override
            public String getName() {
                return name;
            }
        };
        return this.backupIdentityProvider;
    }
    
    @Produces
    @RequestScoped
    public RequestScopedBackupIdentityProvider getJmsRequestScopeIdentityProvider() {
        logger.debug( "Producing backup identity bean for user: {}" , this.backupIdentityProvider.getName() );
        return this.backupIdentityProvider;
    }

}
