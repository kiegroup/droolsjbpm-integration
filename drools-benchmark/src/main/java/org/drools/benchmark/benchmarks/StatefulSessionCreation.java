package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.runtime.StatefulKnowledgeSession;

public class StatefulSessionCreation extends AbstractBenchmark {

    private final StatefulKnowledgeSession[] kSessions;
    private static KnowledgeBase kbase;

    public StatefulSessionCreation(int sessionNumber) {
        this.kSessions = new StatefulKnowledgeSession[sessionNumber];
    }

    public synchronized void init(BenchmarkDefinition definition) {
        if (kbase == null) {
            kbase = createKnowledgeBase(createKnowledgeBuilder("licenseApplication.drl"));
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < kSessions.length; i++) {
            kSessions[i] = kbase.newStatefulKnowledgeSession();
        }
        for (int i = 0; i < kSessions.length; i++) {
            kSessions[i].dispose();
        }
    }

    public StatefulSessionCreation clone() {
        return new StatefulSessionCreation(kSessions.length);
    }
}
