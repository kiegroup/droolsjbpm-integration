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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.junit.Assert.*;

public class TimerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    protected static final String TIMER_CONTAINER_ID = "timer-project";
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "timer-project",
            "1.0.0.Final");

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration configuration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][] {
                                {MarshallingFormat.JAXB, configuration}
                        }
        ));

        return parameterData;
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/timer-project").getFile());

        createContainer(TIMER_CONTAINER_ID, releaseId);
    }

    @Test(timeout = 60 * 1000)
    public void testTimerStartEvent() throws Exception {
        List<Integer> completedOnly = Arrays.asList(2);
        KieServerSynchronization.waitForProcessInstanceStart(queryClient, TIMER_CONTAINER_ID, 3, completedOnly);

        List<ProcessInstance> startedInstances = queryClient.findProcessInstancesByProcessId("timer-start", completedOnly, 0, 10, "Id", false);

        assertEquals(3, startedInstances.size());

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
