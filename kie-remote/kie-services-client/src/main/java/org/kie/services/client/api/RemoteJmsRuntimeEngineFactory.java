package org.kie.services.client.api;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteJmsRuntimeEngineFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration config;
  
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, URL hostUrl, String userName, String password) { 
        InitialContext context = getRemoteJbossInitialContext(hostUrl, userName, password);
        this.config = new RemoteConfiguration(deploymentId, context);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context) { 
        this.config = new RemoteConfiguration(deploymentId, context);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password, int qualityOfServiceThresholdSeconds) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityOfServiceThresholdSeconds*1000);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password, int qualityofServiceThresholdSeconds) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityofServiceThresholdSeconds*1000);
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
            throw new RuntimeException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
    
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    public void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses ) { 
        this.config.addJaxbClasses(new HashSet<Class<?>>(extraJaxbClasses));
    }
}