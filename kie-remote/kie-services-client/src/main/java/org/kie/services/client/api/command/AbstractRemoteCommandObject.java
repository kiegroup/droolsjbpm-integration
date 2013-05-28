package org.kie.services.client.api.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.services.client.api.RemoteRestSessionFactory.AuthenticationType;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;

public abstract class AbstractRemoteCommandObject {

	protected String baseUrl;
    protected String url;
    protected String deploymentId;
    protected AuthenticationType authenticationType;
    protected String username;
    protected String password;

    public AbstractRemoteCommandObject(String baseUrl, String url, String deploymentId, AuthenticationType authenticationType, String username, String password) {
    	this.baseUrl = baseUrl;
        this.url = url;
        this.deploymentId = deploymentId;
        this.authenticationType = authenticationType;
        this.username = username;
        this.password = password;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T execute(Command<T> command) {
    	ClientRequest restRequest = null;
        DefaultHttpClient client = new DefaultHttpClient();
    	if (AuthenticationType.BASIC == authenticationType) {
	        client.getCredentialsProvider().setCredentials(
	            new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
	            new UsernamePasswordCredentials(username, password));
    	} else if (AuthenticationType.FORM_BASED == authenticationType) {
    	    // first challenge authentication to be enforced - return logon form
    	    // currently pointing to a non existing resource
    	    HttpPost mainMethod = new HttpPost(baseUrl);    	    
            List<NameValuePair> nameValuePairsEmpty = new ArrayList<NameValuePair>(1);            
    	    try {
                mainMethod.setEntity(new UrlEncodedFormEntity(nameValuePairsEmpty));
        	    HttpResponse response = client.execute(mainMethod);
        	    
        	    if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Error invoking REST " + url + " " + response.getStatusLine());
                }
            
                EntityUtils.consume(response.getEntity());
   
                // next authenticate the request
        		HttpPost authMethod = new HttpPost(baseUrl + "/j_security_check");
        		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        		nameValuePairs.add(new BasicNameValuePair("j_username", username));
        		nameValuePairs.add(new BasicNameValuePair("j_password", password));
            
				authMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				// make sure that request will be redirected request has been authenticated and authorized
				client.setRedirectStrategy(new DefaultRedirectStrategy() {                
			        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
			            boolean isRedirect=false;
			            try {
			                isRedirect = super.isRedirected(request, response, context);
			            } catch (ProtocolException e) {
			                e.printStackTrace();
			            }
			            if (!isRedirect) {
			                int responseCode = response.getStatusLine().getStatusCode();
			                if (responseCode == 301 || responseCode == 302) {
			                    return true;
			                }
			            }
			            return isRedirect;
			        }	
			    });
				HttpResponse authresponse = client.execute(authMethod);
				List<Integer> invalidCodes = Arrays.asList(new Integer[]{500, 401, 403});
				if (invalidCodes.contains(response.getStatusLine().getStatusCode())) {
	                throw new RuntimeException("Error invoking REST " + url + " " + response.getStatusLine());
	            }
				EntityUtils.consume(authresponse.getEntity());
				
			} catch (UnsupportedEncodingException e) {
        		throw new RuntimeException("Could not initialize form-based authentication", e);
			} catch (ClientProtocolException e) {
        		throw new RuntimeException("Could not initialize form-based authentication", e);
			} catch (IOException e) {
        		throw new RuntimeException("Could not initialize form-based authentication", e);
			}
    	}
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
        restRequest = new ClientRequest(url, executor);
        try {
            restRequest.body(MediaType.APPLICATION_XML, new JaxbCommandsRequest(deploymentId, command));
            ClientResponse<Object> response = restRequest.post(Object.class);
            if (response.getResponseStatus() == Status.OK) {
            	JaxbCommandsResponse commandResponse = response.getEntity(JaxbCommandsResponse.class);
            	List<JaxbCommandResponse<?>> responses = commandResponse.getResponses();
            	if (responses.size() == 0) {
            		return null;
            	} else if (responses.size() == 1) {
            		JaxbCommandResponse<?> responseObject = responses.get(0);
            		if (responseObject instanceof JaxbExceptionResponse) {
            			JaxbExceptionResponse exceptionResponse = 
        					(JaxbExceptionResponse) responseObject;
            			String causeMessage = exceptionResponse.getCauseMessage();
            			throw new RuntimeException(exceptionResponse.getMessage()
        					+ (causeMessage == null ? "" : " Caused by: " + causeMessage));
            		} else {
            			return (T) responseObject.getResult();
            		}
            	} else {
            		throw new RuntimeException("Unexpected number of results: " + responses.size());
            	}
            } else {
                // TODO error handling
                throw new RuntimeException("Error invoking REST " + url + " " 
            		+ response.getResponseStatus() + " " + response.getEntity(String.class));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error invoking REST " + url + " " + e.getMessage(), e);
        }
    	
    	
    }
    
    public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Task.class.getSimpleName() + " implementation.");
    }

    public void writeExternal(ObjectOutput arg0) throws IOException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Task.class.getSimpleName() + " implementation.");
    }

}
