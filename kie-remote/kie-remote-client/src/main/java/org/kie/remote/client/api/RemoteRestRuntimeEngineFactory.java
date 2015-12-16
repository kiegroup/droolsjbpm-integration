/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
 * </p>
 * This class will be removed as of jBPM 7.x
 *  
 * @see {@link RemoteRuntimeEngineBuilder#buildFactory()}
 */
@Deprecated
public class RemoteRestRuntimeEngineFactory extends org.kie.services.client.api.RemoteRestRuntimeEngineFactory {
  
    public RemoteRestRuntimeEngineFactory(RemoteConfiguration config) {
        super(config);
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
        return new RemoteRuntimeEngine(config);
    }

}