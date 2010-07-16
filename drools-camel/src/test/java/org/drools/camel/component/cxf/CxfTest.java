/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.camel.component.cxf;

import java.io.InputStream;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfConstants;
import org.apache.camel.test.CamelSpringTestSupport;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.drools.camel.component.DroolsCamelTestSupport;
import org.drools.camel.component.DroolsPolicy;
import org.drools.core.util.StringUtils;
import org.junit.Test;

public class CxfTest extends DroolsCamelTestSupport {
    public void test1() {
        
    }
//    private static final String REST_ENDPOINT_URI = "cxfrs://http://localhost:9002/rest"
//        + "?resourceClasses=org.drools.jax.rs.CommandExecutorImpl2";
//    
//    public static class CxfrsProducer implements Processor {
//        public void process(Exchange exchange) throws Exception {
//            exchange.setPattern(ExchangePattern.InOut);
//            Message inMessage = exchange.getIn();
//            inMessage.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, Boolean.TRUE);
//            inMessage.setHeader(Exchange.HTTP_METHOD, "POST");
//            inMessage.setHeader(Exchange.HTTP_PATH, "/execute");    
//            inMessage.setHeader(Exchange.ACCEPT_CONTENT_TYPE, "text/plain");
//            inMessage.setHeader(Exchange.CONTENT_TYPE, "text/plain");            
//        }        
//    }
//    
//    public void test1() throws Exception {
////        Response response = (Response) this.context.createProducerTemplate().requestBody("direct://http://localhost:9002/rest", "hello world"); 
////
////        System.out.println( StringUtils.toString( (InputStream) response.getEntity() ));
////    
//    }
//    
//    @Override
//    protected void configureDroolsContext(Context jndiContext) {
////        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
////        sf.setResourceClasses(CommandExecutorImpl2.class);
////        sf.setAddress("http://localhost:9002/rest");
////        //sf.create();
////        try {
////            jndiContext.bind( "rsServer", sf );
////        } catch ( NamingException e ) {
////            throw new RuntimeException( e );
////        }
//
//    }
//    
//    @Override
//    protected RouteBuilder createRouteBuilder() throws Exception {
//        return new RouteBuilder() {
//            public void configure() throws Exception {
//                from(REST_ENDPOINT_URI)
//                .process( new MockProcessor() );
//                
//                from("direct:http://localhost:9002/rest").process( new CxfrsProducer() ).
//                to( "cxfrs://http://localhost:9002/rest" );
//            }
//        };
//    }    
//    
//    public static class MockProcessor implements Processor {
//
//        public void process(Exchange exchange) throws Exception {
//            //System.out.println( exchange.getIn().get)
//            System.out.println( exchange.getIn().getBody( String.class ) );
//            exchange.getOut().setBody( "goodbye world" );
//        }
//        
//    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        // TODO Auto-generated method stub
        
    }

}
