/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import org.junit.Assert;
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

import static org.junit.Assert.assertEquals;

public class TimerRollbackRegressionIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "timer-project",
            "1.0.0.Final");

    @Parameterized.Parameters(name = "{0} {1} {2}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration configuration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][] {
                                {MarshallingFormat.JAXB, configuration, "SINGLETON"},
                                {MarshallingFormat.JAXB, configuration, "PER_PROCESS_INSTANCE"}
                        }
        ));

        return parameterData;
    }

    @Parameterized.Parameter(2)
    public String runtimeStrategy;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/timer-project");
    }

    @After
    public void disposeContainers() {
        disposeAllContainers();
    }

    @Test(timeout = 60 * 1000)
    public void testTimerRollbackTimerCancel() throws Exception {
        String containerId = "timer-project-" + runtimeStrategy;
        createContainer(containerId, releaseId, new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, runtimeStrategy, String.class.getName()));

        this.processClient.startProcess(containerId, "error-handling.test-rollback");
        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByContainerId(containerId, null, 0, 10, null, false);

        assertEquals(1, startedInstances.size());

        // this should fail
        try {
            List<TaskSummary> summary = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertEquals(1, summary.size());
            long taskId = summary.get(0).getId();
            this.taskClient.startTask(containerId, taskId, summary.get(0).getActualOwner());
            this.taskClient.completeTask(containerId, taskId, summary.get(0).getActualOwner(), Collections.emptyMap());
            Assert.fail(); // shout not reach as complete task should throw an exception
        } catch (Exception e) {
            // do nothing as it should fail
        }

        Thread.sleep(6000L);
        // the timer should still be active and triggered
        startedInstances = queryClient.findProcessInstancesByContainerId(containerId, null, 0, 10, null, false);
        assertEquals(0, startedInstances.size());
    }}
