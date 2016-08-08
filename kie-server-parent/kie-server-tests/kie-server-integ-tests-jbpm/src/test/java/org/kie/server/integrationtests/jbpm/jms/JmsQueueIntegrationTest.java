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

package org.kie.server.integrationtests.jbpm.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.category.RemotelyControlled;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.controller.ContainerRemoteController;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

@Category({JMSOnly.class, RemotelyControlled.class})
public class JmsQueueIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    private static ContainerRemoteController containerRemoteController;

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB, jmsConfiguration}
                        }
        ));

        return parameterData;
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        containerRemoteController = new ContainerRemoteController(TestConfig.getContainerId(), TestConfig.getContainerPort());

        createContainer(CONTAINER_ID, RELEASE_ID);
    }

    @Test
    public void testStartProcessFromJmsAfterApplicationStart() throws Exception {
        // Custom client with reduced timeout
        KieServicesConfiguration customConfig = configuration.clone();
        customConfig.setTimeout(3000);
        KieServicesClient customClient = KieServicesFactory.newKieServicesClient(customConfig);
        ProcessServicesClient customProcessClient = customClient.getServicesClient(ProcessServicesClient.class);

        containerRemoteController.undeployWarFile(TestConfig.getKieServerContext(), TestConfig.getKieServerWarPath());

        try {
            customProcessClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
            fail("Should throw exception about Kie server being unavailable.");
        } catch (Exception e) {

            assertTrue(e instanceof KieServicesException);
            assertEquals("Response is empty", ((KieServicesException) e).getMessage());
        } finally {
            containerRemoteController.deployWarFile(TestConfig.getKieServerContext(), TestConfig.getKieServerWarPath());
        }

        // Wait for process instance to start.
        KieServerSynchronization.waitForProcessInstanceStart(queryClient, CONTAINER_ID);

        // Process should be deployed.
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertEquals(1, processInstances.size());

        ProcessInstance pi = processInstances.get(0);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, pi.getState().intValue());
        assertEquals(PROCESS_ID_USERTASK, pi.getProcessId());

        processClient.abortProcessInstance(CONTAINER_ID, pi.getId());
    }
}
