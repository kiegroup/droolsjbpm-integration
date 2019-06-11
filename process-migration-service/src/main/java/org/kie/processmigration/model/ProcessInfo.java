/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.processmigration.model;

import java.util.ArrayList;

public class ProcessInfo {

    private ArrayList<BpmNode> nodes;
    private String containerId;
    private String processId;
    private ArrayList<String> values;
    private ArrayList<String> labels;
    private String svgFile;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public ArrayList<BpmNode> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<BpmNode> nodes) {
        this.nodes = nodes;
    }

    public String getSvgFile() {
        return svgFile;
    }

    public void setSvgFile(String svgFile) {
        this.svgFile = svgFile;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public String toString() {
        return "ProcessInfo [nodes=" + nodes
                + ", containerId=" + containerId + ", processId=" + processId
                + ", values=" + values + ", labels=" + labels
                + "]";
    }
}
