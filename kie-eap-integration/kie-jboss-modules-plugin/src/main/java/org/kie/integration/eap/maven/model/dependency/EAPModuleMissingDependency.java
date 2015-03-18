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
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This class represents a dependency to an artifact that is not included in any static module.
 */
public class EAPModuleMissingDependency implements EAPModuleDependency {

    private String name;
    private String slot;
    private Artifact artifact;
    private boolean optional;
    private boolean export;
    private Collection<EAPModule> references = new LinkedHashSet<EAPModule>();

    public EAPModuleMissingDependency(Artifact artifact) {
        this.artifact = artifact;
        this.name = EAPArtifactUtils.getArtifactCoordinates(artifact);
    }

    public boolean addModuleReference(EAPModule m) {
        return references.add(m);
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

    public Artifact getArtifact() {
        return artifact;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public Collection<EAPModule> getReferences() {
        return references;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
