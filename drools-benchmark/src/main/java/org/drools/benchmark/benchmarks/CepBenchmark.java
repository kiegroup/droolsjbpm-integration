package org.drools.benchmark.benchmarks;

import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.cep.Figure;
import org.drools.benchmark.model.cep.Letter;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KieBaseConfiguration;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.api.conf.EventProcessingOption;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class CepBenchmark extends AbstractBenchmark {

    private final int eventNr;

    private int offset = 0;

    private static KnowledgeBase kbase;

    private StatefulKnowledgeSession ksession;

    public CepBenchmark(int eventNr) {
        this.eventNr = eventNr;
    }

    @Override
    public void init(BenchmarkDefinition definition, boolean isFirst) {
        if (isFirst) {
            KnowledgeBuilder kbuilder = createKnowledgeBuilder("cep.drl");

            KieBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
            config.setOption( EventProcessingOption.STREAM );

            kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
            kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        }

        ksession = kbase.newStatefulKnowledgeSession();
        new Thread(new Runnable() {
            public void run() {
                ksession.fireUntilHalt();
            }
        }).start();
    }

    public void execute(int repNr) {
        for (int key = offset; key < offset + eventNr; key++) {
            insertLetter(key);
            insertFigure(key);
        }
    }

    private void insertLetter(int key) {
        ksession.insert( new Letter( key, (char)('A' + ((key / 100) % 26)) ) );
    }

    private void insertFigure(int key) {
        ksession.insert( new Figure( key, 10000000 + (key % 10000000) ) );
    }

    @Override
    public void terminate(boolean isLast) {
        ksession.halt();
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }

    private int cloneCounter = 0;

    @Override
    public CepBenchmark clone() {
        CepBenchmark clone = new CepBenchmark(eventNr);
        clone.offset = eventNr * cloneCounter;
        cloneCounter++;
        return clone;
    }
}
