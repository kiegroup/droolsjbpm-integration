package org.drools.benchmark.benchmarks;

import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.Bean;
import org.kie.KnowledgeBase;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

public class RandomBeansBenchmark extends AbstractBenchmark {
    private final int beansNumber;

    private KnowledgeBase kbase;

    private StatefulKnowledgeSession ksession;

    private Bean[] beans;
    private FactHandle[] factHandles;

    public RandomBeansBenchmark(int beansNumber, String drlFile) {
        this.beansNumber = beansNumber;
        kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile.split(",")));
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        ksession = kbase.newStatefulKnowledgeSession();
        beans = Bean.generateRandomBeans(beansNumber);
        factHandles = new FactHandle[beans.length];
    }

    public void execute(int repNr) {
        int i = 0;
        for (Bean bean : beans) {
            factHandles[i++] = ksession.insert(bean);
        }
        ksession.fireAllRules();
        for (FactHandle factHandle : factHandles) {
            ksession.retract(factHandle);
        }
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
