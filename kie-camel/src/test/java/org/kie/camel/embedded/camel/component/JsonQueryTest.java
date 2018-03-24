/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.command.Command;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.camel.embedded.camel.testdomain.Person;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.internal.command.CommandFactory;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/** Camel - JSON reproducer
 * https://bugzilla.redhat.com/show_bug.cgi?id=771193 */
public class JsonQueryTest {

    private ProducerTemplate template;

    /** configures camel-drools integration and defines 3 routes:
     * 1) testing route (connection to drools with JSON command format)
     * 2) unmarshalling route (for unmarshalling command results)
     * 3) marshalling route (enables creating commands through API and converting to JSON) */
    private CamelContext configure(KieSession session) throws Exception {
        Context context = new JndiContext();
        context.bind("ksession", session);

        CamelContext camelContext = new DefaultCamelContext(context);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-session").policy(new KiePolicy()).unmarshal("json").to("kie-local://ksession").marshal("json");
                from("direct:unmarshall").policy(new KiePolicy()).unmarshal("json");
                from("direct:marshall").policy(new KiePolicy()).marshal("json");
            }

        });

        return camelContext;
    }

    /** camel context startup and template creation */
    private void initializeTemplate(KieSession session) throws Exception {
        CamelContext context = configure(session);
        this.template = context.createProducerTemplate();
        context.start();
    }

    /** create empty knowledge base */
    private KieSession getKieSession() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        kfs.write(kieResources.newClassPathResource("getOlder.drl", getClass()).setResourceType(ResourceType.DRL));

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
    }

    /** insert 2 facts into session, then launch query command with one argument */
    @Test
    public void testQuery() throws Exception {
        KieSession session = getKieSession();

        initializeTemplate(session);

        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("john", "john", 25));
        persons.add(new Person("sarah", "john", 35));

        session.execute(CommandFactory.newInsertElements(persons));
        assertEquals(2, session.getFactCount());

        QueryResults results = query("people over the age of x", new Object[] {30});
        assertNotNull(results);
    }

    /** build json query command and send it to drools */
    private QueryResults query(String queryName, Object[] args) {
        Command command = CommandFactory.newQuery("persons", queryName, args);
        String queryStr = template.requestBody("direct:marshall", command, String.class);

        String json = template.requestBody("direct:test-session", queryStr, String.class);
        ExecutionResults res = (ExecutionResults)template.requestBody("direct:unmarshall", json);
        return (QueryResults)res.getValue("persons");
    }

}
