package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/runtime/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@Api(
        value="/runtime/{depUnitId}",
        description="Runtime (Process) operations")
public interface RuntimeResource {

    @GET
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/")
    @ApiOperation(
            value = "Get process definition information",
            produces="application/xml, application/json",
            position=0)
    public Response getProcessDefinitionInfo(@PathParam("processDefId") String processId);

    @POST
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response startProcessInstance(@PathParam("processDefId") String processId);
        
    @GET
    @Path("/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/startform")
    public Response getProcessInstanceStartForm(@PathParam("processDefId") String processId);

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}")
    public Response getProcessInstance(@PathParam("procInstId") Long procInstId);

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/abort")
    public Response abortProcessInstance(@PathParam("procInstId") Long procInstId);

    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/signal")
    public Response signalProcessInstance(@PathParam("procInstId") Long procInstId);

    @GET
    @Path("/process/instance/{procInstId: [0-9]+}/variable/{varName: [\\w\\.-]+}")
    public Response getProcessInstanceVariableByProcInstIdByVarName(@PathParam("procInstId") Long procInstId, @PathParam("varName") String varName); 
    
    @POST
    @Path("/signal")
    public Response signalProcessInstances();

    @GET
    @Path("/workitem/{workItemId: [0-9-]+}")
    public Response getWorkItem(@PathParam("workItemId") Long workItemId);

    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public Response doWorkItemOperation(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation);

    /**
     * WithVars methods
     */
    
    @POST
    @Path("/withvars/process/{processDefId: [_a-zA-Z0-9-:\\.]+}/start")
    public Response withVarsStartProcessInstance( @PathParam("processDefId")  String processId );

    @GET
    @Path("/withvars/process/instance/{procInstId: [0-9]+}")
    public Response withVarsGetProcessInstance( @PathParam("procInstId") Long procInstId );
    
    @POST
    @Path("/withvars/process/instance/{procInstId: [0-9]+}/signal")
    public Response withVarsSignalProcessInstance(@PathParam("procInstId") Long procInstId);

}