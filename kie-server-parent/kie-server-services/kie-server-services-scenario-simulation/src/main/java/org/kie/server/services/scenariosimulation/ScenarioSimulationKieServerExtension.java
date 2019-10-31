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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;

public class ScenarioSimulationKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Scenario Simulation";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_SCENARIO_SIMULATION_SERVER_EXT_DISABLED, "true"));
    private static final Boolean disabledDroold = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));
    private static final Boolean disabledDMN = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DMN_SERVER_EXT_DISABLED, "false"));

    private KieServerRegistry context;
    private ScenarioSimulationService scenarioSimulationService;
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * This extension is active if it is not disabled and one of DMN or Drools is active
     * @return
     */
    @Override
    public boolean isActive() {
        return !disabled && (!disabledDroold || !disabledDMN);
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.context = registry;
        this.scenarioSimulationService = new ScenarioSimulationService(context);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        //no-op
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {context, scenarioSimulationService};
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }

        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_SCENARIO_SIMULATION;
    }

    @Override
    public List<Object> getServices() {
        return Collections.emptyList();
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);

        if (report) {
            messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
        }
        return messages;
    }
}