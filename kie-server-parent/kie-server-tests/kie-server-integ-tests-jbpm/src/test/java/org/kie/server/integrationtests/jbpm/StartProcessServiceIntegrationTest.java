/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StartProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final KieServerConfigItem PPI_RUNTIME_STRATEGY = new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, RuntimeStrategy.PER_PROCESS_INSTANCE.name(), String.class.getName());

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "restart-project",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/restart-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID_RESTART, releaseId, PPI_RUNTIME_STRATEGY);
    }


    @Test()
    public void testStartProcessFromNodeId() {
        Map<String, Object> parameters = new HashMap<>();

        Long processInstanceId = processClient.startProcess(CONTAINER_ID_RESTART, PROCESS_ID_RESTART, parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId > 0);

            // Process instance is running and is active.
            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());

            processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);

            List<NodeInstance> list = this.processClient.findNodeInstancesByType(CONTAINER_ID_RESTART, processInstanceId, "ABORTED", 0, 10);
            String[] nodeIds = list.stream().map(NodeInstance::getNodeId).toArray(String[]::new);

            processInstanceId = null;
            processInstanceId = processClient.startProcessFromNodeIds(CONTAINER_ID_RESTART, PROCESS_ID_RESTART, parameters, nodeIds);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());

            processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
        } catch (Exception e) {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
            }
            fail(e.getMessage());
        }
    }

    @Test()
    public void testStartProcessFromNodeIdWithCorrelationKeyProcess() {
        Map<String, Object> parameters = new HashMap<>();

        Long processInstanceId = processClient.startProcess(CONTAINER_ID_RESTART, PROCESS_ID_RESTART, parameters);
        try {
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId > 0);

            // Process instance is running and is active.
            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());

            processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);

            List<NodeInstance> list = this.processClient.findNodeInstancesByType(CONTAINER_ID_RESTART, processInstanceId, "ABORTED", 0, 10);
            String[] nodeIds = list.stream().map(NodeInstance::getNodeId).toArray(String[]::new);

            CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
            CorrelationKey firstKey = correlationKeyFactory.newCorrelationKey("mysimlekey");
            processInstanceId = null;
            processInstanceId = processClient.startProcessFromNodeIds(CONTAINER_ID_RESTART, PROCESS_ID_RESTART, firstKey, parameters, nodeIds);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, processInstance.getState().intValue());

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
            assertThat(pi.getCorrelationKey(), is("mysimlekey"));
            processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
        } catch (Exception e) {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID_RESTART, processInstanceId);
            }
            fail(e.getMessage());
        }
    }
}
