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
package org.drools.jax.soap;

import java.io.InputStream;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PreCxfTransportSoapProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		InputStream is = (InputStream)exchange.getIn().getBody();
		Map<String, Object> headers = exchange.getIn().getHeaders();
		MimeHeaders mimeHeaders = new MimeHeaders();
		for (String header : headers.keySet()) {
			mimeHeaders.addHeader(header, (String) headers.get(header));
		}
		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(mimeHeaders, is);
		exchange.getOut().setBody(soapMessage.getSOAPBody().getTextContent().trim());
		exchange.getIn().setBody(soapMessage.getSOAPBody().getTextContent().trim());
	}

}
