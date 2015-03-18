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
package org.kie.integration.eap.maven.model.module;

import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public abstract class EAPAbstractModule implements  EAPModule {

    private String name;
    private String location;
    private String slot;
    private EAPLayer layer;
    private Artifact artifact;
    private Set<EAPModuleResource> resources;
    private Set<EAPModuleDependency> dependencies;
    private Properties properties;

    protected EAPAbstractModule(String name, String slot) {
        this.name = name;
        this.slot = slot;
        this.resources = new HashSet<EAPModuleResource>();
        this.dependencies = new HashSet<EAPModuleDependency>();
    }

    protected EAPAbstractModule(String name, String location, String slot) {
        this.name = name;
        this.location = location;
        this.slot = slot;
        this.resources = new HashSet<EAPModuleResource>();
        this.dependencies = new HashSet<EAPModuleDependency>();
        this.properties = new Properties();
    }

    protected EAPAbstractModule(String name, String location, String slot, Properties properties) {
        this.name = name;
        this.location = location;
        this.slot = slot;
        this.resources = new HashSet<EAPModuleResource>();
        this.dependencies = new HashSet<EAPModuleDependency>();
        this.properties = properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public String getSlot() {
        if (slot == null || slot.trim().length() == 0) return EAPConstants.SLOT_MAIN;
        return slot;
    }

    @Override
    public Collection<EAPModuleResource> getResources() {
        return resources;
    }

    /**
     * Adds a module resource artifact.
     *
     * @param resource The artifact resource.
     * @return Resource added.
     */
    @Override
    public boolean addResource(EAPModuleResource resource) {
        return resources.add(resource);
    }

    @Override
    public Collection<EAPModuleDependency> getDependencies() {
        return dependencies;
    }

    public EAPLayer getLayer() {
        return layer;
    }

    public void setLayer(EAPLayer layer) {
        this.layer = layer;
    }

    @Override
    public EAPModuleDependency getDependency(String uid) {
        if (uid != null) {
            for (EAPModuleDependency dep : dependencies) {
                if (uid.equalsIgnoreCase(EAPArtifactUtils.getUID(dep.getName(), dep.getSlot()))) return dep;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        try {
            EAPModule mod = (EAPModule) obj;
            return mod.getName().equalsIgnoreCase(getName());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean addDependency(EAPModuleDependency dependency) {
        return dependencies.add(dependency);
    }

    @Override
    public String toString() {
        return getUniqueId();
    }

    @Override
    public String getUniqueId() {
        return EAPArtifactUtils.getUID(getName(), getSlot());
    }

    public Properties getProperties() {
        return properties;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }
}
