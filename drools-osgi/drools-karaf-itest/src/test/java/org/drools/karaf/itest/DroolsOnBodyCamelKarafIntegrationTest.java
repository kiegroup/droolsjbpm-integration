/*
 * Copyright 2012 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.drools.karaf.itest;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.drools.camel.example.Person;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import static org.drools.osgi.spring.OsgiApplicationContextFactory.getOsgiSpringContext;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
public class DroolsOnBodyCamelKarafIntegrationTest extends OSGiIntegrationSpringTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(DroolsOnBodyCamelKarafIntegrationTest.class);

    // templates to send to input endpoints
    @Produce(ref = "ruleOnBodyEndpoint")
    protected ProducerTemplate ruleOnBodyEndpoint;

    //@Override
    protected OsgiBundleXmlApplicationContext createApplicationContext() {
        return getOsgiSpringContext(new ReleaseIdImpl("dummyGroup", "dummyArtifact", "dummyVersion"),
                                    DroolsOnBodyCamelKarafIntegrationTest.class.getResource("/org/drools/karaf/itest/camel-context.xml"));
    }

    @Test
    public void testRuleOnBody() throws Exception {
        Person person = new Person();
        person.setName("Young Scott");
        person.setAge(18);

        Person response = ruleOnBodyEndpoint.requestBody(person, Person.class);

        assertNotNull(response);
        assertFalse(person.isCanDrink());

        // Test for alternative result
        person.setName("Scott");
        person.setAge(21);

        response = ruleOnBodyEndpoint.requestBody(person, Person.class);

        assertNotNull(response);
        assertTrue(person.isCanDrink());
    }

    @Configuration
    public static Option[] configure() {
        return new Option[] {
                getKarafDistributionOption(),

                keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.INFO),

                // Option to be used to do remote debugging
                //debugConfiguration("5005", true),

                // Load Spring DM Karaf Feature
                features(
                        maven().groupId("org.apache.karaf.assemblies.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(),
                        "spring", "spring-dm"
                        ),

                // Load camel-core, camel-spring, camel-test & camel-cxf Features
                loadCamelFeatures("camel-cxf"),

                // Load drools-module (= core + compiler + knowledge), kie-camel & kie-spring
                loadDroolsFeatures("kie-spring","kie-camel")
        };

    }

}