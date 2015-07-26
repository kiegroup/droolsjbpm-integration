package org.kie.services.client.api;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This is the internal implementation of the {@link RemoteJmsRuntimeEngineBuilderImpl} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteJmsRuntimeEngineBuilderImpl 
    extends AbstractRemoteRuntimeEngineBuilderImpl<RemoteJmsRuntimeEngineBuilder, RemoteJmsRuntimeEngineFactory> 
    implements org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder { 
    
    InitialContext remoteInitialContext = null;
    String jbossServerHostName = null;
    
    boolean createOwnFactory = false;
    String hostName = null;
    Integer jmsConnectorPort = null;

    String keystorePassword;
    String keystoreLocation;
    String truststorePassword;
    String truststoreLocation;
    boolean useKeystoreAsTruststore = false;
    
    /**
     * builder logic: 
     * 
     * - Queues: 
     * 1. User submits them 
     * 2. Retrieved via remote initial context 
     * 
     * - Factory
     * 1. if ssl: created here (with keystore, etc. )
     * 2. User submits host/port (create own)
     * 3. User submits remote initial context
     * 4. User submits it 
     */
    
    RemoteJmsRuntimeEngineBuilderImpl() {
        this.config = new RemoteConfiguration(Type.JMS);
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addRemoteInitialContext(InitialContext remoteInitialContext) {
       this.remoteInitialContext = remoteInitialContext; 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addJbossServerUrl(URL serverUrl) {
        addJbossServerHostName(serverUrl.getHost());
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addJbossServerHostName(String hostname) {
        this.jbossServerHostName = hostname;
        addHostName(hostname);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addKieSessionQueue(Queue ksessionQueue) {
        this.config.setKsessionQueue(ksessionQueue); 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addTaskServiceQueue(Queue taskServiceQueue) {
        this.config.setTaskQueue(taskServiceQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addResponseQueue(Queue responseQueue) {
        this.config.setResponseQueue(responseQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addConnectionFactory(ConnectionFactory connectionFactory) {
        this.config.setConnectionFactory(connectionFactory);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addHostName(String hostNameOrIp) {
        this.createOwnFactory = true;
        this.hostName = hostNameOrIp;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addJmsConnectorPort(int port) {
        this.createOwnFactory = true;
        this.jmsConnectorPort = port;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl useSsl(boolean useSsl) {
        this.createOwnFactory = useSsl;
        this.config.setUseSsl(useSsl);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilderImpl addTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
        this.useSsl(true);
        return this;
    }

    public RemoteJmsRuntimeEngineBuilderImpl useKeystoreAsTruststore() { 
        this.useKeystoreAsTruststore = true;
        this.useSsl(true);
        return this;
    }
  
    @Override
    public RemoteJmsRuntimeEngineBuilderImpl disableTaskSecurity() {
        config.setDisableTaskSecurity(true);
        return this;
    }

    private void checkAndFinalizeConfig() {
        RemoteRuntimeEngineFactory.checkAndFinalizeConfig(config, this);
    }
    
    /*
     * (non-Javadoc)
     * @see org.kie.services.client.api.builder.RemoteRuntimeEngineFactoryBuilder#build()
     */
    @Override
    public org.kie.remote.client.api.RemoteJmsRuntimeEngineFactory buildFactory() throws InsufficientInfoToBuildException {
        checkAndFinalizeConfig();
        // return new instance
        return new RemoteJmsRuntimeEngineFactory(config.clone());
    }

    /*
     * (non-Javadoc)
     * @see org.kie.services.client.api.builder.RemoteRuntimeEngineFactoryBuilder#buildRuntimeEngine()
     */
    @Override
    public RemoteRuntimeEngine build() { 
       checkAndFinalizeConfig();
       return new RemoteRuntimeEngine(config.clone());
    }
    
    void checkKeyAndTruststoreInfo() { 
        if( useKeystoreAsTruststore ) { 
            truststoreLocation = keystoreLocation;
            truststorePassword = keystorePassword;
        }
        
        if( keystorePassword == null ) { 
            throw new InsufficientInfoToBuildException("A keystore password is required to build the SSL JMS connection factory.");
        }
        if( truststorePassword == null ) { 
            throw new InsufficientInfoToBuildException("A truststore password is required to build the SSL JMS connection factory.");
        }
        
        String [][] pathInfos = { 
                { keystoreLocation, "keystore" },
                { truststoreLocation, "truststore" }
        };
        
        for( String [] pathInfo : pathInfos ) { 
            String path = pathInfo[0];
            String name = pathInfo[1];
            if( path == null ) { 
                throw new InsufficientInfoToBuildException("A " + name + " location is required to build the SSL JMS connection factory.");
            }
            if( path.startsWith("/") ) { 
                File storeFile = new File(path);
                if( ! storeFile.exists() ) { 
                    throw new InsufficientInfoToBuildException("No " + name + " file could be found at '" + path + "'");
                }
            } else { 
                URL storeFile = this.getClass().getResource("/" + path); 
                if( storeFile == null ) { 
                    throw new InsufficientInfoToBuildException("No " + name + " file could be found on the classpath at '" + path + "'");
                }
            }
        }
    }
    
    public static RemoteJmsRuntimeEngineBuilderImpl newBuilder() { 
        return new RemoteJmsRuntimeEngineBuilderImpl();
    }

}