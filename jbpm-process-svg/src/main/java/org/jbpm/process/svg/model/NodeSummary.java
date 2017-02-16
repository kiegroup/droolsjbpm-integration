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

public class NodeSummary {

    private String nodeId;
    private Element border;
    private Element borderSubProcess;
    private Element background;
    private Element subProcessLink;

    public NodeSummary(String nodeId, Element border, Element background, Element borderSubProcess, Element subProcessLink) {
        this.nodeId = nodeId;
        this.border = border;
        this.background = background;
        this.borderSubProcess = borderSubProcess;
        this.subProcessLink = subProcessLink;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Element getBorder() {
        return border;
    }

    public Element getBackground() {
        return background;
    }

    public Element getBorderSubProcess() {
        return borderSubProcess;
    }

    public Element getSubProcessLink() {
        return subProcessLink;
    }
}
