package org.kie.services.client.api;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class LiveServeApiIntegrationTest {

    protected static Logger logger = LoggerFactory.getLogger(LiveServeApiIntegrationTest.class);

    String deploymentId = "org.jbpm:Evaluation:1.0";
    URL deploymentUrl; 
    
    String user = "mary";
    String password = "mary123@";
    
    String taskUserId = "mary";
    
    public LiveServeApiIntegrationTest() throws Exception { 
        this.deploymentUrl = new URL( "http://localhost:8080/kie-wb/" );
    }
    
    /**
     * Works with the Evaluation demo.
     */
    @Test
    public void remoteRestApi() { 
        RemoteRestSessionFactory restSessionFactory 
            = new RemoteRestSessionFactory(deploymentId, deploymentUrl.toExternalForm(), user, password);

        // create REST request
        RuntimeEngine engine = restSessionFactory.newRuntimeEngine();
        KieSession ksession = engine.getKieSession();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", taskUserId);
        ProcessInstance processInstance = ksession.startProcess("evaluation", params);
        
        logger.debug("Started process instance: " + processInstance + " " + (processInstance == null? "" : processInstance.getId()));
        
        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
        long taskId = findTaskId(processInstance.getId(), tasks);
        
        logger.debug("Found task " + taskId);
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId + ": " + task );
        taskService.start(taskId, taskUserId);
        taskService.complete(taskId, taskUserId, null);
        
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Completed);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(processInstance.getId(), statuses, "en-UK");
        assertEquals("Expected 1 tasks.", 1, taskIds.size());
    }
    
    protected long findTaskId(long procInstId, List<TaskSummary> taskSumList) { 
        long taskId = -1;
        for( TaskSummary task : taskSumList ) { 
            if( task.getProcessInstanceId() == procInstId ) {
                taskId = task.getId();
            }
        }
        assertNotEquals("Could not determine taskId!", -1, taskId);
        return taskId;
    }
}
