package org.kie.remote.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;


/**
 * This interface defines the fluent builder methods that can be used when either configuring a remote REST or remote JMS
 * runtime engine instance. 
 * 
 * @param <B> The builder instance type
 * @param <R> The factory instance type
 */
public interface RemoteRuntimeEngineBuilder<B, F> extends RemoteClientBuilder<B> {

    /**
     * Adds the process instance id, which may be necessary when interacting
     * with deployments that employ the {@link RuntimeStrategy#PER_PROCESS_INSTANCE}.
     * @param processInstanceId The process instance id
     * @return The builder instance
     */
    B addProcessInstanceId(long processInstanceId);
    
    /**
     * If the {@link RemoteRuntimeEngineBuilder} is being reused (in order to build
     * multiple {@link RemoteRuntimeEngineFactory}'s, then this method can be called between 
     * {@ RemoteRuntimeEngineFactoryBuilder#build()} methods to reset the list of user-defined
     * classes being used by the builder. 
     * @return The builder instance
     */
    B clearJaxbClasses();
    
    /**
     * Creates a {@link RemoteRuntimeEngineFactory} instance, using the 
     * configuration given.
     * @return The {@link RemoteRuntimeEngineFactory} instance
     * @throws InsufficientInfoToBuildException when insufficient information 
     * is provided to build the {@link RemoteRuntimeEngineFactory}
     * @see {@link RemoteRuntimeEngineBuilder#build()}.
     */
    F buildFactory() throws InsufficientInfoToBuildException;
   
    /**
     * Creates a {@link RuntimeEngine} instance, using the 
     * configuration built up to this point. 
     * </p>
     * 
     * @return The {@link RuntimeEngine} instance
     * @throws @{link InsufficientInfoToBuildException} when insufficient information 
     * is provided to build the {@link RuntimeEngine}
     */
    RuntimeEngine build();

}