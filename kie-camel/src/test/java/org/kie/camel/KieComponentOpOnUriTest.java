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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.kie.camel.KieCamelUtils.asCamelKieName;
import static org.kie.camel.KieCamelUtils.getResultMessage;

public class KieComponentOpOnUriTest extends BaseKieComponentTest {

    @Test
    public void testBodyParam() throws Exception {
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
        };

        MockEndpoint mockEndpoint = getMockEndpoint( "mock:result" );
        mockEndpoint.expectedMessageCount( 1 );

        Map<String, Object> headers = new HashMap<>();
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
                        .to("kie:" + getAuthenticadUrl("admin", "admin") + "?client=dmn&operation=evaluateAll")
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
