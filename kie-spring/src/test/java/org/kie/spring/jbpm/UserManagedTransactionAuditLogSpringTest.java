/*
 * Copyright 2015 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.Collection;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import static org.junit.Assert.*;

import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;

/**
 * BZ 1123703 - Testing audit log service with user managed transactions.
 */
@RunWith(Parameterized.class)
public class UserManagedTransactionAuditLogSpringTest extends AbstractJbpmSpringParameterizedTest {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { JTA_EMF_SINGLETON_PATH, EmptyContext.get() },
                { JTA_EM_SINGLETON_PATH, EmptyContext.get() }
        };
        return Arrays.asList(data);
    };

    public UserManagedTransactionAuditLogSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testUserTransaction() throws Exception {
        UserTransaction ut = (UserTransaction) new InitialContext().lookup( JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME );
        ut.begin();
        RuntimeManager manager = getManager();
        RuntimeEngine engine = getEngine();
        KieSession ksession = getKieSession();
        ProcessInstance processInstance = ksession.startProcess(SCRIPT_TASK_PROCESS_ID);

        ProcessInstanceLog instanceLog = getLogService().findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_COMPLETED, instanceLog.getStatus().intValue());

        ut.commit();

        manager.disposeRuntimeEngine(engine);
    }
}
