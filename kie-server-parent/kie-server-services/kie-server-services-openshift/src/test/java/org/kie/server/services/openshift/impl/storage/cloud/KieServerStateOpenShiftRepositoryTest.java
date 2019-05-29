/**
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.openshift.impl.storage.cloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.thoughtworks.xstream.XStream;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static org.kie.server.api.KieServerConstants.*;
import static org.kie.server.controller.api.KieServerControllerConstants.*;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_APP_NAME_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_ID_KEY;
import static org.kie.server.services.openshift.impl.storage.cloud.KieServerStateCloudRepository.initializeXStream;

public abstract class KieServerStateOpenShiftRepositoryTest {

    protected static final String KIE_SERVER_STARTUP_IN_PROGRESS_KEY_PREFIX = "org.kie.server.services/";
    protected static final String KIE_SERVER_STARTUP_IN_PROGRESS_VALUE = "kie.server.startup_in_progress";

    // Must match the the kie server id specified at test file
    protected static final String TEST_KIE_SERVER_ID = "myapp2-kieserver";
    protected static final String TEST_APP_NAME = "myapp2";
    protected static XStream xs = initializeXStream();
    protected static Supplier<OpenShiftClient> clouldClientHelper = () -> (new CloudClientFactory() {
    }).createOpenShiftClient();

    /**
     *  Must match current project name associated with OpenShift login
     *  if test against real OCP/K8S cluster
     */
    // The default namespace for MockKubernetes Server is 'test'
    protected String testNamespace = "test";
    protected OpenShiftClient client;
    protected KieServerStateOpenShiftRepository repo;

    @Rule
    public OpenShiftServer server = new OpenShiftServer(false, true);

    @Before
    public void setup() {

        /**
         *  Get fabric8 client and connect to real OpenShift/Kubernetes server
         *  Require to set the following environment properties to run:
         *  	KUBERNETES_MASTER
         *  	KUBERNETES_AUTH_TOKEN
         *  	KIE_SERVER_ID (Value doesn't matter)
         */
        if (System.getenv("KIE_SERVER_ID") != null) {
            System.setProperty(KIE_SERVER_STARTUP_STRATEGY, "OpenShiftStartupStrategy");
            // If KIE_SERVER_ID is set, connect to real OCP/K8S server
            client = clouldClientHelper.get();
        } else {
            // Get client from MockKubernetes Server
            client = server.getOpenshiftClient();
        }

        // Create cloud repository instance with mock K8S server test client
        repo = new KieServerStateOpenShiftRepository() {

            @Override
            public OpenShiftClient createOpenShiftClient() {
                return client;
            }

            @Override
            public KubernetesClient createKubernetesClient() {
                return client;
            }

            @Override
            public boolean isKieServerReady() {
                return true;
            }
            
            @Override
            public boolean isDCStable(DeploymentConfig dc) {
                return true;
            }
            
            @Override
            public Optional<String> getAppNameFromPod(OpenShiftClient client) {
                return Optional.of(TEST_APP_NAME);
            }
            
            @Override
            public ConfigMap createOrReplaceCM(OpenShiftClient client, ConfigMap cm) {
                // Issue workaround: MockKubenetes Server ignores update 
                client.configMaps().inNamespace(testNamespace).delete(cm);
                return client.configMaps().inNamespace(testNamespace).createOrReplace(cm);
            }
        };

        // Load testing KieServerState ConfigMap data into mock server from file
        ConfigMap cfm = client.configMaps()
                              .load(KieServerStateOpenShiftRepositoryTest.class
                              .getResourceAsStream("/test-kieserver-state-config-map-used.yml")).get();

        repo.createOrReplaceCM(client, cfm);
        repo.load(TEST_KIE_SERVER_ID);
    }

    @After
    public void tearDown() {
        System.clearProperty(KIE_SERVER_ID);
        System.clearProperty(KIE_SERVER_LOCATION);
        System.clearProperty(KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED);
        client.configMaps().inNamespace(testNamespace).delete();
        client.deploymentConfigs().inNamespace(testNamespace).delete();
        client.close();
    }
    
    protected void createDummyDCandRC() {
        createDummyDCandRC(TEST_KIE_SERVER_ID, UUID.randomUUID().toString(), 1);
    }

    protected void createDummyDCandRC(String kieServerID, String kieServerDCUID, int replicas) {
        createDummyDCandRC(UUID.randomUUID().toString(), kieServerID, kieServerDCUID, replicas);
    }
    protected void createDummyDCandRC(String name, String kieServerID, String kieServerDCUID, int replicas) {
        Map<String, String> labels = new HashMap<>();
        labels.put(CFG_MAP_LABEL_APP_NAME_KEY, TEST_APP_NAME);
        labels.put(CFG_MAP_LABEL_SERVER_ID_KEY, kieServerID);
        
        DeploymentConfig dc = client.deploymentConfigs().inNamespace(testNamespace)
                                .createOrReplaceWithNew()
                                .withNewMetadata()
                                  .withName(name)
                                  .withLabels(labels)
                                  .withUid(kieServerDCUID)
                                .endMetadata()
                                .withNewSpec()
                                  .withReplicas(replicas)
                                  .addNewTrigger()
                                    .withType("ConfigChange")
                                  .endTrigger()
                                  .addToSelector("app", "kieserver")
                                  .withNewTemplate()
                                    .withNewMetadata()
                                      .addToLabels("app", "kieserver")
                                      .addToLabels(CFG_MAP_LABEL_SERVER_ID_KEY,kieServerID)
                                    .endMetadata()
                                    .withNewSpec()
                                      .addNewContainer()
                                        .withName("kieserver")
                                        .withImage("kieserver")
                                        .addNewPort()
                                          .withContainerPort(80)
                                        .endPort()
                                      .endContainer()
                                    .endSpec()
                                  .endTemplate()
                                .endSpec()
                                .done();
        
        ReplicationController rc = client.replicationControllers().inNamespace(testNamespace)
                                    .createOrReplaceWithNew()
                                    .withNewMetadata()
                                      .withName("decoupled-with-kie-server-id")
                                      .withUid(kieServerDCUID)
                                      .addNewOwnerReference()
                                        .withApiVersion(dc.getApiVersion())
                                        .withKind(dc.getKind())
                                        .withName(dc.getMetadata().getName())
                                        .withUid(dc.getMetadata().getUid())
                                      .endOwnerReference()
                                    .endMetadata()
                                    .withNewSpec()
                                    .withReplicas(0)
                                    .addToSelector("app", "kieserver")
                                    .withNewTemplate()
                                      .withNewMetadata()
                                        .addToLabels("app", "kieserver")
                                        .addToLabels(CFG_MAP_LABEL_SERVER_ID_KEY,kieServerID)
                                      .endMetadata()
                                      .withNewSpec()
                                        .addNewContainer()
                                          .withName("kieserver")
                                          .withImage("kieserver")
                                          .addNewPort()
                                            .withContainerPort(80)
                                          .endPort()
                                        .endContainer()
                                      .endSpec()
                                    .endTemplate()
                                  .endSpec()
                                  .done();

        client.pods().inNamespace(testNamespace)
            .createOrReplaceWithNew()
            .withNewMetadata()
              .withName("decoupled-with-kie-server-id")
              .addToLabels("app", "kieserver")
              .addToLabels(CFG_MAP_LABEL_SERVER_ID_KEY,kieServerID)
              .addNewOwnerReference()
                .withApiVersion(rc.getApiVersion())
                .withKind(rc.getKind())
                .withName(rc.getMetadata().getName())
                .withUid(rc.getMetadata().getUid())
              .endOwnerReference()
            .endMetadata()
            .withNewSpec()
              .addNewContainer()
                .withName("kieserver")
                .withImage("kieserver")
                .addNewPort()
                  .withContainerPort(80)
                .endPort()
              .endContainer()
            .endSpec()
            .done();
        
    }
}
