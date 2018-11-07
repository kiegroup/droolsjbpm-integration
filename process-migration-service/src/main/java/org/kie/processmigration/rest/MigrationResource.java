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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.processmigration.model.Execution.ExecutionType;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.exceptions.InvalidMigrationException;
import org.kie.processmigration.model.exceptions.MigrationNotFoundException;
import org.kie.processmigration.model.exceptions.ReScheduleException;
import org.kie.processmigration.service.MigrationService;

@Path("/migrations")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MigrationResource {

    @Inject
    private MigrationService migrationService;

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findAll() {
        return Response.ok(migrationService.findAll()).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) throws MigrationNotFoundException {
        return Response.ok(migrationService.get(id)).build();
    }

    @GET
    @Path("/{id}/results")
    public Response getResults(@PathParam("id") Long id) throws MigrationNotFoundException {
        return Response.ok(migrationService.getResults(id)).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response submit(@Context HttpHeaders headers, MigrationDefinition migration) throws InvalidMigrationException {
        Migration result = migrationService.submit(migration);
        if (ExecutionType.ASYNC.equals(migration.getExecution().getType())) {
            return Response.accepted(result).build();
        } else {
            return Response.ok(result).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@Context HttpHeaders headers, @PathParam("id") Long id, MigrationDefinition migrationDefinition) throws MigrationNotFoundException, InvalidMigrationException, ReScheduleException {
        Migration migration = migrationService.update(id, migrationDefinition);
        return Response.ok(migration).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("id") Long id) throws MigrationNotFoundException {
        return Response.ok(migrationService.delete(id)).build();
    }

}
