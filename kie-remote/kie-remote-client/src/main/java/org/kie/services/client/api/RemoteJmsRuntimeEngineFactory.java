package org.kie.services.client.api;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineFactoryBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;

/**
 * A factory for creating JMS remote API client instances of the {@link RuntimeEngine}.
 */
public class RemoteJmsRuntimeEngineFactory implements RemoteRuntimeEngineFactory {

    protected RemoteConfiguration config;
   
    protected RemoteJmsRuntimeEngineFactory() {
        // private constructor 
    }
    
    RemoteJmsRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }
  
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * @param deploymentId The deployment id
     * @param hostUrl The url of the application (business-central, kie-wb, etc.) running on a JBoss server.
     * @param userName The user name used for both accessing the remote {@link InitialContext} and the JMS queues.
     * @param password The password associated with the user name
     */
    @Deprecated
    public RemoteJmsRuntimeEngineFactory(String deploymentId, URL hostUrl, String userName, String password) { 
        InitialContext context = getRemoteJbossInitialContext(hostUrl, userName, password);
        this.config = new RemoteConfiguration(deploymentId, context, userName, password);
    }
  
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * @param deploymentId The deployment id
     * @param context The the remote {@link InitialContext} containing the JMS objects ({@link ConnectionFactory} and {@link Queue} instances)
     * @param userName The user name used for accessing the JMS queues.
     * @param password The password associated with the user name
     */
    @Deprecated
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
    }

    /**
     * 
     */
    
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * @param deploymentId The deployment id
     * @param context The the remote {@link InitialContext} containing the JMS objects ({@link ConnectionFactory} and {@link Queue} instances)
     * @param userName The user name used for accessing the JMS queues.
     * @param password The password associated with the user name
     * @param qualityofServiceThresholdSeconds The quality-of-service threshold (maximum timeout) in seconds
     */
    @Deprecated
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password, int qualityofServiceThresholdSeconds) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
        this.config.setTimeout(qualityofServiceThresholdSeconds*1000);
    }

    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * @param deploymentId The deployment id
     * @param connectionFactory The {@link ConnectionFactory} instance
     * @param ksessionQueue The {@link Queue} instance for {@link KieSession} related requests
     * @param taskQueue The {@link Queue} instance for {@link TaskService} related requests
     * @param responseQueue The {@link Queue} instance that response messages are sent to
     * @param username The user name for creating a connection with the application instance
     * @param password The password associated with the user name
     */
    @Deprecated
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
    }
   
    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * @param deploymentId The deployment id
     * @param connectionFactory The {@link ConnectionFactory} instance
     * @param ksessionQueue The {@link Queue} instance for {@link KieSession} related requests
     * @param taskQueue The {@link Queue} instance for {@link TaskService} related requests
     * @param responseQueue The {@link Queue} instance that response messages are sent to
     * @param username The user name for creating a connection with the application instance
     * @param password The password associated with the user name
     * @param qualityofServiceThresholdSeconds The quality-of-service threshold (maximum timeout) in seconds
     */
    @Deprecated
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password, int qualityOfServiceThresholdSeconds) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
        this.config.setTimeout(qualityOfServiceThresholdSeconds*1000);
    }
    
    public static InitialContext getRemoteJbossInitialContext(URL url, String user, String password) { 
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        String jbossServerHostName = url.getHost();
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://"+ jbossServerHostName + ":4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, user);
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, password);

        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RemoteCommunicationException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    /**
     * Deprecated in favor of the fluent builder API. See the {@link RemoteJmsRuntimeEngineFactory#newBuilder()} method.
     * 
     * Adds a list of classes that will be used as parameters (and will thus need to be known to the client- and server-side 
     * serialization contexts).
     */
    @Deprecated
    public void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses ) { 
        this.config.addJaxbClasses(new HashSet<Class<?>>(extraJaxbClasses));
    }
    
    public static RemoteJmsRuntimeEngineFactoryBuilder newBuilder()  { 
       return new RemoteJmsRuntimeEngineFactoryBuilderImpl();
    }

}