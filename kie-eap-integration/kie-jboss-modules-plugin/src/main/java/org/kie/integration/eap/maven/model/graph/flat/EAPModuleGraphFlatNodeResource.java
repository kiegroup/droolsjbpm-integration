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
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.resource.EAPArtifactOptionalResource;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.model.resource.EAPUnresolvableArtifactResource;
import org.eclipse.aether.graph.Exclusion;

import java.util.Collection;

public class EAPModuleGraphFlatNodeResource implements EAPModuleGraphNodeResource {

    private EAPModuleResource resource;

    public EAPModuleGraphFlatNodeResource(EAPModuleResource resource) {
        this.resource = resource;
    }

    @Override
    public Object getResource() {
        return resource.getResource();
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public String getFileName() {
        return resource.getFileName();
    }

    @Override
    public Collection<Exclusion> getExclusions() {
        return null;
    }

    @Override
    public boolean isAddAsResource() {
        if (resource instanceof EAPUnresolvableArtifactResource || resource instanceof EAPArtifactOptionalResource) return false;
        return true;
    }

    @Override
    public PathFilter getFilter() {
        return null;
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNodeResource other = (EAPModuleGraphNodeResource) o;
        return getName().compareTo(other.getName());
    }
}