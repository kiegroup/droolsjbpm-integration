package org.kie.services.client.api.command;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.JaxbCommandMessage;

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
            restRequest.body(MediaType.APPLICATION_XML, new JaxbCommandMessage<T>(deploymentId, 1, command));
            ClientResponse<Object> response = restRequest.post(Object.class);
            if (response.getResponseStatus() == Status.OK) {
                // TODO result
                return (T) response.getEntity();
            } else if (response.getResponseStatus() == Status.NO_CONTENT) {
                return null;
            } else {
                // TODO error handling
                throw new RuntimeException("REST request error code " + response.getResponseStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute REST request: " + e.getMessage(), e);
        }
    }

}
