/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;

@XmlRootElement(name = "update-release-id")
@XStreamAlias( "update-release-id" )
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateReleaseIdCommand
        implements KieServerCommand {

    private static final long serialVersionUID = 1048407484222065032L;

    @XStreamAlias("container-id")
    @XmlAttribute(name = "container-id")
    private String containerId;

    @XStreamAlias("release-id")
    @XmlElement
    private ReleaseId releaseId;

    @XStreamAlias("reset-before-update")
    @XmlElement
    private boolean resetBeforeUpdate;

    public UpdateReleaseIdCommand() {
        super();
    }

    public UpdateReleaseIdCommand(String containerId, ReleaseId releaseId) {
        this(containerId, releaseId, false);
    }

    public UpdateReleaseIdCommand(String containerId, ReleaseId releaseId, boolean resetBeforeUpdate) {
        this.containerId = containerId;
        this.releaseId = releaseId;
        this.resetBeforeUpdate = resetBeforeUpdate;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public boolean isResetBeforeUpdate() {
        return resetBeforeUpdate;
    }

    public void setResetBeforeUpdate(boolean resetBeforeUpdate) {
        this.resetBeforeUpdate = resetBeforeUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateReleaseIdCommand that = (UpdateReleaseIdCommand) o;
        return resetBeforeUpdate == that.resetBeforeUpdate &&
                Objects.equals(containerId, that.containerId) &&
                Objects.equals(releaseId, that.releaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId, releaseId, resetBeforeUpdate);
    }

    @Override
    public String toString() {
        return "UpdateReleaseIdCommand{" +
               "containerId='" + containerId + '\'' +
               ", releaseId=" + releaseId +
                ", resetBeforeUpdate=" + resetBeforeUpdate +
               '}';
    }
}
