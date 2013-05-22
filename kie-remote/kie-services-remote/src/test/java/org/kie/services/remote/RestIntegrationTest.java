/*
 * JBoss, Home of Professional Open Source
 * 
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.services.remote;

import static org.junit.Assert.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.services.client.api.RemoteJmsSessionFactory;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskSummaryListResponse;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(ArquillianJbossServerSetupTask.class)
public class RestIntegrationTest extends IntegrationBase {

    @Deployment(testable = false)
    public static WebArchive createTestWar() {
         return createWebArchive();
    }

    @ArquillianResource
    URL deploymentUrl;

    @Test
    @Ignore
    @InSequence(1)
    public void shouldBeAbleToDeployAndProcessSimpleRestRequest() throws Exception { 
        // create REST request
        String urlString = new URL(deploymentUrl, "/arquillian-test/rest/runtime/test/process/org.jbpm.humantask/start").toExternalForm();
        System.out.println( ">> " + urlString );
        
        ClientRequest restRequest = new ClientRequest(urlString);

        // Get and check response
        ClientResponse responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());

        urlString = deploymentUrl.toExternalForm() + "/rest/task/query?taskOwner=salaboy";
        restRequest = new ClientRequest(urlString);
        responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        // TODO: iterate through
        JaxbTaskSummaryListResponse list = (JaxbTaskSummaryListResponse) responseObj.getEntity(JaxbTaskSummaryListResponse.class);
        long taskId = list.getResult().get(0).getId();
        
        
        urlString = new URL(deploymentUrl, "/arquillian-test/rest/task/" + taskId + "/start?userId=salaboy").toExternalForm();
        System.out.println( ">> " + urlString );
        restRequest = new ClientRequest(urlString);

        // Get response
        responseObj = restRequest.post();

        // Check response
        assertEquals(200, responseObj.getStatus());
//        result = responseObj.getEntity();
//        System.out.println(result);

    }
    
    @Test
    @Ignore
    @InSequence(2)
    public void executeRestRequest() throws Exception { 
        // Start process
        String urlString = new URL(deploymentUrl, "/arquillian-test/rest/runtime/test/execute").toExternalForm();
        System.out.println( ">> " + urlString );
        
        ClientRequest restRequest = new ClientRequest(urlString);
        JaxbCommandsRequest commandMessage = new JaxbCommandsRequest("test", new StartProcessCommand("org.jbpm.humantask"));
        String body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        ClientResponse responseObj = restRequest.post();

        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);

        // query tasks
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest("test", new GetTasksOwnedCommand("salaboy", "en-UK"));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        JaxbCommandsResponse cmdResponse = (JaxbCommandsResponse) responseObj.getEntity(JaxbCommandsResponse.class);
        body = JaxbSerializationProvider.convertJaxbObjectToString(cmdResponse);
        System.out.println(body);
        
        // start task
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(new StartTaskCommand(1, "salaboy"));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        System.out.println(body);
        restRequest.body(MediaType.APPLICATION_XML, commandMessage);

        // Get response
        responseObj = restRequest.post();

        // Check response
        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);

        urlString = new URL(deploymentUrl, "/arquillian-test/rest/task/execute").toExternalForm();
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(new CompleteTaskCommand(1, "salaboy", null));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        System.out.println(body);
        restRequest.body(MediaType.APPLICATION_XML, commandMessage);

        // Get response
        responseObj = restRequest.post();

        // Check response
        System.out.println(responseObj.getStatus());
//        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);

//        System.out.println("Failure now ?");
//        
//        restRequest = new ClientRequest(urlString);
//        commandMessage = new JaxbCommandMessage(null, 1, 
//          new CompleteTaskCommand(1, "salaboy", null));
//        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
//        System.out.println(body);
//        restRequest.body(MediaType.APPLICATION_XML, commandMessage);
//
//        // Get response
//        responseObj = restRequest.post();
//
//        // Check response
//        System.out.println(responseObj.getStatus());
//        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);

    }
    
    @Ignore
    @Test
    public void clientRestRequest() throws Exception {
        System.out.println("clientRestRequest()");
        // create REST request
        RuntimeManager runtimeManager = new RemoteJmsSessionFactory(
            "http://127.0.0.1:8080/arquillian-test", "test").newRuntimeManager();
        RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess("org.jbpm.humantask");
        System.out.println("Started process instance: " + processInstance + " " + (processInstance == null? "" : processInstance.getId()));
        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("salaboy", "en-UK");
        if (tasks.size() != 1) {
        	throw new RuntimeException("Expecting one task " + tasks.size());
        }
        long taskId = tasks.get(0).getId();
        System.out.println("Found task " + taskId);
        System.out.println(taskService.getTaskById(taskId));
        taskService.start(taskId, "salaboy");
        taskService.complete(taskId, "salaboy", null);
        boolean failure = false;
        System.out.println("Now expecting failure");
        try {
        	taskService.complete(taskId, "salaboy", null);
        } catch (Throwable t) {
        	t.printStackTrace();
        	failure = true;
        }
        if (!failure) {
        	throw new RuntimeException("Cannot claim task twice");
    	}
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(processInstance.getId(), statuses, "en-UK");
        if (taskIds.size() != 2) {
        	throw new RuntimeException("Expecting two tasks");
        }
    }
    
}
