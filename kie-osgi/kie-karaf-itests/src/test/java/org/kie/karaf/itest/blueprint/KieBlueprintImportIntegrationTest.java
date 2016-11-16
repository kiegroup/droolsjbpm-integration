package org.kie.karaf.itest.blueprint;

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

import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.blueprint.domain.Customer;
import org.kie.karaf.itest.blueprint.domain.Drink;
import org.kie.karaf.itest.blueprint.domain.Order;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieSession;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Constants;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieBlueprintImportIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String BLUEPRINT_XML_LOCATION = "/org/kie/karaf/itest/blueprint/kie-scanner-import-blueprint.xml";

    @Inject
    KieSession kieSession;

    @Inject
    KieBase kieBase;

    @Inject
    KieScanner kieScanner;

    @Test
    public void kieElementsExistTest() {
        Assert.assertNotNull(kieSession);
        Assert.assertNotNull(kieBase);
        Assert.assertNotNull(kieScanner);
    }

    @Test
    public void kieSessionOldPersonTest() {
        Assert.assertNotNull(kieSession);

        Drink drink = new Drink("whiskey", true);
        Customer customer = new Customer("Customer", 40);
        Order order = new Order(customer, drink);

        kieSession.insert(order);
        kieSession.fireAllRules();

        Assert.assertTrue(order.isApproved());
    }

    @Test
    public void kieSessionYoungPersonTest() {
        Assert.assertNotNull(kieSession);

        Drink drink = new Drink("whiskey", true);
        Customer customer = new Customer("Customer", 14);
        Order order = new Order(customer, drink);

        kieSession.insert(order);
        kieSession.fireAllRules();

        Assert.assertFalse(order.isApproved());
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
                // debugConfiguration("5005", true),

                // Load Kie-Aries-Blueprint
                loadKieFeatures("drools-module", "kie-ci", "kie-aries-blueprint"),

                // wrap and install junit bundle - the DRL imports a class from it
                // (simulates for instance a bundle with domain classes used in rules)
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject()),

                // Load domain model classes
                wrappedBundle(mavenBundle().groupId("org.kie").artifactId("kie-karaf-itests-domain-model").versionAsInProject()),

                // Create a bundle with META-INF/spring/kie-beans.xml - this should be processed automatically by Spring
                streamBundle(bundle()
                        .add("OSGI-INF/blueprint/kie-scanner-import-blueprint.xml",
                                KieBlueprintDependencyKarafIntegrationTest.class.getResource(BLUEPRINT_XML_LOCATION))
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Blueprint-Bundle")
                        .set(Constants.IMPORT_PACKAGE, "org.kie.aries.blueprint," +
                                "org.osgi.service.blueprint.container," +
                                "org.kie.karaf.itest.blueprint.domain," +
                                "org.kie.aries.blueprint.factorybeans," +
                                "org.kie.aries.blueprint.helpers," +
                                "org.kie.api," +
                                "org.kie.api.runtime," +
                                "org.kie.api.builder," +
                                // junit is acting as a dependency for the rule
                                "org.junit," +
                                "*")
                        .set(Constants.EXPORT_PACKAGE, "org.kie.karaf.itest.blueprint.domain")
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Test-Blueprint-Bundle")
                        .build()).start()

        };
    }
}
