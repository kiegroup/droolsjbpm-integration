package org.kie.remote.services.jms.request;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.kie.services.impl.audit.ServicesAwareAuditEventBuilder;
import org.jbpm.services.cdi.RequestScopedBackupIdentityProvider;
import org.kie.remote.services.jms.RequestMessageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See the {@link ServicesAwareAuditEventBuilder} bean for more context.
 * </p>
 * This bean provides a {@link RequestScopedBackupIdentityProvider} instance 
 * for JMS requests, which have a Request scope but for which the "normally" 
 * provided ("normally" as in with REST requests) {@link IdentityProvider} 
 * is not available. This due to vagaries of the code, as always. 
 */
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
        if (this.backupIdentityProvider != null) {
            logger.debug( "Producing backup identity bean for user: {}" , this.backupIdentityProvider.getName() );
            return this.backupIdentityProvider;
        } else {
            // in case there is none set return dummy one as @RequestScoped producers cannot return null
            return new RequestScopedBackupIdentityProvider() {
                @Override
                public String getName() {
                    return "unknown";
                }
            };
        }
    }

}
