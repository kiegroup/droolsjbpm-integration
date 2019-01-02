/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ContainerFailureRecoveryIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers(); 
        KieServerDeployer.removeLocalArtifact(releaseId);
    }

    @Test
    public void testDeployedBrokenProjectFixAndRedeploy() throws IOException {
        DeploymentDescriptor descriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
        
        descriptor = descriptor.getBuilder()
                .addTaskEventListener(new ObjectModel("mvel", "new org.kie.not.existing.TaskEventListener()", new Object[0]))
                .get();
        
        Map<String, String> content = new HashMap<>();
        content.put("src/main/resources/META-INF/kie-deployment-descriptor.xml", descriptor.toXml());
        content.put("src/main/resources/script-process.bpmn2", readFile("/script-process.bpmn2"));
        KieServerDeployer.createAndDeployKJar(releaseId, content);
        
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID, releaseId);
        client.createContainer(CONTAINER_ID, containerResource);
        ServiceResponse<KieContainerResource> response = client.getContainerInfo(CONTAINER_ID);
        KieServerAssert.assertSuccess(response);
        
        KieContainerResource resource = response.getResult();
        assertEquals("Shound not have any messages", 1, resource.getMessages().size());
        Message message = resource.getMessages().get(0);
        assertEquals("Message should be of type info", Severity.ERROR, message.getSeverity());

        client.disposeContainer(CONTAINER_ID);
        KieServerDeployer.removeLocalArtifact(releaseId);
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        
        client.createContainer(CONTAINER_ID, containerResource);
        
        response = client.getContainerInfo(CONTAINER_ID);
        KieServerAssert.assertSuccess(response);         
        resource = response.getResult();
        assertThat(resource.getMessages()).as("Shound have one messages").hasSize(1);
        message = resource.getMessages().get(0);
        assertThat(message.getSeverity()).as("Message should be of type info").isEqualTo(Severity.INFO);

        client.disposeContainer(CONTAINER_ID);
        
    }

    
    private String readFile(String resourceName) {
        try {
            URI resourceUri = this.getClass().getResource(resourceName).toURI();
            return new String(Files.readAllBytes(Paths.get(resourceUri)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
