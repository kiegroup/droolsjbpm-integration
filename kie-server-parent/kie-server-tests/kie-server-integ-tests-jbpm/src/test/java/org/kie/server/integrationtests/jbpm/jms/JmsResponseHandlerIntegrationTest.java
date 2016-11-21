/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.jms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.task.model.Status;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.RequestReplyResponseHandler;
import org.kie.server.client.jms.ResponseCallback;
import org.kie.server.client.jms.ResponseHandler;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

@Category({JMSOnly.class})
public class JmsResponseHandlerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
        Collection<Object[]> parameterData = new ArrayList<>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration},
                {MarshallingFormat.JSON, jmsConfiguration},
                {MarshallingFormat.XSTREAM, jmsConfiguration}
        }));

        return parameterData;
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/query-definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(RELEASE_ID);

        createContainer(CONTAINER_ID, RELEASE_ID);
    }

    @After
    public void resetResponseHandler() {
        processClient.setResponseHandler(new RequestReplyResponseHandler());
        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        taskClient.setResponseHandler(new RequestReplyResponseHandler());
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartProcessUseOfFireAndForgetResponseHandler() throws Exception {
        testStartProcessResponseHandler(new FireAndForgetResponseHandler());
    }

    @Test
    public void testStartProcessUseOfAsyncResponseHandler() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(null);

        testStartProcessResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        ServiceResponsesList response = callback.get();
        assertThat(response).isNotNull();
        assertThat(response.getResponses()).isNotNull().hasSize(1);
        KieServerAssert.assertSuccess(response.getResponses().get(0));

        ServiceResponse<? extends Object> serviceResponse = response.getResponses().get(0);
        Object result = serviceResponse.getResult();
        assertThat(result).isNotNull();
    }

    @Test
    public void testStartProcessUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(createMarshaller());

        testStartProcessResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertThat(processInstanceId).isNotNull().isPositive();
    }

    @Test
    public void testGetProcessInstancesUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(createMarshaller());

        testGetProcessInstancesResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertThat(processInstanceId).isNotNull().isPositive();

        ProcessInstanceList processInstanceList = callback.get(ProcessInstanceList.class);
        assertThat(processInstanceList).isNotNull();
        assertThat(processInstanceList.getItems()).isNotNull().hasSize(1);
    }

    @Test
    public void testGetTasksUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(createMarshaller());

        testGetTaskResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertThat(processInstanceId).isNotNull().isPositive();

        TaskSummaryList taskSummaryList = callback.get(TaskSummaryList.class);
        assertThat(taskSummaryList).isNotNull();
        assertThat(taskSummaryList.getItems()).isNotNull().hasSize(1);
    }

    @Test
    public void testStartAndCompleteTaskUseOfFireAndForgetResponseHandler() throws Exception {
        testStartAndCompleteTask(new FireAndForgetResponseHandler());
    }

    @Test
    public void testStartAndCompleteTaskUseOfAsyncResponseHandler() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(null);
        testStartAndCompleteTask(new AsyncResponseHandler(callback));
    }

    @Test
    public void testQueryRegistrationUseOfFireAndForgetResponseHandler() throws Exception {
        testQueryRegistration(new FireAndForgetResponseHandler());
    }

    @Test
    public void testQueryRegistrationUseOfAsyncResponseHandler() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(null);
        testQueryRegistration(new AsyncResponseHandler(callback));
    }

    @Test
    public void testGlobalConfigurationOfFireAndForgetResponseHandler() throws Exception {
        testStartProcessWithGlobalConfiguration(new FireAndForgetResponseHandler());
    }

    @Test
    public void testGlobalConfigurationOfAsyncResponseHandler() throws Exception {
        ResponseCallback callback = new BlockingResponseCallback(createMarshaller());
        testStartProcessWithGlobalConfiguration(new AsyncResponseHandler(callback));

        Long processInstanceId = callback.get(Long.class);
        assertThat(processInstanceId).isNotNull().isPositive();
    }

    /*
     * helper methods that comes with tests that can be invoked with various response handlers
     */

    private void testStartProcessResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isEmpty();

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertThat(processInstanceId).isNull();

        KieServerSynchronization.waitForProcessInstanceStart(queryClient, CONTAINER_ID);

        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).hasSize(1);

        ProcessInstance pi = processInstances.get(0);
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
    }

    private void testGetProcessInstancesResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isEmpty();

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertThat(processInstanceId).isNull();

        KieServerSynchronization.waitForProcessInstanceStart(queryClient, CONTAINER_ID);

        // change response handler for queryClient others are not affected
        queryClient.setResponseHandler(responseHandler);
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isNull();

        // set it back for the sake of verification
        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isNotNull().hasSize(1);

        ProcessInstance pi = processInstances.get(0);
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
    }

    private void testGetTaskResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isEmpty();

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertThat(processInstanceId).isNull();

        KieServerSynchronization.waitForProcessInstanceStart(queryClient, CONTAINER_ID);

        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isNotNull().hasSize(1);

        ProcessInstance pi = processInstances.get(0);
        assertThat(pi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        // change response handler for taskClient others are not affected
        taskClient.setResponseHandler(responseHandler);
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).isNull();
    }

    private void testStartAndCompleteTask(ResponseHandler responseHandler) throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertThat(processInstanceId).isNotNull();

        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertThat(processInstances).isNotNull().hasSize(1);
        assertThat(processInstances.get(0)).isNotNull();
        assertThat(processInstances.get(0).getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        Long taskId = tasks.get(0).getId();
        taskClient.setResponseHandler(responseHandler);
        taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);

        taskClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForTaskStatus(taskClient, taskId, Status.InProgress.name());

        taskClient.setResponseHandler(responseHandler);
        taskClient.completeTask(CONTAINER_ID, taskId, USER_YODA, new HashMap<String, Object>());

        taskClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForTaskStatus(taskClient, taskId, Status.Completed.name());

        tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        taskId = tasks.get(0).getId();
        taskClient.setResponseHandler(responseHandler);
        taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);

        taskClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForTaskStatus(taskClient, taskId, Status.InProgress.name());

        taskClient.setResponseHandler(responseHandler);
        taskClient.completeTask(CONTAINER_ID, taskId, USER_YODA, new HashMap<String, Object>());

        KieServerSynchronization.waitForProcessInstanceToFinish(processClient, CONTAINER_ID, processInstanceId);
    }

    private void testQueryRegistration(ResponseHandler responseHandler) throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance(CONTAINER_ID));

        processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK, parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("getTasksByState");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from AuditTaskImpl where status = 'Reserved'");
        query.setTarget("CUSTOM");

        queryClient.setResponseHandler(responseHandler);
        queryClient.registerQuery(query);

        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForQuery(queryClient, query);

        List<TaskInstance> tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
        assertThat(tasks).isNotNull().hasSize(1);
        Long taskId = tasks.get(0).getId();

        query.setExpression("select * from AuditTaskImpl where status = 'InProgress'");

        queryClient.setResponseHandler(responseHandler);
        queryClient.replaceQuery(query);

        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForQuery(queryClient, query);

        tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
        assertThat(tasks).isNotNull().isEmpty();

        taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);

        tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
        assertThat(tasks).isNotNull().hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo(taskId);

        queryClient.setResponseHandler(responseHandler);
        queryClient.unregisterQuery(query.getName());

        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        KieServerSynchronization.waitForQueryRemoval(queryClient, query);
    }

    private void testStartProcessWithGlobalConfiguration(ResponseHandler responseHandler) throws Exception {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
        jmsConfiguration.setMarshallingFormat(marshallingFormat);
        jmsConfiguration.setResponseHandler(responseHandler);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(jmsConfiguration);
        ProcessServicesClient fireAndForgetProcessClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);

        Long processInstanceId = fireAndForgetProcessClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertThat(processInstanceId).isNull();

        KieServerSynchronization.waitForProcessInstanceStart(queryClient, CONTAINER_ID);

        abortAllProcesses();
    }

    private Marshaller createMarshaller() {
        return MarshallerFactory.getMarshaller(new HashSet<>(extraClasses.values()),
                configuration.getMarshallingFormat(), client.getClassLoader());
    }

}
