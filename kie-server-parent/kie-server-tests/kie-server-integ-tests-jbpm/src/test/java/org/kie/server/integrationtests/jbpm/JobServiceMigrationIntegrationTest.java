/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.admin.ExecutionErrorInstance;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.client.JobServicesClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.internal.executor.api.STATUS.DONE;
import static org.kie.internal.executor.api.STATUS.ERROR;

public class JobServiceMigrationIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final String PROCESS_ID = "wih.process";
    private static final String KJARS_SOURCES_PATH = "/kjars-sources/";
    private static final String CONTAINER_ID1 = "async-exec-project";
    private static final String CONTAINER_ID2 = "async-exec-project-101";

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "async-exec-project",
            "1.0.0.Final");

    private static ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "async-exec-project",
            "1.0.1.Final");

    private static JobServicesClient jsc = createDefaultStaticClient().getServicesClient(JobServicesClient.class);

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource(KJARS_SOURCES_PATH+CONTAINER_ID1);
        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        
        KieServerDeployer.buildAndDeployMavenProjectFromResource(KJARS_SOURCES_PATH+CONTAINER_ID2);
        kieContainer = KieServices.Factory.get().newKieContainer(releaseId101);

        createContainer(CONTAINER_ID1, releaseId);
        createContainer(CONTAINER_ID2, releaseId101);
    }
    
    @AfterClass
    public static void disposeAndCleanUp() throws Exception {
        disposeAllContainers();
    }

    @After
    public void cleanUpExecutionErrors() throws Exception {
        long jobId = jsc.scheduleRequest(JobRequestInstance.builder().command("org.jbpm.executor.commands.ExecutionErrorCleanupCommand").build());
        KieServerSynchronization.waitForJobToFinish(jsc, jobId, 15000L);
    }
    
    @Test
    public void testMigrateAndRequeueFailingJob() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ID1, PROCESS_ID);
        
        waitForErrors(CONTAINER_ID1, 5000L);
        
        // first container has a wrong deployment-structure (without CustomWorkItemHandler definition), so job ends up in Error status
        List<RequestInfoInstance> errors = jobServicesClient.getRequestsByContainer(CONTAINER_ID1, singletonList(ERROR.toString()), 0, 10);
        
        Long jobId = errors.get(0).getId();
        
        // migrate process instance to container 2 (right deployment-structure containing the CustomWorkItemHandler definition)
        MigrationReportInstance report = processAdminClient.migrateProcessInstance(CONTAINER_ID1, pid, CONTAINER_ID2, PROCESS_ID);
        assertNotNull(report);
        assertTrue(report.isSuccessful());
        
        jobServicesClient.requeueRequest(jobId);
        
        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobId);
        
        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals("org.jbpm.process.core.async.AsyncSignalEventCommand", jobRequest.getCommandName());
        assertEquals(DONE.toString(), jobRequest.getStatus());
    }

    private void waitForErrors(String containerId, long timeOut) throws Exception {
        KieServerSynchronization.waitForCondition(() -> {
            List<ExecutionErrorInstance> list = processAdminClient.getErrors(containerId, false, 0, 10);
            // There are 4 attempts (3 retries by default plus 1 normal), so the same number of errors.
            if (list.size()==4) {
                return true;
            }
            return false;
        }, timeOut);
    }
}
