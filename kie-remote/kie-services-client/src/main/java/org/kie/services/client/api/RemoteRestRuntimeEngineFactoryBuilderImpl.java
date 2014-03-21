package org.kie.services.client.api;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.kie.services.client.api.builder.RemoteRestRuntimeEngineFactoryBuilder;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;

public class RemoteRestRuntimeEngineFactoryBuilderImpl implements RemoteRestRuntimeEngineFactoryBuilder {

    private RemoteConfiguration config;
    
    private String username;
    private String password;
    private URL url;
    
    RemoteRestRuntimeEngineFactoryBuilderImpl() { 
        this.config = new RemoteConfiguration(Type.REST);
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addDeploymentId(String deploymentId) {
        this.config.setDeploymentId(deploymentId);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addProcessInstanceId(long processInstanceId) {
        this.config.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addUserName(String userName) {
        this.username = userName;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addUrl(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addTimeout(int timeoutInSeconds) {
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder addExtraJaxbClasses(Class... classes) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        this.config.addJaxbClasses(classSet);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactoryBuilder clearJaxbClasses() {
        this.config.clearJaxbClasses();
        return this;
    }
    
    @Override
    public RemoteRestRuntimeEngineFactoryBuilder useFormBasedAuth(boolean formBasedAuth) {
        this.config.setUseFormBasedAuth(formBasedAuth);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineFactory build() throws InsufficientInfoToBuildException {
        if( url == null ) { 
            throw new InsufficientInfoToBuildException("A URL is required to build the factory.");
        }
        if( username == null ) { 
            throw new InsufficientInfoToBuildException("A URL is required to build the factory.");
        }
        if( password == null ) { 
            throw new InsufficientInfoToBuildException("A URL is required to build the factory.");
        }
        this.config.createRequestFactory(url, username, password);
        return new RemoteRestRuntimeEngineFactory(config.clone());
    }
    
    public static RemoteRestRuntimeEngineFactoryBuilderImpl newBuilder() { 
        return new RemoteRestRuntimeEngineFactoryBuilderImpl();
    }
}
