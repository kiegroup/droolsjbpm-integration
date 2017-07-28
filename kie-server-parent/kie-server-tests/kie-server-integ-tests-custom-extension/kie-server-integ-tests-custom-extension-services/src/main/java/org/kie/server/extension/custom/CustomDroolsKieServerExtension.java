/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.extension.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.drools.RulesExecutionService;
import org.kie.server.services.impl.KieServerImpl;

public class CustomDroolsKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "custom-drools";

    private static final Boolean droolsDisabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));

    private boolean initialized = false;

    private KieServerRegistry registry;
    private RulesExecutionService rulesExecutionService;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return droolsDisabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        KieServerExtension droolsExtension = registry.getServerExtension(DroolsKieServerExtension.EXTENSION_NAME);
        if (droolsExtension == null) {
            // Drools extension not found, disabling itself
            initialized = false;
            return;
        }

        List<Object> droolsServices = droolsExtension.getServices();

        for (Object service : droolsServices) {
            if (RulesExecutionService.class.isAssignableFrom(service.getClass())) {
                rulesExecutionService = (RulesExecutionService) service;
                continue;
            }
        }
        this.registry = registry;
        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        List<Object> appComponentsList = new ArrayList<Object>();
        if (!initialized) {
            return appComponentsList;
        }

        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices = ServiceLoader.load(KieServerApplicationComponentsService.class);

        Object[] services = {rulesExecutionService, registry};

        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }

        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(rulesExecutionService.getClass())) {
            return (T) rulesExecutionService;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "Custom Drools capability";
    }

    @Override
    public List<Object> getServices() {
        List<Object> services = new ArrayList<Object>();
        services.add(rulesExecutionService);
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        // To be started after Drools
        return 10;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
}
