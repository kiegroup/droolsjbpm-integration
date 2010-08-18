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


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfSoapTest extends CamelSpringTestSupport {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {        
		return new ClassPathXmlApplicationContext("org/drools/camel/component/CxfSoapSpring.xml");
	}

	public void test1() throws Exception {

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
		QName payloadName = new QName("http://soap.jax.drools.org", "execute", "ns1");

		body.addBodyElement(payloadName);

		String cmd = "";
		cmd += "<batch-execution lookup=\"ksession1\">\n";
		cmd += "  <insert out-identifier=\"salaboy\" disconnected=\"true\">\n";
		cmd += "      <org.drools.pipeline.camel.Person>\n";
		cmd += "         <name>salaboy</name>\n";
		cmd += "         <age>27</age>\n";
		cmd += "      </org.drools.pipeline.camel.Person>\n";
		cmd += "   </insert>\n";
		cmd += "   <fire-all-rules/>\n";
		cmd += "</batch-execution>\n";

		body.addTextNode(cmd);

		OutputStream out = new ByteArrayOutputStream();
		soapMessage.writeTo(out);

		String request = out.toString();
		System.out.println("Request = " + request);

		Object object = this.context.createProducerTemplate().requestBody("direct://http", soapMessage);
		System.out.println(object.getClass().getCanonicalName());
		out = new ByteArrayOutputStream();
		soapMessage = (SOAPMessage) object;
		soapMessage.writeTo(out);
		String response = out.toString();
		System.out.println("Response = "+response);
		assertTrue(response.contains("fact-handle identifier=\"salaboy\""));
	}

}
