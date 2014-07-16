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

package org.kie.camel.component;

import javax.naming.Context;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.pipeline.camel.Person;

public class KieEndpointEventTest extends KieCamelTestSupport {
    private KieSession ksession;

    @Test
    public void testChannelSupport() throws Exception {
        Person bob1 = new Person( "bob" );

        MockEndpoint agendaEvent = getMockEndpoint("mock:AgendaEvent");
        MockEndpoint ruleRuntimeEvent = getMockEndpoint("mock:RuleRuntimeEvent");
        MockEndpoint processEvent = getMockEndpoint("mock:ProcessEvent");
        MockEndpoint kieBaseEvent = getMockEndpoint("mock:KieBaseEvent");
        agendaEvent.expectedMessageCount(3);
        ruleRuntimeEvent.expectedMessageCount(1);
        processEvent.expectedMessageCount(0);
        kieBaseEvent.expectedMessageCount(0);

        ksession.insert(bob1);
        ksession.fireAllRules();

        MockEndpoint.assertIsSatisfied(context);
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from( "kie://ksession1?eventType=AgendaEvent" ).to( "mock:AgendaEvent" );
                from( "kie://ksession1?eventType=RuleRuntimeEvent" ).to( "mock:RuleRuntimeEvent" );
                from( "kie://ksession1?eventType=ProcessEvent" ).to( "mock:ProcessEvent" );
                from( "kie://ksession1?eventType=KieBaseEvent" ).to( "mock:KieBaseEvent" );
            }
        };
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        String rule = "";
        rule += "package org.kie.pipeline.camel \n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person( name == 'bob' ) \n";
        rule += "  then \n";
        rule += "    System.out.println(\"bob\"); \n";
        rule += "end\n";

        ksession = registerKnowledgeRuntime( "ksession1", rule );
    }
}
- 
