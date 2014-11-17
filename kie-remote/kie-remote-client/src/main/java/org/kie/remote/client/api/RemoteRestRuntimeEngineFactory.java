package org.kie.remote.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * A builder for creating Rest remote API client instances of the {@link RuntimeEngine}.
 *  * </p>
 * Please see the {@link RemoteRuntimeEngineFactory#newRestBuilder()} method in order to create a {@link RemoteRuntimeEngineBuilder} 
 * instance. 
 * @see {@link RemoteRuntimeEngineBuilder#buildFactory()}
 */
public class RemoteRestRuntimeEngineFactory extends org.kie.services.client.api.RemoteRestRuntimeEngineFactory {
  
    public RemoteRestRuntimeEngineFactory(RemoteConfiguration config) {
        super(config);
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
        return new RemoteRuntimeEngine(config);
    }

}