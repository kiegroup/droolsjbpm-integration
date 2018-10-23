/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.remote.rest.jbpm.ui;

import static org.kie.server.api.rest.RestURI.STATIC_FILES_URI;
import static org.kie.server.api.rest.RestURI.STATIC_BY_TYPE_GET_URI;
import static org.kie.server.api.rest.RestURI.STATIC_RENDERER_BY_TYPE_GET_URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.kie.server.services.jbpm.ui.FormRendererBase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value="Static files endpoint :: BPM")
@Path("server/" + STATIC_FILES_URI)
public class StaticFilesResource {
    
    private FormRendererBase formRendererBase;

    public StaticFilesResource(FormRendererBase formRendererBase) {
        this.formRendererBase = formRendererBase;
    }

    @ApiOperation(value="Retrieves given resource/file based on the type and file name",
            response=String.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "resource/file not found") })
    @GET
    @Path(STATIC_BY_TYPE_GET_URI)
    public Response getSharedContent(
            @ApiParam(value = "Type of the resource e.g. js, css, etc", required = true) @PathParam("type") String type,
            @ApiParam(value = "Name of the resource to look up", required = true) @PathParam("file") String file) {
        
        InputStream resourceStream = formRendererBase.readResources("/" + type + "/" + file);
        if (resourceStream == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        StreamingOutput entity = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                
                IOUtils.copy(resourceStream, output);                
            }
        };      
        return Response.ok().entity(entity).header("Content-Type", getContentType(file)).build();
        
    }

    @ApiOperation(value="Retrieves given resource/file based on the type and file name managed by given provider",
            response=String.class, code=200)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"),
            @ApiResponse(code = 404, message = "resource/file not found") })
    @GET
    @Path(STATIC_RENDERER_BY_TYPE_GET_URI)
    public Response getProviderSpecificContent(
            @ApiParam(value = "Name of the provider that manages given resource", required = true) @PathParam("provider") String provider, 
            @ApiParam(value = "Type of the resource e.g. js, css, etc", required = true) @PathParam("type") String type, 
            @ApiParam(value = "Name of the resource to look up", required = true) @PathParam("file") String file) {
        
        InputStream resourceStream = formRendererBase.readResources("/" + provider + "/" + type + "/" + file);
        if (resourceStream == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        StreamingOutput entity = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                
                IOUtils.copy(resourceStream, output);                
            }
        };      
        return Response.ok().entity(entity).header("Content-Type", getContentType(file)).build();
        
    }
    
    protected String getContentType(String file) {
        if (file.toLowerCase().endsWith(".js")) {
            return "application/javascript";
        } else if (file.toLowerCase().endsWith(".css")) {
            return "text/css";
        } else {
            return "application/octet-stream";
        }
        
    }
}
