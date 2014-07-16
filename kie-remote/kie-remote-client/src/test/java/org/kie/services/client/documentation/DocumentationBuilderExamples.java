package org.kie.services.client.documentation;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.kie.services.client.api.builder.RemoteRestRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteRuntimeEngine;

public class DocumentationBuilderExamples {

    public void minimumRestBuilderExample() throws Exception {

        RuntimeEngine runtimeEngine = RemoteRuntimeEngineFactory.newRestBuilder()
                .addUserName("user")
                .addPassword("pass")
                .addUrl(new URL("http://localhost:8080/business-central"))
                .build();

        runtimeEngine.getTaskService().claim(23l, "user");

        // This will throw a MissingRequiredInfoException because the deployment id is required here
        runtimeEngine.getKieSession().startProcess("org.test.process");
    }

    private static final String KRIS_USER = "kris";
    private static final String KRIS_PASSWORD = "kris123@";
    private static final String MARY_USER = "mary";
    private static final String MARY_PASSWORD = "mary123@";
    private static final String JOHN_USER = "john";
    private static final String JOHN_PASSWORD = "john123@";

    public void multipleDifferentRuntimeExamples(String deploymentId, URL deploymentUrl, boolean useFormBasedAuth) throws Exception {
        RemoteRestRuntimeEngineBuilder runtimeEngineBuilder = RemoteRestRuntimeEngineFactory.newBuilder()
                .addDeploymentId(deploymentId)
                .addUrl(deploymentUrl)
                .useFormBasedAuth(useFormBasedAuth);

        RemoteRuntimeEngine krisRemoteEngine = runtimeEngineBuilder
                .addUserName(KRIS_USER)
                .addPassword(KRIS_PASSWORD)
                .build();
        RemoteRuntimeEngine maryRemoteEngine = runtimeEngineBuilder
                .addUserName(MARY_USER)
                .addPassword(MARY_PASSWORD)
                .build();
        RemoteRuntimeEngine johnRemoteEngine = runtimeEngineBuilder
                .addUserName(JOHN_USER)
                .addPassword(JOHN_PASSWORD)
                .build();
    }
}
