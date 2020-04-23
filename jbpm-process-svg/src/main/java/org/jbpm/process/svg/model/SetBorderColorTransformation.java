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

package org.jbpm.process.svg.model;

import org.w3c.dom.Element;

public class SetBorderColorTransformation extends NodeTransformation {

    private String color;

    public SetBorderColorTransformation(String nodeId, String color) {
        super(nodeId);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void transform(SVGSummary summary) {
        NodeSummary node = summary.getNode(getNodeId());
        if (node != null) {
            Element border = node.getBorder();
            if (border != null) {
                border.setAttribute("stroke", color);
                border.setAttribute("stroke-width", "2");
            }

            Element borderSubProcess = node.getBorderSubProcess();
            if (borderSubProcess != null) {
                borderSubProcess.setAttribute("stroke", color);
                borderSubProcess.setAttribute("stroke-width", "2");
            }
        }
    }

}
