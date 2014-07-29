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
import org.apache.karaf.tooling.exam.options.LogLevelOption;
import org.drools.camel.example.Person;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.io.File;
import java.util.Collection;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.*;
import static org.drools.osgi.spring.OsgiApplicationContextFactory.getOsgiSpringContext;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;

@RunWith(JUnit4TestRunner.class)
@Ignore("This test succeed in isolation but produces a false negative failure when run in the test suite. FIXME")
public class DroolsOnCommandCamelKarafTest extends OSGiIntegrationSpringTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(DroolsOnCommandCamelKarafTest.class);

    // templates to send to input endpoints
    @Produce(ref = "ruleOnCommandEndpoint")
    protected ProducerTemplate ruleOnCommandEndpoint;

    @Override
    protected OsgiBundleXmlApplicationContext createApplicationContext() {
        return getOsgiSpringContext(new ReleaseIdImpl("dummyGroup", "dummyArtifact", "dummyVersion"),
                                    DroolsOnCommandCamelKarafTest.class.getResource("/org/drools/karaf/itest/camel-context.xml"));
    }

    @Test
    public void testRuleOnCommand() throws Exception {
        Person person = new Person();
        person.setName("Young Scott");
        person.setAge(18);

        ExecutionResultImpl response = ruleOnCommandEndpoint.requestBody(person, ExecutionResultImpl.class);

        assertNotNull(response);

        // Expecting single result value of type Person
        Collection<String> identifiers = response.getIdentifiers();
        assertNotNull(identifiers);
        assertTrue(identifiers.size() >= 1);

        for (String identifier : identifiers) {
            final Object value = response.getValue(identifier);
            assertNotNull(value);
            assertIsInstanceOf(Person.class, value);
            assertFalse(((Person) value).isCanDrink());
            System.out.println(identifier + " = " + value);
        }

        // Test for alternative result

        person.setName("Scott");
        person.setAge(21);

        response = ruleOnCommandEndpoint.requestBody(person, ExecutionResultImpl.class);

        assertNotNull(response);

        // Expecting single result value of type Person
        identifiers = response.getIdentifiers();
        assertNotNull(identifiers);
        assertTrue(identifiers.size() >= 1);

        for (String identifier : identifiers) {
            final Object value = response.getValue(identifier);
            assertNotNull(value);
            assertIsInstanceOf(Person.class, value);
            assertTrue(((Person) value).isCanDrink());
            System.out.println(identifier + " = " + value);
        }
    }

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                karafDistributionConfiguration().frameworkUrl(
                        maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").versionAsInProject())
                        //This version doesn't affect the version of karaf we use
                        .karafVersion(MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")).name("Apache Karaf")
                        .unpackDirectory(new File("target/exam/unpack/")),

                keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.INFO),

                // Load Spring DM Karaf Feature
                scanFeatures(
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
