/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.mockito.Mockito;

public class RestUtilsTest {

    private static final String CONTAINER_ID = "my-container";

    private KieServerRegistry registry;
    private HttpHeaders headers;

    private RestUtilsTest() {}

    @Before
    public void setup() {
        registry = Mockito.mock(KieServerRegistry.class);
        headers = Mockito.mock(HttpHeaders.class);
    }

    @Test
    public void buildConversationIdHeaderWithPresentHeader() {
        String conversationId = "my-conversation-id";

        List<String> requestHeaders = new ArrayList<String>();
        requestHeaders.add(conversationId);
        when(headers.getRequestHeader(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER)).thenReturn(requestHeaders);

        Header conversationIdHeader = RestUtils.buildConversationIdHeader(CONTAINER_ID, registry, headers);

        assertEquals(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER, conversationIdHeader.getName());
        assertEquals(conversationId, conversationIdHeader.getValue());
    }

    @Test
    public void buildConversationIdHeaderWithoutContainer() {
        Header conversationIdHeader = RestUtils.buildConversationIdHeader(CONTAINER_ID, registry, headers);
        assertNull(conversationIdHeader);
    }

    @Test
    public void buildConversationIdHeaderNullContainer() {
        Header conversationIdHeader = RestUtils.buildConversationIdHeader(null, registry, headers);
        assertNull(conversationIdHeader);
    }

    @Test
    public void buildConversationIdHeaderRunningContainer() {
        String kieServerId = "KieServerId";
        KieServerEnvironment.setServerId(kieServerId);

        String groupId = "org.kie";
        String artifactId = "testArtifact";
        String version = "1.0";
        ReleaseId releaseId = new ReleaseId(groupId, artifactId, version);
        KieContainerInstanceImpl containerInstanceImpl = new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.STARTED);
        containerInstanceImpl.getResource().setReleaseId(releaseId);
        when(registry.getContainer(CONTAINER_ID)).thenReturn(containerInstanceImpl);

        Header conversationIdHeader = RestUtils.buildConversationIdHeader(CONTAINER_ID, registry, headers);

        assertEquals(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER, conversationIdHeader.getName());
        assertNotNull(conversationIdHeader.getValue());
        assertTrue(conversationIdHeader.getValue().contains(groupId));
        assertTrue(conversationIdHeader.getValue().contains(artifactId));
        assertTrue(conversationIdHeader.getValue().contains(version));
        assertTrue(conversationIdHeader.getValue().contains(CONTAINER_ID));
        assertTrue(conversationIdHeader.getValue().contains(kieServerId));
    }

    @Test
    public void buildConversationIdHeaderCreatingContainer() {
        String kieServerId = "KieServerId";
        KieServerEnvironment.setServerId(kieServerId);

        KieContainerInstanceImpl containerInstanceImpl = new KieContainerInstanceImpl(CONTAINER_ID, KieContainerStatus.CREATING);
        when(registry.getContainer(CONTAINER_ID)).thenReturn(containerInstanceImpl);

        Header conversationIdHeader = RestUtils.buildConversationIdHeader(CONTAINER_ID, registry, headers);

        assertNull(conversationIdHeader);
    }
}
