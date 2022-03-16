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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.KieServerControllerConstants;
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

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();

    private NotificationService notificationService = LoggingNotificationService.getInstance();

    @Override
    public synchronized KieServerSetup connect(KieServerInfo serverInfo) {
        boolean isKieServerLocationAvailable = isKieServerLocationAvailable(serverInfo);
        String serverId = serverInfo.getServerId();
        ServerTemplate serverTemplate = templateStorage.load(serverId);
        KieServerSetup serverSetup = new KieServerSetup();
        ServerInstanceKey serverInstanceKey = null;

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

            serverSetup = toKieServerSetup(serverTemplate);
        } else {
            logger.debug("Server id {} unknown to this controller, registering...", serverInfo.getServerId());
            serverTemplate = new ServerTemplate();
            serverTemplate.setId(serverInfo.getServerId());
            serverTemplate.setName(serverInfo.getName());
            List<String> capabilities = new ArrayList<String>();
            if (serverInfo.getCapabilities() != null) {
                for (final String serverCapability : serverInfo.getCapabilities()) {
                    if (getCompatibleServerTemplateCapability(serverCapability) != null) {
                        capabilities.add(getCompatibleServerTemplateCapability(serverCapability));
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
        List<Container> containerList = new ArrayList<Container>();
        for(ContainerSpec containerSpec : serverTemplate.getContainersSpec()) {
            containerList.add(new Container(containerSpec.getId(),
                                            containerSpec.getContainerName(),
                                            serverInstanceKey,
                                            new ArrayList<Message>(),
                                            containerSpec.getReleasedId(),
                                            serverInstanceKey.getUrl() + "/containers/" + containerSpec.getId()));
        }

        ServerInstance serverInstance = new ServerInstance();
        serverInstance.setServerName(serverInstanceKey.getServerName());
        serverInstance.setServerTemplateId(serverInstanceKey.getServerTemplateId());
        serverInstance.setServerInstanceId(serverInstanceKey.getServerInstanceId());
        serverInstance.setUrl(serverInstanceKey.getUrl());
        serverInstance.setContainers(containerList);

        notifyOnConnect(serverInstance);
        return serverSetup;
    }

    @Override
    public KieServerSetup update(KieServerStateInfo kieServerStateInfo) {
        String serverId = kieServerStateInfo.getServerId();
        ServerTemplate serverTemplate = templateStorage.load(serverId);
        ServerInstanceKey serverInstanceKey = ModelFactory.newServerInstanceKey(kieServerStateInfo.getServerId(), kieServerStateInfo.getLocation());
        if (serverTemplate == null) {
            logger.info("Server id {} unknown to this controller, state update will create the template", kieServerStateInfo.getServerId());
            return new KieServerSetup();
        }


        ServerInstance serverInstance = toServerInstance(serverInstanceKey, serverTemplate);
        // we update and notify
        notificationService.notify(new ServerInstanceUpdated(serverInstance));

        for(ContainerSpec currentSpec : serverTemplate.getContainersSpec()) {
            List<Container> specContainerList = new ArrayList<Container>();
            for(ServerInstanceKey currentServerInstanceKey : serverTemplate.getServerInstanceKeys()) {
               Container container = new Container(currentSpec.getId(),
                                                currentSpec.getContainerName(),
                                                currentServerInstanceKey,
                                                new ArrayList<Message>(),
                                                currentSpec.getReleasedId(),
                                                currentServerInstanceKey.getUrl() + "/containers/" + currentSpec.getId());
               container.setStatus(currentSpec.getStatus());
               specContainerList.add(container);
            }
            notificationService.notify(serverTemplate, currentSpec, specContainerList);
        }


        return toKieServerSetup(serverTemplate);
    }

    private ServerInstance toServerInstance(ServerInstanceKey serverInstanceKey, ServerTemplate serverTemplate) {
        // we update the server instance with the containers
        List<Container> containerList = new ArrayList<Container>();
        List<KieContainerStatus> invalidStatus = Collections.singletonList(KieContainerStatus.STOPPED);
        for(ContainerSpec containerSpec : serverTemplate.getContainersSpec()) {
            if(invalidStatus.contains(containerSpec.getStatus())) {
                continue;
            }
            Container container = new Container(containerSpec.getId(),
                          containerSpec.getContainerName(),
                          serverInstanceKey,
                          new ArrayList<Message>(),
                          containerSpec.getReleasedId(),
                          serverInstanceKey.getUrl() + "/containers/" + containerSpec.getId());
            container.setStatus(containerSpec.getStatus());
            containerList.add(container);
        }

        ServerInstance serverInstance = new ServerInstance();
        serverInstance.setServerName(serverInstanceKey.getServerName());
        serverInstance.setServerTemplateId(serverInstanceKey.getServerTemplateId());
        serverInstance.setServerInstanceId(serverInstanceKey.getServerInstanceId());
        serverInstance.setUrl(serverInstanceKey.getUrl());
        serverInstance.setContainers(containerList);
        return serverInstance;
    }

    private KieServerSetup toKieServerSetup(ServerTemplate serverTemplate) {
        KieServerSetup serverSetup = new KieServerSetup();
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
                computeProcessConfigItems(processConfig).forEach(e -> containerResource.addConfigItem(e));
            }

            containers.add(containerResource);

        }
        serverSetup.setContainers(containers);

        // server configuration
        KieServerConfig serverConfig = new KieServerConfig();
        for (Map.Entry<Capability, ServerConfig> entry : serverTemplate.getConfigs().entrySet()) {

            KieServerConfigItem configItem = new KieServerConfigItem();
            configItem.setType(entry.getKey().toString());

            serverConfig.addConfigItem(configItem);
        }

        serverSetup.setServerConfig(serverConfig);
        return serverSetup;
    }
    

    private List<KieServerConfigItem> computeProcessConfigItems(ProcessConfig processConfig) {
        List<KieServerConfigItem> items = new ArrayList<>();

        KieServerConfigItem configItem = new KieServerConfigItem();
        configItem.setType(KieServerConstants.CAPABILITY_BPM);
        configItem.setName(KieServerConstants.PCFG_KIE_BASE);
        configItem.setValue(processConfig.getKBase());

        items.add(configItem);

        configItem = new KieServerConfigItem();
        configItem.setType(KieServerConstants.CAPABILITY_BPM);
        configItem.setName(KieServerConstants.PCFG_KIE_SESSION);
        configItem.setValue(processConfig.getKSession());

        items.add(configItem);

        configItem = new KieServerConfigItem();
        configItem.setType(KieServerConstants.CAPABILITY_BPM);
        configItem.setName(KieServerConstants.PCFG_MERGE_MODE);
        configItem.setValue(processConfig.getMergeMode());

        items.add(configItem);

        configItem = new KieServerConfigItem();
        configItem.setType(KieServerConstants.CAPABILITY_BPM);
        configItem.setName(KieServerConstants.PCFG_RUNTIME_STRATEGY);
        configItem.setValue(processConfig.getRuntimeStrategy());
        items.add(configItem);
        return items;
    }

    
    private boolean isOpenShiftSupported() {
        return "true".equals(System.getProperty(KieServerControllerConstants.KIE_CONTROLLER_OPENSHIFT_ENABLED, "false"));
    }
    
    private boolean checkValidServerInstance(ServerTemplate serverTemplate, KieServerInfo serverInfo, KieServerSetup serverSetup) {
        // No need for OpenShift enhanced BC/WB and KieServer
        if (isOpenShiftSupported()) {
            return serverSetup.hasNoErrors();
        }
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
                    .filter(currentCapability -> getCompatibleServerTemplateCapability(currentCapability) != null)
                    .map(currentCapability -> getCompatibleServerTemplateCapability(currentCapability))
                    .collect(Collectors.toList());

            if (!convertedCurrentCapabilities.containsAll(expectedCababilities)) {
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


    public void markOnlineAs(KieServerInfo serverInfo, boolean online) {
        String id = serverInfo.getServerId();
        ServerTemplate serverTemplate = templateStorage.load(id);
        ServerInstanceKey serverInstanceKey = new ServerInstanceKey(id, id, id, "");
        if (serverTemplate != null) {
            logger.debug("Server id {} known to the controller, checking if given server exists", serverInfo.getServerId());
            if (!isKieServerLocationAvailable(serverInfo)) {
                serverInstanceKey = ModelFactory.newServerInstanceKey(id, serverInfo.getLocation());
                if(serverTemplate.markAsOnline(serverInstanceKey.getServerInstanceId(), online)) {
                    serverInstanceKey.setOnline(online);
                    templateStorage.update(serverTemplate);
                    logger.info("Server {} is marked as {} from controller", serverInstanceKey, online ? "online" : "offline");
                    if(online) {
                        ServerInstance serverInstance = toServerInstance(serverInstanceKey, serverTemplate);
                        notifyOnConnect(serverInstance);
                    } else {
                        notifyOnDisconnect(serverInstanceKey, serverTemplate);
                    }
                }
            }
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

    private static String getCompatibleServerTemplateCapability(final String serverInstanceCapability) {

        if (Objects.equals(KieServerConstants.CAPABILITY_BRM, serverInstanceCapability)) {
            return Capability.RULE.toString();
        }
        if (Objects.equals(KieServerConstants.CAPABILITY_BPM, serverInstanceCapability)) {
            return Capability.PROCESS.toString();
        }
        if (Objects.equals(KieServerConstants.CAPABILITY_BRP, serverInstanceCapability)) {
            return Capability.PLANNING.toString();
        }

        return null;
    }
}
