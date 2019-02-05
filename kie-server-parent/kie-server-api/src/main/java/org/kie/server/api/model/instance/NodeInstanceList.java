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

package org.kie.server.api.model.instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "node-instance-list")
public class NodeInstanceList implements ItemList<NodeInstance> {

    @XmlElement(name = "node-instance")
    private NodeInstance[] nodeInstances;

    public NodeInstanceList() {
    }

    public NodeInstanceList(NodeInstance[] nodeInstances) {
        this.nodeInstances = nodeInstances;
    }

    public NodeInstanceList(List<NodeInstance> nodeInstances) {
        this.nodeInstances = nodeInstances.toArray(new NodeInstance[nodeInstances.size()]);
    }

    public NodeInstance[] getNodeInstances() {
        return nodeInstances;
    }

    public void setNodeInstances(NodeInstance[] nodeInstances) {
        this.nodeInstances = nodeInstances;
    }

    @Override
    public List<NodeInstance> getItems() {
        if (nodeInstances == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(nodeInstances);
    }
}
