package org.kie.server.services.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

@Path("/server")
@Api(value="/server",description="Kie server api for provisioning and interacting with KieContainers")
public interface KieServer {
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Gets server info", response=ServiceResponse.class)
    public Response getInfo(@Context HttpHeaders headers);
    
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Executes a script (sequence of commands) on the server", response=ServiceResponse.class)
    public Response execute(@Context HttpHeaders headers,
                            @ApiParam(value = "Script to be executed",required=true) CommandScript script );
    
    @GET
    @Path("/containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Lists all existing KieContainers",response=ServiceResponse.class)
    public Response listContainers(@Context HttpHeaders headers);
    
    @GET
    @Path("/containers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Retrieves the Kie Container resource information", response=ServiceResponse.class)
    public Response getContainerInfo( @Context HttpHeaders headers,
                                      @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id );
    
    @PUT
    @Path("/containers/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Creates a new Kie Container", response=ServiceResponse.class)
    public Response createContainer( @Context HttpHeaders headers,
                                     @ApiParam(value = "Id of the Kie Container to create",required=true) @PathParam("id") String id,
                                     @ApiParam(value = "Container configuration",required=true) KieContainerResource container );
    
    @DELETE
    @Path("/containers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Disposes an existing Kie Container", response=ServiceResponse.class)
    public Response disposeContainer( @Context HttpHeaders headers,
                                      @ApiParam(value = "Id of the Kie Container to dispose",required=true) @PathParam("id") String id );
    
    @POST
    @Path("/containers/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Executes a script of commands on the given container", response=ServiceResponse.class)
    public Response execute( @Context HttpHeaders headers,
                             @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id,
                             @ApiParam(value = "Commands to execute",required=true) String cmdPayload );
    
    @GET
    @Path("/containers/{id}/release-id")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Retrieves the configured release ID for the Kie Container", response=ServiceResponse.class)
    public Response getReleaseId( @Context HttpHeaders headers,
                                  @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id);

    @POST
    @Path("/containers/{id}/release-id")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Updates the configured release ID for the Kie Container", response=ServiceResponse.class)
    public Response updateReleaseId( @Context HttpHeaders headers,
                                     @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id,
                                     @ApiParam(value = "New release ID to be used",required=true) ReleaseId releaseId );
    
    @GET
    @Path("/containers/{id}/scanner")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Retrieves the scanner resource attached to the Kie Container, if it is attached", response=ServiceResponse.class)
    public Response getScannerInfo( @Context HttpHeaders headers,
                                    @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id );
    
    @POST
    @Path("/containers/{id}/scanner")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value="Updates the scanner resource attached to the Kie Container, starting/stopping/disposing the scanner", response=ServiceResponse.class)
    public Response updateScanner( @Context HttpHeaders headers,
                                   @ApiParam(value = "Id of the Kie Container",required=true) @PathParam("id") String id,
                                   @ApiParam(value = "Updates the scanner resource attached to the Kie Container. For instance, starts/stops/disposes the scanner.",required=true) KieScannerResource resource );
    
} 
