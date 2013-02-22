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
import org.apache.karaf.tooling.exam.options.LogLevelOption;
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

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.CoreOptions.maven;

@RunWith(JUnit4TestRunner.class)
public class DroolsOnBodyCamelKarafTest extends OSGiIntegrationSpringTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(DroolsOnBodyCamelKarafTest.class);

    // templates to send to input endpoints
    @Produce(ref = "ruleOnBodyEndpoint")
    protected ProducerTemplate ruleOnBodyEndpoint;

    @Override
    protected OsgiBundleXmlApplicationContext createApplicationContext() {
        return new OsgiBundleXmlApplicationContext(new String[]{"org/drools/karaf/itest/camel-context.xml"});
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
        return new Option[]{
                karafDistributionConfiguration().frameworkUrl(
                        maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").versionAsInProject())
                        //This version doesn't affect the version of karaf we use
                        .karafVersion(MavenUtils.getArtifactVersion("org.apache.karaf", "apache-karaf")).name("Apache Karaf")
                        .unpackDirectory(new File("target/exam/unpack/")),

                keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.INFO),

                // Load camel-core, camel-sprig, camel-test & camel-cxf Features
                loadCamelFeatures("camel-cxf"),

                // Load drools-module (= core + compiler + knowledge), drools-camel & drools-spring
                loadDroolsFeatures("drools-camel","drools-spring")

        };

    }


}