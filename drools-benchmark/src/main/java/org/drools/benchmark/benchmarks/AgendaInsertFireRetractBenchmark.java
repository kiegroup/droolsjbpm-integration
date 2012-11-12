package org.drools.benchmark.benchmarks;

import org.drools.common.DefaultFactHandle;
import org.kie.runtime.rule.FactHandle;

public class AgendaInsertFireRetractBenchmark extends AgendaBenchmark {

    public AgendaInsertFireRetractBenchmark(int rulesNr) {
        super(rulesNr);
    }

    public void execute(int repNr) {
        for (int i = 0; i < rulesNr; i++) {
            facts[i] = ksession.insert(new Integer(i));
        }
        ksession.fireAllRules();
        for (FactHandle fact : facts) {
            ksession.retract(fact);
        }
        ksession.fireAllRules();
    }
}
