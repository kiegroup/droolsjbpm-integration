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
package org.kie.integration.eap.maven.builder;

import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.exception.EAPModulesDependencyBuilderException;
import org.kie.integration.eap.maven.model.dependency.EAPCustomModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleMissingDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Component( role = EAPModulesDependencyBuilder.class )
public class EAPStaticModulesDependencyBuilderImpl implements EAPModulesDependencyBuilder {

    protected EAPLayer layer;
    protected EAPArtifactsHolder artifactsHolder;
    
    @Override
    public void build(EAPLayer layer, DependencyNode rootNode, EAPArtifactsHolder artifactsHolder) throws EAPModulesDependencyBuilderException {
        if (rootNode == null) throw new EAPModulesDependencyBuilderException("Root node not set.");
        List<DependencyNode> rootChildren = rootNode.getChildren();
        if (rootChildren == null || rootChildren.isEmpty()) throw new EAPModulesDependencyBuilderException("Root node does not have child nodes.");

        this.layer = layer;
        this.artifactsHolder = artifactsHolder;

        Collection<DependencyNode> parsedNodes = new LinkedList<DependencyNode>();
        visitNode(null, null, rootChildren, parsedNodes);
    }

    protected boolean isNodeParsed(Collection<DependencyNode> parsedNodes, DependencyNode node) {
        for (DependencyNode n : parsedNodes) {
            if (n == node) return true;
        }
        return false;
    }

    protected void visitNode(EAPModule module, EAPModule parent, List<DependencyNode> children, Collection<DependencyNode> parsedNodes) throws EAPModulesDependencyBuilderException {

        if (children != null && !children.isEmpty()) {
            for (DependencyNode node : children) {
                if (!isNodeParsed(parsedNodes, node)) visitNode(module ,parent, node, parsedNodes);
            }
        }
    }

    protected void visitModuleNode(EAPModule module, EAPModule parent, List<DependencyNode> children, Collection<DependencyNode> parsedNodes) throws EAPModulesDependencyBuilderException {

        // System.out.println("Visiting module: " + module.getName());

        if (children != null && !children.isEmpty()) {
            for (DependencyNode node : children) {
                // First level children are the module resources. The dependencies are found in level > 1.
                List<DependencyNode> _children = node.getChildren();
                if (_children != null && !_children.isEmpty()) {
                    for (DependencyNode _node : _children) {
                        if (!isNodeParsed(parsedNodes, node)) visitNode(module , parent, _node, parsedNodes);
                    }
                }
            }
        }
    }

    protected void visitNode(EAPModule module, EAPModule parent, DependencyNode node, Collection<DependencyNode> parsedNodes) throws EAPModulesDependencyBuilderException {

        if (isNodeParsed(parsedNodes, node)) return;

        Dependency dependency = node.getDependency();
        if (dependency == null) throw new EAPModulesDependencyBuilderException("Dependency cannot be null.");

        Artifact artifact = dependency.getArtifact();
        if (artifact == null) throw new EAPModulesDependencyBuilderException("Artifact cannot be null.");

        // System.out.println("Visiting node: " + EAPArtifactUtils.getArtifactCoordinates(artifact));

        parsedNodes.add(node);

        // Check if the artifact is inside a loaded static module.
        EAPModule m = layer.getModule(artifact);

        if (m != null) {
            // The parent module.
            if (parent == null) parent = m;

            // Artifact is contained in an static module.
            createDependency(module, m, dependency.isOptional());

            // Visit child nodes.
            visitModuleNode(m, parent, node.getChildren(), parsedNodes);

        } else {
            // Artifact is NOT contained in an static module.

            // Find the artifact in all static modules defined.
            m = findArtifactInModule(artifact);

            if (m != null) {
                // Artifact found in an existing static module.

                createDependency(module, m, artifact, dependency.isOptional());

                visitNode(m, parent, node.getChildren(), parsedNodes);
            } else if (!EAPArtifactUtils.isArtifactExcludedInModule(module, artifact)) {
                // Artifact NOT present in any existing static module and its not an excluded resource.
                createMissingDependency(module, parent, artifact, dependency.isOptional());
            }

        }

    }

    protected EAPModule findArtifactInModule(Artifact artifact) throws EAPModulesDependencyBuilderException {
        if (artifact == null) throw new EAPModulesDependencyBuilderException("Artifact cannot be null.");

        return artifactsHolder.getModule(artifact);
    }

    protected EAPModuleDependency createDependency(EAPModule parent, EAPModule module, Artifact artifact, boolean isOptional) throws EAPModulesDependencyBuilderException {
        if (module == null) throw new EAPModulesDependencyBuilderException("Module cannot be null.");

        // No cyclic dependencies
        if (parent != null && parent.equals(module)) return null;

        // Create the dependency.
        EAPCustomModuleDependency dep = null;

        // Check if the dependency to the module is already present.
        if (parent != null) {
            dep = (EAPCustomModuleDependency) parent.getDependency(module.getUniqueId());
        }

        // No existing dependency to this module, create it.
        if (dep == null) {
            // dep = new EAPCustomModuleDependency(module.getName());
            dep = (EAPCustomModuleDependency) module.createDependency();
            dep.setSlot(module.getSlot());
            dep.setOptional(isOptional);
            // Add it to the parent.
            if (parent != null) {
                parent.addDependency(dep);
            }
        }

        if (artifact != null) dep.addArtifact(artifact);

        return dep;
    }

    protected EAPModuleDependency createDependency(EAPModule parent, EAPModule module, boolean isOptional) throws EAPModulesDependencyBuilderException {
        return createDependency(parent, module, null, isOptional);
    }

    protected EAPModuleMissingDependency createMissingDependency(EAPModule moodule, EAPModule parent, Artifact artifact, boolean isOptional) throws EAPModulesDependencyBuilderException {
        if (moodule == null) throw new EAPModulesDependencyBuilderException("Module cannot be null.");

        // Create the dependency.
        EAPModuleMissingDependency dep = null;

        // Check if the dependency to the module is already present.
        if (moodule != null) {
            dep = (EAPModuleMissingDependency) moodule.getDependency(EAPArtifactUtils.getArtifactCoordinates(artifact));
            if (dep == null) {
                dep = new EAPModuleMissingDependency(artifact);
                if (parent != null) dep.addModuleReference(parent);
                dep.setOptional(isOptional);
                moodule.addDependency(dep);
            }
        }

        return dep;
    }

}
