/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement(name="kie-server-info")
@XStreamAlias( "kie-server-info" )
public class KieServerInfo {

    private String serverId;
    private String version;

    private String name;
    private String location;

    private List<String> capabilities;

    private List<Message> messages;

    private KieServerMode mode = KieServerMode.DEVELOPMENT;
    
    public KieServerInfo() {
        super();
    }

    public KieServerInfo(String serverId, String version) {
        super();
        this.serverId = serverId;
        this.version = version;
    }

    public KieServerInfo(String serverId, String name, String version, List<String> capabilities, String location) {
        this(serverId, name, version, capabilities, location, KieServerMode.DEVELOPMENT);
    }

    public KieServerInfo(String serverId, String name, String version, List<String> capabilities, String location, KieServerMode mode) {
        super();
        this.serverId = serverId;
        this.name = name;
        this.version = version;
        this.capabilities = capabilities;
        this.location = location;
        this.mode = mode;
    }

    @XmlElement(name="version")
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name="id")
    public String getServerId() {
        return serverId;
    }

    @XmlElement(name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @XmlElement(name="capabilities")
    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    @XmlElement(name="location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @XmlElement(name="messages")
    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @XmlElement(name="mode")
    public KieServerMode getMode() {
        return mode;
    }

    public void setMode(KieServerMode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "KieServerInfo{" +
                "serverId='" + serverId + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", capabilities=" + capabilities + '\'' +
                ", messages=" + messages + '\'' +
                ", mode=" + mode +
                '}';
    }
}
