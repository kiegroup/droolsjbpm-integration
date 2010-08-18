package org.drools.jax.soap;

import java.io.InputStream;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PreCxfSoapProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		InputStream is = (InputStream)exchange.getIn().getBody();
		Map<String, Object> headers = exchange.getIn().getHeaders();
		MimeHeaders mimeHeaders = new MimeHeaders();
		for (String header : headers.keySet()) {
			mimeHeaders.addHeader(header, (String) headers.get(header));
		}
		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(mimeHeaders, is);
		exchange.getOut().setBody(soapMessage.getSOAPBody().getTextContent());
	}

}
