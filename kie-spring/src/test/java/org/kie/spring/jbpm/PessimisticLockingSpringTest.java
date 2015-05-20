package org.kie.spring.jbpm;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(Parameterized.class)
public class PessimisticLockingSpringTest extends AbstractJbpmSpringTest  {


    private static final Logger LOG = LoggerFactory.getLogger(PessimisticLockingSpringTest.class);

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { "classpath:jbpm/pessimistic-lock/pessimistic-locking-local-em-factory-beans.xml" },
                { "classpath:jbpm/pessimistic-lock/pessimistic-locking-local-emf-factory-beans.xml" },
        };
        return Arrays.asList(data);
    };

    @Parameterized.Parameter(0)
    public String contextPath;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            LOG.info(">>> " + description.getMethodName() + " <<<");
        };

        protected void finished(Description description) {
            LOG.info("<<< DONE >>>");
        };
    };

    @Test
    public void testPessimisticLock() throws Exception {

        context = new ClassPathXmlApplicationContext(contextPath);

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");
        final AbstractPlatformTransactionManager tm = (AbstractPlatformTransactionManager) context.getBean("jbpmTxManager");
        AuditLogService logService = (AuditLogService) context.getBean("logService");

        final DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();
        final List<Exception> exceptions = new ArrayList<Exception>();
        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

        final KieSession ksession = engine.getKieSession();

        final ProcessInstance processInstance = ksession.startProcess("org.jboss.qa.bpms.HumanTask");
        final ProcessInstanceStatus abortedProcessInstanceStatus = new ProcessInstanceStatus();

        Thread t1 = new Thread() {
            @Override
            public void run() {
                TransactionStatus status = tm.getTransaction(defTransDefinition);
                LOG.info("Attempting to abort to lock process instance for 3 secs ");
                // getProcessInstance does not lock reliably so let's make a change that actually does something to the entity
                ksession.abortProcessInstance(processInstance.getId());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOG.info("Commited process instance aborting after 3 secs");
                tm.commit(status);
            }
        };

        t1.start();
        Thread.sleep(1000);

        // Trying to retrieve process instance in second thread.
        // Should throw PessimisticLockException because we are trying to get write lock on process instance which already have lock
        Thread t2 = new Thread() {
            @Override
            public void run() {
                LOG.info("Trying to get process instance - should fail because process instance is locked or wait until thread 1 finish and return null because process instance is deleted.");
                try {
                    ProcessInstance abortedProcessInstance = ksession.getProcessInstance(processInstance.getId(), true);

                    if(abortedProcessInstance == null) {
                        abortedProcessInstanceStatus.setAbortedProcessInstance(true);
                    }

                    LOG.info("Get request worked well");
                } catch (Exception e) {
                    LOG.info("Get request failed with error {}", e.getMessage());
                    exceptions.add(e);
                }
            }
        };
        t2.start();

        Thread.sleep(3000);

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

        TransactionStatus status = tm.getTransaction(defTransDefinition);
        ProcessInstanceLog instanceLog = logService.findProcessInstance(processInstance.getId());
        tm.commit(status);
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
