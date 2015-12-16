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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "update-release-id")
@XStreamAlias( "update-release-id" )
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateReleaseIdCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XStreamAlias("container-id")
    @XmlAttribute(name = "container-id")
    private String containerId;

    @XStreamAlias("release-id")
    @XmlElement
    private ReleaseId releaseId;

    public UpdateReleaseIdCommand() {
        super();
    }

    public UpdateReleaseIdCommand(String containerId, ReleaseId releaseId) {
        this.containerId = containerId;
        this.releaseId = releaseId;
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

    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( !(o instanceof UpdateReleaseIdCommand) ) return false;

        UpdateReleaseIdCommand that = (UpdateReleaseIdCommand) o;

        if ( containerId != null ? !containerId.equals( that.containerId ) : that.containerId != null ) return false;
        if ( releaseId != null ? !releaseId.equals( that.releaseId ) : that.releaseId != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = containerId != null ? containerId.hashCode() : 0;
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateReleaseIdCommand{" +
               "containerId='" + containerId + '\'' +
               ", releaseId=" + releaseId +
               '}';
    }
}
