package org.kie.remote.services.rest;

import static org.kie.remote.services.MockSetupTestHelper.FOR_INDEPENDENT_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.FOR_PROCESS_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.TASK_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupTaskMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Test;
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.TaskDeploymentIdTest;
import org.kie.remote.services.cdi.ProcessRequestBean;

@SuppressWarnings("unchecked")
public class RestTaskAndAuditDeploymentIdTest extends TaskResourceImpl implements TaskDeploymentIdTest {
    
    private static final String USER = "user";

    private ProcessService processServiceMock;
    private UserTaskService userTaskServiceMock;
    
    private UriInfo uriInfoMock;
    private HttpServletRequest httpRequestMock;

    @Override
    public void setProcessServiceMock(ProcessService processServiceMock) {
        this.processServiceMock = processServiceMock;
    }

    @Override
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock) {
        this.userTaskServiceMock = userTaskServiceMock;
    }

    public void setupTestMocks() {
        // REST
        uriInfoMock = mock(UriInfo.class);
        setUriInfo(uriInfoMock);
        doReturn(new MultivaluedMapImpl<String,String>()).when(uriInfoMock).getQueryParameters();
        httpRequestMock = mock(HttpServletRequest.class);
        setHttpServletRequest(httpRequestMock);
        
        this.identityProvider = mock(IdentityProvider.class);
        doReturn(USER).when(identityProvider).getName();

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setProcessService(processServiceMock);
        this.processRequestBean.setUserTaskService(userTaskServiceMock);
       
        HttpHeaders headersMock = mock(HttpHeaders.class);
        this.headers = headersMock; 
        
        doReturn(new MultivaluedMapImpl<String, String>()).when(headersMock).getRequestHeaders();
    }

    @Test
    public void testRestUrlIndependentTaskProcessing() throws URISyntaxException {
        setupTaskMocks(this, FOR_INDEPENDENT_TASKS);
        
        String oper = "claim";
        doReturn(new String("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(httpRequestMock).getRequestURI();
        this.doTaskOperation(TASK_ID, oper);
        
        oper = "complete";
        doReturn(new String("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(httpRequestMock).getRequestURI();
        this.doTaskOperation(TASK_ID, oper);
        
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }
 
    @Test
    public void testRestUrlProcessTaskProcessing() throws Exception {
        setupTaskMocks(this, FOR_PROCESS_TASKS);
        
        String oper = "claim";
        doReturn(new String("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(httpRequestMock).getRequestURI();
        this.doTaskOperation(TASK_ID, oper);
        
        oper = "complete";
        doReturn(new String("http://localhost:8080/test/rest/task/" + TASK_ID + "/" + oper)).when(httpRequestMock).getRequestURI();
        this.doTaskOperation(TASK_ID, oper);
        
        // verify
        verify(userTaskServiceMock, times(2)).execute(any(String.class), any(TaskCommand.class));
    }
  
}
