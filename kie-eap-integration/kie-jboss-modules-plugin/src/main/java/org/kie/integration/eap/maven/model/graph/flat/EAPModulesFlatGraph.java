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
package org.kie.integration.eap.maven.model.graph.flat;

import org.kie.integration.eap.maven.model.dependency.*;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPArtifactOptionalResource;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.model.resource.EAPUnresolvableArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPVersionMismatchedArtifactResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;

import java.util.*;

/**
 * This kind of module graph considers:
 * - No module dependencies are NOT exported (except the static ones forced to export in the module definition)
 * - So each module depends on all modules as its model dependencies are.
 */
public class EAPModulesFlatGraph implements EAPModulesGraph {

    public static final String DEPENDENCY_SEPARATOR = "-_-";

    private List<EAPModuleGraphNode> nodes;

    private String distributionName;
    private String printedGraph;

    // Statistics.
    private static int totalResources;
    private static int unresolvableResources;
    private static int totalDependencies;
    private static int staticDependencies;
    private static int missingDependencies;
    private static int missingOptionalDependencies;
    private static int eapDependencies;
    private static int optionalDependencies;
    private static int versionMismatchedResources;
    
    public EAPModulesFlatGraph(String distributionName, EAPLayer layer) {
        this.distributionName = distributionName;

        // Remove dead modules (no other one depedens on them).
        Collection<EAPModule> _modules = getModulesWithResources(layer.getModules());
        if (_modules != null && !_modules.isEmpty()) {
            nodes = new LinkedList<EAPModuleGraphNode>();
            for (EAPModule module : _modules) {
                nodes.add(new EAPModuleGraphFlatNode(module));
            }
        }
    }

    @Override
    public List<EAPModuleGraphNode> getNodes() {
        return nodes;
    }

    @Override
    public EAPModuleGraphNode getNode(String nodeUID) {
        for (EAPModuleGraphNode node : nodes) {
            if (node.getUniqueId().equalsIgnoreCase(nodeUID)) return node;
        }
        return null;
    }

    public String getDistributionName() {
        return distributionName;
    }

    /**
     * Obtain the modules that someother one depends on it.
     *
     * @param modules The modules list.
     * @return Usable modules to create for the distribution.
     */
    protected Collection<EAPModule> getModulesReferenced(Collection<EAPModule> modules) {
        Collection<EAPModule> result = null;

        if (modules != null) {
            Set<String> list = new HashSet<String>();
            result = new LinkedList<EAPModule>();
            for (EAPModule module : modules) {
                Collection<EAPModuleDependency> dependencies = module.getDependencies();
                if (dependencies != null) {
                    Iterator<EAPModuleDependency> it = dependencies.iterator();
                    while (it.hasNext()) {
                        EAPModuleDependency next = it.next();
                        if (!(next instanceof EAPModuleMissingDependency)) {
                            list.add(new StringBuilder(next.getName()).append(DEPENDENCY_SEPARATOR).
                                    append(next.getSlot()).toString());
                        }
                    }

                }
            }
            for (EAPModule module : modules) {
                String key = new StringBuilder(module.getName()).append(DEPENDENCY_SEPARATOR).
                        append(module.getSlot()).toString();
                if (list.contains(key)) result.add(module);
            }
        }

        return result;
    }

    /**
     * Obtain the modules that someother one depends on it.
     *
     * @param modules The modules list.
     * @return Usable modules to create for the distribution.
     */
    protected Collection<EAPModule> getModulesWithResources(Collection<EAPModule> modules) {
        Collection<EAPModule> result = null;

        if (modules != null) {
            result = new LinkedList<EAPModule>();
            for (EAPModule module : modules) {
                Collection<EAPModuleResource> dependencies = module.getResources();
                if (dependencies != null) {
                    boolean resolvable = false;
                    Iterator<EAPModuleResource> it = dependencies.iterator();
                    while (it.hasNext()) {
                        EAPModuleResource next = it.next();
                        if (!(next instanceof EAPUnresolvableArtifactResource)) resolvable = true;
                    }

                    if (resolvable) result.add(module);
                    // TODO: If all module resources not resolvable -> Log.WARN
                }
            }
        }

        return result;
    }

    @Override
    public String print() {
        if (printedGraph != null) return printedGraph;

        // Print in alphabetic sort.
        List<EAPModuleGraphNode> sortedModules = new ArrayList<EAPModuleGraphNode>(nodes);
        Collections.sort(sortedModules);
        StringBuilder nodesPrint = new StringBuilder();
        if (nodes != null) {
            for (EAPModuleGraphNode node : sortedModules ) {
                nodesPrint.append(node.print());
            }
        }

        StringBuilder result = new StringBuilder(EAPConstants.NEW_LINE);
        result.append("********************************************************************************************").append(EAPConstants.NEW_LINE);
        result.append("Dependency graph for " + distributionName).append(EAPConstants.NEW_LINE);
        result.append("Graph type: FLAT - No module dependencies are exported (export=false)").append(EAPConstants.NEW_LINE);
        result.append("Total modules: ").append(nodes.size()).append(EAPConstants.NEW_LINE);
        result.append("Total resources: ").append(totalResources).append(EAPConstants.NEW_LINE);
        result.append("Total unresolvable resources: ").append(unresolvableResources).append(EAPConstants.NEW_LINE);
        result.append("Total version mismatched resources: ").append(versionMismatchedResources).append(EAPConstants.NEW_LINE);
        result.append("Total dependencies: ").append(totalDependencies).append(EAPConstants.NEW_LINE);
        result.append("Total static dependencies: ").append(staticDependencies).append(EAPConstants.NEW_LINE);
        result.append("Total missing dependencies: ").append(missingDependencies).append(EAPConstants.NEW_LINE);
        result.append("Total missing optional dependencies: ").append(missingOptionalDependencies).append(EAPConstants.NEW_LINE);
        result.append("Total EAP/AS dependencies: ").append(eapDependencies).append(EAPConstants.NEW_LINE);
        result.append("Total optional dependencies: ").append(optionalDependencies).append(EAPConstants.NEW_LINE);
        result.append("********************************************************************************************").append(EAPConstants.NEW_LINE);
        result.append(nodesPrint).append(EAPConstants.NEW_LINE);
        result.append("********************************************************************************************").append(EAPConstants.NEW_LINE);

        return printedGraph = result.toString();
    }

    public static String print(EAPModule module) {
        StringBuilder result = new StringBuilder();
        result.append("+ ").append(module.getUniqueId()).append(EAPConstants.NEW_LINE);

        // Iterate over all module resources.
        if (module.getResources() != null && !module.getResources().isEmpty()) {

            // Alphabetic sort.
            List<EAPModuleResource> sortedResources = new ArrayList<EAPModuleResource>(module.getResources());
            Collections.sort(sortedResources, new ResourceComparator());

            for (EAPModuleResource resource : sortedResources) {
                result.append(print(resource));
            }
        }

        // Iterate over all module dependencies.
        if (module.getDependencies() != null && !module.getDependencies().isEmpty()) {

            // Alphabetic sort.
            List<EAPModuleDependency> sortedDependencies = new ArrayList<EAPModuleDependency>(module.getDependencies());
            Collections.sort(sortedDependencies, new DependencyComparator());

            for (EAPModuleDependency dependency : sortedDependencies) {
                result.append(print(dependency));
            }
        }

        return result.toString();
    }

    public static String print(EAPModuleResource resource) {
        StringBuilder result = new StringBuilder("| +- ");
        result.append(resource.getName());

        if (resource instanceof EAPUnresolvableArtifactResource) {
            result.append(" (UNRESOLVABLE) ");
            unresolvableResources++;
        } if (resource instanceof EAPVersionMismatchedArtifactResource) {
            result.append(" (VERSION MISMATCH from '" +  ((EAPVersionMismatchedArtifactResource)resource).getVersion() + "') ");
            versionMismatchedResources++;
        } else if (resource instanceof EAPArtifactOptionalResource) {
            result.append(" (OPTIONAL) ");
        }

        totalResources++;
        result.append(EAPConstants.NEW_LINE);
        return result.toString();
    }

    public static String print(EAPModuleDependency dependency) {
        StringBuilder result = new StringBuilder("| +-> ");
        result.append(EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot()));
        
        String isOptional = dependency.isOptional() ? ",OPTIONAL" : "";
        String isExported = dependency.isExport() ? ",EXPORTED" : "";
        
        if (dependency instanceof EAPBaseModuleDependency) {
            result.append(" (EAP").append(isExported).append(isOptional).append(")");
            eapDependencies++;
        } else if (dependency instanceof EAPStaticDistributionModuleDependency) {
            result.append(" (DISTIBUTION-STATIC").append(isExported).append(isOptional).append(")");
            staticDependencies++;
        } else if (dependency instanceof EAPStaticModuleDependency) {
            result.append(" (STATIC").append(isExported).append(isOptional).append(")");
            staticDependencies++;
        } else if (dependency instanceof EAPModuleMissingDependency) {
            result.append(" (MISSING").append(isExported).append(isOptional).append(")");
            if (!dependency.isOptional()) missingDependencies++;
            else missingOptionalDependencies++;
        } else if (dependency instanceof EAPCustomModuleDependency) {
            if (dependency.isOptional()) result.append(" (OPTIONAL)");
            if (dependency.isExport()) result.append(" (EXPORTED)");
            optionalDependencies++;
        }

        totalDependencies++;
        result.append(EAPConstants.NEW_LINE);
        return result.toString();
    }

    private static class ResourceComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            EAPModuleResource m1 = (EAPModuleResource) o1;
            EAPModuleResource m2 = (EAPModuleResource) o2;
            return m1.getName().compareTo(m2.getName());
        }
    }

    private static class DependencyComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            EAPModuleDependency m1 = (EAPModuleDependency) o1;
            EAPModuleDependency m2 = (EAPModuleDependency) o2;
            return m1.getName().compareTo(m2.getName());
        }
    }
}
