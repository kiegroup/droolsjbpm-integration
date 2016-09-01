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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.remote.services.MockSetupTestHelper.FOR_INDEPENDENT_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.FOR_PROCESS_TASKS;
import static org.kie.remote.services.MockSetupTestHelper.TASK_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupTaskMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.drools.core.impl.EnvironmentFactory;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.GetContentByIdCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.TaskContentRegistry;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.task.model.Content;
import org.kie.internal.command.Context;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.internal.task.api.model.ContentData;
import org.kie.remote.services.TaskDeploymentIdTest;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.util.ExecuteAndSerializeCommand;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SuppressWarnings("unchecked")
public class RestTaskAndAuditDeploymentIdTest extends TaskResourceImpl implements TaskDeploymentIdTest {
    
    private static final String USER = "user";

    private ProcessService processServiceMock;
    private UserTaskService userTaskServiceMock;
    
    private UriInfo uriInfoMock;
    private HttpServletRequest httpRequestMock;
    
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

    @Test
    public void testGetTaskContentExecution() {
        setupTaskMocks(this, FOR_PROCESS_TASKS);
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("test", "test value");
        
        final ContentData contentData = ContentMarshallerHelper.marshal(data, null);
        
        ContentMarshallerContext contentMarshallerContext = Mockito.mock(ContentMarshallerContext.class);
        when(contentMarshallerContext.getClassloader()).thenReturn(this.getClass().getClassLoader());
        when(contentMarshallerContext.getEnvironment()).thenReturn(EnvironmentFactory.newEnvironment());
        TaskContentRegistry.get().addMarshallerContext("deploymentId", contentMarshallerContext);

        doAnswer(new Answer<JaxbContent>() {
            public JaxbContent answer(InvocationOnMock invocation) {
               @SuppressWarnings("rawtypes")
            ExecuteAndSerializeCommand command = (ExecuteAndSerializeCommand) invocation.getArguments()[1];
               
               return (JaxbContent) command.execute(null);
            }
        }).when(userTaskServiceMock).execute(any(String.class), any(ExecuteAndSerializeCommand.class));

        TaskCommand<Content> cmd = Mockito.mock(GetContentByIdCommand.class);
        Content content = Mockito.mock(Content.class);
        when(content.getId()).thenReturn(1l);
        when(content.getContent()).thenReturn(contentData.getContent());
        
        when(cmd.execute(any(Context.class))).thenReturn(content);
        JaxbContent result = (JaxbContent) processRequestBean.doRestTaskOperation(-1l, "deploymentId", -1l, null, cmd);

        assertNotNull(result);
        assertEquals(1l, result.getId().longValue());
        assertNotNull(result.getContent());
        assertNotNull(result.getContentMap());
        assertEquals("test value", result.getContentMap().get("test"));
        verify(userTaskServiceMock, times(1)).execute(any(String.class), any(ExecuteAndSerializeCommand.class));
        verify(contentMarshallerContext, times(1)).getClassloader();
        verify(contentMarshallerContext, times(1)).getEnvironment();
    }
  
}
