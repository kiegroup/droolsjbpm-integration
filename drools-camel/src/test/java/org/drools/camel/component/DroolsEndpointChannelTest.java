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

import javax.naming.Context;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.drools.pipeline.camel.Person;
import org.junit.Test;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class DroolsEndpointChannelTest extends DroolsCamelTestSupport {
    private StatefulKnowledgeSession ksession;

    @Test
    public void testChannelSupport() throws Exception {
        Person bob1 = new Person( "bob" );
        Person bob2 = new Person( "bob" );
        Person bob3 = new Person( "bob" );
        Person mark1 = new Person( "mark" );
        
        MockEndpoint bobs = getMockEndpoint("mock:bobs");
        bobs.expectedMessageCount(3);
        bobs.expectedBodiesReceived( bob1, bob2, bob3 );
        
        MockEndpoint marks = getMockEndpoint("mock:marks");
        marks.expectedMessageCount(1);
        marks.expectedBodiesReceived( mark1 );

        ksession.insert( bob1 );
        ksession.insert( mark1 );
        ksession.fireAllRules();

        ksession.insert( bob2 );
        ksession.fireAllRules();

        ksession.insert( bob3 );
        ksession.fireAllRules();

        bobs.assertIsSatisfied();
        marks.assertIsSatisfied();
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from( "drools://node/ksession1?channel=bobs" ).to( "mock:bobs" );
                from( "drools://node/ksession1?channel=marks" ).to( "mock:marks" );
            }
        };
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        String rule = "";
        rule += "package org.drools.pipeline.camel \n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person( name == 'bob' ) \n";
        rule += "  then \n";
        rule += "    channels[\"bobs\"].send( $p ); \n";
        rule += "end\n";
        rule += "rule rule2 \n";
        rule += "  when \n";
        rule += "    $p : Person( name == 'mark' ) \n";
        rule += "  then \n";
        rule += "    channels[\"marks\"].send( $p ); \n";
        rule += "end\n";

        ksession = registerKnowledgeRuntime( "ksession1",
                                             rule );
    }
}
