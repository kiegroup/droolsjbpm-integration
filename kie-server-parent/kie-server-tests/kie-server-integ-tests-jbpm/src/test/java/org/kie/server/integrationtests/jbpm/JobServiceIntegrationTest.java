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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.internal.executor.api.STATUS;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.integrationtests.category.Smoke;

import static org.junit.Assert.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.IsEqual.*;

public class JobServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Before
    public void finishAllJobs() throws Exception {
        List<String> status = new ArrayList<String>();
        status.add(STATUS.QUEUED.toString());
        status.add(STATUS.RUNNING.toString());
        status.add(STATUS.RETRYING.toString());
        List<RequestInfoInstance> requests = jobServicesClient.getRequestsByStatus(status, 0, 100);
        for (RequestInfoInstance instance : requests) {
            jobServicesClient.cancelRequest(instance.getId());
            waitForJobToFinish(instance.getId());
        }
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testScheduleViewAndCancelJob() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        String businessKey = "test key";
        String command = "org.jbpm.executor.commands.PrintOutCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.QUEUED.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());
        assertNotNull(jobRequest.getScheduledDate());

        jobServicesClient.cancelRequest(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.CANCELLED.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());
    }

    @Test
    @Category(Smoke.class)
    public void testScheduleAndRunJob() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        String businessKey = "test key";
        String command = "org.jbpm.executor.commands.PrintOutCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
            equalTo(STATUS.QUEUED.toString()),
            equalTo(STATUS.RUNNING.toString()),
            equalTo(STATUS.DONE.toString())));
        assertEquals(command, jobRequest.getCommandName());

        waitForJobToFinish(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.DONE.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());

    }

    @Test
    public void testScheduleAndRunJobWithCustomTypeFromContainer() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Class<?> personClass = Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader());
        String businessKey = "test key";
        String command = "org.jbpm.executor.commands.PrintOutCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);
        data.put("person", createPersonInstance("john"));

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);

        Long jobId = jobServicesClient.scheduleRequest("definition-project", jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
            equalTo(STATUS.QUEUED.toString()),
            equalTo(STATUS.RUNNING.toString()),
            equalTo(STATUS.DONE.toString())));
        assertEquals(command, jobRequest.getCommandName());

        waitForJobToFinish(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, true);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.DONE.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());


        Map<String, Object> requestData = jobRequest.getData();
        assertNotNull(requestData);
        assertEquals(3, requestData.size());

        assertTrue(requestData.containsKey("person"));
        assertTrue(requestData.containsKey("businessKey"));
        assertTrue(requestData.containsKey("deploymentId"));

        assertTrue(personClass.isAssignableFrom(requestData.get("person").getClass()));
        assertTrue(String.class.isAssignableFrom(requestData.get("businessKey").getClass()));
        assertTrue(String.class.isAssignableFrom(requestData.get("deploymentId").getClass()));

        assertEquals("john", valueOf(requestData.get("person"), "name"));
        assertEquals("definition-project", requestData.get("deploymentId"));
        assertEquals(businessKey, requestData.get("businessKey"));

        Map<String, Object> responseData = jobRequest.getResponseData();
        assertNotNull(responseData);
        assertEquals(0, responseData.size());

    }

    @Test
    public void testScheduleSearchByStatusAndCancelJob() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        int currentNumberOfCancelled = jobServicesClient.getRequestsByStatus(Collections.singletonList(STATUS.CANCELLED.toString()), 0, 100).size();

        String businessKey = "test key";
        String command = "org.jbpm.executor.commands.PrintOutCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        List<String> status = new ArrayList<String>();
        status.add(STATUS.QUEUED.toString());

        List<RequestInfoInstance> result = jobServicesClient.getRequestsByStatus(status, 0, 100);
        assertNotNull(result);
        assertEquals(1, result.size());

        RequestInfoInstance jobRequest = result.get(0);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.QUEUED.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());
        assertNotNull(jobRequest.getScheduledDate());

        jobServicesClient.cancelRequest(jobId);

        result = jobServicesClient.getRequestsByStatus(status, 0, 100);
        assertNotNull(result);
        assertEquals(0, result.size());

        // clear status to search only for canceled
        status.clear();
        status.add(STATUS.CANCELLED.toString());

        result = jobServicesClient.getRequestsByStatus(status, 0, 100);
        assertNotNull(result);
        assertEquals(1 + currentNumberOfCancelled, result.size());
    }

    @Test
    public void testScheduleAndRequeueJob() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        String businessKey = "test key";
        String command = "org.jbpm.executor.commands.PrintOutCommand123";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);
        data.put("retries", 0);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
            equalTo(STATUS.QUEUED.toString()),
            equalTo(STATUS.RUNNING.toString()),
            equalTo(STATUS.ERROR.toString())));
        assertEquals(command, jobRequest.getCommandName());

        waitForJobToFinish(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.ERROR.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());

        jobServicesClient.requeueRequest(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(businessKey, jobRequest.getBusinessKey());
        assertEquals(STATUS.QUEUED.toString(), jobRequest.getStatus());
        assertEquals(command, jobRequest.getCommandName());

        waitForJobToFinish(jobId);
    }

    @Test
    public void testScheduleSearchByKeyJob() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        String businessKey = "testkey";
        String command = "org.jbpm.executor.commands.PrintOutCommand";

        int currentNumberOfRequests = jobServicesClient.getRequestsByBusinessKey(businessKey, 0, 100).size();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue(jobId.longValue() > 0);

        List<RequestInfoInstance> result = jobServicesClient.getRequestsByBusinessKey(businessKey, 0, 100);
        assertNotNull(result);
        assertEquals(1 + currentNumberOfRequests, result.size());

        List<RequestInfoInstance> queuedJobs = new ArrayList<RequestInfoInstance>();
        for(RequestInfoInstance job : result) {
            if(job.getStatus().equals(STATUS.QUEUED.toString())) {
                queuedJobs.add(job);
            }
        }

        assertEquals(1, queuedJobs.size());

        RequestInfoInstance queuedJob = queuedJobs.get(0);
        assertEquals(jobId, queuedJob.getId());
        assertEquals(businessKey, queuedJob.getBusinessKey());
        assertEquals(STATUS.QUEUED.toString(), queuedJob.getStatus());
        assertEquals(command, queuedJob.getCommandName());

        jobServicesClient.cancelRequest(jobId);
    }

    @Test
    public void testScheduleSearchByCommandCancelJob() throws Exception {
        String firstCommand = "org.jbpm.executor.commands.PrintOutCommand";
        String secondCommand = "org.jbpm.executor.commands.PrintOutCommand123";
        String businessKey = "test key";

        int originalNumberOfSecondCommands = jobServicesClient.getRequestsByCommand(secondCommand, 0, 100).size();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", businessKey);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(firstCommand);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        // Executing fist command.
        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        // Number of commands should be same as we are checking second command.
        int numberOfSecondCommands = jobServicesClient.getRequestsByCommand(secondCommand, 0, 100).size();
        assertEquals(originalNumberOfSecondCommands, numberOfSecondCommands);

        jobServicesClient.cancelRequest(jobId);

        jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(secondCommand);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        // Executing second command.
        jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        // Number of commands should raise.
        numberOfSecondCommands = jobServicesClient.getRequestsByCommand(secondCommand, 0, 100).size();
        assertEquals(1 + originalNumberOfSecondCommands, numberOfSecondCommands);

        jobServicesClient.cancelRequest(jobId);
    }

}
