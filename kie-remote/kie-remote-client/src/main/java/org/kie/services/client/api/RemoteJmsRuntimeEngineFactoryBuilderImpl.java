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
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineFactoryBuilder;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteConfiguration.Type;
/**
 * The fluent API builder 
 * 
 *
 */
public class RemoteJmsRuntimeEngineFactoryBuilderImpl implements RemoteJmsRuntimeEngineFactoryBuilder {

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
    
    RemoteJmsRuntimeEngineFactoryBuilderImpl() {
        this.config = new RemoteConfiguration(Type.JMS);
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addDeploymentId(String deploymentId) {
        this.config.setDeploymentId(deploymentId);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addProcessInstanceId(long processInstanceId) {
        this.config.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addUserName(String userName) {
        this.config.setUserName(userName);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addPassword(String password) {
        this.config.setPassword(password);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addTimeout(int timeoutInSeconds) {
        this.config.setTimeout((long) timeoutInSeconds);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addExtraJaxbClasses(Class... classes) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for( Class clazz : classes ) { 
            classSet.add(clazz);
        }
        this.config.addJaxbClasses(classSet);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder clearJaxbClasses() {
        this.config.clearJaxbClasses();
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addRemoteInitialContext(InitialContext remoteInitialContext) {
       this.remoteInitialContext = remoteInitialContext; 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addJbossServerUrl(URL serverUrl) {
        this.jbossServerUrl = serverUrl;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addKieSessionQueue(Queue ksessionQueue) {
        this.config.setKsessionQueue(ksessionQueue); 
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addTaskServiceQueue(Queue taskServiceQueue) {
        this.config.setTaskQueue(taskServiceQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addResponseQueue(Queue responseQueue) {
        this.config.setResponseQueue(responseQueue);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder addConnectionFactory(ConnectionFactory connectionFactory) {
        this.config.setConnectionFactory(connectionFactory);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addHostName(String hostNameOrIp) {
        this.createOwnFactory = true;
        this.hostName = hostNameOrIp;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addJmsConnectorPort(int port) {
        this.createOwnFactory = true;
        this.jmsConnectorPort = port;
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilder useSsl(boolean useSsl) {
        this.createOwnFactory = useSsl;
        this.config.setUseSsl(useSsl);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
        this.useSsl(true);
        return this;
    }

    @Override
    public RemoteJmsRuntimeEngineFactoryBuilderImpl addTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
        this.useSsl(true);
        return this;
    }

    public RemoteJmsRuntimeEngineFactoryBuilderImpl useKeystoreAsTruststore() { 
        this.useKeystoreAsTruststore = true;
        this.useSsl(true);
        return this;
    }
    
    @Override
    public RemoteJmsRuntimeEngineFactory build() throws InsufficientInfoToBuildException {
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
        
        // return new instance
        return new RemoteJmsRuntimeEngineFactory(config.clone());
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
    
    public static RemoteJmsRuntimeEngineFactoryBuilderImpl newBuilder() { 
        return new RemoteJmsRuntimeEngineFactoryBuilderImpl();
    }

}