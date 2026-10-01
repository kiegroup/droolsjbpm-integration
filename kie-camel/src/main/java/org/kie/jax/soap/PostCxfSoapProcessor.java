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

package org.kie.jax.soap;

import java.io.InputStream;
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.core.util.StringUtils;

public class PostCxfSoapProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        // In Camel 4, marshal() leaves a String on getMessage(); use getBody(InputStream.class)
        // for automatic type conversion rather than a raw cast.
        InputStream is = exchange.getMessage().getBody(InputStream.class);
        if (is != null) {
            String results = StringUtils.toString(is);
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
            QName payloadName = new QName("http://soap.jax.drools.org/", "executeResponse", "ns1");
            QName responseName = new QName("http://soap.jax.drools.org/", "return", "ns1");
            SOAPBodyElement payload = body.addBodyElement(payloadName);
            SOAPElement response = payload.addChildElement(responseName);
            // Bad Hack - Need to remote it and fix it in Camel (if it's a camel problem)
            // response.addTextNode( StringUtils.toString( is ) );
            response.addTextNode(results);
            exchange.getMessage().setBody(soapMessage);
        }
    }

}
