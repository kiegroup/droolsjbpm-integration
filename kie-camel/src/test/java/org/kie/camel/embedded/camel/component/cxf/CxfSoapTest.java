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
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPMessage;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieSession;
import org.kie.camel.embedded.component.DroolsXStreamDataFormat;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.camel.embedded.component.XStreamXml;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Camel 4 rewrite of the former CxfSoapTest.
 * The old test used CamelSpringTestSupport + CxfSoapSpring.xml which relied on the
 * {@code http://camel.apache.org/schema/cxf} Spring XML namespace (removed in Camel 4).
 * This version wires CxfEndpoint programmatically via Java RouteBuilder.
 */
public class CxfSoapTest extends CamelTestSupport {

    private static final String SOAP_ADDRESS = "http://localhost:58002/soap";
    private static final String WSDL_URL = "classpath:org/kie/camel/embedded/camel/component/soap.wsdl";
    private static final String SERVICE_NS = "http://soap.jax.drools.org/";

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        ks.newKieBuilder(kfs).buildAll();
        KieSession ksession1 = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        registry.bind("ksession1", ksession1);

        // Wire CXF SOAP endpoint programmatically (replaces <cxf:cxfEndpoint> XML bean)
        CxfEndpoint soapServer = context.getEndpoint(
                "cxf:" + SOAP_ADDRESS
                + "?wsdlURL=" + WSDL_URL
                + "&serviceName={" + SERVICE_NS + "}CommandExecutor"
                + "&endpointName={" + SERVICE_NS + "}CommandExecutorPort"
                + "&dataFormat=MESSAGE"
                + "&defaultOperationName=execute",
                CxfEndpoint.class);
        registry.bind("soapServer", soapServer);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        org.apache.camel.spi.DataFormat xstream = XStreamXml.newXStreamMarshaller(new DroolsXStreamDataFormat());
        return new RouteBuilder() {
            @Override
            public void configure() {
                // Server route: KiePolicy detects CxfEndpoint and automatically applies
                // PreCxfTransportSoapProcessor (before) and PostCxfTransportSoapProcessor (after).
                // Do NOT also add explicit Pre/PostCxfSoapProcessor — that would double-process.
                from("cxf:bean:soapServer")
                    .policy(new KiePolicy())
                    .unmarshal(xstream)
                    .to("kie-local:ksession1")
                    .marshal(xstream);

                // Client route: send SOAP request
                from("direct://http")
                    .policy(new KiePolicy())
                    .to("cxf:bean:soapServer");
            }
        };
    }

    @Test
    public void test1() throws Exception {
        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        QName payloadName = new QName(SERVICE_NS, "execute", "ns1");

        body.addBodyElement(payloadName);

        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\" disconnected=\"true\">\n";
        cmd += "      <org.kie.pipeline.camel.Person>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "         <age>27</age>\n";
        cmd += "      </org.kie.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        body.addTextNode(cmd);

        Object object = context.createProducerTemplate().requestBody("direct://http", soapMessage);
        OutputStream out = new ByteArrayOutputStream();
        soapMessage = (SOAPMessage) object;
        soapMessage.writeTo(out);
        String response = out.toString();
        assertTrue(response.contains("fact-handle identifier=\"salaboy\""),
                "Expected fact-handle in SOAP response but got: " + response);
    }
}
