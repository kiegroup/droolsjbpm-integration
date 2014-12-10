package org.kie.spring.jbpm;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.PessimisticLockException;


import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.junit.After;
import org.junit.Before;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PessimisticLockingSpringTest  {


    private static final Logger LOG = LoggerFactory.getLogger(PessimisticLockingSpringTest.class);
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { "classpath:jbpm/pessimistic-lock/pessimistic-locking-local-em-factory-beans.xml" },
                { "classpath:jbpm/pessimistic-lock/pessimistic-locking-local-emf-factory-beans.xml" },
        };
        return Arrays.asList(data);
    };


    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            LOG.info(">>> " + description.getMethodName() + " <<<");
        };

        protected void finished(Description description) {
            LOG.info("<<< DONE >>>");
        };
    };

    protected static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {

            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                new File(tempDir, file).delete();
            }
        }
    }

    protected ConfigurableApplicationContext context;
    protected String contextPath;
    protected RuntimeManager manager;
    protected AbstractPlatformTransactionManager tm;

    public PessimisticLockingSpringTest(String contextPath) {
        this.contextPath = contextPath;
    }

    @Before
    public void setup() {
        cleanupSingletonSessionId();
        LOG.info("Creating spring context - " + contextPath);
        context = new ClassPathXmlApplicationContext(contextPath);
        LOG.info("The spring context created.");
        tm = (AbstractPlatformTransactionManager) context.getBean("jbpmTxManager");
        assertNotNull(tm);


        manager = (RuntimeManager) context.getBean("runtimeManager");
    }

    @After
    public void cleanup() {
        try {
            if (manager != null) {
                manager.close();
                manager = null;
            }


        } catch (Exception ex) {

        }
        try {
            if (context != null) {
                context.close();
                context = null;
            }
        } catch (Exception ex) {

        }
    }

    public AuditLogService getAuditLogService() {
        return (AuditLogService) context.getBean("logService");
    }


    @Test
    public void testPessimisticLock() throws Exception {
        final DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();
        final List<Exception> exceptions = new ArrayList<Exception>();
        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

        final KieSession ksession = engine.getKieSession();

        final ProcessInstance processInstance = ksession.startProcess("org.jboss.qa.bpms.HumanTask");

        Thread t1 = new Thread() {
            @Override
            public void run() {
                TransactionStatus status = tm.getTransaction(defTransDefinition);
                LOG.info("Attempting to abort to lock process instance for 5 secs ");
                // getProcessInstance does not lock reliably so let's make a change that actually does something to the entity
                ksession.abortProcessInstance(processInstance.getId());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOG.info("Unlocked process instance after 5 secs");
                tm.rollback(status);
            }
        };

        t1.start();
        Thread.sleep(1000);

        // Trying to delete process instance in second transaction.
        // Should throw PessimisticLockException because we are trying to get write log on process instance with read lock
        Thread t2 = new Thread() {
            @Override
            public void run() {
                TransactionStatus status2 = tm.getTransaction(defTransDefinition);
                LOG.info("Trying to abort locked process instance");
                try {
                    ksession.abortProcessInstance(processInstance.getId());

                    LOG.info("Abort worked well");
                } catch (Exception e) {
                    LOG.info("Abort failed with error {}", e.getMessage());
                    exceptions.add(e);

                } finally {
                    tm.rollback(status2);
                }
            }
        };
        t2.start();

        Thread.sleep(3000);

        assertEquals(1, exceptions.size());
        assertEquals(PessimisticLockException.class.getName(), exceptions.get(0).getClass().getName());

        TransactionStatus status = tm.getTransaction(defTransDefinition);
        ProcessInstanceLog instanceLog = getAuditLogService().findProcessInstance(processInstance.getId());
        tm.commit(status);
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());


        status = tm.getTransaction(defTransDefinition);
        ksession.abortProcessInstance(processInstance.getId());
        tm.commit(status);

        status = tm.getTransaction(defTransDefinition);
        instanceLog = getAuditLogService().findProcessInstance(processInstance.getId());
        tm.commit(status);
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);

//        DefaultTransactionDefinition defTransDefinition = new DefaultTransactionDefinition();
//        DefaultTransactionDefinition requireNewdefTransDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
//
//        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
//        KieSession ksession = engine.getKieSession();
//        ProcessInstance processInstance = ksession.startProcess("org.jboss.qa.bpms.HumanTask");
//
//        // Using getProcessInstance for obtaining read lock for process instance
//        TransactionStatus status = tm.getTransaction(defTransDefinition);
//        ksession.getProcessInstance(processInstance.getId());
//
//        // Trying to delete process instance in nested transaction.
//        // Should throw PessimisticLockException because we are trying to get write log on process instance with read lock
//        TransactionStatus status2 = tm.getTransaction(requireNewdefTransDefinition);
//        try {
//            ksession.abortProcessInstance(processInstance.getId());
//            fail("Expected exception of type " + PessimisticLockException.class);
//        } catch (Exception e) {
//            assertEquals(e.getClass().getName(), PessimisticLockException.class.getName());
//        } finally {
//            tm.rollback(status2);
//            tm.rollback(status);
//        }
//
//        status = tm.getTransaction(defTransDefinition);
//        ProcessInstanceLog instanceLog = getAuditLogService().findProcessInstance(processInstance.getId());
//        tm.commit(status);
//        assertNotNull(instanceLog);
//        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());
//
//
//        status = tm.getTransaction(defTransDefinition);
//        ksession.abortProcessInstance(processInstance.getId());
//        tm.commit(status);
//
//        status = tm.getTransaction(defTransDefinition);
//        instanceLog = getAuditLogService().findProcessInstance(processInstance.getId());
//        tm.commit(status);
//        assertNotNull(instanceLog);
//        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());
//
//        manager.disposeRuntimeEngine(engine);

    }
}
