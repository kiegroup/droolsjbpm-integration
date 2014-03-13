package org.kie.services.remote.rest;

import static org.kie.services.remote.TaskResourceAndDeploymentIdTestHelper.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.services.task.commands.ClaimTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Test;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.remote.TaskResourceAndDeploymentIdTest;
import org.kie.services.remote.cdi.DeploymentInfoBean;
import org.kie.services.remote.cdi.ProcessRequestBean;

@SuppressWarnings("unchecked")
public class RestTaskResourceAndDeploymentIdTest extends TaskResource implements TaskResourceAndDeploymentIdTest {

    private final static String USER = "user";

    private DeploymentInfoBean runtimeMgrMgrMock;
    private InternalTaskService injectedTaskService;
    private InternalTaskService runtimeTaskService;

    
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
        // REST
        this.uriInfo = mock(UriInfo.class);
        doReturn(new MultivaluedMapImpl<String,String>()).when(uriInfo).getQueryParameters();
        this.identityProvider = mock(IdentityProvider.class);
        doReturn(USER).when(identityProvider).getName();

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setRuntimeMgrMgr(runtimeMgrMgrMock);
        this.processRequestBean.setInjectedTaskService(injectedTaskService);
       
        HttpHeaders headersMock = mock(HttpHeaders.class);
        this.headers = headersMock; 
        doReturn(new MultivaluedMapImpl<String, String>()).when(headersMock).getRequestHeaders();
    }

    @Test
    public void testRestUrlIndependentTaskProcessing() throws URISyntaxException {
        setupMocks(this, FOR_INDEPENDENT_TASKS);
        
        String oper = "claim";
        doReturn(new URI("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(uriInfo).getRequestUri();
        this.taskId_oper(TASK_ID, oper);
        
        oper = "complete";
        doReturn(new URI("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(uriInfo).getRequestUri();
        this.taskId_oper(TASK_ID, oper);
        
        verify(injectedTaskService, times(2)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
    }

    /**
     * When doing operations with a non-process (independent) task, 
     * the injected (non-runtime engine) taskService should be used. 
     */
    @Test
    public void testRestExecuteCommandIndependentTaskProcessing() {
        setupMocks(this, FOR_INDEPENDENT_TASKS);
        
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.execute(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.execute(cmdsRequest);
       
        // verify
        verify(injectedTaskService, times(2)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
    }

    @Test
    public void testRestUrlProcessTaskProcessing() throws Exception {
        setupMocks(this, FOR_PROCESS_TASKS);
        
        String oper = "claim";
        doReturn(new URI("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(uriInfo).getRequestUri();
        this.taskId_oper(TASK_ID, oper);
        
        oper = "complete";
        doReturn(new URI("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(uriInfo).getRequestUri();
        this.taskId_oper(TASK_ID, oper);
        
        // verify
        verify(injectedTaskService, times(1)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
        // complete operation should be done by runtime task service
        verify(runtimeTaskService, times(1)).execute(any(TaskCommand.class));
    }

    @Test
    public void testRestExecuteCommandProcessTaskProcessing() {
        setupMocks(this, FOR_PROCESS_TASKS);

        JaxbCommandsRequest cmdsRequest = new JaxbCommandsRequest(new ClaimTaskCommand(TASK_ID, USER));
        this.execute(cmdsRequest);
        cmdsRequest = new JaxbCommandsRequest(new CompleteTaskCommand(TASK_ID, USER, null));
        this.execute(cmdsRequest);
    
        // verify
        verify(injectedTaskService, times(1)).execute(any(TaskCommand.class));
        verify(injectedTaskService, times(1)).getTaskById(eq(TASK_ID));
        // complete operation should be done by runtime task service
        verify(runtimeTaskService, times(1)).execute(any(TaskCommand.class));
    }
}
