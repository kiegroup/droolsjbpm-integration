package org.jbpm.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RuntimeManagerComponent.class)
public class SimpleProcessTest {

    @Autowired
    RuntimeManagerComponent runtimeManagerComponent;

    @Test
    public void testRuntimeManagerComponent() {
        assertNotNull(runtimeManagerComponent);
    }

    @Test
    public void runSimpleProcess() {
        RuntimeManager manager = runtimeManagerComponent.createRuntimeManager("simpleprocess.bpmn2");
        assertNotNull(manager);
        RuntimeEngine engine = runtimeManagerComponent.getRuntimeEngine(EmptyContext.get());

        KieSession ksession = engine.getKieSession();

        ProcessInstance processInstance = ksession.startProcess("simpleprocess");
        long processInstanceId = processInstance.getId();
        assertEquals(1L, processInstanceId);
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }
}
