package org.kie.services.client.jaxb;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.services.client.SerializationTest;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceWithVariablesResponse;

public class JaxbSerializationTest extends SerializationTest {

    public Object testRoundtrip(Object in) throws Exception {
        String xmlObject = JaxbSerializationProvider.convertJaxbObjectToString(in);
        log.debug(xmlObject);
        return JaxbSerializationProvider.convertStringToJaxbObject(xmlObject);
    }
 
    @Test
    public void factHandleTest() throws Exception { 
        KieSession ksession = createKnowledgeSession(null);
        
        InsertObjectCommand cmd = new InsertObjectCommand("The Sky is Green");
        FactHandle factHandle = ksession.execute(cmd);
        JaxbOtherResponse jor = new JaxbOtherResponse(factHandle, 0, cmd);
        testRoundtrip(jor);
    }
    
    @Test
    public void processInstanceWithVariablesTest() throws Exception {
        KieSession ksession = createKnowledgeSession("BPMN2-StringStructureRef.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        ProcessInstance processInstance = ksession.startProcess("StructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "test value");
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), res);

        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "initial-val");
        
        JaxbProcessInstanceWithVariablesResponse jpiwvr = new JaxbProcessInstanceWithVariablesResponse(processInstance, map);
        testRoundtrip(jpiwvr);
    }
}
