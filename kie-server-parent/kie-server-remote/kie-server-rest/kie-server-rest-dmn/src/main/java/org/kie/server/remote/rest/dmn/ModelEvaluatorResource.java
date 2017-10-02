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

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

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

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.dmn.ModelEvaluatorServiceBase;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Decision Service :: DMN")
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

    @ApiOperation(value="Evaluates decisions for given imput",
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


}
