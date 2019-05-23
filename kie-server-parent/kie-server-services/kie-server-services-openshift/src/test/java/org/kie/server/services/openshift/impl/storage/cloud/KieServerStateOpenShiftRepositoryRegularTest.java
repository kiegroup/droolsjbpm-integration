/**
 * Copyright (C) 2019 Red Hat, Inc.
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.services.impl.StartupStrategyProvider;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.server.api.KieServerConstants.KIE_SERVER_ID;
import static org.kie.server.controller.api.KieServerControllerConstants.KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_DATA_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_APP_NAME_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_ID_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_KEY;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.CFG_MAP_LABEL_SERVER_STATE_VALUE_USED;
import static org.kie.server.services.openshift.api.KieServerOpenShiftConstants.ROLLOUT_REQUIRED;

public class KieServerStateOpenShiftRepositoryRegularTest extends KieServerStateOpenShiftRepositoryTest {

    @Test
    public void testLiteralConfigMap() throws InterruptedException {
        HashMap<String, String> data = new HashMap<>();
        data.put("foo", "bar");
        data.put("cheese", "gouda");
    
        Map<String, String> ant = new ConcurrentHashMap<>();
        ant.put("services.server.kie.org/kie-server-state.changeTimestamp",
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    
        Map<String, String> lab = new ConcurrentHashMap<>();
        lab.put("startup_in_progress", "kieserverId");
    
        client.configMaps().inNamespace(testNamespace)
              .createOrReplace(new ConfigMapBuilder()
                                                     .withNewMetadata()
                                                     .withName("cfg1")
                                                     .endMetadata()
                                                     .addToData(data).build());
    
        try {
            client.configMaps().inNamespace(testNamespace)
                  .create(new ConfigMapBuilder()
                                                .withNewMetadata()
                                                .withName("cfg1")
                                                .withLabels(lab)
                                                .withAnnotations(ant)
                                                .endMetadata()
                                                .addToData(data).build());
        } catch (Exception e) {
            // If test against real cluster, second create will fail
        }
    
        // If test against real cluster, uncomment out the following
        //        assertTrue(client.configMaps().inNamespace(testNamespace)
        //                   .withLabel("startup_in_progress", "kieserverId")
        //                   .list().getItems().isEmpty());
    
        client.configMaps().inNamespace(testNamespace)
              .createOrReplace(new ConfigMapBuilder()
                                                     .withNewMetadata()
                                                     .withName("cfg2")
                                                     .withLabels(lab)
                                                     .withAnnotations(ant)
                                                     .endMetadata()
                                                     .addToData(data).build());
    
        ConfigMapList cfgList = client.configMaps().inNamespace(testNamespace)
                                      .withLabel("startup_in_progress", "kieserverid")
                                      .list();
    
        Map<String, String> keys = client.configMaps()
                                         .inNamespace(testNamespace)
                                         .withName("cfg1").get().getData();
    
        assertEquals("gouda", keys.get("cheese"));
        assertEquals("bar", keys.get("foo"));
    
        client.configMaps().inNamespace(testNamespace).delete(cfgList.getItems());
    
        assertTrue(client.configMaps().inNamespace(testNamespace)
                         .withLabel("startup_in_progress", "kieserverid")
                         .list().getItems().isEmpty());
    }

    @Test
    public void testKieServerStateConfigMap() throws InterruptedException {
        Optional<ConfigMap> cmOpt = repo.getKieServerCM(client, TEST_KIE_SERVER_ID);
        
        assertTrue(cmOpt.isPresent());
        cmOpt.ifPresent(configMap -> assertTrue(configMap.getMetadata().getLabels().containsValue(TEST_KIE_SERVER_ID)));
    
        Map<String, String> data = cmOpt.get().getData();
    
        // Avoid attribute name having '.' as it confuses jsonpath
        String srvStateInXML = data.get(CFG_MAP_DATA_KEY);
        KieServerState kieServerState = (KieServerState) xs.fromXML(srvStateInXML);
    
        assertNotNull(kieServerState);
        assertEquals(TEST_KIE_SERVER_ID,
                     kieServerState.getConfiguration().getConfigItem(KIE_SERVER_ID).getValue());
    
    }

    @Test
    public void testStoreAndLoad() throws InterruptedException {
        // Retrieve the seeded KSSConfigMap and Store it under new name
        String srvStateInXML = repo.getKieServerCM(client, TEST_KIE_SERVER_ID)
                                   .get().getData().get(CFG_MAP_DATA_KEY);
        KieServerState kieServerState = (KieServerState) xs.fromXML(srvStateInXML);
    
        assertNotNull(kieServerState);
        
        /**
         * At normal situation, DC will not be null otherwise there will no KieServer Pod running
         */
        createDummyDCandRC();
        repo.store(TEST_KIE_SERVER_ID, kieServerState);
        assertNotNull(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get());
    
        KieServerState kssLoaded = repo.load(TEST_KIE_SERVER_ID);
        assertNotNull(kssLoaded);
        assertEquals(TEST_KIE_SERVER_ID,
                     kssLoaded.getConfiguration().getConfigItem(KIE_SERVER_ID).getValue());
    
        KieContainerResource[] kcr = kssLoaded.getContainers()
                                              .<KieContainerResource> toArray(new KieContainerResource[]{});
    
        assertEquals(2, kcr.length);
        assertEquals("mortgages_1.0.0-SNAPSHOT", kcr[0].getContainerId());
        assertEquals("mortgage-process_1.0.0-SNAPSHOT", kcr[1].getContainerId());
    }

    @Test
    public void testStoreAndLoadWithRolloutTrigger() throws InterruptedException {
        // Retrieve the seeded KSSConfigMap and Store it under new name
        String srvStateInXML = repo.getKieServerCM(client, TEST_KIE_SERVER_ID)
                                   .get().getData().get(CFG_MAP_DATA_KEY);
        KieServerState kieServerState = (KieServerState) xs.fromXML(srvStateInXML);
    
        repo.store(TEST_KIE_SERVER_ID, kieServerState);
        assertTrue(repo.getKieServerCM(client, TEST_KIE_SERVER_ID)
                            .get()
                            .getMetadata()
                            .getAnnotations()
                            .containsKey(ROLLOUT_REQUIRED));
    
    }

    @Test
    public void testLoadWithNullServerId() {
        assertNull(repo.load(null));
    }

    @Test
    public void testLoadWithInvalidServerId() {
        assertNull(repo.load("dummy"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreWithEmptyKieServerState() {
        repo.store("dummy", new KieServerState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreWithMissMatchingServerId() {
        KieServerState kss = repo.load(TEST_KIE_SERVER_ID);
        repo.store("dummy", kss);
    }

    @Test(expected = IllegalStateException.class)
    public void testStoreWithoutPreSeededConfigMap() {
        KieServerState kss = repo.load(TEST_KIE_SERVER_ID);
    
        // Remove the configmap created at Setup to set a no pre-seeded scenario
        client.configMaps().withLabel(CFG_MAP_LABEL_SERVER_ID_KEY, TEST_KIE_SERVER_ID).delete();
    
        repo.store(TEST_KIE_SERVER_ID, kss);
    }

    @Test
    public void testAnnotation() {
        String kieServer1 = KIE_SERVER_STARTUP_IN_PROGRESS_KEY_PREFIX + UUID.randomUUID().toString();
        String kieServer2 = KIE_SERVER_STARTUP_IN_PROGRESS_KEY_PREFIX + UUID.randomUUID().toString();
    
        ConfigMap cm = repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get();
        ObjectMeta md = cm.getMetadata();
        Map<String, String> ann = md.getAnnotations() == null ? new HashMap<>() : md.getAnnotations();
        md.setAnnotations(ann);
        ann.put(kieServer1, KIE_SERVER_STARTUP_IN_PROGRESS_VALUE);
        ann.put(kieServer2, KIE_SERVER_STARTUP_IN_PROGRESS_VALUE);
        repo.createOrReplaceCM(client, cm);
        
        assertNotNull(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations());
        assertTrue(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations().containsKey(kieServer1));
        assertTrue(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations().containsKey(kieServer2));
        assertTrue(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations().containsValue(KIE_SERVER_STARTUP_IN_PROGRESS_VALUE));
    
        ann.remove(kieServer1);
        repo.createOrReplaceCM(client, cm);
    
        // Reload CM
        assertTrue(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations().containsValue(KIE_SERVER_STARTUP_IN_PROGRESS_VALUE));
    
        ann.remove(kieServer2);
        repo.createOrReplaceCM(client, cm);
    
        assertFalse(repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getMetadata().getAnnotations().containsValue(KIE_SERVER_STARTUP_IN_PROGRESS_VALUE));
    }

    @Test
    public void testNPEWhenNoServiceProviderConfig() {
        ServiceLoader<KieServerStateRepository> serverStateRepos = ServiceLoader.load(KieServerStateRepository.class);
        assertNotNull(serverStateRepos);
    
        String repoType = StartupStrategyProvider.get().getStrategy().getRepositoryType();
        for (KieServerStateRepository repo : serverStateRepos) {
            assertNotNull(repo);
            assertNotNull(repo.getClass().getSimpleName());
            if (repo.getClass().getSimpleName().equals(repoType)) {
                fail("Unexpected repo type: " + repoType);
            }
        }
    }

    @Test
    public void testCreateAndLoad() {
        // Retrieve the seeded KSSConfigMap and Store it under new name
        String srvStateInXML = repo.getKieServerCM(client, TEST_KIE_SERVER_ID).get().getData().get(CFG_MAP_DATA_KEY);
        KieServerState kieServerState = (KieServerState) xs.fromXML(srvStateInXML);
        String newKieServerID = TEST_KIE_SERVER_ID + "_NEW";
        String kieServerDCUID = UUID.randomUUID().toString();
    
        assertNotNull(kieServerState);
    
        kieServerState.getConfiguration().getConfigItem(KIE_SERVER_ID).setValue(newKieServerID);
        // Create a dummy KIE server DC
        createDummyDCandRC(newKieServerID, kieServerDCUID, 1);

        repo.create(kieServerState);
        assertTrue(repo.getKieServerCM(client, newKieServerID).isPresent());
        assertNotNull(repo.load(newKieServerID));
        
        ConfigMap newKieSeverCM = repo.getKieServerCM(client, newKieServerID).get();
        
        assertEquals(TEST_APP_NAME, newKieSeverCM.getMetadata().getLabels().get(CFG_MAP_LABEL_APP_NAME_KEY));
        assertEquals(kieServerDCUID, newKieSeverCM.getMetadata().getOwnerReferences().get(0).getUid());
    }

    @Test
    public void testDeleteAndExists() {
        assertTrue(repo.exists(TEST_KIE_SERVER_ID));
        repo.delete(TEST_KIE_SERVER_ID);
        assertTrue(!repo.exists(TEST_KIE_SERVER_ID));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeleteAttachedKieServerStateIsNotAllowed() {
        assertTrue(repo.exists(TEST_KIE_SERVER_ID));
        // Create a dummy DeploymentConfig to simulate attached KieServeState scenario
        createDummyDCandRC();
        repo.delete(TEST_KIE_SERVER_ID);
    }

    @Test
    public void testRetrieveAllKieServerIdsAndStates() {
        KieServerState state = repo.load(TEST_KIE_SERVER_ID);
        state.getConfiguration()
             .getConfigItem(KIE_SERVER_ID).setValue(TEST_KIE_SERVER_ID + "_1");
        // Create new KieServer    
        repo.create(state);
    
        state.getConfiguration()
             .getConfigItem(KIE_SERVER_ID).setValue(TEST_KIE_SERVER_ID + "_2");
        // Create new KieServer    
        repo.create(state);
    
        List<String> kIds = repo.retrieveAllKieServerIds();
        assertEquals(3, kIds.size());
        assertTrue(kIds.contains(TEST_KIE_SERVER_ID));
        assertTrue(kIds.contains(TEST_KIE_SERVER_ID + "_1"));
        assertTrue(kIds.contains(TEST_KIE_SERVER_ID + "_2"));
    
        List<KieServerState> kStates = repo.retrieveAllKieServerStates();
        assertEquals(3, kStates.size());
    
        repo.delete(TEST_KIE_SERVER_ID);
        repo.delete(TEST_KIE_SERVER_ID + "_1");
        repo.delete(TEST_KIE_SERVER_ID + "_2");
    
        assertEquals(0, repo.retrieveAllKieServerIds().size());
        assertEquals(0, repo.retrieveAllKieServerStates().size());
    }

    @Test
    @Ignore ("Ignored due to unsupported API method, withLabelSelector, by Mock OpenShiftServer.")
    public void testRetrieveAllKieServerIdsAndStatesWithContaminatedCF() {
        // Adding a contaminated configmap which does not include required label
        ConfigMap cfm = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-without-label.yml")).get();
    
        repo.createOrReplaceCM(client, cfm);
        
        // Now there are two configmaps in the test namespace
        assertEquals(2, client.configMaps().list().getItems().size());
        
        // But still have only 1 valid KieServerState
        List<String> kIds = repo.retrieveAllKieServerIds();
        assertEquals(1, kIds.size());
    
        List<KieServerState> kStates = repo.retrieveAllKieServerStates();
        assertEquals(1, kStates.size());
    }

    @Test
    public void testKieContainerRemoval() {
        ConfigMap cfmContainer = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-used-without-container.yml")).get();
        ConfigMap cfmStopped = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-used-with-stopped-container.yml")).get();
    
        client.configMaps().inNamespace(testNamespace).createOrReplace(cfmContainer);
        client.configMaps().inNamespace(testNamespace).createOrReplace(cfmStopped);

        KieServerState stateWithoutContainer = (KieServerState) xs.fromXML(
            cfmContainer.getData().get(CFG_MAP_DATA_KEY));
        KieServerState stateWithStopped = (KieServerState) xs.fromXML(
            cfmStopped.getData().get(CFG_MAP_DATA_KEY));
        
        // Removing STOPPED container is not allowed
        assertFalse(repo.isKieContainerRemovalAllowed(cfmStopped, stateWithoutContainer));
        // Adding STOPPED container is fine
        assertTrue(repo.isKieContainerRemovalAllowed(cfmContainer, stateWithStopped));
    }
    
    @Test
    public void testKieContainerUpdateDuringRolloutAllowed() {
        ConfigMap cfmContainer = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-used-with-container.yml")).get();
        ConfigMap cfmStopped = client.configMaps()
                .load(KieServerStateOpenShiftRepositoryTest.class
                .getResourceAsStream("/test-kieserver-state-config-map-used-with-stopped-container.yml")).get();
    
        client.configMaps().inNamespace(testNamespace).createOrReplace(cfmContainer);
        client.configMaps().inNamespace(testNamespace).createOrReplace(cfmStopped);

        KieServerState state = (KieServerState) xs.fromXML(
            cfmContainer.getData().get(CFG_MAP_DATA_KEY));
        KieServerState stateWithStopped = (KieServerState) xs.fromXML(
            cfmStopped.getData().get(CFG_MAP_DATA_KEY));
        
        // Only allow state transition from STARTED to STOPPED
        assertTrue(repo.isKieContainerUpdateDuringRolloutAllowed(cfmContainer, stateWithStopped));
        // Does not allow state transition from STOPPED to STARTED as it is not necessary.
        assertFalse(repo.isKieContainerUpdateDuringRolloutAllowed(cfmStopped, state));
    }

    @Test
    public void testLabelSelector() {
        LabelSelector selector = repo.getKieServerCMLabelSelector(client);
        assertNotNull(selector);
        // By default, global discovery is disabled, meaning that it must have an 'application' label in the selector
        boolean foundAppNameLabel = false;
        for (LabelSelectorRequirement selReq : selector.getMatchExpressions()) {
            if (selReq.getKey() == CFG_MAP_LABEL_APP_NAME_KEY) {
                foundAppNameLabel = true;
                assertEquals("In", selReq.getOperator());
                assertTrue(selReq.getValues().contains(TEST_APP_NAME));
            }
            if (selReq.getKey() == CFG_MAP_LABEL_SERVER_STATE_KEY) {
                assertEquals("In", selReq.getOperator());
                assertTrue(selReq.getValues().contains(CFG_MAP_LABEL_SERVER_STATE_VALUE_USED));
                assertTrue(selReq.getValues().contains(CFG_MAP_LABEL_SERVER_STATE_VALUE_IMMUTABLE));
            }
        }
        assertTrue(foundAppNameLabel);
                
        System.setProperty(KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED, "true");
        assertTrue(repo.getKieServerCMLabelSelector(client).getMatchExpressions().stream().noneMatch(
            selReq -> selReq.getKey() == CFG_MAP_LABEL_APP_NAME_KEY));
        System.clearProperty(KIE_CONTROLLER_OPENSHIFT_GLOBAL_DISCOVERY_ENABLED);
    }
    
    @Test
    public void testCreateAndLoadWithZeroReplica() {
        String newKieServerID = TEST_KIE_SERVER_ID + "_NEW";
        String kieServerDCUID = UUID.randomUUID().toString();
        createDummyDCandRC(newKieServerID, kieServerDCUID, 0);
        assertFalse(repo.getKieServerDC(client, newKieServerID).isPresent());
    }

    @Test
    public void testCreateAndLoadWithMoreReplicas() {
        String newKieServerID = TEST_KIE_SERVER_ID + "_NEW";
        String kieServerDCUID = UUID.randomUUID().toString();
        createDummyDCandRC(newKieServerID, kieServerDCUID, 2);
        assertTrue(repo.getKieServerDC(client, newKieServerID).isPresent());
    }

}
