package org.kie.remote.client.api;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.api.runtime.KieSession;
import org.kie.api.task.TaskService;

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
    
    /**
     * Use the given hostname to look up and retrieve a Remote {@link InitialContext} instance. The
     * information in the remote {@link InitialContext} instance will be used to retrieve
     * the {@link Queue} and {@link ConnectionFactory} instances. 
     * @param hostanem The hostname (or ip-address) of the Jboss Server instance on which Console or BPMS is running.
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addJbossServerHostName(String hostname);
   
    /**
     * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
     * instance given should allow the client to send {@link KieSession} related command requests. 
     * 
     * @param ksessionQueue a {@link Queue} instance
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addKieSessionQueue(Queue ksessionQueue);
   
    /**
     * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
     * instance given should allow the client to send {@link TaskService} related command requests. 
     * 
     * @param ksessionQueue a {@link Queue} instance
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addTaskServiceQueue(Queue taskServiceQueue);
   
    /**
     * Add a {@link Queue} instance that can be used to communicate with the remote Console or BPMS instance. The {@link Queue}
     * instance given should allow the client to send {@link TaskService} related command requests. 
     * 
     * @param ksessionQueue a {@link Queue} instance
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addResponseQueue(Queue responseQueue);
    
    /**
     * Add a {@link ConnectionFactory} instance that can be used to create (JMS) {@link Connection}s with the remote Console or 
     * BPMS instance. 
     * 
     * @param connectionFactory a {@link ConnectionFactory} instance
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addConnectionFactory(ConnectionFactory connectionFactory);
    
    /**
     * Whether or not this client instance should use SSL. This is optional when doing operations that concern the 
     * {@link KieSession}, but required[*] when doing operations that concern the {@link TaskService}. 
     * </p>
     * [*] If you are sure you do <i>not</i> want to use SSL when communicating with Console or BPMS while
     * doing {@link TaskService} related operations, then use the {@link RemoteJmsRuntimeEngineBuilder#disableTaskSecurity()}
     * method to turn off SSL usage. Doing this will expose the password used in JMS {@link Message} instances to
     * a man-in-the-middle attack, since no encryption will be used when sending the message (which will contain the 
     * password). 
     * 
     * @param useSsl a boolean indicating whether or not to use ssl. 
     * @return
     */
    RemoteJmsRuntimeEngineBuilder useSsl(boolean useSsl);
    
    /**
     * See {@link #useSsl(boolean)}. 
     * </p>
     * This method should only be used if you are sure you do <i>not</i> want to use SSL when communicating with 
     * Console or BPMS while doing {@link TaskService} related operations.
     * </p>
     * This method turns off SSL usage. Doing this will expose the user to a man-in-the-middle attack, since no encryption 
     * will be used when sending the message (which will contain the password). 
     * @return An instnace of this builder
     */
    RemoteJmsRuntimeEngineBuilder disableTaskSecurity();
   
    /**
     * Add the host name of the server on which Console or BPMS is running. 
     * @param string The host name (or ip-address)
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addHostName(String string);
    
    /**
     * Add the JMS connector port being used on the server on which Console or BPMS is running.
     * @param port The port
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addJmsConnectorPort(int port);
   
    /**
     * Add the keystore password: the keystore is a storage facility for cryptographic keys and certificates. These keys
     * and certificates are used when communicating over a SSL-encrypted connection. The client-side keystore stores the
     * credentials of the client needed to identify the client to the server. 
     * @param string The password associated with the keystore
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addKeystorePassword(String string);
    
    /**
     * Add the keystore file location: the keystore is a storage facility for cryptographic keys and certificates. These keys
     * and certificates are used when communicating over a SSL-encrypted connection. The client-side keystore stores the
     * credentials of the client needed to identify the client to the server. 
     * @param string The path of the keystore file, relative or absolute
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addKeystoreLocation(String string);
    
    /**
     * Add the truststore password: the trustore is a storage facility for cryptographic keys and certificates. These keys
     * and certificates are used when communicating over a SSL-encrypted connection. The client-side truststore stores the
     * credentials of the server needed to verify the server's identity.
     * @param string The password associated with the truststore
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addTruststorePassword(String string);
    
    /**
     * Add the truststore file location: the trustore is a storage facility for cryptographic keys and certificates. These keys
     * and certificates are used when communicating over a SSL-encrypted connection. The client-side truststore stores the
     * credentials of the server needed to verify the server's identity.
     * @param string The path of the truststore file, relative or absolute
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder addTruststoreLocation(String string);
    
    /**
     * Use this option if the keystore and truststore are both located in the same file. In this case, only add the keystore
     * location (and password) and then use this option to configure the client to use the file being used for the keystore 
     * also as the truststore. 
     * @return The current instance of this builder
     */
    RemoteJmsRuntimeEngineBuilder useKeystoreAsTruststore();
    
    
}