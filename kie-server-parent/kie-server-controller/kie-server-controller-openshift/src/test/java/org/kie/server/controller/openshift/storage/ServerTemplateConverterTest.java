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

import java.util.List;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.openshift.impl.storage.cloud.KieServerStateOpenShiftRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServerTemplateConverterTest {

    // Must match the the kie server id specified at test file
    public static final String TEST_KIE_SERVER_ID = "myapp2-kieserver";

    private String testNamespace = "myproject";
    private OpenShiftClient client;
    private KieServerStateOpenShiftRepository repo;

    @Rule
    public OpenShiftServer server = new OpenShiftServer(false, true);

    @Before
    public void setup() {
        // Get client from MockKubernetes Server
        client = server.getOpenshiftClient();

        // The default namespace for MockKubernetes Server is 'test'
        testNamespace = "test";

        // Load testing KieServerState ConfigMap data into mock server from file
        ConfigMap cfm = client.configMaps().load(
                                                 ServerTemplateConverterTest.class
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
        };

        repo.load(TEST_KIE_SERVER_ID);
    }

    @Test
    public void testFromState() {
        KieServerState state = repo.load(TEST_KIE_SERVER_ID);
        ServerTemplate template = ServerTemplateConverter.fromState(state);

        assertTrue(isMatching(template, state));
    }

    @Test
    public void testToState() {
        String tmpId = TEST_KIE_SERVER_ID + "-from-template";

        KieServerState state = repo.load(TEST_KIE_SERVER_ID);
        ServerTemplate template = ServerTemplateConverter.fromState(state);

        // Change to a new temporary KieServerId and save back into KieServerStateRepo 
        template.setId(tmpId);
        template.setName(tmpId);
        template.getServerInstance(TEST_KIE_SERVER_ID).setServerInstanceId(tmpId);
        template.getServerInstance(tmpId).setServerName(tmpId);
        template.getServerInstance(tmpId).setServerTemplateId(tmpId);

        template.getContainersSpec().stream().forEach(conSpec -> {
            conSpec.setId(tmpId);
            conSpec.setContainerName(tmpId);
            conSpec.setServerTemplateKey(new ServerTemplateKey(tmpId, tmpId));
        });

        repo.create(ServerTemplateConverter.toState(template));

        state = repo.load(tmpId);
        assertTrue(isMatching(template, state));
    }

    @Test
    public void testResolveServerUrl() {
        if (ServerTemplateConverter.PREFER_KIESERVER_SERVICE) {
            KieServerState state = repo.load(TEST_KIE_SERVER_ID);
            assertEquals("http://172.30.99.129:8080/services/rest/server",
                         ServerTemplateConverter.fromState(state).getServerInstance(TEST_KIE_SERVER_ID).getUrl());
        }
    }

    @After
    public void tearDown() {
        client.configMaps().inNamespace(testNamespace).delete();
        client.close();
    }

    private static boolean isMatching(ServerTemplate template, KieServerState state) {
        boolean result = true;
        String id = null;
        String url = null;
        try {
            id = state.getConfiguration().getConfigItemValue(KieServerConstants.KIE_SERVER_ID);
            url = ServerTemplateConverter.resolveServerUrl(state);

            if (!template.getId().equals(id)) {
                fail("Id doesn't match!");
            }

            if (!template.getName().equals(id)) {
                fail("Name doesn't match!");
            }

            if (template.getServerInstanceKeys().size() != 1) {
                fail("ServerTemplate can have ONLY ONE server instance, but now contains: [" +
                     template.getServerInstanceKeys().size() + "].");
            }

            if (!template.hasServerInstanceId(id)) {
                fail("Server instance id missing.");
            }

            if (!template.hasServerInstance(url)) {
                fail("Server URL missing.");
            }

            if (!template.getServerInstance(id).getServerName().equals(id)) {
                fail("Server instance name doesn't match up with Id!");
            }

            if (!template.getServerInstance(id).getServerTemplateId().equals(id)) {
                fail("Server instance templateId doesn't match up with Id!");
            }

            if (!template.getServerInstance(id).getUrl().equals(url)) {
                fail("Server URL doesn't match!");
            }

            // ContainerSpec and ContainerResource mapping check
            for (ContainerSpec conSpec : template.getContainersSpec()) {
                String conId = conSpec.getId();
                String conName = conSpec.getContainerName();
                ReleaseId relId = conSpec.getReleasedId();
                KieContainerStatus status = conSpec.getStatus();
                KieContainerResource conResFound = null;

                if (!conSpec.getServerTemplateKey().getId().equals(id)) {
                    fail("Server template id within container spec doesn't match!");
                }
                if (!conSpec.getServerTemplateKey().getName().equals(id)) {
                    fail("Server template name within container spec doesn't match!");
                }

                for (KieContainerResource conRes : state.getContainers()) {
                    if (conRes.getReleaseId().equals(relId)) {
                        conResFound = conRes;
                        break;
                    }
                }

                if (conResFound == null) {
                    fail("Container Spec/Resource doesn't match! Container Id: [" + conId + "]");
                }
                if (!conResFound.getContainerId().equals(conId)) {
                    fail("Container Id doesn't match!");
                }
                if (!conResFound.getContainerAlias().equals(conName)) {
                    fail("Container Name/Alias doesn't match!");
                }
                if (!conResFound.getReleaseId().equals(relId)) {
                    fail("Container releaseId doesn't match!");
                }
                if (!conResFound.getStatus().equals(status)) {
                    fail("Container status doesn't match!");
                }

                List<KieServerConfigItem> cfgItems = conResFound.getConfigItems();

                // ContainerSpec.configs check
                for (ContainerConfig conCfg : conSpec.getConfigs().values()) {
                    if (conCfg instanceof ProcessConfig) {
                        ProcessConfig pconfig = (ProcessConfig) conCfg;
                        if (pconfig.getKBase() != null && !pconfig.getKBase().equals(getConfigItemValue(KieServerConstants.PCFG_KIE_BASE, cfgItems)) && !KieServerConstants.CAPABILITY_BPM.equals(getConfigItem(
                                                                                                                                                                                                                KieServerConstants.PCFG_KIE_BASE,
                                                                                                                                                                                                                cfgItems).getType())) {
                            fail("Container config: value of KBase doesn't match!");
                        }
                        if (pconfig.getKSession() != null && !pconfig.getKSession().equals(getConfigItemValue(KieServerConstants.PCFG_KIE_SESSION, cfgItems)) && !KieServerConstants.CAPABILITY_BPM.equals(getConfigItem(
                                                                                                                                                                                                                         KieServerConstants.PCFG_KIE_SESSION,
                                                                                                                                                                                                                         cfgItems).getType())) {
                            fail("Container config: value of KSession doesn't match!");
                        }
                        if (pconfig.getMergeMode() != null && !pconfig.getMergeMode().equals(getConfigItemValue(KieServerConstants.PCFG_MERGE_MODE, cfgItems)) && !KieServerConstants.CAPABILITY_BPM.equals(getConfigItem(
                                                                                                                                                                                                                          KieServerConstants.PCFG_MERGE_MODE,
                                                                                                                                                                                                                          cfgItems).getType())) {
                            fail("Container config: value of MergeMode doesn't match!");
                        }
                        if (pconfig.getRuntimeStrategy() != null && !pconfig.getRuntimeStrategy().equals(getConfigItemValue(KieServerConstants.PCFG_RUNTIME_STRATEGY, cfgItems)) && !KieServerConstants.CAPABILITY_BPM
                                                                                                                                                                                                                      .equals(getConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY,
                                                                                                                                                                                                                                            cfgItems).getType())) {
                            fail("Container config: value of RuntimeStrategy doesn't match!");
                        }
                    } else if (conCfg instanceof RuleConfig) {
                        RuleConfig rconfig = (RuleConfig) conCfg;
                        KieScannerResource sRes = conResFound.getScanner();
                        if (sRes == null) {
                            fail("Container config: Scanner can't be null!");
                        }
                        if (rconfig.getScannerStatus() != null && !rconfig.getScannerStatus().equals(sRes.getStatus())) {
                            fail("Container config: Scanner status doesn't match!");
                        }
                        if (rconfig.getPollInterval() != null) {
                            if (!rconfig.getPollInterval().toString().equals(sRes.getPollInterval().toString())) {
                                fail("Container config: Scanner PollInterval doesn't match!");
                            }
                        }
                    }
                }
            }

            // Checking Capability mappings; by default, all capabilities are enabled
            KieServerConfig kcfg = state.getConfiguration();
            if (kcfg.getConfigItem(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED) != null && template.getCapabilities().contains(Capability.PROCESS.name())) {
                fail("Server Template can not specify disabled capability: [" + Capability.PROCESS + "].");
            }

            if (kcfg.getConfigItem(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED) != null && template.getCapabilities().contains(Capability.RULE.name())) {
                fail("Server Template can not specify disabled capability: [" + Capability.RULE + "].");
            }

            if (kcfg.getConfigItem(KieServerConstants.KIE_OPTAPLANNER_SERVER_EXT_DISABLED) != null && template.getCapabilities().contains(Capability.PLANNING.name())) {
                fail("Server Template can not specify disabled capability: [" + Capability.PLANNING + "].");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        return result;
    }

    private static KieServerConfigItem getConfigItem(String name, List<KieServerConfigItem> configItems) {
        KieServerConfigItem configItem = null;

        for (KieServerConfigItem item : configItems) {
            if (name.equals(item.getName())) {
                configItem = item;
                break;
            }
        }

        return configItem;
    }

    private static String getConfigItemValue(String name, List<KieServerConfigItem> configItems) {
        KieServerConfigItem item = getConfigItem(name, configItems);

        if (item != null) {
            return item.getValue();
        }

        return null;
    }

}
