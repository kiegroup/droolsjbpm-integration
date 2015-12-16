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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * Test used to verify correct initialization of runtime manager without initial context factory configured for local transactions.
 */
@RunWith(Parameterized.class)
public class RuntimeManagerInitNoInitialContextSpringTest extends AbstractJbpmSpringParameterizedTest {

    private static String CONTEXT_FACTORY;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { NO_INITIAL_CONTEXT_LOCAL_EMF_SINGLETON_PATH, EmptyContext.get() },
                { NO_INITIAL_CONTEXT_LOCAL_EMF_PER_PROCESS_PATH, ProcessInstanceIdContext.get() },
                { NO_INITIAL_CONTEXT_LOCAL_EMF_PER_REQUEST_PATH, EmptyContext.get() }
        };
        return Arrays.asList(data);
    };

    @BeforeClass
    public static void setUpProperty() throws Exception {
        CONTEXT_FACTORY = System.getProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY);

        // setting some text as INITIAL_CONTEXT_FACTORY to overwrite test configuration
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "incorrectFactory");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (CONTEXT_FACTORY != null) {
            System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        } else {
            System.clearProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
        }
    }

    public RuntimeManagerInitNoInitialContextSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    /**
     * Simple test to verify that application is operational even without context factory defined.
     */
    @Test
    public void testSimpleTaskInvocation() throws Exception {

        RuntimeManager manager = getManager();
        AuditLogService logService = getLogService();

        RuntimeEngine engine = getEngine();
        KieSession ksession = getKieSession();
        ProcessInstance processInstance = ksession.startProcess(SAMPLE_HELLO_PROCESS_ID);

        System.out.println("Process started");

        ProcessInstanceLog instanceLog = logService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());

        ksession.abortProcessInstance(processInstance.getId());

        System.out.println("Process instance aborted");

        instanceLog = logService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);
    }
}
