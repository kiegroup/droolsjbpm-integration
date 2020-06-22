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

package org.kie.server.services.taskassigning.planning;

public class SolverDef {

    private String containerId;
    private String groupId;
    private String artifactId;
    private String version;
    private String moveThreadCount;
    private int moveThreadBufferSize;
    private String threadFactoryClass;

    private String solverConfigResource;

    public SolverDef(String solverConfigResource) {
        this.solverConfigResource = solverConfigResource;
    }

    public SolverDef(String solverConfigResource,
                     String moveThreadCount,
                     int moveThreadBufferSize,
                     String threadFactoryClass) {
        this.solverConfigResource = solverConfigResource;
        this.moveThreadCount = moveThreadCount;
        this.moveThreadBufferSize = moveThreadBufferSize;
        this.threadFactoryClass = threadFactoryClass;
    }

    public SolverDef(String containerId,
                     String groupId,
                     String artifactId,
                     String version,
                     String solverConfigResource,
                     String moveThreadCount,
                     int moveThreadBufferSize,
                     String threadFactoryClass) {
        this.containerId = containerId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.solverConfigResource = solverConfigResource;
        this.moveThreadCount = moveThreadCount;
        this.moveThreadBufferSize = moveThreadBufferSize;
        this.threadFactoryClass = threadFactoryClass;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getSolverConfigResource() {
        return solverConfigResource;
    }

    public String getMoveThreadCount() {
        return moveThreadCount;
    }

    public int getMoveThreadBufferSize() {
        return moveThreadBufferSize;
    }

    public String getThreadFactoryClass() {
        return threadFactoryClass;
    }
}
