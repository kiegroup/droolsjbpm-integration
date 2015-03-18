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
package org.kie.integration.eap.maven.model.resource;

import org.eclipse.aether.graph.Exclusion;

import java.util.ArrayList;
import java.util.Collection;

public abstract  class EAPAbstractResource<T> implements  EAPModuleResource<T> {

    private String name;
    private Collection<Exclusion> exclusions;

    protected EAPAbstractResource(String name) {
        this.name = name;
        exclusions = new ArrayList<Exclusion>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean addExclusion(Exclusion artifact) {
        return exclusions.add(artifact);
    }

    public Collection<Exclusion> getExclusions() {
        return exclusions;
    }
}
