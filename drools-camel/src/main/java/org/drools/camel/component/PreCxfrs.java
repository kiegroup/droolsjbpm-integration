/**
 * 
 */
package org.drools.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfConstants;

public class PreCxfrs implements Processor {
    public void process(Exchange exchange) throws Exception {
        exchange.setPattern(ExchangePattern.InOut);
        Message inMessage = exchange.getIn();
        inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
        inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
        inMessage.setHeader(Exchange.HTTP_PATH, "/execute");    
        inMessage.setHeader(Exchange.ACCEPT_CONTENT_TYPE, "text/plain");
        inMessage.setHeader(Exchange.CONTENT_TYPE, "text/plain");                
    }        
}