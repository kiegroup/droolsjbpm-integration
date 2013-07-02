package org.kie.services.remote.war;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;
import org.jbpm.kie.services.api.Vfs;
import org.jbpm.kie.services.impl.VFSDeploymentUnit;
import org.kie.commons.java.nio.file.api.FileSystemProviders;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;

@Singleton
@Startup
public class TestDomainLoader {

    @Inject
    @Vfs
    private DeploymentService deploymentService;
    
    @Inject
    private Logger logger;
    
    @PostConstruct
    public void init() { 
        System.out.println("Initializing the 'test' domain.");
        logger.warning("Initializing the 'test' domain.");
        DeploymentUnit deploymentUnit = new VFSDeploymentUnit("test", "", "src/test/resources/repo/test/");
        deploymentService.deploy(deploymentUnit);
    }
    
}
