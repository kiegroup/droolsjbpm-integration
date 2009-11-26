/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.impl.XStreamFromXmlVsmTransformer;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;
import org.drools.vsm.ServiceManager;

/**
 *
 * @author salaboy
 */
public class CamelXStreamFromXmlVsmTransformer implements Processor{
    
    private XStreamFromXmlVsmTransformer transformer;



    public CamelXStreamFromXmlVsmTransformer(XStreamResolverStrategy xstreamStrategy) {

        
        this.transformer = new XStreamFromXmlVsmTransformer(xstreamStrategy);
    }

    public void process(Exchange exchange) throws Exception {
        this.transformer.processPayload(exchange.getIn().getBody(), (PipelineContext)exchange.getProperty("drools-context"));
        exchange.getIn().setBody(this.transformer.getPayload());
        exchange.setProperty("drools-context",this.transformer.getContext() );
    }

   

    

    

}
