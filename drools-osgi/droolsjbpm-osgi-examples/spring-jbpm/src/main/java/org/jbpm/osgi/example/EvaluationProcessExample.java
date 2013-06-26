package org.jbpm.osgi.example;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationProcessExample {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationProcessExample.class);
    private KieSession ksession;
    private RuntimeManager rt;
    private RuntimeEngine re;

    public static void main(String[] args) throws Exception {
        EvaluationProcessExample pr = new EvaluationProcessExample();
        pr.init();
        pr.destroy();
    }

    public void init() throws Exception {
        logger.info("Loading EvaluationProcess.bpmn2");

        rt = getRuntimeManager("bpmn/EvaluationProcess.bpmn2");
        re = rt.getRuntimeEngine(EmptyContext.get());
        ksession = re.getKieSession();

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
        ksession.destroy();
        rt.disposeRuntimeEngine(re);
    }

    private static RuntimeManager getRuntimeManager(String process) {
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.getEmpty()
                .addAsset(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }

}
