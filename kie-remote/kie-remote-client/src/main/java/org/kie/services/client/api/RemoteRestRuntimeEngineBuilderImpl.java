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

import java.net.URL;

import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This is the internal implementation of the {@link RemoteRestRuntimeEngineBuilderImpl} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteRestRuntimeEngineBuilderImpl 
    extends AbstractRemoteRuntimeEngineBuilderImpl<RemoteRestRuntimeEngineBuilder, RemoteRestRuntimeEngineFactory> 
    implements org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder { 

    URL url;
    
    RemoteRestRuntimeEngineBuilderImpl() { 
        this.config = new RemoteConfiguration(Type.REST);
    }

    @Override
    public RemoteRestRuntimeEngineBuilderImpl addUrl(URL url) {
        config.setServerBaseRestUrl(url);
        return this;
    }

    private void checkAndFinalizeConfig() { 
        RemoteRuntimeEngineFactory.checkAndFinalizeConfig(config, this);
    }
    
    @Override
    public org.kie.remote.client.api.RemoteRestRuntimeEngineFactory buildFactory() throws InsufficientInfoToBuildException {
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
