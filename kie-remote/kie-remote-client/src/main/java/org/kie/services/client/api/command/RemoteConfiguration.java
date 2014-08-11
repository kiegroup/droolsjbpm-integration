package org.kie.services.client.api.command;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;
import org.kie.services.client.api.rest.KieRemoteHttpRequest;
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

    public static final int DEFAULT_TIMEOUT = 5;
    private long timeout = DEFAULT_TIMEOUT; // in seconds
    
    // REST or JMS
    private final Type type;

    // General
    private String deploymentId;
    private Long processInstanceId;
    private String userName;
    private String password;
    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();

    // REST
    private KieRemoteHttpRequest httpRequest;

    // JMS
    private boolean useSsl = false;
    private ConnectionFactory connectionFactory;
    private Queue ksessionQueue;
    private Queue taskQueue;
    private Queue responseQueue;
    private int jmsSerializationType = JaxbSerializationProvider.JMS_SERIALIZATION_TYPE;

    private static final AtomicInteger idGen = new AtomicInteger(0);
    
    /**
     * Public constructors and setters
     */

    @SuppressWarnings("unused")
    private RemoteConfiguration() {
        // no public constructor!
        this.type = Type.CONSTRUCTOR;
    }

    public RemoteConfiguration(Type type) { 
        this.type = type;
    }
    
    // REST ----------------------------------------------------------------------------------------------------------------------

    public RemoteConfiguration(String deploymentId, URL url, String username, String password) {
        this(deploymentId, url, username, password, DEFAULT_TIMEOUT);
    }

    public RemoteConfiguration(String deploymentId, URL url, String username, String password, int timeout) {
        this.type = Type.REST;
        this.deploymentId = deploymentId;
        this.timeout = timeout;
        createHttpRequest(url, username, password);
    }

    public void createHttpRequest(URL url, String username, String password) { 
      createHttpRequest(url, username, password, (int) timeout)  ;
    }
    
    public void createHttpRequest(URL url, String username, String password, int timeout) { 
        URL serverPlusRestUrl = initializeRestServicesUrl(url);
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The user name may not be empty or null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("The password may not be null.");
        }
        this.httpRequest = new KieRemoteHttpRequest(serverPlusRestUrl, username, password, timeout);
    }
    
    /**
     * Initializes the URL that will be used for the request factory
     * 
     * @param deploymentId Deployment ID
     * @param url URL of the server instance
     * @return An URL that can be used for the REST request factory
     */
    private URL initializeRestServicesUrl(URL url) {
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
        urlString += "rest";

        URL serverPlusRestUrl;
        try {
            serverPlusRestUrl = new URL(urlString);
        } catch (MalformedURLException murle) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + murle.getMessage(), murle);
        }

        return serverPlusRestUrl;
    }

    public KieRemoteHttpRequest getHttpRequest() { 
        return this.httpRequest;
    }
    
    /**
     * This method is used in order to create the authenticating REST client factory.
     * 
     * @param userName
     * @param password
     * @param timeout
     * @param localContext
     * 
     * @return A {@link DefaultHttpClient} instance that will authenticate using the given username and password.
     */
    /**
    private static DefaultHttpClient createPreemptiveAuthHttpClient(String userName, String password, int timeout,
            BasicHttpContext localContext) {
        BasicHttpParams params = new BasicHttpParams();
        int timeoutMilliSeconds = timeout * 1000;
        HttpConnectionParams.setConnectionTimeout(params, timeoutMilliSeconds);
        HttpConnectionParams.setSoTimeout(params, timeoutMilliSeconds);
        DefaultHttpClient client = new DefaultHttpClient(params);

        if (userName != null && !"".equals(userName)) {
            client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(userName, password));
            // Generate BASIC scheme object and stick it to the local execution context
            BasicScheme basicAuth = new BasicScheme();

            String contextId = UUID.randomUUID().toString();
            localContext.setAttribute(contextId, basicAuth);

            // Add as the first request interceptor
            client.addRequestInterceptor(new PreemptiveAuth(contextId), 0);
        }

        String hostname = "localhost";

        try {
            hostname = Inet6Address.getLocalHost().toString();
        } catch (Exception e) {
            // do nothing
        }

        // set the following user agent with each request
        String userAgent = "org.kie.services.client (" + idGen.incrementAndGet() + " / " + hostname + ")";
        HttpProtocolParams.setUserAgent(client.getParams(), userAgent);

        return client;
    }

     * This class is used in order to effect preemptive authentication in the REST request factory.
     
    static class PreemptiveAuth implements HttpRequestInterceptor {

        private final String contextId;

        public PreemptiveAuth(String contextId) {
            this.contextId = contextId;
        }

        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {

            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(contextId);
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.update(authScheme, creds);
                }
            }
        }
    }
    */

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

    String getDeploymentId() {
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
        REST, JMS, CONSTRUCTOR;
    }

    // ----
    // JMS
    // ----

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

    public void addJaxbClasses(Set<Class<?>> extraJaxbClassList) {
        this.extraJaxbClasses.addAll(extraJaxbClassList);
    }

    public void clearJaxbClasses() {
        this.extraJaxbClasses.clear();
    }

    Set<Class<?>> getExtraJaxbClasses() {
        return this.extraJaxbClasses;
    }

    JaxbSerializationProvider getJaxbSerializationProvider() {
        JaxbSerializationProvider provider = new JaxbSerializationProvider(extraJaxbClasses);
        return provider;
    }

    public Type getType() { 
        return this.type;
    }

    public long getTimeout() {
        return timeout;
    }
    
    public boolean getUseUssl() {
        return useSsl;
    } 
    
    Long getProcessInstanceId() {
        return processInstanceId;
    }

    // Setters -------------------------------------------------------------------------------------------------------------------

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
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
       this.httpRequest = config.httpRequest;
       this.taskQueue = config.taskQueue;
       this.timeout = config.timeout;
       this.type = config.type;
       this.userName = config.userName;
       this.useSsl = config.useSsl;
    }
    
    public RemoteConfiguration clone() {
       return new RemoteConfiguration(this);
    }

    public RemoteSessionCommandService newRemoteSessionCommandService() { 
        return new RemoteSessionCommandService(this);
    }

    public RemoteTaskCommandExecutor newRemoteTaskCommandExecutor() { 
        return new RemoteTaskCommandExecutor(this);
    }
}
