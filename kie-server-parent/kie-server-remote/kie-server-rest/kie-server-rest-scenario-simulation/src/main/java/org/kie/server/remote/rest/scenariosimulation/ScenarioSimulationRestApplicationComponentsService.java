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

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.scenariosimulation.ScenarioSimulationKieServerExtension;
import org.kie.server.services.scenariosimulation.ScenarioSimulationService;

public class ScenarioSimulationRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = ScenarioSimulationKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {

        // skip calls from other than owning extension
        if (!OWNER_EXTENSION.equals(extension)) {
            return Collections.emptyList();
        }

        ScenarioSimulationService scenarioSimulationService = Arrays.stream(services)
                .filter(elem -> ScenarioSimulationService.class.isAssignableFrom(elem.getClass()))
                .map(ScenarioSimulationService.class::cast)
                .findFirst()
                .orElse(null);

        List<Object> components = new ArrayList<Object>(1);
        if (SupportedTransports.REST.equals(type)) {
            components.add(new ScenarioSimulationResource(scenarioSimulationService));
        }

        return components;
    }
}
