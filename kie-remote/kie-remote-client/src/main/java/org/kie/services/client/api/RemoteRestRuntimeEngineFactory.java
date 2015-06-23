/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
