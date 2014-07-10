package org.kie.services.client.api.builder;

import java.net.URL;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactoryBuilderImpl;

public interface RemoteJmsRuntimeEngineFactoryBuilder  extends RemoteRuntimeEngineFactoryBuilder<RemoteJmsRuntimeEngineFactoryBuilder, RemoteJmsRuntimeEngineFactory> {

    RemoteJmsRuntimeEngineFactoryBuilder addRemoteInitialContext(InitialContext remoteInitialContext); 
    RemoteJmsRuntimeEngineFactoryBuilder addJbossServerUrl(URL serverUrl);
    
    RemoteJmsRuntimeEngineFactoryBuilder addKieSessionQueue(Queue ksessionQueue);
    RemoteJmsRuntimeEngineFactoryBuilder addTaskServiceQueue(Queue taskServiceQueue);
    RemoteJmsRuntimeEngineFactoryBuilder addResponseQueue(Queue responseQueue);
    RemoteJmsRuntimeEngineFactoryBuilder addConnectionFactory(ConnectionFactory connectionFactory);
    RemoteJmsRuntimeEngineFactoryBuilder useSsl(boolean useSsl);
    
    RemoteJmsRuntimeEngineFactoryBuilderImpl addHostName(String string);
    RemoteJmsRuntimeEngineFactoryBuilderImpl addJmsConnectorPort(int port);
    
    RemoteJmsRuntimeEngineFactoryBuilderImpl addKeystorePassword(String string);
    RemoteJmsRuntimeEngineFactoryBuilderImpl addKeystoreLocation(String string);
    RemoteJmsRuntimeEngineFactoryBuilderImpl addTruststorePassword(String string);
    RemoteJmsRuntimeEngineFactoryBuilderImpl addTruststoreLocation(String string);
    
}