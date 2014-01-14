package org.kie.services.client.api.command;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.kie.api.runtime.manager.Context;
import org.kie.services.client.serialization.JaxbSerializationProvider;

/**
 * In order to protect the Remote (Java) API, this class may not be extended nor may its constructor be made public.
 */
public final class RemoteConfiguration {

    public static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    public static final String SESSION_QUEUE_NAME = "jms/queue/KIE.SESSION";
    public static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    public static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";

    // REST or JMS
    private final Type type;

    // General
    private String deploymentId;
    private String jmsQueueUsername;
    private String jmsQueuePassword;
    private Context<?> context;
    private Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();

    // REST
    private ClientRequestFactory requestFactory;

    // JMS
    private ConnectionFactory connectionFactory;
    private Queue ksessionQueue;
    private Queue taskQueue;
    private Queue responseQueue;
    private int qualityOfServiceThresholdMilliSeconds = 5 * 1000; // 5 seconds
    private int jmsSerializationType = JaxbSerializationProvider.JMS_SERIALIZATION_TYPE;

    // ID
    private static final AtomicInteger idGen = new AtomicInteger(0);

    /**
     * Public constructors and setters
     */

    // REST ----------------------------------------------------------------------------------------------------------------------

    private RemoteConfiguration() {
        // no public constructor!
        this.type = Type.CONSTRUCTOR;
    }

    public RemoteConfiguration(String deploymentId, URL url) {
        this.type = Type.REST;
        URL realUrl = initializeRestServicesUrl(deploymentId, url);
        try { 
            this.requestFactory = new ClientRequestFactory(realUrl.toURI());
        } catch (URISyntaxException urise) {
            throw new IllegalArgumentException("URL (" + realUrl.toExternalForm() + ") is incorrectly formatted: " + urise.getMessage(), urise);
        }
    }

    public RemoteConfiguration(String deploymentId, URL url, String username, String password) {
        this(deploymentId, url, username, password, 5);
    }

    public RemoteConfiguration(String deploymentId, URL url, String username, String password, int timeout) {
        this.type = Type.REST;
        URL serverPlusRestUrl = initializeRestServicesUrl(deploymentId, url);
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The user name may not be empty or null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("The password may not be null.");
        }
        this.requestFactory = createAuthenticatingRequestFactory(serverPlusRestUrl, username, password, timeout);

    }

    /**
     * Initializes the URL that will be used for the request factory
     * 
     * @param deploymentId Deployment ID
     * @param url URL of the server instance
     * @return An URL that can be used for the REST request factory 
     */
    private URL initializeRestServicesUrl(String deploymentId, URL url) {
        if (deploymentId == null || deploymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("The deployment id may not be empty or null.");
        }
        if (url == null ) {
            throw new IllegalArgumentException("The url may not be empty or null.");
        }
        try { 
            url.toURI();
        } catch( URISyntaxException urise) { 
            throw new IllegalArgumentException("URL (" + url.toExternalForm() + ") is incorrectly formatted: " + urise.getMessage(), urise);
        }
        this.deploymentId = deploymentId;
       
        String urlString = url.toExternalForm();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        urlString += "rest";
        
        URL serverPlusRestUrl;
        try {
            serverPlusRestUrl = new URL(urlString);
        } catch (MalformedURLException murle) {
            throw new IllegalArgumentException("URL (" + url.toExternalForm() + ") is incorrectly formatted: " + murle.getMessage(), murle);
        }

        return serverPlusRestUrl;
    }

    /**
     * Creates an request factory that autenticates using the given username and password
     * 
     * @param url 
     * @param username
     * @param password
     * @param timeout
     * 
     * @return A request factory that can be used to send (authenticating) requests to REST services
     */
    public static ClientRequestFactory createAuthenticatingRequestFactory(URL url, String username, String password, int timeout) {
        BasicHttpContext localContext = new BasicHttpContext();
        HttpClient preemptiveAuthClient = createPreemptiveAuthHttpClient(username, password, timeout, localContext);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(preemptiveAuthClient, localContext);
        try {
            return new ClientRequestFactory(clientExecutor, url.toURI());
        } catch (URISyntaxException urise) {
            throw new IllegalArgumentException("URL (" + url.toExternalForm() + ") is not formatted correctly.", urise);
        }
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

    /**
     * This class is used in order to effect preemptive authentication in the REST request factory.
     */
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
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
        }
    }

    // JMS ----------------------------------------------------------------------------------------------------------------------

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue,
            Queue responseQueue) {
        checkValidValues(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);
        this.deploymentId = deploymentId;
        this.connectionFactory = connectionFactory;
        this.ksessionQueue = ksessionQueue;
        this.taskQueue = taskQueue;
        this.responseQueue = responseQueue;

        this.type = Type.JMS;
    }

    private static void checkValidValues(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue,
            Queue taskQueue, Queue responseQueue) {
        if (deploymentId == null || deploymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("The deployment id may not be empty or null.");
        }
        if (connectionFactory == null) {
            throw new IllegalArgumentException("The connection factory argument may not be null.");
        }
        if (ksessionQueue == null) {
            throw new IllegalArgumentException("The ksession queue argument may not be null.");
        }
        if (taskQueue == null) {
            throw new IllegalArgumentException("The task queue argument may not be null.");
        }
        if (responseQueue == null) {
            throw new IllegalArgumentException("The response queue argument may not be null.");
        }
    }

    public RemoteConfiguration(String deploymentId, ConnectionFactory connectionFactory, Queue ksessionQueue, Queue taskQueue,
            Queue responseQueue, String username, String password) {
        this(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);

        setAndCheckUserNameAndPassword(username, password);
    }

    public RemoteConfiguration(String deploymentId, InitialContext context) {
        this(deploymentId, context, null, null);
    }

    public RemoteConfiguration(String deploymentId, InitialContext context, String username, String password) {
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
        } catch (NamingException ne) {
            throw new RemoteRuntimeException("Unable to retrieve object for " + prop, ne);
        }
        checkValidValues(deploymentId, connectionFactory, ksessionQueue, taskQueue, responseQueue);

        this.type = Type.JMS;

        setAndCheckUserNameAndPassword(username, password);
    }

    private void setAndCheckUserNameAndPassword(String username, String password) { 
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The user name may not be empty or null.");
        }
        this.jmsQueueUsername = username;
        if (password == null) {
            throw new IllegalArgumentException("The password may not be null.");
        }
        this.jmsQueuePassword = password;
    }
    
    // Setters -------------------------------------------------------------------------------------------------------------------

    public void setQualityOfServiceThresholdMilliSeconds(int qualityOfServiceThresholdMilliSeconds) {
        if (qualityOfServiceThresholdMilliSeconds < 0) {
            throw new IllegalArgumentException("The QOS threshold limit must be positve.");
        }
        this.qualityOfServiceThresholdMilliSeconds = qualityOfServiceThresholdMilliSeconds;
    }

    public void setSerializationType(int serializationType) {
        this.jmsSerializationType = serializationType;
    }

    public void setContext(Context<?> context) {
        this.context = context;
    }

    /**
     * (Package-scoped) Getters
     */

    String getDeploymentId() {
        assert deploymentId != null : "deploymentId value should not be null!";
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

    private enum Type {
        REST, JMS, CONSTRUCTOR;
    }

    // ----
    // REST
    // ----

    ClientRequestFactory getRequestFactory() {
        return this.requestFactory;
    }

    // ----
    // JMS
    // ----

    String getJmsQueueUsername() {
        assert jmsQueueUsername != null : "username value should not be null!";
        return jmsQueueUsername;
    }

    String getJmsQueuePassword() {
        // helpful during tests: assert password != null : "password value should not be null!";
        return jmsQueuePassword;
    }

    Context<?> getContext() {
        assert context != null : "context value should not be null!";
        return context;
    }

    ConnectionFactory getConnectionFactory() {
        assert connectionFactory != null : "connectionFactory value should not be null!";
        return connectionFactory;
    }

    Queue getKsessionQueue() {
        assert ksessionQueue != null : "ksessionQueue value should not be null!";
        return ksessionQueue;
    }

    Queue getTaskQueue() {
        assert taskQueue != null : "taskQueue value should not be null!";
        return taskQueue;
    }

    Queue getResponseQueue() {
        assert responseQueue != null : "responseQueue value should not be null!";
        return responseQueue;
    }

    int getQualityOfServiceThresholdMilliSeconds() {
        return qualityOfServiceThresholdMilliSeconds;
    }
    
    public void addJaxbClasses(Set<Class<?>> extraJaxbClassList) { 
        this.extraJaxbClasses.addAll(extraJaxbClassList);
    }
    
    Set<Class<?>> getExtraJaxbClasses() { 
        return this.extraJaxbClasses;
    }
    
    JaxbSerializationProvider getJaxbSerializationProvider() { 
       JaxbSerializationProvider provider = new JaxbSerializationProvider(extraJaxbClasses); 
       return provider;
    }

}
