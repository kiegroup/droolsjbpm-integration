package org.kie.services.remote.war;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.runtime.manager.impl.DefaultRuntimeEnvironment;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.jbpm.shared.services.cdi.Selectable;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.cdi.qualifier.PerProcessInstance;
import org.kie.internal.runtime.manager.cdi.qualifier.PerRequest;
import org.kie.internal.runtime.manager.cdi.qualifier.Singleton;
import org.kie.internal.task.api.UserGroupCallback;

@Singleton
public class TestRuntimeDependenciesProducer {
    
	@Inject
	@Selectable
	private UserGroupCallback userGroupCallback;

    @Produces
    @Singleton
    @PerRequest
    @PerProcessInstance
    public RuntimeEnvironment produceEnvironment(EntityManagerFactory emf) {
        SimpleRuntimeEnvironment environment = new DefaultRuntimeEnvironment(emf);
        environment.setUserGroupCallback(userGroupCallback);
        return environment;
    }
    
    @Produces
    public UserGroupCallback getUserGroupCallback() {
    	return userGroupCallback;
    }
    
    @Produces
    public IdentityProvider getIdentityProvider() {
    	return new IdentityProvider() {
			
			@Override
			public List<String> getRoles() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
		};
    }
}
