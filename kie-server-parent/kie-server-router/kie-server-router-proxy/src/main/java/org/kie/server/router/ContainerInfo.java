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

package org.kie.server.router;

public class ContainerInfo {

    private String containerId;
    private String alias;
    private String releaseId;

    public ContainerInfo(String containerId, String alias, String releaseId) {
        this.containerId = containerId;
        this.alias = alias;
        this.releaseId = releaseId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainerInfo)) {
            return false;
        }

        ContainerInfo that = (ContainerInfo) o;

        if (alias != null ? !alias.equals(that.alias) : that.alias != null) {
            return false;
        }
        if (containerId != null ? !containerId.equals(that.containerId) : that.containerId != null) {
            return false;
        }
        if (releaseId != null ? !releaseId.equals(that.releaseId) : that.releaseId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = containerId != null ? containerId.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (releaseId != null ? releaseId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContainerInfo{" +
                "containerId='" + containerId + '\'' +
                ", alias='" + alias + '\'' +
                ", releaseId='" + releaseId + '\'' +
                '}';
    }
}
