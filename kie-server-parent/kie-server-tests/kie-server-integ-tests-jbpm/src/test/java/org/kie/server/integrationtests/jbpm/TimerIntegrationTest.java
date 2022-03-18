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
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class TimerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

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
        String containerId = "timer-project-" + runtimeStrategy;
        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByContainerId(containerId, null, 0, 10, "log.processInstanceId", false);
        for(ProcessInstance processInstanceId : startedInstances) {
            processClient.abortProcessInstance(containerId, processInstanceId.getId());
        }
        disposeAllContainers();
    }

    @Test(timeout = 60 * 1000)
    public void testTimerStartEvent() throws Exception {
        String containerId = "timer-project-" + runtimeStrategy;
        createContainer(containerId, releaseId, new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, runtimeStrategy, String.class.getName()));

        List<Integer> completedOnly = Arrays.asList(2);
        KieServerSynchronization.waitForProcessInstanceStart(queryClient, containerId, 3, completedOnly);

        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByContainerId(containerId, completedOnly, 0, 10, "log.processInstanceId", false);

        assertEquals(3, startedInstances.size());
        assertEquals(Long.valueOf(3), queryClient.countProcessInstancesByContainerId(containerId, completedOnly));

        long thirdInstance = startedInstances.get(0).getDate().getTime();
        long secondInstance = startedInstances.get(1).getDate().getTime();
        long firstInstance = startedInstances.get(2).getDate().getTime();

        // let's round it up to be on simple value
        double distance1 = Math.ceil((thirdInstance - secondInstance));
        double distance2 = Math.ceil((secondInstance - firstInstance));
        // since the expiration time is 5 seconds let's make sure it's not more than doubled of expiration time
        assertTrue(distance1 < 10000);
        assertTrue(distance2 < 10000);

    }
}
