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

package org.jbpm.process.svg.processor;

import org.jbpm.process.svg.model.Transformation;
import org.w3c.dom.NodeList;

public interface SVGProcessor {

    String COMPLETED_COLOR = "#C0C0C0";
    String COMPLETED_BORDER_COLOR = "#030303";
    String ACTIVE_BORDER_COLOR = "#FF0000";

    void transform(Transformation t);

    void defaultCompletedTransformation(String nodeId, String completedNodeColor, String completedNodeBorderColor);

    void defaultActiveTransformation(String nodeId, String activeNodeBorderColor);

    void defaultCompletedTransformation(String nodeId);

    void defaultActiveTransformation(String nodeId);

    void defaultSubProcessLinkTransformation(String nodeId, String link);

    String getSVG(String svgWidth, String svgHeight);

    String getSVG();

    void processNodes(NodeList nodes);
}
