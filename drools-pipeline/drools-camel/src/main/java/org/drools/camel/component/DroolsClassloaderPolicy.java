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

import org.apache.camel.Exchange;
import org.apache.camel.Navigate;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.Service;
import org.apache.camel.impl.RoutePolicySupport;
import org.apache.camel.processor.SendProcessor;
import org.drools.grid.ExecutionNode;
import org.drools.runtime.pipeline.impl.ExecutionNodePipelineContextImpl;

public class DroolsClassloaderPolicy extends RoutePolicySupport {
    private ExecutionNodePipelineContextImpl context;

    @Override
    public void onInit(Route route) {
        ExecutionNode node = getDroolsNode(route.navigate());
        if (node != null) {
            context = new ExecutionNodePipelineContextImpl(node, null);
        }
    }

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        if (context != null) {
            exchange.setProperty("drools-context", context);
        }
    }
    
    private ExecutionNode getDroolsNode(Navigate<Processor> nav) {
        if (nav.hasNext()) {
            for (Processor child : nav.next()) {
                if (child instanceof Navigate) {
                    ExecutionNode id = getDroolsNode((Navigate) child);
                    if (id != null) {
                        return id;
                    }
                } else if (child instanceof SendProcessor && 
                        ((SendProcessor)child).getDestination() instanceof DroolsEndpoint) {
                    DroolsEndpoint e = (DroolsEndpoint)((SendProcessor)child).getDestination();
                    return e.getExecutionNode();
                }
            }
        }
        return null;
    }
}
