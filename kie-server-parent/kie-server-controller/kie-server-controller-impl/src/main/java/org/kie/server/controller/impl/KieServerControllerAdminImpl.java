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

package org.kie.server.controller.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.KieServerControllerAdmin;
import org.kie.server.controller.api.KieServerControllerException;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This admin api is deprecated from 6.4.x and should not be used, instead
 * SpecManagementService should be used
 * @see org.kie.server.controller.api.service.SpecManagementService
 */
@Deprecated
public abstract class KieServerControllerAdminImpl implements KieServerControllerAdmin {

    private static final Logger logger = LoggerFactory.getLogger(KieServerControllerAdminImpl.class);


    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();

    @Override
    public KieServerInstance addKieServerInstance(KieServerInfo kieServerInfo) throws KieServerControllerException {
        ServerTemplate serverTemplate = this.templateStorage.load(kieServerInfo.getServerId());
        if (serverTemplate != null) {
            throw new KieServerControllerException("KieServerInstance is already registered with id: " + kieServerInfo.getServerId());
        }
        // kie server instance is just for backward compatibility
        KieServerInstance kieServerInstance = new KieServerInstance(kieServerInfo.getServerId(),
                kieServerInfo.getName(),
                kieServerInfo.getVersion(),
                new HashSet<KieServerInstanceInfo>(),
                KieServerStatus.UP,
                new KieServerSetup());

        if (kieServerInfo.getLocation() != null && !kieServerInfo.getLocation().isEmpty()) {
            KieServerInstanceInfo instanceInfo = new KieServerInstanceInfo(kieServerInfo.getLocation(), KieServerStatus.UP, kieServerInfo.getCapabilities());
            kieServerInstance.getManagedInstances().add(instanceInfo);
        }
        // persist the configuration in based on server templates
        serverTemplate = new ServerTemplate(kieServerInfo.getServerId(),
                kieServerInfo.getName(),
                kieServerInfo.getCapabilities(),
                new HashMap<Capability, ServerConfig>(),
                new ArrayList<ContainerSpec>());

        if (kieServerInfo.getLocation() != null && !kieServerInfo.getLocation().isEmpty()) {
            serverTemplate.addServerInstance(ModelFactory.newServerInstanceKey(serverTemplate.getId(), kieServerInfo.getLocation()));
        }

        templateStorage.store(serverTemplate);

        // return kie server instance for backward compatibility reason
        return kieServerInstance;
    }

    @Override
    public KieServerInstance removeKieServerInstance(String identifier) throws KieServerControllerException {

        if (!this.templateStorage.exists(identifier)) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + identifier);
        }
        templateStorage.delete(identifier);
        return null;
    }

    @Override
    public List<KieServerInstance> listKieServerInstances() {
        List<KieServerInstance> serverInstances = new ArrayList<KieServerInstance>();
        Collection<ServerTemplate> templates = templateStorage.load();

        for (ServerTemplate serverTemplate : templates) {
            KieServerInstance kieServerInstance = forServerTemplate(serverTemplate);

            serverInstances.add(kieServerInstance);
        }


        return serverInstances;
    }

    @Override
    public KieServerInstance getKieServerInstance(String identifier) throws KieServerControllerException {
        ServerTemplate serverTemplate = this.templateStorage.load(identifier);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + identifier);
        }
        KieServerInstance kieServerInstance = forServerTemplate(serverTemplate);

        return kieServerInstance;
    }

    @Override
    public KieContainerResource createContainer(String id, String containerId, KieContainerResource container) {

        ServerTemplate serverTemplate = this.templateStorage.load(id);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
        if (containerSpec != null) {
            throw new KieServerControllerException("KieContainer already exists with id: " + id);
        }

        containerSpec = new ContainerSpec(container.getContainerId(),
                container.getContainerId(),
                serverTemplate,
                container.getReleaseId(),
                KieContainerStatus.STOPPED,
                new HashMap<Capability, ContainerConfig>());

        if (container.getScanner() != null) {
            RuleConfig ruleConfig = new RuleConfig();
            ruleConfig.setPollInterval(container.getScanner().getPollInterval());
            ruleConfig.setScannerStatus(container.getScanner().getStatus());

            containerSpec.addConfig(Capability.RULE, ruleConfig);
        }

        serverTemplate.addContainerSpec(containerSpec);

        this.templateStorage.update(serverTemplate);
        // keep as it was before
        container.setStatus(KieContainerStatus.STOPPED);

        return container;
    }

    @Override
    public void startContainer(String id, String containerId) {

        ServerTemplate serverTemplate = this.templateStorage.load(id);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
        if (containerSpec != null) {
            containerSpec.setStatus(KieContainerStatus.STARTED);

            this.templateStorage.update(serverTemplate);

            KieServerInstance kieServerInstance = forServerTemplate(serverTemplate);
            KieContainerResource kieContainerResource = forContainerSpec(containerSpec);

            notifyKieServersOnCreateContainer(kieServerInstance, kieContainerResource);
        } else {
            throw new KieServerControllerException("KieContainer not found with id: " + id);
        }
    }

    @Override
    public void stopContainer(String id, String containerId) {
        ServerTemplate serverTemplate = this.templateStorage.load(id);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
        if (containerSpec != null) {
            containerSpec.setStatus(KieContainerStatus.STOPPED);

            this.templateStorage.update(serverTemplate);

            KieServerInstance kieServerInstance = forServerTemplate(serverTemplate);

            notifyKieServersOnDeleteContainer(kieServerInstance, containerId);
        } else {
            throw new KieServerControllerException("KieContainer not found with id: " + id);
        }

    }

    @Override
    public void deleteContainer(String id, String containerId) {
        ServerTemplate serverTemplate = this.templateStorage.load(id);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
        if (containerSpec == null) {
            throw new KieServerControllerException("KieContainer not found with id: " + id);
        }

        serverTemplate.deleteContainerSpec(containerId);

        templateStorage.update(serverTemplate);

        KieServerInstance kieServerInstance = forServerTemplate(serverTemplate);
        notifyKieServersOnDeleteContainer(kieServerInstance, containerId);
    }

    @Override
    public KieContainerResource getContainer(String id, String containerId) {
        ServerTemplate serverTemplate = this.templateStorage.load(id);
        if (serverTemplate == null) {
            throw new KieServerControllerException("KieServerInstance not found with id: " + id);
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerId);
        if (containerSpec == null) {
            throw new KieServerControllerException("Container not found with id: " + containerId + " within kie server with id " + id);
        }

        KieContainerResource containerResource = forContainerSpec(containerSpec);

        return containerResource;
    }

    public KieServerTemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public void setTemplateStorage(KieServerTemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public abstract void notifyKieServersOnCreateContainer(KieServerInstance kieServerInstance, KieContainerResource container);

    public abstract void notifyKieServersOnDeleteContainer(KieServerInstance kieServerInstance, String containerId);


    protected KieServerInstance forServerTemplate(ServerTemplate serverTemplate) {
        KieServerInstance kieServerInstance = new KieServerInstance();
        kieServerInstance.setIdentifier(serverTemplate.getId());
        kieServerInstance.setVersion("");
        kieServerInstance.setName(serverTemplate.getName());
        kieServerInstance.setKieServerSetup(new KieServerSetup());
        kieServerInstance.setStatus(KieServerStatus.DOWN);
        kieServerInstance.setManagedInstances(new HashSet<KieServerInstanceInfo>());

        if (serverTemplate.getServerInstanceKeys() != null) {
            for (ServerInstanceKey instanceKey : serverTemplate.getServerInstanceKeys()) {
                KieServerInstanceInfo instanceInfo = new KieServerInstanceInfo(instanceKey.getUrl(), KieServerStatus.UP, serverTemplate.getCapabilities());

                kieServerInstance.getManagedInstances().add(instanceInfo);
            }
        }

        return kieServerInstance;
    }

    protected KieContainerResource forContainerSpec(ContainerSpec containerSpec) {
        KieContainerResource containerResource = new KieContainerResource();
        containerResource.setContainerId(containerSpec.getId());
        containerResource.setReleaseId(containerSpec.getReleasedId());
        containerResource.setStatus(containerSpec.getStatus());

        // cover scanner and rules config
        ContainerConfig containerConfig = containerSpec.getConfigs().get(Capability.RULE);
        if (containerConfig != null) {
            RuleConfig ruleConfig = (RuleConfig) containerConfig;

            KieScannerResource scannerResource = new KieScannerResource();
            scannerResource.setPollInterval(ruleConfig.getPollInterval());
            scannerResource.setStatus(ruleConfig.getScannerStatus());

            containerResource.setScanner(scannerResource);
        }


        return containerResource;
    }
}
