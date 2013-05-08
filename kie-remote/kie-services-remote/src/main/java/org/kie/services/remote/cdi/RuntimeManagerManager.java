package org.kie.services.remote.cdi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbpm.kie.services.impl.event.Deploy;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.kie.api.runtime.manager.RuntimeManager;

/*
 * {@inheritdoc}
 */
@Singleton
public class RuntimeManagerManager {

    private static ConcurrentHashMap<String, RuntimeManager> domainRuntimeManagers = new ConcurrentHashMap<String, RuntimeManager>();
   
    @Inject
    private Logger logger;
    
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.put(event.getDeploymentId(), event.getDeployedUnit().getRuntimeManager());
        if( runtimeManager != null ) { 
            logger.warning("RuntimeManager for domain " + event.getDeploymentId() + " has been replaced.");
        }
    }
    
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.remove(event.getDeploymentId());
        if( runtimeManager == null ) { 
            logger.warning("RuntimeManager for domain " + event.getDeploymentId() + " does not exist and can not be undeployed.");
        }
    }
    
    public RuntimeManager getRuntimeManager(String domainName) { 
       return domainRuntimeManagers.get(domainName);
    }

}
