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

package org.kie.karaf.itest;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Constants;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SimpleKieSpringKarafIntegrationTest extends AbstractKieSpringKarafIntegrationTest {

    private static final String SPRING_XML_LOCATION = "/org/kie/karaf/itest/kie-beans.xml";
    private static final String SPRING_APPLICATION_CONTEXT_ID = "." + ApplicationContext.class.getName();

    // this is the way to get actual Spring Application Context through "bridging" Blueprint Container
    @Inject
    @Filter("(osgi.blueprint.container.symbolicname=Test-Kie-Spring-Bundle)")
    private BlueprintContainer container;

    @Before
    public void init() {
        applicationContext = (ConfigurableApplicationContext) container.getComponentInstance(SPRING_APPLICATION_CONTEXT_ID);
        assertNotNull("Should have created a valid spring context", applicationContext);
    }

    @Test
    public void testKieBase() throws Exception {
        refresh();
        KieBase kbase = (KieBase) applicationContext.getBean("drl_kiesample");
        assertNotNull(kbase);
    }

    @Test
    public void testKieSession() throws Exception {
        refresh();
        StatelessKieSession ksession = (StatelessKieSession) applicationContext.getBean("ksession9");
        assertNotNull(ksession);
    }

    @Test
    public void testKieSessionDefaultType() throws Exception {
        refresh();
        Object obj = applicationContext.getBean("ksession99");
        assertNotNull(obj);
        assertTrue(obj instanceof KieSession);
    }


    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
                //  debugConfiguration("5005", true),

                // Load Kie-Spring
                loadKieFeatures("kie-spring"),
                features(getFeaturesUrl("org.apache.karaf.features", "spring-legacy", getKarafVersion()), "aries-blueprint-spring"),

                // Create a bundle with META-INF/spring/kie-beans.xml - this should be processed automatically by Spring
                streamBundle(bundle()
                        .set(Constants.BUNDLE_MANIFESTVERSION, "2")
                        .add("META-INF/spring/kie-beans.xml",
                                SimpleKieSpringKarafIntegrationTest.class.getResource(SPRING_XML_LOCATION))
                        .set(Constants.IMPORT_PACKAGE, "org.kie.osgi.spring," +
                                "org.kie.api," +
                                "org.kie.api.runtime," +
                                "*")
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Kie-Spring-Bundle")
                        .build()).start()
        };
    }

}