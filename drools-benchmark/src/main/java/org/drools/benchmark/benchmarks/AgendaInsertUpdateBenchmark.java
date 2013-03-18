package org.drools.benchmark.benchmarks;

import org.drools.core.common.InternalFactHandle;
import org.kie.api.runtime.rule.FactHandle;

public class AgendaInsertUpdateBenchmark extends AgendaBenchmark {

    public AgendaInsertUpdateBenchmark(int rulesNr) {
        super(rulesNr);
    }

    public void execute(int repNr) {
        for (int i = 0; i < rulesNr; i++) {
            facts[i] = ksession.insert(new Integer(i));
        }
        for (FactHandle fact : facts) {
            int oldInt = (Integer)((InternalFactHandle)fact).getObject();
            ksession.update(fact, new Integer(oldInt+1));
        }
    }


    @Override
    public void terminate() {
        for (FactHandle fact : facts) {
            ksession.retract(fact);
        }
        super.terminate();
    }
}
