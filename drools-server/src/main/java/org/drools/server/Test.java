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

package org.drools.server;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.test.CamelSpringTestSupport;
import org.drools.core.util.StringUtils;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
    
    public static void main(String[] args) {
        String msg = "Hello World";
        
        System.out.println( "Sending Message:\n"  + msg);        
        
        Test test = new Test();
        String response = test.send( msg );
        
        System.out.println( );
        System.out.println( );
        
        System.out.println( "Received Response:\n" + response);      
    }
    
    public String send(String msg) {
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath:camel-client.xml");
        String batch = "";
        batch += "<batch-execution lookup=\"ksession1\">\n";
        batch += "  <insert out-identifier=\"message\">\n";
        batch += "      <org.test.Message>\n";
        batch += "         <text>" + msg + "</text>\n";
        batch += "      </org.test.Message>\n";
        batch += "   </insert>\n";
        batch += "</batch-execution>\n";                
        
        
        Test test = new Test();
        String response = test.execute( batch, 
                                        ( CamelContext ) springContext.getBean( "camel" ) );
        
        return response;
    }
    
    public String execute(String msg, CamelContext camelContext) {                
               
        String response = camelContext.createProducerTemplate().requestBody( "direct://kservice", msg, String.class );
  
        return response;
    }
}