package org.kie.remote.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * A factory for creating JMS remote API client instances of the {@link RuntimeEngine}.
 * @see {@link RemoteRuntimeEngineBuilder#buildFactory()}
 */
public class RemoteJmsRuntimeEngineFactory extends org.kie.services.client.api.RemoteJmsRuntimeEngineFactory {
  
    public RemoteJmsRuntimeEngineFactory(RemoteConfiguration config) {
        super(config);
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
        return new RemoteRuntimeEngine(config);
    }

}