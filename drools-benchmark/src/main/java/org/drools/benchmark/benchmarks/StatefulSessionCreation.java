package org.drools.benchmark.benchmarks;

import org.drools.benchmark.BenchmarkDefinition;
import org.kie.internal.KnowledgeBase;
import org.kie.runtime.StatefulKnowledgeSession;

public class StatefulSessionCreation extends AbstractBenchmark {

    private final StatefulKnowledgeSession[] kSessions;
    private static KnowledgeBase kbase;

    public StatefulSessionCreation(int sessionNumber) {
        this.kSessions = new StatefulKnowledgeSession[sessionNumber];
    }

    public void init(BenchmarkDefinition definition, boolean isFirst) {
        if (isFirst) {
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
