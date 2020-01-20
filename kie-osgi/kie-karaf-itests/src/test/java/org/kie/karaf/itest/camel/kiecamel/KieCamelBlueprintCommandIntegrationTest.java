/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest.camel.kiecamel;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.component.bean.PojoProxyHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.ExecutionResults;
import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.camel.kiecamel.model.Person;
import org.kie.karaf.itest.camel.kiecamel.proxy.CommandExecutionService;
import org.kie.karaf.itest.camel.kiecamel.tools.PersonFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

/**
 * Basic KIE-Camel with Blueprint KIE Command execution tests running in Fuse.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KieCamelBlueprintCommandIntegrationTest extends AbstractKarafIntegrationTest {

    @Inject
    private CamelContext camelContext;

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
                // debugConfiguration("5005", true),

                // Load Kie-Aries-Blueprint
                loadKieFeatures("drools-module", "drools-decisiontable", "kie-ci", "kie-aries-blueprint", "kie-camel"),

                // wrap and install junit bundle - the DRL imports a class from it
                // (simulates for instance a bundle with domain classes used in rules)
                wrappedBundle(mavenBundle().groupId("junit").artifactId("junit").versionAsInProject())
        };
    }

    @Test(timeout = 60000)
    public void testOldPerson() throws Exception {
        final Person person = PersonFactory.createOldPerson();
        Assert.assertFalse(person.isCanDrink());

        final Person verifiedPerson = executeCommand(person);

        Assert.assertNotNull(verifiedPerson);
        Assert.assertTrue(verifiedPerson.isCanDrink());
    }

    @Test(timeout = 60000)
    public void testYoungPerson() throws Exception {
        final Person person = PersonFactory.createYoungPerson();
        Assert.assertFalse(person.isCanDrink());

        final Person verifiedPerson = executeCommand(person);

        Assert.assertNotNull(verifiedPerson);
        Assert.assertFalse(verifiedPerson.isCanDrink());
    }

    private Person executeCommand(final Person person) throws Exception {
        final CommandExecutionService service = getCommandExecutionProxy();
        final ExecutionResults results = service.executeCommandWithPerson(person);

        Assert.assertNotNull(results);
        return (Person) results.getValue("person");
    }

    private CommandExecutionService getCommandExecutionProxy() throws Exception {
        // need to use PojoProxyHelper to avoid sending BeanInvocation object as payload
        return PojoProxyHelper.createProxy(camelContext.getEndpoint("direct://ruleOnCommand"),
                CommandExecutionService.class);
    }
}
