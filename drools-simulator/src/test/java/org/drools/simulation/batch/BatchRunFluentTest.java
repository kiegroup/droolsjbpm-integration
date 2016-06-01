package org.drools.simulation.batch;

import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.ContextBatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
import org.drools.simulation.fluent.batch.impl.ContextBatchBuilderFluentImpl;
import org.drools.simulation.impl.Person;
import org.kie.api.builder.ReleaseId;

public class BatchRunFluentTest {

    public void test1() {
        //BatchRunImpl batchRun = new BatchRunImpl();

        ContextBatchBuilderFluent fr =  new ContextBatchBuilderFluentImpl();

        BatchBuilderFluent f = fr.newBatch();

        f.newContext("xxx");


        ReleaseId releaseId = null;

        f.newSession(releaseId).insert(null).end().getKieContainer(null);
        f.after(100).getKieContainer(null).newSession().after(100).end().getKieContainer(null).newSession();

        f.getContext("ctx1").getKieContainer(releaseId).newSession().set("s1").insert( new Person() ).out("p1");

        f.getKieContainer(releaseId).newSession().end();

//        KieSessionBatchFluent ksf = fr.get((KieSessionBatchFluent<BatchFluentBuilder>) KieSessionBatchFluent.class, "kkk").insert(1);
//        ksf.end();

        //ksf.insert()


        //f.newBatchBuilderFluent("x1").


    }
}
