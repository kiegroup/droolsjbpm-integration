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
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPConstants;

import java.util.Collection;

public class EAPModuleGraphDistributionNodeDependency implements EAPModuleGraphNodeDependency {

    private String name;
    private String slot;
    private boolean optional;
    private boolean missing;
    private Boolean export;
    private String services;
    private String metaInf;

    public EAPModuleGraphDistributionNodeDependency(String name, String slot, boolean optional, boolean missing, Boolean export, String services, String metaInf) {
        this.name = name;
        this.slot = slot;
        this.optional = optional;
        this.missing = missing;
        this.export = export;
        this.services = services;
        this.metaInf = metaInf;
    }

    public String getName() {
        return name;
    }

    public String getSlot() {
        if (slot == null || slot.trim().length() == 0) return EAPConstants.SLOT_MAIN;
        return slot;
    }

    public boolean isOptional() {
        return optional;
    }

    public Boolean isMissing() {
        return missing;
    }

    public boolean isExport() {
        return export;
    }

    public String getServices() {
        return services;
    }

    public String getMetaInf() {
        return metaInf;
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
    public int compareTo(Object o) {
        EAPModuleGraphNodeDependency other = (EAPModuleGraphNodeDependency) o;
        return getName().compareTo(other.getName());
    }
}