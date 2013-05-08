package org.kie.services.client;

/**
 * RuntimeEngine runtimeEngine = SomeClientServiceClassOrInstance.getRuntimeEngine(possible config params here);
 * KieSession ksession = runtimeEngine.getKieSession()
 * ksession.startProcess(..);
 * SomeClientServiceClassOrInstance.send(ksession);
 */

public class UnfinishedTestException extends RuntimeException { 
    public UnfinishedTestException(String msg) { 
        super(msg);
    }
}