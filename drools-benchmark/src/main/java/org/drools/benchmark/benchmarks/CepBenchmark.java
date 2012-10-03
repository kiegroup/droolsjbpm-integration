package org.drools.benchmark.benchmarks;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.cep.Figure;
import org.drools.benchmark.model.cep.Letter;
import org.drools.builder.KnowledgeBuilder;
import org.drools.conf.EventProcessingOption;
import org.drools.runtime.StatefulKnowledgeSession;

public class CepBenchmark extends AbstractBenchmark {

    private final int eventNr;

    private StatefulKnowledgeSession ksession;

    public CepBenchmark(int eventNr) {
        this.eventNr = eventNr;
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KnowledgeBuilder kbuilder = createKnowledgeBuilder("cep.drl");

        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption( EventProcessingOption.STREAM );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(config);
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        ksession = kbase.newStatefulKnowledgeSession();

        new Thread(new Runnable() {
            public void run() {
                ksession.fireUntilHalt();
            }
        }).start();
    }

    public void execute(int repNr) {
        for (int key = 0; key < eventNr; key++) {
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
    public void terminate() {
        ksession.halt();
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
