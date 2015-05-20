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
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test used to verify correct initialization of runtime manager without initial context factory configured for local transactions.
 */
@RunWith(Parameterized.class)
public class RuntimeManagerInitNoInitialContextSpringTest extends AbstractJbpmSpringTest {

    private static String CONTEXT_FACTORY;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { "jbpm/local-emf/singleton.xml", EmptyContext.get() },
                { "jbpm/local-emf/per-process-instance.xml", ProcessInstanceIdContext.get() },
                { "jbpm/local-emf/per-request.xml", EmptyContext.get() },
        };
        return Arrays.asList(data);
    };

    @Parameterized.Parameter(0)
    public String contextPath;

    @Parameterized.Parameter(1)
    public Context<?> runtimeManagerContext;

    @BeforeClass
    public static void setUp() throws Exception {
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

    /**
     * Simple test to verify that application is operational even without context factory defined.
     */
    @Test
    public void testSimpleTaskInvocation() throws Exception{

        context = new ClassPathXmlApplicationContext(contextPath);

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");
        AuditLogService auditLogService = (AuditLogService) context.getBean("logService");

        RuntimeEngine engine = manager.getRuntimeEngine(runtimeManagerContext);
        KieSession ksession = engine.getKieSession();

        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        System.out.println("Process started");

        ProcessInstanceLog instanceLog = auditLogService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());

        ksession.abortProcessInstance(processInstance.getId());

        System.out.println("Process instance aborted");

        instanceLog = auditLogService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);
    }
}
