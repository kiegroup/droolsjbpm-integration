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

package org.kie.server.services.jbpm.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.jbpm.ui.img.ImageReference;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class ImageServiceBaseTest {

    @Mock
    KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

    @Mock
    RuntimeDataService dataService;

    @Mock
    ImageReference imageReference;

    @Mock
    KieServerConfig config;

    @Test
    public void testHighValueSubProcessInstanceId() {

        ImageServiceBase imageServiceBase = new ImageServiceBase();
        NodeInstanceDesc node = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("9999",
                                                                                      "1234",
                                                                                      "Test node",
                                                                                      "SubProcessNode",
                                                                                      "test-deployment",
                                                                                      1001L,
                                                                                      new Date(),
                                                                                      "test-connection",
                                                                                      1,
                                                                                      2001L,
                                                                                      3001L,
                                                                                      "",
                                                                                      null,
                                                                                      null);
        Map<String, String> subProcessLinks = new HashMap<>();

        imageServiceBase.populateSubProcessLink("test", node, subProcessLinks);

        assertEquals(1, subProcessLinks.size());
        assertEquals("containers/test/images/processes/instances/3001", subProcessLinks.get("1234"));
    }

    @Test
    public void testIncludedViewBoxAtProcessImage() throws Exception {
        String containerId = "test-container";
        String processId = "test-processId";
        byte[] byteArray = getInputStreamAsByteArray(ImageServiceBaseTest.class.getResourceAsStream("/evaluation-svg.svg"));

        when(dataService.getProcessesByDeploymentIdProcessId(containerId, processId)).thenReturn(mock(ProcessDefinition.class));
        when(imageReference.getImageContent(anyString(), anyString())).thenReturn(byteArray);
        when(kieServerRegistry.getContainerId(anyString(), any(ContainerLocator.class))).thenReturn(containerId);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(config.getConfigItemValue(anyString(), anyString())).thenReturn("");

        Map<String, ImageReference> imageReferenceMap = new HashMap<>();
        imageReferenceMap.put(containerId, imageReference);

        ImageServiceBase imageServiceBase = new ImageServiceBase(dataService, imageReferenceMap, kieServerRegistry);
        String processImageStr = imageServiceBase.getProcessImage(containerId, "test-processId");

        Document svgDocument = readSVG(processImageStr);
        assertEquals("", ((Element) svgDocument.getFirstChild()).getAttribute("width"));
        assertEquals("", ((Element) svgDocument.getFirstChild()).getAttribute("height"));
        assertEquals("0 0 3000 2000", svgDocument.getFirstChild().getAttributes().getNamedItem("viewBox").getNodeValue());
    }

    private byte[] getInputStreamAsByteArray(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return buffer.toByteArray();
        } catch (IOException e) {

        }
        return null;
    }

    private Document readSVG(String svgContent) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        factory.setValidating(false);
        Document svgDocument = factory.createDocument("http://jbpm.org", new StringReader(svgContent));

        return svgDocument;
    }

    @Test
    public void testLoopSubProcess() throws Exception {
        String containerId = "test-container";
        String processId = "test-processId";
        long processInstanceId = 10;

        byte[] byteArray = getInputStreamAsByteArray(ImageServiceBaseTest.class.getResourceAsStream("/reusable-loop-svg-test-svg.svg"));

        when(dataService.getProcessesByDeploymentIdProcessId(containerId, processId)).thenReturn(mock(ProcessDefinition.class));
        when(imageReference.getImageContent(anyString(), anyString())).thenReturn(byteArray);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(config.getConfigItemValue(anyString(), anyString())).thenReturn("");
        final String nodeId = "_D2EEB1C1-4C06-4FDC-A0CA-73DFDA3A068D";

        NodeInstanceDesc nodeInstanceDescCompleted1 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("1", nodeId, "", "SubProcessNode", "", 1L, null,
                                                                                                            "", 0, 1L, 3L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescCompleted2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("2", nodeId, "", "", "", 1L, null,
                                                                                                            "", 0, 1L, 1L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescActive1 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("3", nodeId, "", "SubProcessNode", "", 1L, null,
                                                                                                         "", 0, 1L, 4L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescActive2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("4", nodeId, "", "", "", 1L, null,
                                                                                                         "", 0, 1L, 1L, "", null, 1);

        when(dataService.getProcessInstanceHistoryCompleted(anyLong(), any())).thenReturn(Arrays.asList(nodeInstanceDescCompleted1, nodeInstanceDescCompleted2));
        when(dataService.getProcessInstanceHistoryActive(anyLong(), any())).thenReturn(Arrays.asList(nodeInstanceDescActive1, nodeInstanceDescActive2));
        Map<String, ImageReference> imageReferenceMap = new HashMap<>();
        imageReferenceMap.put(containerId, imageReference);

        ProcessInstanceDesc processInstanceDesc = new org.jbpm.kie.services.impl.model.ProcessInstanceDesc(processInstanceId, processId, "", "", 1, containerId, null
                , "", "", "");

        when(dataService.getProcessInstanceById(processInstanceId)).thenReturn(processInstanceDesc);

        ImageServiceBase imageServiceBase = new ImageServiceBase(dataService, imageReferenceMap, kieServerRegistry);
        String processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId);

        Document svgDocument = readSVG(processImageStr);

        Element subprocessPlusIcon = svgDocument.getElementById(nodeId + "_subProcessReusableNormalReusableIcon");
        String onclick = subprocessPlusIcon.getAttribute("onclick");
        assertNotNull(onclick);
        assertEquals("window.open('/containers/test-container/images/processes/instances/4')", onclick);
        String style = subprocessPlusIcon.getAttribute("style");
        assertNotNull(style);
        assertEquals("cursor: pointer;", style);
    }
}
