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

package org.kie.spring.jbpm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;

@RunWith(Parameterized.class)
public class AuditLogSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { JTA_EM_SINGLETON_PATH, EmptyContext.get() },
                { LOCAL_EM_SINGLETON_PATH, EmptyContext.get() },
                { JTA_EMF_SINGLETON_PATH, EmptyContext.get() },
                { LOCAL_EMF_SINGLETON_PATH, EmptyContext.get() }
             };
        return Arrays.asList(data);
    };

    public AuditLogSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testAuditLog() throws Exception {
        KieSession ksession = getKieSession();
        final ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

        RuntimeManager manager = getManager();
        RuntimeEngine engine = getEngine();
        AuditLogService logService = getLogService();
        ProcessInstanceLog instanceLog = logService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());

        ksession.abortProcessInstance(processInstance.getId());

        instanceLog = logService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);
    }
    
    @Test
	public void testAuditLogFromRuntimeEngine() throws Exception {
		KieSession ksession = getKieSession();
		final ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

		RuntimeManager manager = getManager();
		RuntimeEngine engine = getEngine();
		AuditService logService = engine.getAuditService();
		org.kie.api.runtime.manager.audit.ProcessInstanceLog instanceLog = logService.findProcessInstance(processInstance.getId());
		assertNotNull(instanceLog);
		assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());

		ksession.abortProcessInstance(processInstance.getId());

		instanceLog = logService.findProcessInstance(processInstance.getId());
		assertNotNull(instanceLog);
		assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

		manager.disposeRuntimeEngine(engine);
	}

    
    
   
}
