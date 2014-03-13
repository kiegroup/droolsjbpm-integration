package org.kie.services.remote.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jbpm.kie.services.impl.event.Deploy;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.remote.exception.DeploymentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <ul>
 * <li>Keeps track of the {@link RuntimeManager} instances for each deployment for use by the Remote API.</li>
 * <li>Keeps track of the list of classes in deployments.<ul>
 *   <li>This is necessary in order for serialization of inputs containing instances of (user) classes defined in the KJar deployments.</li>
 *   <li>See the {@link JaxbContextResolver} for more info.</li></ul>
 * <ul>
 */
@ApplicationScoped
public class DeploymentInfoBean {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentInfoBean.class);
    
    private Map<String, Collection<Class<?>>> deploymentClassesMap = new ConcurrentHashMap<String, Collection<Class<?>>>();
    
    private Map<String, RuntimeManager> domainRuntimeManagers = new ConcurrentHashMap<String, RuntimeManager>();  
    
    // Observer methods -----------------------------------------------------------------------------------------------------------
   
    /**
     * Called when the workbench/console/business-central deploys a new deployment.
     * @param event
     */
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.put(event.getDeploymentId(), event.getDeployedUnit().getRuntimeManager());
        if( runtimeManager != null ) { 
            logger.warn("RuntimeManager for domain {} has been replaced", event.getDeploymentId());
        }
        deploymentClassesMap.put(event.getDeploymentId(), event.getDeployedUnit().getDeployedClasses());
    }
   
    /**
     * Called when the workbench/console/business-central *un*deploys (removes) a deployment.
     * @param event
     */
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        RuntimeManager runtimeManager = domainRuntimeManagers.remove(event.getDeploymentId());
        if( runtimeManager == null ) { 
            logger.warn("RuntimeManager for domain {}  does not exist and can not be undeployed.", event.getDeploymentId());
        }
        deploymentClassesMap.remove(event.getDeploymentId());
    }
    
    // Methods for other beans/resources ------------------------------------------------------------------------------------------
   
    public RuntimeManager getRuntimeManager(String domainName) { 
       return domainRuntimeManagers.get(domainName);
    }

    public void disposeRuntimeEngine(RuntimeEngine runtimeEngine) {
        if (runtimeEngine != null) {
            RuntimeManager manager = ((RuntimeEngineImpl) runtimeEngine).getManager();
            manager.disposeRuntimeEngine(runtimeEngine);
        }
    }

    /**
     * Retrieve the relevant {@link RuntimeEngine} instance.
     * 
     * @param deploymentId The id of the deployment for the {@link RuntimeEngine}.
     * @param processInstanceId The process instance id, if available.
     * @return The {@link RuntimeEngine} instance.
     */
    public RuntimeEngine getRuntimeEngine(String deploymentId, Long processInstanceId) {
        RuntimeManager runtimeManager = getRuntimeManager(deploymentId);
        if (runtimeManager == null) {
            throw new DeploymentNotFoundException("No runtime manager could be found for deployment '" + deploymentId + "'.");
        }
        Context<?> runtimeContext;
        if( runtimeManager instanceof PerProcessInstanceRuntimeManager ) { 
            if( processInstanceId == null || processInstanceId == -1) { 
                throw new IllegalStateException("No process instance id supplied for operation!");
            }
            runtimeContext = new ProcessInstanceIdContext(processInstanceId);
        } else { 
            runtimeContext = EmptyContext.get();
        }
        return runtimeManager.getRuntimeEngine(runtimeContext);
    }
   
    /**
     * Used by classes involved with de/serialzation in order to retrieve (user-defined) clases 
     * to be used in de/serialization.
     * @param deploymentId The deployment unit id
     * @return A Collection of Classes that are in the deployment unit
     */
    public Collection<Class<?>> getDeploymentClasses(String deploymentId) { 
        if( deploymentId == null ) { 
            return Collections.emptySet();
        }
        Collection<Class<?>> classes = deploymentClassesMap.get(deploymentId);
        if( classes == null ) { 
            return Collections.emptySet();
        }
        return classes;
     }

     public Collection<String> getDeploymentIds() { 
         return deploymentClassesMap.keySet();
     }
     
}
