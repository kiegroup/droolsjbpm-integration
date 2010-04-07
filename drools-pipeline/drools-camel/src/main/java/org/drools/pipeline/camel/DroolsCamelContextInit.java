package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.drools.grid.ExecutionNode;
import org.drools.runtime.pipeline.impl.ExecutionNodePipelineContextImpl;

/**
 * Camel Processor to initialize the drools-camel context 
 * 
 * @author salaboy
 *
 */
public class DroolsCamelContextInit {

    private ExecutionNodePipelineContextImpl context;

    public DroolsCamelContextInit(ExecutionNode node) {
        this.context = new ExecutionNodePipelineContextImpl(node, null);
    }

    @Handler
    public void initialize(Exchange exchange) throws Exception {
        exchange.setProperty("drools-context", context);
    }
}
