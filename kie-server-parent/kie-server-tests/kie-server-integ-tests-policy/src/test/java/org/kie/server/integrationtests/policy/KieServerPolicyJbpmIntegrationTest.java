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

package org.kie.server.integrationtests.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class KieServerPolicyJbpmIntegrationTest extends KieServerPolicyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");


    protected static final String CONTAINER_ALIAS = "project";
    protected static final String CONTAINER_ID_101 = "definition-project-101";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Before
    public void createContainer() {
        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @After
    public void removeExtraContainer() {
        abortAllProcesses();
        client.disposeContainer(CONTAINER_ID_101);
    }

    protected void createExtraContainer() {
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID_101, releaseId101);
        containerResource.setContainerAlias(CONTAINER_ALIAS);
        client.createContainer(CONTAINER_ID_101, containerResource);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartProcessInDifferentDeploymentWithAlias() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", list);
        parameters.put("person", person);


        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV1);
        assertTrue(processInstanceIdV1.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        ServiceResponse<KieContainerResourceList> containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);

        List<KieContainerResource> containerResources = containersResponse.getResult().getContainers();
        assertEquals(1, containerResources.size());

        createExtraContainer();

        // wait for sync
        KieServerSynchronization.waitForKieServerSynchronization(client, 2);

        Long processInstanceIdV2 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION_2, parameters);

        assertNotNull(processInstanceIdV2);
        assertTrue(processInstanceIdV2.longValue() > 0);

        processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV2);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID_101, processInstance.getContainerId());

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(2, tasks.size());

        // there are instances in both containers thus the older one cannot be disposed
        processClient.abortProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);

        // In case of race condition when we abort process instance on container 
        // which is in process of dispose try by policy, we will wait and synchronize again.
        containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);
        if(containersResponse.getResult().getContainers().size() == 1) {
            Thread.sleep(1000);
        }

        // wait for policy to be activated
        KieServerSynchronization.waitForKieServerSynchronization(client, 1);

        containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);

        containerResources = containersResponse.getResult().getContainers();
        assertEquals(1, containerResources.size());

        ReleaseId latestContainerReleaseId = containerResources.get(0).getReleaseId();
        assertEquals(releaseId101, latestContainerReleaseId);

    }
}
