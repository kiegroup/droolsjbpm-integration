package org.jbpm.osgi.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import static org.junit.Assert.*;

public class RuleTaskExample {

    public void configure() {
        System.out.println("Loading process BPMN2-RuleTask.bpmn2");
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("bpmn/BPMN2-RuleTask.bpmn2"), ResourceType.BPMN2);
        kbuilder.add(ResourceFactory.newClassPathResource("bpmn/BPMN2-RuleTask.drl"), ResourceType.DRL);
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        List<String> list = new ArrayList<String>();
        ksession.setGlobal("list", list);
        System.out.println("Start process BPMN2-RuleTask.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("RuleTask");
        System.out.println("Process BPMN2-RuleTask.bpmn2 started");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession.fireAllRules();
        System.out.println("Fire drools rules");
        assertTrue(list.size() == 1);
        assertNull(ksession.getProcessInstance(processInstance.getId()));
    }

    public void configure2() throws Exception {
        System.out.println("Loading BPMN2-EvaluationProcess.bpmn2");
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-EvaluationProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        System.out.println("Register tasks");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");
        System.out.println("Start process BPMN2-EvaluationProcess.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_COMPLETED);
        System.out.println("Stated compledted");
    }

    private KnowledgeBase createKnowledgeBase(String process) throws Exception {
        System.out.println("Loading process " + process);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("bpmn/" + process), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    private StatefulKnowledgeSession createKnowledgeSession(KnowledgeBase kbase) {
        return kbase.newStatefulKnowledgeSession();
    }

}
