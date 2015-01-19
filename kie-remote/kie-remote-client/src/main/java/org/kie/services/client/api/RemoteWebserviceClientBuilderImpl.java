package org.kie.services.client.api;

import java.net.MalformedURLException;
import java.net.URL;

import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
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
        return null;
    }
    
    @Override
    public RemoteWebserviceClientBuilder addTimeout(int timeoutInSeconds) {
        config.setTimeout(timeoutInSeconds);
        return this;
    }

    protected void checkAndFinalizeConfig() { 
        RemoteRuntimeEngineFactory.checkAndFinalizeConfig(config, this);
    }
    
}
