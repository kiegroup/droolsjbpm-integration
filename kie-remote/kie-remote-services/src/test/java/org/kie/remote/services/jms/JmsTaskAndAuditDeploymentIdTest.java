package org.kie.remote.services.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.kie.remote.services.MockSetupTestHelper.FOR_INDEPENDENT_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.FOR_PROCESS_TASKS;
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

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
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

    @Override
    public void setProcessServiceMock(ProcessService processServiceMock) {
        this.processServiceMock = processServiceMock;
    }

    @Override
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock) {
        this.userTaskServiceMock = userTaskServiceMock;
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

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
       
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }

    @Test
    public void testJmsProcessTaskProcessing() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }

    @Test
    public void testJmsAuditCommandWithoutDeploymentId() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);

        // run cmd (no deploymentId set on JaxbCommandsRequest object
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new FindProcessInstancesCommand());
        JaxbCommandsResponse 
        response = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
       
        // check result
        assertEquals( "Number of response objects", 1, response.getResponses().size() );
        JaxbCommandResponse<?> 
        responseObj = response.getResponses().get(0);
        assertFalse( "Command did not complete successfully", responseObj instanceof JaxbExceptionResponse );
        
        // run cmd (no deploymentId set on JaxbCommandsRequest object
        cmdsRequest = new JaxbCommandsRequest(new ClearHistoryLogsCommand());
        cmdsRequest.setVersion(ServicesVersion.VERSION);
        response = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
        
        // check result
        assertEquals( "Number of response objects", 0, response.getResponses().size() );
        
        // verify
        verify(auditLogService, times(1)).findProcessInstances();
        verify(auditLogService, times(1)).clear();
    }

}
