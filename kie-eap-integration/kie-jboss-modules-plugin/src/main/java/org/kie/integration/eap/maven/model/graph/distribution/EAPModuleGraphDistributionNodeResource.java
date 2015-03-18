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
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Exclusion;

import java.util.Collection;

public class EAPModuleGraphDistributionNodeResource implements EAPModuleGraphNodeResource {

    private String name;
    private String fileName;
    private boolean addAsResource;
    private Artifact artifact;

    public EAPModuleGraphDistributionNodeResource(String name, String fileName, boolean addAsResource) {
        this.name = name;
        this.fileName = fileName;
        this.addAsResource = addAsResource;
    }

    @Override
    public Object getResource() {
        return artifact;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public Collection<Exclusion> getExclusions() {
        return null;
    }

    public boolean isAddAsResource() {
        return addAsResource;
    }

    @Override
    public PathFilter getFilter() {
        return null;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public int compareTo(Object o) {
        EAPModuleGraphNodeResource other = (EAPModuleGraphNodeResource) o;
        return getName().compareTo(other.getName());
    }
}