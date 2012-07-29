package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.A;
import org.drools.benchmark.model.B;
import org.drools.runtime.StatefulKnowledgeSession;

import java.util.Random;

public class CompareIndexRightModifyNoMatchManyLeft extends AbstractBenchmark {

    private final int objectNr;
    private final int modifications;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private A[] as;
    private B[] bs;

    public CompareIndexRightModifyNoMatchManyLeft(int aNr, int modifications, String drlFile) {
        this.objectNr = aNr;
        this.modifications = modifications;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();

        int aLimit = objectNr * modifications;

        as = new A[objectNr];
        for (int i = 0; i < objectNr; i++) {
            as[i] = new A(random.nextInt(aLimit));
        }

        bs = new B[objectNr];
        for (int i = 0; i < objectNr; i++) {
            int randomInt = random.nextInt(aLimit) + (aLimit * 2);
            bs[i] = new B(randomInt, randomInt+modifications);
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < objectNr; i++) {
            ksession.insert(as[i]);
            ksession.insert(bs[i]);
        }
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
