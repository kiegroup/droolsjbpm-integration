package org.drools.jax.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.core.util.StringUtils;

public class PostCxfSoapProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		byte[] body = (byte[]) exchange.getIn().getBody();

		ByteArrayInputStream bais = new ByteArrayInputStream(body);

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
		SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
		QName payloadName = new QName("http://soap.jax.drools.org", "executeResponse", "ns1");
		SOAPBodyElement payload = soapBody.addBodyElement(payloadName);
		payload.addChildElement("responseType");
		soapBody.addTextNode(StringUtils.toString(bais));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		soapMessage.writeTo(baos);

		exchange.getOut().setBody(new String(baos.toByteArray()));
	}

}
