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

package org.kie.server.api.model;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="kie-server-state-info")
@XStreamAlias( "kie-server-state-info" )
public class KieServerStateInfo {

    @XmlElement(name="controller")
    private Set<String> controllers = new HashSet<String>();

    @XmlElement(name="config")
    private KieServerConfig configuration;

    @XmlElement(name="containers")
    private Set<KieContainerResource> containers = new HashSet<KieContainerResource>();

    public KieServerStateInfo() {

    }

    public KieServerStateInfo(Set<String> controllers, KieServerConfig configuration, Set<KieContainerResource> containers) {
        this.controllers = controllers;
        this.configuration = configuration;
        this.containers = containers;
    }

    public Set<String> getControllers() {
        return controllers;
    }

    public void setControllers(Set<String> controllers) {
        this.controllers = controllers;
    }

    public KieServerConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(KieServerConfig configuration) {
        this.configuration = configuration;
    }

    public Set<KieContainerResource> getContainers() {
        return containers;
    }

    public void setContainers(Set<KieContainerResource> containers) {
        this.containers = containers;
    }
}
