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

package org.kie.server.services.jbpm.ui;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.svg.SVGImageProcessor;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.jbpm.ui.img.ImageReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceBase.class);

    private RuntimeDataService dataService;
    private Map<String, ImageReference> imageReferenceMap;

    private KieServerRegistry registry;

    public ImageServiceBase(RuntimeDataService dataService, Map<String, ImageReference> imageReferenceMap, KieServerRegistry registry) {
        this.dataService = dataService;
        this.imageReferenceMap = imageReferenceMap;
        this.registry = registry;
    }

    private byte[] getProcessImageAsBytes(String containerId, String processId) {

        ProcessDefinition procDef = dataService.getProcessesByDeploymentIdProcessId(containerId, processId);
        if( procDef == null ) {
            throw new IllegalArgumentException("No process found for " + processId + " within container " + containerId);
        }

        String location = "";
        if (procDef.getPackageName() != null && !procDef.getPackageName().trim().isEmpty()) {
            location = procDef.getPackageName().replaceAll("\\.", "/") + "/";
        }
        // get SVG String
        byte[] imageSVG = imageReferenceMap.get(containerId).getImageContent(location, processId);
        if( imageSVG == null ) {
            logger.warn("Could not find SVG image file for process '" + processId + "' within container " + containerId);
            return null;
        }

        return imageSVG;
    }

    public String getProcessImage(String containerId, String processId) {
        containerId = registry.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());

        String imageSVGString = null;
        byte[] imageSVG = getProcessImageAsBytes(containerId, processId);
        if (imageSVG != null) {
            try {
                imageSVGString = new String(imageSVG, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.debug("UnsupportedEncodingException while building process image due to {}", e.getMessage());
            }
        }

        return imageSVGString;
    }

    public String getActiveProcessImage(String containerId, long procInstId) {
        ProcessInstanceDesc instance = dataService.getProcessInstanceById(procInstId);
        if (instance == null) {
            throw new ProcessInstanceNotFoundException("No instance found for process instance id " + procInstId);
        }
        String imageSVGString = null;
        // get SVG String
        byte[] imageSVG = getProcessImageAsBytes(instance.getDeploymentId(), instance.getProcessId());
        if (imageSVG != null) {
            // find active nodes and modify image
            Collection<NodeInstanceDesc> activeLogs = dataService.getProcessInstanceHistoryActive(procInstId, new QueryContext(0, 1000));
            Collection<NodeInstanceDesc> completedLogs = dataService.getProcessInstanceHistoryCompleted(procInstId, new QueryContext(0, 1000));
            Map<Long, String> active = new HashMap<Long, String>();
            List<String> completed = new ArrayList<String>();

            for (NodeInstanceDesc activeNode : activeLogs) {
                active.put(activeNode.getId(), activeNode.getNodeId());
            }

            for (NodeInstanceDesc completeNode : completedLogs) {
                completed.add(completeNode.getNodeId());

                active.remove(completeNode.getId());
            }

            ByteArrayInputStream svgStream = new ByteArrayInputStream(imageSVG);

            imageSVGString = SVGImageProcessor.transform(svgStream, completed, new ArrayList<String>(active.values()));

            return imageSVGString;
        }
        throw new IllegalArgumentException("No process found for " + instance.getProcessId() + " within container " + containerId);
    }
}
