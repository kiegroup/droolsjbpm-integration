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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.ModelFactory;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.events.ServerInstanceConnected;
import org.kie.server.controller.api.model.events.ServerInstanceDeleted;
import org.kie.server.controller.api.model.events.ServerInstanceDisconnected;
import org.kie.server.controller.api.model.events.ServerInstanceUpdated;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.service.LoggingNotificationService;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieServerControllerImpl implements KieServerController {

    private static final Logger logger = LoggerFactory.getLogger(KieServerControllerImpl.class);

    private static Map<String, String> SERVER_INSTANCE_TO_SERVER_TEMPLATE_CAPABILITIES = new HashMap<String, String>() {{
        put(KieServerConstants.CAPABILITY_BRM, Capability.RULE.toString());
        put(KieServerConstants.CAPABILITY_BPM, Capability.PROCESS.toString());
        put(KieServerConstants.CAPABILITY_BRP, Capability.PLANNING.toString());
    }};

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();

    private NotificationService notificationService = LoggingNotificationService.getInstance();

    @Override
    public synchronized KieServerSetup connect(KieServerInfo serverInfo) {
        boolean isKieServerLocationAvailable = isKieServerLocationAvailable(serverInfo);
        String serverId = serverInfo.getServerId();
        ServerTemplate serverTemplate = templateStorage.load(serverId);
        KieServerSetup serverSetup = new KieServerSetup();
        ServerInstanceKey serverInstanceKey = null;
        List<Container> containerList = new ArrayList<Container>();
        if (isKieServerLocationAvailable) {
            if (serverTemplate == null || serverTemplate.getServerInstance(serverId) == null) {
                logger.warn("Trying to connect a detached or undefined KIE server: {} is not supported.", serverId);
                return serverSetup;
            } else {
                serverInstanceKey = serverTemplate.getServerInstance(serverId);
                serverInfo.setLocation(serverInstanceKey.getUrl());
            }
        } else {
            serverInstanceKey = ModelFactory.newServerInstanceKey(serverInfo.getServerId(), serverInfo.getLocation());
        }
        if (serverTemplate != null) {
            if (!checkValidServerInstance(serverTemplate, serverInfo, serverSetup)) {
                return serverSetup;
            }
            logger.debug("Server id {} know to the controller, checking if given server exists", serverInfo.getServerId());

            if (!serverTemplate.hasServerInstance(serverInfo.getLocation())) {
                logger.debug("Server instance '{}' not yet registered", serverInfo.getLocation());
                serverTemplate.addServerInstance(serverInstanceKey);
            }
            if (!isKieServerLocationAvailable) {
                templateStorage.update(serverTemplate);
                logger.debug("KieServerInstance updated after connect from server {}", serverInfo.getLocation());
            }

            Set<KieContainerResource> containers = new HashSet<KieContainerResource>();
            for (ContainerSpec containerSpec : serverTemplate.getContainersSpec()) {

                KieContainerResource containerResource = new KieContainerResource();
                containerResource.setContainerId(containerSpec.getId());
                containerResource.setContainerAlias(containerSpec.getContainerName());
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
                // cover process config
                containerConfig = containerSpec.getConfigs().get(Capability.PROCESS);
                if (containerConfig != null) {
                    ProcessConfig processConfig = (ProcessConfig) containerConfig;

                    KieServerConfigItem configItem = new KieServerConfigItem();
                    configItem.setType(KieServerConstants.CAPABILITY_BPM);
                    configItem.setName(KieServerConstants.PCFG_KIE_BASE);
                    configItem.setValue(processConfig.getKBase());

                    containerResource.addConfigItem(configItem);

                    configItem = new KieServerConfigItem();
                    configItem.setType(KieServerConstants.CAPABILITY_BPM);
                    configItem.setName(KieServerConstants.PCFG_KIE_SESSION);
                    configItem.setValue(processConfig.getKSession());

                    containerResource.addConfigItem(configItem);

                    configItem = new KieServerConfigItem();
                    configItem.setType(KieServerConstants.CAPABILITY_BPM);
                    configItem.setName(KieServerConstants.PCFG_MERGE_MODE);
                    configItem.setValue(processConfig.getMergeMode());

                    containerResource.addConfigItem(configItem);

                    configItem = new KieServerConfigItem();
                    configItem.setType(KieServerConstants.CAPABILITY_BPM);
                    configItem.setName(KieServerConstants.PCFG_RUNTIME_STRATEGY);
                    configItem.setValue(processConfig.getRuntimeStrategy());

                    containerResource.addConfigItem(configItem);
                }

                containers.add(containerResource);

                containerList.add(new Container(containerSpec.getId(),
                                                containerSpec.getContainerName(),
                                                serverInstanceKey,
                                                new ArrayList<Message>(),
                                                containerSpec.getReleasedId(),
                                                serverInstanceKey.getUrl() + "/containers/" + containerSpec.getId()));
            }
            serverSetup.setContainers(containers);

            // server configuration
            KieServerConfig serverConfig = new KieServerConfig();
            for (Map.Entry<Capability, ServerConfig> entry : serverTemplate.getConfigs().entrySet()) {

                KieServerConfigItem configItem = new KieServerConfigItem();

                ServerConfig config = entry.getValue();
                // currently ServerConfig does not have data...
                //configItem.setName();
                //configItem.setValue();
                // type of the config item is capability
                configItem.setType(entry.getKey().toString());

                serverConfig.addConfigItem(configItem);
            }

            serverSetup.setServerConfig(serverConfig);
        } else {
            logger.debug("Server id {} unknown to this controller, registering...", serverInfo.getServerId());
            serverTemplate = new ServerTemplate();
            serverTemplate.setId(serverInfo.getServerId());
            serverTemplate.setName(serverInfo.getName());
            List<String> capabilities = new ArrayList<String>();
            if (serverInfo.getCapabilities() != null) {
                for (final String serverCapability : serverInfo.getCapabilities()) {
                    if (SERVER_INSTANCE_TO_SERVER_TEMPLATE_CAPABILITIES.containsKey(serverCapability)) {
                        capabilities.add(SERVER_INSTANCE_TO_SERVER_TEMPLATE_CAPABILITIES.get(serverCapability));
                    }
                }
            }
            serverTemplate.setCapabilities(capabilities);

            serverTemplate.setMode(serverInfo.getMode());

            // add newly connected server instance
            serverTemplate.addServerInstance(serverInstanceKey);

            logger.debug("KieServerInstance stored after connect (register) from server {}", serverInfo.getLocation());
            templateStorage.store(serverTemplate);

            notificationService.notify(new ServerTemplateUpdated(serverTemplate));
        }

        logger.info("Server {} connected to controller", serverInfo.getLocation());

        ServerInstance serverInstance = new ServerInstance();
        serverInstance.setServerName(serverInstanceKey.getServerName());
        serverInstance.setServerTemplateId(serverInstanceKey.getServerTemplateId());
        serverInstance.setServerInstanceId(serverInstanceKey.getServerInstanceId());
        serverInstance.setUrl(serverInstanceKey.getUrl());

        serverInstance.setContainers(containerList);

        notifyOnConnect(serverInstance);
        return serverSetup;
    }

    private boolean checkValidServerInstance(ServerTemplate serverTemplate, KieServerInfo serverInfo, KieServerSetup serverSetup) {
        // if there is no mode we go to the next step
        if (serverTemplate.getMode() != null && !serverInfo.getMode().equals(serverTemplate.getMode())) {
            serverSetup.getMessages().add(new Message(Severity.ERROR, "Expected mode was " + serverTemplate.getMode()));
            logger.warn("Server id {} mode expected {} but it was {}", serverInfo.getServerId(), serverTemplate.getMode(), serverInfo.getMode());
        }

        // not required capabilities so any server is ok
        if (serverTemplate.getCapabilities() != null && !serverTemplate.getCapabilities().isEmpty()) {
            List<String> currentCapabilities = serverInfo.getCapabilities() != null ? serverInfo.getCapabilities() : Collections.emptyList();
            List<String> expectedCababilities = new ArrayList<>(serverTemplate.getCapabilities());

            List<String> convertedCurrentCapabilities = currentCapabilities.stream()
                    .filter(currentCapability -> SERVER_INSTANCE_TO_SERVER_TEMPLATE_CAPABILITIES.containsKey(currentCapability))
                    .map(currentCapability -> SERVER_INSTANCE_TO_SERVER_TEMPLATE_CAPABILITIES.get(currentCapability))
                    .collect(Collectors.toList());


            if (!Objects.equals(expectedCababilities, convertedCurrentCapabilities)) {
                serverSetup.getMessages().add(new Message(Severity.ERROR, "Expected capabilities were " + serverTemplate.getCapabilities()));
                List<String> missingCapabilities = expectedCababilities;
                missingCapabilities.removeAll(convertedCurrentCapabilities);
                logger.warn("Server id {} capabilities expected {} but there was missing {}",
                            serverInfo.getServerId(),
                            serverTemplate.getCapabilities(),
                            missingCapabilities);
            }
        }

        return serverSetup.hasNoErrors();
    }

    @Override
    public synchronized void disconnect(KieServerInfo serverInfo) {
        String id = serverInfo.getServerId();
        ServerTemplate serverTemplate = templateStorage.load(id);
        ServerInstanceKey serverInstanceKey = new ServerInstanceKey(id, id, id, "");
        if (serverTemplate != null) {
            logger.debug("Server id {} known to the controller, checking if given server exists", serverInfo.getServerId());
            if (!isKieServerLocationAvailable(serverInfo)) {
                serverInstanceKey = ModelFactory.newServerInstanceKey(id, serverInfo.getLocation());
                serverTemplate.deleteServerInstance(serverInstanceKey.getServerInstanceId());
                templateStorage.update(serverTemplate);
            }
            logger.info("Server {} disconnected from controller", serverInstanceKey);

            notifyOnDisconnect(serverInstanceKey, serverTemplate);
        }
    }

    private boolean isKieServerLocationAvailable(KieServerInfo serverInfo) {
        return serverInfo.getLocation() == null || serverInfo.getLocation().trim().length() == 0;
    }

    protected void notifyOnConnect(ServerInstance serverInstance) {
        notificationService.notify(new ServerInstanceUpdated(serverInstance));
        notificationService.notify(new ServerInstanceConnected(serverInstance));
    }

    protected void notifyOnDisconnect(ServerInstanceKey serverInstanceKey, ServerTemplate serverTemplate) {
        notificationService.notify(new ServerInstanceDeleted(serverInstanceKey.getServerInstanceId()));
        notificationService.notify(new ServerTemplateUpdated(serverTemplate));
        notificationService.notify(new ServerInstanceDisconnected(serverInstanceKey.getServerInstanceId()));
    }

    public KieServerTemplateStorage getTemplateStorage() {
        return templateStorage;
    }

    public void setTemplateStorage(KieServerTemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
