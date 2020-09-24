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

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.api.task.model.Status.Reserved;

@Category(JEEOnly.class)
public class NotificationSaveContentIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", CONTAINER_ID_NOTIFICATION,
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/"+CONTAINER_ID_NOTIFICATION);

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID_NOTIFICATION, releaseId);
    }

    @Test
    public void testSaveContentOnNotification() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID_NOTIFICATION, PROCESS_ID_NOTIFICATION);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        try {
            List<TaskSummary> tasks=taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertEquals(1, tasks.size());
            
            String reserved = Reserved.name();
            //Task is claimed during onNotification, as well as saving output content
            //Waiting until switched to Reserved (when claimed)
            KieServerSynchronization.waitForTaskStatus(taskClient, tasks.get(0).getId(), reserved);
            
            tasks=taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, singletonList(reserved), 0, 10);
            assertEquals(1, tasks.size());
            
            Map<String, Object> output = taskClient.getTaskOutputContentByTaskId(CONTAINER_ID_NOTIFICATION, tasks.get(0).getId());
            assertNotNull(output);
            assertEquals(1, output.size());
            assertTrue(output.containsKey("grade"));
            assertTrue(output.containsValue("E"));
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID_NOTIFICATION, processInstanceId);
        }
    }
}
