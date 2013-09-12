package org.kie.services.remote.cdi;

import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jbpm.kie.services.impl.event.Deploy;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.kie.api.runtime.manager.RuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * {@inheritdoc}
 */
@Singleton
public class RuntimeManagerManager {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeManagerManager.class);
    
    private static ConcurrentHashMap<String, RuntimeManager> domainRuntimeManagers = new ConcurrentHashMap<String, RuntimeManager>();  
    
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.put(event.getDeploymentId(), event.getDeployedUnit().getRuntimeManager());
        if( runtimeManager != null ) { 
            logger.warn("RuntimeManager for domain {} has been replaced", event.getDeploymentId());
        }
    }
    
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.remove(event.getDeploymentId());
        if( runtimeManager == null ) { 
            logger.warn("RuntimeManager for domain {}  does not exist and can not be undeployed.", event.getDeploymentId());
        }
    }
    
    public RuntimeManager getRuntimeManager(String domainName) { 
       return domainRuntimeManagers.get(domainName);
    }

}
