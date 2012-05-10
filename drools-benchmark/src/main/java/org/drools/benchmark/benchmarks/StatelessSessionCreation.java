package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.runtime.StatelessKnowledgeSession;

public class StatelessSessionCreation extends AbstractBenchmark {

    private final int sessionNumber;
    private static KnowledgeBase kbase;

    public StatelessSessionCreation(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public synchronized void init(BenchmarkDefinition definition) {
        if (kbase == null) {
            kbase = createKnowledgeBase(createKnowledgeBuilder("licenseApplication.drl"));
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < sessionNumber; i++) {
            StatelessKnowledgeSession session = kbase.newStatelessKnowledgeSession();
        }
    }

    public StatelessSessionCreation clone() {
        return new StatelessSessionCreation(sessionNumber);
    }
}