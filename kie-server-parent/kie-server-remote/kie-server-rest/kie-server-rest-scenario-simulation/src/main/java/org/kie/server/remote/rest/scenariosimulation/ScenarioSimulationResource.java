/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.remote.rest.scenariosimulation;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.scenariosimulation.api.model.ScenarioSimulationModel;
import org.drools.scenariosimulation.backend.util.ScenarioSimulationXMLPersistence;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationResult;
import org.kie.server.services.scenariosimulation.ScenarioSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;

@Path("server/containers/{id}/scesim")
public class ScenarioSimulationResource {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioSimulationResource.class);
    private final ScenarioSimulationService scenarioSimulationService;

    public ScenarioSimulationResource(ScenarioSimulationService scenarioSimulationService) {
        this.scenarioSimulationService = scenarioSimulationService;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response executeSimulation(@Context HttpHeaders headers,
                                      @PathParam("id") String id,
                                      String content) throws Exception {

        ScenarioSimulationModel scenarioSimulationModel = ScenarioSimulationXMLPersistence.getInstance().unmarshal(content);

        ServiceResponse<ScenarioSimulationResult> response = scenarioSimulationService.executeScenario(id, scenarioSimulationModel);

        return createCorrectVariant(response, headers, Response.Status.OK);
    }
}
