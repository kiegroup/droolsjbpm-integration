/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.springboot;

import org.kie.server.api.model.ReleaseId;


public class EmbeddedKieJar {

    private String groupId;
    private String artifactId;
    private String version;
    private String containerId;
    private String alias;

    public EmbeddedKieJar() {
    }

    public EmbeddedKieJar(String groupId, String artifactId, String version, String containerId, String alias) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.containerId = containerId;
        this.alias = alias;
    }

    public ReleaseId getReleaseId() {
        return new ReleaseId(groupId, artifactId, version);

    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    @Override
    public String toString() {
        return "Kjar{" + "groupId='" + groupId + '\'' + ", artifactId='" + artifactId + '\'' + ", version='" + version
                + '\'' + ", containerId='" + containerId + '\'' + ", alias='" + alias + '\'' + '}';
    }
}