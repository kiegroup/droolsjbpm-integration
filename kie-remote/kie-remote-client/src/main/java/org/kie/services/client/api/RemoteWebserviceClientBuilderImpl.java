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

package org.kie.services.client.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;

/**
 * This is the internal implementation of the {@link RemoteWebserviceClientBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
abstract class RemoteWebserviceClientBuilderImpl<S> implements RemoteWebserviceClientBuilder<RemoteWebserviceClientBuilder, S> {

    protected RemoteConfiguration config;
    
    RemoteWebserviceClientBuilderImpl() { 
        this.config = new RemoteConfiguration(Type.WS);
    }

    @Override
    public RemoteWebserviceClientBuilder addUserName(String userName) {
        config.setUserName(userName);
        return this;
    }

    @Override
    public RemoteWebserviceClientBuilder addPassword(String password) {
        config.setPassword(password);
        return this;
    }

    @Override
    public RemoteWebserviceClientBuilder addServerUrl(URL url) {
        config.setServerBaseWsUrl(url);
        return this;
    }

    @Override
    public RemoteWebserviceClientBuilder addServerUrl( String instanceUrlString ) throws MalformedURLException {
        URL serverUrl = new URL(instanceUrlString);
        config.setServerBaseWsUrl(serverUrl);
        return this;
    }
    
    @Override
    public RemoteWebserviceClientBuilder addTimeout(int timeoutInSeconds) {
        config.setTimeout(timeoutInSeconds);
        return this;
    }
    
    @Override
    public RemoteWebserviceClientBuilder addDeploymentId(String deploymentId) {
        config.setDeploymentId(deploymentId);
        return this;
    }

    @Override
    public RemoteWebserviceClientBuilder addExtraJaxbClasses( Class... classes ) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        config.addJaxbClasses(classSet);
        return this;
    }
   
    @Override
    public RemoteWebserviceClientBuilder setWsdlLocationRelativePath( String wsdlLocationRelativePath ) {
        config.setWsdlLocationRelativePath(wsdlLocationRelativePath);
        return this;
    }
    
    @Override
    public RemoteWebserviceClientBuilder useHttpRedirect() {
        config.setHttpRedirect(true);
        return this;
    }
    
    protected void checkAndFinalizeConfig() { 
        RemoteRuntimeEngineFactory.checkAndFinalizeConfig(config, this);
    }
    
}
