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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.drools.scenariosimulation.api.model.ScenarioSimulationModel;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.scenariosimulation.ScenarioSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.SCENARIO_SIMULATION_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Api(value = "Test scenario execution")
@Path("server/" + SCENARIO_SIMULATION_URI)
public class ScenarioSimulationResource {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioSimulationResource.class);
    private final ScenarioSimulationService scenarioSimulationService;

    public ScenarioSimulationResource(ScenarioSimulationService scenarioSimulationService) {
        this.scenarioSimulationService = scenarioSimulationService;
    }

    @ApiOperation(value = "Execute given test scenario against specified container",
            response = ServiceResponse.class, code = 200)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "Container not found"), @ApiResponse(code = 400, message = "Malformed test scenario file")})
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response executeSimulation(@Context HttpHeaders headers,
                                      @ApiParam(value = "Container id", required = true) @PathParam(CONTAINER_ID) String containerId,
                                      @ApiParam(value = "Test scenario file content to be executed", required = true) String rawContent) {
        KieContainer kieContainer = null;
        ScenarioSimulationModel model = null;

        Variant v = getVariant(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, scenarioSimulationService.getKieServerRegistry(), headers);

        try {
            kieContainer = scenarioSimulationService.getKieContainerById(containerId);
        } catch (Exception e) {
            return createCorrectVariant(
                    createFailedServiceResponse("Impossible to find container with id " + containerId, e),
                    headers,
                    Response.Status.NOT_FOUND,
                    conversationIdHeader);
        }

        try {
            model = scenarioSimulationService.parseModel(rawContent);
        } catch (Exception e) {
            return createCorrectVariant(
                    createFailedServiceResponse("Test scenario parsing error", e),
                    headers,
                    Response.Status.BAD_REQUEST,
                    conversationIdHeader);
        }

        try {
            return createCorrectVariant(
                    scenarioSimulationService.executeScenario(kieContainer, model),
                    headers,
                    Response.Status.OK,
                    conversationIdHeader);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return internalServerError(
                    e.getMessage(),
                    v,
                    conversationIdHeader);
        }
    }

    protected ServiceResponse<ScenarioSimulationResource> createFailedServiceResponse(String errorMessage, Exception e) {
        return new ServiceResponse<>(KieServiceResponse.ResponseType.FAILURE, errorMessage + ": " + e.getMessage());
    }
}
