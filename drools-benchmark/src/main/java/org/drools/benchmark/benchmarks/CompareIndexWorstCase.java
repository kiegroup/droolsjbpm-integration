package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.A;
import org.drools.benchmark.model.B;
import org.drools.runtime.StatefulKnowledgeSession;

import java.util.Random;

public class CompareIndexWorstCase extends AbstractBenchmark {

    private final int objectNr;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private B[] bs;

    public CompareIndexWorstCase(int objectNr, String drlFile) {
        this.objectNr = objectNr;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();
    }

    public void execute(int repNr) {
        ksession.insert(new A(Integer.MAX_VALUE, Integer.MAX_VALUE, true));

        for (int i = 0; i < objectNr; i++) {
            ksession.insert(new B(i, 0, i == 0));
        }

        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
