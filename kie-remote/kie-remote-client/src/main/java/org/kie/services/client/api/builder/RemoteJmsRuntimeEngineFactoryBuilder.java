package org.kie.services.client.api.builder;

import java.net.URL;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRuntimeEngineBuilder;



/**
 * This class will be deleted after the 6.2.x branch. 
 * </p>
 * Please see the {@link RemoteJmsRuntimeEngineBuilder} interface.
 */
@Deprecated
public interface RemoteJmsRuntimeEngineFactoryBuilder extends RemoteRuntimeEngineBuilder<RemoteJmsRuntimeEngineBuilder, RemoteJmsRuntimeEngineFactory> {

    /**
     * Use the given url to look up and retrieve a Remote {@link InitialContext} instance. The
     * information in the remote {@link InitialContext} instance will be used to retrieve
     * the {@link Queue} and {@link ConnectionFactory} instances. 
     * </p>
     * <i>This method has been deprecated in favor of the {@link #addJbossServerHostName(String)} method.</i> 
     * @param serverUrl The url of the Jboss Server instance on which Console or BPMS is running.
     * @return The current instance of this builder
     */
    @Deprecated
    RemoteJmsRuntimeEngineBuilder addJbossServerUrl(URL serverUrl);
    
}