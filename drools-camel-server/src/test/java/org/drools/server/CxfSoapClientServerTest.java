/*
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

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.CamelContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class CxfSoapClientServerTest {

    @org.junit.Test
    public void test1() throws Exception {
        ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext( "classpath:beans-test.xml" );

        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName = new QName( "http://soap.jax.drools.org",
                                       "execute",
                                       "ns1" );

        body.addBodyElement( payloadName );

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"message\">\n";
        cmd += "      <org.test.Message>\n";
        cmd += "         <text>Helllo World</text>\n";
        cmd += "      </org.test.Message>\n";
        cmd += "   </insert>\n";
        cmd += "</batch-execution>\n";

        body.addTextNode( cmd );

        Test test = new Test();
        String response = test.execute( soapMessage,
                                        (CamelContext) springContext.getBean( "camel-client-ctx" ) );

        assertTrue( response.contains( "execution-results" ) );
        assertTrue( response.contains( "echo" ) );  
        springContext.stop();
    }

}
