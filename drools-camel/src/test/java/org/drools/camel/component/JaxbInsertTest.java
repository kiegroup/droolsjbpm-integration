/*
 * Copyright 2012 JBoss Inc
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
package org.drools.camel.component;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.util.jndi.JndiContext;
import org.drools.camel.testdomain.Person;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.command.BatchExecutionCommand;
import org.kie.command.Command;
import org.kie.command.CommandFactory;
import org.kie.runtime.ExecutionResults;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

/**
 * Camel - JAXB reproducer test
 * https://bugzilla.redhat.com/show_bug.cgi?id=771203
 * https://bugzilla.redhat.com/show_bug.cgi?id=771209
 */
public class JaxbInsertTest {

    private StatefulKnowledgeSession ksession;
    private ProducerTemplate template;

    @Before
    public void setUp() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        ksession = kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
        initializeTemplate(ksession);
    }

    /**
     * camel context startup and template creation
     */
    private void initializeTemplate(StatefulKnowledgeSession session) throws Exception {
        CamelContext context = configure(session);
        this.template = context.createProducerTemplate();
        context.start();
    }
    
    /**
     * configures camel-drools integration and defines 3 routes:
     * 1) testing route (connection to drools with JAXB command format)
     * 2) unmarshalling route (for unmarshalling command results)
     * 3) marshalling route (enables creating commands through API and converting to XML)
     */
    private CamelContext configure(StatefulKnowledgeSession session) throws Exception {
        GridImpl grid = new GridImpl(new HashMap<String, Object>());        
        GridNode node = grid.createGridNode("testnode");

        Context context = new JndiContext();
        context.bind("testnode", node);
        node.set("ksession", session);

        CamelContext camelContext = new DefaultCamelContext(context);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {          
                JaxbDataFormat jdf = new JaxbDataFormat();
                jdf.setContextPath("org.drools.camel.testdomain");
                jdf.setPrettyPrint(true);

                from("direct:test-session").policy(new DroolsPolicy())
                        .unmarshal(jdf)
                        .to("drools://testnode/ksession")
                        .marshal(jdf);
                from("direct:unmarshall").policy(new DroolsPolicy())
                        .unmarshal(jdf);
                from("direct:marshall").policy(new DroolsPolicy())
                        .marshal(jdf);
            }
        });
        
        return camelContext;
    }
    
    /**
     * bz771203
     * creates batch-execution command with insert. Marshalls it to XML 
     * and send to drools
     */
    @Test
    public void testInsert() throws Exception {
        Person p = new Person("Alice", "spicy meals", 30);
        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(p, "tempPerson"));
        BatchExecutionCommand command = CommandFactory.newBatchExecution(commands);
        String xmlCommand = template.requestBody("direct:marshall", command, String.class);

        String xml = template.requestBody("direct:test-session", xmlCommand, String.class);
        ExecutionResults res = (ExecutionResults) template.requestBody("direct:unmarshall", xml);

        Object o = res.getFactHandle("tempPerson");
        assertTrue("returned String instead of FactHandle instance", o instanceof FactHandle);       
    }
    
    @Test
    public void testInsertElements() {
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("John", "nobody", 50));
        persons.add(new Person("Peter", "himself", 24));
        
        insertElements(persons);
        assertEquals(2, ksession.getFactCount());
    }
    
    private void insertElements(List<Person> objects) {
        String insertElements = 
            "<batch-execution>\n"
            + "  <insert-elements return-objects=\"true\">\n"
            + "    <list>\n";
        for(Person p : objects) {
            insertElements += "      <element xsi:type=\"person\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + "        <age>" + p.getAge() + "</age>\n"
                + "        <likes>" + p.getLikes() + "</likes>\n"
                + "        <name>" + p.getName() + "</name>\n"
                + "      </element>\n";
        }
        insertElements +=
            "    </list>\n"
            + "  </insert-elements>\n"
            + "</batch-execution>";
        template.requestBody("direct:test-session", insertElements, String.class);
    }

}
