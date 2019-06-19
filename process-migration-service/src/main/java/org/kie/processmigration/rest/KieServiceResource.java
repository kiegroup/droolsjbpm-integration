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
package org.kie.processmigration.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.processmigration.model.ProcessInfo;
import org.kie.processmigration.model.RunningInstance;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.model.exceptions.ProcessDefinitionNotFoundException;
import org.kie.processmigration.service.KieService;

@Path("/kieserver")
@Produces(MediaType.APPLICATION_JSON)
public class KieServiceResource {

    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_PAGE_SIZE = "1000";

    @Inject
    KieService kieService;

    @GET
    public Response getKieServers() {
        return Response.ok(kieService.getConfigs()).build();
    }

    @GET
    @Path("/{kieServerId}/definitions/{containerId}/{processId}")
    public Response getDefinition(
            @PathParam("kieServerId") String kieServerId,
            @PathParam("containerId") String containerId,
            @PathParam("processId") String processId
    ) throws InvalidKieServerException {
        try {
            ProcessInfo definition = kieService.getDefinition(kieServerId, containerId, processId);
            return Response.ok(definition).build();
        } catch (ProcessDefinitionNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/instances")
    public Response getRunningInstances(@QueryParam("containerId") String containerId,
                                        @QueryParam("kieServerId") String kieServerId,
                                        @DefaultValue(DEFAULT_PAGE) @QueryParam("page") Integer page,
                                        @DefaultValue(DEFAULT_PAGE_SIZE) @QueryParam("pageSize") Integer pageSize) throws InvalidKieServerException {
        List<RunningInstance> result = kieService.getRunningInstances(containerId, kieServerId, page, pageSize);
        return Response.ok(result).build();
    }
}
