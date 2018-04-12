/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl.service;

import java.util.List;

import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerInstanceManager;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

public class RuleCapabilitiesServiceImpl implements RuleCapabilitiesService {

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();
    private KieServerInstanceManager kieServerInstanceManager = KieServerInstanceManager.getInstance();
    private NotificationService notificationService = LoggingNotificationService.getInstance();

    @Override
    public void scanNow(final ContainerSpecKey containerSpecKey) {
        ServerTemplate serverTemplate = templateStorage.load(containerSpecKey.getServerTemplateKey().getId());
        if (serverTemplate == null) {
            throw new KieServerControllerIllegalArgumentException("No server template found for id " + containerSpecKey.getServerTemplateKey().getId());
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerSpecKey.getId());
        if (containerSpec == null) {
            throw new KieServerControllerIllegalArgumentException("No container spec found for id " + containerSpecKey.getId());
        }

        List<Container> containers = kieServerInstanceManager.scanNow(serverTemplate,
                                                                      containerSpec);

        notificationService.notify(serverTemplate,
                                   containerSpec,
                                   containers);
    }

    @Override
    public void startScanner(final ContainerSpecKey containerSpecKey,
                             final Long interval) {

        ServerTemplate serverTemplate = templateStorage.load(containerSpecKey.getServerTemplateKey().getId());
        if (serverTemplate == null) {
            throw new KieServerControllerIllegalArgumentException("No server template found for id " + containerSpecKey.getServerTemplateKey().getId());
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerSpecKey.getId());
        if (containerSpec == null) {
            throw new KieServerControllerIllegalArgumentException("No container spec found for id " + containerSpecKey.getId());
        }

        ContainerConfig containerConfig = containerSpec.getConfigs().get(Capability.RULE);
        if (containerConfig == null) {
            containerConfig = new RuleConfig();
            containerSpec.getConfigs().put(Capability.RULE,
                                           containerConfig);
        }

        ((RuleConfig) containerConfig).setPollInterval(interval);
        ((RuleConfig) containerConfig).setScannerStatus(KieScannerStatus.STARTED);

        templateStorage.update(serverTemplate);

        List<Container> containers = kieServerInstanceManager.startScanner(serverTemplate,
                                                                           containerSpec,
                                                                           interval);

        notificationService.notify(serverTemplate,
                                   containerSpec,
                                   containers);
    }

    @Override
    public void stopScanner(final ContainerSpecKey containerSpecKey) {
        ServerTemplate serverTemplate = templateStorage.load(containerSpecKey.getServerTemplateKey().getId());
        if (serverTemplate == null) {
            throw new KieServerControllerIllegalArgumentException("No server template found for id " + containerSpecKey.getServerTemplateKey().getId());
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerSpecKey.getId());
        if (containerSpec == null) {
            throw new KieServerControllerIllegalArgumentException("No container spec found for id " + containerSpecKey.getId());
        }

        ContainerConfig containerConfig = containerSpec.getConfigs().get(Capability.RULE);
        if (containerConfig == null) {
            containerConfig = new RuleConfig();
            containerSpec.getConfigs().put(Capability.RULE,
                                           containerConfig);
        }

        if(((RuleConfig) containerConfig).getScannerStatus() == KieScannerStatus.STOPPED){
            return;
        }

        ((RuleConfig) containerConfig).setPollInterval(null);
        ((RuleConfig) containerConfig).setScannerStatus(KieScannerStatus.STOPPED);

        templateStorage.update(serverTemplate);

        List<Container> containers = kieServerInstanceManager.stopScanner(serverTemplate,
                                                                          containerSpec);

        notificationService.notify(serverTemplate,
                                   containerSpec,
                                   containers);
    }

    @Override
    public void upgradeContainer(final ContainerSpecKey containerSpecKey,
                                 ReleaseId releaseId) {
        ServerTemplate serverTemplate = templateStorage.load(containerSpecKey.getServerTemplateKey().getId());
        if (serverTemplate == null) {
            throw new KieServerControllerIllegalArgumentException("No server template found for id " + containerSpecKey.getServerTemplateKey().getId());
        }

        ContainerSpec containerSpec = serverTemplate.getContainerSpec(containerSpecKey.getId());
        if (containerSpec == null) {
            throw new KieServerControllerIllegalArgumentException("No container spec found for id " + containerSpecKey.getId());
        }
        if (releaseId.getGroupId() == null) {
            releaseId.setGroupId(containerSpec.getReleasedId().getGroupId());
        }
        if (releaseId.getArtifactId() == null) {
            releaseId.setArtifactId(containerSpec.getReleasedId().getArtifactId());
        }

        final List<Container> containers;

        containerSpec.setReleasedId(releaseId);

        if (containerSpec.getStatus() == KieContainerStatus.STARTED) {
            containers = kieServerInstanceManager.upgradeContainer(serverTemplate, containerSpec);
        } else {
            containers = kieServerInstanceManager.startContainer(serverTemplate, containerSpec);
        }

        containerSpec.setStatus(KieContainerStatus.STARTED);
        templateStorage.update(serverTemplate);

        notificationService.notify(serverTemplate,
                                   containerSpec,
                                   containers);
    }

    public KieServerTemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public void setTemplateStorage(KieServerTemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public KieServerInstanceManager getKieServerInstanceManager() {
        return kieServerInstanceManager;
    }

    public void setKieServerInstanceManager(KieServerInstanceManager kieServerInstanceManager) {
        this.kieServerInstanceManager = kieServerInstanceManager;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
