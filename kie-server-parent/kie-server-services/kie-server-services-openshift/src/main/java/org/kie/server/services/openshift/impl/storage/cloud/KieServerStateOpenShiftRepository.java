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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirementBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepositoryUtils;
import org.kie.server.services.openshift.api.KieServerOpenShift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.*;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_STATE_IMMUTABLE_INIT;
import static org.kie.server.controller.api.KieServerControllerConstants.KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_DATA_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_APP_NAME_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_ID_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_USED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.UNKNOWN;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_NAME_SYNTHETIC_NAME;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.ENV_HOSTNAME;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.ROLLOUT_REQUIRED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.STATE_CHANGE_TIMESTAMP;

public class KieServerStateOpenShiftRepository extends KieServerStateCloudRepository implements KieServerOpenShift {

    private static final Logger logger = LoggerFactory.getLogger(KieServerStateOpenShiftRepository.class);

    public synchronized void create(KieServerState kieServerState) {
        String serverId = retrieveKieServerId(kieServerState);
        processKieServerStateByOpenShift(client -> {
            if (getKieServerCM(client, serverId).isPresent()) {
                logger.info("Create new ConfigMap action ignored. ConfigMap for KieServer [{}] exists.", serverId);
            } else {
                createOrReplaceKieServerStateCM(client, serverId, kieServerState);
            }
            return null;
        });
    }

    public List<String> retrieveAllKieServerIds() {
        return processKieServerStateByOpenShift(client ->
            client.configMaps().withLabelSelector(getKieServerCMLabelSelector(client))
                         .list()
                         .getItems()
                         .stream()
                         .map(cfg -> cfg.getMetadata().getLabels().get(CFG_MAP_LABEL_SERVER_ID_KEY))
                         .collect(Collectors.toList())
        );
    }

    public List<KieServerState> retrieveAllKieServerStates() {
        return processKieServerStateByOpenShift(client ->
            client.configMaps().withLabelSelector(getKieServerCMLabelSelector(client))
                         .list()
                         .getItems()
                         .stream()
                         .map(this::getKieServerState)
                         .collect(Collectors.toList())
        );
    }

    public boolean exists(String id) {
        return processKieServerStateByOpenShift(client -> getKieServerCM(client, id).isPresent());
    }

    public KieServerState delete(String id) {
        KieServerState state = load(id);
        boolean isUnsupported = processKieServerStateByOpenShift(client -> {
            if (getKieServerDC(client, id).isPresent()) {
                return true;
            } else {
                client.configMaps().withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, id).delete();
                return false;
            }
        });

        if (isUnsupported) {
            logger.error("Can not delete attached KieServerState with id [{}].", id);
            throw new UnsupportedOperationException("Can not delete attached KieServerState with id [" + id + "]");
        }
        return state;
    }

    @Override
    public synchronized void store(String serverId, KieServerState kieServerState) {
        if (!retrieveKieServerId(kieServerState).equals(serverId)) {
            throw new IllegalArgumentException("Invalid KieServerId: Id does not match with KieServerState.");
        }

        processKieServerStateByOpenShift(client -> {
            Optional<ConfigMap> cmOpt = getKieServerCM(client, serverId);

            if (Boolean.parseBoolean(kieServerState.getConfiguration()
                                                   .getConfigItemValue(KIE_SERVER_STATE_IMMUTABLE, Boolean.FALSE.toString()))) {
                if (Boolean.parseBoolean(System.getProperty(KIE_SERVER_STATE_IMMUTABLE_INIT, Boolean.FALSE.toString()))) {
                    // Provide compatibility to KieServerStateFileInit
                    createOrReplaceKieServerStateCM(client, serverId, kieServerState);
                } else {
                    logger.debug("Overwrite immutable KieServer[id:{}] state is not allowed.", serverId);
                }
            } else {
                ConfigMap cm = cmOpt.orElseThrow(() -> 
                    new IllegalStateException("KieServerState ConfigMap must exist before update."));
                if (isServerStateUpdateAllowed(client, serverId, cm, kieServerState)) {
                    ObjectMeta md = cm.getMetadata();
                    Map<String, String> ann = md.getAnnotations() == null ? new ConcurrentHashMap<>() : md.getAnnotations();
                    md.setAnnotations(ann);
                    ann.put(STATE_CHANGE_TIMESTAMP,
                            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
                    ann.put(ROLLOUT_REQUIRED, "true");
                    cm.setData(Collections.singletonMap(CFG_MAP_DATA_KEY, xs.toXML(kieServerState)));
                    createOrReplaceCM(client, cm);
                }
            }
            return null;
        });
    }
    
    @Override
    public KieServerState load(String serverId) {
        if (serverId == null) {
            return null;
        }
        KieServerState kieServerState = processKieServerStateByOpenShift(client -> {
            Optional<ConfigMap> cmOpt = getKieServerCM(client, serverId);
            if (isKieServerRuntime() && getKieServerDC(client, serverId).isPresent()) {
                ConfigMap kieCM = cmOpt.filter(cm -> !cm.getMetadata()
                                                        .getLabels()
                                                        .containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED))
                                       .orElseGet(() -> {
                                           // Merge Detached KieServerCM when KIE Pod initializes
                                           KieServerState state = cmOpt.map(this::getKieServerState)
                                                                       .orElseGet(KieServerState::new);
                                           KieServerConfig config = Optional.ofNullable(state.getConfiguration())
                                                                            .orElseGet(() -> {
                                                                                state.setConfiguration(new KieServerConfig());
                                                                                return state.getConfiguration();
                                                                            });
                                           // Create or update KieServer ConfigMap with values from System properties during KIE server init
                                           KieServerStateRepositoryUtils.populateWithSystemProperties(config);
                                           if (config.getConfigItemValue(KIE_SERVER_ID) == null ||
                                               !config.getConfigItemValue(KIE_SERVER_ID).equals(serverId)) {
                                               throw new IllegalStateException(("KieServerId: [" + serverId +
                                                                                "], must NOT be null and be set by system property or environment varible."));
                                           }
                                           return createOrReplaceKieServerStateCM(client, serverId, state);
                                       });
                return getKieServerState(kieCM);
            } else if (cmOpt.isPresent()) {
                return getKieServerState(cmOpt.get());
            } else {
                return null;
            }
        });
    
        if (kieServerState == null) {
            if (isKieServerRuntime()) {
                throw new IllegalStateException("Invalid KieServerId: [" + serverId +
                                                "], load kie server state failed.");
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
    
    public Optional<String> getAppName(OpenShiftClient client, String serverId) {
        Optional<String> appNameFromDC = getKieServerDC(client, serverId)
                .map(dc -> dc.getMetadata().getLabels().get(CFG_MAP_LABEL_APP_NAME_KEY));
        return appNameFromDC.isPresent() ? appNameFromDC : getAppNameFromPod(client);
    }
    
    public Optional<String> getAppNameFromPod(OpenShiftClient client) {
        Optional<Pod> pod = Optional.ofNullable(client.pods().withName(System.getenv(ENV_HOSTNAME)).get());
        return pod.map(p -> p.getMetadata().getLabels().get(CFG_MAP_LABEL_APP_NAME_KEY));
    }

    public LabelSelector getKieServerCMLabelSelector(OpenShiftClient client) {
        List<LabelSelectorRequirement> selectorReqs = new ArrayList<>();
        Optional<String> appName = getAppNameFromPod(client);
        selectorReqs.add(new LabelSelectorRequirementBuilder()
                         .withKey(CFG_MAP_LABEL_SERVER_STATE_KEY)
                         .withOperator("In")
                         .withValues(CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE, 
                                     CFG_MAP_LABEL_SERVER_STATE_VALUE_USED,
                                     CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED)
                         .build());
        if (!isKieServerGlobalDiscoveryEnabled() && appName.isPresent()) {
            selectorReqs.add(new LabelSelectorRequirementBuilder()
                             .withKey(CFG_MAP_LABEL_APP_NAME_KEY)
                             .withOperator("In")
                             .withValues(appName.get())
                             .build());
        }
        return new LabelSelectorBuilder().withMatchExpressions(selectorReqs).build();
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

    public ConfigMap createOrReplaceKieServerStateCM(OpenShiftClient client, String serverId, KieServerState kieServerState) {
        Map<String, String> labels = new HashMap<>();
        Optional<String> appName = getAppName(client, serverId);
        Optional<DeploymentConfig> dcOpt = getKieServerDC(client, serverId);
        String cfgMapName = dcOpt.map(dc -> dc.getMetadata().getName()).orElseGet(() -> getKieServerCMSyntheticName(client));
        
        kieServerState.getConfiguration().addConfigItem(
            new KieServerConfigItem(KIE_SERVER_SERVICES_OPENSHIFT_SERVICE_NAME, cfgMapName, String.class.getName()));
        appName.ifPresent(aName -> labels.put(CFG_MAP_LABEL_APP_NAME_KEY, aName));
        labels.put(CFG_MAP_LABEL_SERVER_ID_KEY, serverId);
        if (Boolean.parseBoolean(kieServerState.getConfiguration()
                                 .getConfigItemValue(KIE_SERVER_STATE_IMMUTABLE, "false"))) {
            labels.put(CFG_MAP_LABEL_SERVER_STATE_KEY, CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE);
        } else if (kieServerState.getConfiguration().getConfigItem(KIE_SERVER_LOCATION) == null) {
            labels.put(CFG_MAP_LABEL_SERVER_STATE_KEY, CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED);
        } else {
            labels.put(CFG_MAP_LABEL_SERVER_STATE_KEY, CFG_MAP_LABEL_SERVER_STATE_VALUE_USED);
        }
        
        return dcOpt.map(dc -> createOrReplaceCM(client, new ConfigMapBuilder()
                                                   .withNewMetadata()
                                                     .withName(cfgMapName)
                                                     .withLabels(labels)
                                                     .withOwnerReferences(new OwnerReferenceBuilder()
                                                                          .withApiVersion(dc.getApiVersion())
                                                                          .withKind(dc.getKind())                     
                                                                          .withName(dc.getMetadata().getName())
                                                                          .withUid(dc.getMetadata().getUid())
                                                                          .build())     
                                                   .endMetadata()
                                                   .withData(Collections.singletonMap(CFG_MAP_DATA_KEY, xs.toXML(kieServerState)))
                                                   .build()))
                    .orElseGet(() -> createOrReplaceCM(client, new ConfigMapBuilder()
                                       .withNewMetadata()
                                         .withName(cfgMapName)
                                         .withLabels(labels)
                                       .endMetadata()
                                       .withData(Collections.singletonMap(CFG_MAP_DATA_KEY, xs.toXML(kieServerState)))
                                       .build()));
    }
    
    public ConfigMap createOrReplaceCM(OpenShiftClient client, ConfigMap cm) {
        return client.configMaps().createOrReplace(cm);
    }
    
    private KieServerState getKieServerState(ConfigMap cm) {
        // Hide 'org.kie.server.location' for detached KIE server CM
        return Optional.of(cm).filter(kieCM -> kieCM.getMetadata().getLabels()
                                                                  .containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED))
                              .map(this::getKieServerStateFromCMWithoutLocation)
                              .orElseGet(() -> getKieServerStateFromCM(cm));
     }
    
    private KieServerState getKieServerStateFromCMWithoutLocation(ConfigMap cm) {
        KieServerState state = (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
        state.getConfiguration().removeConfigItem(state.getConfiguration().getConfigItem(KIE_SERVER_LOCATION));
        return state;
    }
    
    private KieServerState getKieServerStateFromCM(ConfigMap cm) {
        return (KieServerState) xs.fromXML(cm.getData().get(CFG_MAP_DATA_KEY));
    }

    private String getKieServerCMSyntheticName(OpenShiftClient client) {
        String appName = getAppNameFromPod(client).orElse(UNKNOWN);
        int index = client.configMaps().withLabelSelector(getKieServerCMLabelSelector(client))
                          .list().getItems().size() + 1;
        return new StringBuilder(appName).append("-").append(CFG_MAP_NAME_SYNTHETIC_NAME).append("-").append(index).toString();
    }

    private boolean isServerStateUpdateAllowed(OpenShiftClient client, String serverId, ConfigMap cm, KieServerState newState) {
        if (cm.getMetadata().getLabels() == null) {
            return false;
        }
        if (cm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_DETACHED)) {
            return !isKieServerRuntime();
        }
        if (cm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_USED)) {
            if (isKieServerRuntime()) {
                return isUpdateByKieServerProcessAllowed(client, serverId, cm, newState);
            } else {
                return isUpdateByNonKieServerProcessAllowed(client, serverId, cm, newState);
            }
        }
        if (cm.getMetadata().getLabels().containsValue(CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE)) {
            logger.warn("Add or remove kie container not allowed for immutable kie server: {}",
                        cm.getMetadata().getLabels().get(CFG_MAP_LABEL_SERVER_ID_KEY));
        }
        return false;
    }
    
    private boolean isUpdateByNonKieServerProcessAllowed(OpenShiftClient client, String serverId, ConfigMap cm, KieServerState newState) {
        Optional<DeploymentConfig> dcOpt = getKieServerDC(client, serverId);
        if (dcOpt.isPresent()) {
            if (isDCStable(dcOpt.get())) {
                logger.debug("Non KieServer process updated KieServerState.");
                return true;
            } else {
                return isKieContainerUpdateDuringRolloutAllowed(cm, newState);
            }
        } else {
            return true;
        }
    }

    private boolean isUpdateByKieServerProcessAllowed(OpenShiftClient client, String serverId, ConfigMap cm, KieServerState newState) {
        DeploymentConfig dc = getKieServerDC(client, serverId).orElseThrow(IllegalStateException::new);
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
        return System.getProperty(KIE_SERVER_ID) != null;
    }
    
    /**
     * 
     * @return false as default value if associated system property does not exist.
     */
    private boolean isKieServerGlobalDiscoveryEnabled() {
        return Boolean.parseBoolean(System.getProperty(KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED, "false"));
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
