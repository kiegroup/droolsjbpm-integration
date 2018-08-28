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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.controller.api.model.runtime.ServerInstanceKey;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "server-template-details")
public class ServerTemplate extends ServerTemplateKey {

    @XmlElement(name = "container-specs")
    private Collection<ContainerSpec> containersSpec = new ArrayList<ContainerSpec>();
    @XmlElement(name = "server-config")
    private Map<Capability, ServerConfig> configs = new HashMap<Capability, ServerConfig>();
    @XmlElement(name = "server-instances")
    private Collection<ServerInstanceKey> serverInstances = new ArrayList<ServerInstanceKey>();
    @XmlElement(name="capabilities")
    private List<String> capabilities = new ArrayList<String>();

    public ServerTemplate() {
    }

    public ServerTemplate( final String id,
            final String name ) {
        super( id, name );
    }

    public ServerTemplate( final String id,
            final String name,
            final Collection<String> capabilities,
            final Map<Capability, ServerConfig> configs,
            final Collection<ContainerSpec> containersSpec ) {
        super( id, name );
        this.capabilities.addAll( capabilities );
        this.configs.putAll( configs );
        this.containersSpec.addAll(containersSpec);
    }

    public ServerTemplate( final String id,
            final String name,
            final Collection<String> capabilities,
            final Map<Capability, ServerConfig> configs,
            final Collection<ContainerSpec> containersSpec,
            final Collection<ServerInstanceKey> serverIntanceKeys ) {
        this( id, name, capabilities, configs, containersSpec );
        this.serverInstances.addAll(serverIntanceKeys);
    }

    public Map<Capability, ServerConfig> getConfigs() {
        if (configs == null) {
            configs = new HashMap<Capability, ServerConfig>();
        }
        return new HashMap<Capability, ServerConfig>( configs );
    }

    public Collection<ContainerSpec> getContainersSpec() {
        if (containersSpec == null) {
            containersSpec = new ArrayList<ContainerSpec>();
        }
        //Errai doesn't play nice with unmod collection
        return new ArrayList<ContainerSpec>( containersSpec );
    }

    public Collection<ServerInstanceKey> getServerInstanceKeys() {
        if (serverInstances == null) {
            serverInstances = new ArrayList<ServerInstanceKey>();
        }
        return new ArrayList<ServerInstanceKey>( serverInstances );
    }

    public boolean hasContainerSpec(String containerSpecId) {
        for (ContainerSpec spec : getContainersSpec()) {
            if (containerSpecId.equals(spec.getId())) {
                return true;
            }
        }
        return false;
    }

    public ContainerSpec getContainerSpec(String containerSpecId) {
        for (ContainerSpec spec : getContainersSpec()) {
            if (containerSpecId.equals(spec.getId())) {
                return spec;
            }
        }

        return null;
    }

    public void addContainerSpec(ContainerSpec containerSpec) {
        if (containersSpec == null) {
            containersSpec = new ArrayList<ContainerSpec>();
        }
        containersSpec.add(containerSpec);
    }

    public void deleteContainerSpec(String containerSpecId) {
        if (containersSpec == null) {
            return;
        }
        Iterator<ContainerSpec> iterator = containersSpec.iterator();

        while(iterator.hasNext()) {
            ContainerSpec spec = iterator.next();
            if (containerSpecId.equals(spec.getId())) {
                iterator.remove();
            }
        }
    }

    public boolean hasServerInstance(String serverInstanceUrl) {
        for (ServerInstanceKey spec : getServerInstanceKeys()) {
            if (serverInstanceUrl.equals(spec.getUrl())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasServerInstanceId(String serverInstanceId) {
        for (ServerInstanceKey instance : getServerInstanceKeys()) {
            if (instance.getServerInstanceId().equals(serverInstanceId)) {
                return true;
            }
        }
        return false;
    }

    public ServerInstanceKey getServerInstance(String serverInstanceId) {
        for (ServerInstanceKey instance : getServerInstanceKeys()) {
            if (instance.getServerInstanceId().equals(serverInstanceId)) {
                return instance;
            }
        }
        return null;
    }

    public void addServerInstance(ServerInstanceKey serverInstance) {
        if (serverInstances == null) {
            serverInstances = new ArrayList<ServerInstanceKey>();
        }
        if (!serverInstances.contains(serverInstance)) {
            serverInstances.add(serverInstance);
        }
    }

    public void deleteServerInstance(String serverInstanceId) {
        if (serverInstances == null) {
            return;
        }
        Iterator<ServerInstanceKey> iterator = serverInstances.iterator();

        while(iterator.hasNext()) {
            ServerInstanceKey serverInstanceKey = iterator.next();
            if (serverInstanceId.equals(serverInstanceKey.getServerInstanceId())) {
                iterator.remove();
            }
        }
    }

    public void setContainersSpec(Collection<ContainerSpec> containersSpec) {
        this.containersSpec = containersSpec;
    }

    public void setConfigs(Map<Capability, ServerConfig> configs) {
        this.configs = configs;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public boolean hasMatchingId(ServerTemplateKey serverTemplateKey) {
        return getId().equals(serverTemplateKey.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerTemplate)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ServerTemplate that = (ServerTemplate) o;

        if (capabilities != null ? !capabilities.equals(that.capabilities) : that.capabilities != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (capabilities != null ? capabilities.hashCode() : 0);
        return result;
    }
}
