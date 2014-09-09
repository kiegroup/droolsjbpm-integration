package org.kie.spring.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static junit.framework.Assert.*;

public class LocalEntityManagerFactorySpringLaneTest extends AbstractJbpmSpringTest {

    @Test
    public void testSpringWithJTAAndEMF() throws Exception{


        context = new ClassPathXmlApplicationContext("jbpm/jpa-local-lane/jpa-spring.xml");


        EntityManagerFactory emf = (EntityManagerFactory) context.getBean("jbpmEMF");
        AbstractPlatformTransactionManager aptm = (AbstractPlatformTransactionManager) context.getBean( "jbpmTxManager" );

        Properties auth = new Properties();
        auth.setProperty("max", "Standard");

        UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(auth);

        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.getDefault()
                .entityManagerFactory(emf)
                .userGroupCallback(userGroupCallback)
                .addAsset(ResourceFactory.newClassPathResource("jbpm/processes/sample1.bpmn2"), ResourceType.BPMN2)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, aptm)
                .get();

        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());

        KieSession ksession = engine.getKieSession();
        TaskService taskService = engine.getTaskService();

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("userid", "max");
        ProcessInstance processInstance = ksession.startProcess("agu.samples.sample1", params);

        AuditLogService logService = (AuditLogService) context.getBean("logService");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = aptm.getTransaction(def);
        ProcessInstanceLog log = logService.findProcessInstance(processInstance.getId());
        assertNotNull(log);
        aptm.commit(status);

        List<TaskSummary> tasks = taskService.getTasksOwned("max", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'max'");

        long taskId = tasks.get(0).getId();
        taskService.start(taskId, "max");
        taskService.complete(taskId, "max", null);


        tasks = taskService.getTasksAssignedAsPotentialOwner("max", "en-UK");
        System.out.println("Found " + tasks.size() + " task(s) for user 'max'");

        taskId = tasks.get(0).getId();
        taskService.start(taskId, "max");
        taskService.complete(taskId, "max", null);


        processInstance = ksession.getProcessInstance(processInstance.getId());
        assertNull(processInstance);
        System.out.println("Process instance completed");
    }

}
