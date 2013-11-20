package org.kie.services.remote.cdi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.drools.core.command.GetDefaultValue;
import org.jbpm.kie.services.api.DeployedUnit;
import org.jbpm.kie.services.impl.event.Deploy;
import org.jbpm.kie.services.impl.event.DeploymentEvent;
import org.jbpm.kie.services.impl.event.Undeploy;
import org.jbpm.runtime.manager.impl.PerProcessInstanceRuntimeManager;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.remote.exception.DomainNotFoundBadRequestException;
import org.kie.services.remote.rest.JaxbContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class keeps track of the list of classes in deployments. 
 * </p>
 * This is necessary in order for serialization of inputs containing instances of (user) classes defined in the KJar deployments. 
 * </p>
 * See the {@link JaxbContextResolver} for more info.
 */
@Singleton
public class DeploymentClassNamesBean {

    private Map<String, Collection<String>> deploymentClassNamesMap = new ConcurrentHashMap<String, Collection<String>>();
    
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        deploymentClassNamesMap.put(event.getDeploymentId(), event.getDeployedUnit().getDeployedClassNames());
    }
    
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        deploymentClassNamesMap.remove(event.getDeploymentId());
    }
    
    public Collection<String> getClassNames(String deploymentId) { 
       Collection<String> classNames = deploymentClassNamesMap.get(deploymentId);
       if( classNames == null ) { 
           classNames = new HashSet<String>();
       }
       return classNames;
    }

}
