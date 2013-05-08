package org.kie.services.client.api.fluent.api; 



public interface RemoteRuntimeEngineFluent {

    RemoteKieSessionFluent getKieSession();
    RemoteTaskFluent getTaskService();   
    
}
