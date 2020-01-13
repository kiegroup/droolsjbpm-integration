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

package org.kie.server.client;

import java.io.IOException;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationResult;

public interface ScenarioSimulationServicesClient {

    /**
     * Execute given test scenario against specified containerId
     * @param containerId id of container to be used to execute the scenario
     * @param localPath to Test Scenario file (scesim) to be executed. It must be a valid {@link java.nio.file.Paths#get(String, String...)} parameter
     * @return result of Test Scenario execution (jUnit like)
     */
    ServiceResponse<ScenarioSimulationResult> executeScenarioByPath(String containerId, String localPath) throws IOException;

    /**
     * Execute given test scenario against specified containerId
     * @param containerId id of container to be used to execute the scenario
     * @param content of Test Scenario file (scesim) to be executed. This string should contain the XML of the scesim file
     * @return result of Test Scenario execution (jUnit like)
     */
    ServiceResponse<ScenarioSimulationResult> executeScenario(String containerId, String content);
}
