/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.ui.img.ImageReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JBPMUIKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM-UI";

    private static final Logger logger = LoggerFactory.getLogger(JBPMUIKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_UI_SERVER_EXT_DISABLED, "false"));
    private static final Boolean jbpmDisabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED, "false"));

    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;

    private ConcurrentMap<String, ImageReference> imageReferences = new ConcurrentHashMap<String, ImageReference>();

    private KieServerRegistry registry;

    private FormServiceBase formServiceBase;
    private ImageServiceBase imageServiceBase;

    private DeploymentService deploymentService;

    private KieContainerCommandService kieContainerCommandService;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return disabled == false && jbpmDisabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {

        this.registry = registry;
        KieServerExtension jbpmExtension = registry.getServerExtension("jBPM");
        if (jbpmExtension == null) {
            initialized = false;
            logger.warn("jBPM extension not found, jBPM UI cannot work without jBPM extension, disabling itself");
            return;
        }

        List<Object> jbpmServices = jbpmExtension.getServices();


        RuntimeDataService runtimeDataService = null;
        DefinitionService definitionService = null;
        UserTaskService userTaskService = null;
        FormManagerService formManagerService = null;

        for( Object object : jbpmServices ) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if( RuntimeDataService.class.isAssignableFrom(object.getClass()) ) {
                runtimeDataService = (RuntimeDataService) object;
                continue;
            } else if( DefinitionService.class.isAssignableFrom(object.getClass()) ) {
                definitionService = (DefinitionService) object;
                continue;
            } else if( UserTaskService.class.isAssignableFrom(object.getClass()) ) {
                userTaskService = (UserTaskService) object;
                continue;
            } else if( FormManagerService.class.isAssignableFrom(object.getClass()) ) {
                formManagerService = (FormManagerService) object;
                continue;
            } else if( DeploymentService.class.isAssignableFrom(object.getClass()) ) {
                deploymentService = (DeploymentService) object;
                continue;
            }
        }

        formServiceBase = new FormServiceBase(definitionService, runtimeDataService, userTaskService, formManagerService, registry);
        imageServiceBase = new ImageServiceBase(runtimeDataService, imageReferences, registry);

        services.add(formServiceBase);
        services.add(imageServiceBase);

        this.kieContainerCommandService = new JBPMUIKieContainerCommandServiceImpl(null, formServiceBase, imageServiceBase);

        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if (!initialized) {
            return;
        }
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!initialized) {
            return;
        }
        try {

            String kieBaseName = ((KModuleDeploymentUnit)deploymentService.getDeployedUnit(id).getDeploymentUnit()).getKbaseName();

            KieContainer kieContainer = kieContainerInstance.getKieContainer();
            imageReferences.putIfAbsent(id, new ImageReference(kieContainer, kieBaseName));
        } catch (Exception e) {
            logger.warn("Unable to create image reference for container {} due to {}", id, e.getMessage());
        }

    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // recreate configuration for updated container
        disposeContainer(id, kieContainerInstance, parameters);
        createContainer(id, kieContainerInstance, parameters);
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!initialized) {
            return;
        }

        imageReferences.remove(id);
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        List<Object> appComponentsList = new ArrayList<Object>();
        if (!initialized) {
            return appComponentsList;
        }

        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices = ServiceLoader.load(KieServerApplicationComponentsService.class);

        Object [] services = {
                formServiceBase,
                imageServiceBase,
                registry
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;


    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (!initialized) {
            return null;
        }
        if (serviceType.isAssignableFrom(kieContainerCommandService.getClass())) {
            return (T) kieContainerCommandService;
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BPM_UI;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 10;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

}
