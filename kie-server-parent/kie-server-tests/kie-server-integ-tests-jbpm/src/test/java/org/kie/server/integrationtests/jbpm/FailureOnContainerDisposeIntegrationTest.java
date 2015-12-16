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

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;

public class FailureOnContainerDisposeIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String DISPOSE_FAILURE_MSG = "Container definition-project failed to dispose, exception was raised: java.lang.IllegalStateException:" +
            " Undeploy forbidden - there are active processes instances for deployment definition-project";

    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testNotAllowedDisposeContainerDueToActiveProcessInstances() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long processInstanceId = processClient.startProcess("definition-project", "definition-project.usertask", parameters);
        // dispose not allowed as there is active process instance
        ServiceResponse<Void> disposeContainerResponse = client.disposeContainer("definition-project");

        assertEquals(ServiceResponse.ResponseType.FAILURE, disposeContainerResponse.getType());
        String failureMessage = disposeContainerResponse.getMsg();
        assertEquals(DISPOSE_FAILURE_MSG, failureMessage);
        // after failed dispose container should be fully operational and in started state
        ServiceResponse<KieContainerResource> containerResponse = client.getContainerInfo("definition-project");
        assertEquals(ServiceResponse.ResponseType.SUCCESS, containerResponse.getType());
        KieContainerResource container = containerResponse.getResult();
        assertNotNull(container);
        assertEquals(KieContainerStatus.STARTED, container.getStatus());
        // let's abort the active instance
        processClient.abortProcessInstance("definition-project", processInstanceId);
        // and now proceed with dispose again which must be successful
        disposeContainerResponse = client.disposeContainer("definition-project");
        assertEquals(ServiceResponse.ResponseType.SUCCESS, disposeContainerResponse.getType());
    }
}
