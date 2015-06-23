/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.client.documentation;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;

//TODO: changed, add to documentation
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
        RemoteRestRuntimeEngineBuilder runtimeEngineBuilder = RemoteRuntimeEngineFactory.newRestBuilder()
                .addDeploymentId(deploymentId)
                .addUrl(deploymentUrl);

        RuntimeEngine krisRemoteEngine = runtimeEngineBuilder
                .addUserName(KRIS_USER)
                .addPassword(KRIS_PASSWORD)
                .build();
        RuntimeEngine maryRemoteEngine = runtimeEngineBuilder
                .addUserName(MARY_USER)
                .addPassword(MARY_PASSWORD)
                .build();
        RuntimeEngine johnRemoteEngine = runtimeEngineBuilder
                .addUserName(JOHN_USER)
                .addPassword(JOHN_PASSWORD)
                .build();
    }
   
    // TODO
    public void jmsBuilderExamples() { 
        
    }
}
