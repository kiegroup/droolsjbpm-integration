/**
 * 
 */
package org.drools.camel.component;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfConstants;
import org.drools.core.util.StringUtils;

public class PostCxfrs implements Processor {
    public void process(Exchange exchange) throws Exception {
        Object object = exchange.getIn().getBody();
        if ( object instanceof Response ) {
            Response res = ( Response ) object;
            if ( res.getStatus() == Status.OK.getStatusCode()) {
                exchange.getOut().setBody( StringUtils.toString( (InputStream) ((Response)object).getEntity() ) );
            }
        }
    }        
}