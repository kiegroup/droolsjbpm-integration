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

package org.kie.remote.services.rest;

import static org.junit.Assert.*;
import static org.kie.remote.services.MockSetupTestHelper.FOR_INDEPENDENT_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.FOR_PROCESS_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.TASK_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupTaskMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.process.audit.AuditLogService;
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
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.TaskDeploymentIdTest;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;

@SuppressWarnings("unchecked")
public class ExecuteResourceTest extends ExecuteResourceImpl implements TaskDeploymentIdTest {

    private static final String USER = "user";

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
    
    public void setupTestMocks() {
        // REST
        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setProcessService(processServiceMock);
        this.processRequestBean.setUserTaskService(userTaskServiceMock);
        
        this.identityProvider = mock(IdentityProvider.class);
        doReturn(USER).when(this.identityProvider).getName();
    }

    /**
     * When doing operations with a non-process (independent) task, 
     * the injected (non-runtime engine) taskService should be used. 
     */
    @Test
    public void testRestExecuteCommandIndependentTaskProcessing() {
        setupTaskMocks(this, FOR_INDEPENDENT_TASKS);
        
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.execute(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.execute(cmdsRequest);
       
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }

    /**
     * When a GetTask* command is processed, the user id should be checked 
     * against the authenticated user. An exception should be thrown if
     * a different user is used. 
     */
    @Test
    public void testRestExecuteCommandChecksAgainstAuthUser() {
        this.getTasksTest = true;
        setupTaskMocks(this, FOR_INDEPENDENT_TASKS);
       
        // control case
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(USER));
        this.execute(cmdsRequest);
       
        String otherUser = "differentUser";
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(otherUser));
        try { 
            this.execute(cmdsRequest);
            fail("Processing the GetTask* command should have failed!");
        } catch( KieRemoteRestOperationException krroe ) { 
            assertTrue( "Exception should reference incorrect user", krroe.getMessage().contains(otherUser) );
            assertTrue( "Exception should reference correct/auth user", krroe.getMessage().contains(USER) );
            assertTrue( "Exception should explain fault", krroe.getMessage().contains("must match the authenticating user"));
        }
        
        otherUser = null;
        cmdsRequest = new JaxbCommandsRequest(new GetTasksOwnedCommand(otherUser));
        try { 
            this.execute(cmdsRequest);
            fail("Processing the GetTask* command should have failed!");
        } catch( KieRemoteRestOperationException krroe ) { 
            assertTrue( "Exception should reference correct/auth user", krroe.getMessage().contains(USER) );
            assertTrue( "Exception should explain fault", krroe.getMessage().contains("null user id"));
        }
    }
    
    @Test
    public void testRestExecuteCommandProcessTaskProcessing() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);

        JaxbCommandsRequest cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.execute(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.execute(cmdsRequest);
    
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }
  
    @Test
    public void testRestAuditCommandWithoutDeploymentId() {
        // setup
        setupTaskMocks(this, FOR_PROCESS_TASKS);
        this.processRequestBean.setAuditLogService(auditLogService);

        // run cmd (no deploymentId set on JaxbConmandsRequest object
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new FindProcessInstancesCommand());
        JaxbCommandsResponse 
        response = this.execute(cmdsRequest);
       
        // check result
        assertEquals( "Number of response objects", 1, response.getResponses().size() );
        JaxbCommandResponse<?> 
        responseObj = response.getResponses().get(0);
        assertFalse( "Command did not complete successfully", responseObj instanceof JaxbExceptionResponse );
        
        // run cmd (no deploymentId set on JaxbConmandsRequest object
        cmdsRequest = new JaxbCommandsRequest(new ClearHistoryLogsCommand());
        response = this.execute(cmdsRequest);
        
        // check result
        assertEquals( "Number of response objects", 0, response.getResponses().size() );
        
        // verify
        verify(auditLogService, times(1)).findProcessInstances();
        verify(auditLogService, times(1)).clear();
    }
   
}
