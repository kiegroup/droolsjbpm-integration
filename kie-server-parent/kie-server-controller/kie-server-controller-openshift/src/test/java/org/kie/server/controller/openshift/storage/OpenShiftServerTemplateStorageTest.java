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

package org.kie.server.controller.openshift.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.impl.storage.ServerTemplateStorageTest;
import org.kie.server.services.openshift.impl.storage.cloud.CloudClientFactory;
import org.kie.server.services.openshift.impl.storage.cloud.KieServerStateOpenShiftRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OpenShiftServerTemplateStorageTest extends ServerTemplateStorageTest {

    private static Supplier<OpenShiftClient> clouldClientHelper = () -> (new CloudClientFactory() {
    }).createOpenShiftClient();

    /**
     *  Must match current project name associated with OpenShift login
     *  if test against real OCP/K8S cluster
     */
    private String testNamespace = "test";
    private OpenShiftClient client;
    
    protected static final String TEST_APP_NAME = "myapp2";

    @Rule
    public OpenShiftServer server = new OpenShiftServer(false, true);

    @Before
    public void setup() throws IOException {
        /**
         *  Get fabric8 client and connect to real OpenShift/Kubernetes server
         *  Require to set the following environment properties to run:
         *      KUBERNETES_MASTER
         *      KUBERNETES_AUTH_TOKEN
         *      KIE_SERVER_ID (Value doesn't matter)
         */
        if (System.getenv("KIE_SERVER_ID") != null) {
            System.setProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY, "OpenShiftStartupStrategy");
            // If KIE_SERVER_ID is set, connect to real OCP/K8S server
            client = clouldClientHelper.get();
        } else {
            // Get client from MockKubernetes Server
            client = server.getOpenshiftClient();
        }

        // Create cloud repository instance with mock K8S server test client
        KieServerStateOpenShiftRepository repo = new KieServerStateOpenShiftRepository() {

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

        templateStorage = new OpenShiftServerTemplateStorage(repo);
        createServerTemplateWithContainer();
    }

    @Override
    protected void checkTemplateInstances(ServerTemplate fromStorage) {
        Collection<ServerInstanceKey> instances = fromStorage.getServerInstanceKeys();
        assertNotNull(instances);

        //OpenShift based ServerTemplate contains one and only one ServerInstance
        assertEquals(1, instances.size());
    }

    @Override
    protected boolean isEnclosedTestAssertionRequired() {
        return false;
    }
    
    @Override
    protected void setServerTemplateId() {
        serverTemplate.setId(ServerTemplateConverterTest.TEST_KIE_SERVER_ID);
    }

    @Test
    public void testLoadKeysWithCache() {
        OpenShiftServerTemplateStorage ocpStorage = (OpenShiftServerTemplateStorage) templateStorage;
        ocpStorage.store(serverTemplate);
        assertEquals(1, ocpStorage.loadKeys().size());

        // Removed directly from KieServerStateOpenShiftRepository
        ocpStorage.repo.delete(serverTemplate.getId());

        // Still in the cache
        assertEquals(1, ocpStorage.loadKeys().size());
    }

    @Test
    public void testLoadKeysWithCacheExpire() {
        OpenShiftServerTemplateStorage ocpStorage = (OpenShiftServerTemplateStorage) templateStorage;

        // Set TTL to 10 milliseconds
        ocpStorage.cacheTTL = 10;

        ocpStorage.store(serverTemplate);
        assertEquals(1, ocpStorage.loadKeys().size());

        // Removed directly from KieServerStateOpenShiftRepository
        ocpStorage.repo.delete(serverTemplate.getId());

        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // Cache should be expired
        assertEquals(0, ocpStorage.loadKeys().size());
    }

    @Test
    public void testLoadWithCache() {
        OpenShiftServerTemplateStorage ocpStorage = (OpenShiftServerTemplateStorage) templateStorage;
        ocpStorage.store(serverTemplate);
        assertEquals(1, ocpStorage.load().size());

        // Removed directly from KieServerStateOpenShiftRepository
        ocpStorage.repo.delete(serverTemplate.getId());

        // Still in the cache
        assertEquals(1, ocpStorage.load().size());
    }

    @After
    public void cleanup() {
        client.configMaps().inNamespace(testNamespace).delete();
        client.close();
    }
}
