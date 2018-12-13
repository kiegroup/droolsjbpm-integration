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
import org.kie.server.services.impl.storage.KieServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerStateOpenShiftRepository extends KieServerStateCloudRepository {

    private static final Logger logger = LoggerFactory.getLogger(KieServerStateOpenShiftRepository.class);

    public synchronized void create(@NotNull KieServerState kieServerState) {
        String serverId = retrieveKieServerId(kieServerState);
        processKieServerStateByOpenShift(client -> {
            ConfigMap cm = client.configMaps().withName(serverId).get();

            String stateXML = xs.toXML(kieServerState);
            if (cm != null) {
                logger.info("Create new ConfigMap action ignored. ConfigMap for KieServer [{}] exists.", serverId);
                return null;
            }

            client.configMaps().create(new ConfigMapBuilder()
                                       .withNewMetadata()
                                         .withName(serverId)
                                         .withLabels(Collections.singletonMap(CFG_MAP_LABEL_NAME, CFG_MAP_LABEL_VALUE))
                                       .endMetadata()
                                       .withData(Collections.singletonMap(CFG_MAP_DATA_KEY, stateXML))
                                       .build());
            return null;
        });
    }

    public List<String> retrieveAllKieServerIds() {
        return processKieServerStateByOpenShift(client -> {
            return client.configMaps().withLabel(CFG_MAP_LABEL_NAME, CFG_MAP_LABEL_VALUE).list().getItems()
                         .stream()
                         .map(cfg -> cfg.getMetadata().getName())
                         .collect(Collectors.toList());
        });
    }

    public List<KieServerState> retrieveAllKieServerStates() {
        return processKieServerStateByOpenShift(client -> {
            return client.configMaps().withLabel(CFG_MAP_LABEL_NAME, CFG_MAP_LABEL_VALUE).list().getItems()
                         .stream()
                         .map(cfg -> (KieServerState) xs.fromXML(cfg.getData().get(CFG_MAP_DATA_KEY)))
                         .collect(Collectors.toList());
        });
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
            String stateXML = xs.toXML(kieServerState);
            if (cm == null) {
                throw new IllegalStateException("KieServerState ConfigMap must exist before update.");
            } else {
                cm.setData(Collections.singletonMap(CFG_MAP_DATA_KEY, stateXML));
            }

            ObjectMeta md = cm.getMetadata();
            Map<String, String> ann = md.getAnnotations() == null ? new ConcurrentHashMap<>() : md.getAnnotations();
            md.setAnnotations(ann);
            ann.put(STATE_CHANGE_TIMESTAMP,
                    ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

            if (isKieServerReady()) {
                if (isDCStable(dc)) {
                    ann.put(ROLLOUT_REQUIRED, "true");
                    client.configMaps().createOrReplace(cm);
                } else {
                    throw new UnsupportedOperationException(
                            "Updating KieServerState is not supported if there is in-progress DeploymentConfig activities.");
                }
            }
            return null;
        });
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
