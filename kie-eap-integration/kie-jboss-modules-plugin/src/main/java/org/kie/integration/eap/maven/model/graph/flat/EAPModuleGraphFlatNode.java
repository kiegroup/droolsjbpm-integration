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

import org.kie.integration.eap.maven.model.common.PathFilter;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class EAPModuleGraphFlatNode implements EAPModuleGraphNode {

    /**
     * The module definitions.
     */
    private EAPModule module;

    private List<EAPModuleGraphNodeResource> resources;

    private List<EAPModuleGraphNodeDependency> dependencies;
    
    private Artifact moduleArtifact;
    
    private Properties properties;

    public EAPModuleGraphFlatNode(EAPModule module) {
        this.module = module;
        init();
    }

    private void init() {
        Collection<EAPModuleResource> resources = module.getResources();
        Collection<EAPModuleDependency> dependencies = module.getDependencies();
        
        this.moduleArtifact = module.getArtifact();
        this.properties = module.getProperties();
        
        if (resources != null && !resources.isEmpty()) {
            this.resources = new LinkedList<EAPModuleGraphNodeResource>();
            for ( EAPModuleResource resource : resources ) {
                this.resources.add(new EAPModuleGraphFlatNodeResource(resource));
            }
        }

        if (dependencies != null && !dependencies.isEmpty()) {
            this.dependencies = new LinkedList<EAPModuleGraphNodeDependency>();
            for ( EAPModuleDependency dependency : dependencies ) {
                this.dependencies.add(new EAPModuleGraphFlatNodeDependency(dependency));
            }
        }
    }

    @Override
    public String print() {
        return EAPModulesFlatGraph.print(module);
    }

    @Override
    public String getName() {
        return module.getName();
    }

    @Override
    public String getLocation() {
        return module.getLocation();
    }

    @Override
    public String getSlot() {
        return module.getSlot();
    }

    @Override
    public Artifact getArtifact() {
        return moduleArtifact;
    }

    @Override
    public String getUniqueId() {
        return module.getUniqueId();
    }

    public List<EAPModuleGraphNodeResource> getResources() {
        return resources;
    }

    public List<EAPModuleGraphNodeDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public Collection<PathFilter> getExports() {
        return null;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNode other = (EAPModuleGraphNode) o;
        return getName().compareTo(other.getName());
    }
}
