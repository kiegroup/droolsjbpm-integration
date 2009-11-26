/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.XStreamToXmlVsmTransformer;

/**
 *
 * @author salaboy
 */
class CamelXStreamToXmlVsmTransformer implements Processor {
    private XStreamToXmlVsmTransformer transformer;
    
    public CamelXStreamToXmlVsmTransformer() {
        
        this.transformer = new XStreamToXmlVsmTransformer();
    }

    public void process(Exchange exchange) throws Exception {
        Object result = this.transformer.transform((PipelineContext)exchange.getProperty("drools-context"), exchange.getIn().getBody());
        exchange.getIn().setBody(result);

    }

}
