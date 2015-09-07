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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;

public class FailureOnContainerDisposeIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String DISPOSE_FAILURE_MSG = "Container definition-project failed to dispose, exception was raised: java.lang.IllegalStateException:" +
            " Undeploy forbidden - there are active processes instances for deployment definition-project";

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
                    KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);

            localServerConfig.addJaxbClasses(extraClasses);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
        configuration.setTimeout(5000);
        setupClients(kieServicesClient);

        return kieServicesClient;
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
