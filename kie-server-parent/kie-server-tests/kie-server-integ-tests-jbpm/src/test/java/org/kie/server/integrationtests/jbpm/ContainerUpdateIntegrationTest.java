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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ContainerUpdateIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testUserTaskWithUpdatedContainer() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);

        try {
            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            assertEquals("First task", tasks.get(0).getName());
            assertTrue("Task should be skippable.", tasks.get(0).getSkipable().booleanValue());

            // Update container to new version and restart process.
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));
            processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);

            tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            assertEquals("Updated first task", tasks.get(0).getName());
            assertFalse("Task shouldn't be skippable.", tasks.get(0).getSkipable().booleanValue());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test
    public void testProcessDefinitionWithUpdatedContainer() throws Exception {
        UserTaskDefinitionList userTaskDefinitions = processClient.getUserTaskDefinitions(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertEquals(2, userTaskDefinitions.getItems().size());

        UserTaskDefinition firstTaskDefinition = userTaskDefinitions.getItems().get(0);
        assertEquals("First task", firstTaskDefinition.getName());
        assertTrue("Task should be skippable.", firstTaskDefinition.isSkippable());

        // Update container to new version.
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));

        userTaskDefinitions = processClient.getUserTaskDefinitions(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertEquals(2, userTaskDefinitions.getItems().size());

        firstTaskDefinition = userTaskDefinitions.getItems().get(0);
        assertEquals("Updated first task", firstTaskDefinition.getName());
        assertFalse("Task shouldn't be skippable.", firstTaskDefinition.isSkippable());
    }

    @Test
    public void testUpdateContainerWithActiveProcessInstance() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);

        try {
            ServiceResponse<ReleaseId> updateReleaseId = client.updateReleaseId(CONTAINER_ID, releaseId101);
            KieServerAssert.assertSuccess(updateReleaseId);

            // TODO how it should behave in this case? Throwing error or keeping old container and returning failure?
            assertEquals(ServiceResponse.ResponseType.FAILURE, updateReleaseId.getType());
            assertEquals("Cannot update container, active process instances found.", updateReleaseId.getMsg());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
}
