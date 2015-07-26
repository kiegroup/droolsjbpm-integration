package org.kie.services.client.api;

import java.util.HashSet;
import java.util.Set;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteRuntimeEngineBuilder;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;

abstract class AbstractRemoteRuntimeEngineBuilderImpl<T,R> implements RemoteRuntimeEngineBuilder<T,R> {

    protected RemoteConfiguration config;
    
    @Override
    public T addUserName(String userName) {
        config.setUserName(userName);
        return (T) this;
    }

    @Override
    public T addPassword(String password) {
        config.setPassword(password);
        return (T) this;
    }

    @Override
    public T addTimeout(int timeoutInSeconds) {
        config.setTimeout(timeoutInSeconds);
        return (T) this;
    }

    @Override
    public T addDeploymentId( String deploymentId ) {
        config.setDeploymentId(deploymentId);
        return (T) this;
    }

    @Override
    public T addExtraJaxbClasses( Class... classes ) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        this.config.addJaxbClasses(classSet);
        return (T) this;
    }

    @Override
    public T clearJaxbClasses() {
        this.config.clearJaxbClasses();
        return (T) this;
    }
    
    @Override
    public T addProcessInstanceId( long processInstanceId ) {
        this.config.setProcessInstanceId(processInstanceId);
        return (T) this;
    }

    @Override
    public T addCorrelationProperties( String... correlationProperty ) {
        this.config.addCorrelationProperties(correlationProperty);
        return (T) this;
    }

    @Override
    public T clearCorrelationProperties() {
        this.config.clearCorrelationProperties();
        return (T) this;
    }
}
