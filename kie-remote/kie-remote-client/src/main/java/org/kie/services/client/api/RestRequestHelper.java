package org.kie.services.client.api;

import static org.kie.services.client.api.command.RemoteConfiguration.DEFAULT_TIMEOUT_IN_SECS;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.kie.remote.client.rest.KieRemoteHttpRequest;

/**
 * This class is meant to help users help interact with the (kie-wb or business-central) REST api by creating 
 * either {@link ClientRequest} (REST request) instances or {@link ClientRequestFactory} instances to 
 * create {@link ClientRequest} instances. 
 */
public class RestRequestHelper {

    // Just for building/config, not for use
    private URL serverPlusRestUrl = null;
    private MediaType type = null;
    private String username = null;
    private String password = null;
    private int timeoutInMilliseconds = DEFAULT_TIMEOUT_IN_SECS;

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
     * </p>
     * This method is deprecated because the <code>formBasedAuth</code> parameter is no longer used. 
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username
     * @param timeout The timeout used for REST requests
     * @param mediaType The media type used for REST requests
     * @param formBasedAuth Whether the request should use form based authentication (only recommended for tomcat instances)
     */
    @Deprecated
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password, int timeout, MediaType mediaType, boolean formBasedAuth) {
       return newInstance(serverPortUrl, username, password, timeout, mediaType);
    }
    
    /**
     * Creates a {@link RestRequestHelper} instance.
     * 
     * @param serverPortUrl in the format of "http://server:port/"
     * @param username The username (registered on the kie-wb or business-central server)
     * @param password The password associated with the username
     * @param timeoutInSecs The timeout used for REST requests in seconds
     * @param mediaType The media type used for REST requests
     * @param formBasedAuth Whether the request should use form based authentication (only recommended for tomcat instances)
     */
    public static RestRequestHelper newInstance(URL serverPortUrl, String username, String password, int timeoutInSecs, MediaType mediaType) {
        RestRequestHelper inst = new RestRequestHelper();
        URL serverPlusRestUrl = inst.addRestToPath(serverPortUrl);
        inst.serverPlusRestUrl = serverPlusRestUrl;
        inst.type = mediaType;
        inst.username = username;
        inst.password = password;
        inst.timeoutInMilliseconds = timeoutInSecs * 1000;
        return inst;
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
        return newInstance(serverPortUrl, username, password, DEFAULT_TIMEOUT_IN_SECS, null);
    }
    
    public RestRequestHelper setMediaType(MediaType type) { 
        this.type = type;
        return this;
    }
    
    public RestRequestHelper setTimeout(int timeoutInMillisecs) { 
        this.timeoutInMilliseconds = timeoutInMillisecs;
        return this;
    }
    
    public int getTimeout() { 
        return this.timeoutInMilliseconds;
    }
  
    /**
     * This method no longer does anything.
     * @param useFormBasedAuth
     * @return
     */
    @Deprecated
    public RestRequestHelper setFormBasedAuth(boolean useFormBasedAuth) { 
        return this;
    }
    
    /**
     * This method no longer does anything.
     */
    @Deprecated
    public boolean getFormBasedAuth() { 
        return false;
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
    public KieRemoteHttpRequest createRequest(String restOperationUrl) {
        KieRemoteHttpRequest request =  KieRemoteHttpRequest.newRequest(serverPlusRestUrl).relativeRequest(restOperationUrl);
        request.timeout(timeoutInMilliseconds);
        request.basicAuthorization(username, password);
        if( type != null ) { 
            request.accept(type.toString());
        }
        return request;
    }

}
