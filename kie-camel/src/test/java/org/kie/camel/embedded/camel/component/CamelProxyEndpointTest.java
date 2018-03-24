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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.kie.camel.embedded.camel.component;

import java.util.Arrays;
import javax.naming.Context;

import org.apache.camel.builder.RouteBuilder;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.CommandFactory;
import org.kie.pipeline.camel.Person;

public class CamelProxyEndpointTest extends KieCamelTestSupport {
    private String handle;

    @Test
    public void testSessionInsert() throws Exception {
        Person person = new Person();
        person.setName("Mauricio");

        InsertObjectCommand cmd = (InsertObjectCommand) CommandFactory.newInsert(person, "salaboy");
        ExecutionResults response = (ExecutionResults)template.requestBody("direct:test-no-marshal", cmd);
        assertTrue("Expected valid ExecutionResults object", response != null);
        assertTrue("ExecutionResults missing expected fact", response.getFactHandle("salaboy") != null);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:test-no-marshal").to("kie-local://ksession1");
            }
        };
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        Person me = new Person();
        me.setName("Hadrian");

        KieSession ksession = registerKnowledgeRuntime("ksession1", null);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);
        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl(Arrays.asList(new Command<?>[] {cmd}));

        ExecutionResults results = ksession.execute(script);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
    }
}
