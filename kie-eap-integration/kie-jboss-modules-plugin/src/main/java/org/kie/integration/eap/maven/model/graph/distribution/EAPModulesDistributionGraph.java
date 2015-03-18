/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.integration.eap.maven.model.graph.distribution;

import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;

import java.util.LinkedList;
import java.util.List;

public class EAPModulesDistributionGraph implements EAPModulesGraph {

    public static final String DEPENDENCY_SEPARATOR = "-_-";

    private List<EAPModuleGraphNode> nodes;

    private String distributionName;

    public EAPModulesDistributionGraph(String distributionName) {
        this.distributionName = distributionName;
        nodes = new LinkedList<EAPModuleGraphNode>();
    }

    public boolean addNode(EAPModuleGraphNode node) {
        return nodes.add(node);
    }

    @Override
    public List<EAPModuleGraphNode> getNodes() {
        return nodes;
    }

    @Override
    public String print() {
        // TODO
        return "";
    }

    public String getDistributionName() {
        return distributionName;
    }

    @Override
    public EAPModuleGraphNode getNode(String nodeUID) {
        for (EAPModuleGraphNode node : nodes) {
            if (node.getUniqueId().equalsIgnoreCase(nodeUID)) return node;
        }
        return null;
    }
}
