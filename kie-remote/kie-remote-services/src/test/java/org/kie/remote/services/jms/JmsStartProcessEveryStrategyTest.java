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

package org.kie.remote.services.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.remote.services.MockSetupTestHelper.DEPLOYMENT_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupProcessMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.Context;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.remote.services.MockSetupTestHelper;
import org.kie.remote.services.StartProcessEveryStrategyTest;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.jms.request.BackupIdentityProviderProducer;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.shared.ServicesVersion;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * JMS counterpart to RestStartProcessEveryStrategyTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EmptyContext.class, ProcessInstanceIdContext.class})
@SuppressWarnings("unchecked")
public class JmsStartProcessEveryStrategyTest extends RequestMessageBean implements StartProcessEveryStrategyTest {

    private DeploymentInfoBean runtimeMgrMgrMock;

    private ProcessService processServiceMock;
    private UserTaskService userTaskServiceMock;
    
    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock) {
        this.runtimeMgrMgrMock = mock;
    }

    @Override
    public void setProcessServiceMock(ProcessService processServiceMock) {
        this.processServiceMock = processServiceMock;
    }

    @Override
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock) {
        this.userTaskServiceMock = userTaskServiceMock;
    }

    public void setupTestMocks() {
        this.runtimeMgrMgr = runtimeMgrMgrMock;

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setProcessService(processServiceMock);
        this.processRequestBean.setUserTaskService(userTaskServiceMock);

        this.backupIdentityProviderProducer = Mockito.mock(BackupIdentityProviderProducer.class);
    }

    @Test
    public void startProcessAndDoStuffPerProcessStrategyTest() throws Exception {
        // This method does some static mock magic to make sure 
        // - that EmptyContext.get() throws an exception if it is called here (since a ProcessInstanceIdContext should be used instead)
        // - that a process instance id *is* used in the second call in the constructor of the ProcessInstanceIdContext
        setupProcessMocks(this, RuntimeStrategy.PER_PROCESS_INSTANCE);
       
        // test
        runStartProcessAndDoStuffTest();

    }

    @Test
    public void startProcessAndDoStuffPerRequestStrategyTest() throws Exception { 
        setupProcessMocks(this, RuntimeStrategy.PER_REQUEST);
        
        // test
        runStartProcessAndDoStuffTest();
    }

    @Test
    public void startProcessAndDoStuffSingletonStrategyTest() throws Exception { 
        setupProcessMocks(this, RuntimeStrategy.SINGLETON);
        
        // test
        runStartProcessAndDoStuffTest();
    }

    /**
     * Runs the test. See {@link MockSetupTestHelper#setupProcessMocks(StartProcessEveryStrategyTest, RuntimeStrategy)}
     * for the real test logic.
     */
    private void runStartProcessAndDoStuffTest() { 
        // test start process
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(DEPLOYMENT_ID, new StartProcessCommand(TEST_PROCESS_DEF_NAME));
        cmdsRequest.setCorrelationKeyString("anton");
        JaxbCommandsResponse
        resp = this.jmsProcessJaxbCommandsRequest(cmdsRequest);

        // check response
        assertNotNull( "Null response", resp);
        List<JaxbCommandResponse<?>> resplist = resp.getResponses();
        assertNotNull( "Null response list", resplist);
        assertEquals( "Incorrect resp list size", 1, resplist.size() );
        JaxbCommandResponse<?> realResp = resplist.get(0);
        assertFalse( "An exception was thrown!", realResp instanceof JaxbExceptionResponse );
        assertTrue( "Expected process instance response", realResp instanceof JaxbProcessInstanceResponse );
        JaxbProcessInstanceResponse procInstResp = (JaxbProcessInstanceResponse) realResp;
        assertNotNull( "Null process instance", procInstResp);
        assertEquals( "Invalid process instance id", TEST_PROCESS_INST_ID, procInstResp.getId() );
        
        // Do jms call with process instance id this time. This will fail if: 
        // - the ProcessInstanceIdContext is not used (and an EmptyContext is used instead)
        // - The ProcessInstanceIdContext constructor gets a null value for the process instance id
        cmdsRequest = new JaxbCommandsRequest(DEPLOYMENT_ID, new SignalEventCommand(TEST_PROCESS_INST_ID, "test", null));
        cmdsRequest.setVersion(ServicesVersion.VERSION);
        cmdsRequest.setProcessInstanceId(TEST_PROCESS_INST_ID);
        resp = this.jmsProcessJaxbCommandsRequest(cmdsRequest);
      
        // check response
        assertNotNull( "Null response", resp);
        resplist = resp.getResponses();
        assertNotNull( "Null response list", resplist);
        assertEquals( "Incorrect resp list size", 1, resplist.size() );
        realResp = resplist.get(0);
        assertFalse( "An exception was thrown!", realResp instanceof JaxbExceptionResponse );
        
       // verify ksession is called
       verify(processServiceMock, times(2)).execute(any(String.class), any(Context.class), any(Command.class));
    }
    
}
