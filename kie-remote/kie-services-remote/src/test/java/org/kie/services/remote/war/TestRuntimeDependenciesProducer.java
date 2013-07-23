package org.kie.services.remote.war;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.kie.services.api.Kjar;
import org.jbpm.runtime.manager.impl.DefaultRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.shared.services.cdi.Selectable;
import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;
import org.kie.internal.task.api.UserGroupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TestRuntimeDependenciesProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(TestRuntimeDependenciesProducer.class);
    
	@Inject
	@Selectable
	private UserGroupCallback userGroupCallback;
    
    @Produces
    public UserGroupCallback getUserGroupCallback() {
    	return userGroupCallback;
    }
    
    @Inject
    @Kjar
    DeploymentService deploymentService;

    @Produces
    @Default
    public DeploymentService produceDeploymentService() { 
        return deploymentService;
    }
    
    private final IOService ioService = new IOServiceDotFileImpl();
    
    @Produces
    @Named("ioStrategy")
    public IOService ioService() {
        return ioService;
    }
    
    @Produces
    @Singleton
    @PerRequest
    @PerProcessInstance
    public RuntimeEnvironment produceEnvironment(EntityManagerFactory emf) {
        SimpleRuntimeEnvironment environment = new DefaultRuntimeEnvironment(emf);
        environment.setUserGroupCallback(userGroupCallback);
        return environment;
    }
    
    public static final String IDENTITY_NAME = "salaboy";
    public static final List<String> IDENTITY_ROLES = new ArrayList<String>();
    static { 
       IDENTITY_ROLES.add("kie-user"); 
    }
    
    @Produces
    public IdentityProvider getIdentityProvider() {
    	return new IdentityProvider() {
			
			@Override
			public List<String> getRoles() {
				return IDENTITY_ROLES;
			}
			
			@Override
			public String getName() {
				return IDENTITY_NAME;
			}
		};
    }
}
