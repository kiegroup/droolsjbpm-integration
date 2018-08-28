/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.api.model.events;

import java.util.List;

import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;

public class ContainerSpecUpdated implements KieServerControllerEvent {

    private ServerTemplate serverTemplate;

    private ContainerSpec containerSpec;

    private List<Container> containers;

    public ContainerSpecUpdated() {
    }

    public ContainerSpecUpdated(ServerTemplate serverTemplate,
                                ContainerSpec containerSpec,
                                List<Container> containers) {
        this.serverTemplate = serverTemplate;
        this.containerSpec = containerSpec;
        this.containers = containers;
    }

    public ServerTemplate getServerTemplate() {
        return serverTemplate;
    }

    public void setServerTemplate(ServerTemplate serverTemplate) {
        this.serverTemplate = serverTemplate;
    }

    public ContainerSpec getContainerSpec() {
        return containerSpec;
    }

    public void setContainerSpec(ContainerSpec containerSpec) {
        this.containerSpec = containerSpec;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainerSpecUpdated)) {
            return false;
        }

        ContainerSpecUpdated that = (ContainerSpecUpdated) o;

        if (!getServerTemplate().equals(that.getServerTemplate())) {
            return false;
        }
        if (!getContainerSpec().equals(that.getContainerSpec())) {
            return false;
        }
        return getContainers().equals(that.getContainers());
    }

    @Override
    public int hashCode() {
        int result = getServerTemplate().hashCode();
        result = 31 * result + getContainerSpec().hashCode();
        result = 31 * result + getContainers().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ContainerSpecUpdated{" +
                "serverTemplate=" + serverTemplate +
                ", containerSpec=" + containerSpec +
                ", containers=" + containers +
                '}';
    }
}
