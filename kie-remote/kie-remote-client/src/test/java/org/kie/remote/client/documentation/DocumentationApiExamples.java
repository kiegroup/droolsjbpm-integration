package org.kie.remote.client.documentation;

import java.net.URL;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;

//TODO: changed, add to documentation
public class DocumentationApiExamples {

    public void startProcessAndTaskViaJmsRemoteJavaAPI(URL serverUrl, String deploymentId, String user, String password) {
        // the serverURL should contain a URL similar to "http://localhost:8080/jbpm-console"
        
        // Setup remote JMS runtime engine factory
        RuntimeEngine engine = RemoteRuntimeEngineFactory.newJmsBuilder()
            .addDeploymentId(deploymentId)
            .addJbossServerHostName(serverUrl.getHost())
            .addUserName(user)
            .addPassword(password)
            .build();

        // Interface with JMS api
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess("com.burns.reactor.maintenance.cycle");
        long procId = processInstance.getId();
        TaskService taskService = engine.getTaskService();
        List<Long> tasks = taskService.getTasksByProcessInstanceId(procId);
        taskService.start(tasks.get(0), user);
    }

    public void startProcessAndHandleTaskViaRestRemoteJavaAPI(URL instanceUrl, String deploymentId, String user, String password) {
        // the serverRestUrl should contain a URL similar to "http://localhost:8080/jbpm-console/"
        
        // Setup the factory class with the necessarry information to communicate with the REST services
        RuntimeEngine engine = RemoteRuntimeEngineFactory.newRestBuilder()
            .addUrl(instanceUrl)
            .addDeploymentId(deploymentId)
            .addUserName(user)
            .addPassword(password)
            .build();

        // Create KieSession and TaskService instances and use them
        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        // Each opertion on a KieSession, TaskService or AuditLogService (client) instance 
        // sends a request for the operation to the server side and waits for the response
        // If something goes wrong on the server side, the client will throw an exception. 
        ProcessInstance processInstance 
            = ksession.startProcess("com.burns.reactor.maintenance.cycle");
        long procId = processInstance.getId();

        String taskUserId = user;
        taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");

        long taskId = -1;
        for (TaskSummary task : tasks) {
            if (task.getProcessInstanceId() == procId) {
                taskId = task.getId();
            }
        }

        if (taskId == -1) {
            throw new IllegalStateException("Unable to find task for " + user + " in process instance " + procId);
        }

        taskService.start(taskId, taskUserId);
    }
}
