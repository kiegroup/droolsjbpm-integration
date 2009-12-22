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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.drools.command.Command;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.vsm.ServiceManager;

public class DroolsProducer extends DefaultProducer {
    private ServiceManager serviceManager;
    private CommandExecutor executor;

    public DroolsProducer(Endpoint endpoint, ServiceManager serviceManager) {
        super(endpoint);
        this.serviceManager = serviceManager;
        
        DroolsEndpoint de = (DroolsEndpoint) endpoint;
        executor = serviceManager.lookup(de.getKsession());
    }

    public void process(Exchange exchange) throws Exception {
        CommandExecutor exec = executor;
        if (exec == null) {
            // need to look it up
            String ksession = exchange.getIn().getHeader(DroolsComponent.DROOLS_LOOKUP, String.class);
            exec = serviceManager.lookup(ksession == null ? "" : ksession);
            
            // cannot continue if executor is not available
            if (exec == null) {
                throw new RuntimeCamelException("Null executor");
            }
        }
        
        Command cmd = exchange.getIn().getBody(Command.class);
        if (cmd == null) {
            throw new RuntimeCamelException("Body of in message not of the expected type 'org.drools.command.Command'");
        }
        ExecutionResults results = exec.execute(cmd);
        exchange.getOut().setBody(results);
    }

    // There are nicer ways of doing this
    public static class ResultHandlerImpl implements ResultHandler {
        Object object;

        public void handleResult(Object object) {
            this.object = object;
        }
        
        public Object getObject() {
            return this.object;
        }
    }
}
