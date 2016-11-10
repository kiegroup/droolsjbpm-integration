/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class PerProcessInstanceWorkItemIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "per-process-instance-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "per-process-instance-project";
    private static final String PROCESS_ID = "per-process-instance-project.email";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/per-process-instance-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testGetWorkItemByProcessInstance() throws Exception {
        Long instanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);

        List<WorkItemInstance> workItemByProcessInstance = processClient.getWorkItemByProcessInstance(CONTAINER_ID, instanceId);
        Assert.assertNotNull(workItemByProcessInstance);
        Assert.assertEquals(1, workItemByProcessInstance.size());

        WorkItemInstance workItem = workItemByProcessInstance.get(0);
        Assert.assertEquals("Email", workItem.getName());
        Assert.assertEquals(instanceId, workItem.getProcessInstanceId());
    }

    @Test
    public void testGetWorkItemByProcessInstanceAbortedProcess() throws Exception {
        Long instanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        processClient.abortProcessInstance(CONTAINER_ID, instanceId);

        try {
            processClient.getWorkItemByProcessInstance(CONTAINER_ID, instanceId);
            fail("Calling getWorkItemByProcessInstance() on aborted process instance should throw KieServicesException.");
        } catch(KieServicesException e) {
            // expected
        }
    }

    @Test(expected=KieServicesException.class)
    public void testGetWorkItemByProcessInstanceNonExistingProcess() throws Exception {
        processClient.getWorkItemByProcessInstance(CONTAINER_ID, 123456L);
    }

    @Test
    public void testGetWorkItemNonExistingItem() throws Exception {
        Long instanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);

        try {
            processClient.getWorkItem(CONTAINER_ID, instanceId, 123456L);
            fail("Calling getWorkItem() on non existing work item should throw KieServicesException.");
        } catch(KieServicesException e) {
            // expected
        }
    }

    @Test
    public void testGetWorkItemAbortedProcess() throws Exception {
        Long instanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        processClient.abortProcessInstance(CONTAINER_ID, instanceId);

        try {
            processClient.getWorkItem(CONTAINER_ID, instanceId, 123456L);
            fail("Calling getWorkItem() on aborted process instance should throw KieServicesException.");
        } catch(KieServicesException e) {
            // expected
        }
    }

    @Test(expected=KieServicesException.class)
    public void testGetWorkItemNonExistingProcess() throws Exception {
        processClient.getWorkItem(CONTAINER_ID, 123456L, 123456L);
    }
}
