/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.remote.services.MockSetupTestHelper.FOR_INDEPENDENT_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.FOR_PROCESS_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.PASSWORD;
import static org.kie.remote.services.MockSetupTestHelper.TASK_ID;
import static org.kie.remote.services.MockSetupTestHelper.USER;
import static org.kie.remote.services.MockSetupTestHelper.setupTaskMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.GetTasksOwnedCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Before;
import org.junit.Test;
import org.kie.remote.services.TaskDeploymentIdTest;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.jms.request.BackupIdentityProviderProducer;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.shared.ServicesVersion;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
public class JmsTaskAndAuditDeploymentIdTest extends RequestMessageBean implements TaskDeploymentIdTest {

    private ProcessService processServiceMock;
    private UserTaskService userTaskServiceMock;
    
    private AuditLogService auditLogService = mock(AuditLogService.class);
    
    private boolean getTasksTest = false;

    @Override
    public void setProcessServiceMock(ProcessService processServiceMock) {
        this.processServiceMock = processServiceMock;
    }

    @Override
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock) {
        this.userTaskServiceMock = userTaskServiceMock;
    }

    @Override
    public boolean getTasksTest() {
        return this.getTasksTest;
    }
   
    @Before
    public void before() { 
        this.getTasksTest = false;
    }
    
    @Override
    protected Subject tryLogin(String[] userPass) throws LoginException {
       return new Subject(); 
    }
    
    public void setupTestMocks() {
        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setProcessService(processServiceMock);
        this.processRequestBean.setUserTaskService(userTaskServiceMock);
        
        // audit log service
        doReturn(new ArrayList<ProcessInstanceLog>()).when(auditLogService).findProcessInstances();
        doNothing().when(auditLogService).clear();
        this.processRequestBean.setAuditLogService(auditLogService);

        this.backupIdentityProviderProducer = Mockito.mock(BackupIdentityProviderProducer.class);
    }

    @Test
    public void testJmsIndependentTaskProcessing() {
        setupTaskMocks(this, FOR_INDEPENDENT_TASKS);
        String [] userPass = { USER, PASSWORD };

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        cmdsRequest.setUserPass(userPass);
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        cmdsRequest.setUserPass(userPass);
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
       
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }
    
    /**
     * When a GetTask* command is processed, the user id should be checked 
     * against the authenticated user. An exception should be thrown if
     * a different user is used. 
     */
    @Test
    public void testJmsProcessGetTaskCommandChecksAgainstAuthUser() {
        this.getTasksTest = true;
        setupTaskMocks(this, FOR_INDEPENDENT_TASKS);
       
        // control case
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(USER));
        String [] userPass = { USER, PASSWORD };
        cmdsRequest.setUserPass(userPass);
        JaxbCommandsResponse 
        cmdsResponse = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        List<JaxbCommandResponse<?>> respList = cmdsResponse.getResponses();
        assertFalse( "Empty response list", respList == null || respList.isEmpty() );
        JaxbCommandResponse resp = respList.get(0);
        assertNotNull( "Null response", resp );
        assertFalse( "Incorrect response", resp instanceof JaxbExceptionResponse);
      
        // different user 
        String otherUser = "differentUser";
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(otherUser));
        cmdsRequest.setUserPass(userPass);
        cmdsResponse = this.jmsProcessJaxbCommandsRequest(cmdsRequest);

        respList = cmdsResponse.getResponses();
        assertFalse( "Empty response list", respList == null || respList.isEmpty() );
        resp = respList.get(0);
        assertNotNull( "Null response", resp );
        assertTrue( "Expected an exception response", resp instanceof JaxbExceptionResponse);
       
        String msg = ((JaxbExceptionResponse) resp).getMessage();
        assertTrue( "Exception should reference incorrect user", msg.contains(otherUser) );
        assertTrue( "Exception should reference correct/auth user", msg.contains(USER) );
        assertTrue( "Exception should explain fault", msg.contains("must match the authenticating user"));

        // null user 
        otherUser = null;
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(otherUser));
        cmdsRequest.setUserPass(userPass);
        cmdsResponse = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        respList = cmdsResponse.getResponses();
        assertFalse( "Empty response list", respList == null || respList.isEmpty() );
        resp = respList.get(0);
        assertNotNull( "Null response", resp );
        assertTrue( "Expected an exception response", resp instanceof JaxbExceptionResponse);
       
        msg = ((JaxbExceptionResponse) resp).getMessage();
        assertTrue( "Exception should reference correct/auth user", msg.contains(USER) );
        assertTrue( "Exception should explain fault", msg.contains("null user id"));

        // null user 
    }

    @Test
    public void testJmsProcessTaskProcessing() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);
        String [] userPass = { USER, PASSWORD };

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        cmdsRequest.setUserPass(userPass);
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        cmdsRequest.setUserPass(userPass);
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }

    @Test
    public void testJmsAuditCommandWithoutDeploymentId() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);
        String [] userPass = { USER, PASSWORD };

        // run cmd (no deploymentId set on JaxbCommandsRequest object
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new FindProcessInstancesCommand());
        cmdsRequest.setUserPass(userPass);
        JaxbCommandsResponse 
        response = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
       
        // check result
        assertEquals( "Number of response objects", 1, response.getResponses().size() );
        JaxbCommandResponse<?> 
        responseObj = response.getResponses().get(0);
        assertFalse( "Command did not complete successfully", responseObj instanceof JaxbExceptionResponse );
        
        // run cmd (no deploymentId set on JaxbCommandsRequest object
        cmdsRequest = new JaxbCommandsRequest(new ClearHistoryLogsCommand());
        cmdsRequest.setUserPass(userPass);
        cmdsRequest.setVersion(ServicesVersion.VERSION);
        response = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        // check result
        assertEquals( "Number of response objects", 0, response.getResponses().size() );
        
        // verify
        verify(auditLogService, times(1)).findProcessInstances();
        verify(auditLogService, times(1)).clear();
    }

}
