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

package org.kie.server.services.scenariosimulation;

import java.util.Optional;

import org.drools.scenariosimulation.api.model.ScenarioSimulationModel;
import org.drools.scenariosimulation.api.model.Settings;
import org.drools.scenariosimulation.api.model.Simulation;
import org.drools.scenariosimulation.backend.runner.AbstractScenarioRunner;
import org.drools.scenariosimulation.backend.util.ScenarioSimulationXMLPersistence;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationFailure;
import org.kie.server.api.model.scenariosimulation.ScenarioSimulationResult;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;

public class ScenarioSimulationService {

    private KieServerRegistry kieServerRegistry;

    public ScenarioSimulationService(KieServerRegistry kieServerRegistry) {
        this.kieServerRegistry = kieServerRegistry;
    }

    public ServiceResponse<ScenarioSimulationResult> executeScenario(KieContainer kieContainer, ScenarioSimulationModel scenarioSimulationModel) {

        AbstractScenarioRunner runner = newRunner(kieContainer, scenarioSimulationModel);

        JUnitCore jUnitCore = new JUnitCore();

        Result result = jUnitCore.run(runner);

        if (result.wasSuccessful()) {
            return new ServiceResponse<>(KieServiceResponse.ResponseType.SUCCESS, "Test Scenario successfully executed", convertResult(result));
        } else {
            return new ServiceResponse<>(KieServiceResponse.ResponseType.FAILURE, "Test Scenario execution failed", convertResult(result));
        }
    }

    public KieServerRegistry getKieServerRegistry() {
        return kieServerRegistry;
    }

    public ScenarioSimulationModel parseModel(String rawContent) throws Exception {
        return ScenarioSimulationXMLPersistence.getInstance().unmarshal(rawContent);
    }

    public KieContainer getKieContainerById(String containerId) {
        ContainerLocator locator = ContainerLocatorProvider.get().getLocator();
        return Optional.ofNullable(kieServerRegistry)
                .map(elem -> elem.getContainer(containerId, locator))
                .map(KieContainerInstance::getKieContainer)
                .orElseThrow(() -> new IllegalStateException("Impossible to retrieve kieContainer with id " + containerId));
    }

    protected AbstractScenarioRunner newRunner(KieContainer kieContainer, ScenarioSimulationModel scenarioSimulationModel) {
        Simulation simulation = scenarioSimulationModel.getSimulation();
        Settings settings = scenarioSimulationModel.getSettings();
        return AbstractScenarioRunner.getSpecificRunnerProvider(settings.getType())
                .create(kieContainer, simulation.getScesimModelDescriptor(), simulation.getScenarioWithIndex(), settings);
    }

    protected ScenarioSimulationResult convertResult(Result result) {
        ScenarioSimulationResult scenarioSimulationResult = new ScenarioSimulationResult();
        scenarioSimulationResult.setRunCount(result.getRunCount());
        scenarioSimulationResult.setIgnoreCount(result.getIgnoreCount());
        scenarioSimulationResult.setRunTime(result.getRunTime());
        for (Failure failure : result.getFailures()) {
            scenarioSimulationResult.getFailures().add(convertFailure(failure));
        }
        return scenarioSimulationResult;
    }

    protected ScenarioSimulationFailure convertFailure(Failure failure) {
        ScenarioSimulationFailure scenarioSimulationFailure = new ScenarioSimulationFailure();
        scenarioSimulationFailure.setErrorMessage(failure.getMessage());
        scenarioSimulationFailure.setDescription(failure.getDescription().getDisplayName());
        return scenarioSimulationFailure;
    }
}
