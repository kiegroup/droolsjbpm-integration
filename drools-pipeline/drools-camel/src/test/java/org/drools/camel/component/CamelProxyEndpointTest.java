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

import org.apache.camel.builder.RouteBuilder;
import org.drools.command.CommandFactory;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public class CamelProxyEndpointTest extends DroolsCamelTestSupport {
    private String handle;

    public void testSessionInsert() throws Exception {
        Person person = new Person();
        person.setName("Mauricio");

        InsertObjectCommand cmd = (InsertObjectCommand) CommandFactory.newInsert(person,"salaboy");
        ExecutionResults response = (ExecutionResults) template.requestBody("direct:test-no-marshal", cmd);
        assertTrue("Expected valid ExecutionResults object", response != null);
        assertTrue("ExecutionResults missing expected fact", response.getFactHandle("salaboy") != null);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:test-no-marshal").to("drools://sm/ksession1");
            }
        };
    }

    @Override
    protected void configureDroolsContext() {
        Person me = new Person();
        me.setName("Hadrian");

        StatefulKnowledgeSession ksession = registerKnowledgeRuntime("ksession1", null);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);
        ExecutionResults results = ksession.execute(cmd);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
    }
}
