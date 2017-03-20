/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseIdFilter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.kie.camel.KieCamelUtils.getResultMessage;
import static org.kie.camel.KieCamelConstants.KIE_CLIENT;
import static org.kie.camel.KieCamelConstants.KIE_OPERATION;

@Ignore
public class KieComponentIntegrationTest extends BaseKieComponentTest {

    @Test
    public void interactsOverRest() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "kieServices");
        headers.put(KIE_OPERATION, "getServerInfo");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();

        KieServerInfo result = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(KieServerInfo.class);
        assertEquals("Server version", "1.2.3", result.getVersion());
    }

    @Test
    public void testListContainers() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "kieServices");
        headers.put(KIE_OPERATION, "listContainers");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();

        KieContainerResourceList result = getResultMessage( mockEndpoint.getExchanges().get( 0 ) ).getBody( KieContainerResourceList.class );
        assertEquals("Number of listed containers", 2, result.getContainers().size());
    }

    @Test
    public void testListContainersOverload() throws Exception {
        KieContainerResourceFilter filter = new KieContainerResourceFilter( ReleaseIdFilter.ACCEPT_ALL,
                                                                            KieContainerStatusFilter.ACCEPT_ALL );

        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "kieServices");
        headers.put(KIE_OPERATION, "listContainers");
        headers.put("kie.containerFilter", filter);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();

        KieContainerResourceList result = getResultMessage( mockEndpoint.getExchanges().get( 0 ) ).getBody( KieContainerResourceList.class );
        assertEquals("Number of listed containers", 2, result.getContainers().size());
    }

    @Test
    public void performCustomOperation() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "kieServices");
        headers.put(KIE_OPERATION, "myCustomOperation");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();

        KieServerInfo result = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(KieServerInfo.class);
        assertEquals("Server version", "1.2.3", result.getVersion());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        stubFor(get(urlEqualTo("/"))
                        .withHeader("Accept", equalTo("application/xml"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                                      "  <kie-server-info>\n" +
                                                      "    <version>1.2.3</version>\n" +
                                                      "  </kie-server-info>\n" +
                                                      "</response>")));

        stubFor(get(urlEqualTo("/containers"))
                        .withHeader("Accept", equalTo("application/xml"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody("<response type=\"SUCCESS\" msg=\"List of created containers\">\n" +
                                                      "  <kie-containers>\n" +
                                                      "    <kie-container container-id=\"kjar1\" status=\"FAILED\"/>\n" +
                                                      "    <kie-container container-id=\"kjar2\" status=\"STARTED\"/>" +
                                                      "  </kie-containers>" +
                                                      "</response>")));
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .to("kie:" + getAuthenticadUrl("admin", "admin"))
                        .to("mock:result");
            }
        };
    }
}
