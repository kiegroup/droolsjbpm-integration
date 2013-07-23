package org.kie.services.remote.war;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;
import org.jbpm.kie.services.api.Vfs;
import org.jbpm.kie.services.impl.VFSDeploymentUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class TestDeploymentLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDeploymentLoader.class);

    @Inject
    @Vfs
    private DeploymentService deploymentService;
    
    @PostConstruct
    public void init() { 
        logger.warn("Initializing the 'test' domain.");
        DeploymentUnit deploymentUnit = new VFSDeploymentUnit("test", "", "src/test/resources/repo/test/");
        deploymentService.deploy(deploymentUnit);
    }
    
}
