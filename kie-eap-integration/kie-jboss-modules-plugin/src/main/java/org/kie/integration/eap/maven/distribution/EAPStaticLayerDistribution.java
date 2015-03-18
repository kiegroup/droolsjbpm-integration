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
package org.kie.integration.eap.maven.distribution;

import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.kie.integration.eap.maven.util.EAPConstants;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class EAPStaticLayerDistribution {
    private String distributionName;
    private EAPLayer staticLayer;
    private EAPLayer baseLayer;
    private EAPContainer container;
    private EAPArtifactsHolder artifactsHolder;

    private EAPModulesGraph graph;
    private String printedDistro;
    private Boolean includedOptionalDependencies;

    public EAPStaticLayerDistribution(String distributionName, EAPModulesGraph graph, EAPContainer container) {
        this.container = container;
        this.distributionName = distributionName;
        this.graph = graph;
        this.includedOptionalDependencies = null;
    }

    public String print() {
        if (printedDistro != null) return printedDistro;

        StringBuilder result = new StringBuilder(EAPConstants.NEW_LINE);
        result.append("********************************************************************************************").append(EAPConstants.NEW_LINE);
        result.append("Distribution ").append(distributionName).append(EAPConstants.NEW_LINE);
        if (container != null) result.append("Base EAP: ").append(container.toString()).append(EAPConstants.NEW_LINE);
        if (baseLayer != null) result.append("Base EAP modules count: ").append(baseLayer.getModules().size()).append(EAPConstants.NEW_LINE);
        if (staticLayer != null) result.append("Static modues count: ").append(staticLayer.getModules().size()).append(EAPConstants.NEW_LINE);
        if (baseLayer != null && staticLayer != null) result.append("Total modues count: ").append(staticLayer.getModules().size() + baseLayer.getModules().size()).append(EAPConstants.NEW_LINE);
        if (includedOptionalDependencies != null && includedOptionalDependencies) result.append("Scanned optional dependencies included.").append(EAPConstants.NEW_LINE);
        else if (includedOptionalDependencies != null && !includedOptionalDependencies) result.append("Scanned optional dependencies not included.").append(EAPConstants.NEW_LINE);
        result.append("********************************************************************************************").append(EAPConstants.NEW_LINE);

        if (graph != null) result.append(graph.print());
        // if (artifactsHolder != null) result.append(printArtifactResolutionModulesMapping());
        
        // Unreferenced modules
        String unreferencendModules = printUnreferencedModules();
        if (unreferencendModules != null) result.append(unreferencendModules);
        
        return printedDistro = result.toString();
    }

    protected String printArtifactResolutionModulesMapping() {
        StringBuilder result = new StringBuilder();
        Map<String, String> mappings = artifactsHolder.getMappedCoordinates();
        if (mappings != null && !mappings.isEmpty()) {
            result.append("****************************************************************************************").append(EAPConstants.NEW_LINE);
            result.append("+++++++++++ Artifact resolution perfomed for each module ++++++++++++++++++++").append(EAPConstants.NEW_LINE);
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                String allCords = entry.getKey();
                String shortCords = entry.getValue();
                StringBuilder line = new StringBuilder();
                line.append("'").append(allCords).append("' <-> '").append(shortCords).append("'").append(EAPConstants.NEW_LINE);
                result.append(line.toString());
            }
            result.append("****************************************************************************************").append(EAPConstants.NEW_LINE);
        }

        return result.toString();
    }
    
    protected Collection<String> getReferencedModuleUIDs() {
        Collection<String> result = null;
        List<EAPModuleGraphNode> nodes = graph.getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            result = new LinkedHashSet<String>();
            for (EAPModuleGraphNode node: nodes) {
                List<EAPModuleGraphNodeDependency> dependencies = node.getDependencies();
                if (dependencies != null && !dependencies.isEmpty()) {
                    for (EAPModuleGraphNodeDependency dependency : dependencies) {
                        String depModuleUID = EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot());
                        result.add(depModuleUID);
                    }
                }
            }
        }
        return result;
    }
    
    protected String printUnreferencedModules() {
        StringBuilder result = null;

        Collection<String> referencedModuleUIDs = getReferencedModuleUIDs();
        if (referencedModuleUIDs != null && !referencedModuleUIDs.isEmpty()) {
            List<EAPModuleGraphNode> nodes = graph.getNodes();
            Collection<EAPModuleGraphNode> unreferencedModules = new LinkedHashSet<EAPModuleGraphNode>();
            for (String referencedNodeUID : referencedModuleUIDs) {
                for (EAPModuleGraphNode node: nodes) {
                    String moduleUID = node.getUniqueId();
                    if (!referencedModuleUIDs.contains(moduleUID)) unreferencedModules.add(node);
                }
            }

            if (!unreferencedModules.isEmpty()) {
                result = new StringBuilder();
                result.append("****************************************************************************************").append(EAPConstants.NEW_LINE);
                result.append("+++++++++++ Unreferenced modules ++++++++++++++++++++").append(EAPConstants.NEW_LINE);
                for (EAPModuleGraphNode node: unreferencedModules) {
                    result.append(node.getUniqueId()).append(EAPConstants.NEW_LINE);
                }
                result.append("****************************************************************************************").append(EAPConstants.NEW_LINE);
            }
        }

        return result != null ? result.toString() : null;
    }

    public void setStaticLayer(EAPLayer staticLayer) {
        this.staticLayer = staticLayer;
    }

    public void setBaseLayer(EAPLayer baseLayer) {
        this.baseLayer = baseLayer;
    }

    public void setArtifactsHolder(EAPArtifactsHolder artifactsHolder) {
        this.artifactsHolder = artifactsHolder;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public EAPLayer getStaticLayer() {
        return staticLayer;
    }

    public EAPLayer getBaseLayer() {
        return baseLayer;
    }

    public EAPModulesGraph getGraph() {
        return graph;
    }

    public Boolean getIncludedOptionalDependencies() {
        return includedOptionalDependencies;
    }

    public void setIncludedOptionalDependencies(Boolean includedOptionalDependencies) {
        this.includedOptionalDependencies = includedOptionalDependencies;
    }

    public EAPContainer getContainer() {
        return container;
    }
}
