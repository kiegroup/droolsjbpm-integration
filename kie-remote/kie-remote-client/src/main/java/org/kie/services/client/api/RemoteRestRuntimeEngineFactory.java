package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.builder.RemoteRestRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * A factory for creating REST remote API client instances of the {@link RuntimeEngine}.
 * @see {@link RemoteRuntimeEngineFactory}
 */
public class RemoteRestRuntimeEngineFactory extends RemoteRuntimeEngineFactory {

    private RemoteConfiguration config;

    RemoteRestRuntimeEngineFactory() { 
        // default constructor for builder use
    }
    
    RemoteRestRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }
  
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    public static RemoteRestRuntimeEngineBuilder newBuilder() {
        return newRestBuilder();
    }

}
