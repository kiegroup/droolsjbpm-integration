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
import org.kie.integration.eap.maven.model.dependency.EAPModuleMissingDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.util.EAPConstants;

import java.util.Collection;

public class EAPModuleGraphFlatNodeDependency implements EAPModuleGraphNodeDependency {

    private EAPModuleDependency dependency;

    public EAPModuleGraphFlatNodeDependency(EAPModuleDependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public Boolean isMissing() {
        return dependency instanceof EAPModuleMissingDependency;
    }

    @Override
    public boolean isExport() {
        return dependency.isExport();
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
        return dependency.getName();
    }

    @Override
    public String getSlot() {
        return dependency.getSlot();
    }

    @Override
    public boolean isOptional() {
        return dependency.isOptional();
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNodeDependency other = (EAPModuleGraphNodeDependency) o;
        return getName().compareTo(other.getName());
    }
}