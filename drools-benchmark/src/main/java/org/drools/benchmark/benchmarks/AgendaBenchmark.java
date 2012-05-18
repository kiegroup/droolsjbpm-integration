package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public abstract class AgendaBenchmark extends AbstractBenchmark {

    protected final int rulesNr;

    private static final String RULE =
            "rule R%i salience %i\n" +
            "when\n" +
            "    $a : Integer( intValue == %i )\n" +
            "then\n" +
            "end\n\n";

    protected StatefulKnowledgeSession ksession;
    protected FactHandle[] facts;
    private KnowledgeBase kbase;

    public AgendaBenchmark(int rulesNr) {
        this.rulesNr = rulesNr;
        String drl = repeatPatternString(RULE, rulesNr);
        kbase = createKnowledgeBase(drl);
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        ksession = kbase.newStatefulKnowledgeSession();
        facts = new FactHandle[rulesNr];
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
