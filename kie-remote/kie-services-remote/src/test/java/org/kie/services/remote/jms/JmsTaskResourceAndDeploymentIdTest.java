package org.kie.services.remote.jms;

import static org.junit.Assert.*;
import static org.kie.services.remote.TaskResourceAndDeploymentIdTestHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.command.ClearHistoryLogsCommand;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindProcessInstancesCommand;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Test;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.remote.TaskResourceAndDeploymentIdTest;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.mockito.Mock;

@SuppressWarnings("unchecked")
public class JmsTaskResourceAndDeploymentIdTest extends RequestMessageBean implements TaskResourceAndDeploymentIdTest {

    private DeploymentInfoBean runtimeMgrMgrMock;
    private InternalTaskService injectedTaskService;
    private InternalTaskService runtimeTaskService;
    
    private AuditLogService auditLogService = mock(AuditLogService.class);

    @Override
    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock) {
        this.runtimeMgrMgrMock = mock;
    }

    @Override
    public void setInjectedTaskServiceMock(InternalTaskService mock) {
        this.injectedTaskService = mock;
    }

    @Override
    public void setRuntimeTaskServiceMock(InternalTaskService mock) {
        this.runtimeTaskService = mock;
    }

    public void setupTestMocks() {
        this.runtimeMgrMgr = runtimeMgrMgrMock;

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setRuntimeMgrMgr(runtimeMgrMgrMock);
        this.processRequestBean.setInjectedTaskService(injectedTaskService);
        this.processRequestBean.setInjectedTaskService(injectedTaskService);
        
        // audit log service
        doReturn(new ArrayList<ProcessInstanceLog>()).when(auditLogService).findProcessInstances();
        doNothing().when(auditLogService).clear();
        this.processRequestBean.setAuditLogService(auditLogService);
    }

    @Test
    public void testJmsIndependentTaskProcessing() {
        setupMocks(this, FOR_INDEPENDENT_TASKS);

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.processJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.processJaxbCommandsRequest(cmdsRequest);
       
        // verify
        verify(injectedTaskService, times(2)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
    }

    @Test
    public void testJmsProcessTaskProcessing() {
        setupMocks(this, FOR_PROCESS_TASKS);

        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.processJaxbCommandsRequest(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.processJaxbCommandsRequest(cmdsRequest);
        
        // verify
        verify(injectedTaskService, times(1)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
        // complete operation should be done by runtime task service
        verify(runtimeTaskService, times(1)).execute(any(TaskCommand.class));
    }

    @Test
    public void testJmsAuditCommandWithoutDeploymentId() {
        setupMocks(this, FOR_PROCESS_TASKS);

        // run cmd (no deploymentId set on JaxbConmandsRequest object
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new FindProcessInstancesCommand());
        JaxbCommandsResponse 
        response = this.processJaxbCommandsRequest(cmdsRequest);
       
        // check result
        assertEquals( "Number of response objects", 1, response.getResponses().size() );
        JaxbCommandResponse<?> 
        responseObj = response.getResponses().get(0);
        assertFalse( "Command did not complete successfully", responseObj instanceof JaxbExceptionResponse );
        
        // run cmd (no deploymentId set on JaxbConmandsRequest object
        cmdsRequest = new JaxbCommandsRequest(new ClearHistoryLogsCommand());
        response = this.processJaxbCommandsRequest(cmdsRequest);
        
        // check result
        assertEquals( "Number of response objects", 0, response.getResponses().size() );
        
        // verify
        verify(auditLogService, times(1)).findProcessInstances();
        verify(auditLogService, times(1)).clear();
    }

}
