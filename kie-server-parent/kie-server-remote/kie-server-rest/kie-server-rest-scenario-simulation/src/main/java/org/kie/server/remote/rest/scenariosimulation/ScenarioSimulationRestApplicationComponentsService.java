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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.scenariosimulation.ScenarioSimulationKieServerExtension;
import org.kie.server.services.scenariosimulation.ScenarioSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioSimulationRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioSimulationRestApplicationComponentsService.class);

    private static final String OWNER_EXTENSION = ScenarioSimulationKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {

        // skip calls from other than owning extension
        if (!Objects.equals(OWNER_EXTENSION, extension)) {
            return Collections.emptyList();
        }

        Optional<ScenarioSimulationService> simulationService = Arrays.stream(services)
                .filter(elem -> ScenarioSimulationService.class.isAssignableFrom(elem.getClass()))
                .map(ScenarioSimulationService.class::cast)
                .findFirst();

        List<Object> components = new ArrayList<>(1);
        if (SupportedTransports.REST.equals(type)) {
            if (simulationService.isPresent()) {
                components.add(new ScenarioSimulationResource(simulationService.get()));
            } else {
                logger.error("Impossible to load Test Scenario extension because of missing ScenarioSimulationService. " +
                                     "This should never happen if ScenarioSimulationKieServerExtension is properly deployed and activated");
            }
        }

        return components;
    }
}