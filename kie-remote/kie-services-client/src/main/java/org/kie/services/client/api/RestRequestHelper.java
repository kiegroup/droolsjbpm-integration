package org.kie.services.client.api;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.kie.services.client.api.command.RemoteConfiguration;

/**
 * This class is meant to help users help interact with the (kie-wb or business-central) REST api by creating 
 * either {@link ClientRequest} (REST request) instances or {@link ClientRequestFactory} instances to 
 * create {@link ClientRequest} instances. 
 */
public class RestRequestHelper extends RemoteConfiguration {

    private ClientRequestFactory requestFactory;
    private static int DEFAULT_TIMEOUT = 5;

    /**
     * Creates a {@link RestRequestHelper} instance.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username.
     * @param timeout The timeout used for REST requests.
     */
    public RestRequestHelper(String serverPortUrl, String username, String password, int timeout) {
        super();
        this.requestFactory = createAuthenticatingRequestFactory(serverPortUrl + "rest/", username, password, timeout);
    }

    /**
     * Creates a {@link RestRequestHelper} instance. A default timeout of 5 seconds is used for REST requests.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username.
     * 
     */
    public RestRequestHelper(String serverPortUrl, String username, String password) {
        super();
        if (!serverPortUrl.endsWith("/")) {
            serverPortUrl = serverPortUrl + "/";
        }
        this.requestFactory = createAuthenticatingRequestFactory(serverPortUrl + "rest/", username, password, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a REST request for the given REST operation URL. 
     * </p>
     * For example, if you wanted to create a REST request for the following URL:<ul>
     * <li><code>http://my.server.com:8080/rest/runtime/test-deploy/process/org.jbpm:HR:1.0/start</code></li>
     * </ul>
     * Then you could call this method as follows: <code>
     * restRequestHelperInstance.createRestRequest( "runtime/test-deploy/process/org.jbpm:HR:1.0/start" );
     * </code>
     * @param restOperationUrl The URL of the REST operation, exculding the server/port and "/rest" base. 
     * @return A {@link ClientRequest} instance that authenticates based on the username/password arguments
     * given to the constructor of this {@link RestRequestHelper} instance.
     */
    public ClientRequest createRestRequest(String restOperationUrl) {
        if (restOperationUrl.startsWith("/")) {
            restOperationUrl = restOperationUrl.substring(1);
        }
        return this.requestFactory.createRelativeRequest(restOperationUrl);
    }

    /**
     * This method creates a {@link ClientRequestFactory} instance that can be used to create {@link ClientRequest} instances
     * that will authenticate against a kie-wb or business-central server using the given username and password. 
     * </p>
     * The {@link ClientRequestFactory} instance can then be used like this to create {@link ClientRequest} REST request instances:
     * <pre>
     * {@link ClientRequestFactory} requestFactory = {@link RestRequestHelper}.createRestRequest( "http://my.server:8080/rest", "user", "pass", 10);
     * {@link ClientRequest} restRequest = requestFactory.createRelativeRequest( "task/2/start" );
     * ClientResponse restResponse =  restRequest.post();
     * // do something with the response
     * </pre>
     * 
     * @param restBaseUrl The base URL of the rest server, which will have this format: "http://server[:port]/rest". 
     * @param username The username to use when authenticating.
     * @param password The password to use when authenticating.
     * @param timeout The timeout to use for the REST request.
     * @return A {@link ClientRequestFactory} in order to create REST request ( {@link ClientRequest} ) instances
     * to interact with the REST api. 
     */
    public static ClientRequestFactory createRestRequest(String restBaseUrl, String username, String password, int timeout) {
        return createAuthenticatingRequestFactory(restBaseUrl, username, password, timeout);
    }

    /**
     * See {@link RestRequestHelper#createRestRequest(String, String, String, int)}. This method uses a default timeout of 
     * 5 seconds, whereas the referred method allows users to pass the value for the timeout. 
     * 
     * @param restBaseUrl The base URL of the rest server, which will have this format: "http://server[:port]/rest". 
     * @param username The username to use when authenticating.
     * @param password The password to use when authenticating.
     * @return A {@link ClientRequestFactory} in order to create REST request ( {@link ClientRequest} ) instances
     * to interact with the REST api. 
     */
    public static ClientRequestFactory createRestRequest(String restBaseUrl, String username, String password) {
        return createAuthenticatingRequestFactory(restBaseUrl, username, password, DEFAULT_TIMEOUT);
    }

}
