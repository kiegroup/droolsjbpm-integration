package org.kie.remote.services.jms;

import static org.junit.Assert.*;
import static org.kie.remote.services.MockSetupTestHelper.DEPLOYMENT_ID;
import static org.kie.remote.services.MockSetupTestHelper.setupProcessMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.internal.deployment.DeploymentUnit.RuntimeStrategy;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.remote.services.MockSetupTestHelper;
import org.kie.remote.services.StartProcessEveryStrategyTest;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * JMS counterpart to RestStartProcessEveryStrategyTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EmptyContext.class, ProcessInstanceIdContext.class})
public class JmsStartProcessEveryStrategyTest extends RequestMessageBean implements StartProcessEveryStrategyTest {

    private DeploymentInfoBean runtimeMgrMgrMock;
    private KieSession kieSessionMock;
    
    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock) {
        this.runtimeMgrMgrMock = mock;
    }

    public void setKieSessionMock(KieSession kieSessionMock) {
        this.kieSessionMock = kieSessionMock;
    }

    public void setupTestMocks() {
        this.runtimeMgrMgr = runtimeMgrMgrMock;

        this.processRequestBean = new ProcessRequestBean();
        this.processRequestBean.setRuntimeMgrMgr(runtimeMgrMgrMock);
    }

    @Test
    public void startProcessAndDoStuffPerProcessStartegyTest() throws Exception {
        // This method does some static mock magic to make sure 
        // - that EmptyContext.get() throws an exception if it is called here (since a ProcessInstanceIdContext should be used instead)
        // - that a process instance id *is* used in the second call in the constructor of the ProcessInstanceIdContext
        setupProcessMocks(this, RuntimeStrategy.PER_PROCESS_INSTANCE);
       
        // test
        runStartProcessAndDoStuffTest();

        // verify
        PowerMockito.verifyStatic(times(0));
        EmptyContext.get();
        PowerMockito.verifyStatic(times(1));
        ProcessInstanceIdContext.get();
        PowerMockito.verifyStatic(times(1));
        ProcessInstanceIdContext.get(anyLong());
    }

    @Test
    public void startProcessAndDoStuffPerRequestStrategyTest() throws Exception { 
        setupProcessMocks(this, RuntimeStrategy.PER_REQUEST);
        
        // test
        runStartProcessAndDoStuffTest();
        
        // verify
        PowerMockito.verifyStatic(times(2));
        EmptyContext.get();
    }

    @Test
    public void startProcessAndDoStuffSingletonStrategyTest() throws Exception { 
        setupProcessMocks(this, RuntimeStrategy.SINGLETON);
        
        // test
        runStartProcessAndDoStuffTest();
        
        // verify
        PowerMockito.verifyStatic(times(2));
        EmptyContext.get();
    }

    /**
     * Runs the test. See {@link MockSetupTestHelper#setupProcessMocks(StartProcessEveryStrategyTest, RuntimeStrategy)}
     * for the real test logic.
     */
    private void runStartProcessAndDoStuffTest() { 
        // test start process
        JaxbCommandsRequest 
        cmdsRequest = new JaxbCommandsRequest(DEPLOYMENT_ID, new StartProcessCommand(TEST_PROCESS_DEF_NAME));
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
        
        // Do rest call with process instance id this time. This will fail if: 
        // - the ProcessInstanceIdContext is not used (and an EmptyContext is used instead)
        // - The ProcessInstanceIdContext constructor gets a null value for the process instance id
        cmdsRequest = new JaxbCommandsRequest(DEPLOYMENT_ID, new SignalEventCommand(TEST_PROCESS_INST_ID, "test", null));
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
       verify(kieSessionMock, times(2)).execute(any(Command.class));
    }
    
}
