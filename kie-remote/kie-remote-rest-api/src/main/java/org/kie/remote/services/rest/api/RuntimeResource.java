package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/runtime/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
public interface RuntimeResource {

    @GET
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/")
    public Response process_defId( String processId );

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response process_defId_start( String processId );

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/startform")
    public Response process_defId_startform( String processId );

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}")
    public Response process_instance_procInstId( Long procInstId );

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/abort")
    public Response process_instance_procInstId_abort( Long procInstId );

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/signal")
    public Response process_instance_procInstId_signal( Long procInstId );

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}/variable/{varName: [\\w\\.-]+}")
    public Response process_instance_procInstId_variable_varName( Long procInstId, String varName );

    @POST
    @Path("/signal")
    public Response signal();

    @GET
    @Path("/workitem/{workItemId: [0-9-]+}")
    public Response workitem_workItemId( Long workItemId );

    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response worktiem_workItemId_oper( Long workItemId, String operation );

    @POST
    @Path("/withvars/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response withvars_process_processDefId_start( String processId );

    @GET
    @Path("/withvars/process/instance/{procInstId: [0-9]+}")
    public Response withvars_process_instance_procInstId( Long procInstId );
    
    @POST
    @Path("/withvars/process/instance/{procInstId: [0-9]+}/signal")
    public Response withvars_process_instance_procInstid_signal( Long procInstId );

}