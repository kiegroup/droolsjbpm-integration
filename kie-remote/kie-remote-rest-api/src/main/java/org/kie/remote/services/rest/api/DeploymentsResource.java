package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/deployment")
@Api(
        value="/deployment", 
        description="Retrieve deployment unit information")
public interface DeploymentsResource {

    @GET
    @ApiOperation(
            value="Retrieve a list of deployment unit information",
            notes="The list is sorted alphabetically by deployment id",
            position=0,
            produces="application/xml, application/json",
            response=JaxbDeploymentUnitList.class)
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
    public Response listDeployments();

    @GET
    @Path("/processes")
    @ApiOperation(
            value="Retrieve a list of process definition information",
            notes="The list is sorted alphabetically by deployment id and process (definition) id",
            position=1,
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