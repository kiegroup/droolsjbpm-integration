package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/deployment/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@Api(
        value="/deployment/{deploymentId}", 
        description="Deployment unit management operations")
public interface DeploymentResource {

    /**
     * Retrieve the status of the {@link DeploymentUnit} specified in the URL.
     * 
     * @return A {@link JaxbDeploymentUnit} instance
     */
    @GET
    @ApiOperation(
            value="Retrieve basic information, including the status, of a deployment unit",
            position=0,
            produces="application/xml, application/json",
            response=JaxbDeploymentUnit.class)
    public Response getConfig();

    /**
     * Queues a request to deploy the given deployment unit. If the deployment already exist, this
     * operation will fail.
     * 
     * @param deployDescriptor An optional {@link DeploymentDescriptor} instance specifying additional information about how
     * the deployment unit should be deployed.
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/deploy")
    @ApiOperation(
            value="Deploy a deployment unit",
            notes="Submits an asynchronous request; takes an optional JaxbDeploymentDescriptor argument for more fine-grained configuration of the configuration",
            position=1,
            consumes="application/xml, application/json",
            produces="application/xml, application/json",
            response=JaxbDeploymentJobResult.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="strategy",
                value="The runtime strategy for process instances from the deployment unit",
                allowableValues="SINGLETON, PER_REQUEST, PER_PROCESS_INSTANCE (case is ignored)",
                allowMultiple=false,
                paramType="query",
                required=false,
                defaultValue="SINGLETON"),
        @ApiImplicitParam(
                name="mergemode",
                value="The merging strategy used when deploying the deployment unit",
                allowableValues="KEEP_ALL, OVERRIDE_ALL, OVERRIDE_EMPTY, MERGE_COLLECTIONS (case is ignored)",
                allowMultiple=false,
                paramType="query",
                required=false,
                defaultValue="MERGE_COLLECTIONS")
    })
    @ApiResponses({
       @ApiResponse(code=202, message="Request accepted")
    })
    public Response deploy( JaxbDeploymentDescriptor deployDescriptor );

    /**
     * Queues a request to undeploy the deployment unit specified in the URL
     * 
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/undeploy")
    @ApiOperation(
            value="Undeploy a deployment unit",
            notes="Submits an asynchronous request",
            position=2,
            produces="application/xml, application/json",
            response=JaxbDeploymentJobResult.class)
    @ApiResponses({
       @ApiResponse(code=202, message="Request accepted")
    })
    public Response undeploy();

    @GET
    @Path("/processes")
    @ApiOperation(
            value="Retrieve a list of process definition info for a deployment unit",
            notes="The list is sorted alphabetically by process id",
            position=3,
            produces="application/xml, application/json",
            response=JaxbProcessDefinitionList.class)
    @ApiImplicitParams({
        @ApiImplicitParam(
                name="page",
                value="The page number of the results (the abbreviated version 'p' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false),
        @ApiImplicitParam(
                name="pagesize",
                value="The page size used for the results (the abbreviated version 's' is also acceptable)",
                allowMultiple=false,
                paramType="query",
                required=false)
    })
    public Response listProcessDefinitions();

}