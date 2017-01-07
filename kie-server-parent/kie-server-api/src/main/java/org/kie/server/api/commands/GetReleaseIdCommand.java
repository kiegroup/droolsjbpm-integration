/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name = "get-release-id")
@XStreamAlias("get-release-id")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetReleaseIdCommand implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    public GetReleaseIdCommand() {
    }

    public GetReleaseIdCommand(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetReleaseIdCommand that = (GetReleaseIdCommand) o;

        return containerId != null ? containerId.equals(that.containerId) : that.containerId == null;
    }

    @Override
    public int hashCode() {
        return containerId != null ? containerId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GetReleaseIdCommand{" + "containerId='" + containerId + '\'' + '}';
    }
}
