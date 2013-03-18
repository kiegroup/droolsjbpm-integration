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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.drools.camel.testdomain.Person;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.junit.Test;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.command.Command;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.io.ResourceType;
import org.kie.runtime.ExecutionResults;
import org.kie.runtime.rule.QueryResults;

/**
 * Camel - JSON reproducer
 * https://bugzilla.redhat.com/show_bug.cgi?id=771193
 */
public class JsonQueryTest {
    
    private ProducerTemplate template;       
    
    /**
     * configures camel-drools integration and defines 3 routes:
     * 1) testing route (connection to drools with JSON command format)
     * 2) unmarshalling route (for unmarshalling command results)
     * 3) marshalling route (enables creating commands through API and converting to JSON)
     */
    private CamelContext configure(StatefulKnowledgeSession session) throws Exception {
        GridImpl grid = new GridImpl(new HashMap());
        GridNode node = grid.createGridNode("testnode");
        Context context = new JndiContext();
        context.bind("testnode", node);
        node.set("ksession", session);
        
        CamelContext camelContext = new DefaultCamelContext(context);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-session").policy(new DroolsPolicy()).unmarshal("json").to("drools://testnode/ksession").marshal("json");
                from("direct:unmarshall").policy(new DroolsPolicy()).unmarshal("json");
                from("direct:marshall").policy(new DroolsPolicy()).marshal("json");
            }
 
        });
        
        return camelContext;
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
     * create empty knowledge base
     */
    private KnowledgeBase getKbase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("getOlder.drl", getClass()), ResourceType.DRL);
        return kbuilder.newKnowledgeBase();
    }
    
    /**
     * insert 2 facts into session, then launch query command with one argument
     */
    @Test
    public void testQuery() throws Exception {
        StatefulKnowledgeSession session = getKbase().newStatefulKnowledgeSession();
        
        initializeTemplate(session);
        
        List<Person> persons = new ArrayList<Person>();
        persons.add(new Person("john", "john", 25));
        persons.add(new Person("sarah", "john", 35));
        
        session.execute(CommandFactory.newInsertElements(persons));
        assertEquals(2, session.getFactCount());
        
        QueryResults results = query("people over the age of x", new Object[] {30});
        assertNotNull(results);
    }
    
    /**
     * build json query command and send it to drools
     */
    private QueryResults query(String queryName, Object[] args) {
        Command command = CommandFactory.newQuery("persons", queryName, args);
        String queryStr = template.requestBody("direct:marshall", command, String.class);

        String json = template.requestBody("direct:test-session", queryStr, String.class);
        ExecutionResults res = (ExecutionResults) template.requestBody("direct:unmarshall", json);
        return (QueryResults) res.getValue("persons");
    }

}
