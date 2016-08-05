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

package org.kie.server.integrationtests.jbpm.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.KieServicesConfiguration;
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

import static org.junit.Assert.*;

@Category({JMSOnly.class})
public class JmsResponseHandlerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB, jmsConfiguration} ,
                                {MarshallingFormat.JSON, jmsConfiguration},
                                {MarshallingFormat.XSTREAM, jmsConfiguration}
                        }
        ));

        return parameterData;
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, RELEASE_ID);
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
        assertNotNull(response);
        assertNotNull(response.getResponses());
        assertEquals(1, response.getResponses().size());
        KieServerAssert.assertSuccess(response.getResponses().get(0));

        ServiceResponse serviceResponse = response.getResponses().get(0);
        Object result = serviceResponse.getResult();
        assertNotNull(result);

    }

    @Test
    public void testStartProcessUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback callback = new BlockingResponseCallback(marshaller);

        testStartProcessResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);
    }

    @Test
    public void testGetProcessInstancesUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback callback = new BlockingResponseCallback(marshaller);

        testGetProcessInstancesResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        ProcessInstanceList processInstanceList = callback.get(ProcessInstanceList.class);
        assertNotNull(processInstanceList);

        List<ProcessInstance> instances = processInstanceList.getItems();
        assertNotNull(instances);
        assertEquals(1, instances.size());
    }

    @Test
    public void testGetTasksUseOfAsyncResponseHandlerWithMarshaller() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), client.getClassLoader());
        ResponseCallback callback = new BlockingResponseCallback(marshaller);

        testGetTaskResponseHandler(new AsyncResponseHandler(callback));
        // now let's check if response has arrived
        Long processInstanceId = callback.get(Long.class);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        TaskSummaryList taskSummaryList = callback.get(TaskSummaryList.class);
        assertNotNull(taskSummaryList);

        List<TaskSummary> tasks = taskSummaryList.getItems();
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
    }

    /*
     * helper methods that comes with tests that can be invoked with various response handlers
     */

    private void testStartProcessResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertEquals(0, processInstances.size());

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertNull(processInstanceId);

        delay();
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertEquals(1, processInstances.size());

        ProcessInstance pi = processInstances.get(0);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, pi.getState().intValue());

        // set to reqreply so it finishes the test properly
        processClient.setResponseHandler(new RequestReplyResponseHandler());
        processClient.abortProcessInstance(CONTAINER_ID, pi.getId());
    }

    private void testGetProcessInstancesResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertEquals(0, processInstances.size());

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertNull(processInstanceId);

        delay();
        // change response handler for queryClient others are not affected
        queryClient.setResponseHandler(responseHandler);
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertNull(processInstances);

        // set it back for the sake of verification
        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());

        ProcessInstance pi = processInstances.get(0);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, pi.getState().intValue());

        // set to reqreply so it finishes the test properly
        processClient.setResponseHandler(new RequestReplyResponseHandler());
        processClient.abortProcessInstance(CONTAINER_ID, pi.getId());
    }

    private void testGetTaskResponseHandler(ResponseHandler responseHandler) throws Exception {
        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 100);
        assertEquals(0, processInstances.size());

        // change response handler for processClient others are not affected
        processClient.setResponseHandler(responseHandler);
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        // since we use fire and forget there will always be null response
        assertNull(processInstanceId);

        delay();
        // set it back for the sake of verification
        queryClient.setResponseHandler(new RequestReplyResponseHandler());
        // Process should be started completely async - fire and forget.
        processInstances = queryClient.findProcessInstances(0, 100);
        assertNotNull(processInstances);
        assertEquals(1, processInstances.size());

        ProcessInstance pi = processInstances.get(0);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, pi.getState().intValue());

        // change response handler for taskClient others are not affected
        taskClient.setResponseHandler(responseHandler);
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertNull(tasks);

        // set to reqreply so it finishes the test properly
        processClient.setResponseHandler(new RequestReplyResponseHandler());
        processClient.abortProcessInstance(CONTAINER_ID, pi.getId());
    }

    /*
     * since these tests are about async processing on the server we need to introduce delay,
     * even though it might not be reliable...
     */
    private void delay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.debug("InterruptedException caught while delaying execution...");
        }
    }
}
