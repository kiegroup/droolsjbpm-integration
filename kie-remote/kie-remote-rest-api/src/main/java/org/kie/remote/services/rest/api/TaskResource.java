package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/task")
@Api(value="/task", description="Human Task operations")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface TaskResource {

    @GET
    @Path("/query")
    @Deprecated
    @ApiOperation(value = "Query tasks", 
        notes = "Returns a list of TaskSummary instances")
    public Response query();

    @GET
    @Path("/{taskId: [0-9-]+}")
    @ApiOperation(value = "Get a specific task", 
        notes = "Returns a Task")
    @ApiResponses(value = { 
            @ApiResponse(code = 404, message = "Task can not be found") })
    public Response getTask( @PathParam("taskId") long taskId );

    @POST
    @Path("/{taskId: [0-9-]+}/{oper: [a-zA-Z]+}")
    @ApiOperation(value = "Get a specific task", 
        notes = "Returns a Task")
    @ApiResponses(value = { 
            @ApiResponse(code = 404, message = "Task can not be found") })
    public Response doTaskOperation( 
            @PathParam("taskId") long taskId, 
            @ApiParam(value = "Operation names", required = true, 
            allowableValues = "activate, claim, claimnextavailable, complete, delegate, exit, fail, forward, nominate, release, resume, skip, start, stop, suspend",
            allowMultiple = false) 
            @PathParam("oper") String operation );

    @GET
    @Path("/{taskId: [0-9-]+}/content")
    @ApiOperation(value = "Get the content associated with a specific task", 
    notes = "Returns Task content")
    @ApiResponses(value = { 
            @ApiResponse(code = 404, message = "Task can not be found") })
    public Response getTaskContentByTaskId( @PathParam("taskId") long taskId );

    @GET
    @Path("/{taskId: [0-9-]+}/showTaskForm")
    public Response getTaskFormByTaskId( @PathParam("taskId") long taskId );

    @GET
    @Path("/content/{contentId: [0-9-]+}")
    @ApiOperation(value = "Get a specific task content", 
        notes = "Returns task content")
    @ApiResponses(value = { 
            @ApiResponse(code = 404, message = "Content can not be found") })
    public Response getTaskContentByContentId( @PathParam("contentId") long contentId );

    @POST
    @Path("/history/bam/clear")
    public Response clearTaskBamHistory();

}