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

package org.kie.server.remote.rest.common.resource;

import static org.kie.server.remote.rest.common.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.common.docs.ParameterSamples.EXECUTE_CMD_JSON;
import static org.kie.server.remote.rest.common.docs.ParameterSamples.EXECUTE_CMD_RESPONSE_JSON;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.impl.marshal.MarshallerHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

@Api(value="KIE Server and KIE containers")
@Path("server/config")
public class KieServerResource {

    private KieContainerCommandService delegate;
    private MarshallerHelper marshallerHelper = new MarshallerHelper(null);

    public KieServerResource() {

    }

    public KieServerResource(KieContainerCommandService delegate) {
        this.delegate = delegate;
    }

    @ApiOperation(value="Executes one or more KIE Server commands for server-related or container-related operations",
            response=ServiceResponsesList.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 200, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=EXECUTE_CMD_RESPONSE_JSON)})) })
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeCommands(@Context HttpHeaders headers,
            @ApiParam(value = "command script payload", required = true, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=EXECUTE_CMD_JSON)})) String commandScriptPayload ) {

        String contentType = getContentType(headers);

        CommandScript command = marshallerHelper.unmarshal(commandScriptPayload, contentType, CommandScript.class);

        ServiceResponsesList result = delegate.executeScript(command, MarshallerHelper.getFormat(contentType), null);

        return createCorrectVariant(result, headers);
    }

}
