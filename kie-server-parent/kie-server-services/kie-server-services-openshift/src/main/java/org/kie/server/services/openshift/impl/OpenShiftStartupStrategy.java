/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.openshift.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.StartupStrategy;
import org.kie.server.services.impl.ContainerManager;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.openshift.api.KieServerOpenShift;
import org.kie.server.services.openshift.impl.storage.cloud.CloudClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_ID_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.ROLLOUT_REQUIRED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.UNKNOWN;

public class OpenShiftStartupStrategy implements StartupStrategy {

    private static final String KIE_SERVER_ROLLOUT_IN_PROGRESS = "kieserver-rollout-in-progress";
    private static final String KIE_SERVER_INSTANCE_ID = "kieserver-" + UUID.randomUUID().toString();
    private static final Logger logger = LoggerFactory.getLogger(OpenShiftStartupStrategy.class);

    private static Supplier<OpenShiftClient> clouldClientHelper = () -> (new CloudClientFactory() {
    }).createOpenShiftClient();

    private static String namespace;

    private static class WatchRunner implements Runnable,
                                                KieServerOpenShift {

        private boolean isWatchRunning = true;
        private String kieServerId;
        private Thread currentThread;

        public WatchRunner(String serverId) {
            kieServerId = serverId;
        }

        @Override
        public void run() {
            try (OpenShiftClient client = clouldClientHelper.get()) {
                logger.info("Watching ConfigMap in namespace: [{}]", client.getNamespace());
                try (Watch watch = client.configMaps().inNamespace(namespace).withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, kieServerId).watch(new Watcher<ConfigMap>() {

                    @Override
                    public void eventReceived(Action action, ConfigMap kieServerState) {
                        logger.debug("Event - Action: {}, {} on ConfigMap ", action, kieServerState.getMetadata().getLabels().getOrDefault(CFG_MAP_LABEL_SERVER_ID_KEY, UNKNOWN));

                        getKieServerDC(client, kieServerId).ifPresent(dc -> {
                            if (action.equals(Action.MODIFIED) && isRolloutRequired(client, kieServerId, isDCStable(dc))) {
                                String dcName = dc.getMetadata().getName();
                                logger.info("Triggering rollout for DeploymentConfig: {}", dcName);
                                client.deploymentConfigs().inNamespace(namespace).withName(dcName).deployLatest();
                            } else {
                                logger.debug("Event - Ignored");
                            }
                        });
                    }

                    @Override
                    public void onClose(KubernetesClientException cause) {
                        if (cause != null && cause.getMessage().contains("too old resource version")) {
                            logger.debug("Watcher should not be closed: {}", cause.getMessage());
                            currentThread.interrupt();
                            startWatcherThread(kieServerId);
                        } else {
                            logger.debug("Watcher closed.");
                        }
                    }
                })) {

                    logger.debug("Watcher created");
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        synchronized (this) {
                            isWatchRunning = false;
                            notifyAll();
                            logger.info("ShutdownHook sent notifyAll");
                        }
                    }));

                    synchronized (this) {
                        while (isWatchRunning && !Thread.currentThread().isInterrupted()) {
                            logger.debug("WatchRunner thread run");
                            currentThread = Thread.currentThread();
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                if (!Thread.currentThread().isInterrupted()) {
                                    Thread.currentThread().interrupt();
                                }
                                logger.debug("WatchRunner thread being interrupted", e);
                            }
                            logger.debug("WatchRunner thread being notified");
                        }
                        logger.debug("WatchRunner thread exits");
                    }
                }
            } catch (KubernetesClientException kce) {
                logger.debug("WatchRunner thread failed", kce);
                logger.warn("WatchRunner thread failed, enable debug to see the stack trace");
            }
        }

        private boolean isRolloutRequired(OpenShiftClient client, String kieServerId, boolean isDCStable) {
            boolean pullTrigger = false;
            String triggerName = KIE_SERVER_ROLLOUT_IN_PROGRESS + "-" + kieServerId;
            ConfigMap cm = getKieServerCM(client, kieServerId).orElseThrow(IllegalStateException::new);
            Map<String, String> ann = cm.getMetadata().getAnnotations();

            if (ann != null && ann.containsKey(ROLLOUT_REQUIRED)) {
                try {
                    if (isDCStable) {
                        // Create temporary rollout-in-progress configmap only if there is no DC activities.
                        client.configMaps().inNamespace(namespace).create(new ConfigMapBuilder().withNewMetadata().withName(triggerName).withLabels(Collections.singletonMap(KIE_SERVER_INSTANCE_ID, kieServerId)).endMetadata().build());
                        pullTrigger = true;
                        logger.info("KieServer: {}, DC rollout - Begin", KIE_SERVER_INSTANCE_ID);
                    } else {
                        logger.info("KieServer: {}, DC rollout - In progress", KIE_SERVER_INSTANCE_ID);
                    }

                    /**
                     * Cleanup the configmap annotation related to rollout-in-progress
                     */
                    ann.remove(ROLLOUT_REQUIRED);
                    client.configMaps().inNamespace(namespace).createOrReplace(cm);
                } catch (KubernetesClientException kce) {
                    logger.debug("Mark DC rollout failed", kce);
                }
            }
            return pullTrigger;
        }
    }

    @Override
    public void startup(KieServerImpl kieServer, ContainerManager containerManager, KieServerState currentState, AtomicBoolean kieServerActive) {

        String kieServerId = currentState.getConfiguration().getConfigItemValue(KieServerConstants.KIE_SERVER_ID);

        try (OpenShiftClient client = clouldClientHelper.get()) {

            Set<KieContainerResource> containers = prepareContainers(currentState.getContainers());
            containerManager.installContainersSync(kieServer, containers, currentState, new KieServerSetup());

            namespace = client.getNamespace();
            startWatcherThread(kieServerId);

            clearRollout(client, kieServerId);
        }
    }

    private static void startWatcherThread(String kieServerId) {
        new Thread(new WatchRunner(kieServerId)).start();
        Thread.currentThread().setName("watcher-" + kieServerId);
    }

    private static void clearRollout(OpenShiftClient client, String kieServerId) {
        String triggerName = KIE_SERVER_ROLLOUT_IN_PROGRESS + "-" + kieServerId;

        // Cleanup the configmap related to rollout-in-progress if needed
        if (client.configMaps().inNamespace(namespace).withName(triggerName).delete()) {
            logger.info("Kie server: {}, DC rollout - End", KIE_SERVER_INSTANCE_ID);
        }
    }

    @Override
    public String getRepositoryType() {
        return KieServerConstants.KIE_SERVER_STATE_REPO_TYPE_OPENSHIFT;
    }

    @Override
    public String toString() {
        return "OpenShiftStartupStrategy - deploys only kie containers defined from OpenShift ConfigMap, ignores kie containers given by controller";
    }
}
