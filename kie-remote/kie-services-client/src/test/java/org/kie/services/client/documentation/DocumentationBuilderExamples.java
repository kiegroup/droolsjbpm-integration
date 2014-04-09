package org.kie.services.client.documentation;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;

public class DocumentationBuilderExamples {

    public void minimumRestBuilderExample() throws Exception { 
       
        RemoteRestRuntimeEngineFactory factory = 
                RemoteRestRuntimeEngineFactory.newBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();
        
        RuntimeEngine runtimeEngine = factory.newRuntimeEngine();
        runtimeEngine.getTaskService().claim(23l, "user");
        
        // This will throw a MissingRequiredInfoException because the deployment id is required here
        runtimeEngine.getKieSession().startProcess("org.test.process");
    }
}
