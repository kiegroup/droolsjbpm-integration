package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This class will be deleted as of 7.x
 * </p>
 * A factory for creating REST remote API client instances of the {@link RuntimeEngine}.
 *
 * @see {@link org.kie.remote.client.api.RemoteRestRuntimeEngineFactory}
 */
@Deprecated
public class RemoteRestRuntimeEngineFactory {

    // The name of this class may not be changed until 7.x for backwards compatibility reasons!
   
    protected RemoteConfiguration config;
    
    protected RemoteRestRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }
  
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    /**
     * @see {@link RemoteRuntimeEngineFactory#newRestBuilder()}
     */
    @Deprecated
    public static RemoteRestRuntimeEngineBuilder newBuilder() {
        return RemoteRuntimeEngineFactory.newRestBuilder();
    }

}
