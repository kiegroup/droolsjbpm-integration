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

package org.kie.server.remote.rest.drools;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.kie.server.api.model.KieServiceResponse.ResponseType.FAILURE;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getClassType;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;

@Api(value="KIE session assets")
@Path("server/containers/instances/{" + RestURI.CONTAINER_ID + "}")
public class CommandResource {

    private static final Logger logger = LoggerFactory.getLogger(CommandResource.class);

    private KieContainerCommandService delegate;
    private KieServerRegistry registry;
    private MarshallerHelper marshallerHelper;

    public CommandResource() {

    }

    public CommandResource(KieContainerCommandService delegate, KieServerRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
        this.marshallerHelper = new MarshallerHelper(registry);
    }

    @ApiOperation(value = "Executes one or more runtime commands" )
    @ApiResponses({@ApiResponse(code = 200, message = "Successful execution", response = ServiceResponse.class),
                   @ApiResponse(code = 500, message = "Unexpected error", response = ServiceResponse.class),
                   @ApiResponse(code = 204, message = "Command execute successfully, but without response",
                                response = ServiceResponse.class)})
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response manageContainer(@Context HttpHeaders headers, 
            @ApiParam(value = "Container id where rules should be evaluated on", required = true) @PathParam(RestURI.CONTAINER_ID) String id, 
            @ApiParam(value = "Commands to be executed on rule engine given as BatchExecutionCommand type", required = true) String cmdPayload) {

        Variant v = getVariant(headers);
        String contentType = getContentType(headers);

        String classType = getClassType(headers);
        MarshallingFormat format = MarshallingFormat.fromType(contentType);
        if (format == null) {
            format = MarshallingFormat.valueOf(contentType);
        }
        logger.debug("Received request with content '{}'", cmdPayload);
        Header conversationIdHeader = buildConversationIdHeader(id, registry, headers);
        @SuppressWarnings("squid:S3740")
        ServiceResponse<?> result = delegate.callContainer(id, cmdPayload, format, classType);
        Status status = result.getType() == FAILURE ? INTERNAL_SERVER_ERROR : OK;
        try {
            String response = marshallerHelper.marshal(id, format.getType(), result, ContainerLocatorProvider
                    .get()
                    .getLocator());
            logger.debug("Returning {} response with content '{}'", status, response);

            return createResponse(response, v, status, conversationIdHeader);
        } catch (IllegalArgumentException e) {
            // in case marshalling failed return the call container response to keep backward compatibility
            String response = marshallerHelper.marshal(format.getType(), result);
            logger.debug("Returning {} response with content '{}'", status, response);
            return createResponse(response, v, status, conversationIdHeader);
        }
    }
}
