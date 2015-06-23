/*
 * Copyright 2015 JBoss Inc
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

import static org.kie.server.remote.rest.common.util.RestUtils.*;

@Path("/server")
public class KieServerResource {

    private KieContainerCommandService delegate;
    private MarshallerHelper marshallerHelper = new MarshallerHelper(null);

    public KieServerResource(KieContainerCommandService delegate) {
        this.delegate = delegate;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response executeCommands(@Context HttpHeaders headers, String commandScriptPayload ) {

        String contentType = getContentType(headers);

        CommandScript command = marshallerHelper.unmarshal(commandScriptPayload, contentType, CommandScript.class);

        ServiceResponsesList result = delegate.executeScript(command, MarshallerHelper.getFormat(contentType), null);

        return createCorrectVariant(result, headers);
    }

}
