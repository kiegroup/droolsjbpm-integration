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

package org.kie.server.remote.rest.drools;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.remote.rest.common.util.RestUtils.*;

@Path("server/containers/instance/{id}")
public class CommandResource {

    private static final Logger logger = LoggerFactory.getLogger(CommandResource.class);

    private KieContainerCommandService delegate;

    private MarshallerHelper marshallerHelper;

    public CommandResource(KieContainerCommandService delegate, KieServerRegistry registry) {
        this.delegate = delegate;
        this.marshallerHelper = new MarshallerHelper(registry);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response manageContainer(@Context HttpHeaders headers, @PathParam("id") String id, String cmdPayload) {

        Variant v = getVariant(headers);
        String contentType = getContentType(headers);

        String classType = getClassType(headers);
        MarshallingFormat format = MarshallingFormat.fromType(contentType);
        if (format == null) {
            format = MarshallingFormat.valueOf(contentType);
        }

        Object result = delegate.callContainer(id, cmdPayload, format, classType);
        try {
            String response = marshallerHelper.marshal(id, format.getType(), result);
            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);
        } catch (IllegalArgumentException e) {
            // in case marshalling failed return the call container response to keep backward compatibility
            String response = marshallerHelper.marshal(format.getType(), result);
            logger.debug("Returning OK response with content '{}'", response);
            return createResponse(response, v, Response.Status.OK);
        }

    }

}
