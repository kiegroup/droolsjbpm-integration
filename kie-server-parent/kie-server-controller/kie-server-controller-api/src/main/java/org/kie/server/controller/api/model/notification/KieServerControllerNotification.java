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

package org.kie.server.controller.api.model.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.controller.api.model.events.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.NONE)
public class KieServerControllerNotification {

    @XmlElements({
            //Kie Server Controller events
            @XmlElement(name = "server-instance-connected", type = ServerInstanceConnected.class),
            @XmlElement(name = "server-template-updated", type = ServerTemplateUpdated.class),
            @XmlElement(name = "server-template-deleted", type = ServerTemplateDeleted.class),
            @XmlElement(name = "server-instance-updated", type = ServerInstanceUpdated.class),
            @XmlElement(name = "server-instance-deleted", type = ServerInstanceDeleted.class),
            @XmlElement(name = "server-instance-disconnected", type = ServerInstanceDisconnected.class),
            @XmlElement(name = "container-spec-updated", type = ContainerSpecUpdated.class)
    })
    @JsonProperty
    private KieServerControllerEvent event;

    public KieServerControllerNotification() {
    }

    public KieServerControllerNotification(final KieServerControllerEvent event) {
        this.event = event;
    }

    public KieServerControllerEvent getEvent() {
        return event;
    }

    public void setEvent(KieServerControllerEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "KieServerControllerNotification{" +
                "event=" + event +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KieServerControllerNotification)) {
            return false;
        }

        KieServerControllerNotification that = (KieServerControllerNotification) o;

        return getEvent().equals(that.getEvent());
    }

    @Override
    public int hashCode() {
        return getEvent().hashCode();
    }
}
