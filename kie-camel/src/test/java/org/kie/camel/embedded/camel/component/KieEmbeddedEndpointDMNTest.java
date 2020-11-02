/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.embedded.camel.component;

import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ClaimCheckOperation;
import org.drools.compiler.kie.builder.impl.DrlProject;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieSession;
import org.kie.camel.embedded.dmn.DecisionsToHeadersProcessor;
import org.kie.camel.embedded.dmn.ToDMNEvaluateAllCommandProcessor;
import org.kie.camel.embedded.dmn.ToMapProcessor;
import org.kie.pipeline.camel.Person;

public class KieEmbeddedEndpointDMNTest extends KieCamelTestSupport {

    @Test
    public void testDMN() throws Exception {
        Person johnDoe = new Person("John Doe", 47);
        Person alice = new Person("Alice", 18);
        Person bob = new Person("Bob", 17);
        Person charlie = new Person("Charlie", 21);

        MockEndpoint canDrink = getMockEndpoint("mock:canDrink");
        canDrink.expectedMessageCount(3);
        canDrink.expectedBodiesReceived(johnDoe, alice, charlie);

        MockEndpoint noDrink = getMockEndpoint("mock:noDrink");
        noDrink.expectedMessageCount(1);
        noDrink.expectedBodiesReceived(bob);

        template.requestBody("direct:start", johnDoe);
        template.requestBody("direct:start", alice);
        template.requestBody("direct:start", bob);
        template.requestBody("direct:start", charlie);

        canDrink.assertIsSatisfied();
        noDrink.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        final Processor toMap = new ToMapProcessor("a person");
        final Processor toDMNCommand = new ToDMNEvaluateAllCommandProcessor("https://kiegroup.org/dmn/_FD3D17D0-D23E-457E-B41A-380644F030A8",
                                                                            "Can Drink?",
                                                                            "dmnResult");
        final Processor dmnToHeader = DecisionsToHeadersProcessor.builder("dmnResult",
                                                                          "canDrinkHeader",
                                                                          "Can Drink?")
                                                                 .build();

        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:start").claimCheck(ClaimCheckOperation.Push)
                                    .process(toMap)
                                    .process(toDMNCommand)
                                    .to("kie-local://ksession1?channel=default")
                                    .process(dmnToHeader)
                                    .claimCheck(ClaimCheckOperation.Pop)
                                    .to("log:org.kie.test?level=DEBUG&showAll=true&multiline=true")
                                    .choice()
                                    .when(header("canDrinkHeader").isEqualTo(true))
                                        .to("mock:canDrink")
                                    .otherwise()
                                        .to("mock:noDrink");
            }
        };
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        kfs.write("src/main/resources/canDrink.dmn", kieResources.newClassPathResource("/dmn/canDrink.dmn", this.getClass()));

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll(DrlProject.class);

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        try {
            jndiContext.bind("ksession1", ksession);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

    }
}
