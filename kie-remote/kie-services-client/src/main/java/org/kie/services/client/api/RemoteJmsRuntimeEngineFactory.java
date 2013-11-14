package org.kie.services.client.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class RemoteJmsRuntimeEngineFactory implements RemoteRuntimeEngineFactory {

    private RemoteConfiguration config;
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);
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
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password, int qualityOfServiceThresholdMillisecs) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityOfServiceThresholdMillisecs);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password, int qualityofServiceThresholdMillisecs) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityofServiceThresholdMillisecs);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue, String username, String password, int qualityOfServiceThresholdMillisecs, int serializationType) {
        this.config = new RemoteConfiguration(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityOfServiceThresholdMillisecs);
        this.config.setSerializationType(serializationType);
    }
    
    public RemoteJmsRuntimeEngineFactory(String deploymentId, InitialContext context, String username, String password, int qualityofServiceThresholdMillisecs, int serialization) { 
        this.config = new RemoteConfiguration(deploymentId, context, username, password);
        this.config.setQualityOfServiceThresholdMilliSeconds(qualityofServiceThresholdMillisecs);
        this.config.setSerializationType(serialization);
    }
    
    public RuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }
    
    public void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses ) { 
        this.config.addJaxbClasses(new HashSet<Class<?>>(extraJaxbClasses));
    }
}