/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.kie.server.router.repository.ConfigFileWatcher;
import org.kie.server.router.repository.ConfigurationMarshaller;
import org.kie.server.router.spi.ConfigRepository;
import org.kie.server.router.utils.FailedHostInfo;
import org.kie.server.router.utils.HttpUtils;

/**
 * this class centralizes and makes atomics the operations against configuration and persistence avoid race conditions between read/write
 *
 */
public class ConfigurationManager {

    private static final Logger log = Logger.getLogger(ConfigurationManager.class);

    private KieServerRouterEnvironment environment;
    private ConfigRepository repository;
    private Configuration configuration;
    private ConfigurationMarshaller marshaller;
    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> failedHostsReconnects;
    private ScheduledFuture<?> addToControllerAttempts;
    private ScheduledFuture<?> removeFromControllerAttempts;

    private CopyOnWriteArrayList<FailedHostInfo> failedHosts = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<ContainerInfo> containersToAddToController = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> containersToRemoveFromController = new CopyOnWriteArrayList<>();

    private ConfigFileWatcher watcher;
    private CopyOnWriteArrayList<String> controllerContainers;

    public ConfigurationManager(KieServerRouterEnvironment environment, ConfigRepository repository, ScheduledExecutorService executorService) {
        this.marshaller = new ConfigurationMarshaller();
        this.environment = environment;
        this.repository = repository;
        this.executorService = executorService;
        this.configuration = repository.load();
        this.controllerContainers = new CopyOnWriteArrayList<>();
    }

    public void startWatcher () {
        if (this.watcher != null) {
            return;
        }
        this.watcher = new ConfigFileWatcher(this.environment, this);
        this.executorService.submit(this.watcher);
    }

    public void stopWatcher () {
        if (this.watcher == null) {
            return;
        }
        this.watcher.stop();
    }

    public String toJsonConfig() throws Exception {
        return marshaller.marshall(configuration);
    }

    public synchronized void persist() {
        repository.persist(this.configuration);
    }

    public synchronized void syncPersistent() {
        Configuration conf = repository.load();
        this.configuration.syncFromRepository(conf);
    }

    public synchronized void add(String containerId, String alias, String serverId, String serverUrl, String releaseId) {
        configuration.reloadFromRepository(repository.load());
        configuration.addContainerHost(containerId, serverUrl);
        configuration.addContainerHost(alias, serverUrl);
        configuration.addServerHost(serverId, serverUrl);
        configuration.addContainerInfo(new ContainerInfo(containerId, alias, releaseId));
        repository.persist(configuration);
        updateControllerOnAdd(containerId, releaseId, alias, new ContainerInfo(containerId, alias, releaseId));
    }

    public synchronized void remove(String containerId, String alias, String serverId, String serverUrl, String releaseId) {
        configuration.reloadFromRepository(repository.load());
        configuration.removeContainerHost(containerId, serverUrl);
        configuration.removeContainerHost(alias, serverUrl);
        configuration.removeServerHost(serverId, serverUrl);
        configuration.removeContainerInfo(new ContainerInfo(containerId, alias, releaseId));
        repository.persist(configuration);
        updateControllerOnRemove(containerId);
    }

    public synchronized Configuration getConfiguration() {
        return configuration;
    }

    public void addControllerContainers(List<String> containers) {
        controllerContainers.addAll(containers);
    }

    private KieServerRouterEnvironment environment() {
        return environment;
    }

    public  synchronized FailedHostInfo disconnectFailedHost(String url) {
        log.info("Server at " + url+ " is now offline");
        FailedHostInfo failedHost = configuration.removeUnavailableServer(url);
        failedHosts.add(failedHost);
        log.debug("Scheduling host checks...");
        long attemptInterval = environment().getKieControllerAttemptInterval();
        failedHostsReconnects = executorService.scheduleAtFixedRate(this::pingFailedHost, attemptInterval, attemptInterval, TimeUnit.SECONDS);
        repository.persist(configuration);
        return failedHost;
    }

    public synchronized void reconnectFailedHost(FailedHostInfo failedHostInfo) {
        log.info("Server at " + failedHostInfo.getServerUrl() + " is back online");
        configuration.reloadFromRepository(repository.load());
        for (String containerId : failedHostInfo.getContainers()) {
            configuration.addContainerHost(containerId, failedHostInfo.getServerUrl());
        }
        configuration.addServerHost(failedHostInfo.getServerId(), failedHostInfo.getServerUrl());
        repository.persist(configuration);
    }

    private void pingFailedHost() {
        if (failedHosts.isEmpty()) {
            return;
        }
        Iterator<FailedHostInfo> it = failedHosts.iterator();
        List<FailedHostInfo> toRemove = new ArrayList<>();
        while (it.hasNext()) {
            FailedHostInfo failedHost = it.next();
            if (environment().getKieControllerRecoveryAttemptLimit() == failedHost.getAttempts()) {
            	toRemove.add(failedHost);
                log.info("Host " + failedHost.getServerUrl() + " has reached reconnect attempts limit " + environment().getKieControllerRecoveryAttemptLimit() + " quiting");
                continue;
            }
            try {
                HttpUtils.getHttpCall(environment(), failedHost.getServerUrl());

                failedHostsReconnects.cancel(false);
                reconnectFailedHost(failedHost);
                toRemove.add(failedHost);
            } catch (Exception e) {
                log.debug("Host " + failedHost.getServerUrl() + " is still not available, attempting to reconnect in " + environment().getKieControllerAttemptInterval() + " seconds, error "
                        + e.getMessage());
            } finally {
                failedHost.attempted();
            }
        }
        failedHosts.removeAll(toRemove);
    }

    private void updateControllerOnRemove(String containerId) {
        if (environment().hasKieControllerUrl() && controllerContainers.contains(containerId)) {
            List<String> hostsPerContainer = configuration.getHostsPerContainer().getOrDefault(containerId, Collections.emptyList());
            if (hostsPerContainer.isEmpty()) {

                controllerContainers.remove(containerId);
                containersToRemoveFromController.add(containerId);
                if (removeFromControllerAttempts == null) {
                    removeFromControllerAttempts = executorService.scheduleAtFixedRate(() -> {

                        try {
                            List<String> sent = new ArrayList<>();
                            for (String container : containersToRemoveFromController) {

                                if (controllerContainers.contains(container)) {
                                    // skip given container if it's back in controllers containers                                            
                                    sent.add(container);
                                    continue;
                                }
                                dropFromController(container);
                                sent.add(container);
                            }
                            containersToRemoveFromController.removeAll(sent);
                            if (containersToRemoveFromController.isEmpty()) {
                                removeFromControllerAttempts.cancel(false);
                                removeFromControllerAttempts = null;
                            }
                        } catch (Exception e) {
                            log.warn("Exception when notifying controller about deleted containers " + e.getMessage() + " next attempt in " + environment().getKieControllerAttemptInterval()
                                    + " seconds");
                            log.debug(e);
                        }
                    },
                            environment().getKieControllerAttemptInterval(), environment().getKieControllerAttemptInterval(), TimeUnit.SECONDS);
                }
            }

        }
    }

    private void updateControllerOnAdd(String containerId, String releaseId, String alias, ContainerInfo containerInfo) {
        if (environment().hasKieControllerUrl() && releaseId != null && !controllerContainers.contains(containerId)) {

            controllerContainers.add(containerId);
            containersToAddToController.add(containerInfo);
            if (addToControllerAttempts == null) {
                addToControllerAttempts = executorService.scheduleAtFixedRate(() -> {

                    try {
                        List<ContainerInfo> sent = new ArrayList<>();
                        for (ContainerInfo container : containersToAddToController) {
                            if (!controllerContainers.contains(container.getContainerId())) {

                                sent.add(container);
                                continue;
                            }

                            pushToController(container.getReleaseId(), container.getContainerId(), container.getAlias());
                            sent.add(container);
                        }

                        containersToAddToController.removeAll(sent);
                        if (containersToAddToController.isEmpty()) {
                            addToControllerAttempts.cancel(false);
                            addToControllerAttempts = null;
                        }
                    } catch (Exception e) {
                        log.warn("Exception when notifying controller about deleted containers " + e.getMessage() + " next attempt in " + environment().getKieControllerAttemptInterval() + " seconds");
                        log.debug("Stacktrace", e);
                    }
                },
                        environment().getKieControllerAttemptInterval(), environment().getKieControllerAttemptInterval(), TimeUnit.SECONDS);
            }

        }
    }

    private static final String CONTAINER_SPEC_JSON = "{\n" +
            "    \"container-id\" : \"#1@\",\n" +
            "    \"container-name\" : \"#2@\",\n" +
            "    \"server-template-key\" : {\n" +
            "      \"server-id\" : \"kie-server-router\",\n" +
            "      \"server-name\" : \"KIE Server Router\"\n" +
            "    },\n" +
            "    \"release-id\" : {\n" +
            "      \"group-id\" : \"#3@\",\n" +
            "      \"artifact-id\" : \"#4@\",\n" +
            "      \"version\" : \"#5@\"\n" +
            "    },\n" +
            "    \"configuration\" : { },\n" +
            "    \"status\" : \"STARTED\"\n" +
            "  }";

    private void pushToController(String releaseId, String containerId, String alias) throws Exception {
        String[] gav = releaseId.split(":");
        String jsonPayload = CONTAINER_SPEC_JSON
                .replaceFirst("#1@", containerId)
                .replaceFirst("#2@", alias)
                .replaceFirst("#3@", gav[0])
                .replaceFirst("#4@", gav[1])
                .replaceFirst("#5@", gav[2]);
        HttpUtils.putHttpCall(environment(), environment().getKieControllerUrl() + "/management/servers/" + environment().getRouterId() + "/containers/" + containerId, jsonPayload);        
        log.infof("Added %s container into controller at %s ", containerId, environment().getKieControllerUrl());        
    }

    private void dropFromController(String containerId) throws Exception {
        HttpUtils.deleteHttpCall(environment(), environment().getKieControllerUrl() + "/management/servers/" + environment().getRouterId() + "/containers/" + containerId);
        
        log.infof("Removed %s container from controller at %s ", containerId, environment().getKieControllerUrl());
    }

    public void close() {
        if (this.watcher != null) {
            this.watcher.stop();
        }
    }

}
