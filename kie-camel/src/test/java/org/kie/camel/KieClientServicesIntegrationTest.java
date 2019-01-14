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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.kie.internal.executor.api.STATUS;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.kie.camel.KieCamelConstants.KIE_CLIENT;
import static org.kie.camel.KieCamelConstants.KIE_OPERATION;
import static org.kie.camel.KieCamelUtils.asCamelKieName;
import static org.kie.camel.KieCamelUtils.getResultMessage;

public class KieClientServicesIntegrationTest extends BaseKieComponentTest {

    @Test
    public void testCaseServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "case");
        headers.put(KIE_OPERATION, "findProcesses");
        headers.put(asCamelKieName("page"), 1);
        headers.put(asCamelKieName("pageSize"), 1);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        List<ProcessDefinition> result = (List<ProcessDefinition>) getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(List.class);
        assertCollectionSize(result, 1);
    }

    @Test
    public void testDocumentServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "document");
        headers.put(KIE_OPERATION, "getDocument");
        headers.put(asCamelKieName("identifier"), "1234");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        DocumentInstance documentInstance = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(DocumentInstance.class);
        assertEquals(documentInstance.getIdentifier(), "1234");
    }

    @Test
    public void testJobServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "job");
        headers.put(KIE_OPERATION, "getRequestsByStatus");

        headers.put(asCamelKieName("statuses"), Arrays.asList(STATUS.values()));
        headers.put(asCamelKieName("page"), 1);
        headers.put(asCamelKieName("pageSize"), 10);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        List<RequestInfoInstance> requestInfoInstanceList = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(List.class);
        assertCollectionSize(requestInfoInstanceList, 0);
    }

    @Test
    public void testQueryServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "query");
        headers.put(KIE_OPERATION, "findCompletedNodeInstances");
        headers.put(asCamelKieName("processInstanceId"), 100L);
        headers.put(asCamelKieName("page"), 1);
        headers.put(asCamelKieName("pageSize"), 10);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        List<NodeInstance> nodeInstanceList = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(List.class);
        assertCollectionSize(nodeInstanceList, 0);
    }

    @Test
    public void testSolverServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "solver");
        headers.put(KIE_OPERATION, "getSolvers");
        headers.put(asCamelKieName("containerId"), "my-container");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        List<NodeInstance> nodeInstanceList = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(List.class);
        assertCollectionSize(nodeInstanceList, 0);
    }

    @Test
    public void testUiServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "ui");
        headers.put(KIE_OPERATION, "getProcessForm");
        headers.put(asCamelKieName("containerId"), "my-container");
        headers.put(asCamelKieName("processId"), "my-process");
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        String form = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(String.class);
        assertEquals(form, "my form");
    }

    @Test
    public void testUserTaskServiceCamelProducer() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(KIE_CLIENT, "userTask");
        headers.put(KIE_OPERATION, "findTasks");

        headers.put(asCamelKieName("userId"), "rhpamAdmin");
        headers.put(asCamelKieName("page"), 1);
        headers.put(asCamelKieName("pageSize"), 10);
        template.sendBodyAndHeaders("direct:start", null, headers);
        assertMockEndpointsSatisfied();
        List<TaskSummary> taskSummary = getResultMessage(mockEndpoint.getExchanges().get(0)).getBody(List.class);
        assertCollectionSize(taskSummary, 0);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {

        KieServerInfo info = new KieServerInfo("mock", "1.2.3");
        List<String> capabilities = Arrays.asList(KieServerConstants.CAPABILITY_BPM, KieServerConstants.CAPABILITY_BPM_UI,
                                                  KieServerConstants.CAPABILITY_BRM, KieServerConstants.CAPABILITY_BRP,
                                                  KieServerConstants.CAPABILITY_CASE, KieServerConstants.CAPABILITY_DMN);
        info.setCapabilities(capabilities);
        ServiceResponse<KieServerInfo> response = new ServiceResponse<KieServerInfo>(ResponseType.SUCCESS, "Kie Server info");
        response.setResult(info);

        stubFor(get(urlEqualTo("/"))
                                    .willReturn(aResponse()
                                                           .withStatus(200)
                                                           .withHeader("Content-Type", "application/xml")
                                                           .withBody(toXML(response, KieServerInfo.class, ServiceResponse.class))));

        // case mock response
        ProcessDefinitionList caseResponse = new ProcessDefinitionList();
        caseResponse.setProcesses(new ProcessDefinition[]{
                                                          new ProcessDefinition()
        });
        stubFor(get(urlMatching("/queries/cases/processes.*"))
                                                              .willReturn(aResponse()
                                                                                     .withStatus(200)
                                                                                     .withHeader("Content-Type", "application/xml")
                                                                                     .withBody(toXML(caseResponse, ProcessDefinitionList.class, ProcessDefinition.class))));

        // document mock response
        DocumentInstance documentResponse = new DocumentInstance();
        documentResponse.setIdentifier("1234");
        stubFor(get(urlMatching("/documents/1234"))
                                                   .willReturn(aResponse()
                                                                          .withStatus(200)
                                                                          .withHeader("Content-Type", "application/xml")
                                                                          .withBody(toXML(documentResponse, DocumentInstance.class))));

        // job service mock response
        RequestInfoInstanceList jobResponse = new RequestInfoInstanceList();
        stubFor(get(urlMatching("/jobs.*"))
                                           .willReturn(aResponse()
                                                                  .withStatus(200)
                                                                  .withHeader("Content-Type", "application/xml")
                                                                  .withBody(toXML(jobResponse,
                                                                                  RequestInfoInstanceList.class))));

        // query service mock response
        NodeInstanceList queryResponse = new NodeInstanceList();
        stubFor(get(urlMatching("/queries/processes/instances/100/nodes/instances.*"))
                                                                                      .willReturn(aResponse()
                                                                                                             .withStatus(200)
                                                                                                             .withHeader("Content-Type", "application/xml")
                                                                                                             .withBody(toXML(queryResponse,
                                                                                                                             NodeInstanceList.class))));
        // solver service mock response
        SolverInstanceList solverResponse = new SolverInstanceList();
        stubFor(get(urlMatching("/containers/my-container/solvers"))
                                                                    .willReturn(aResponse()
                                                                                           .withStatus(200)
                                                                                           .withHeader("Content-Type", "application/xml")
                                                                                           .withBody(toXML(solverResponse,
                                                                                                           SolverInstanceList.class))));

        // ui service mock response
        stubFor(get(urlMatching("/containers/my-container/forms/processes/my-process.*"))
                                                                                         .willReturn(aResponse()
                                                                                                                .withStatus(200)
                                                                                                                .withHeader("Content-Type", "application/xml")
                                                                                                                .withBody("my form")));
        // user task mock response
        TaskSummaryList userTaskResponse = new TaskSummaryList();
        stubFor(get(urlMatching("/queries/tasks/instances.*"))
                                                              .willReturn(aResponse()
                                                                                     .withStatus(200)
                                                                                     .withHeader("Content-Type", "application/xml")
                                                                                     .withBody(toXML(userTaskResponse, TaskSummaryList.class))));
        return new RouteBuilder() {

            @Override
            public void configure() {
                from("direct:start")
                                    .to("kie:" + getAuthenticadUrl("admin", "admin"))
                                    .to("mock:result");
            }
        };
    }

    private String toXML(Object pojo, Class<?>... classes) throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(Arrays.asList(classes)), MarshallingFormat.JAXB, getClass().getClassLoader());
        return marshaller.marshall(pojo);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        KieComponent kieComponent = new KieComponent();
        kieComponent.getConfiguration()
                    .clearBodyParams()
                    .setBodyParam("case", "findProcesses", "page")
                    .setBodyParam("case", "findProcesses", "pageSize");

        context.addComponent("kie", kieComponent);
        return context;
    }

}
