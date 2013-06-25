package org.jbpm.osgi.example;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationProcessExample {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProcessExample.class);

    private StatefulKnowledgeSession ksession;

    public static void main(String[] args) throws Exception {
        EvaluationProcessExample pr = new EvaluationProcessExample();
        pr.init();
        pr.destroy();
    }

    public void init() throws Exception {
        logger.info("Loading EvaluationProcess.bpmn2");

        /*KnowledgeBase kbase = createKnowledgeBase();
        ksession = createKnowledgeSession(kbase);

        RuntimeManager manager = getRuntimeManager("bpmn/EvaluationProcess.bpmn2");
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        ksession = runtime.getKieSession();*/

        KnowledgeBase kbase = readKnowledgeBase();
        ksession = kbase.newStatefulKnowledgeSession();

        logger.info("Register tasks");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("RegisterRequest", new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "UserId-12345");

        logger.info("Start process EvaluationProcess.bpmn2");
        ProcessInstance processInstance = ksession.startProcess("Evaluation", params);
        logger.info("Stated completed");
    }

    public void destroy() {
        this.ksession.destroy();
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("bpmn/EvaluationProcess.bpmn2"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    /*
    private KnowledgeBase createKnowledgeBase() {
        logger.info("Loading process " + "EvaluationProcess.bpmn2");
        kbuilder.add(ResourceFactory.newClassPathResource("bpmn/" + "EvaluationProcess.bpmn2"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    private static RuntimeManager getRuntimeManager(String process) {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.getDefault()
                .addAsset(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

   private StatefulKnowledgeSession createKnowledgeSession(KnowledgeBase kbase) {
        this.ksession = kbase.newStatefulKnowledgeSession();
        return this.ksession;
    }
    */

}
