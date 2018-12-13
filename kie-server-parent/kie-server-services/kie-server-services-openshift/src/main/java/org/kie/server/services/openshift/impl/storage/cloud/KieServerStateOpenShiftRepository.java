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

package org.kie.server.services.openshift.impl.storage.cloud;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE_INIT;

public class KieServerStateOpenShiftRepository extends KieServerStateCloudRepository {

    private static final Logger logger = LoggerFactory.getLogger(KieServerStateOpenShiftRepository.class);

    public synchronized void create(@NotNull KieServerState kieServerState) {
        String serverId = retrieveKieServerId(kieServerState);
        processKieServerStateByOpenShift(client -> {
            ConfigMap cm = client.configMaps().withName(serverId).get();
            if (cm != null) {
                logger.info("Create new ConfigMap action ignored. ConfigMap for KieServer [{}] exists.", serverId);
                return null;
            }
            createNewKieServerStateCM(kieServerState, serverId, client);
            return null;
        });
    }

    public List<String> retrieveAllKieServerIds() {
        return processKieServerStateByOpenShift(client ->
            client.configMaps().withLabelIn(KieServerStateCloudRepository.CFG_MAP_LABEL_NAME,
                                                   KieServerStateCloudRepository.CFG_MAP_LABEL_VALUE_IMMUTABLE,
                                                   KieServerStateCloudRepository.CFG_MAP_LABEL_VALUE_USED)
                         .list()
                         .getItems()
                         .stream()
                         .map(cfg -> cfg.getMetadata().getName())
                         .collect(Collectors.toList())
        );
    }

    public List<KieServerState> retrieveAllKieServerStates() {
        return processKieServerStateByOpenShift(client ->
            client.configMaps().withLabelIn(KieServerStateCloudRepository.CFG_MAP_LABEL_NAME,
                                                   KieServerStateCloudRepository.CFG_MAP_LABEL_VALUE_IMMUTABLE,
                                                   KieServerStateCloudRepository.CFG_MAP_LABEL_VALUE_USED)
                         .list()
                         .getItems()
                         .stream()
                         .map(cfg -> (KieServerState) xs.fromXML(cfg.getData().get(CFG_MAP_DATA_KEY)))
                         .collect(Collectors.toList())
        );
    }

    public boolean exists(String id) {
        return processKieServerStateByOpenShift(client -> client.configMaps().withName(id).get() != null);
    }

    public KieServerState delete(String id) {
        KieServerState state = load(id);
        boolean isUnsupported = processKieServerStateByOpenShift(client -> {
            if (client.deploymentConfigs().withName(id).get() == null) {
                client.configMaps().withName(id).delete();
            } else {
                return true;
            }
            return false;
        });

        if (isUnsupported) {
            logger.error("Can not delete attached KieServerState with id [{}].", id);
            throw new UnsupportedOperationException("Can not delete attached KieServerState with id [" + id + "]");
        }
        return state;
    }

    @Override
    public synchronized void store(@NotNull String serverId, @NotNull KieServerState kieServerState) {
        if (!retrieveKieServerId(kieServerState).equals(serverId)) {
            throw new IllegalArgumentException("Invalid KieServerId: Id does not match with KieServerState.");
        }

        processKieServerStateByOpenShift(client -> {
            DeploymentConfig dc = client.deploymentConfigs().withName(serverId).get();
            ConfigMap cm = client.configMaps().withName(serverId).get();

            if (Boolean.parseBoolean(kieServerState.getConfiguration()
                                                   .getConfigItemValue(KIE_SERVER_STATE_IMMUTABLE, Boolean.FALSE.toString()))) {
                if (Boolean.parseBoolean(System.getProperty(KIE_SERVER_STATE_IMMUTABLE_INIT, Boolean.FALSE.toString()))) {
                    // Provide compatibility to KieServerStateFileInit
                    createNewKieServerStateCM(kieServerState, serverId, client);
                } else {
                    logger.debug("Overwrite immutable KieServer, id:{}, state is not allow.", serverId);
                }
            } else if (isServerStateUpdateAllowed(dc, cm, kieServerState)) {
                ObjectMeta md = cm.getMetadata();
                Map<String, String> ann = md.getAnnotations() == null ? new ConcurrentHashMap<>() : md.getAnnotations();
                md.setAnnotations(ann);
                ann.put(STATE_CHANGE_TIMESTAMP,
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
                ann.put(ROLLOUT_REQUIRED, "true");
                cm.setData(Collections.singletonMap(CFG_MAP_DATA_KEY, xs.toXML(kieServerState)));
                client.configMaps().createOrReplace(cm);
            }
            return null;
        });
    }

    @Override
    public KieServerState load(@NotNull String serverId) {
        KieServerState kieServerState = processKieServerStateByOpenShift(client -> {
            ConfigMap cm = client.configMaps().withName(serverId).get();
            if (cm == null) {
                if (isKieServerRuntime()) {
                    // Create KieServer ConfigMap with values from System properties
                    KieServerState initkieServerState = new KieServerState();
                    KieServerConfig config = new KieServerConfig();
                    KieServerStateRepositoryUtils.populateWithSystemProperties(config);
                    initkieServerState.setConfiguration(config);

                    if (config.getConfigItemValue(KieServerConstants.KIE_SERVER_ID) == null ||
                        !config.getConfigItemValue(KieServerConstants.KIE_SERVER_ID).equals(serverId)) {
                        throw new IllegalStateException(("KieServerId: [" + serverId +
                                                         "], must NOT be null and be set by system property or environment varible."));
                    }
                    cm = createNewKieServerStateCM(initkieServerState, serverId, client);
                } else {
                    return null;
                }
            }
            return (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
        });
    
        if (kieServerState == null) {
            if (isKieServerRuntime()) {
                throw new IllegalStateException("Invalid KieServerId: [" + serverId +
                                                "], load kie server state failed.");
            } else {
                return null;
            }
        } else if (!retrieveKieServerId(kieServerState).equals(serverId)) {
            throw new IllegalStateException("Inconsistent kie server state data, " +
                                            "requested KieServerId: [" + serverId +
                                            "], whereas loaded KieServerId: [" + 
                                            retrieveKieServerId(kieServerState) + "]," +
                                            "from kie server state.");
        }

        return kieServerState;
    }
    
    @Override
    public boolean isKieServerReady() {
        return KieServerLocator.getInstance().isKieServerReady();
    }

    /**
     * To be compatible with non kieserver process, (workbench), kie container at certain status,
     * i.e. STOPPED, should not be removed.
     * @param dc
     * @param c
     * @param newState
     * @return
     */
    public boolean isKieContainerRemovalAllowed(ConfigMap cm, KieServerState newState) {
        KieServerState state = (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
        for (KieContainerResource container : state.getContainers()) {
            if (container.getStatus().equals(KieContainerStatus.STOPPED) &&
                newState.getContainers().stream()
                    .noneMatch(c -> c.getContainerId().equals(container.getContainerId()))) {
                // STOPPED kie container not found
                logger.warn("Removing STOPPED KieContainer by KieServer process is not allowed.");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Non KieServer process including workbench is allowed to update KieServerState
     * even during DC rollout, but ONLY under certain conditions.
     * @param cm
     * @param newState
     * @return
     */
    public boolean isKieContainerUpdateDuringRolloutAllowed(ConfigMap cm, KieServerState newState) {
        KieServerState state = (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
        for (KieContainerResource container : state.getContainers()) {
            if (container.getStatus().equals(KieContainerStatus.STARTED) &&
                newState.getContainers().stream()
                        .anyMatch(c -> c.getContainerId().equals(container.getContainerId()) &&
                                       c.getStatus().equals(KieContainerStatus.STOPPED))) {
                logger.warn("Non KieServer process updated KieServerState during DC rollout for STOPPING containers.");
                return true;
            }
        }
        logger.warn("Non KieServer process updates KieServerState during DC rollout is prohibited!");
        return false;
    }

    protected ConfigMap createNewKieServerStateCM(KieServerState kieServerState, String serverId, OpenShiftClient client) {
        String stateXML = xs.toXML(kieServerState);
        Map<String, String> labels = null;
        if (Boolean.parseBoolean(kieServerState.getConfiguration()
                                 .getConfigItemValue(KieServerConstants.KIE_SERVER_STATE_IMMUTABLE, "false"))) {
            labels = Collections.singletonMap(CFG_MAP_LABEL_NAME, CFG_MAP_LABEL_VALUE_IMMUTABLE);
        } else {    
            labels = Collections.singletonMap(CFG_MAP_LABEL_NAME, CFG_MAP_LABEL_VALUE_USED);
        }
        return client.configMaps().createOrReplace(new ConfigMapBuilder()
                                   .withNewMetadata()
                                     .withName(serverId)
                                     .withLabels(labels)
                                   .endMetadata()
                                   .withData(Collections.singletonMap(CFG_MAP_DATA_KEY, stateXML))
                                   .build());
    }

    protected boolean isServerStateUpdateAllowed(DeploymentConfig dc, ConfigMap cm, KieServerState newState) {
        if (cm == null) {
            throw new IllegalStateException("KieServerState ConfigMap must exist before update.");
        }
        if (cm.getMetadata().getLabels() == null) {
            return false;
        }
        if (cm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_VALUE_USED)) {
            if (isKieServerRuntime()) {
                return isUpdateByKieServerProcessAllowed(dc, cm, newState);
            } else {
                return isUpdateByNonKieServerProcessAllowed(dc, cm, newState);
            }
        }
        if (cm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_VALUE_IMMUTABLE)) {
            logger.warn("Add or remove kie container not allowed for immutable kie server: {}",
                        cm.getMetadata().getName());
        }
        return false;
    }
    
    protected boolean isUpdateByNonKieServerProcessAllowed(DeploymentConfig dc, ConfigMap cm, KieServerState newState) {
        if (isDCStable(dc)) {
            logger.debug("Non KieServer process updated KieServerState.");
            return true;
        } else {
            return isKieContainerUpdateDuringRolloutAllowed(cm, newState);
        }
    }

    protected boolean isUpdateByKieServerProcessAllowed(DeploymentConfig dc, ConfigMap cm, KieServerState newState) {
        if (!isKieContainerRemovalAllowed(cm, newState)) {
            return false;
        }
        // Regular KieServerState updates and triggers rollout
        if (!isKieServerReady()) { 
            logger.debug("KieServerState updates during KieServer starting up is ignored!");
            return false;
        }
        if (!isDCStable(dc)) {
            logger.debug("KieServerState updates during DC rollout is ignored!");
            return false;
        } 
        logger.debug("KieServer process updated KieServerState.");
        return true;
    }
    
    /**
     * To provide compatibility to non kie server use case, such as supporting Workbench or 
     * standalone kie server controller, this utility method indicates if the runtime environment
     * is kie server or not.
     * @return
     */
    private boolean isKieServerRuntime() {
        return System.getProperty(KieServerConstants.KIE_SERVER_ID) != null;
    }

    private <R> R processKieServerStateByOpenShift(Function<OpenShiftClient, R> func) {
        R result = null;
        try (OpenShiftClient client = createOpenShiftClient()) {
            result = func.apply(client);
        } catch (UnsupportedOperationException uoe) {
            logger.error("Processing KieServerState failed - Unsupported", uoe);
            throw uoe;
        } catch (IllegalStateException ise) {
            logger.error("Processing KieServerState failed - Missing required configuration", ise);
            throw ise;
        } catch (Exception e) {
            logger.error("Processing KieServerState failed.", e);
        }
        return result;
    }

}
