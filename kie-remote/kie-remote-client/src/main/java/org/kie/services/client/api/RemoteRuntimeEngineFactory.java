package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineBuilder;
import org.kie.services.client.api.builder.RemoteRestRuntimeEngineBuilder;
import org.kie.services.client.api.builder.RemoteRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This factory class is the start point for building and configuring {@link RuntimeEngine} instances
 * that can interact with the remote API. 
 * </p> 
 * The main use for this class will be to create builder instances (see 
 * {@link #newJmsBuilder()} and * {@link #newRestBuilder()}). These builder instances 
 * can then be used to either directly create a {@link RuntimeEngine} instance that will 
 * act as a client to the remote (REST or JMS) API, or to create an instance of this factory.
 * </p>
 * An instance of this factory can be used to create client {@link RuntimeEngine} instances
 * using the {@link newRuntimeEngine()} method. 
 */
public abstract class RemoteRuntimeEngineFactory {

    /**
     * @return a new (remote client) {@link RuntimeEngine} instance.
     * @see {@link RemoteRuntimeEngineBuilder#buildRuntimeEngine()}
     */
    abstract public RemoteRuntimeEngine newRuntimeEngine();

    /**
     * Create a new {@link RemoteJmsRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteJmsRuntimeEngineBuilder} instance
     */
    public static RemoteJmsRuntimeEngineBuilder newJmsBuilder() { 
       return new RemoteJmsRuntimeEngineBuilderImpl(); 
    }
    
    /**
     * Create a new {@link RemoteRestRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteRestRuntimeEngineBuilder} instance
     */
    public static RemoteRestRuntimeEngineBuilder newRestBuilder() { 
       return new RemoteRestRuntimeEngineBuilderImpl(); 
    }
}