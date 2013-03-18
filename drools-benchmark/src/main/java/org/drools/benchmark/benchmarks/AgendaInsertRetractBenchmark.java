package org.drools.benchmark.benchmarks;

import org.kie.api.runtime.rule.FactHandle;

public class AgendaInsertRetractBenchmark extends AgendaBenchmark {

    public AgendaInsertRetractBenchmark(int rulesNr) {
        super(rulesNr);
    }

    public void execute(int repNr) {
        for (int i = 0; i < rulesNr; i++) {
            facts[i] = ksession.insert(new Integer(i));
        }
        for (FactHandle fact : facts) {
            ksession.retract(fact);
        }
    }
}
