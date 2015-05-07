package org.kie.server.remote.rest.drools;

import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieContainerCommandService;

@Path("/server")
public class CommandResource {

    private KieContainerCommandService delegate;

    public CommandResource(KieContainerCommandService delegate) {
        this.delegate = delegate;
    }

    @POST
    @Path("containers/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response manageContainer(@Context HttpHeaders headers, @PathParam("id") String id, String cmdPayload) {
        return createCorrectVariant(delegate.callContainer(id, cmdPayload), headers);
    } 
        
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response executeCommands(@Context HttpHeaders headers, CommandScript command ) {
        return createCorrectVariant(
                new GenericEntity<ServiceResponsesList>(delegate.executeScript(command)) { }, 
                headers);
    }

}
