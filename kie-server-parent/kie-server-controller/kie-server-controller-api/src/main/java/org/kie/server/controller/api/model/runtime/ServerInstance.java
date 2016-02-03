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

package org.kie.server.controller.api.model.runtime;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.Message;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "server-instance-details")
public class ServerInstance extends ServerInstanceKey {

    @XmlElement(name = "server-version")
    private String version;
    @XmlElement(name = "server-messages")
    private Collection<Message> messages = new ArrayList<Message>();
    @XmlElement(name = "containers")
    private Collection<Container> containers = new ArrayList<Container>();

    public ServerInstance() {

    }

    public ServerInstance( final String serverTemplateId,
            final String serverName,
            final String serverInstanceId,
            final String url,
            final String version,
            final Collection<Message> status,
            final Collection<Container> containers ) {
        super( serverTemplateId, serverName, serverInstanceId, url );
        this.version = version;
        this.messages.addAll( status );
        this.containers.addAll( containers );
    }

    public String getVersion() {
        return version;
    }

    public Collection<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<Message>();
        }
        return messages;
    }

    public Collection<Container> getContainers() {
        if (containers == null) {
            containers = new ArrayList<Container>();
        }
        return containers;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMessages(Collection<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void setContainers(Collection<Container> containers) {
        this.containers = containers;
    }

    public void addContainer(Container container) {
        this.containers.add(container);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerInstance)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ServerInstance that = (ServerInstance) o;

        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
