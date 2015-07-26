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

package org.kie.remote.services.rest;

import static org.junit.Assert.assertEquals;
import static org.kie.remote.services.MockSetupTestHelper.DEPLOYMENT_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupProcessMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.Context;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.remote.services.StartProcessEveryStrategyTest;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EmptyContext.class, ProcessInstanceIdContext.class })
@SuppressWarnings("unchecked")
public class RestStartProcessEveryStrategyTest extends RuntimeResourceImpl implements StartProcessEveryStrategyTest {

    private ProcessService processServiceMock;
    private UserTaskService userTaskServiceMock;
    
    private HttpServletRequest httpRequestMock;

    private Map<String, String[]> queryParams;

    @Override
    public void setProcessServiceMock(ProcessService processServiceMock) {
        this.processServiceMock = processServiceMock;
    }

    @Override
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock) {
        this.userTaskServiceMock = userTaskServiceMock;
    }

    @Override
    public void setupTestMocks() {
        httpRequestMock = mock(HttpServletRequest.class);
        setHttpServletRequest(httpRequestMock);
        doReturn(queryParams).when(httpRequestMock).getParameterMap();

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setProcessService(processServiceMock);
        this.processRequestBean.setUserTaskService(userTaskServiceMock);

        HttpHeaders headersMock = mock(HttpHeaders.class);
        this.headers = headersMock;
        doReturn(new MultivaluedMapImpl<String, String>()).when(headersMock).getRequestHeaders();
    }


    @Before
    public void before() {
        this.queryParams = new HashMap<String, String[]>();
        this.deploymentId = DEPLOYMENT_ID;
    }

    @Test
    public void startProcessAndDoStuffPerProcessStrategyTest() throws Exception {
        // This method does some static mock magic to make sure
        // that EmptyContext.get() throws an exception if it is called here
        // (since a ProcessInstanceIdContext should be used instead

        setupProcessMocks(this, RuntimeStrategy.PER_PROCESS_INSTANCE);
        doReturn(new String("http://localhost:8080/test/rest/process/" + TEST_PROCESS_DEF_NAME + "/start")).when(httpRequestMock)
                .getRequestURI();
        String [] corrProps = { "anton" };
        this.queryParams.put("corrProp", corrProps);

        Response resp = startProcessInstance(TEST_PROCESS_DEF_NAME);
        // verify ksession is called
        JaxbProcessInstanceResponse procInstResp = (JaxbProcessInstanceResponse) resp.getEntity();
        assertEquals("Invalid process instance id", TEST_PROCESS_INST_ID, procInstResp.getId());

        // Do rest call with process instance id this time. This will fail if:
        // - the ProcessInstanceIdContext is not used (and an EmptyContext is used instead)
        // - The ProcessInstanceIdContext constructor gets a null value for the process instance id
        String [] procInstParamVal = {String.valueOf(TEST_PROCESS_INST_ID)};
        queryParams.put(PROC_INST_ID_PARAM_NAME, procInstParamVal);
        String [] signalParamVal = { "test" };
        queryParams.put("signal", signalParamVal);
        resp = signalProcessInstances();

        // verify ksession is called
        verify(processServiceMock, times(2)).execute(any(String.class), any(Context.class), any(Command.class));
        PowerMockito.verifyStatic(times(1));
        EmptyContext.get();
    }

    @Test
    public void startProcessAndDoStuffPerRequestStrategyTest() throws Exception {
        setupProcessMocks(this, RuntimeStrategy.PER_REQUEST);
        doReturn(new String("http://localhost:8080/test/rest/process/" + TEST_PROCESS_DEF_NAME + "/start")).when(httpRequestMock)
                .getRequestURI();

        Response resp = startProcessInstance(TEST_PROCESS_DEF_NAME);
        // verify non-process contexts are used
        JaxbProcessInstanceResponse procInstResp = (JaxbProcessInstanceResponse) resp.getEntity();
        assertEquals("Invalid process instance id", TEST_PROCESS_INST_ID, procInstResp.getId());

        String [] signalParamVal = {"test"};
        queryParams.put("signal", signalParamVal);
        resp = signalProcessInstances();

        // verify ksession is called
        verify(processServiceMock, times(2)).execute(any(String.class), any(Context.class), any(Command.class));
    }

    @Test
    public void startProcessAndDoStuffSingletonStrategyTest() throws Exception {
        setupProcessMocks(this, RuntimeStrategy.SINGLETON);
        doReturn(new String("http://localhost:8080/test/rest/process/" + TEST_PROCESS_DEF_NAME + "/start")).when(httpRequestMock)
                .getRequestURI();

        Response resp = startProcessInstance(TEST_PROCESS_DEF_NAME);
        // verify non-process contexts are used
        JaxbProcessInstanceResponse procInstResp = (JaxbProcessInstanceResponse) resp.getEntity();
        assertEquals("Invalid process instance id", TEST_PROCESS_INST_ID, procInstResp.getId());

        String [] signalParamVal = { "test" };
        queryParams.put("signal", signalParamVal);
        resp = signalProcessInstances();

        // verify ksession is called
        verify(processServiceMock, times(2)).execute(any(String.class), any(Context.class), any(Command.class));
    }

}
