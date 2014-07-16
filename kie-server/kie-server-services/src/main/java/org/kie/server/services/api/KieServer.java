package org.kie.server.services.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;

@Path("/server")
public interface KieServer {
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInfo();
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response execute( CommandScript command );
    
    @GET
    @Path("containers")
    @Produces(MediaType.APPLICATION_XML)
    public Response listContainers();
    
    @GET
    @Path("containers/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getContainerInfo( @PathParam("id") String id );
    
    @PUT
    @Path("containers/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response createContainer( @PathParam("id") String id, KieContainerResource container );
    
    @DELETE
    @Path("containers/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response disposeContainer( @PathParam("id") String id );
    
    @POST
    @Path("containers/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response execute( @PathParam("id") String id, String cmdPayload );
    
    @GET
    @Path("containers/{id}/scanner")
    @Produces(MediaType.APPLICATION_XML)
    public Response getScannerInfo( @PathParam("id") String id );
    
    @POST
    @Path("containers/{id}/scanner")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response updateScanner( @PathParam("id") String id, KieScannerResource resource );
    
} 
