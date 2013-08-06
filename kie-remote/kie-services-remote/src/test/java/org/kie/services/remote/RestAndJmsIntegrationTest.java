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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.ws.rs.core.MediaType;

import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.shrinkwrap.api.Archive;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.xml.AbstractJaxbHistoryObject;
import org.jbpm.process.audit.xml.JaxbHistoryLog;
import org.jbpm.process.audit.xml.JaxbVariableInstanceLog;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.RemoteJmsRuntimeEngineFactory;
import org.kie.services.client.api.RemoteRestSessionFactory;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.JaxbLongListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbTaskSummaryListResponse;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(ArquillianJbossServerSetupTask.class)
public class RestAndJmsIntegrationTest extends IntegrationTestBase {

    private static Logger logger = LoggerFactory.getLogger(RestAndJmsIntegrationTest.class);

    private static final String CONNECTION_FACTORY_NAME = "jms/RemoteConnectionFactory";
    private static final String TASK_QUEUE_NAME = "jms/queue/KIE.TASK";
    private static final String RESPONSE_QUEUE_NAME = "jms/queue/KIE.RESPONSE";

    private static final String DEPLOYMENT_ID = "test";
    private static final String USER_ID = "salaboy";
    
    @Deployment(testable = false)
    public static Archive<?> createWar() {
       return createWebArchive();
    }

    @ArquillianResource
    URL deploymentUrl;

    private static final long QUALITY_OF_SERVICE_THRESHOLD_MS = 5 * 1000;
    
    @AfterClass
    public static void waitForTxOnServer() throws InterruptedException { 
        Thread.sleep(1000);
    }
   
    @Test
    @Ignore("JMS isn't working.. :/")
    public void testJmsStartProcess() throws Exception {
        // send cmd
        Command<?> cmd = new StartProcessCommand("org.jbpm.humantask"); 
        JaxbCommandsRequest req = new JaxbCommandsRequest(DEPLOYMENT_ID, cmd);
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
        req = new JaxbCommandsRequest(DEPLOYMENT_ID, cmd);
        response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        // check response 
        assertNotNull("response was null.", response);
        assertTrue("response did not contain any command responses",  response.getResponses() != null && response.getResponses().size() > 0);
        cmdResponse = response.getResponses().get(0);
        assertTrue( "response is not the proper class type : " + cmdResponse.getClass().getSimpleName(), cmdResponse instanceof JaxbLongListResponse );
        long taskId = ((JaxbLongListResponse) cmdResponse).getResult().get(0);
        
        // send cmd
        cmd = new StartTaskCommand(taskId, USER_ID);
        req = new JaxbCommandsRequest(DEPLOYMENT_ID, cmd);
        req.getCommands().add(new CompleteTaskCommand(taskId, USER_ID, null));
        response = sendJmsJaxbCommandsRequest(TASK_QUEUE_NAME, req);
        
        // check response 
        assertNotNull("response was null.", response);
        assertTrue("response list was not empty", response.getResponses().size() == 0);
        
        // send cmd
        cmd = new GetTasksOwnedCommand(USER_ID, "en-UK");
        req = new JaxbCommandsRequest(DEPLOYMENT_ID, cmd);
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
            connection = factory.createConnection(USER, PASSWORD);
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer(jbpmQueue);
            String corrId = UUID.randomUUID().toString();
            String selector = "JMSCorrelationID = '" + corrId + "'";
            MessageConsumer consumer = session.createConsumer(responseQueue, selector);

            connection.start();

            // Create msg
            BytesMessage msg = session.createBytesMessage();
            msg.setJMSCorrelationID(corrId);
            msg.setIntProperty("serialization", 1);
            String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(req);
            msg.writeUTF(xmlStr);
            
            // send
            producer.send(msg);
            
            // receive
            Message response = consumer.receive(QUALITY_OF_SERVICE_THRESHOLD_MS);
            
            // check
            assertNotNull("Response is empty.", response);
            assertEquals("Correlation id not equal to request msg id.", corrId, response.getJMSCorrelationID() );
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
   
    private long findTaskId(long procInstId, List<TaskSummary> taskSumList) { 
        long taskId = -1;
        for( TaskSummary task : taskSumList ) { 
            if( task.getProcessInstanceId() == procInstId ) {
                taskId = task.getId();
            }
        }
        assertNotEquals("Could not determine taskId!", -1, taskId);
        return taskId;
    }
    
    @Test
    @Ignore("JMS isn't working.. :/")
    public void testJmsRemoteApiHumanTaskProcess() throws Exception {
        // create JMS request
        RuntimeEngine engine = new RemoteJmsRuntimeEngineFactory(DEPLOYMENT_ID, getRemoteInitialContext()).newRuntimeEngine();
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess("org.jbpm.humantask");
        
        logger.debug("Started process instance: " + processInstance + " " + (processInstance == null? "" : processInstance.getId()));
        
        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(USER_ID, "en-UK");
        long taskId = findTaskId(processInstance.getId(), tasks);
        
        logger.debug("Found task " + taskId);
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId + ": " + task );
        taskService.start(taskId, USER_ID);
        taskService.complete(taskId, USER_ID, null);
        
        logger.debug("Now expecting failure");
        try {
            taskService.complete(taskId, USER_ID, null);
            fail( "Should not have been able to complete task " + taskId + " a second time.");
        } catch (Throwable t) {
            // do nothing
        }
        
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(processInstance.getId(), statuses, "en-UK");
        assertEquals("Expected 2 tasks.", 2, taskIds.size());
    }

    @Test
    public void testRestUrlStartHumanTaskProcess() throws Exception { 
        // create REST request
        String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/runtime/test/process/org.jbpm.humantask/start").toExternalForm();
        ClientRequest restRequest = new ClientRequest(urlString);

        // Get and check response
        logger.debug( ">> [org.jbpm.humantask/start]" + urlString );
        ClientResponse responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        JaxbProcessInstanceResponse processInstance = (JaxbProcessInstanceResponse) responseObj.getEntity(JaxbProcessInstanceResponse.class);
        long procInstId = processInstance.getId();

        urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/task/query?taskOwner=" + USER_ID).toExternalForm();
        restRequest = new ClientRequest(urlString);
        logger.debug( ">> [task/query]" + urlString );
        responseObj = restRequest.get();
        assertEquals(200, responseObj.getStatus());
        
        JaxbTaskSummaryListResponse taskSumlistResponse = (JaxbTaskSummaryListResponse) responseObj.getEntity(JaxbTaskSummaryListResponse.class);
        long taskId = findTaskId(procInstId, taskSumlistResponse.getResult());
        
        urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/task/" + taskId + "/start?userId=" + USER_ID).toExternalForm();
        restRequest = new ClientRequest(urlString);

        // Get response
        logger.debug( ">> [task/?/start] " + urlString );
        responseObj = restRequest.post();

        // Check response
        checkResponse(responseObj);
    }
    
    @Test
    public void testRestExecuteStartProcess() throws Exception { 
        // Start process
        String urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/runtime/test/execute").toExternalForm();
        
        ClientRequest restRequest = new ClientRequest(urlString);
        JaxbCommandsRequest commandMessage = new JaxbCommandsRequest(DEPLOYMENT_ID, new StartProcessCommand("org.jbpm.humantask"));
        String body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        logger.debug( ">> [startProcess] " + urlString );
        ClientResponse responseObj = restRequest.post();

        assertEquals(200, responseObj.getStatus());
        JaxbCommandsResponse cmdsResp = (JaxbCommandsResponse) responseObj.getEntity(JaxbCommandsResponse.class);
        long procInstId = ((ProcessInstance) cmdsResp.getResponses().get(0)).getId();

        // query tasks
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(DEPLOYMENT_ID, new GetTasksByProcessInstanceIdCommand(procInstId));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        logger.debug( ">> [getTasksByProcessInstanceId] " + urlString );
        responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        JaxbCommandsResponse cmdResponse = (JaxbCommandsResponse) responseObj.getEntity(JaxbCommandsResponse.class);
        List list = (List) cmdResponse.getResponses().get(0).getResult();
        long taskId = (Long) list.get(0);
        
        // start task
        
        logger.debug( ">> [startTask] " + urlString );
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(new StartTaskCommand(taskId, USER_ID));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, commandMessage);

        // Get response
        responseObj = restRequest.post();

        // Check response
        checkResponse(responseObj);

        urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/task/execute").toExternalForm();
        
        restRequest = new ClientRequest(urlString);
        commandMessage = new JaxbCommandsRequest(new CompleteTaskCommand(taskId, USER_ID, null));
        body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, commandMessage);

        // Get response
        logger.debug( ">> [completeTask] " + urlString );
        responseObj = restRequest.post();
        checkResponse(responseObj);
    }
    
    @Test
    public void testRestRemoteApiHumanTaskProcess() throws Exception {
        // create REST request
        RemoteRestSessionFactory restSessionFactory = new RemoteRestSessionFactory(DEPLOYMENT_ID, deploymentUrl.toExternalForm());
        RuntimeEngine engine = restSessionFactory.newRuntimeEngine();
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess("org.jbpm.humantask");
        
        logger.debug("Started process instance: " + processInstance + " " + (processInstance == null? "" : processInstance.getId()));
        
        TaskService taskService = engine.getTaskService();
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(USER_ID, "en-UK");
        long taskId = findTaskId(processInstance.getId(), tasks);
        
        logger.debug("Found task " + taskId);
        Task task = taskService.getTaskById(taskId);
        logger.debug("Got task " + taskId + ": " + task );
        taskService.start(taskId, USER_ID);
        taskService.complete(taskId, USER_ID, null);
        
        logger.debug("Now expecting failure");
        try {
        	taskService.complete(taskId, USER_ID, null);
        	fail( "Should not be able to complete task " + taskId + " a second time.");
        } catch (Throwable t) {
            // do nothing
        }
        
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.Reserved);
        List<TaskSummary> taskIds = taskService.getTasksByStatusByProcessInstanceId(processInstance.getId(), statuses, "en-UK");
        assertEquals("Expected 2 tasks.", 2, taskIds.size());
    }
    
    @Test
    public void testRestExecuteTaskCommands() throws Exception {
        RuntimeEngine engine = new RemoteRestSessionFactory(DEPLOYMENT_ID, deploymentUrl.toExternalForm()).newRuntimeEngine();
        KieSession ksession = engine.getKieSession();
        ProcessInstance processInstance = ksession.startProcess("org.jbpm.humantask");
        
        long processInstanceId = processInstance.getId();
        JaxbCommandResponse<?> response = executeTaskCommand(DEPLOYMENT_ID, new GetTasksByProcessInstanceIdCommand(processInstanceId));
        
        long taskId = ((JaxbLongListResponse) response).getResult().get(0);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", USER_ID);
    }
    
    private JaxbCommandResponse<?> executeTaskCommand(String deploymentId, Command<?> command) throws Exception {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        commands.add(command);
        
        String urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/runtime/" + DEPLOYMENT_ID + "/execute").toExternalForm();
        logger.info("Client request to: " + urlString);
        ClientRequest restRequest = new ClientRequest(urlString);
        
        JaxbCommandsRequest commandMessage = new JaxbCommandsRequest(commands);
        assertNotNull( "Commands are null!", commandMessage.getCommands() );
        assertTrue( "Commands are empty!", commandMessage.getCommands().size() > 0 );
        
        String body = JaxbSerializationProvider.convertJaxbObjectToString(commandMessage);
        restRequest.body(MediaType.APPLICATION_XML, body);

        ClientResponse<JaxbCommandsResponse> responseObj = restRequest.post(JaxbCommandsResponse.class);
        checkResponse(responseObj);
        
        JaxbCommandsResponse cmdsResp = responseObj.getEntity();
        return cmdsResp.getResponses().get(0);
    }
    
    private void checkResponse(ClientResponse<?> responseObj) throws Exception {
        ClientResponse<?> test = BaseClientResponse.copyFromError(responseObj);
        responseObj.resetStream();
        if (test.getResponseStatus() == javax.ws.rs.core.Response.Status.BAD_REQUEST) {
            throw new BadRequestException(test.getEntity(String.class));
        } else if (test.getResponseStatus() != javax.ws.rs.core.Response.Status.OK) {
            throw new Exception("Request operation failed. Response status = " + test.getResponseStatus() + "\n\n" + test.getEntity(String.class));
        } else {
            logger.debug( "Response: " + test.getEntity(String.class));
        }
    }
    
    @Test
    public void testRestHistoryLogs() throws Exception {
        String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/runtime/test/process/var-proc/start?map_x=initVal").toExternalForm();
        ClientRequest restRequest = new ClientRequest(urlString);

        // Get and check response
        logger.debug( ">> " + urlString );
        ClientResponse responseObj = restRequest.post();
        assertEquals(200, responseObj.getStatus());
        JaxbProcessInstanceResponse processInstance = (JaxbProcessInstanceResponse) responseObj.getEntity(JaxbProcessInstanceResponse.class);
        long procInstId = processInstance.getId();

        urlString = new URL(deploymentUrl, deploymentUrl.getPath() + "rest/runtime/test/history/instance/" + procInstId + "/variable/x").toExternalForm();
        restRequest = new ClientRequest(urlString);
        logger.debug( ">> [history/variables]" + urlString );
        responseObj = restRequest.get();
        assertEquals(200, responseObj.getStatus());
        JaxbHistoryLogList logList = (JaxbHistoryLogList) responseObj.getEntity(JaxbHistoryLogList.class);
        List<AbstractJaxbHistoryObject> varLogList = logList.getHistoryLogList();
        assertEquals("Incorrect number of variable logs", 4, varLogList.size());
        
        for( AbstractJaxbHistoryObject<?> log : logList.getHistoryLogList() ) {
           JaxbVariableInstanceLog varLog = (JaxbVariableInstanceLog) log;
           assertEquals( "Incorrect variable id", "x", varLog.getVariableId() );
           assertEquals( "Incorrect process id", "var-proc", varLog.getProcessId() );
           assertEquals( "Incorrect process instance id", "var-proc", varLog.getProcessId() );
        }
    }
    
}
