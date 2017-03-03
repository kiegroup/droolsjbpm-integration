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
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.dmn.ModelEvaluatorServiceBase;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

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

    @GET
    // TODO if model should get its own sub-URI
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getModels(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam( CONTAINER_ID ) String containerId ) {
        Variant v = getVariant( headers );
        Header conversationIdHeader = buildConversationIdHeader(containerId, modelEvaluatorService.getKieServerRegistry(), headers);
        try {
            ServiceResponse<JaxbList> result = modelEvaluatorService.getModels( containerId );
            if( result.getType() == ServiceResponse.ResponseType.SUCCESS ) {
                return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.OK, conversationIdHeader );
            }
            return createCorrectVariant(marshallerHelper, containerId, result, headers, Response.Status.NOT_FOUND, conversationIdHeader );
        }  catch (Exception e) {
            LOG.error("Unexpected error retrieving models. Message: '{}'", e.getMessage(), e);
            return internalServerError(MessageFormat.format( "ERROR", e.getMessage()), v, conversationIdHeader);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response evaluateAllDecisions(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam(CONTAINER_ID) String containerId, String payload) {
        LOG.info( "About to evaluateAllDecisions on container {}", containerId );
        Variant v = getVariant( headers );
        Header conversationIdHeader = buildConversationIdHeader(containerId, modelEvaluatorService.getKieServerRegistry(), headers);
        try {
            String contentType = getContentType( headers );
            
            LOG.info( "Payload received: {}", payload);
            ServiceResponse<DMNResultKS> result = modelEvaluatorService.evaluateAllDecisions(containerId, payload, contentType);
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
