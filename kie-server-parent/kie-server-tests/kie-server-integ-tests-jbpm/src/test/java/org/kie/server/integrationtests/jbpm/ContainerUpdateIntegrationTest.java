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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.api.model.definition.UserTaskDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;
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
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/broken-project").getFile());
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

        Map<String, UserTaskDefinition> map = mapByName(userTaskDefinitions.getItems());

        assertTrue(map.containsKey("First task"));
        assertTrue(map.containsKey("Second task"));

        UserTaskDefinition firstTaskDefinition = map.get("First task");
        assertEquals("First task", firstTaskDefinition.getName());
        assertTrue("Task should be skippable.", firstTaskDefinition.isSkippable());

        // Update container to new version.
        KieServerAssert.assertSuccess(client.updateReleaseId(CONTAINER_ID, releaseId101));

        userTaskDefinitions = processClient.getUserTaskDefinitions(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertEquals(2, userTaskDefinitions.getItems().size());

        map = mapByName(userTaskDefinitions.getItems());

        assertTrue(map.containsKey("Updated first task"));
        assertTrue(map.containsKey("Second task"));

        firstTaskDefinition = map.get("Updated first task");
        assertEquals("Updated first task", firstTaskDefinition.getName());
        assertFalse("Task shouldn't be skippable.", firstTaskDefinition.isSkippable());
    }

    @Test
    public void testUpdateContainerWithActiveProcessInstance() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);

        try {
            ServiceResponse<ReleaseId> updateReleaseId = client.updateReleaseId(CONTAINER_ID, releaseId101);
            KieServerAssert.assertFailure(updateReleaseId);
            assertEquals("Update of container forbidden - there are active process instances for container " + CONTAINER_ID, updateReleaseId.getMsg());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testMessagesOfContainer() throws Exception {
        ServiceResponse<KieContainerResource> response = client.getContainerInfo(CONTAINER_ID);
        KieServerAssert.assertSuccess(response);
        
        KieContainerResource resource = response.getResult();
        assertEquals("Shound not have any messages", 1, resource.getMessages().size());
        Message message = resource.getMessages().get(0);
        assertEquals("Message should be of type info", Severity.INFO, message.getSeverity());
     
        ServiceResponse<KieContainerResource> createNotExsting = client.createContainer(
                "broken-project", 
                new KieContainerResource("broken-project",
                        new ReleaseId(
                                "org.kie.server.testing", 
                                "broken-project",
                                "1.0.0.Final")));
        KieServerAssert.assertFailure(createNotExsting);
               
        response = client.getContainerInfo("broken-project");
        KieServerAssert.assertSuccess(response);
        
        resource = response.getResult();
        assertEquals("Shound have one message", 1, resource.getMessages().size());
        message = resource.getMessages().get(0);
        assertEquals("Message should be of type error", Severity.ERROR, message.getSeverity());
    }

    protected Map<String, UserTaskDefinition> mapByName(List<UserTaskDefinition> taskDefinitions) {
        Map<String, UserTaskDefinition> mapped = new HashMap<String, UserTaskDefinition>();

        for (UserTaskDefinition definition : taskDefinitions) {
            mapped.put(definition.getName(), definition);
        }

        return mapped;
    }
}
