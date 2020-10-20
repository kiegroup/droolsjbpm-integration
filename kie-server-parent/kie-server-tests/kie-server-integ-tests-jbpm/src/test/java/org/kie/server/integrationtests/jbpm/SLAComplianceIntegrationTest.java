/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.api.runtime.process.ProcessInstance.SLA_MET;
import static org.kie.api.runtime.process.ProcessInstance.SLA_NA;
import static org.kie.api.runtime.process.ProcessInstance.SLA_PENDING;
import static org.kie.api.runtime.process.ProcessInstance.SLA_VIOLATED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.category.UnstableOnJenkinsPrBuilder;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class SLAComplianceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final KieServerConfigItem PPI_RUNTIME_STRATEGY = new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, RuntimeStrategy.PER_PROCESS_INSTANCE.name(), String.class.getName());

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId, PPI_RUNTIME_STRATEGY);
    }

    @Test
    @Category({UnstableOnJenkinsPrBuilder.class})
    public void testSLAonProcessViolated() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_WITH_SLA, new HashMap<>());
        assertProcessInstance(pid, STATE_ACTIVE, SLA_PENDING);

        // Yoda should have one task available without SLA
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        TaskSummary task = tasks.get(0);
        assertThat(task.getName()).isEqualTo("Hello");

        List<NodeInstance> activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).hasSize(1);
        NodeInstance taskNode = activeNodes.get(0);
        assertNodeInstance(taskNode, "Hello", SLA_NA);

        // Let's wait for SLA violation
        KieServerSynchronization.waitForProcessInstanceSLAViolated(queryClient, pid, 8_000L);
        assertProcessInstance(pid, STATE_ACTIVE, SLA_VIOLATED);

        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, null);
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 0);
        assertThat(tasks).isEmpty();

        activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).isEmpty();
        taskNode = getNodeInstanceById(queryClient.findCompletedNodeInstances(pid, 0, 10), taskNode.getId());
        assertNodeInstance(taskNode, "Hello", SLA_NA);

        // Process should be completed, but SLA should still be violated
        assertProcessInstance(pid, STATE_COMPLETED, SLA_VIOLATED);

    }

    @Test
    public void testSLAonProcessMet() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_WITH_SLA, new HashMap<>());
        assertProcessInstance(pid, STATE_ACTIVE, SLA_PENDING);

        // Yoda should have one task available without SLA
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);
        TaskSummary task = tasks.get(0);
        assertThat(task.getName()).isEqualTo("Hello");

        List<NodeInstance> activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).hasSize(1);
        NodeInstance taskNode = activeNodes.get(0);
        assertNodeInstance(taskNode, "Hello", SLA_NA);

        // Complete task before SLA violation
        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, null);
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 0);
        assertThat(tasks).isEmpty();

        activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).isEmpty();
        taskNode = getNodeInstanceById(queryClient.findCompletedNodeInstances(pid, 0, 10), taskNode.getId());
        assertNodeInstance(taskNode, "Hello", SLA_NA);

        // Process should be completed and SLA should be met
        assertProcessInstance(pid, STATE_COMPLETED, SLA_MET);

    }

    @Test
    @Category({UnstableOnJenkinsPrBuilder.class})
    public void testSLAonUserTaskViolated() throws Exception {
        Assume.assumeFalse(TestConfig.isWebLogicHomeProvided()); //Skip the test for WebLogic due to deadlock issues related to https://issues.redhat.com/browse/JBPM-9309

        Long pid = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_WITH_SLA_ON_TASK, new HashMap<>());
        assertProcessInstance(pid, STATE_ACTIVE, SLA_NA);

        // Yoda should have one task available with SLA
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        TaskSummary task = tasks.get(0);
        assertThat(tasks).hasSize(1);
        assertThat(task.getName()).isEqualTo("Hello");

        List<NodeInstance> activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).hasSize(1);

        NodeInstance taskNode = activeNodes.get(0);
        assertNodeInstance(taskNode, "Hello", SLA_PENDING);

        // Let's wait for SLA violation
        KieServerSynchronization.waitForNodeInstanceSLAViolated(queryClient, pid, taskNode.getId(), 8_000L);

        assertProcessInstance(pid, STATE_ACTIVE, SLA_NA);

        activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).hasSize(1);
        taskNode = activeNodes.get(0);
        assertNodeInstance(taskNode, "Hello", SLA_VIOLATED);

        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, null);
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).isEmpty();

        activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).isEmpty();
        taskNode = getNodeInstanceById(queryClient.findCompletedNodeInstances(pid, 0, 10), taskNode.getId());
        assertNodeInstance(taskNode, "Hello", SLA_VIOLATED);

        assertProcessInstance(pid, STATE_COMPLETED, SLA_NA);

    }

    @Test
    public void testSLAonUserTaskMet() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_WITH_SLA_ON_TASK, new HashMap<>());
        assertProcessInstance(pid, STATE_ACTIVE, SLA_NA);

        // Yoda should have one task available with SLA
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        TaskSummary task = tasks.get(0);
        assertThat(tasks).hasSize(1);
        assertThat(task.getName()).isEqualTo("Hello");

        List<NodeInstance> activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).hasSize(1);

        NodeInstance taskNode = activeNodes.get(0);
        assertNodeInstance(taskNode, "Hello", SLA_PENDING);

        taskClient.completeAutoProgress(CONTAINER_ID, task.getId(), USER_YODA, null);
        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).isEmpty();

        activeNodes = processClient.findActiveNodeInstances(CONTAINER_ID, pid, 0, 10);
        assertThat(activeNodes).isEmpty();
        taskNode = getNodeInstanceById(queryClient.findCompletedNodeInstances(pid, 0, 10), taskNode.getId());
        assertNodeInstance(taskNode, "Hello", SLA_MET);

        assertProcessInstance(pid, STATE_COMPLETED, SLA_NA);

    }

    private void assertProcessInstance(Long pid, int processState, int slaStatus) {
        assertThat(pid).isNotNull();
        ProcessInstance pi = queryClient.findProcessInstanceById(pid);
        assertThat(pi.getState()).isEqualTo(processState);
        assertThat(pi.getSlaCompliance()).isEqualTo(slaStatus);
        if (slaStatus != SLA_NA) {
            assertThat(pi.getSlaDueDate()).isCloseTo(new Date(), 30000);
        }
    }

    private void assertNodeInstance(NodeInstance ni, String nodeName, int slaStatus) {
        assertThat(ni.getName()).isEqualTo(nodeName);
        assertThat(ni.getSlaCompliance()).isEqualTo(slaStatus);
        if (slaStatus != SLA_NA) {
            assertThat(ni.getSlaDueDate()).isCloseTo(new Date(), 30000);
        }
    }

    private NodeInstance getNodeInstanceById(List<NodeInstance> nodes, Long nodeInstanceId) {
        List<NodeInstance> foundNode = nodes.stream().filter(ni -> ni.getId().equals(nodeInstanceId)).collect(Collectors.toList());
        assertThat(foundNode).hasSize(1);
        return foundNode.get(0);
    }
}
