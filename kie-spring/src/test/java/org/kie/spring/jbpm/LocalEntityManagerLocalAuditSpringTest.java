package org.kie.spring.jbpm;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class LocalEntityManagerLocalAuditSpringTest extends AbstractJbpmSpringTest {

    @Test
    public void testAuditLogTxLocal() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/local-em/local-em-spring-audit-em.xml");

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

    @Test
    public void testAuditLogTxJTA() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/jta-em/jta-em-spring-audit-em.xml");

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
