package org.drools.benchmark.benchmarks;

import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.A;
import org.drools.benchmark.model.B;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import java.util.Random;

public class RangeIndexLeftModifyNoMatch extends AbstractBenchmark {
    private final int aNr;
    private final int modifications;

    private final String drlFile;

    private StatefulKnowledgeSession ksession;

    private Random random = new Random(0);

    private A[] as;

    public RangeIndexLeftModifyNoMatch(int aNr, int modifications, String drlFile) {
        this.aNr = aNr;
        this.modifications = modifications;
        this.drlFile = drlFile;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();

        int aLimit = aNr * modifications;

        as = new A[aNr];
        for (int i = 0; i < aNr; i++) {
            int randomInt = random.nextInt(aLimit);
            if (i % 2 == 1) {
                randomInt += aLimit * 3;
            }
            as[i] = new A(randomInt, randomInt+modifications);
        }
    }

    public void execute(int repNr) {
        for (int i = 0; i < aNr; i++) {
            ksession.insert(as[i]);
        }
        ksession.insert(new B(aNr * modifications * 2));
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
