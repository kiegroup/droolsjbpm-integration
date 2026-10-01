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
import java.util.Map;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class PreCxfTransportSoapProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        // In Camel 4 with dataFormat=MESSAGE, the CXF endpoint may provide the body as a
        // SOAPMessage or as an InputStream.  Use getBody(InputStream.class) so Camel's type
        // converters handle both cases instead of a raw cast.
        InputStream is = exchange.getMessage().getBody(InputStream.class);
        Map<String, Object> headers = exchange.getIn().getHeaders();
        MimeHeaders mimeHeaders = new MimeHeaders();
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            if (header.getValue() instanceof String) {
                String value = (String) header.getValue();
                mimeHeaders.addHeader(header.getKey(), value);
            }
        }
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(mimeHeaders, is);
        exchange.getMessage().setBody(soapMessage.getSOAPBody().getTextContent().trim());
    }

}
