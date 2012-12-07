/*
 * Copyright 2010 JBoss Inc
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

/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.camel.component;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.naming.Context;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.drools.pipeline.camel.Person;
import org.junit.Test;
import org.kie.event.rule.ActivationCreatedEvent;
import org.kie.event.rule.AgendaEventListener;
import org.kie.event.rule.MatchCreatedEvent;
import org.kie.event.rule.ObjectInsertedEvent;
import org.kie.event.rule.WorkingMemoryEventListener;
import org.kie.runtime.StatefulKnowledgeSession;
import org.mockito.ArgumentCaptor;

public class CamelEndpointActionInsertTest extends DroolsCamelTestSupport {
    private StatefulKnowledgeSession ksession;
    private AgendaEventListener ael;
    private WorkingMemoryEventListener wmel;

    @Test
    public void testSessionInsert() throws Exception {
        Person person = new Person();
        person.setName( "Bob" );

        template.sendBody( "direct:test-no-ep", person );
        
        ArgumentCaptor<ObjectInsertedEvent> oie = ArgumentCaptor.forClass( ObjectInsertedEvent.class );
        ArgumentCaptor<MatchCreatedEvent> ace = ArgumentCaptor.forClass( MatchCreatedEvent.class );
        
        verify( wmel ).objectInserted( oie.capture() );
        assertThat( (Person) oie.getValue().getObject(), is( person ) );
        
        verify( ael ).activationCreated( ace.capture() );
        assertThat( ace.getValue().getMatch().getRule().getName(), is("rule1") );
    }

    @Test
    public void testSessionInsertEntryPoint() throws Exception {
        Person person = new Person();
        person.setName( "Bob" );

        template.sendBody( "direct:test-with-ep", person );
        
        ArgumentCaptor<ObjectInsertedEvent> oie = ArgumentCaptor.forClass( ObjectInsertedEvent.class );
        ArgumentCaptor<MatchCreatedEvent> ace = ArgumentCaptor.forClass( MatchCreatedEvent.class );
        
        verify( wmel ).objectInserted( oie.capture() );
        assertThat( (Person) oie.getValue().getObject(), is( person ) );
        
        verify( ael ).activationCreated( ace.capture() );
        assertThat( ace.getValue().getMatch().getRule().getName(), is("rule2") );
    }

    @Test
    public void testSessionInsertMessage() throws Exception {
        Person person = new Person();
        person.setName( "Bob" );

        template.sendBody( "direct:test-message", person );
        
        ArgumentCaptor<ObjectInsertedEvent> oie = ArgumentCaptor.forClass( ObjectInsertedEvent.class );
        ArgumentCaptor<MatchCreatedEvent> ace = ArgumentCaptor.forClass( MatchCreatedEvent.class );
        
        verify( wmel ).objectInserted( oie.capture() );
        assertThat( (Person) ((Message) oie.getValue().getObject()).getBody(), is( person ) );
        
        verify( ael ).activationCreated( ace.capture() );
        assertThat( ace.getValue().getMatch().getRule().getName(), is("rule3") );
    }

    @Test
    public void testSessionInsertExchange() throws Exception {
        Person person = new Person();
        person.setName( "Bob" );

        template.sendBody( "direct:test-exchange", person );
        
        ArgumentCaptor<ObjectInsertedEvent> oie = ArgumentCaptor.forClass( ObjectInsertedEvent.class );
        ArgumentCaptor<MatchCreatedEvent> ace = ArgumentCaptor.forClass( MatchCreatedEvent.class );
        
        verify( wmel ).objectInserted( oie.capture() );
        assertThat( (Person) ((Exchange) oie.getValue().getObject()).getIn().getBody(), is( person ) );
        
        verify( ael ).activationCreated( ace.capture() );
        assertThat( ace.getValue().getMatch().getRule().getName(), is("rule4") );
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from( "direct:test-no-ep" ).to( "drools://node/ksession1?action=insertBody" );
                from( "direct:test-with-ep" ).to( "drools://node/ksession1?action=insertBody&entryPoint=ep1" );
                from( "direct:test-message" ).to( "drools://node/ksession1?action=insertMessage" );
                from( "direct:test-exchange" ).to( "drools://node/ksession1?action=insertExchange" );
            }
        };
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        String rule = "";
        rule += "package org.drools.pipeline.camel \n";
        rule += "import org.apache.camel.Exchange \n";
        rule += "import org.apache.camel.Message \n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person() \n";
        rule += "  then \n";
        rule += "    # no-op \n";
        rule += "end\n";
        rule += "rule rule2 \n";
        rule += "  when \n";
        rule += "    $p : Person() from entry-point ep1 \n";
        rule += "  then \n";
        rule += "    # no-op \n";
        rule += "end\n";
        rule += "rule rule3 \n";
        rule += "  when \n";
        rule += "    $m : Message() \n";
        rule += "  then \n";
        rule += "    # no-op \n";
        rule += "end\n";
        rule += "rule rule4 \n";
        rule += "  when \n";
        rule += "    $e : Exchange() \n";
        rule += "  then \n";
        rule += "    # no-op \n";
        rule += "end\n";

        ksession = registerKnowledgeRuntime( "ksession1",
                                             rule );
        ael = mock( AgendaEventListener.class );
        wmel = mock( WorkingMemoryEventListener.class );
        ksession.addEventListener( ael );
        ksession.addEventListener( wmel );
    }
}
