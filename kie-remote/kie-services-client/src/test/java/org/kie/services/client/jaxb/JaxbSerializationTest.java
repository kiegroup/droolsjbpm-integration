package org.kie.services.client.jaxb;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.drools.core.command.assertion.AssertEquals;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.services.client.SerializationTest;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceWithVariablesResponse;

public class JaxbSerializationTest extends SerializationTest {

    public Object testRoundtrip(Object in) throws Exception {
        String xmlObject = JaxbSerializationProvider.convertJaxbObjectToString(in);
        log.debug(xmlObject);
        return JaxbSerializationProvider.convertStringToJaxbObject(xmlObject);
    }
 
    @Test
    public void acceptedCommandsCanBeSerializedTest() throws Exception { 
       Field commandsField = JaxbCommandsRequest.class.getDeclaredField("commands");
       XmlElements xmlElemsAnno = (XmlElements) commandsField.getAnnotations()[0];
       XmlElement [] xmlElems = xmlElemsAnno.value();
       Set<Class> cmdSet = new HashSet<Class>(AcceptedCommands.getSet());
       assertEquals(cmdSet.size(), xmlElems.length);
       for( XmlElement xmlElemAnno : xmlElems ) {
           Class cmdClass = xmlElemAnno.type();
           assertTrue( cmdClass.getSimpleName() + " is present in " + JaxbCommandsRequest.class.getSimpleName() + " but not in " + AcceptedCommands.class.getSimpleName(),
                   cmdSet.remove(cmdClass) );
       }
       for( Class cmdClass : cmdSet ) { 
           System.out.println( "Missing: " + cmdClass.getSimpleName());
       }
       assertTrue( "See output for classes in " + AcceptedCommands.class.getSimpleName() + " that are not in " + JaxbCommandsRequest.class.getSimpleName(),
               cmdSet.size() == 0);
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
        
        JaxbProcessInstanceListResponse jpilp = new JaxbProcessInstanceListResponse();
        List<ProcessInstance> procInstList = new ArrayList<ProcessInstance>();
        procInstList.add(new JaxbProcessInstanceResponse(processInstance));
        jpilp.setResult(procInstList);
        testRoundtrip(jpilp);
    }
    
    @Test 
    public void commandsSerializationTest() throws Exception { 
        KieSession ksession = createKnowledgeSession("BPMN2-StringStructureRef.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        ProcessInstance processInstance = ksession.startProcess("StructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        
        
    }
}
