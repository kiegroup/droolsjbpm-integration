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
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieSession;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.camel.embedded.factories.DroolsXStreamDataFormatFactory;
import org.kie.jax.rs.CommandExecutorImpl;
import org.kie.jax.rs.CommandMessageBodyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Camel 4 rewrite of the former CxfRestWithImportTest.
 * The original used a {@code <kie:import>} of an external kjar (named-kiesession 7.5.0.Final).
 * That kjar is no longer resolvable; we use a local default kbase instead, which lets us
 * validate the full CXF-RS→KieSession integration path regardless.
 */
public class CxfRestWithImportTest extends CamelTestSupport {

    private static final Logger logger = LoggerFactory.getLogger(CxfRestWithImportTest.class);
    private static final String RS_ADDRESS = "http://localhost:58001/rest";

    @Override
    protected void bindToRegistry(Registry registry) throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        ks.newKieBuilder(kfs).buildAll();
        KieSession ksession1 = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        registry.bind("ksession1", ksession1);

        // In Camel 4, providers= in URI string is resolved as a registry bean name.
        registry.bind("commandMessageBodyReader", new CommandMessageBodyReader());

        CxfRsEndpoint rsServer = context.getEndpoint(
                "cxfrs:" + RS_ADDRESS
                + "?resourceClasses=" + CommandExecutorImpl.class.getName()
                + "&providers=#commandMessageBodyReader",
                CxfRsEndpoint.class);
        registry.bind("rsServer", rsServer);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        org.apache.camel.spi.DataFormat xstream = DroolsXStreamDataFormatFactory.getDroolsXStreamDataFormat("xstream");
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("cxfrs:bean:rsServer")
                    .policy(new KiePolicy())
                    .unmarshal(xstream)
                    .to("kie-local:ksession1")
                    .marshal(xstream);

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
        cmd += "  <insert out-identifier=\"vkiran\">\n";
        cmd += "      <org.kie.pipeline.camel.Person>\n";
        cmd += "         <name>vkiran</name>\n";
        cmd += "      </org.kie.pipeline.camel.Person>\n";
        cmd += "   </insert>\n";
        cmd += "   <fire-all-rules/>\n";
        cmd += "</batch-execution>\n";

        String response = context.createProducerTemplate().requestBody("direct://http", cmd, String.class);
        logger.debug("{}", response);
        assertNotNull(response, "Expected non-null response from CXF-RS endpoint");
        assertTrue(response.contains("fact-handle identifier=\"vkiran\""),
                "Expected fact-handle in response but got: " + response);
    }
}
