package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.Bean;
import org.drools.benchmark.model.Fire;
import org.drools.benchmark.model.Room;
import org.drools.benchmark.model.Sprinkler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import java.util.Random;

public class JoinBenchmark extends AbstractBenchmark {
    private final int beansNumber;

    private String[] drlFile;

    private StatefulKnowledgeSession ksession;

    private Bean[] beans;

    public JoinBenchmark(int beansNumber, String drlFile) {
        this.beansNumber = beansNumber;
        this.drlFile = drlFile.split(",");
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newStatefulKnowledgeSession();
        beans = Bean.generateRandomBeans(beansNumber);
    }

    public void execute(int repNr) {
        for (Bean bean : beans) {
            ksession.insert(bean);
        }
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
