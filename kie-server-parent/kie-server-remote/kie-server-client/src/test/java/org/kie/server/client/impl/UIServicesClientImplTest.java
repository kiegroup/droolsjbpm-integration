/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.impl;

import org.junit.Test;
import org.kie.server.api.rest.RestURI;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

import static org.junit.Assert.assertEquals;

public class UIServicesClientImplTest {

    @Test
    public void testCreateColorUrlParams() {
        String completedNodeColor = "#FFFFFF";
        String completedNodeBorderColor = "#C0C0C0";
        String activeNodeBorderColor = "#F0F0F0";

        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration("testServerUrl",
                                                                                         "testUSer",
                                                                                         "TestPassword");
        UIServicesClientImpl uiServicesClientImpl = new UIServicesClientImpl(configuration);

        assertEquals("", uiServicesClientImpl.createColorURLParams("", "", ""));
        assertEquals("", uiServicesClientImpl.createColorURLParams(null, null, null));

        assertEquals(RestURI.SVG_NODE_COMPLETED_COLOR + "=" + uiServicesClientImpl.encode(completedNodeColor) +
                             "&" + RestURI.SVG_NODE_COMPLETED_BORDER_COLOR + "=" + uiServicesClientImpl.encode(completedNodeBorderColor) +
                             "&" + RestURI.SVG_NODE_ACTIVE_COLOR + "=" + uiServicesClientImpl.encode(activeNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams(completedNodeColor, completedNodeBorderColor, activeNodeBorderColor));

        assertEquals(RestURI.SVG_NODE_COMPLETED_COLOR + "=" + uiServicesClientImpl.encode(completedNodeColor),
                     uiServicesClientImpl.createColorURLParams(completedNodeColor, "", ""));
        assertEquals(RestURI.SVG_NODE_COMPLETED_BORDER_COLOR + "=" + uiServicesClientImpl.encode(completedNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams("", completedNodeBorderColor, ""));
        assertEquals(RestURI.SVG_NODE_ACTIVE_COLOR + "=" + uiServicesClientImpl.encode(activeNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams("", "", activeNodeBorderColor));

        assertEquals(RestURI.SVG_NODE_COMPLETED_COLOR + "=" + uiServicesClientImpl.encode(completedNodeColor) +
                             "&" + RestURI.SVG_NODE_COMPLETED_BORDER_COLOR + "=" + uiServicesClientImpl.encode(completedNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams(completedNodeColor, completedNodeBorderColor, ""));
        assertEquals(RestURI.SVG_NODE_COMPLETED_BORDER_COLOR + "=" + uiServicesClientImpl.encode(completedNodeBorderColor) +
                             "&" + RestURI.SVG_NODE_ACTIVE_COLOR + "=" + uiServicesClientImpl.encode(activeNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams("", completedNodeBorderColor, activeNodeBorderColor));
        assertEquals(RestURI.SVG_NODE_COMPLETED_COLOR + "=" + uiServicesClientImpl.encode(completedNodeColor) +
                             "&" + RestURI.SVG_NODE_ACTIVE_COLOR + "=" + uiServicesClientImpl.encode(activeNodeBorderColor),
                     uiServicesClientImpl.createColorURLParams(completedNodeColor, "", activeNodeBorderColor));
    }
}
