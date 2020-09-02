/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot;

import java.util.Collections;
import java.util.List;

import org.appformer.maven.support.DependencyFilter;
import org.drools.core.impl.InternalKieContainer;
import org.kie.internal.identity.IdentityProvider;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.ImmutableContainerStartupStrategy;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.storage.memory.InMemoryKieServerStateRepository;

public class ImmutableSpringBootKieServerImpl extends SpringBootKieServerImpl {

    private List<KieContainerResource> containers;
    
    public ImmutableSpringBootKieServerImpl(List<KieServerExtension> extensions, IdentityProvider identityProvider) {
        this(extensions, identityProvider, Collections.emptyList());
    }

    public ImmutableSpringBootKieServerImpl(List<KieServerExtension> extensions, IdentityProvider identityProvider, List<KieContainerResource> containers) {
        super(extensions, identityProvider, new InMemoryKieServerStateRepository());
        this.containers = containers;
    }

    @Override
    protected KieModuleMetaData buildKieModuleMetadata(org.kie.api.builder.ReleaseId releaseId) {
        return KieModuleMetaData.Factory.newInJarKieModuleMetaData(releaseId, DependencyFilter.COMPILE_FILTER);
    }

    @Override
    protected InternalKieContainer createInternalKieContainer(String containerId, ReleaseId releaseId, KieModuleMetaData metaData) {
        return (InternalKieContainer) ks.newKieClasspathContainer(containerId, metaData.getClassLoader(), releaseId);
    }

    @Override
    protected KieContainerInstanceImpl createContainerInstanceImpl(String containerId, ReleaseId releaseId) {
        return new ImmutableSpringBootKieContainerInstanceImpl(containerId, KieContainerStatus.CREATING, null, releaseId, this);
    }

    @Override
    public void init() {
        init(new ImmutableContainerStartupStrategy(containers));
    }

}
