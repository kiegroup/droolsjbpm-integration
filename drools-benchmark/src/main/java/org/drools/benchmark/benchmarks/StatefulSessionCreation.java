package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.runtime.StatefulKnowledgeSession;

public class StatefulSessionCreation extends AbstractBenchmark {

    private KnowledgeBase kbase;

    public void init(BenchmarkDefinition definition) {
        kbase = createKnowledgeBase(createKnowledgeBuilder("licenseApplication.drl"));
    }

    public void execute(int repNr) {
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
    }

}
