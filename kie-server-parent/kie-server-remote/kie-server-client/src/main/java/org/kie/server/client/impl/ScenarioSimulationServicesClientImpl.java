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

package org.kie.server.client.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationResult;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.ScenarioSimulationServicesClient;

import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.SCENARIO_SIMULATION_URI;
import static org.kie.server.api.rest.RestURI.build;

public class ScenarioSimulationServicesClientImpl extends AbstractKieServicesClientImpl implements ScenarioSimulationServicesClient {

    public ScenarioSimulationServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public ScenarioSimulationServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public ServiceResponse<ScenarioSimulationResult> executeScenarioByPath(String containerId, String path) throws IOException {
        try (Stream<String> contentStream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
            String fileContent = contentStream.collect(Collectors.joining("\n"));
            return executeScenario(containerId, fileContent);
        }
    }

    @Override
    public ServiceResponse<ScenarioSimulationResult> executeScenario(String containerId, String content) {

        if (!config.isRest()) {
            throw new IllegalStateException("Only REST is supported");
        }

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(CONTAINER_ID, containerId);

        ServiceResponse<ScenarioSimulationResult> response = makeHttpPostRequestAndCreateServiceResponse(
                build(loadBalancer.getUrl(), SCENARIO_SIMULATION_URI, valuesMap),
                content,
                ScenarioSimulationResult.class);

        if (shouldReturnWithNullResponse(response)) {
            return null;
        }

        return response;
    }
}
