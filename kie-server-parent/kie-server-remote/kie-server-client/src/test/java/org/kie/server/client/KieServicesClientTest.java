/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.credentials.EnteredTokenCredentialsProvider;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class KieServicesClientTest extends BaseKieServicesClientTest {

    private static final String CONTAINER_ID = "mycontainer";

    private static final String ARTIFACT_ID = "myproject";
    private static final String GROUP_ID = "org.kie";
    private static final String VERSION = "1.2.3";

    @Test
    public void testGetServerInfo() {
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

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());
    }

    @Test
    public void testListContainers() {
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
                                "    <kie-container container-id=\"kjar2\" status=\"FAILED\"/>" +
                                "  </kie-containers>" +
                                "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        assertSuccess(response);
        assertEquals("Number of listed containers", 2, response.getResult().getContainers().size());
    }

    @Test
    public void testCreateContainer() {
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
        stubFor(put(urlEqualTo("/containers/kie1"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Container successfully deployed\">\n" +
                                "  <kie-container container-id=\"kie1\" status=\"STARTED\">\n" +
                                "    <release-id>\n" +
                                "      <group-id>org.kie.server.testing</group-id>\n" +
                                "      <artifact-id>kjar2</artifact-id>\n" +
                                "      <version>1.0-SNAPSHOT</version>\n" +
                                "    </release-id>\n" +
                                "    <resolved-release-id>\n" +
                                "      <group-id>org.kie.server.testing</group-id>\n" +
                                "      <artifact-id>kjar2</artifact-id>\n" +
                                "      <version>1.0-SNAPSHOT</version>\n" +
                                "    </resolved-release-id>\n" +
                                "  </kie-container>\n" +
                                "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "kjar2", "1.0-SNAPSHOT");
        KieContainerResource resource = new KieContainerResource("kie1", releaseId);
        ServiceResponse<KieContainerResource> response = client.createContainer("kie1", resource);
        assertSuccess(response);
        KieContainerResource container = response.getResult();
        assertEquals("Container id", "kie1", container.getContainerId());
        assertEquals("Release id", releaseId, container.getReleaseId());
        assertEquals("Resolved release Id", releaseId, container.getResolvedReleaseId());
    }

    @Test
    public void testGetServerInfoWithSingletonExtraClass() {
        stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"type\" : \"SUCCESS\",\n" +
                                "  \"msg\" : \"Kie Server info\",\n" +
                                "  \"result\" : {\n" +
                                "    \"kie-server-info\" : {\n" +
                                "      \"version\" : \"1.2.3\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")));

        config.setMarshallingFormat(MarshallingFormat.JSON);
        config.setExtraClasses(Collections.singleton(KieContainerStatus.class));
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());
    }

    @Test
    public void testGetServerInfoBasicAuth() {
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

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());

        verify(1, getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Basic bnVsbDpudWxs")));
        verify(0, getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Bearer abcdefghijk")));
    }

    @Test
    public void testGetServerInfoTokenAuth() {
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

        config.setCredentialsProvider(new EnteredTokenCredentialsProvider("abcdefghijk"));
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());

        verify(1, getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Bearer abcdefghijk")));
        verify(0, getRequestedFor(urlEqualTo("/")).withHeader("Authorization", equalTo("Basic bnVsbDpudWxs")));
    }

    @Test
    public void testAdditionalHeaderViaClient() {
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

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-KIE-First-Header", "first-value");
        headers.put("X-KIE-Second-Header", "second value");
        config.setHeaders(headers);
        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<KieServerInfo> response = client.getServerInfo();
        assertSuccess(response);
        assertEquals("Server version", "1.2.3", response.getResult().getVersion());

        verify(1, getRequestedFor(urlEqualTo("/")).withHeader("X-KIE-First-Header", equalTo("first-value")));
        verify(1, getRequestedFor(urlEqualTo("/")).withHeader("X-KIE-Second-Header", equalTo("second value")));
    }

    @Test
    public void testGetReleaseId() {
        stubFor(get(urlEqualTo("/containers/" + CONTAINER_ID + "/release-id"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Release ID for " + CONTAINER_ID + "\">\n" +
                                "  <release-id>\n" +
                                "    <artifact-id>" + ARTIFACT_ID + "</artifact-id>\n" +
                                "    <group-id>" + GROUP_ID + "</group-id>\n" +
                                "    <version>" + VERSION + "</version>\n" +
                                "  </release-id>\n" +
                                "</response>")));

        KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
        ServiceResponse<ReleaseId> response = client.getReleaseId(CONTAINER_ID);
        assertSuccess(response);

        ReleaseId releaseId = response.getResult();
        assertNotNull(releaseId);
        assertEquals("Artifact ID", ARTIFACT_ID, releaseId.getArtifactId());
        assertEquals("Group ID", GROUP_ID, releaseId.getGroupId());
        assertEquals("Version", VERSION, releaseId.getVersion());
    }

    // TODO create more tests for other operations

    private void assertSuccess(ServiceResponse<?> response) {
        assertEquals("Response type", ServiceResponse.ResponseType.SUCCESS, response.getType());
    }
}
