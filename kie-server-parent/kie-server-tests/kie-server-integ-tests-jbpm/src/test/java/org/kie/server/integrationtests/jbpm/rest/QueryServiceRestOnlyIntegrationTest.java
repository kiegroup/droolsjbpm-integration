/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.rest;

import java.util.Collections;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.rest.RestURI.QUERY_URI;
import static org.kie.server.api.rest.RestURI.TASK_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;

public class QueryServiceRestOnlyIntegrationTest extends RestJbpmBaseIntegrationTest {

    protected static final String CONTAINER_ID = "definition-project";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        createContainer(CONTAINER_ID, new ReleaseId("org.kie.server.testing", "definition-project",
                                                    "1.0.0.Final"));
    }

    @Test
    public void testFindTaskById() throws Exception {
        long processId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        ProcessInstance desc = processClient.getProcessInstance(CONTAINER_ID, processId);
        TaskSummary[] tasks = desc.getActiveUserTasks().getTasks();
        assertTrue(tasks.length > 0);
        WebTarget target = newRequest(RestURI.build(TestConfig.getKieServerHttpUrl(), QUERY_URI + "/" + TASK_GET_URI, Collections.singletonMap(TASK_INSTANCE_ID, tasks[0].getId())));
        assertTrue(target.request(MediaType.APPLICATION_JSON_TYPE).get(String.class).contains("null"));
        assertFalse(target.request().header("content-type", MediaType.APPLICATION_JSON + ";fields=not_null,strict=true").get(String.class).contains("null"));
    }
}
