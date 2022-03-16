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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.beans.AbstractProcessWithPersistenceBean;
import org.kie.karaf.itest.beans.ProcessWithPersistenceDirectBean;
import org.kie.karaf.itest.beans.ProcessWithPersistenceEnvBean;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

/**
 * Tests starting a jBPM process using RuntimeManager with persistence enabled in Blueprint environment.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieBlueprintjBPMPersistenceKarafIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String BLUEPRINT_XML_LOCATION = "/org/kie/karaf/itest/blueprint/processpersistence/kie-beans-blueprint-process-persistence.xml";
    private static final String PERSISTENCE_XML_LOCATION = "/org/kie/karaf/itest/blueprint/processpersistence/persistence.xml";
    private static final String DATASOURCE_XML_LOCATION = "/org/kie/karaf/itest/blueprint/processpersistence/datasource.xml";
    private static final String KMODULE_XML_LOCATION = "/org/kie/karaf/itest/blueprint/processpersistence/kmodule.xml";
    private static final String POM_PROPS_LOCATION = "/META-INF/maven/org.kie/kie-osgi/pom.properties";

    private static final String DRL_LOCATION = "/blueprint_process_persistence/sampleRule.drl";
    private static final String BPMN_LOCATION = "/blueprint_process_persistence/sampleProcess.bpmn2";

    private static final String PROCESS_ID = "orderApproval";

    @Inject
    @Filter(value = "(osgi.jndi.service.name=kiesession/sessionWithoutEnv)", timeout = 120000)
    private KieSession processWithPersistenceNoEnv;

    @Inject
    @Filter(value = "(osgi.jndi.service.name=kiesession/sessionWithEnv)", timeout = 120000)
    private KieSession processWithPersistenceEnv;

    @Test
    public void testStartProcessNoEnv() throws Exception {
        assertNotNull(processWithPersistenceNoEnv);
        final ProcessInstance processInstance = processWithPersistenceNoEnv.startProcess(PROCESS_ID);
        assertEquals("Unexpected process instance state.", ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    @Test
    public void testStartProcessEnv() throws Exception {
        assertNotNull(processWithPersistenceEnv);
        final ProcessInstance processInstance = processWithPersistenceEnv.startProcess(PROCESS_ID);
        assertEquals("Unexpected process instance state.", ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    @Configuration
    public static Option[] configure() {
        final String jdbcDriverPath = System.getProperty("jdbc.driver.path");
        final List<Option> configurationOptions = getDefaultOptions();
        if (jdbcDriverPath != null && !"".equals(jdbcDriverPath)) {
            configurationOptions.add(wrappedBundle("file://" + jdbcDriverPath));
        }
        return configurationOptions.toArray(new Option[]{});
    }

    private static List<Option> getDefaultOptions() {
        final List<Option> options = new ArrayList<>();
        // Install Karaf Container
        options.add(getKarafDistributionOption());

        // Don't bother with local console output as it just ends up cluttering the logs
        options.add(configureConsole().ignoreLocalConsole());
        // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
        options.add(logLevel(LogLevelOption.LogLevel.WARN));

        // Option to be used to do remote debugging
//                  options.add(debugConfiguration("5005", true));

        // Load KIE features
        options.add(loadKieFeatures("jndi", "transaction", "droolsjbpm-hibernate", "h2", "jbpm", "kie-aries-blueprint"));

        // Create Datasource for the test
        options.add(streamBundle(bundle()
                .set(Constants.BUNDLE_MANIFESTVERSION, "2")
                .add("OSGI-INF/blueprint/datasource.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(DATASOURCE_XML_LOCATION))
                .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Blueprint-Datasource-Bundle")
                .set(Constants.IMPORT_PACKAGE, "javax.transaction," +
                                               "javax.sql," +
                                               "org.apache.commons.dbcp2.managed," +
                                               "org.h2")
                .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                .build()).start());

        // Create a bundle with META-INF/blueprint/kie-beans-?.xml - this should be processed automatically by Blueprint
        options.add(streamBundle(bundle()
                .set(Constants.BUNDLE_MANIFESTVERSION, "2")
                .add("OSGI-INF/blueprint/kie-beans-blueprint-process-persistence.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(BLUEPRINT_XML_LOCATION))

                // add persistence resources
                .add("META-INF/persistence.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(PERSISTENCE_XML_LOCATION))
                .add("META-INF/JBPMorm.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource("/META-INF/JBPMorm.xml"))
                .add("META-INF/Taskorm.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource("/META-INF/Taskorm.xml"))
                .add("META-INF/TaskAuditorm.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource("/META-INF/TaskAuditorm.xml"))

                // add kmodule resources
                .add("blueprint_process_persistence/sampleRule.drl",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(DRL_LOCATION))
                .add("blueprint_process_persistence/sampleProcess.bpmn2",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(BPMN_LOCATION))
                .add("META-INF/kmodule.xml",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(KMODULE_XML_LOCATION))
                .add("META-INF/maven/kjar/pom.properties",
                        KieBlueprintjBPMPersistenceKarafIntegrationTest.class.getResource(POM_PROPS_LOCATION))

                // add helper beans
                .add(ProcessWithPersistenceDirectBean.class)
                .add(ProcessWithPersistenceEnvBean.class)
                .add(AbstractProcessWithPersistenceBean.class)

                .set("Meta-Persistence", "META-INF/persistence.xml")
                .set(Constants.IMPORT_PACKAGE, "org.kie.aries.blueprint," +
                                               "org.kie.aries.blueprint.factorybeans," +
                                               "org.kie.aries.blueprint.helpers," +
                                               "org.kie.api," +
                                               "org.kie.api.runtime," +
                                               "org.kie.api.runtime.manager," +
                                               "org.kie.api.runtime.process," +
                                               "org.kie.api.task," +
                                               "org.kie.api.task.model," +
                                               "org.jbpm.persistence.correlation," +
                                               "org.jbpm.persistence.processinstance," +
                                               "org.jbpm.runtime.manager.impl," +
                                               "org.jbpm.runtime.manager.impl.jpa," +
                                               "org.jbpm.process.audit," +
                                               "org.jbpm.process.instance.impl," +
                                               "org.jbpm.services.task.audit.impl.model," +
                                               "org.jbpm.services.task.identity," +
                                               "org.jbpm.services.task.impl.model," +
                                               "org.jbpm.services.task.query," +
                                               "org.kie.internal.task.api," +
                                               "org.kie.internal.task.api.model," +
                                               "org.kie.internal.runtime.manager.context," +
                                               "org.drools.persistence.info," +
                                               "javax.transaction," +
                                               "javax.persistence")
                .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Blueprint-Bundle")
                .build()).start());

        return options;
    }
}