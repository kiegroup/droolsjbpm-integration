package org.kie.services.client.api;

import java.lang.reflect.Field;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.runtime.manager.Context;
import org.kie.services.client.api.command.RemoteRuntimeException;

public class RemoteConfiguration {

    public static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    public static final String SESSION_QUEUE_NAME = "jms/queue/KIE.SESSION";
    public static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    public static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";
    
    // REST or JMS
    private final Type type;

    // General
    private String deploymentId;
    private String username;
    private String password;
    private Context<?> context;

    // REST
    private String url;
    private AuthenticationType authenticationType;

    // JMS
    private ConnectionFactory connectionFactory;
    private Queue ksessionQueue;
    private Queue taskQueue;
    private Queue responseQueue;
    private int qualityOfServiceThresholdMilliSeconds = 5 * 1000; // 5 seconds
    private int serializationType = 1;

    public RemoteConfiguration(String deploymentId, String url) {
        this.deploymentId = deploymentId;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "rest";
        this.url = url;
        this.type = Type.REST;
    }

    public RemoteConfiguration(String deploymentId, String url, AuthenticationType authenticationType, String username,
            String password) {
        this(deploymentId, url);

        this.authenticationType = authenticationType;
        this.username = username;
        this.password = password;
    }

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) {
        this.deploymentId = deploymentId;
        this.connectionFactory = connectionFactory;
        this.ksessionQueue = ksessionQueue;
        this.taskQueue = taskQueue;
        this.responseQueue = responseQueue;

        this.type = Type.JMS;
    }

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue,
            String username, String password) {
        this(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);

        this.username = username;
        this.password = password;
    }

    public RemoteConfiguration(String deploymentId, InitialContext context) { 
        this.deploymentId = deploymentId;
        String prop = CONNECTION_FACTORY_NAME;
        try { 
            this.connectionFactory = (ConnectionFactory) context.lookup(prop);
            prop = SESSION_QUEUE_NAME;
            this.ksessionQueue = (Queue) context.lookup(prop);
            prop = TASK_QUEUE_NAME;
            this.taskQueue = (Queue) context.lookup(prop);
            prop = RESPONSE_QUEUE_NAME;
            this.responseQueue = (Queue) context.lookup(prop);
        } catch( NamingException ne ) { 
            throw new RemoteRuntimeException("Unable to retrieve object for " +  prop, ne);
        }

        this.type = Type.JMS;
    }
    
    public RemoteConfiguration(String deploymentId, InitialContext context, String username, String password) { 
        this(deploymentId, context);
        
        this.username = username;
        this.password = password;
    } 
    
    public String getUrl() {
        notNullAssert();
        return url;
    }

    public String getDeploymentId() {
        notNullAssert();
        return deploymentId;
    }

    public AuthenticationType getAuthenticationType() {
        notNullAssert();
        return authenticationType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        notNullAssert();
        return password;
    }

    public Context<?> getContext() {
        return context;
    }

    public void setContext(Context<?> context) {
        this.context = context;
    }

    public ConnectionFactory getConnectionFactory() {
        notNullAssert();
        return connectionFactory;
    }

    public Queue getKsessionQueue() {
        notNullAssert();
        return ksessionQueue;
    }

    public Queue getTaskQueue() {
        notNullAssert();
        return taskQueue;
    }

    public Queue getResponseQueue() {
        notNullAssert();
        return responseQueue;
    }

    public void setQualityOfServiceThresholdMilliSeconds(int qualityOfServiceThresholdMilliSeconds) {
        this.qualityOfServiceThresholdMilliSeconds = qualityOfServiceThresholdMilliSeconds;
    }

    public int getQualityOfServiceThresholdMilliSeconds() {
        return qualityOfServiceThresholdMilliSeconds;
    }

    public int getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(int serializationType) {
        this.serializationType = serializationType;
    }

    public boolean isJms() {
        return (this.type == Type.JMS);
    }

    public boolean isRest() {
        return (this.type == Type.REST);
    }

    public enum AuthenticationType {
        BASIC, FORM_BASED
    }

    private enum Type {
        REST, JMS;
    }

    private void notNullAssert() {
        String fieldName = (new Throwable().getStackTrace())[1].getMethodName();
        fieldName = fieldName.replace("get", "");
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        Object fieldVal = null;
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            fieldVal = field.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        assert fieldVal != null : fieldName + " is null!";
    }
}
