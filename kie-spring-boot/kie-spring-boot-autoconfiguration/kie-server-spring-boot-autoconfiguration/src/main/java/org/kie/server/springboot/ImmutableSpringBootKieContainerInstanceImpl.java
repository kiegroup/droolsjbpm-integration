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

import org.drools.core.impl.InternalKieContainer;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;


public class ImmutableSpringBootKieContainerInstanceImpl extends KieContainerInstanceImpl {

    public ImmutableSpringBootKieContainerInstanceImpl(String containerId, KieContainerStatus status, InternalKieContainer kieContainer, ReleaseId releaseId, KieServerImpl kieServer) {
        super(containerId, status, kieContainer, releaseId, kieServer);
    }

    @Override
    protected boolean updateReleaseId() {
        return false;
    }

}
