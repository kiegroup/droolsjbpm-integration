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

package org.kie.server.services.openshift.api;

import java.util.List;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.server.services.impl.KieServerLocator;

import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_ID_KEY;

public interface KieServerOpenShift {

    default boolean isKieServerReady() {
        return KieServerLocator.getInstance().isKieServerReady();
    }

    default boolean isDeploymentStable(Deployment deployment) {
        return deployment.getStatus() != null
                && deployment.getStatus().getConditions() != null
                && !deployment.getStatus().getConditions().isEmpty()
                && "True".equalsIgnoreCase(deployment.getStatus().getConditions().get(0).getStatus())
                && (deployment.getStatus().getUnavailableReplicas() == null
                    || deployment.getStatus().getUnavailableReplicas() == 0);
    }

    default boolean isDCStable(DeploymentConfig dc) {
        return "True".equalsIgnoreCase(dc.getStatus().getConditions().get(0).getStatus())
                && dc.getStatus().getUnavailableReplicas() == 0;
    }

    default Optional<Deployment> getKieServerDeployment(OpenShiftClient client, String serverId) {
        List<Deployment> deployments = client.apps().deployments().inNamespace(client.getNamespace())
                .withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, serverId).list().getItems();
        if (deployments.isEmpty()) { return Optional.empty();}
        if (deployments.size() == 1) {
            return deployments.stream().filter(d -> d.getSpec().getReplicas().intValue() > 0).findFirst();
        }
        throw new IllegalStateException("Ambiguous KIE server id: [" + serverId +
                                        "]; more than one KIE server Deployment exists.");
    }

    default Optional<DeploymentConfig> getKieServerDC(OpenShiftClient client, String serverId) {
        List<DeploymentConfig> deployments = client.deploymentConfigs().inNamespace(client.getNamespace())
                .withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, serverId).list().getItems();
        if (deployments.isEmpty()) { return Optional.empty();}
        if (deployments.size() == 1) {
            return deployments.stream().filter(dc -> dc.getSpec().getReplicas().intValue() > 0).findFirst();
        }
        throw new IllegalStateException("Ambiguous KIE server id: [" + serverId +
                                        "]; more than one KIE server DeploymentConfig exists.");
    }
    
    default Optional<ConfigMap> getKieServerCM(OpenShiftClient client, String serverId) {
        List<ConfigMap> configMaps = client.configMaps().inNamespace(client.getNamespace())
                .withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, serverId).list().getItems();
        if (configMaps.isEmpty()) { return Optional.empty();}
        if (configMaps.size() == 1) { return Optional.ofNullable(configMaps.get(0)); }
        throw new IllegalStateException("Ambiguous KIE server id: [" + serverId + 
                                        "]; more than one KIE server ConfigMaps exists.");
    }

}
