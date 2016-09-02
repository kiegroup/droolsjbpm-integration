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

import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;

/**
 * These tests aims at verifying if RemoteClient can run on Karaf.
 * By default they are disabled (ignored) as they require Workbench to be up and running so client can connect to it.
 * To be able to use it following constants must be given (either directly or as system properties)
 * - SERVER_URL (-Dorg.kie.workbench.itest.server.url)
 * - USER (-Dorg.kie.workbench.itest.user)
 * - PASSWORD (-Dorg.kie.workbench.itest.password)
 * - DEPLOYMENT_ID (-Dorg.kie.workbench.itest.deploymentid)
 * - PROCESS_ID (-Dorg.kie.workbench.itest.process)
 *
 * Tests are very basic and focus only on verification rather than complete test suite.
 */

public class BaseRemoteClientKarafIntegrationTest extends AbstractKarafIntegrationTest {

    protected String serverUrl = System.getProperty("org.kie.workbench.itest.server.url", "http://localhost:8080/jbpm-console");
    protected String user = System.getProperty("org.kie.workbench.itest.user", "krisv");
    protected String password = System.getProperty("org.kie.workbench.itest.password", "krisv");
    protected String deploymentId = System.getProperty("org.kie.workbench.itest.deploymentid", "org.jbpm:Evaluation:1.0");
    protected String processId = System.getProperty("org.kie.workbench.itest.process", "evaluation");

    @Configuration
    public static Option[] configure() {

        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
//                  debugConfiguration("5005", true),

                // Load kie-server-client
                loadKieFeatures("kie-server-client")
        };
    }


}