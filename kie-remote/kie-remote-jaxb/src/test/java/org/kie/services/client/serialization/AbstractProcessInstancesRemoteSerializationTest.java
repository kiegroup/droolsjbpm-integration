package org.kie.services.client.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.serialization.AbstractRemoteSerializationTest.TestType;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessInstancesRemoteSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRemoteSerializationTest.class);

    abstract public TestType getType();

    abstract public void addClassesToSerializationProvider( Class<?>... extraClass );

    public abstract <T> T testRoundTrip( T in ) throws Exception;

    public AbstractProcessInstancesRemoteSerializationTest() {
        super(true, true);
    }
    
    @Test
    public void processInstanceWithVariablesTest() throws Exception {

        RuntimeEngine runtimeEngine = createRuntimeManager("BPMN2-StringStructureRef.bpmn2").getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        ProcessInstance processInstance = ksession.startProcess("StructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "test value");
        // ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), res);

        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "initial-val");

        JaxbProcessInstanceWithVariablesResponse jpiwvr = new JaxbProcessInstanceWithVariablesResponse(processInstance, map);
        testRoundTrip(jpiwvr);

        JaxbProcessInstanceListResponse jpilp = new JaxbProcessInstanceListResponse();
        List<ProcessInstance> procInstList = new ArrayList<ProcessInstance>();
        procInstList.add(new JaxbProcessInstanceResponse(processInstance));
        jpilp.setResult(procInstList);
        testRoundTrip(jpilp);
    }

}
