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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;
import org.jbpm.process.audit.AuditLogService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(Parameterized.class)
public class PessimisticLockingSpringTest extends AbstractJbpmSpringParameterizedTest  {


    private static final Logger log = LoggerFactory.getLogger(PessimisticLockingSpringTest.class);

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { PESSIMISTIC_LOCK_LOCAL_EM_PATH, EmptyContext.get() },
                { PESSIMISTIC_LOCK_LOCAL_EMF_PATH, EmptyContext.get() },
        };
        return Arrays.asList(data);
    };

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            log.debug(">>> " + description.getMethodName() + " <<<");
        };

        protected void finished(Description description) {
            log.debug("<<< DONE >>>");
        };
    };

    public PessimisticLockingSpringTest(String contextPath, Context<?> runtimeManagerContext) {
        super(contextPath, runtimeManagerContext);
    }

    @Test
    public void testPessimisticLock() throws Exception {

        RuntimeManager manager = getManager();
        final AbstractPlatformTransactionManager transactionManager = getTransactionManager();
        AuditLogService logService = getLogService();
        final DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();
        final List<Exception> exceptions = new ArrayList<Exception>();
        RuntimeEngine engine = getEngine();

        final KieSession ksession = getKieSession();

        final ProcessInstance processInstance = ksession.startProcess(HUMAN_TASK_PROCESS_ID);
        final ProcessInstanceStatus abortedProcessInstanceStatus = new ProcessInstanceStatus();

        final CountDownLatch txAcquiredSignal = new CountDownLatch(1);
        final CountDownLatch pessLockExceptionSignal = new CountDownLatch(1);
        final CountDownLatch threadsAreDoneLatch = new CountDownLatch(2);

        Thread t1 = new Thread() {
            @Override
            public void run() {
                TransactionStatus status = transactionManager.getTransaction(defTransDefinition);
                log.debug("Attempting to abort to lock process instance for 3 secs ");
                // getProcessInstance does not lock reliably so let's make a change that actually does something to the entity
                ksession.abortProcessInstance(processInstance.getId());
                
                // let thread 2 start once we have the transaction
                txAcquiredSignal.countDown();


                try {
                    // keep the lock until thread 2 let's us know it's done
                    pessLockExceptionSignal.await();
                } catch (InterruptedException e) {
                    // do nothing
                }
                log.debug("Commited process instance aborting after 3 secs");
                transactionManager.commit(status);
                
                // let main test thread know we're done
                threadsAreDoneLatch.countDown();
            }
        };

        // Trying to retrieve process instance in second thread.
        // Should throw PessimisticLockException because we are trying to get write lock on process instance which already have lock
        Thread t2 = new Thread() {
            @Override
            public void run() {
                try { 
                    // wait for thread 1 to tell us it has the lock
                    txAcquiredSignal.await();
                } catch( InterruptedException e ) { 
                    // do nothing
                }
                log.debug("Trying to get process instance - should fail because process instance is locked or wait until thread 1 finish and return null because process instance is deleted.");
                try {
                    ProcessInstance abortedProcessInstance = ksession.getProcessInstance(processInstance.getId(), true);

                    if(abortedProcessInstance == null) {
                        abortedProcessInstanceStatus.setAbortedProcessInstance(true);
                    }

                    log.debug("Get request worked well");
                } catch (Exception e) {
                    log.debug("Get request failed with error {}", e.getMessage());
                    exceptions.add(e);
                } finally {
                    // Tell thread 1 that we're done
                    pessLockExceptionSignal.countDown();
                }
                
                // let main test thread know we're done
                threadsAreDoneLatch.countDown();
            }
        };
        
        t1.start();
        t2.start();

        // wait for both threads to finish!
        threadsAreDoneLatch.await();

        // If process instance read by thread 2 is aborted then it means that database transaction timeout is bigger than waiting time set here and
        // getProcessInstance was waiting for thread 1 to finish its work.
        // Therefore exception list should be empty.
        if(abortedProcessInstanceStatus.isAbortedProcessInstance()) {
            assertEquals(0, exceptions.size());
        } else {
            // Otherwise database transaction timeout should be lower than waiting time set and thread 2 should throw PessimisticLockException or
            // LockTimeoutException.
            assertEquals(1, exceptions.size());
            assertThat(exceptions.get(0).getClass().getName(), anyOf(equalTo(PessimisticLockException.class.getName()), equalTo(LockTimeoutException.class.getName())));
        }

        TransactionStatus status = transactionManager.getTransaction(defTransDefinition);
        ProcessInstanceLog instanceLog = logService.findProcessInstance(processInstance.getId());
        transactionManager.commit(status);
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);
    }

    /**
     * Helper class to pass information about aborted process instance between threads.
     */
    private class ProcessInstanceStatus {
        private boolean abortedProcessInstance = false;

        public boolean isAbortedProcessInstance() {
            return abortedProcessInstance;
        }

        public void setAbortedProcessInstance(boolean abortedProcessInstance) {
            this.abortedProcessInstance = abortedProcessInstance;
        }
    }
}
