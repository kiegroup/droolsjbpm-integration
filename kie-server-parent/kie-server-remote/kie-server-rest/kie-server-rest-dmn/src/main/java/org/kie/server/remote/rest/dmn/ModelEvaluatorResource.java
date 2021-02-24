/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.dmn;

import java.text.MessageFormat;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.dmn.ModelEvaluatorServiceBase;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.DECISIONSERVICE_ID;
import static org.kie.server.api.rest.RestURI.DMN_MODEL_DMNRESULT_URI;
import static org.kie.server.api.rest.RestURI.DMN_MODEL_DS_DMNRESULT_URI;
import static org.kie.server.api.rest.RestURI.DMN_MODEL_DS_URI;
import static org.kie.server.api.rest.RestURI.DMN_MODEL_URI;
import static org.kie.server.api.rest.RestURI.MODEL_ID;
import static org.kie.server.api.rest.RestURI.OPENAPI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Api(value="DMN models")
@Path("server/"+ RestURI.DMN_URI )
public class ModelEvaluatorResource {

    public static final Logger LOG = LoggerFactory.getLogger( ModelEvaluatorResource.class );

    private ModelEvaluatorServiceBase modelEvaluatorService;
    private MarshallerHelper  marshallerHelper;

    public ModelEvaluatorResource() {
    }

    public ModelEvaluatorResource(ModelEvaluatorServiceBase modelEvaluatorService) {
        this.modelEvaluatorService = modelEvaluatorService;
        this.marshallerHelper = new MarshallerHelper(modelEvaluatorService.getKieServerRegistry());
    }

    @ApiOperation(value="Retrieves DMN model for given container",
            response=ServiceResponse.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Models or container not found") })
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getModels(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "Container id that modesl should be loaded from", required = true) @PathParam( CONTAINER_ID ) String containerId ) {
        Variant v = getVariant( headers );
        Header conversationIdHeader = buildConversationIdHeader(containerId, modelEvaluatorService.getKieServerRegistry(), headers);
        try {
            ServiceResponse<DMNModelInfoList> result = modelEvaluatorService.getModels( containerId );
            if( result.getType() == ServiceResponse.ResponseType.SUCCESS ) {
                return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.OK, conversationIdHeader );
            }
            return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.NOT_FOUND, conversationIdHeader );
        }  catch (Exception e) {
            LOG.error("Unexpected error retrieving models. Message: '{}'", e.getMessage(), e);
            return internalServerError(MessageFormat.format( "ERROR", e.getMessage()), v, conversationIdHeader);
        }
    }

    @ApiOperation(value="Evaluates decisions for given input",
            response=ServiceResponse.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Container not found") })
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response evaluateDecisions(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId, 
            @ApiParam(value = "DMN context to be used while evaluation decisions as DMNContextKS type", required = true) String payload) {
        LOG.debug( "About to evaluateDecisions() on container {}", containerId );
        Variant v = getVariant( headers );
        Header conversationIdHeader = buildConversationIdHeader(containerId, modelEvaluatorService.getKieServerRegistry(), headers);
        try {
            String contentType = getContentType( headers );
            
            LOG.debug( "Payload received: {}", payload);
            ServiceResponse<DMNResultKS> result = modelEvaluatorService.evaluateDecisions(containerId, payload, contentType);
            if( result.getType() == ServiceResponse.ResponseType.SUCCESS ) {
                return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.OK, conversationIdHeader );
            }
            return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.NOT_FOUND, conversationIdHeader );
        }  catch (Exception e) {
            LOG.error("Unexpected error retrieving models. Message: '{}'", e.getMessage(), e);
            return internalServerError(MessageFormat.format( "ERROR", e.getMessage()), v, conversationIdHeader);
        }
    }

    @Path(DMN_MODEL_URI)
    @ApiOperation(value = "Model-specific DMN evaluation. Reference container-specific Swagger/OAS descriptor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateModel(@ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId,
                                  @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(MODEL_ID) String modelId,
                                  @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) String payload) {
        LOG.debug("About to evaluateModel() on container {}", containerId);
        return modelEvaluatorService.evaluateModel(containerId, modelId, payload, false, null);
    }

    @Path(DMN_MODEL_URI)
    @ApiOperation(value = "Model-specific definitions get. Reference container-specific Swagger/OAS descriptor")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getModel(@ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId,
                             @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(MODEL_ID) String modelId) {
        LOG.debug("About to evaluateModel() on container {}", containerId);
        return modelEvaluatorService.getModel(containerId, modelId);
    }

    @Path(DMN_MODEL_DMNRESULT_URI)
    @ApiOperation(value = "Model-specific DMN evaluation. Reference container-specific Swagger/OAS descriptor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateModelAsDmnResult(@ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId,
                                             @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(MODEL_ID) String modelId,
                                             @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) String payload) {
        LOG.debug("About to evaluateModelAsDmnResult() on container {}", containerId);
        return modelEvaluatorService.evaluateModel(containerId, modelId, payload, true, null);
    }

    @Path(DMN_MODEL_DS_URI)
    @ApiOperation(value = "Model-specific DMN evaluation. Reference container-specific Swagger/OAS descriptor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateModelDS(@ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId,
                                    @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(MODEL_ID) String modelId,
                                    @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(DECISIONSERVICE_ID) String decisionServiceId,
                                    @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) String payload) {
        LOG.debug("About to evaluateModelDS() on container {}", containerId);
        return modelEvaluatorService.evaluateModel(containerId, modelId, payload, false, decisionServiceId);
    }

    @Path(DMN_MODEL_DS_DMNRESULT_URI)
    @ApiOperation(value = "Model-specific DMN evaluation. Reference container-specific Swagger/OAS descriptor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateModelDSAsDmnResult(@ApiParam(value = "Container id to be used to evaluate decisions on", required = true) @PathParam(CONTAINER_ID) String containerId,
                                               @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(MODEL_ID) String modelId,
                                               @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) @PathParam(DECISIONSERVICE_ID) String decisionServiceId,
                                               @ApiParam(value = "Reference container-specific Swagger/OAS descriptor", required = true) String payload) {
        LOG.debug("About to evaluateModelDSAsDmnResult() on container {}", containerId);
        return modelEvaluatorService.evaluateModel(containerId, modelId, payload, true, decisionServiceId);
    }

    @Path(OPENAPI + ".{type:json|yaml}")
    @ApiOperation(value = "Model-specific definitions get. Reference container-specific Swagger/OAS descriptor")
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    public Response getOASType(@ApiParam(value = "Container id to retrieve the Swagger/OAS descriptor for DMN endpoints", required = true) @PathParam(CONTAINER_ID) String containerId,
                               @PathParam("type") String type) {
        LOG.debug("About to getOASType() on container {}", containerId);
        if (type != null && type.equals("yaml")) {
            return modelEvaluatorService.getOAS(containerId, false);
        } else {
            return modelEvaluatorService.getOAS(containerId, true);
        }
    }

    @Path(OPENAPI)
    @ApiOperation(value = "JSON Model-specific definitions get. Reference container-specific Swagger/OAS descriptor")
    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    public Response getOAS(@ApiParam(value = "Container id to retrieve the Swagger/OAS descriptor for DMN endpoints", required = true) @PathParam(CONTAINER_ID) String containerId) {
        LOG.debug("About to getOAS() on container {}", containerId);
        return getOASType(containerId, "json");
    }
}
