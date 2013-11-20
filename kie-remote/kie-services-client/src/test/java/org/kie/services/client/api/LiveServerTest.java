package org.kie.services.client.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class LiveServerTest {

    protected static Logger logger = LoggerFactory.getLogger(LiveServerTest.class);

    String deploymentId = "org.jbpm:Evaluation:1.0";
    URL deploymentUrl;

    String userId = "mary";
    String password = "mary123@";

    public LiveServerTest() throws Exception {
        this.deploymentUrl = new URL("http://localhost:8080/kie-wb/");
    }

    /**
     * Works with the Evaluation demo.
     */
    @Test
    public void remoteRestApi() {
        String taskUserId = userId;
        
        RemoteRestRuntimeFactory restSessionFactory 
            = new RemoteRestRuntimeFactory(deploymentId, deploymentUrl, userId, password);

        // create REST request
        RuntimeEngine engine = restSessionFactory.newRuntimeEngine();
        KieSession ksession = engine.getKieSession();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", taskUserId);
        ProcessInstance processInstance = ksession.startProcess("evaluation", params);

        logger.debug("Started process instance: " + processInstance + " "
                + (processInstance == null ? "" : processInstance.getId()));

        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        long taskId = findTaskId(processInstance.getId(), tasks);

        logger.debug("Found task " + taskId);
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId + ": " + task);
        taskService.start(taskId, taskUserId);
        taskService.complete(taskId, taskUserId, null);

        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Completed);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(processInstance.getId(), statuses, "en-UK");
        assertEquals("Expected 1 tasks.", 1, taskIds.size());
    }

    protected long findTaskId(long procInstId, List<TaskSummary> taskSumList) {
        long taskId = -1;
        for (TaskSummary task : taskSumList) {
            if (task.getProcessInstanceId() == procInstId) {
                taskId = task.getId();
            }
        }
        assertNotEquals("Could not determine taskId!", -1, taskId);
        return taskId;
    }

    @Test
    // BZ 994905
    public void anonymousTaskInitiatorTest() throws Exception {

        boolean like_BZ_994905 = false;

        ClientRequestFactory requestFactory;
        if (like_BZ_994905) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                    new UsernamePasswordCredentials(userId, password));
            ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
            requestFactory = new ClientRequestFactory(clientExecutor, ResteasyProviderFactory.getInstance());
        } else {
            requestFactory = RestRequestHelper.createRestRequestFactory(deploymentUrl, userId, password);
        }

        // Create (start process) request
        String urlString = new URL(deploymentUrl, 
                deploymentUrl.getPath() + "rest/runtime/" + deploymentId + "/process/evaluation/start").toExternalForm();
        urlString = urlString + "?map_employee=mary";
        RemoteRestRuntimeFactory restSessionFactory 
            = new RemoteRestRuntimeFactory(deploymentId, deploymentUrl, userId, password);
        ClientRequest restRequest = requestFactory.createRequest(urlString);
        logger.debug(">> " + urlString);
        
        // Post, get response, check status response, and get info
        ClientResponse<?> responseObj = checkResponse(restRequest.post());
        JaxbProcessInstanceResponse processInstance = (JaxbProcessInstanceResponse) responseObj
                .getEntity(JaxbProcessInstanceResponse.class);
        long procInstId = processInstance.getId();

        // Check that task has correct info
        RuntimeEngine engine = restSessionFactory.newRuntimeEngine();
        TaskService taskService = engine.getTaskService();
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        List<Task> tasks = new ArrayList<Task>();
        for( long taskId : taskIds ) { 
            Task gottenTask = taskService.getTaskById(taskId);
            tasks.add(gottenTask);
        }
        assertEquals("Number of tasks: ", 1, tasks.size() );
        assertEquals("Potential owner of task: ", userId, tasks.get(0).getPeopleAssignments().getPotentialOwners().get(0).getId());
    }

    /**
     * Helper methods
     */

    private ClientResponse<?> checkResponse(ClientResponse<?> responseObj) throws Exception {
        responseObj.resetStream();
        int status = responseObj.getStatus();
        if (status != 200) {
            logger.warn("Response with exception:\n" + responseObj.getEntity(String.class));
            assertEquals("Status OK", 200, status);
        }
        return responseObj;
    }

}
