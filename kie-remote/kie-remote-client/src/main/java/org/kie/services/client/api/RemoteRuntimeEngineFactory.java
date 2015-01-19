package org.kie.services.client.api;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteClientBuilder;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteWebserviceClientBuilder;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.remote.services.ws.command.generated.CommandServiceBasicAuthClient;
import org.kie.services.client.api.command.RemoteConfiguration;

/**
 * This factory class is the start point for building and configuring {@link RuntimeEngine} instances
 * that can interact with the remote API. 
 * </p> 
 * The main use for this class will be to create builder instances (see 
 * {@link #newJmsBuilder()} and * {@link #newRestBuilder()}). These builder instances 
 * can then be used to either directly create a {@link RuntimeEngine} instance that will 
 * act as a client to the remote (REST or JMS) API, or to create an instance of this factory.
 * </p>
 * An instance of this factory can be used to create client {@link RuntimeEngine} instances
 * using the {@link newRuntimeEngine()} method. 
 */
public abstract class RemoteRuntimeEngineFactory extends org.kie.remote.client.api.RemoteRuntimeEngineFactory {

    // The name of this class may not be changed until 7.x for backwards compatibility reasons!
    
    protected RemoteConfiguration config;
    
    /**
     * Create a new {@link RemoteJmsRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteJmsRuntimeEngineBuilder} instance
     */
    public static RemoteJmsRuntimeEngineBuilder newJmsBuilder() { 
       return new org.kie.services.client.api.RemoteJmsRuntimeEngineBuilderImpl(); 
    }
    
    /**
     * Create a new {@link RemoteRestRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteRestRuntimeEngineBuilder} instance
     */
    public static RemoteRestRuntimeEngineBuilder newRestBuilder() { 
       return new org.kie.services.client.api.RemoteRestRuntimeEngineBuilderImpl(); 
    }

    /**
     * Create a new {@link RemoteRestRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteRestRuntimeEngineBuilder} instance
     */
    public static RemoteWebserviceClientBuilder<RemoteWebserviceClientBuilder, CommandServiceBasicAuthClient> newCommandServiceClientBuilder() { 
       return new RemoteCommandWebserviceClientBuilderImpl();
    }
    
    static void checkAndFinalizeConfig(RemoteConfiguration config, RemoteClientBuilder builder ) {
        if( builder instanceof org.kie.services.client.api.RemoteJmsRuntimeEngineBuilderImpl ) { 
            org.kie.services.client.api.RemoteJmsRuntimeEngineBuilderImpl jmsBuilder = (org.kie.services.client.api.RemoteJmsRuntimeEngineBuilderImpl) builder;
            // check
            if( config.getUserName() == null ) { 
                throw new InsufficientInfoToBuildException("A user name is required to access the JMS queues!"); 
            } 
            if( config.getPassword() == null ) { 
                throw new InsufficientInfoToBuildException("A password is required to access the JMS queues!"); 
            }

            // Connection Factory
            if( jmsBuilder.createOwnFactory ) { 
                ConnectionFactory createdConnectionFactory = null;
                if( jmsBuilder.hostName == null ) { 
                    throw new InsufficientInfoToBuildException("A host name or IP address is required to create a JMS ConnectionFactory!"); 
                }
                if( jmsBuilder.jmsConnectorPort == null ) { 
                    throw new InsufficientInfoToBuildException("A connector port is required to create a JMS ConnectionFactory!"); 
                }
                Map<String, Object> connParams;
                if( config.getUseUssl() ) { 
                    connParams = new HashMap<String, Object>(7);  
                    connParams.put(TransportConstants.PORT_PROP_NAME, jmsBuilder.jmsConnectorPort);  
                    connParams.put(TransportConstants.HOST_PROP_NAME, jmsBuilder.hostName);

                    jmsBuilder.checkKeyAndTruststoreInfo();

                    // SSL
                    connParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);  
                    connParams.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, jmsBuilder.keystorePassword); 
                    connParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, jmsBuilder.keystoreLocation);
                    connParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, jmsBuilder.truststorePassword); 
                    connParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, jmsBuilder.truststoreLocation);
                } else { 
                    // setup
                    connParams = new HashMap<String, Object>(3);  
                    connParams.put(TransportConstants.PORT_PROP_NAME, jmsBuilder.jmsConnectorPort);  
                    connParams.put(TransportConstants.HOST_PROP_NAME, jmsBuilder.hostName);
                    connParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, false);  
                }
                // create connection factory
                createdConnectionFactory = new HornetQJMSConnectionFactory(false, 
                        new TransportConfiguration(NettyConnectorFactory.class.getName(), connParams));
                config.setConnectionFactory(createdConnectionFactory);
            } 

            if( jmsBuilder.jbossServerHostName != null && jmsBuilder.remoteInitialContext == null ) { 
                jmsBuilder.remoteInitialContext = getRemoteJbossInitialContext(jmsBuilder.jbossServerHostName, config.getUserName(), config.getPassword());
            }

            if( jmsBuilder.remoteInitialContext != null ) {
                // sets connection factory, if null
                config.setRemoteInitialContext(jmsBuilder.remoteInitialContext);
            } else { 
                config.checkValidJmsValues();
            }
        } else if( builder instanceof RemoteRestRuntimeEngineBuilder ) { 
            if( config.getServerBaseUrl() == null ) { 
                throw new InsufficientInfoToBuildException("A URL is required to access the REST services.");
            }
            if( config.getUserName() == null ) { 
                throw new InsufficientInfoToBuildException("A user name is required to access the REST services.");
            }
            if( config.getPassword() == null ) { 
                throw new InsufficientInfoToBuildException("A password is required to access the REST services.");
            }
        } else if(builder instanceof RemoteWebserviceClientBuilder ) { 
            if( config.getServerBaseUrl() == null ) { 
                throw new InsufficientInfoToBuildException("A URL is required to access the web services.");
            }
            if( config.getUserName() == null ) { 
                throw new InsufficientInfoToBuildException("A user name is required to access the web services.");
            }
            if( config.getPassword() == null ) { 
                throw new InsufficientInfoToBuildException("A password is required to access the web services.");
            }
        }
        if( config.getExtraJaxbClasses() != null && ! config.getExtraJaxbClasses().isEmpty() ) { 
           if( config.getDeploymentId() == null ) { 
                throw new InsufficientInfoToBuildException("A deployment id is required if user-defined class instances are being sent.");
           }
        }
    }
}