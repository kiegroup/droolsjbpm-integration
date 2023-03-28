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

import static org.jbpm.process.svg.processor.SVGProcessor.ACTIVE_ASYNC_BORDER_COLOR;
import static org.jbpm.process.svg.processor.SVGProcessor.ACTIVE_BORDER_COLOR;
import static org.jbpm.process.svg.processor.SVGProcessor.COMPLETED_BORDER_COLOR;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

        List<NodeInstanceDesc> activeNodes = Arrays.asList(nodeInstanceDescActive1, nodeInstanceDescActive2);
        List<NodeInstanceDesc> finishedNodes = Arrays.asList(nodeInstanceDescCompleted1, nodeInstanceDescCompleted2);
        List<NodeInstanceDesc> fullLogs = Arrays.asList(nodeInstanceDescCompleted1, nodeInstanceDescCompleted2, nodeInstanceDescActive1, nodeInstanceDescActive2);
        when(dataService.getProcessInstanceHistoryFinished(anyLong(), any())).thenReturn(finishedNodes);
        when(dataService.getProcessInstanceHistoryActive(anyLong(), any())).thenReturn(activeNodes);
        when(dataService.getProcessInstanceFullHistory(anyLong(), any())).thenReturn(fullLogs);
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

    @Test
    public void testSvgNodesBorderColoring() throws Exception {
        String containerId = "test-container";
        String processId = "test-processId";
        long processInstanceId = 10;
        String humanTaskOne = "_C8E8A0C7-ECC6-4C28-B1FE-D6DE5999239E";
        String oneScriptNode = "_BA99E908-87C6-46C9-99ED-901DFCD1A2AA";
        String twoScriptNode = "_1C98770B-2C62-41F1-AFBF-7B138EE5270E";
        String threeScriptNode = "_D7C7D018-DC2D-4B97-BEFF-55B50F201103";
        byte[] byteArray = getInputStreamAsByteArray(ImageServiceBaseTest.class.getResourceAsStream("/testProcess.svg"));

        when(dataService.getProcessesByDeploymentIdProcessId(containerId, processId)).thenReturn(mock(ProcessDefinition.class));
        when(imageReference.getImageContent(anyString(), anyString())).thenReturn(byteArray);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(config.getConfigItemValue(anyString(), anyString())).thenReturn("");

        NodeInstanceDesc nodeInstanceDescActive = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("1", humanTaskOne, "", "HumanTask", "", 1L, null,
                                                                                                        "", 0, 1L, 3L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescCompleted1 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("2", oneScriptNode, "", "ScriptNode", "", 1L, null,
                                                                                                            "", 0, 1L, 1L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescCompleted2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("3", twoScriptNode, "", "ScriptNode", "", 1L, null,
                                                                                                            "", 0, 1L, 4L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescError = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("4", threeScriptNode, "", "ScriptNode", "", 1L, null,
                                                                                                       "", 6, 1L, 1L, "", null, 1);

        List<NodeInstanceDesc> activeNodes = Arrays.asList(nodeInstanceDescActive);
        List<NodeInstanceDesc> finishedNodes = Arrays.asList(nodeInstanceDescCompleted1, nodeInstanceDescCompleted2);
        List<NodeInstanceDesc> fullLogs = Arrays.asList(nodeInstanceDescActive, nodeInstanceDescCompleted1, nodeInstanceDescCompleted2, nodeInstanceDescError);
        when(dataService.getProcessInstanceHistoryFinished(anyLong(), any())).thenReturn(finishedNodes);
        when(dataService.getProcessInstanceHistoryActive(anyLong(), any())).thenReturn(activeNodes);
        when(dataService.getProcessInstanceFullHistory(anyLong(), any())).thenReturn(fullLogs);
        Map<String, ImageReference> imageReferenceMap = new HashMap<>();
        imageReferenceMap.put(containerId, imageReference);

        ProcessInstanceDesc processInstanceDesc = new org.jbpm.kie.services.impl.model.ProcessInstanceDesc(processInstanceId, processId, "", "", 1, containerId, null
                , "", "", "");

        when(dataService.getProcessInstanceById(processInstanceId)).thenReturn(processInstanceDesc);

        ImageServiceBase imageServiceBase = new ImageServiceBase(dataService, imageReferenceMap, kieServerRegistry);
        String processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId);

        Document svgDocument = readSVG(processImageStr);

        checkStrokeAttributeAtNode(svgDocument, humanTaskOne, ACTIVE_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, oneScriptNode, COMPLETED_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, twoScriptNode, COMPLETED_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, threeScriptNode, ACTIVE_ASYNC_BORDER_COLOR);

        String completedNodeColor = "black";
        String completedNodeBorderColor = "grey";
        String activeNodeBorderColor = "blue";
        String activeAsyncNodeBorderColor = "red";
        processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId, completedNodeColor,
                                                                 completedNodeBorderColor, activeNodeBorderColor, false,
                                                                 activeAsyncNodeBorderColor);
        svgDocument = readSVG(processImageStr);

        checkStrokeAttributeAtNode(svgDocument, humanTaskOne, activeNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, oneScriptNode, completedNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, twoScriptNode, completedNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, threeScriptNode, activeAsyncNodeBorderColor);
    }

    @Test
    public void testSignalEventSubProcess() throws Exception {
        String containerId = "test-container";
        String processId = "test-processId";
        long processInstanceId = 10;

        byte[] byteArray = getInputStreamAsByteArray(ImageServiceBaseTest.class.getResourceAsStream("/signal-event-subprocess-svg.svg"));

        when(dataService.getProcessesByDeploymentIdProcessId(containerId, processId)).thenReturn(mock(ProcessDefinition.class));
        when(imageReference.getImageContent(anyString(), anyString())).thenReturn(byteArray);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(config.getConfigItemValue(anyString(), anyString())).thenReturn("");
        final String abortNodeId = "_66CB2BD8-424B-4057-B672-F17B6D6F2AB4";
        final String completedNodeId = "_9ACAA96A-93EC-4D51-8CF0-EAB1F28F5F56";
        NodeInstanceDesc nodeInstanceDescCompleted1 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("1", completedNodeId, "", "StartNode", "", 1L, null,
                                                                                                            "", 1, 1L, 3L, "", null, 1);

        NodeInstanceDesc nodeInstanceDescAbort = new org.jbpm.kie.services.impl.model.NodeInstanceDesc("2", abortNodeId, "", "SubProcessNode", "", 1L, null,
                                                                                                       "", 2, 1L, 3L, "", null, 1);

        when(dataService.getProcessInstanceHistoryFinished(anyLong(), any())).thenReturn(Arrays.asList(nodeInstanceDescCompleted1));
        when(dataService.getProcessInstanceHistoryActive(anyLong(), any())).thenReturn(Collections.EMPTY_LIST);
        List<NodeInstanceDesc> fullLogs = Arrays.asList(nodeInstanceDescAbort, nodeInstanceDescCompleted1);
        when(dataService.getProcessInstanceFullHistory(anyLong(), any())).thenReturn(fullLogs);

        Map<String, ImageReference> imageReferenceMap = new HashMap<>();
        imageReferenceMap.put(containerId, imageReference);

        ProcessInstanceDesc processInstanceDesc = new org.jbpm.kie.services.impl.model.ProcessInstanceDesc(processInstanceId, processId, "", "", 1, containerId, null
                , "", "", "");

        when(dataService.getProcessInstanceById(processInstanceId)).thenReturn(processInstanceDesc);

        ImageServiceBase imageServiceBase = new ImageServiceBase(dataService, imageReferenceMap, kieServerRegistry);
        String processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId);

        Document svgDocument = readSVG(processImageStr);

        Element subprocessPlusIcon = svgDocument.getElementById(abortNodeId + "_subProcessReusableNormalReusableIcon");
        String onclick = subprocessPlusIcon.getAttribute("onclick");
        assertNotNull(onclick);
        assertEquals("window.open('/containers/test-container/images/processes/instances/3')", onclick);
        String style = subprocessPlusIcon.getAttribute("style");
        assertNotNull(style);
        assertEquals("cursor: pointer;", style);
    }

    @Test
    public void testSvgAsyncNodesBorderColoring() throws Exception {
        String containerId = "test-container";
        String processId = "test-processId";
        long processInstanceId = 10;

        String node_start = "_ED165B85-E65D-42A6-B0EF-8A160356271E";
        String node_self_evaluation = "_D3E17247-1D94-47D8-93AD-D645E317B736";
        String node_asyncOk = "_9C7235D4-C26C-4EB8-9724-9AAC5C02CCE5";
        String node_split = "_930D6071-9D06-42C3-946F-BA46C09EF157";
        String node_hr_evaluation = "_AB431E82-86BC-460F-9D8B-7A7617565B36";
        String node_pm_evaluation = "_E35438DF-03AF-4D7B-9DCB-30BC70E7E92E";
        String node_async_failing = "_502513E3-41BD-40AC-8C41-F32566D9FA2B";

        byte[] byteArray = getInputStreamAsByteArray(ImageServiceBaseTest.class.getResourceAsStream("/evaluation_async.svg"));

        when(dataService.getProcessesByDeploymentIdProcessId(containerId, processId)).thenReturn(mock(ProcessDefinition.class));
        when(imageReference.getImageContent(anyString(), anyString())).thenReturn(byteArray);
        when(kieServerRegistry.getConfig()).thenReturn(config);
        when(config.getConfigItemValue(anyString(), anyString())).thenReturn("");

        //async triggered again
        NodeInstanceDesc nodeInstance8_2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("8", node_async_failing, "", "AsyncEventNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 6, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance8 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("8", node_async_failing, "", "AsyncEventNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 6, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance6_end = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("6", node_pm_evaluation, "PM_evaluation", "HumanTaskNode", "evaluation_1.0.0-SNAPSHOT",
                 processInstanceId, null, "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance6_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("6", node_pm_evaluation, "PM_evaluation", "HumanTaskNode", "evaluation_1.0.0-SNAPSHOT",
                 processInstanceId, null, "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance5_end = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("5", node_split, "", "Split", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance7_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("7", node_hr_evaluation, "HR Evaluation", "HumanTaskNode", "evaluation_1.0.0-SNAPSHOT",
                 processInstanceId, null, "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance5_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("5", node_split, "", "Split", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance5_end2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("5", node_split, "", "Split", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance3_end = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("3", node_asyncOk, "AsyncOK", "ActionNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance3_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("3", node_asyncOk, "AsyncOK", "ActionNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance2 = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("2", node_asyncOk, "", "AsyncEventNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 6, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance1_end = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("1", node_self_evaluation, "Self Evaluation", "HumanTaskNode", "evaluation_1.0.0-SNAPSHOT",
                 processInstanceId, null, "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance1_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("1", node_self_evaluation, "Self Evaluation", "HumanTaskNode", "evaluation_1.0.0-SNAPSHOT",
                 processInstanceId, null, "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance0_end = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("0", node_start, "", "StartNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 1, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);
        NodeInstanceDesc nodeInstance0_start = new org.jbpm.kie.services.impl.model.NodeInstanceDesc
                ("0", node_start, "", "StartNode", "evaluation_1.0.0-SNAPSHOT", processInstanceId, null,
                 "", 0, null, null, "evaluation_1.0.0-SNAPSHOT", null, 1);

        List<NodeInstanceDesc> activeNodes = Arrays.asList(nodeInstance7_start);
        List<NodeInstanceDesc> finishedNodes = Arrays.asList(nodeInstance0_end, nodeInstance1_end, nodeInstance3_end,
                                                              nodeInstance5_end, nodeInstance5_end2, nodeInstance6_end);
        List<NodeInstanceDesc> fullLogs = Arrays.asList(nodeInstance0_end, nodeInstance0_start, nodeInstance1_start,
                                                        nodeInstance1_end, nodeInstance2, nodeInstance3_end,
                                                        nodeInstance3_start, nodeInstance5_end, nodeInstance5_end2,
                                                        nodeInstance5_start, nodeInstance6_end, nodeInstance6_start,
                                                        nodeInstance7_start, nodeInstance8, nodeInstance8_2);
        when(dataService.getProcessInstanceHistoryFinished(anyLong(), any())).thenReturn(finishedNodes);
        when(dataService.getProcessInstanceHistoryActive(anyLong(), any())).thenReturn(activeNodes);
        when(dataService.getProcessInstanceFullHistory(anyLong(), any())).thenReturn(fullLogs);
        Map<String, ImageReference> imageReferenceMap = new HashMap<>();
        imageReferenceMap.put(containerId, imageReference);

        ProcessInstanceDesc processInstanceDesc = new org.jbpm.kie.services.impl.model.ProcessInstanceDesc(processInstanceId, processId, "", "", 1, containerId, null
                , "", "", "");

        when(dataService.getProcessInstanceById(processInstanceId)).thenReturn(processInstanceDesc);

        ImageServiceBase imageServiceBase = new ImageServiceBase(dataService, imageReferenceMap, kieServerRegistry);
        String processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId);

        Document svgDocument = readSVG(processImageStr);

        checkStrokeAttributeAtNode(svgDocument, node_hr_evaluation, ACTIVE_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, node_self_evaluation, COMPLETED_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, node_pm_evaluation, COMPLETED_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, node_async_failing, ACTIVE_ASYNC_BORDER_COLOR);
        checkStrokeAttributeAtNode(svgDocument, node_asyncOk, COMPLETED_BORDER_COLOR);

        String completedNodeColor = "black";
        String completedNodeBorderColor = "grey";
        String activeNodeBorderColor = "blue";
        String activeAsyncNodeBorderColor = "red";
        processImageStr = imageServiceBase.getActiveProcessImage(containerId, processInstanceId, completedNodeColor,
                                                                 completedNodeBorderColor, activeNodeBorderColor, false,
                                                                 activeAsyncNodeBorderColor);
        svgDocument = readSVG(processImageStr);

        checkStrokeAttributeAtNode(svgDocument, node_hr_evaluation, activeNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, node_self_evaluation, completedNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, node_pm_evaluation, completedNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, node_asyncOk, completedNodeBorderColor);
        checkStrokeAttributeAtNode(svgDocument, node_async_failing, activeAsyncNodeBorderColor);
    }

    private void checkStrokeAttributeAtNode(Document svgDocument, String nodeId, String expectedStrokeValue) {
        Element humanTaskBorderElement = svgDocument.getElementById(nodeId + "?shapeType=BORDER&renderType=STROKE");
        String stroke = humanTaskBorderElement.getAttribute("stroke");
        assertNotNull(stroke);
        assertEquals(expectedStrokeValue, stroke);
    }
}
