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
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;

import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
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
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskSummaryListResponse;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(ArquillianJbossServerSetupTask.class)
public class RestAndJmsIntegrationTest extends IntegrationBase {

    private static Logger logger = LoggerFactory.getLogger(RestAndJmsIntegrationTest.class);

    private static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    private static final String DOMAIN_TASK_QUEUE_NAME = "jms/queue/KIE.TASK.DOMAIN.TEST";
    private static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    private static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";

    @Deployment(testable = false)
    public static Archive<?> createWar() {
       return createWebArchive();
    }
    
    /**
     * Initializes a (remote) IntialContext instance.
     * 
     * @return a remote {@link InitialContext} instance
     */
    private static InitialContext getRemoteInitialContext() {
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://localhost:4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, "guest");
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, "1234");
        
        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }

    @ArquillianResource
    URL deploymentUrl;

    private static final long QUALITY_OF_SERVICE_THRESHOLD_MS = 5 * 1000;
    
    @AfterClass
    public static void waitForTxOnServer() throws InterruptedException { 
        Thread.sleep(1000);
    }
   
    @Test
    @InSequence(0)
    public void testJmsStartProcess() throws Exception {
        // send cmd
        Command<?> cmd = new StartProcessCommand("org.jbpm.humantask"); 
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        JaxbCommandsResponse response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        // check response 
        assertNotNull("response was null.", response);
        assertTrue("response did not contain any command responses",  response.getResponses() != null && response.getResponses().size() > 0);
        JaxbCommandResponse<?> cmdResponse = response.getResponses().get(0);
        assertTrue( "response is not the proper class type : " + cmdResponse.getClass().getSimpleName(), cmdResponse instanceof JaxbProcessInstanceResponse );
        ProcessInstance procInst = (ProcessInstance) cmdResponse;
        long procInstId = procInst.getId();
       
        // send cmd
        cmd = new GetTasksByProcessInstanceIdCommand(procInstId);
        req = new JaxbCommandsRequest("test", cmd);
        response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        // check response 
        assertNotNull("response was null.", response);
        assertTrue("response did not contain any command responses",  response.getResponses() != null && response.getResponses().size() > 0);
        cmdResponse = response.getResponses().get(0);
        assertTrue( "response is not the proper class type : " + cmdResponse.getClass().getSimpleName(), cmdResponse instanceof JaxbLongListResponse );
        long taskId = ((JaxbLongListResponse) cmdResponse).getResult().get(0);
        
        // send cmd
        cmd = new StartTaskCommand(taskId, "salaboy");
        req = new JaxbCommandsRequest("test", cmd);
        req.getCommands().add(new CompleteTaskCommand(taskId, "salaboy", null));
        response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        // check response 
        assertNotNull("response was null.", response);
        assertTrue("response list was not empty", response.getResponses().size() == 0);
        
        // send cmd
        cmd = new GetTasksOwnedCommand("salaboy", "en-UK");
        req = new JaxbCommandsRequest("test", cmd);
        req.getCommands().add(new GetTasksOwnedCommand("bob", "fr-CA"));
        req.getCommands().add(new GetProcessInstanceCommand(procInstId));
        response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        assertNotNull("response was null.", response);
        assertTrue("response did not contain any command responses",  response.getResponses() != null && response.getResponses().size() > 0);
        cmdResponse = response.getResponses().get(0);
        assertTrue( "response is not the proper class type : " + cmdResponse.getClass().getSimpleName(), cmdResponse instanceof JaxbTaskSummaryListResponse );
        List<TaskSummary> taskSummaries = ((JaxbTaskSummaryListResponse) cmdResponse).getResult();
        assertTrue( "task summary list is empty", taskSummaries.size() > 0);
        for( TaskSummary taskSum : taskSummaries ) { 
            if( taskSum.getId() == taskId ) { 
                assertTrue( "Task " + taskId + " should have completed.", taskSum.getStatus().equals(Status.Completed));
            }
        }
        
        cmdResponse = response.getResponses().get(1);
        assertTrue( "response is not the proper class type : " + cmdResponse.getClass().getSimpleName(), cmdResponse instanceof JaxbTaskSummaryListResponse );
        taskSummaries = ((JaxbTaskSummaryListResponse) cmdResponse).getResult();
        assertTrue( "task summary list should be empty, but has " + taskSummaries.size() + " elements", taskSummaries.size() == 0);
        
        cmdResponse = response.getResponses().get(2);
        assertNotNull(cmdResponse);
    }
    
    private JaxbCommandsResponse sendJmsJaxbCommandsRequest(String sendQueueName, JaxbCommandsRequest req) throws Exception { 
        InitialContext context = getRemoteInitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);
        Queue jbpmQueue = (Queue) context.lookup(sendQueueName);
        Queue responseQueue = (Queue) context.lookup(RESPONSE_QUEUE_NAME);

        Connection connection = null;
        Session session = null;
        JaxbCommandsResponse cmdResponse = null;
        try {
            // setup
            connection = factory.createConnection("guest", "1234");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer(jbpmQueue);
            MessageConsumer consumer = session.createConsumer(responseQueue);

            connection.start();

            // Create msg
            BytesMessage msg = session.createBytesMessage();
            msg.setIntProperty("serialization", 1);
            String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(req);
            msg.writeUTF(xmlStr);
            
            // send
            producer.send(msg);
            
            // receive
            Message response = consumer.receive(QUALITY_OF_SERVICE_THRESHOLD_MS);
            
            // check
            assertNotNull("Response from MDB was null!", response);
            xmlStr = ((BytesMessage) response).readUTF();
            cmdResponse = (JaxbCommandsResponse) JaxbSerializationProvider.convertStringToJaxbObject(xmlStr);
            assertNotNull("Jaxb Cmd Response was null!", cmdResponse);
        } finally {
            if (connection != null) {
                connection.close();
                session.close();
            }
        }
        return cmdResponse;
    }
    
    @Test
    @InSequence(1)
    @Ignore
    public void testRestUrlStartHumanTaskProcess() throws Exception { 
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
    @InSequence(2)
    public void testRestJaxbStartProcess() throws Exception { 
        // Start process
        String urlString = new URL(deploymentUrl, "/arquillian-test/rest/runtime/test/execute").toExternalForm();
        System.out.println( ">> " + urlString );
        
        ClientRequest restRequest = new ClientRequest(urlString);
        JaxbCommandsRequest commandMessage = new JaxbCommandsRequest("test", new StartProcessCommand("org.jbpm.humantask"));
        String body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        ClientResponse responseObj = restRequest.post();

        assertEquals(200, responseObj.getStatus());
        JaxbCommandsResponse cmdsResp = (JaxbCommandsResponse) responseObj.getEntity(JaxbCommandsResponse.class);
        long procInstId = ((ProcessInstance) cmdsResp.getResponses().get(0)).getId();
//        System.out.println(result);

        // query tasks
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest("test", new GetTasksByProcessInstanceIdCommand(procInstId));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        JaxbCommandsResponse cmdResponse = (JaxbCommandsResponse) responseObj.getEntity(JaxbCommandsResponse.class);
        long taskId = ((JaxbLongListResponse) cmdResponse.getResponses().get(0)).getResult().get(0);
        
        // start task
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(new StartTaskCommand(taskId, "salaboy"));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
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
        commandMessage = new JaxbCommandsRequest(new CompleteTaskCommand(taskId, "salaboy", null));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
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
    
    @Test
    @InSequence(3)
    @Ignore
    public void testRestRemoteApiHumanTaskProcess() throws Exception {
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
