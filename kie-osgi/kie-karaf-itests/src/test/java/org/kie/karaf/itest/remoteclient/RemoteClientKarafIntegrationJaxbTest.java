/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest.remoteclient;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.util.PaxExamWithWireRestClientMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExamWithWireRestClientMock.class)
@ExamReactorStrategy(PerMethod.class)
public class RemoteClientKarafIntegrationJaxbTest extends BaseRemoteClientKarafIntegrationTest {

    public static final String HOST = "localhost";
    public static final int PORT = 59400;

    public RemoteClientKarafIntegrationJaxbTest() {
        serverUrl = System.getProperty("org.kie.workbench.itest.server.url", "http://" + HOST + ":" + PORT + "/jbpm-console");
    }

    @Test
    public void testProcess() throws Exception {
        RuntimeEngine engine = RemoteRuntimeEngineFactory.newRestBuilder()
            .addUrl(new URL(serverUrl))
            .addUserName(user).addPassword(password)
            .addDeploymentId(deploymentId)
                .build();
        KieSession ksession = engine.getKieSession();
        
        // start a new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("employee", "krisv");
        params.put("reason", "Yearly performance evaluation");
        ProcessInstance processInstance = 
            ksession.startProcess("evaluation", params);
        System.out.println("Start Evaluation process " + processInstance.getId());
    }

    @Configuration
    public static Option[] configure() {

        return new Option[]{
                // Install Karaf Container
                AbstractKarafIntegrationTest.getKarafDistributionOption(),

                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
//                  debugConfiguration("5005", true),

                // Load kie-remote-client
                AbstractKarafIntegrationTest.loadKieFeatures("kie-remote-client")
        };
    }




}