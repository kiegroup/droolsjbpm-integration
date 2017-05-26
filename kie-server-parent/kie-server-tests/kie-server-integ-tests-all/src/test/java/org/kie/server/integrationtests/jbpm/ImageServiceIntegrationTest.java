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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ImageServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");

    protected static final String CONTAINER_ALIAS = "project";
    private static final String CONTAINER_ID = "definition-project";
    protected static final String CONTAINER_ID_101 = "definition-project-101";

    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101").getFile());

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

    @Test
    public void testGetProcessImageViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessNotExistingImageViaUIClientTest() throws Exception {
        uiServicesClient.getProcessImage(CONTAINER_ID, "not-existing");
    }

    @Test
    public void testGetProcessInstanceImageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
        logger.debug("Image content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();

    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessInstanceNotExistingImageViaUIClientTest() throws Exception {
        uiServicesClient.getProcessInstanceImage(CONTAINER_ID, 9999l);
    }

    @Test
    public void testGetProcessImageInPackageViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_2_PROCESS_ID);
        logger.debug("Image content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetProcessInstanceImageInPackageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
        logger.debug("Image content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();

    }

    @Test
    public void testGetProcessImageViaUIClientWithAliasTest() throws Exception {
        String oldResultViaContainerId = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", oldResultViaContainerId);
        assertThat(oldResultViaContainerId).isNotNull().isNotEmpty();

        String oldResultViaAlias = uiServicesClient.getProcessImage(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", oldResultViaAlias);
        assertThat(oldResultViaAlias).isNotNull().isNotEmpty();

        assertThat(oldResultViaAlias).isEqualTo(oldResultViaContainerId);
        assertThat(oldResultViaAlias).contains("HR Interview");
        assertThat(oldResultViaAlias).doesNotContain("Updated HR");

        createExtraContainer();

        String newResultViaContainerId = uiServicesClient.getProcessImage(CONTAINER_ID_101, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", newResultViaContainerId);
        assertThat(newResultViaContainerId).isNotNull().isNotEmpty();

        String newResultViaAlias = uiServicesClient.getProcessImage(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", newResultViaAlias);
        assertThat(newResultViaAlias).isNotNull().isNotEmpty();

        assertThat(newResultViaAlias).isEqualTo(newResultViaContainerId);
        assertThat(newResultViaAlias).contains("Updated HR");

        assertThat(oldResultViaAlias).isNotEqualTo(newResultViaAlias);

    }

    @Test
    public void testGetProcessInstanceImageViaUIClientWithAliasTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID);

        String oldResultViaContainerId = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
        logger.debug("Image content is '{}'", oldResultViaContainerId);
        assertThat(oldResultViaContainerId).isNotNull().isNotEmpty();

        String oldResultViaAlias = uiServicesClient.getProcessInstanceImage(CONTAINER_ALIAS, processInstanceId);
        logger.debug("Image content is '{}'", oldResultViaAlias);
        assertThat(oldResultViaAlias).isNotNull().isNotEmpty();

        assertThat(oldResultViaAlias).isEqualTo(oldResultViaContainerId);
        assertThat(oldResultViaAlias).contains("HR Interview");
        assertThat(oldResultViaAlias).doesNotContain("Updated HR");

        createExtraContainer();

        processInstanceId = processClient.startProcess(CONTAINER_ALIAS, HIRING_PROCESS_ID);
        assertThat(processInstanceId).isGreaterThan(0);

        pi = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceId);
        assertThat(pi.getContainerId()).isEqualTo(CONTAINER_ID_101);

        String newResultViaContainerId = uiServicesClient.getProcessInstanceImage(CONTAINER_ID_101, processInstanceId);
        logger.debug("Image content is '{}'", newResultViaContainerId);
        assertThat(newResultViaContainerId).isNotNull().isNotEmpty();

        String newResultViaAlias = uiServicesClient.getProcessInstanceImage(CONTAINER_ALIAS, processInstanceId);
        logger.debug("Image content is '{}'", newResultViaAlias);
        assertThat(newResultViaAlias).isNotNull().isNotEmpty();

        assertThat(newResultViaAlias).isEqualTo(newResultViaContainerId);
        assertThat(newResultViaAlias).contains("Updated HR");

        assertThat(oldResultViaAlias).isNotEqualTo(newResultViaAlias);

    }

}
