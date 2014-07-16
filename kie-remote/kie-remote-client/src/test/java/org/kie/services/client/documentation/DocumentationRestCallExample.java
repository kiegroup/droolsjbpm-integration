package org.kie.services.client.documentation;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.kie.api.task.model.Task;

public class DocumentationRestCallExample {

    /*
     * Example 13.8
     * A GET call that returns a task details to a locally running application in Java with the direct tasks/TASKID request
    */ 
    
    private static final String USER_AGENT_ID = "org.kie.remote.client.docs";
    private static final int REST_REQUEST_TIMEOUT_IN_SECONDS = 1;
  
    private static final AtomicInteger userAgentIdGen = new AtomicInteger(0);
    
    /**
     * Retrieves a task instance from the remote REST API
     * 
     * @param serverUrl The URL of the machine on which BPMS is running
     * @param taskId The task id of the task that should be retrieved via the remote REST API
     * @return A Task instance, with information about the task specified in the taskId parameter
     * @throws Exception if something goes wrong
     */
    public Task getTaskInstanceInfo(URL serverUrl, long taskId, String user, String password) throws Exception {
        // serverUrl should look something like this: "http://192.178.168.1:8080/"
       
        String slashIfNeeded = "/";
        if( serverUrl.toExternalForm().endsWith("/") ) {
           slashIfNeeded = ""; 
        }
        URL restServicesBaseUrl = new URL(serverUrl.toExternalForm() + slashIfNeeded + "business-central/rest/");
        ClientRequestFactory requestFactory 
            = createAuthenticatingRequestFactory(restServicesBaseUrl, user, password, REST_REQUEST_TIMEOUT_IN_SECONDS);
        ClientRequest restRequest = requestFactory.createRelativeRequest("task/" + taskId);
        
        ClientResponse<?> responseObj = restRequest.get();
        JaxbTask jaxbTask = responseObj.getEntity(JaxbTask.class);
        return (Task) jaxbTask;
    }


    /**
     * Creates an request factory that authenticates using the given username and password
     * 
     * @param restServicesBaseUrl A URL that references the REST services base, it should end with a slash. For example: 
     * "http://bpms.company.com:8080/business-central/rest/" or "http://127.0.0.1:8080/business-central/rest/"
     * @param username The name of the user with which to login to BPMS to complete the REST call
     * @param password The password of the specified user, used to login to BPMS to complete the REST call
     * @param timeout The amount to time (in seconds) to wait for the call to complete. 
     * @return A request factory that can be used to send (authenticating) requests to REST services
     */
    public static ClientRequestFactory createAuthenticatingRequestFactory(URL restServicesBaseUrl, String username, String password, int timeout) {
        BasicHttpContext localContext = new BasicHttpContext();
        HttpClient preemptiveAuthClient = createPreemptiveAuthHttpClient(username, password, timeout, localContext);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(preemptiveAuthClient, localContext);
        try {
            return new ClientRequestFactory(clientExecutor, restServicesBaseUrl.toURI());
        } catch (URISyntaxException urise) {
            throw new IllegalArgumentException("URL (" + restServicesBaseUrl.toExternalForm() + ") is not formatted correctly.", urise);
        }
    }
    
    /**
     * This method is used in order to create a component of the {@link ClientRequestFactory} created by the
     * {@link #createAuthenticatingRequestFactory(URL, String, String, int)} method.
     * 
     * @param userName The user name with which to login
     * @param password The password needed to login 
     * @param timeout The timeout for the rest call
     * @param localContext This object is used to track the execution state of the HTTP call
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
        String userAgent = USER_AGENT_ID + " (" + userAgentIdGen.incrementAndGet() + " / " + hostname + ")";
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
                    authState.update(authScheme, creds);
                }
            }
        }
    }

}