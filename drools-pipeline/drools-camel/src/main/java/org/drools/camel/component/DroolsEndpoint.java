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

import java.net.URISyntaxException;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.runtime.CommandExecutor;

public class DroolsEndpoint extends DefaultEndpoint {

    private String ksession;
    private String pipeline;
    private CommandExecutor executor;
    private ExecutionNode node;

    public DroolsEndpoint(String endpointUri, String remaining, DroolsComponent component) throws URISyntaxException {
        super(endpointUri, component);
        configure(component, remaining);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Drools consumers not supported.");
    }

    public Producer createProducer() throws Exception {
        return new DroolsProducer(this, node);
    }

    public boolean isSingleton() {
        return true;
    }

    public String getKsession() {
        return ksession;
    }

    public void setKsession(String ksession) {
        this.ksession = ksession;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public ExecutionNode getExecutionNode() {
        return node;
    }

    protected void configure(DroolsComponent component, String uri) {
        String smId = DroolsComponent.getSessionManagerId(uri);
        ksession = DroolsComponent.getKsessionId(uri);

        if (smId.length() > 0) {
            // initialize the component if needed
            node = component.getExecutionNode();
            if (node == null) {
                // let's look it up
                node = component.getCamelContext().getRegistry().lookup(smId, ExecutionNode.class);
                if (node == null) {
                    throw new RuntimeCamelException("Could not find ServiceManager with id=\""
                        + smId + "\" in CamelContext. Check configuration.");
                }
                // use this ServiceManager
                component.setExecutionNodeId(smId);
                component.setExecutionNode(node);
            } else if (!smId.equals(component.getExecutionNodeId())) {
                // make sure we deal with the same ServiceManager.
                // having multiple ServiceManagers instances in the same process is not supported
                throw new RuntimeCamelException("ServiceManager already initialized from id=\""
                    + component.getExecutionNodeId() + "\" yet current endpoint requries id=\"" + smId + "\"");
            }
            
            // if id is empty this endpoint is not attached to a CommandExecutor and will have to look it up at runtime.
            if (ksession.length() > 0) {
                // lookup command executor
                executor = node.get(DirectoryLookupFactoryService.class).lookup(ksession);
                if (executor == null) {
                    throw new RuntimeCamelException("Failed to instantiate DroolsEndpoint. " 
                        + "Lookup of CommandExecutor with id=\"" + uri + "\" failed. Check configuration.");
                }
            }
        } else {
            // this is a hanging entity, not attached to an SM
            executor = component.getCamelContext().getRegistry().lookup(ksession, CommandExecutor.class);
            
            // TODO: test this scenario...
        }
    }
}
