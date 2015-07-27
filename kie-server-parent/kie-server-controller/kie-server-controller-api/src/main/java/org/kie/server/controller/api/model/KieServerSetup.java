/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.controller.api.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;

/**
 * Complete setup of KieServer that covers containers and its configuration
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "kie-server-setup")
public class KieServerSetup {

    @XmlElement(name = "server-config")
    private KieServerConfig serverConfig;

    @XmlElementWrapper(name = "server-containers")
    private Set<KieContainerResource> containers;

    public KieServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(KieServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public Set<KieContainerResource> getContainers() {
        if (containers == null) {
            containers = new HashSet<KieContainerResource>();
        }
        return containers;
    }

    public void setContainers(Set<KieContainerResource> containers) {
        this.containers = containers;
    }
}
