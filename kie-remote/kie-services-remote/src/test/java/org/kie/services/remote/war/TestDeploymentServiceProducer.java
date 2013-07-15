package org.kie.services.remote.war;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.Kjar;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;

@Singleton
public class TestDeploymentServiceProducer {
    
    @Inject
    @Kjar
    DeploymentService deploymentService;

    @Produces
    @Default
    public DeploymentService produceDeploymentService() { 
        return deploymentService;
    }
    
}
