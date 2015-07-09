/*
 * Copyright 2015 JBoss Inc
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;


public class BARuntimeDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");



    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    protected KieServicesClient createDefaultClient() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        try {
            extraClasses.add(Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        KieServicesClient kieServicesClient = null;
        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);

            localServerConfig.addJaxbClasses(extraClasses);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            configuration.setUserName("Administrator");
            kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
        configuration.setTimeout(5000);
        setupClients(kieServicesClient);

        return kieServicesClient;
    }



    @Test
    public void testFindTaskAssignedAsBusinessAdmin() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);

        try {

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsBusinessAdministrator("Administrator", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary taskInstance = tasks.get(0);
            assertNotNull(taskInstance);
            assertEquals("First task", taskInstance.getName());
            assertEquals("", taskInstance.getDescription());
            assertEquals("Reserved", taskInstance.getStatus());
            assertEquals(0, taskInstance.getPriority().intValue());
            assertEquals("yoda", taskInstance.getActualOwner());
            assertEquals("yoda", taskInstance.getCreatedBy());
            assertEquals("definition-project.usertask", taskInstance.getProcessId());
            assertEquals("definition-project", taskInstance.getContainerId());
            assertEquals(-1, taskInstance.getParentId().longValue());
            assertEquals(processInstanceId, taskInstance.getProcessInstanceId());

            List<String> status = new ArrayList<String>();
            status.add(Status.InProgress.toString());

            tasks = taskClient.findTasksAssignedAsBusinessAdministrator("Administrator", status, 0, 10);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());


        } finally {
            processClient.abortProcessInstance("definition-project", processInstanceId);
        }
    }
}
