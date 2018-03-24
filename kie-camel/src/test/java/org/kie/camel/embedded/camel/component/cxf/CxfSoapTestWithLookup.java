/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.embedded.camel.component.cxf;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Ignore;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Ignore
public class CxfSoapTestWithLookup extends CamelSpringTestSupport {

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/kie/camel/component/CxfSoapSpringWithoutSession.xml");
    }

    // This test fails, I make it work with some hacks.
    // Look for //Bad Hack - Need to remote it and fix it in Camel (if it's a camel problem)
    // In DroolsPolicy and PostCxfSoapProcessor

    // Watch out: this functionality affects Drools Server
    //
    // The problem seems to be related with the CXFProducer and the async processors,
    // and it only appears when we do a lookup for different sessions.
    // Using different sessions requires that camel switch classloader to execute commands
    // in the existing sessions. That could be realted too..
    // but in Drools Camel code I don't find any problem
    // The rest endpoint is working ok
    public void testCxfSoapSessionLookup() throws Exception {

        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName = new QName("http://soap.jax.drools.org", "execute", "ns1");

        body.addBodyElement(payloadName);

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\" disconnected=\"true\">\n";
        cmd += "      <org.kie.springframework.Person2>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "         <age>27</age>\n";
        cmd += "      </org.kie.springframework.Person2>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        body.addTextNode(cmd);

        Object object = this.context.createProducerTemplate().requestBody("direct://http", soapMessage);

        OutputStream out = new ByteArrayOutputStream();
        out = new ByteArrayOutputStream();
        soapMessage = (SOAPMessage)object;
        soapMessage.writeTo(out);
        String response = out.toString();
        assertTrue(response.contains("fact-handle identifier=\"salaboy\""));

        SOAPMessage soapMessage2 = MessageFactory.newInstance().createMessage();
        SOAPBody body2 = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName2 = new QName("http://soap.jax.drools.org", "execute", "ns1");

        body2.addBodyElement(payloadName2);

        String cmd2 = "";
        cmd2 += "<batch-execution lookup=\"ksession2\">\n";
        cmd2 += "  <insert out-identifier=\"salaboy\" disconnected=\"true\">\n";
        cmd2 += "      <org.kie.springframework.Person3>\n";
        cmd2 += "         <name>salaboy</name>\n";
        cmd2 += "         <age>27</age>\n";
        cmd2 += "      </org.kie.springframework.Person3>\n";
        cmd2 += "   </insert>\n";
        cmd2 += "   <fire-all-rules/>\n";
        cmd2 += "</batch-execution>\n";

        body2.addTextNode(cmd2);

        Object object2 = this.context.createProducerTemplate().requestBody("direct://http", soapMessage2);

        OutputStream out2 = new ByteArrayOutputStream();
        out2 = new ByteArrayOutputStream();
        soapMessage2 = (SOAPMessage)object2;
        soapMessage2.writeTo(out2);
        String response2 = out2.toString();
        assertTrue(response2.contains("fact-handle identifier=\"salaboy\""));

    }

}
