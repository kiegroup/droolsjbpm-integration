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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxrs.CxfRsEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.jupiter.api.Test;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieSession;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.camel.embedded.component.XStreamXml;
import org.kie.camel.embedded.component.DroolsXStreamDataFormat;
import org.kie.jax.rs.CommandExecutorImpl;
import org.kie.jax.rs.CommandMessageBodyReader;
import org.kie.api.KieServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Camel 4 rewrite of the former CxfRestTest.
 * The old test used CamelSpringTestSupport + CxfRsSpring.xml which relied on the
 * {@code http://camel.apache.org/schema/cxf} Spring XML namespace — that namespace
 * was removed in Camel 4.  This version wires everything in pure Java using
 * CxfRsEndpoint and RouteBuilder.
 */
public class CxfRestTest extends CamelTestSupport {

    private static final Logger logger = LoggerFactory.getLogger(CxfRestTest.class);
    private static final String RS_ADDRESS = "http://localhost:58001/rest";

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {
        // Build ksession1 with test1 rules package
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        ks.newKieBuilder(kfs).buildAll();
        KieSession ksession1 = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        registry.bind("ksession1", ksession1);

        // In Camel 4, providers= in URI string is resolved as a registry bean name.
        // Register an instance so the lookup by name succeeds.
        registry.bind("commandMessageBodyReader", new CommandMessageBodyReader());

        // CXF-RS server endpoint wired programmatically
        CxfRsEndpoint rsServer = context.getEndpoint(
                "cxfrs:" + RS_ADDRESS
                + "?resourceClasses=" + CommandExecutorImpl.class.getName()
                + "&providers=#commandMessageBodyReader",
                CxfRsEndpoint.class);
        registry.bind("rsServer", rsServer);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        org.apache.camel.spi.DataFormat xstream = XStreamXml.newXStreamMarshaller(new DroolsXStreamDataFormat());
        return new RouteBuilder() {
            @Override
            public void configure() {
                // Server route: receive REST call → execute against ksession1 → return result
                from("cxfrs:bean:rsServer")
                    .policy(new KiePolicy())
                    .unmarshal(xstream)
                    .to("kie-local:ksession1")
                    .marshal(xstream);

                // Client route: send XStream XML via REST
                from("direct://http")
                    .policy(new KiePolicy())
                    .to("cxfrs:" + RS_ADDRESS);
            }
        };
    }

    @Test
    public void test1() throws Exception {
        String cmd = "";
        cmd += "<batch-execution lookup=\"ksession1\">\n";
        cmd += "  <insert out-identifier=\"salaboy\">\n";
        cmd += "      <org.kie.pipeline.camel.Person>\n";
        cmd += "         <name>salaboy</name>\n";
        cmd += "      </org.kie.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        Object object = context.createProducerTemplate().requestBody("direct://http", cmd);
        logger.debug("{}", object);
        assertNotNull(object, "Expected non-null response from CXF-RS endpoint");
    }
}
