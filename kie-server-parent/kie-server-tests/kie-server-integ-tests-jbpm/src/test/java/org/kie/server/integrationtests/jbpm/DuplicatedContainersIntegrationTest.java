/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DuplicatedContainersIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @After
    public void cleanContainers() {
        disposeAllContainers();
    }

    @Test
    public void testGetProcessDefinitionsByContainerDuplicatedContainers() throws Exception {
        createContainer(CONTAINER_ID, releaseId);

        List<ProcessDefinition> definitions = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 20);
        assertThat(definitions).hasSize(12);

        createContainer(CONTAINER_ID_V2, releaseId);

        definitions = queryClient.findProcessesByContainerId(CONTAINER_ID, 0, 20);
        assertThat(definitions).hasSize(12);
        definitions = queryClient.findProcessesByContainerId(CONTAINER_ID_V2, 0, 20);
        assertThat(definitions).hasSize(12);
    }
}
