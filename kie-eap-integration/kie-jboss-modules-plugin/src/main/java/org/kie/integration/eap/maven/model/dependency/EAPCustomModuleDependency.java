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
package org.kie.integration.eap.maven.model.dependency;

import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.HashSet;

/**
 * A dependency to a custom distribution static module.
 */
public class EAPCustomModuleDependency implements EAPModuleDependency {

    private String name;
    private String slot;
    private boolean optional;
    private boolean export;

    // The artifacts that are dependant.
    private Collection<Artifact> artifacts;

    public EAPCustomModuleDependency(String name) {
        this.name = name;
        this.artifacts = new HashSet<Artifact>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlot() {
        if (slot == null || slot.trim().length() == 0) return EAPConstants.SLOT_MAIN;
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Collection<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Collection<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public boolean addArtifact(Artifact artifact) {
        return this.artifacts.add(artifact);
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        try {
            EAPCustomModuleDependency dep = (EAPCustomModuleDependency) obj;
            return name.equalsIgnoreCase(dep.getName()) && slot.equalsIgnoreCase(dep.getSlot()) &&
                    isOptional() == isOptional();
        } catch (ClassCastException e) {
            return false;
        }
    }
}
