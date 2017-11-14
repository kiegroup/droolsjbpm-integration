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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.internal.executor.api.STATUS;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.integrationtests.category.Smoke;

import static org.junit.Assert.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.IsEqual.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class JobServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    protected static final String BUSINESS_KEY = "test key";
    protected static final String PRINT_OUT_COMMAND = "org.jbpm.executor.commands.PrintOutCommand";
    protected static final String LOG_CLEANUP_COMMAND = "org.jbpm.executor.commands.LogCleanupCommand";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
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
            KieServerSynchronization.waitForJobToFinish(jobServicesClient, instance.getId());
        }
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testScheduleViewAndCancelJob() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = createJobRequestInstance();
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        RequestInfoInstance expected = createExpectedRequestInfoInstance(jobId, STATUS.QUEUED);
        assertRequestInfoInstance(expected, jobRequest);
        assertNotNull(jobRequest.getScheduledDate());

        jobServicesClient.cancelRequest(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        expected.setStatus(STATUS.CANCELLED.toString());
        assertRequestInfoInstance(expected, jobRequest);
    }

    @Test
    @Category(Smoke.class)
    public void testScheduleAndRunJob() throws Exception {
        JobRequestInstance jobRequestInstance = createJobRequestInstance();

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(BUSINESS_KEY, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
            equalTo(STATUS.QUEUED.toString()),
            equalTo(STATUS.RUNNING.toString()),
            equalTo(STATUS.DONE.toString())));
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());

        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(BUSINESS_KEY, jobRequest.getBusinessKey());
        assertEquals(STATUS.DONE.toString(), jobRequest.getStatus());
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());

    }

    @Test
    public void testScheduleAndRunJobWithCustomTypeFromContainer() throws Exception {
        int currentNumberOfDone = jobServicesClient.getRequestsByContainer(CONTAINER_ID, Collections.singletonList(STATUS.DONE.toString()), 0, 100).size();
        Class<?> personClass = Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader());

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", BUSINESS_KEY);
        data.put("person", createPersonInstance(USER_JOHN));

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(PRINT_OUT_COMMAND);
        jobRequestInstance.setData(data);

        Long jobId = jobServicesClient.scheduleRequest(CONTAINER_ID, jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(BUSINESS_KEY, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
            equalTo(STATUS.QUEUED.toString()),
            equalTo(STATUS.RUNNING.toString()),
            equalTo(STATUS.DONE.toString())));
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());

        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, true);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(BUSINESS_KEY, jobRequest.getBusinessKey());
        assertEquals(STATUS.DONE.toString(), jobRequest.getStatus());
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());


        Map<String, Object> requestData = jobRequest.getData();
        assertNotNull(requestData);
        assertEquals(3, requestData.size());

        assertTrue(requestData.containsKey("person"));
        assertTrue(requestData.containsKey("businessKey"));
        assertTrue(requestData.containsKey("deploymentId"));

        assertTrue(personClass.isAssignableFrom(requestData.get("person").getClass()));
        assertTrue(String.class.isAssignableFrom(requestData.get("businessKey").getClass()));
        assertTrue(String.class.isAssignableFrom(requestData.get("deploymentId").getClass()));

        assertEquals(USER_JOHN, KieServerReflections.valueOf(requestData.get("person"), "name"));
        assertEquals(CONTAINER_ID, requestData.get("deploymentId"));
        assertEquals(BUSINESS_KEY, requestData.get("businessKey"));

        Map<String, Object> responseData = jobRequest.getResponseData();
        assertNotNull(responseData);
        assertEquals(0, responseData.size());

        List<RequestInfoInstance> result = jobServicesClient.getRequestsByContainer(CONTAINER_ID, Arrays.asList(STATUS.QUEUED.name()), 0, 100);
        assertNotNull(result);
        assertEquals(0, result.size());

        result = jobServicesClient.getRequestsByContainer(CONTAINER_ID, Arrays.asList(STATUS.DONE.name()), 0, 100);
        assertNotNull(result);
        assertEquals(1 + currentNumberOfDone, result.size());

    }

    @Test
    public void testScheduleSearchByStatusAndCancelJob() {
        int currentNumberOfCancelled = jobServicesClient.getRequestsByStatus(Collections.singletonList(STATUS.CANCELLED.toString()), 0, 100).size();

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = createJobRequestInstance();
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
        RequestInfoInstance expected = createExpectedRequestInfoInstance(jobId, STATUS.QUEUED);
        assertRequestInfoInstance(expected, jobRequest);
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
        String command = "org.jbpm.executor.commands.DelayedPrintOutCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", BUSINESS_KEY);
        data.put("delay", "wrong-value");
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
        assertEquals(BUSINESS_KEY, jobRequest.getBusinessKey());
        assertThat(jobRequest.getStatus(),anyOf(
                equalTo(STATUS.QUEUED.toString()),
                equalTo(STATUS.RUNNING.toString()),
                equalTo(STATUS.ERROR.toString())));
        assertEquals(command, jobRequest.getCommandName());

        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobId);

        RequestInfoInstance expected = createExpectedRequestInfoInstance(jobId, STATUS.ERROR);
        expected.setCommandName(command);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertRequestInfoInstance(expected, jobRequest);

        jobServicesClient.requeueRequest(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        expected.setStatus(STATUS.QUEUED.toString());
        assertRequestInfoInstance(expected, jobRequest);
    }

    @Test
    public void testScheduleSearchByKeyJob() throws Exception {
        int currentNumberOfRequests = jobServicesClient.getRequestsByBusinessKey(BUSINESS_KEY, 0, 100).size();

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = createJobRequestInstance();
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue(jobId.longValue() > 0);

        List<RequestInfoInstance> result = jobServicesClient.getRequestsByBusinessKey(BUSINESS_KEY, 0, 100);
        assertNotNull(result);
        assertEquals(1 + currentNumberOfRequests, result.size());

        List<RequestInfoInstance> queuedJobs = result.stream().
                filter(n -> n.getStatus().equals(STATUS.QUEUED.name())).collect(Collectors.toList());

        assertNotNull(queuedJobs);
        assertEquals(1, queuedJobs.size());

        RequestInfoInstance expected = createExpectedRequestInfoInstance(jobId, STATUS.QUEUED);
        RequestInfoInstance queuedJob = queuedJobs.get(0);
        assertRequestInfoInstance(expected, queuedJob);

        result = jobServicesClient.getRequestsByBusinessKey(BUSINESS_KEY, Arrays.asList(STATUS.QUEUED.name()),  0, 100);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertRequestInfoInstance(expected, result.get(0));

        jobServicesClient.cancelRequest(jobId);
    }

    @Test
    public void testScheduleSearchByCommandCancelJob() throws Exception {
        String firstCommand = PRINT_OUT_COMMAND;
        String secondCommand = LOG_CLEANUP_COMMAND;

        int originalNumberOfSecondCommands = jobServicesClient.getRequestsByCommand(secondCommand, 0, 100).size();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", BUSINESS_KEY);

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

        numberOfSecondCommands = jobServicesClient.getRequestsByCommand(secondCommand, Arrays.asList(STATUS.QUEUED.name()), 0, 100).size();
        assertEquals(1, numberOfSecondCommands);

        jobServicesClient.cancelRequest(jobId);
    }

    @Test
    public void testScheduleViewUpdateDataAndCancelJob() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", BUSINESS_KEY);
        data.put("customValue", "just a simple value");
        data.put("processInstanceId", 1234);

        JobRequestInstance jobRequestInstance = createJobRequestInstance();
        jobRequestInstance.setScheduledDate(tomorrow.getTime());
        jobRequestInstance.setData(data);

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, true);
        RequestInfoInstance expected = createExpectedRequestInfoInstance(jobId, STATUS.QUEUED);
        assertRequestInfoInstance(expected, jobRequest);
        assertNotNull(jobRequest.getScheduledDate());

        Map<String, Object> jobsData = jobRequest.getData();
        assertNotNull(jobsData);
        assertEquals("just a simple value", jobsData.get("customValue"));
        assertEquals(1234, jobsData.get("processInstanceId"));

        Map<String, Object> updates = new HashMap<>();
        updates.put("customValue", "updated string");

        jobServicesClient.updateRequestData(jobId, null, updates);
        jobRequest = jobServicesClient.getRequestById(jobId, false, true);
        jobsData = jobRequest.getData();
        assertNotNull(jobsData);
        assertEquals("updated string", jobsData.get("customValue"));
        assertEquals(1234, jobsData.get("processInstanceId"));

        List<RequestInfoInstance> processRequests = jobServicesClient.getRequestsByProcessInstance(1234L, Arrays.asList(STATUS.QUEUED.name()), 0, 100);
        assertNotNull(processRequests);
        assertEquals(1, processRequests.size());

        jobServicesClient.cancelRequest(jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        expected.setStatus(STATUS.CANCELLED.toString());
        assertRequestInfoInstance(expected, jobRequest);
    }

    private void assertRequestInfoInstance(RequestInfoInstance expected, RequestInfoInstance actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBusinessKey(), actual.getBusinessKey());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCommandName(), actual.getCommandName());
    }

    private RequestInfoInstance createExpectedRequestInfoInstance(Long jobId, STATUS expected) {
        return RequestInfoInstance.builder()
                .id(jobId)
                .businessKey(BUSINESS_KEY)
                .status(expected.toString())
                .command(PRINT_OUT_COMMAND)
                .build();
    }

    private JobRequestInstance createJobRequestInstance() {
        Map<String, Object> data = new HashMap<>();
        data.put("businessKey", BUSINESS_KEY);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(PRINT_OUT_COMMAND);
        jobRequestInstance.setData(data);
        return jobRequestInstance;
    }

    @Test
    public void testExecutorServiceDisabling() throws Exception {
        String command = "invalidCommand";

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("businessKey", BUSINESS_KEY);

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, 1);

        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(command);
        jobRequestInstance.setData(data);
        jobRequestInstance.setScheduledDate(tomorrow.getTime());

        // Executing fist command.
        try {
            jobServicesClient.scheduleRequest(jobRequestInstance);
        } catch (Exception e){
            assertTrue(e instanceof KieServicesException);
            assertTrue(e.getMessage().contains("Invalid command type"));
        }

    }

    @Test
    public void testScheduleAndRunJobWithoutData() throws Exception {
        JobRequestInstance jobRequestInstance = new JobRequestInstance();
        jobRequestInstance.setCommand(PRINT_OUT_COMMAND);

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue( jobId.longValue() > 0);

        RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertThat(jobRequest.getStatus(),anyOf(
                equalTo(STATUS.QUEUED.toString()),
                equalTo(STATUS.RUNNING.toString()),
                equalTo(STATUS.DONE.toString())));
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());

        KieServerSynchronization.waitForJobToFinish(jobServicesClient, jobId);

        jobRequest = jobServicesClient.getRequestById(jobId, false, false);
        assertNotNull(jobRequest);
        assertEquals(jobId, jobRequest.getId());
        assertEquals(STATUS.DONE.toString(), jobRequest.getStatus());
        assertEquals(PRINT_OUT_COMMAND, jobRequest.getCommandName());
    }

    @Test
    public void testScheduleAndRunJobWithWorkItem() throws Exception {
        JobRequestInstance jobRequestInstance = createJobRequestInstance();
        final WorkItemImpl workItem = new WorkItemImpl();
        workItem.setId(1);
        workItem.setName("testWorkItemName");
        workItem.setDeploymentId("test-1.0.0");
        workItem.setState(1);
        jobRequestInstance.getData().put("workItem",
                                         workItem);

        Long jobId = jobServicesClient.scheduleRequest(jobRequestInstance);
        assertNotNull(jobId);
        assertTrue(jobId.longValue() > 0);

        KieServerSynchronization.waitForJobToFinish(jobServicesClient,
                                                    jobId);

        final RequestInfoInstance jobRequest = jobServicesClient.getRequestById(jobId,
                                                                                true,
                                                                                true);
        assertNotNull(jobRequest);
        assertEquals(jobId,
                     jobRequest.getId());
        assertEquals(STATUS.DONE.toString(),
                     jobRequest.getStatus());
        assertEquals(PRINT_OUT_COMMAND,
                     jobRequest.getCommandName());
        assertNotNull(jobRequest.getData().get("workItem"));
    }
}
