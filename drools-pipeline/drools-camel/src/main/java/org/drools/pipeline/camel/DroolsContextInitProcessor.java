package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;

/**
 * Camel Processor to initialize the drools-camel context 
 * 
 * @author Lucas Amador
 *
 */
public class DroolsContextInitProcessor implements Processor {

	private ServiceManagerPipelineContextImpl context;

	public DroolsContextInitProcessor(ServiceManagerPipelineContextImpl context) {
		this.context = context;
	}

	public void process(Exchange exchange) throws Exception {
		exchange.setProperty("drools-context", context);
	}

}
