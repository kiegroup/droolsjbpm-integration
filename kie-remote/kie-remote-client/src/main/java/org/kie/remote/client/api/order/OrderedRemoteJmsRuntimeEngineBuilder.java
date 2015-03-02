package org.kie.remote.client.api.order;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.order.OrderedRemoteRestRuntimeEngineBuilder.RemoteRestRuntimeEngineBuilder1;

/**
 * username -> password -> hostname -> connector port
 *   -> useSsl
 *   -> disableTaskSecurity
 *   -> connection factory -> session queue -> task queue -> response queue 
 *   -> initial context 
 */
public interface OrderedRemoteJmsRuntimeEngineBuilder {

    /**
     * Adds the user name used. If no other user name is specified, the user id
     * specified is used for all purposes.
     * 
     * @param userName The user name
     * @return The builder instance
     */
    RemoteJmsRuntimeEngineBuilder1 addUserName(String userName);

    public static interface RemoteJmsRuntimeEngineBuilder1 {

        /**
         * Adds the password used. If no other password is specified, the password 
         * specified is used for all purposes.
         * 
         * @param userName The password
         * @return The builder instance
         */
        RemoteJmsRuntimeEngineBuilder2 addPassword(String password);
    } 
    
    public static interface RemoteJmsRuntimeEngineBuilder2 {

        /**
         * Add the host name of the server on which Console or BPMS is running. 
         * @param string The host name (or ip-address)
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilder3 addHostName(String string);
    }

    public static interface RemoteJmsRuntimeEngineBuilder3 {

        /**
         * Add the JMS connector port being used on the server on which Console or BPMS is running.
         * @param port The port
         * @return The current instance of this builder
         */
         RemoteJmsRuntimeEngineBuilder4 addJmsConnectorPort(int port);
    }
    
    public static interface RemoteJmsRuntimeEngineBuilder4 {
       
        RemoteJmsRuntimeEngineBuilderSsl1 useSsl();
        
        RemoteJmsRuntimeEngineBuilder5 disableTaskSecurity();
    }
   
    public static interface RemoteJmsRuntimeEngineBuilder5 {
        
        /**
         * Add a {@link ConnectionFactory} instance that can be used to create (JMS) {@link Connection}s with the remote Console or 
         * BPMS instance. 
         * 
         * @param connectionFactory a {@link ConnectionFactory} instance
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderJms1 addConnectionFactory(ConnectionFactory connectionFactory);
       
        /**
         * Add a remote {@link InitialContext} instance to the configuration. This
         * {@link InitialContext} instance is then used to retrieve the
         * JMS {@link Queue} instances 
         * so that the 
         * @param remoteInitialContext
         * @return
         */
        RemoteJmsRuntimeEngineBuilderOpt addRemoteInitialContext(InitialContext remoteInitialContext); 

        /**
         * Use the given hostname to look up and retrieve a Remote {@link InitialContext} instance. The
         * information in the remote {@link InitialContext} instance will be used to retrieve
         * the {@link Queue} and {@link ConnectionFactory} instances. 
         * @param hostanem The hostname (or ip-address) of the Jboss Server instance on which Console or BPMS is running.
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderOpt addJbossServerHostName(String hostname);
    }

    public static interface RemoteJmsRuntimeEngineBuilderJms1 {

        /**
         * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
         * instance given should allow the client to send {@link KieSession} related command requests. 
         * 
         * @param ksessionQueue a {@link Queue} instance
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilder2 addKieSessionQueue(Queue ksessionQueue);
    }

    public static interface RemoteJmsRuntimeEngineBuilderJms2 {
        /**
         * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
         * instance given should allow the client to send {@link TaskService} related command requests. 
         * 
         * @param ksessionQueue a {@link Queue} instance
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderJms3 addTaskServiceQueue(Queue taskServiceQueue);
    }

    public static interface RemoteJmsRuntimeEngineBuilderJms3 {
        /**
         * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
         * instance given should allow the client to send {@link TaskService} related command requests. 
         * 
         * @param ksessionQueue a {@link Queue} instance
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderOpt addResponseQueue(Queue responseQueue);
    } 

    public static interface RemoteJmsRuntimeEngineBuilderSsl1  {
       
        /**
         * Add the keystore password: the keystore is a storage facility for cryptographic keys and certificates. These keys
         * and certificates are used when communicating over a SSL-encrypted connection. The client-side keystore stores the
         * credentials of the client needed to identify the client to the server. 
         * @param string The password associated with the keystore
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderSsl2 addKeystorePassword(String string);
    }
    
    public static interface RemoteJmsRuntimeEngineBuilderSsl2 {
        
        /**
         * Add the keystore file location: the keystore is a storage facility for cryptographic keys and certificates. These keys
         * and certificates are used when communicating over a SSL-encrypted connection. The client-side keystore stores the
         * credentials of the client needed to identify the client to the server. 
         * @param string The path of the keystore file, relative or absolute
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderSsl3 addKeystoreLocation(String string);
    }
    
    public static interface RemoteJmsRuntimeEngineBuilderSsl3 {
        
        /**
         * Add the truststore password: the trustore is a storage facility for cryptographic keys and certificates. These keys
         * and certificates are used when communicating over a SSL-encrypted connection. The client-side truststore stores the
         * credentials of the server needed to verify the server's identity.
         * @param string The password associated with the truststore
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderSsl4 addTruststorePassword(String string);
        
        /**
         * Use this option if the keystore and truststore are both located in the same file. In this case, only add the keystore
         * location (and password) and then use this option to configure the client to use the file being used for the keystore 
         * also as the truststore. 
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderOpt useKeystoreAsTruststore();      
    }
   
    public static interface RemoteJmsRuntimeEngineBuilderSsl4 {
        
        /**
         * Add the truststore file location: the trustore is a storage facility for cryptographic keys and certificates. These keys
         * and certificates are used when communicating over a SSL-encrypted connection. The client-side truststore stores the
         * credentials of the server needed to verify the server's identity.
         * @param string The path of the truststore file, relative or absolute
         * @return The current instance of this builder
         */
        RemoteJmsRuntimeEngineBuilderOpt addTruststoreLocation(String string);
    }
    
    public static interface RemoteJmsRuntimeEngineBuilderOpt {

        RemoteJmsRuntimeEngineBuilderOpt setConnectionFactoryJNDIName(String connFactoryName);
        
        RemoteJmsRuntimeEngineBuilderOpt setSessionQueueJNDIName(String sessionQueueName);
        
        RemoteJmsRuntimeEngineBuilderOpt setTaskQueueJNDIName(String taskQueueName);
        
        RemoteJmsRuntimeEngineBuilderOpt setResponseQueueJNDIName(String responseQueueName);
        
        /**
         * The quality-of-service threshold when sending JMS msgs.
         * @param timeoutInSeconds The timeout in seconds
         * @return The builder instance
         */
        RemoteJmsRuntimeEngineBuilderOpt addTimeout(int timeoutInSeconds);

        /**
         * Adds the deployment id to the configuration.
         * @param deploymentId The deployment id
         * @return The builder instance
         */
        RemoteJmsRuntimeEngineBuilderOpt addDeploymentId(String deploymentId);

        /**
         * Adds the process instance id, which may be necessary when interacting
         * with deployments that employ the {@link RuntimeStrategy#PER_PROCESS_INSTANCE}.
         * @param processInstanceId The process instance id
         * @return The builder instance
         */
        RemoteJmsRuntimeEngineBuilderOpt addProcessInstanceId(long processInstanceId);

        /**
         * When sending non-primitive class instances, it's necessary to add the class instances
         * beforehand to the configuration so that the class instances can be serialized correctly
         * in requests
         * @param classes One or more class instances
         * @return The builder instance
         */
        RemoteJmsRuntimeEngineBuilderOpt addExtraJaxbClasses(Class... classes); 
        
        /**
         * Creates a {@link RuntimeEngine} instance, using the 
         * configuration built up to this point. 
         * </p>
         * 
         * @return The {@link RuntimeEngine} instance
         * @throws @{link InsufficientInfoToBuildException} when insufficient information 
         * is provided to build the {@link RuntimeEngine}
         */
        RuntimeEngine build();
    }

    public static interface OrderedRemoteJmsRuntimeEngineBuilderAll 
        extends OrderedRemoteJmsRuntimeEngineBuilder,
        RemoteJmsRuntimeEngineBuilder1, RemoteJmsRuntimeEngineBuilder2, 
        RemoteJmsRuntimeEngineBuilder3, RemoteJmsRuntimeEngineBuilder4, 
        RemoteJmsRuntimeEngineBuilder5, 
        RemoteJmsRuntimeEngineBuilderJms1, RemoteJmsRuntimeEngineBuilderJms2, 
        RemoteJmsRuntimeEngineBuilderJms3, 
        RemoteJmsRuntimeEngineBuilderSsl1, RemoteJmsRuntimeEngineBuilderSsl2, 
        RemoteJmsRuntimeEngineBuilderSsl3, RemoteJmsRuntimeEngineBuilderSsl4,
        RemoteJmsRuntimeEngineBuilderOpt {
        
    }
}
