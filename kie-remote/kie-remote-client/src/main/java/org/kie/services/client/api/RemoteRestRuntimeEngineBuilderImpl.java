package org.kie.services.client.api;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.kie.services.client.api.builder.RemoteRestRuntimeEngineBuilder;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This is the internal implementation of the {@link RemoteRestRuntimeEngineBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
public class RemoteRestRuntimeEngineBuilderImpl implements RemoteRestRuntimeEngineBuilder {

    private RemoteConfiguration config;
    
    private String username;
    private String password;
    private URL url;
    
    RemoteRestRuntimeEngineBuilderImpl() { 
        this.config = new RemoteConfiguration(Type.REST);
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addDeploymentId(String deploymentId) {
        this.config.setDeploymentId(deploymentId);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addProcessInstanceId(long processInstanceId) {
        this.config.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addUserName(String userName) {
        this.username = userName;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addUrl(URL url) {
        this.url = url;
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addTimeout(int timeoutInSeconds) {
        config.setTimeout(timeoutInSeconds);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder addExtraJaxbClasses(Class... classes) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        this.config.addJaxbClasses(classSet);
        return this;
    }

    @Override
    public RemoteRestRuntimeEngineBuilder clearJaxbClasses() {
        this.config.clearJaxbClasses();
        return this;
    }
    
    @Override
    @Deprecated
    public RemoteRestRuntimeEngineBuilder useFormBasedAuth(boolean formBasedAuth) {
        // TODO: delete me after 6.2.0.x
        return this;
    }

    private void checkAndFinalizeConfig() { 
        if( url == null ) { 
            throw new InsufficientInfoToBuildException("A URL is required to build the factory.");
        }
        if( username == null ) { 
            throw new InsufficientInfoToBuildException("A user name is required to build the factory.");
        }
        if( password == null ) { 
            throw new InsufficientInfoToBuildException("A password is required to build the factory.");
        }
        this.config.createHttpRequest(url, username, password);
    }
    
    @Override
    public RemoteRestRuntimeEngineFactory buildFactory() throws InsufficientInfoToBuildException {
        checkAndFinalizeConfig();
        return new RemoteRestRuntimeEngineFactory(config.clone());
    }
   
    @Override
    public RemoteRuntimeEngine build() { 
        checkAndFinalizeConfig();
        return new RemoteRuntimeEngine(config.clone());
    }
    
    public static RemoteRestRuntimeEngineBuilderImpl newBuilder() { 
        return new RemoteRestRuntimeEngineBuilderImpl();
    }
}
