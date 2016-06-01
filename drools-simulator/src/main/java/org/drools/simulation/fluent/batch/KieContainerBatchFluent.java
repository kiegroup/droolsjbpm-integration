package org.drools.simulation.fluent.batch;


public interface KieContainerBatchFluent {

    KieSessionBatchFluent newSession();
    KieSessionBatchFluent newSession(String id);
    //KieSessionBatchFluent<BatchFluentBuilder> getKieSession();
    //KieSessionBatchFluent<BatchFluentBuilder> getKieSession(String id);
}
