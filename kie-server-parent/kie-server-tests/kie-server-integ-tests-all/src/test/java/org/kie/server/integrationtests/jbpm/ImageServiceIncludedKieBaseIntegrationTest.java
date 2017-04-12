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
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.*;

public class ImageServiceIncludedKieBaseIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "top-level-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "top-level";
    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";
    private static final String HIRING_3_PROCESS_ID = "hiring3";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/top-level-project").getFile());

        createContainer(CONTAINER_ID, releaseId, new KieServerConfigItem(KieServerConstants.PCFG_KIE_BASE, "customKB", ""));
    }

    @Test
    public void testGetProcessImageViaUIClientTest() throws Exception {
        // image coming from root of dependency kjar
        String result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_PROCESS_ID);
        assertImageContent(result);
        // image coming from org.kie.server package of dependency kjar
        result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertImageContent(result);
        // image coming from org.kie.server package of main kjar
        result = uiServicesClient.getProcessImage(CONTAINER_ID, HIRING_3_PROCESS_ID);
        assertImageContent(result);
    }


    @Test
    public void testGetProcessInstanceImageViaUIClientTest() throws Exception {
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            // image coming from root of dependency kjar
            String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            assertImageContent(result);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }

        processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            // image coming from org.kie.server package of dependency kjar
            String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            assertImageContent(result);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }

        processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_3_PROCESS_ID);
        assertTrue(processInstanceId > 0);
        try {
            // image coming from org.kie.server package of main kjar
            String result = uiServicesClient.getProcessInstanceImage(CONTAINER_ID, processInstanceId);
            assertImageContent(result);
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }

    protected void assertImageContent(String result) {
        logger.debug("Image content is '{}'", result);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
