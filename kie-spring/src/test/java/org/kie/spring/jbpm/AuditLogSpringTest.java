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
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Parameterized.class)
public class AuditLogSpringTest extends AbstractJbpmSpringTest {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][] {
                { "jbpm/jta-em/singleton.xml" },
                { "jbpm/local-em/singleton.xml" }
             };
        return Arrays.asList(data);
    };

    @Parameterized.Parameter(0)
    public String contextPath;

    @Test
    public void testAuditLog() throws Exception {

        context = new ClassPathXmlApplicationContext(contextPath);

        RuntimeManager manager = (RuntimeManager) context.getBean("runtimeManager");
        AuditLogService auditLogService = (AuditLogService) context.getBean("logService");

        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
        final KieSession ksession = engine.getKieSession();
        final ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        ProcessInstanceLog instanceLog = auditLogService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ACTIVE, instanceLog.getStatus().intValue());

        ksession.abortProcessInstance(processInstance.getId());

        instanceLog = auditLogService.findProcessInstance(processInstance.getId());
        assertNotNull(instanceLog);
        assertEquals(ProcessInstance.STATE_ABORTED, instanceLog.getStatus().intValue());

        manager.disposeRuntimeEngine(engine);
    }
}
