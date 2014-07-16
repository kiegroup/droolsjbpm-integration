package org.kie.services.client.api;

import static org.kie.services.client.api.RemoteJmsRuntimeEngineFactory.getRemoteJbossInitialContext;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineBuilder;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

/**
 * This is the internal implementation of the {@link RemoteJmsRuntimeEngineBuilder} class.
 * </p>
 * It takes care of implementing the methods specified as well as managing the 
 * state of the internal {@link RemoteConfiguration} instance.
 */
class RemoteJmsRuntimeEngineBuilderImpl implements RemoteJmsRuntimeEngineBuilder {

    private RemoteConfiguration config;
    
    private InitialContext remoteInitialContext = null;
    
    private URL jbossServerUrl = null;
    
    private boolean createOwnFactory = false;
    private String hostName = null;
    private Integer jmsConnectorPort = null;

    private String keystorePassword;
    private String keystoreLocation;
    private String truststorePassword;
    private String truststoreLocation;
    private boolean useKeystoreAsTruststore = false;
    
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
    public RemoteJmsRuntimeEngineBuilder addDeploymentId(String deploymentId) {
        this.config.setDeploymentId(deploymentId);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addProcessInstanceId(long processInstanceId) {
        this.config.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addUserName(String userName) {
        this.config.setUserName(userName);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addPassword(String password) {
        this.config.setPassword(password);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addTimeout(int timeoutInSeconds) {
        this.config.setTimeout((long) timeoutInSeconds);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addExtraJaxbClasses(Class... classes) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        this.config.addJaxbClasses(classSet);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder clearJaxbClasses() {
        this.config.clearJaxbClasses();
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addRemoteInitialContext(InitialContext remoteInitialContext) {
       this.remoteInitialContext = remoteInitialContext; 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addJbossServerUrl(URL serverUrl) {
        this.jbossServerUrl = serverUrl;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addKieSessionQueue(Queue ksessionQueue) {
        this.config.setKsessionQueue(ksessionQueue); 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addTaskServiceQueue(Queue taskServiceQueue) {
        this.config.setTaskQueue(taskServiceQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addResponseQueue(Queue responseQueue) {
        this.config.setResponseQueue(responseQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineBuilder addConnectionFactory(ConnectionFactory connectionFactory) {
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
    public RemoteJmsRuntimeEngineBuilder useSsl(boolean useSsl) {
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
  
    private void checkAndFinalizeConfig() { 
        // check
        if( config.getUserName() == null ) { 
           throw new InsufficientInfoToBuildException("A user name is required to access the JMS queues!"); 
        } 
        if( config.getPassword() == null ) { 
           throw new InsufficientInfoToBuildException("A password is required to access the JMS queues!"); 
        }
        
        // Connection Factory
        if( createOwnFactory ) { 
            ConnectionFactory createdConnectionFactory = null;
           if( hostName == null ) { 
               throw new InsufficientInfoToBuildException("A host name or IP address is required to create a JMS ConnectionFactory!"); 
           }
           if( jmsConnectorPort == null ) { 
               throw new InsufficientInfoToBuildException("A connector port is required to create a JMS ConnectionFactory!"); 
           }
           Map<String, Object> connParams;
           if( config.getUseUssl() ) { 
               connParams = new HashMap<String, Object>(7);  
               connParams.put(TransportConstants.PORT_PROP_NAME, jmsConnectorPort);  
               connParams.put(TransportConstants.HOST_PROP_NAME, hostName);
              
               checkKeyAndTruststoreInfo();

               // SSL
               connParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);  
               connParams.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, keystorePassword); 
               connParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keystoreLocation);
               connParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, truststorePassword); 
               connParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, truststoreLocation);
           } else { 
               // setup
               connParams = new HashMap<String, Object>(3);  
               connParams.put(TransportConstants.PORT_PROP_NAME, jmsConnectorPort);  
               connParams.put(TransportConstants.HOST_PROP_NAME, hostName);
               connParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, false);  
           }
           // create connection factory
           createdConnectionFactory = new HornetQJMSConnectionFactory(false, 
                   new TransportConfiguration(NettyConnectorFactory.class.getName(), connParams));
           this.config.setConnectionFactory(createdConnectionFactory);
        } 
                
        if( jbossServerUrl != null && this.remoteInitialContext == null ) { 
            this.remoteInitialContext = getRemoteJbossInitialContext(jbossServerUrl, this.config.getUserName(), this.config.getPassword());
        }
        
        if( remoteInitialContext != null ) {
            // sets connection factory, if null
            this.config.setRemoteInitialContext(this.remoteInitialContext);
        } else { 
           this.config.checkValidJmsValues();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.kie.services.client.api.builder.RemoteRuntimeEngineFactoryBuilder#build()
     */
    @Override
    public RemoteJmsRuntimeEngineFactory buildFactory() throws InsufficientInfoToBuildException {
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
    
    private void checkKeyAndTruststoreInfo() { 
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