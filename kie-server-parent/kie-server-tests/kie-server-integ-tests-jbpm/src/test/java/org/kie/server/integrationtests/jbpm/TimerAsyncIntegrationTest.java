/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;
import static org.kie.server.integrationtests.shared.KieServerSynchronization.waitForCondition;

public class TimerAsyncIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "timer-async-project",
            "1.0.0.Final");

    @Parameterized.Parameters(name = "{0} {1} {2}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration configuration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][] {
                            {MarshallingFormat.JAXB, configuration, "PER_REQUEST"}
                        }
        ));

        return parameterData;
    }

    @Parameterized.Parameter(2)
    public String runtimeStrategy;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/timer-async-project");
    }

    @After
    public void disposeContainers() {
        String containerId = "timer-async-project-" + runtimeStrategy;
        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByContainerId(containerId, null, 0, 10, "log.processInstanceId", false);
        for(ProcessInstance processInstanceId : startedInstances) {
            processClient.abortProcessInstance(containerId, processInstanceId.getId());
        }
        disposeAllContainers();
    }

    @Test(timeout = 60 * 1000)
    public void testTimerAfterAsyncTask() throws Exception {
        String containerId = "timer-async-project-" + runtimeStrategy;
        createContainer(containerId, releaseId, new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, runtimeStrategy, String.class.getName()));

        Long pid = this.processClient.startProcess(containerId, "timer-per-request.timer-per-request");

        waitForCondition(() -> {
          return processClient.findActiveNodeInstances(containerId, pid, 0, 10)
                              .stream()
                              .map(n -> n.getNodeType()).collect(toList())
                              .contains("HumanTaskNode");
        }, 15000L);
        
        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Long taskId = taskList.get(0).getId();
        taskClient.startTask(containerId, taskId, USER_YODA);
        taskClient.completeTask(containerId, taskId, USER_YODA, Collections.emptyMap());
        
        // the timer after the async task should not be triggered yet
        assertTrue(processClient.findActiveNodeInstances(containerId, pid, 0, 10)
                                .stream()
                                .map(n -> n.getNodeType()).collect(toList())
                                .contains("TimerNode"));
    }

}
