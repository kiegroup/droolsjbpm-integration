/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.api.model.spec;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "container-spec-details")
public class ContainerSpec extends ContainerSpecKey  {

    @XmlElement(name = "release-id")
    private ReleaseId releasedId;
    @XmlElement(name = "configuration")
    private Map<Capability, ContainerConfig> configs = new EnumMap<>(Capability.class);
    @XmlElement(name = "status")
    private KieContainerStatus status = KieContainerStatus.STOPPED;

    public ReleaseId getReleasedId() {
        return releasedId;
    }

    public ContainerSpec() {
    }

    public ContainerSpec(ContainerSpec other) {
        super(other.getId(), other.getContainerName(), other.getServerTemplateKey());
        this.releasedId = other.releasedId;
        this.status = other.status;
        this.configs = other.getConfigs().isEmpty() ? new EnumMap<>(Capability.class) : new EnumMap<>(other.getConfigs());
    }

    public ContainerSpec( final String id,
            final String containerName,
            final ServerTemplateKey serverTemplateKey,
            final ReleaseId releasedId,
            final KieContainerStatus status,
            final Map<Capability, ContainerConfig> configs ) {
        super( id, containerName, serverTemplateKey );
        this.releasedId = releasedId;
        this.status = status;
        this.configs = configs;
    }

    public Map<Capability, ContainerConfig> getConfigs() {
        if (configs == null) {
            configs = new HashMap<>();
        }
        return configs;
    }

    public KieContainerStatus getStatus() {
        return status;
    }

    public void setStatus(KieContainerStatus status) {
        this.status = status;
    }

    public void setReleasedId(ReleaseId releasedId) {
        this.releasedId = releasedId;
    }

    public void setConfigs(Map<Capability, ContainerConfig> configs) {
        this.configs = configs;
    }

    public void addConfig(Capability capability, ContainerConfig config) {
        this.configs.put(capability, config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainerSpec)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ContainerSpec that = (ContainerSpec) o;

        if (releasedId != null ? !releasedId.equals(that.releasedId) : that.releasedId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (releasedId != null ? releasedId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContainerSpec{" +
                "releasedId=" + releasedId +
                ", configs=" + configs +
                ", status=" + status +
                "} " + super.toString();
    }

}
