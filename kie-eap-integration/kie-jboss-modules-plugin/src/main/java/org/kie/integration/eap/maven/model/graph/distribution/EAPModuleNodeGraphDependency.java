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
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.util.EAPConstants;

import java.util.Collection;

/**
 * Represents a dependency to a static module.
 */
public class EAPModuleNodeGraphDependency implements EAPModuleGraphNodeDependency {

    private String name;
    private String slot;
    private Boolean export;

    public EAPModuleNodeGraphDependency(String name, String slot, Boolean export) {
        this.name = name;
        this.slot = slot;
        this.export = export;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            EAPModuleNodeGraphDependency other = (EAPModuleNodeGraphDependency) obj;
            if (getName().equalsIgnoreCase(other.getName())) return true;
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(name).append(":").append(slot).toString();
    }

    @Override
    public Boolean isMissing() {
        return false;
    }

    @Override
    public boolean isExport() {
        return export;
    }

    @Override
    public String getServices() {
        return EAPConstants.MODULE_SERVICES_IMPORT;
    }

    @Override
    public String getMetaInf() {
        return EAPConstants.MODULE_SERVICES_IMPORT;
    }

    @Override
    public Collection<PathFilter> getExports() {
        return null;
    }

    @Override
    public Collection<PathFilter> getImports() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSlot() {
        return slot;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNodeDependency other = (EAPModuleGraphNodeDependency) o;
        return getName().compareTo(other.getName());
    }
}