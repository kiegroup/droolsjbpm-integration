package org.kie.services.client.api.builder;

import java.net.URL;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;

public interface RemoteJmsRuntimeEngineBuilder extends RemoteRuntimeEngineBuilder<RemoteJmsRuntimeEngineBuilder, RemoteJmsRuntimeEngineFactory> {

    /**
     * Add a remote {@link InitialContext} instance to the configuration. This
     * {@link InitialContext} instance is then used to retrieve the
     * JMS {@link Queue} instances 
     * so that the 
     * @param remoteInitialContext
     * @return
     */
    RemoteJmsRuntimeEngineBuilder addRemoteInitialContext(InitialContext remoteInitialContext); 
    RemoteJmsRuntimeEngineBuilder addJbossServerUrl(URL serverUrl);
    
    RemoteJmsRuntimeEngineBuilder addKieSessionQueue(Queue ksessionQueue);
    RemoteJmsRuntimeEngineBuilder addTaskServiceQueue(Queue taskServiceQueue);
    RemoteJmsRuntimeEngineBuilder addResponseQueue(Queue responseQueue);
    RemoteJmsRuntimeEngineBuilder addConnectionFactory(ConnectionFactory connectionFactory);
    RemoteJmsRuntimeEngineBuilder useSsl(boolean useSsl);
    
    RemoteJmsRuntimeEngineBuilder addHostName(String string);
    RemoteJmsRuntimeEngineBuilder addJmsConnectorPort(int port);
    
    RemoteJmsRuntimeEngineBuilder addKeystorePassword(String string);
    RemoteJmsRuntimeEngineBuilder addKeystoreLocation(String string);
    RemoteJmsRuntimeEngineBuilder addTruststorePassword(String string);
    RemoteJmsRuntimeEngineBuilder addTruststoreLocation(String string);
    RemoteJmsRuntimeEngineBuilder useKeystoreAsTruststore();
    
}