package org.kie.services.client.api.command;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.drools.core.command.CommandService;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.api.command.Command;
import org.kie.internal.command.Context;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbSerializationProvider;

public class RemoteCommandService implements CommandService {

	private String url;
	private String deploymentId;
	
	public RemoteCommandService(String url, String deploymentId) {
		this.url = url;
		this.deploymentId = deploymentId;
	}
	
	public <T> T execute(Command<T> command) {
        ClientRequest restRequest = new ClientRequest(url);
        try {
            restRequest.body(MediaType.APPLICATION_XML, 
        		JaxbSerializationProvider.convertJaxbObjectToString(
    				new JaxbCommandMessage<T>(deploymentId, 1, command)));
            ClientResponse<Object> response = restRequest.post(Object.class);
            if (response.getResponseStatus() == Status.OK) {
            	// TODO result
                return (T) response.getEntity();
            } else {
            	// TODO error handling
                throw new RuntimeException("REST request error code " + response.getResponseStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute REST request: " + e.getMessage(), e);
        }
    }

	public Context getContext() {
		return null;
	}
    
}
