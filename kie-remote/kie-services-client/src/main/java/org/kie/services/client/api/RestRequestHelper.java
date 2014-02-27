package org.kie.services.client.api;

import static org.kie.services.client.api.command.RemoteConfiguration.*;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;

/**
 * This class is meant to help users help interact with the (kie-wb or business-central) REST api by creating 
 * either {@link ClientRequest} (REST request) instances or {@link ClientRequestFactory} instances to 
 * create {@link ClientRequest} instances. 
 */
public class RestRequestHelper {

    private ClientRequestFactory requestFactory;

    
    // Just for building/config, not for use
    private URL serverPlusRestUrl = null;
    private MediaType type = null;
    private String username = null;
    private String password = null;
    private int timeout = DEFAULT_TIMEOUT;
    private boolean formBasedAuth = false;

    /**
     * Helper methods
     */
    
    private URL addRestToPath(URL origUrl) { 
        StringBuilder urlString = new StringBuilder(origUrl.toExternalForm());
        if (!urlString.toString().endsWith("/")) {
            urlString.append("/");
        }
        urlString.append("rest/");
        serverPlusRestUrl = convertStringToUrl(urlString.toString());
        return serverPlusRestUrl;
    }

    private static URL convertStringToUrl(String urlString) { 
        URL realUrl;
        try { 
            realUrl = new URL(urlString);
        } catch (MalformedURLException murle) {
            throw new IllegalArgumentException("URL (" + urlString + ") is incorrectly formatted: " + murle.getMessage(), murle);
        }
        return realUrl;
    }

    private RestRequestHelper() { 
        
    }
   
    /**
     * Creates a {@link RestRequestHelper} instance.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username.
     * @param timeout The timeout used for REST requests.
     */
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password, int timeout, MediaType mediaType, boolean formBasedAuth) {
        RestRequestHelper inst = new RestRequestHelper();
        URL serverPlusRestUrl = inst.addRestToPath(serverPortUrl);
        if( formBasedAuth ) { 
            inst.requestFactory = createFormBasedAuthenticatingRequestFactory(serverPlusRestUrl, username, password, timeout);
        } else { 
            inst.requestFactory = createAuthenticatingRequestFactory(serverPlusRestUrl, username, password, timeout);
        }
        inst.type = mediaType;
        inst.username = username;
        inst.password = password;
        return inst;
    }
    
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password, int timeout, MediaType mediaType) {
       return newInstance(serverPortUrl, username, password, timeout, mediaType, false);
    }
    
    /**
     * Creates a {@link RestRequestHelper} instance.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username.
     * @param timeout The timeout used for REST requests.
     */
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password, int timeout) {
        return newInstance(serverPortUrl, username, password, timeout, null);
    }

    /**
     * Creates a {@link RestRequestHelper} instance. A default timeout of 5 seconds is used for REST requests.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username.
     * 
     */
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password) {
        return newInstance(serverPortUrl, username, password, DEFAULT_TIMEOUT, null);
    }
    
    public RestRequestHelper setMediaType(MediaType type) { 
        this.type = type;
        return this;
    }
    
    public MediaType getMediaType() { 
        return this.type;
    }

    public RestRequestHelper setTimeout(int timeout) { 
        this.timeout = timeout;
        this.requestFactory = createAuthenticatingRequestFactory(serverPlusRestUrl, username, password, timeout);
        return this;
    }
    
    public int getTimeout() { 
        return this.timeout;
    }
    
    public RestRequestHelper setFormBasedAuth(boolean useFormBasedAuth) { 
        this.formBasedAuth = useFormBasedAuth;
        return this;
    }
    
    public boolean getFormBasedAuth() { 
        return this.formBasedAuth;
    }

    /**
     * Creates a REST request for the given REST operation URL. 
     * </p>
     * For example, if you wanted to create a REST request for the following URL:<ul>
     * <li><code>http://my.server.com:8080/rest/runtime/test-deploy/process/org.jbpm:HR:1.0/start</code></li>
     * </ul>
     * Then you could call this method as follows: <code>
     * restRequestHelperInstance.createRequest( "runtime/test-deploy/process/org.jbpm:HR:1.0/start" );
     * </code>
     * @param restOperationUrl The URL of the REST operation, exculding the server/port and "/rest" base. 
     * @return A {@link ClientRequest} instance that authenticates based on the username/password arguments
     * given to the constructor of this {@link RestRequestHelper} instance.
     */
    public ClientRequest createRequest(String restOperationUrl) {
        if (restOperationUrl.startsWith("/")) {
            restOperationUrl = restOperationUrl.substring(1);
        }
        ClientRequest request =  requestFactory.createRelativeRequest(restOperationUrl);
        if( type != null ) { 
            request.accept(type);
        }
        return request;
    }

    /**
     * This method creates a {@link ClientRequestFactory} instance that can be used to create {@link ClientRequest} instances
     * that will authenticate against a kie-wb or business-central server using the given username and password. 
     * </p>
     * The {@link ClientRequestFactory} instance can then be used like this to create {@link ClientRequest} REST request instances:
     * <pre>
     * {@link ClientRequestFactory} requestFactory = {@link RestRequestHelper}.createRequest( "http://my.server:8080/rest", "user", "pass", 10);
     * {@link ClientRequest} restRequest = requestFactory.createRelativeRequest( "task/2/start" );
     * ClientResponse restResponse =  restRequest.post();
     * // do something with the response
     * </pre>
     * 
     * @param restBaseUrl The base URL of the rest server, which should have this format: "http://server[:port]/rest". 
     * @param username The username to use when authenticating.
     * @param password The password to use when authenticating.
     * @param timeout The timeout to use for the REST request.
     * @return A {@link ClientRequestFactory} in order to create REST request ( {@link ClientRequest} ) instances
     * to interact with the REST api. 
     */
    public static ClientRequestFactory createRequestFactory(URL restBaseUrl, String username, String password, int timeout) {
        return createAuthenticatingRequestFactory(restBaseUrl, username, password, timeout);
    }

    /**
     * See {@link RestRequestHelper#createRequestFactory(String, String, String, int)}. This method uses a default timeout of 
     * 5 seconds, whereas the referred method allows users to pass the value for the timeout. 
     * 
     * @param restBaseUrl The base URL of the rest server, which should have this format: "http://server[:port]/rest". 
     * @param username The username to use when authenticating.
     * @param password The password to use when authenticating.
     * @return A {@link ClientRequestFactory} in order to create REST request ( {@link ClientRequest} ) instances
     * to interact with the REST api. 
     */
    public static ClientRequestFactory createRequestFactory(URL restBaseUrl, String username, String password) {
        return createAuthenticatingRequestFactory(restBaseUrl, username, password, DEFAULT_TIMEOUT);
    }
    
    public static ClientRequestFactory createRequestFactory(URL restBaseUrl, String username, String password, boolean useFormBasedAuth) {
        if( useFormBasedAuth ) { 
            return createFormBasedAuthenticatingRequestFactory(restBaseUrl, username, password, DEFAULT_TIMEOUT);
        } else { 
            return createAuthenticatingRequestFactory(restBaseUrl, username, password, DEFAULT_TIMEOUT);
        } 
    }
    
    public static ClientRequestFactory createRequestFactory(URL restBaseUrl, String username, String password, int timeout, boolean useFormBasedAuth) {
        if( useFormBasedAuth ) { 
            return createFormBasedAuthenticatingRequestFactory(restBaseUrl, username, password, timeout);
        } else { 
            return createAuthenticatingRequestFactory(restBaseUrl, username, password, timeout);
        } 
    }
}
