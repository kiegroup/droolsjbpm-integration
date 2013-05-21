package org.kie.services.client.api.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskSummaryListResponse;

public abstract class AbstractRemoteCommandObject {

    protected String url;
    protected String deploymentId;

    public AbstractRemoteCommandObject(String url, String deploymentId) { 
        this.url = url;
        this.deploymentId = deploymentId;
    }
    
    public <T> T execute(Command<T> command) {
        ClientRequest restRequest = new ClientRequest(url);
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
                throw new RuntimeException("REST request error code " + response.getResponseStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute REST request: " + e.getMessage(), e);
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
