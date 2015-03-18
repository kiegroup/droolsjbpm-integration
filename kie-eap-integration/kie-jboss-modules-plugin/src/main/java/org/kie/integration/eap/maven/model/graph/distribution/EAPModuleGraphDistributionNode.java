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

import org.kie.integration.eap.maven.model.common.PathFilter;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class EAPModuleGraphDistributionNode implements EAPModuleGraphNode {

    private String name;
    private String location;
    private String slot;
    private List<EAPModuleGraphNodeResource> resources;
    private List<EAPModuleGraphNodeDependency> dependencies;
    private Artifact moduleArtifact;
    private Properties properties;

    public EAPModuleGraphDistributionNode(String name, String location, String slot) {
        this.name = name;
        this.location = location;
        this.slot = slot;
        this.resources = new LinkedList<EAPModuleGraphNodeResource>();
        this.dependencies = new LinkedList<EAPModuleGraphNodeDependency>();
        this.properties = new Properties();
    }

    public boolean addResource(EAPModuleGraphNodeResource resource) {
        return resources.add(resource);
    }

    public boolean addDependency(EAPModuleGraphNodeDependency dependency) {
        return dependencies.add(dependency);
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getSlot() {
        if (slot == null || slot.trim().length() == 0) return EAPConstants.SLOT_MAIN;
        return slot;
    }

    @Override
    public String getUniqueId() {
        return EAPArtifactUtils.getUID(getName(), getSlot());
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public List<EAPModuleGraphNodeResource> getResources() {
        return resources;
    }

    @Override
    public List<EAPModuleGraphNodeDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public Collection<PathFilter> getExports() {
        return null;
    }

    @Override
    public String print() {
        // TODO
        return "";
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNode other = (EAPModuleGraphNode) o;
        return getName().compareTo(other.getName());
    }

    public Artifact getArtifact() {
        return moduleArtifact;
    }

    public void setModuleArtifact(Artifact moduleArtifact) {
        this.moduleArtifact = moduleArtifact;
    }
}
