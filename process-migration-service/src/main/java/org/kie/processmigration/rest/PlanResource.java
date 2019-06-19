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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.exceptions.PlanNotFoundException;
import org.kie.processmigration.service.PlanService;

@Path("/plans")
@Api(value = "Plans")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PlanResource {

    @Inject
    private PlanService planService;

    @GET
    @ApiOperation(value = "Get all existing Migration plans")
    public Response findAll() {
        return Response.ok(planService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    @ApiOperation(value = "Finds a migration plan by the given plan Id")
    public Response get(@ApiParam(value = "Plan Id") @PathParam("id") Long id) throws PlanNotFoundException {
        return Response.ok(planService.get(id)).build();
    }

    @POST
    @ApiOperation(value = "Create a migration plan")
    public Response create(@ApiParam(value = "Plan") Plan plan) {
        if (plan.getId() != 0) {
            throw new IllegalArgumentException("The plan ID must not be provided when creating a new plan");
        }
        return Response.ok(planService.create(plan)).build();
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(value = "Save a migration plan")
    public Response save(@ApiParam(value = "Plan Id to update") @PathParam("id") Long id,
                         @ApiParam(value = "Plan") Plan plan) throws PlanNotFoundException {
        return Response.ok(planService.update(id, plan)).build();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete an existing migration plan")
    public Response delete(@ApiParam(value = "Plan Id to update") @PathParam("id") Long id) throws PlanNotFoundException {
        return Response.ok(planService.delete(id)).build();
    }
}