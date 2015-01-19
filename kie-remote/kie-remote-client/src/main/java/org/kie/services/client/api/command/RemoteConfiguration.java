package org.kie.services.client.api.command;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.services.client.serialization.JaxbSerializationProvider;

/**
 * In order to protect the Remote (Java) API, this class may not be extended nor may its constructor be made public.
 */
public final class RemoteConfiguration {

    public static final String SSL_CONNECTION_FACTORY_NAME = "jms/SslRemoteConnectionFactory";
    public static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    public static final String SESSION_QUEUE_NAME = "jms/queue/KIE.SESSION";
    public static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    public static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";

    public static final int DEFAULT_TIMEOUT_IN_SECS = 5;
    private long timeoutInMillisecs = DEFAULT_TIMEOUT_IN_SECS * 1000;; // in seconds
    
    // REST or JMS
    private final Type type;

    // General
    private String deploymentId;
    private Long processInstanceId;
    
    private String userName;
    private String password;
    private URL serverBaseUrl;

    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
    private JaxbSerializationProvider jaxbSerializationProvider;

    // JMS
    private boolean useSsl = false;
    private boolean doNotUseSsl = false;
    private ConnectionFactory connectionFactory;
    private Queue ksessionQueue;
    private Queue taskQueue;
    private Queue responseQueue;
    private int jmsSerializationType = JaxbSerializationProvider.JMS_SERIALIZATION_TYPE;

    /**
     * Public constructors and setters
     */

    public RemoteConfiguration(Type type) { 
        this.type = type;
    }
   
    public void dispose() { 
       if( jaxbSerializationProvider != null ) { 
           jaxbSerializationProvider.dispose();
           jaxbSerializationProvider = null;
       }
       if( extraJaxbClasses != null ) { 
           extraJaxbClasses.clear();
           extraJaxbClasses = null;
       }
       if( connectionFactory != null ) { 
          connectionFactory = null; 
       }
       if( ksessionQueue != null ) { 
           ksessionQueue = null; 
       }
       if( taskQueue != null ) { 
           taskQueue = null; 
       }
       if( responseQueue != null ) { 
           responseQueue = null; 
       }
    }
    
    public void initializeJaxbSerializationProvider() {
        if( extraJaxbClasses != null ) { 
            jaxbSerializationProvider = ClientJaxbSerializationProvider.newInstance(extraJaxbClasses);
        } else {  
            jaxbSerializationProvider = ClientJaxbSerializationProvider.newInstance();
        }
    }

    // REST ----------------------------------------------------------------------------------------------------------------------

    public RemoteConfiguration(String deploymentId, URL url, String username, String password) {
        this(deploymentId, url, username, password, DEFAULT_TIMEOUT_IN_SECS);
    }

    public RemoteConfiguration(String deploymentId, URL url, String username, String password, int timeoutInSecs) {
        this.type = Type.REST;
        this.deploymentId = deploymentId;
        
        this.userName = username;
        this.password = password;
        this.timeoutInMillisecs = timeoutInSecs * 1000;
    }

    /**
     * Initializes the URL that will be used for REST service access
     * 
     * @param deploymentId Deployment ID
     * @param url URL of the server instance
     * @return An URL that can be used to access the REST services
     */
    URL initializeRestServicesUrl(URL url) {
       return initializeServicesUrl(url, "rest");
    }
    
    /**
     * Initializes the URL that will be used for web service access
     * 
     * @param deploymentId Deployment ID
     * @param url URL of the server instance
     * @return An URL that can be used for the web services
     */
    URL initializeWebServicesUrl(URL url) {
       return initializeServicesUrl(url, "ws");
    }
    
    /**
     * Initializes the URL that will be used for web service access
     * 
     * @param deploymentId Deployment ID
     * @param url URL of the server instance
     * @return An URL that can be used for the web services
     */
    private URL initializeServicesUrl(URL url, String servicePrefix) {
        if (url == null) {
            throw new IllegalArgumentException("The url may not be empty or null.");
        }
        try {
            url.toURI();
        } catch (URISyntaxException urise) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + urise.getMessage(), urise);
        }

        String urlString = url.toExternalForm();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        urlString += servicePrefix;

        URL serverPlusServicePrefixUrl;
        try {
            serverPlusServicePrefixUrl = new URL(urlString);
        } catch (MalformedURLException murle) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + murle.getMessage(), murle);
        }

        return serverPlusServicePrefixUrl;
    }
    
    KieRemoteHttpRequest createHttpRequest() { 
        return KieRemoteHttpRequest.newRequest(serverBaseUrl, userName, password).timeout(timeoutInMillisecs);
    }
    
    // JMS ----------------------------------------------------------------------------------------------------------------------

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) {
        this.deploymentId = deploymentId;
        this.type = Type.JMS;
        setQueuesAndConnectionFactory(connectionFactory, ksessionQueue, taskQueue, responseQueue);
    }

    public void setQueuesAndConnectionFactory(ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) { 
        this.connectionFactory = connectionFactory;
        this.ksessionQueue = ksessionQueue;
        this.taskQueue = taskQueue;
        this.responseQueue = responseQueue;
        checkValidValues(this.connectionFactory, this.ksessionQueue, this.taskQueue, this.responseQueue);
    }
    
    public void checkValidJmsValues() {
        checkValidValues(connectionFactory, ksessionQueue, taskQueue, responseQueue);
    }
    
    private static void checkValidValues(ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue, Queue responseQueue) 
            throws InsufficientInfoToBuildException {
        if (connectionFactory == null) {
            throw new InsufficientInfoToBuildException("The connection factory argument may not be null.");
        }
        if (ksessionQueue == null && taskQueue == null) {
            throw new InsufficientInfoToBuildException("At least a ksession queue or task queue is required.");
        }
        if (responseQueue == null) {
            throw new InsufficientInfoToBuildException("The response queue argument may not be null.");
        }
    }

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue,
            Queue responseQueue, String username, String password) {
        this(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);
        setAndCheckUserNameAndPassword(username, password);
    }

    public RemoteConfiguration(String deploymentId, InitialContext context, String username, String password) {
        this.deploymentId = deploymentId;
        this.type = Type.JMS;
        setAndCheckUserNameAndPassword(username, password);
        setRemoteInitialContext(context);
    }
    
    public void setRemoteInitialContext(InitialContext context) { 
        String prop = CONNECTION_FACTORY_NAME;
        try {
            if( this.connectionFactory == null ) { 
                this.connectionFactory = (ConnectionFactory) context.lookup(prop);
            }
            prop = SESSION_QUEUE_NAME;
            this.ksessionQueue = (Queue) context.lookup(prop);
            prop = TASK_QUEUE_NAME;
            this.taskQueue = (Queue) context.lookup(prop);
            prop = RESPONSE_QUEUE_NAME;
            this.responseQueue = (Queue) context.lookup(prop);
        } catch (NamingException ne) {
            throw new RemoteCommunicationException("Unable to retrieve object for " + prop, ne);
        }
        checkValidValues(connectionFactory, ksessionQueue, taskQueue, responseQueue);
    }

    private void setAndCheckUserNameAndPassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The user name may not be empty or null.");
        }
        this.userName = username;
        if (password == null) {
            throw new IllegalArgumentException("The password may not be null.");
        }
        this.password = password;
    }

    /**
     * (Package-scoped) Getters
     */

    public String getDeploymentId() {
        return deploymentId;
    }

    int getSerializationType() {
        return jmsSerializationType;
    }

    boolean isJms() {
        assert type != null : "type is null!";
        return (this.type == Type.JMS);
    }

    boolean isRest() {
        assert type != null : "type is null!";
        return (this.type == Type.REST);
    }

    public enum Type {
        REST, JMS, WS, CONSTRUCTOR;
    }

    public URL getServerBaseUrl() { 
        return serverBaseUrl;
    }
    
    public String getUserName() {
        // assert userName != null : "username value should not be null!"; // disabled for tests
        return userName;
    }

    public String getPassword() {
        // assert password != null : "password value should not be null!"; // disabled for tests
        return password;
    }

    ConnectionFactory getConnectionFactory() {
        assert connectionFactory != null : "connectionFactory value should not be null!";
        return connectionFactory;
    }

    Queue getKsessionQueue() {
        // assert ksessionQueue != null : "ksessionQueue value should not be null!"; // disabled for testing
        return ksessionQueue;
    }

    Queue getTaskQueue() {
        // assert taskQueue != null : "taskQueue value should not be null!"; // disabled for testing
        return taskQueue;
    }

    Queue getResponseQueue() {
        assert responseQueue != null : "responseQueue value should not be null!";
        return responseQueue;
    }

    public boolean addJaxbClasses(Set<Class<?>> extraJaxbClassList) {
        return this.extraJaxbClasses.addAll(extraJaxbClassList);
    }

    public void clearJaxbClasses() {
        this.extraJaxbClasses.clear();
    }

    public Set<Class<?>> getExtraJaxbClasses() {
        return this.extraJaxbClasses;
    }
    
    JaxbSerializationProvider getJaxbSerializationProvider() { 
        return jaxbSerializationProvider;
    }

    public Type getType() { 
        return this.type;
    }

    public long getTimeout() {
        return timeoutInMillisecs;
    }
    
    public boolean getUseUssl() {
        return useSsl;
    } 
    
    public boolean getDoNotUseSsl() { 
        return doNotUseSsl;
    } 
    
    Long getProcessInstanceId() {
        return processInstanceId;
    }

    // Setters -------------------------------------------------------------------------------------------------------------------

    public void setTimeout(long timeout) {
        this.timeoutInMillisecs = timeout*1000;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setServerBaseRestUrl(URL url) {
        URL checkedModifiedUrl = initializeRestServicesUrl(url);
        this.serverBaseUrl = checkedModifiedUrl;
    }

    public void setServerBaseWsUrl(URL url) {
        URL checkedModifiedUrl = initializeWebServicesUrl(url);
        this.serverBaseUrl = checkedModifiedUrl;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setExtraJaxbClasses(Set<Class<?>> extraJaxbClasses) {
        this.extraJaxbClasses = extraJaxbClasses;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setKsessionQueue(Queue ksessionQueue) {
        this.ksessionQueue = ksessionQueue;
    }

    public void setTaskQueue(Queue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void setResponseQueue(Queue responseQueue) {
        this.responseQueue = responseQueue;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }
   
    public void setDoNotUseSssl(boolean doNotUseSsl) {
        this.doNotUseSsl = doNotUseSsl;
    }
   
    // Clone --- 
   
    private RemoteConfiguration(RemoteConfiguration config) { 
       this.connectionFactory = config.connectionFactory;
       
       this.deploymentId = config.deploymentId;
       this.extraJaxbClasses = config.extraJaxbClasses;
       this.jmsSerializationType = config.jmsSerializationType;
       this.ksessionQueue = config.ksessionQueue;
       this.password = config.password;
       this.processInstanceId = config.processInstanceId;
       this.responseQueue = config.responseQueue;
       this.serverBaseUrl = config.serverBaseUrl;
       this.taskQueue = config.taskQueue;
       this.timeoutInMillisecs = config.timeoutInMillisecs;
       this.type = config.type;
       this.userName = config.userName;
       this.useSsl = config.useSsl;
       this.doNotUseSsl = config.doNotUseSsl;
    }
    
    public RemoteConfiguration clone() {
       return new RemoteConfiguration(this);
    }

}
