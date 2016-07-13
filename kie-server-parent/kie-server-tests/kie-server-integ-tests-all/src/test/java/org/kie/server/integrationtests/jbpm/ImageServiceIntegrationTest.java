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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesException;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class ImageServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testGetProcessImageViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        logger.debug("Image content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessNotExistingImageViaUIClientTest() throws Exception {
        uiServicesClient.getProcessImage(CONTAINER_ID, "not-existing");
    }

    @Test
    public void testGetProcessInstanceImageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            logger.debug("Image content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    @Test(expected = KieServicesException.class)
    public void testGetProcessInstanceNotExistingImageViaUIClientTest() throws Exception {
        uiServicesClient.getProcessInstanceImage(CONTAINER_ID, 9999l);
    }

    @Test
    public void testGetProcessImageInPackageViaUIClientTest() throws Exception {
        String result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_2_PROCESS_ID);
        logger.debug("Image content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetProcessInstanceImageInPackageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            logger.debug("Image content is '{}'", result);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
}
