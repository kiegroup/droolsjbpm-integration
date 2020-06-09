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
import java.util.HashSet;
import java.util.List;

import org.drools.core.impl.InternalKieContainer;
import org.kie.internal.identity.IdentityProvider;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.KieServerController;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.InmutableContainerStartupStrategy;
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
    protected InternalKieContainer createInternalKieContainer(String containerId, ReleaseId releaseId, KieModuleMetaData metaData) {
        return (InternalKieContainer) ks.newKieClasspathContainer(containerId, metaData.getClassLoader(), releaseId);
    }

    @Override
    protected KieContainerInstanceImpl createContainerInstanceImpl(String containerId, ReleaseId releaseId) {
        return new InmutableSpringBootKieContainerInstanceImpl(containerId, KieContainerStatus.CREATING, null, releaseId, this);
    }

    private class SpringBootKieServerController implements KieServerController {

        @Override
        public void disconnect(KieServerInfo serverInfo) {
            // immutable container cannot be disconnected
        }

        @Override
        public KieServerSetup connect(KieServerInfo serverInfo) {
            KieServerSetup serverSetup = new KieServerSetup();
            serverSetup.setContainers(new HashSet<>(containers));
            return serverSetup;
        }
    }

    @Override
    public KieServerController getController() {
        return new SpringBootKieServerController();
    }

    @Override
    public void init() {
        init(new InmutableContainerStartupStrategy());
    }

}
