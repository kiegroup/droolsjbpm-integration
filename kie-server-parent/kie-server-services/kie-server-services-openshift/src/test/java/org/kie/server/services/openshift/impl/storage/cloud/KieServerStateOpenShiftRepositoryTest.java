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

import java.util.function.Supplier;

import com.thoughtworks.xstream.XStream;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.kie.server.api.KieServerConstants;

import static org.kie.server.services.openshift.impl.storage.cloud.KieServerStateCloudRepository.initializeXStream;

public class KieServerStateOpenShiftRepositoryTest {

    protected static final String KIE_SERVER_STARTUP_IN_PROGRESS_KEY_PREFIX = "org.kie.server.services/";
    protected static final String KIE_SERVER_STARTUP_IN_PROGRESS_VALUE = "kie.server.startup_in_progress";

    // Must match the the kie server id specified at test file
    protected static final String TEST_KIE_SERVER_ID = "myapp2-kieserver";
    protected static XStream xs = initializeXStream();
    protected static Supplier<OpenShiftClient> clouldClientHelper = () -> (new CloudClientFactory() {
    }).createOpenShiftClient();

    /**
     *  Must match current project name associated with OpenShift login
     *  if test against real OCP/K8S cluster
     */
    protected String testNamespace = "myproject";
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
            System.setProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY, "OpenShiftStartupStrategy");
            // If KIE_SERVER_ID is set, connect to real OCP/K8S server
            client = clouldClientHelper.get();
        } else {
            // Get client from MockKubernetes Server
            client = server.getOpenshiftClient();

            // The default namespace for MockKubernetes Server is 'test'
            testNamespace = "test";
        }

        // Load testing KieServerState ConfigMap data into mock server from file
        ConfigMap cfm = client.configMaps()
                              .load(KieServerStateOpenShiftRepositoryTest.class
                              .getResourceAsStream("/test-kieserver-state-config-map.yml")).get();

        client.configMaps().inNamespace(testNamespace).createOrReplace(cfm);

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
        };

        repo.load(TEST_KIE_SERVER_ID);
    }

    @After
    public void tearDown() {
        System.clearProperty(KieServerConstants.KIE_SERVER_ID);
        client.configMaps().inNamespace(testNamespace).delete();
        client.close();
    }

    protected void createDummyDC() {
        client.deploymentConfigs().inNamespace(testNamespace).createOrReplaceWithNew()
              .withNewMetadata()
                .withName(TEST_KIE_SERVER_ID)
              .endMetadata()
              .withNewSpec()
                .withReplicas(0)
                  .addNewTrigger()
                    .withType("ConfigChange")
                  .endTrigger()
                .withNewTemplate()
                  .withNewMetadata()
                    .addToLabels("app", "kieserver")
                  .endMetadata()
                  .withNewSpec()
                    .addNewContainer()
                      .withName("kieserver")
                      .withImage("kiserver")
                      .addNewPort()
                        .withContainerPort(80)
                      .endPort()
                    .endContainer()
                  .endSpec()
                .endTemplate()
              .endSpec()
              .done();
    }
}
