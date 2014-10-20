package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/history")
@Api(
        value="/history",
        description="History information operations")
public interface HistoryResource {

    @GET
    @Path("/instances")
    @ApiOperation(
            value="Retrieve a list of all process instance logs across all deployment units",
            notes="The list of process instance logs is sorted first by deployment id, then process (definition) id and lastly by process instance id",
            produces="application/xml, application/json",
            position=0)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getProcessInstanceLogs();

    @GET
    @Path("/instance/{procInstId: [0-9]+}")
    @ApiOperation(
            value="Retrieve a specific process instance log",
            position=1, 
            produces="application/xml, application/json",
            response=JaxbProcessInstanceLog.class)
    public Response getProcessInstanceLog(
            @ApiParam(name="procInstId", value="The process instance id", required=true)
            @PathParam("procInstId") long procInstId );

    @GET
    @Path("/instance/{procInstId: [0-9]+}/{type: [a-zA-Z]+}")
    @ApiOperation(
            value="Retrieve a list of either child process, node or variable instance logs by process instance id",
            notes="The list of process instance logs is sorted first by deployment id, then process (definition) id and lastly by process instance id",
            produces="application/xml, application/json",
            position=2)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getInstanceLogsByProcInstId(
            @ApiParam(name="procInstId", value="The process instance id", required=true)
            @PathParam("procInstId") Long procInstId, 
            @ApiParam(name="type", value="The log type", allowableValues="child, node, variable", required=true)
            @PathParam("type") String logType );

    @GET
    @Path("/instance/{procInstId: [0-9]+}/{type: [a-zA-Z]+}/{logId: [a-zA-Z0-9-:\\._]+}")
    @ApiOperation(
            value="Retrieve a list of either node or variable instance logs by process instance id and log id",
            notes="The list of instance logs is sorted in order by deployment id, process (definition) id, by process instance id, by node or variable id and lastly by instance log id",
            produces="application/xml, application/json",
            position=3)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getInstanceLogsByProcInstIdByLogId(
            @ApiParam(name="procInstId", value="The process instance id", required=true)
            @PathParam("procInstId") Long instId, 
            @ApiParam(name="type", value="The log type", allowableValues="node, variable", required=true)
            @PathParam("type") String logType, 
            @ApiParam(name="logId", value="The log id", required=true)
            @PathParam("logId") String logId);

    @GET
    @Path("/process/{processDefId: [a-zA-Z0-9-:\\._]+}")
    @ApiOperation(
            value="Retrieve a list of process instance logs by process (definition) id",
            notes="The list of instance logs is sorted in order by deployment id, process (definition) id and lastly by process instance id",
            produces="application/xml, application/json",
            position=4)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getProcessInstanceLogsByProcessId(
            @ApiParam(name="processDefid", value="The process (definition) id", required=true)
            @PathParam("processDefId") String processId);

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}")
    @ApiOperation(
            value="Retrieve a list of variable instance logs by variable id",
            notes="The list of instance logs is sorted in order by deployment id, process (definition) id, by process instance id, by variable id and lastly by variable instance id",
            produces="application/xml, application/json",
            position=5)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getVariableInstanceLogsByVariableId(
            @ApiParam(name="varId", value="The variable id, equivalent to the variable name", required=true)
            @PathParam("varId") String variableId );

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}/value/{value: [a-zA-Z0-9-:\\._]+}")
    @ApiOperation(
            value="Retrieve a list of variable instance logs by variable id and value",
            notes="The list of instance logs is sorted in order by deployment id, process (definition) id, by process instance id, by variable id and lastly by variable instance id.",
            produces="application/xml, application/json",
            position=6)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getVariableInstanceLogsByVariableIdByVariableValue(
            @ApiParam(name="varId", value="The variable id, equivalent to the variable name", required=true)
            @PathParam("varId") String variableId, 
            @ApiParam(name="value", value="The variable value", required=true)
            @PathParam("value") String value);

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\._]+}/instances")
    @ApiOperation(
            value="Retrieve a list of process instance logs for process instances that contain the specified variable",
            notes="The list of process instance logs is sorted in order by deployment id, process (definition) id, and lastly by process instance id",
            produces="application/xml, application/json",
            position=7)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getProcessInstanceLogsByVariableId(
            @ApiParam(name="varId", value="The variable id, equivalent to the variable name", required=true)
            @PathParam("varId") String variableId );

    @GET
    @Path("/variable/{varId: [a-zA-Z0-9-:\\.]+}/value/{value: [a-zA-Z0-9-:\\._]+}/instances")
    @ApiOperation(
            value="Retrieve a list of process instance logs for process instances that contain the specified variable with the specified value",
            notes="The list of process instance logs is sorted in order by deployment id, process (definition) id, and lastly by process instance id",
            produces="application/xml, application/json",
            position=8)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response getProcessInstanceLogsByVariableIdByVariableValue(
            @ApiParam(name="varId", value="The variable id, equivalent to the variable name", required=true)
            @PathParam("varId") String variableId, 
            @ApiParam(name="value", value="The variable value", required=true)
            @PathParam("value") String value );

    @POST
    @Path("/clear")
    @ApiOperation(
            value="Clear (delete) all history information",
            notes="This operation deletes *ALL* history information for every deployment unit",
            produces="application/xml, application/json",
            position=9)
    public Response clear();

}