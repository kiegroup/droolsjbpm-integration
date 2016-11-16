/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.karaf.itest.blueprint;

import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieBlueprintProcessDependencyKarafIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String BLUEPRINT_XML_LOCATION = "/org/kie/karaf/itest/blueprint/kie-beans-blueprint-process-dep.xml";
    private static final String DRL_LOCATION = "/blueprint_process_dep/sampleRule.drl";
    private static final String BPMN_LOCATION = "/blueprint_process_dep/sampleProcess.bpmn2";

    private static final String PROCESS_ID = "orderApproval";

    @Inject
    private KieSession kieSession;

    @Test
    public void testKieBase() throws Exception {
        assertNotNull(kieSession);
        assertTrue("KieBase contains no packages?", kieSession.getKieBase().getKiePackages().size() > 0);
    }

    @Test
    public void testStartProcess() throws Exception {
        assertNotNull(kieSession);
        final ProcessInstance processInstance = kieSession.startProcess(PROCESS_ID);
        assertEquals("Unexpected process instance state.", ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

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
                //  debugConfiguration("5005", true),

                // Load KIE features
                loadKieFeatures("jbpm", "kie-aries-blueprint"),

                // wrap and install junit bundle - the DRL imports a class from it
                // (simulates for instance a bundle with domain classes used in rules)
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject()),

                // Create a bundle with META-INF/spring/kie-beans.xml - this should be processed automatically by Spring
                streamBundle(bundle()
                        .add("OSGI-INF/blueprint/kie-beans-blueprint-process-dep.xml",
                                KieBlueprintProcessDependencyKarafIntegrationTest.class.getResource(BLUEPRINT_XML_LOCATION))
                        .add("blueprint_process_dep/sampleRule.drl",
                                KieBlueprintProcessDependencyKarafIntegrationTest.class.getResource(DRL_LOCATION))
                        .add("blueprint_process_dep/sampleProcess.bpmn2",
                                KieBlueprintProcessDependencyKarafIntegrationTest.class.getResource(BPMN_LOCATION))
                        .set(Constants.IMPORT_PACKAGE, "org.kie.aries.blueprint," +
                                                       "org.kie.aries.blueprint.factorybeans," +
                                                       "org.kie.aries.blueprint.helpers," +
                                                       "org.kie.api," +
                                                       "org.kie.api.runtime," +
                                                       // junit is acting as a dependency for the rule
                                                       "org.junit," +
                                                       "*")
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Blueprint-Bundle")
                        // alternative for enumerating org.kie.aries.blueprint packages in Import-Package:
                        //.set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                        .build()).start()

        };
    }
}