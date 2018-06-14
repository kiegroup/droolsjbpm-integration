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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseIdFilter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.kie.camel.KieCamelConstants.KIE_CLIENT;
import static org.kie.camel.KieCamelConstants.KIE_OPERATION;
import static org.kie.camel.KieCamelUtils.asCamelKieName;
import static org.kie.camel.KieCamelUtils.getResultMessage;

public class KieComponentIntegrationTest extends BaseKieComponentTest {

    @Test
    public void testRest() throws Exception {
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
        headers.put(asCamelKieName("containerFilter"), filter);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();

        KieContainerResourceList result = getResultMessage( mockEndpoint.getExchanges().get( 0 ) ).getBody( KieContainerResourceList.class );
        assertEquals("Number of listed containers", 2, result.getContainers().size());
    }

    @Test
    public void testCustomOperation() throws Exception {
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

    @Test
    public void testBodyParam() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "process");
        headers.put(KIE_OPERATION, "signal");
        headers.put(asCamelKieName("containerId"), "containerId");
        headers.put(asCamelKieName("signalName"), "signalName");
        template.sendBodyAndHeaders("direct:start", "test", headers);
        assertMockEndpointsSatisfied();

        String result = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(String.class);
        assertNull(result);
    }

    @Test
    public void testBodyParam2() throws Exception {
        DMNContext body = new DMNContext() {
            @Override
            public Object set( String s, Object o ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object get( String s ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, Object> getAll() {
                return Collections.emptyMap();
            }

            @Override
            public boolean isDefined( String s ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public DMNContext clone() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void pushScope(String name, String namespace) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void popScope() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<String> scopeNamespace() {
                throw new UnsupportedOperationException();
            }
        };

        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "dmn");
        headers.put(KIE_OPERATION, "evaluateAll");
        headers.put(asCamelKieName("containerId"), "containerId");
        template.sendBodyAndHeaders("direct:start", body, headers);
        assertMockEndpointsSatisfied();

        DMNResult result = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(DMNResult.class);
        assertEquals(1, result.getDecisionResults().size());
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
                                                      "     <capabilities>BPM</capabilities>\n" +
                                                      "     <capabilities>DMN</capabilities>\n" +
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

        stubFor(post(urlEqualTo("/containers/containerId/processes/instances/signal/signalName"))
                        .withHeader("Accept", equalTo("application/xml"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody("<string-type/>")));

        stubFor(post(urlEqualTo("/containers/containerId/dmn"))
                        .withHeader("Accept", equalTo("application/xml"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/xml")
                                            .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                      "<response type=\"SUCCESS\" msg=\"OK from container 'two-dmn-models'\">\n" +
                                                      "   <dmn-evaluation-result>\n" +
                                                      "       <model-namespace>https://github.com/kiegroup/kie-dmn/input-data-string</model-namespace>\n" +
                                                      "       <model-name>input-data-string</model-name>\n" +
                                                      "       <dmn-context xsi:type=\"jaxbListWrapper\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                      "           <type>MAP</type>\n" +
                                                      "           <element xsi:type=\"jaxbStringObjectPair\" key=\"Full Name\">\n" +
                                                      "               <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">John Doe</value>\n" +
                                                      "           </element>\n" +
                                                      "           <element xsi:type=\"jaxbStringObjectPair\" key=\"Greeting Message\">\n" +
                                                      "               <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">Hello John Doe</value>\n" +
                                                      "           </element>\n" +
                                                      "       </dmn-context>\n" +
                                                      "       <messages/>\n" +
                                                      "       <decisionResults>\n" +
                                                      "           <entry>\n" +
                                                      "               <key>d_GreetingMessage</key>\n" +
                                                      "               <value>\n" +
                                                      "                   <decision-id>d_GreetingMessage</decision-id>\n" +
                                                      "                   <decision-name>Greeting Message</decision-name>\n" +
                                                      "                   <result xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">Hello John Doe</result>\n" +
                                                      "                   <status>SUCCEEDED</status>\n" +
                                                      "               </value>\n" +
                                                      "           </entry>\n" +
                                                      "       </decisionResults>\n" +
                                                      "   </dmn-evaluation-result>\n" +
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

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        KieComponent kieComponent = new KieComponent();
        kieComponent.getConfiguration()
                    .clearBodyParams()
                    .setBodyParam( "process", "signal", "event" )
                    .setBodyParam( "dmn", "evaluateAll", "dmnContext" )
                    .setBodyParam( "dmn", "evaluateDecisionByName", "dmnContext" )
                    .setBodyParam( "dmn", "evaluateDecisionById", "dmnContext" );
        context.addComponent( "kie", kieComponent );
        return context;
    }

}
