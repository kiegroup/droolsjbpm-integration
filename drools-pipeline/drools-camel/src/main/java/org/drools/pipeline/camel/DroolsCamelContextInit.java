package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;
import org.drools.vsm.ServiceManager;

/**
 * Camel Processor to initialize the drools-camel context 
 * 
 * @author salaboy
 *
 */
public class DroolsCamelContextInit {

    private ServiceManagerPipelineContextImpl context;

    public DroolsCamelContextInit(ServiceManager serviceManager) {
        this.context = new ServiceManagerPipelineContextImpl(serviceManager, null);
    }

    @Handler
    public void initialize(Exchange exchange) throws Exception {
        exchange.setProperty("drools-context", context);
    }
}
