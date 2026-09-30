/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.swagger;

import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

// This class is needed to have the server/openapi.() rest available (otherwise, it would default to "/openapi.()))
@Path("server/openapi.{type:json|yaml}")
public class KieApiListingResource extends OpenApiResource {


    public KieApiListingResource() {
        Json.mapper().registerModule(new JakartaXmlBindAnnotationModule());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    public Response getOpenApi(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo,
                               @PathParam("type") String type) throws Exception {

        String acceptType = "yaml".equalsIgnoreCase(type)
                ? "application/yaml"
                : MediaType.APPLICATION_JSON;

        return super.getOpenApi(headers, uriInfo, acceptType);
    }
}