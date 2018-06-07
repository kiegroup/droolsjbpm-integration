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

package org.kie.server.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.KieServerControllerIllegalArgumentException;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.impl.client.KieServicesClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerInstanceManager {

    private static final Logger logger = LoggerFactory.getLogger(KieServerInstanceManager.class);
    private static final String CONTAINERS_URI_PART = "/containers/";
    private static KieServerInstanceManager INSTANCE = new KieServerInstanceManager();
    private List<KieServicesClientProvider> clientProviders = new ArrayList<>();

    public KieServerInstanceManager() {
        ServiceLoader<KieServicesClientProvider> loader = ServiceLoader.load(KieServicesClientProvider.class);

        loader.forEach(provider -> clientProviders.add(provider));

        clientProviders.sort((KieServicesClientProvider one, KieServicesClientProvider two) -> one.getPriority().compareTo(two.getPriority()));
    }

    public static KieServerInstanceManager getInstance() {
        return INSTANCE;
    }

    public List<Container> startScanner(ServerTemplate serverTemplate,
                                        final ContainerSpec containerSpec,
                                        final long interval) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            new RemoteKieServerOperation<Void>() {
                                                @Override
                                                public Void doOperation(KieServicesClient client,
                                                                        Container container) {
                                                    KieScannerResource scannerResource = new KieScannerResource();
                                                    scannerResource.setPollInterval(interval);
                                                    scannerResource.setStatus(KieScannerStatus.STARTED);

                                                    ServiceResponse<KieScannerResource> response = client.updateScanner(containerSpec.getId(),
                                                                                                                        scannerResource);
                                                    if (!response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                                                        logger.debug("Scanner failed to start on server instance {} due to {}",
                                                                     container.getUrl(),
                                                                     response.getMsg());
                                                    }
                                                    collectContainerInfo(containerSpec,
                                                                         client,
                                                                         container);

                                                    return null;
                                                }
                                            });
    }

    public List<Container> stopScanner(ServerTemplate serverTemplate,
                                       final ContainerSpec containerSpec) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            new RemoteKieServerOperation<Void>() {
                                                @Override
                                                public Void doOperation(KieServicesClient client,
                                                                        Container container) {
                                                    KieScannerResource scannerResource = new KieScannerResource();
                                                    scannerResource.setPollInterval(null);
                                                    scannerResource.setStatus(KieScannerStatus.STOPPED);

                                                    ServiceResponse<KieScannerResource> response = client.updateScanner(containerSpec.getId(),
                                                                                                                        scannerResource);
                                                    if (!response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                                                        logger.debug("Scanner failed to stop on server instance {} due to {}",
                                                                     container.getUrl(),
                                                                     response.getMsg());
                                                    }

                                                    collectContainerInfo(containerSpec,
                                                                         client,
                                                                         container);

                                                    return null;
                                                }
                                            });
    }

    public List<Container> scanNow(ServerTemplate serverTemplate,
                                   final ContainerSpec containerSpec) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            new RemoteKieServerOperation<Void>() {
                                                @Override
                                                public Void doOperation(KieServicesClient client,
                                                                        Container container) {
                                                    KieScannerResource scannerResource = new KieScannerResource();
                                                    scannerResource.setPollInterval(null);
                                                    scannerResource.setStatus(KieScannerStatus.SCANNING);

                                                    ServiceResponse<KieScannerResource> response = client.updateScanner(containerSpec.getId(),
                                                                                                                        scannerResource);
                                                    if (!response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                                                        logger.debug("Scanner (scan now) failed on server instance {} due to {}",
                                                                     container.getUrl(),
                                                                     response.getMsg());
                                                    }
                                                    collectContainerInfo(containerSpec,
                                                                         client,
                                                                         container);
                                                    return null;
                                                }
                                            });
    }

    public synchronized List<Container> startContainer(final ServerTemplate serverTemplate,
                                                       final ContainerSpec containerSpec) {

        final RemoteKieServerOperation<Void> startContainerOperation = makeStartContainerOperation(containerSpec);

        return callRemoteKieServerOperation(serverTemplate, containerSpec, startContainerOperation);
    }

    RemoteKieServerOperation<Void> makeStartContainerOperation(final ContainerSpec containerSpec) {
        return new RemoteKieServerOperation<Void>() {
            @Override
            public Void doOperation(final KieServicesClient client,
                                    final Container container) {

                final KieContainerResource resource = makeContainerResource(container, containerSpec);
                final ServiceResponse<KieContainerResource> response = client.createContainer(containerSpec.getId(), resource);

                if (response.getType() != ServiceResponse.ResponseType.SUCCESS) {
                    log("Container {} failed to start on server instance {} due to {}", container, response, containerSpec);
                }

                collectContainerInfo(containerSpec, client, container);

                return null;
            }
        };
    }

    void log(final String message,
             final Object... objects) {
        logger.debug(message, objects);
    }

    KieContainerResource makeContainerResource(final Container container,
                                               final ContainerSpec containerSpec) {

        final KieContainerResource containerResource = new KieContainerResource(containerSpec.getId(),
                                                                                containerSpec.getReleasedId(),
                                                                                container.getResolvedReleasedId(),
                                                                                container.getStatus());

        containerResource.setContainerAlias(containerSpec.getContainerName());
        containerResource.setMessages((List<Message>) container.getMessages());

        if (containerSpec.getConfigs() != null) {
            setRuleConfigAttributes(containerSpec, containerResource);
            setProcessConfigAttributes(containerSpec, containerResource);
        }

        return containerResource;
    }

    KieServerConfigItem makeKieServerConfigItem(final String type,
                                                final String name,
                                                final String value) {

        final KieServerConfigItem configItem = new KieServerConfigItem();

        configItem.setType(type);
        configItem.setName(name);
        configItem.setValue(value);

        return configItem;
    }

    void setRuleConfigAttributes(final ContainerSpec containerSpec,
                                 final KieContainerResource containerResource) {

        final ContainerConfig containerConfig = containerSpec.getConfigs().get(Capability.RULE);

        if (containerConfig != null) {

            final RuleConfig ruleConfig = (RuleConfig) containerConfig;
            final KieScannerResource scannerResource = new KieScannerResource();

            scannerResource.setPollInterval(ruleConfig.getPollInterval());
            scannerResource.setStatus(ruleConfig.getScannerStatus());

            containerResource.setScanner(scannerResource);
        }
    }

    void setProcessConfigAttributes(final ContainerSpec containerSpec,
                                    final KieContainerResource containerResource) {

        final ContainerConfig containerConfig = containerSpec.getConfigs().get(Capability.PROCESS);

        if (containerConfig != null) {

            final ProcessConfig processConfig = (ProcessConfig) containerConfig;

            containerResource.addConfigItem(makeKieServerConfigItem(KieServerConstants.CAPABILITY_BPM,
                                                                    KieServerConstants.PCFG_KIE_BASE,
                                                                    processConfig.getKBase()));

            containerResource.addConfigItem(makeKieServerConfigItem(KieServerConstants.CAPABILITY_BPM,
                                                                    KieServerConstants.PCFG_KIE_SESSION,
                                                                    processConfig.getKSession()));

            containerResource.addConfigItem(makeKieServerConfigItem(KieServerConstants.CAPABILITY_BPM,
                                                                    KieServerConstants.PCFG_MERGE_MODE,
                                                                    processConfig.getMergeMode()));

            containerResource.addConfigItem(makeKieServerConfigItem(KieServerConstants.CAPABILITY_BPM,
                                                                    KieServerConstants.PCFG_RUNTIME_STRATEGY,
                                                                    processConfig.getRuntimeStrategy()));
        }
    }

    public synchronized List<Container> stopContainer(ServerTemplate serverTemplate,
                                                      final ContainerSpec containerSpec) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            new RemoteKieServerOperation<Void>() {
                                                @Override
                                                public Void doOperation(KieServicesClient client,
                                                                        Container container) {

                                                    ServiceResponse<Void> response = client.disposeContainer(containerSpec.getId());
                                                    if (!response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                                                        logger.debug("Container {} failed to stop on server instance {} due to {}",
                                                                     containerSpec.getId(),
                                                                     container.getUrl(),
                                                                     response.getMsg());
                                                    }
                                                    collectContainerInfo(containerSpec,
                                                                         client,
                                                                         container);
                                                    return null;
                                                }
                                            });
    }

    public List<Container> upgradeContainer(final ServerTemplate serverTemplate,
                                            final ContainerSpec containerSpec) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            makeUpgradeContainerOperation(containerSpec));
    }

    public List<Container> upgradeAndStartContainer(final ServerTemplate serverTemplate,
                                                    final ContainerSpec containerSpec) {

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            makeUpgradeAndStartContainerOperation(containerSpec));
    }

    RemoteKieServerOperation<Void> makeUpgradeContainerOperation(final ContainerSpec containerSpec) {
        return new RemoteKieServerOperation<Void>() {
            @Override
            public Void doOperation(final KieServicesClient client,
                                    final Container container) {

                remoteUpgradeContainer(client, container, containerSpec);

                return null;
            }
        };
    }

    RemoteKieServerOperation<Void> makeUpgradeAndStartContainerOperation(final ContainerSpec containerSpec) {
        return new RemoteKieServerOperation<Void>() {
            @Override
            public Void doOperation(final KieServicesClient client,
                                    final Container container) {

                final KieContainerResource containerResource = makeContainerResource(container, containerSpec);

                remoteCreateContainer(client, containerResource, containerSpec);
                remoteUpgradeContainer(client, container, containerSpec);

                return null;
            }
        };
    }

    private void remoteCreateContainer(final KieServicesClient client, final KieContainerResource containerResource, final ContainerSpec containerSpec) {
        client.createContainer(containerSpec.getId(), containerResource);
    }

    private void remoteUpgradeContainer(final KieServicesClient client, final Container container, final ContainerSpec containerSpec) {
        final ServiceResponse<ReleaseId> response = client.updateReleaseId(containerSpec.getId(), containerSpec.getReleasedId());

        if (response.getType() != ServiceResponse.ResponseType.SUCCESS) {
            log("Container {} failed to upgrade on server instance {} due to {}", containerSpec.getId(), container.getUrl(), response.getMsg());
        }

        collectContainerInfo(containerSpec, client, container);
    }

    public List<Container> getContainers(final ServerTemplate serverTemplate,
                                         final ContainerSpec containerSpec) {

        RemoteKieServerOperation<Void> operation = getContainersRemoteOperation(serverTemplate,
                                                                                containerSpec);

        return callRemoteKieServerOperation(serverTemplate,
                                            containerSpec,
                                            operation);
    }

    RemoteKieServerOperation<Void> getContainersRemoteOperation(final ServerTemplate serverTemplate,
                                                                final ContainerSpec containerSpec) {
        return new RemoteKieServerOperation<Void>() {
            @Override
            public Void doOperation(KieServicesClient client,
                                    Container container) {

                final ServiceResponse<KieContainerResource> response = client.getContainerInfo(containerSpec.getId());
                final KieContainerResource containerResource = response.getResult();

                if (response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                    container.setContainerSpecId(containerResource.getContainerId());
                    container.setContainerName(containerResource.getContainerAlias());
                    container.setResolvedReleasedId(containerResource.getResolvedReleaseId() == null ? containerResource.getReleaseId() : containerResource.getResolvedReleaseId());
                    container.setServerTemplateId(serverTemplate.getId());
                    container.setStatus(containerResource.getStatus());
                    container.setMessages(containerResource.getMessages());
                }

                return null;
            }
        };
    }

    public List<Container> getContainers(ServerInstanceKey serverInstanceKey) {

        List<Container> containers = new ArrayList<>();
        if (serverInstanceKey == null || serverInstanceKey.getUrl() == null) {
            return containers;
        }
        try {
            KieServicesClient client = getClient(serverInstanceKey.getUrl());

            ServiceResponse<KieContainerResourceList> response = client.listContainers();

            if (response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                KieContainerResourceList resourceList = response.getResult();

                for (KieContainerResource containerResource : resourceList.getContainers()) {

                    Container container = new Container();
                    container.setContainerSpecId(containerResource.getContainerId());
                    container.setContainerName(containerResource.getContainerAlias());
                    container.setServerInstanceId(serverInstanceKey.getServerInstanceId());
                    container.setUrl(serverInstanceKey.getUrl() + CONTAINERS_URI_PART + containerResource.getContainerId());
                    container.setResolvedReleasedId(containerResource.getResolvedReleaseId() == null ? containerResource.getReleaseId() : containerResource.getResolvedReleaseId());
                    container.setServerTemplateId(serverInstanceKey.getServerTemplateId());
                    container.setStatus(containerResource.getStatus());
                    container.setMessages(containerResource.getMessages());

                    containers.add(container);
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to get list of containers from remote server at url {} due to {}",
                        serverInstanceKey.getUrl(),
                        e.getMessage());
            throw e;
        }
        return containers;
    }


    /*
     * helper methods
     */

    protected List<Container> callRemoteKieServerOperation(ServerTemplate serverTemplate,
                                                           ContainerSpec containerSpec,
                                                           RemoteKieServerOperation operation) {
        List<Container> containers = new ArrayList<org.kie.server.controller.api.model.runtime.Container>();

        if (serverTemplate.getServerInstanceKeys() == null || serverTemplate.getServerInstanceKeys().isEmpty() || containerSpec == null) {
            return containers;
        }

        for (ServerInstanceKey instanceUrl : serverTemplate.getServerInstanceKeys()) {

            Container container = new Container();
            container.setContainerSpecId(containerSpec.getId());
            container.setServerTemplateId(serverTemplate.getId());
            container.setServerInstanceId(instanceUrl.getServerInstanceId());
            container.setUrl(instanceUrl.getUrl() + "/containers/" + containerSpec.getId());
            container.setStatus(containerSpec.getStatus());

            try {
                final KieServicesClient client = getClient(instanceUrl.getUrl());
                operation.doOperation(client, container);
                containers.add(container);
            } catch (Exception e) {
                logger.debug("Unable to connect to {}",
                             instanceUrl);
            }
        }

        return containers;
    }

    public boolean isAlive(ServerInstanceKey serverInstanceKey) {
        boolean alive = false;
        try {
            // get client will internally call serverinfo
            getClient(serverInstanceKey.getUrl());
            alive = true;
        } catch (Exception e) {
            logger.debug("Unable to connect to server instance at {} due to {}",
                         serverInstanceKey.getUrl(),
                         e.getMessage());
        }
        return alive;
    }

    protected KieServicesClient getClient(final String url) {
        KieServicesClientProvider clientProvider = clientProviders.stream().filter(provider -> provider.supports(url)).findFirst().orElseThrow(() -> new KieServerControllerIllegalArgumentException("Kie Services Client Provider not found for url: " + url));
        logger.debug("Using client provider {}", clientProvider);
        KieServicesClient client = clientProvider.get(url);
        logger.debug("Using client {}", client);
        if(client == null){
            throw new KieServerControllerIllegalArgumentException("Kie Services Client not found for url: " + url);
        }
        return client;
    }

    protected void collectContainerInfo(ContainerSpec containerSpec,
                                        KieServicesClient client,
                                        Container container) {
        // collect up to date information
        ServiceResponse<KieContainerResource> serviceResponse = client.getContainerInfo(containerSpec.getId());
        if (serviceResponse.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
            KieContainerResource containerResource = serviceResponse.getResult();
            container.setResolvedReleasedId(containerResource.getResolvedReleaseId() == null ? containerResource.getReleaseId() : containerResource.getResolvedReleaseId());
            container.setMessages(containerResource.getMessages());
        }
    }

    protected class RemoteKieServerOperation<T> {

        public T doOperation(KieServicesClient client,
                             Container container) {

            return null;
        }
    }
}
