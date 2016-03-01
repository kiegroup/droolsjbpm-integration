/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.commands.optaplanner;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "create-solver")
@XStreamAlias("create-solver")
@XmlAccessorType(XmlAccessType.NONE)
public class CreateSolverCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlAttribute(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    @XmlElement(name = "solver-config-file")
    @XStreamAlias("solver-config-file")
    private String solverConfigFile;

    public CreateSolverCommand() {
        super();
    }

    public CreateSolverCommand(String containerId, String solverId, String solverConfigFile) {
        this.containerId = containerId;
        this.solverId = solverId;
        this.solverConfigFile = solverConfigFile;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getSolverId() {
        return solverId;
    }

    public void setSolverId(String solverId) {
        this.solverId = solverId;
    }

    public String getSolverConfigFile() {
        return solverConfigFile;
    }

    public void setSolverConfigFile(String solverConfigFile) {
        this.solverConfigFile = solverConfigFile;
    }

    @Override
    public String toString() {
        return "CreateSolverCommand{" +
               "containerId='" + containerId + '\'' +
               ", solverId='" + solverId + '\'' +
               ", solverConfigFile='" + solverConfigFile + '\'' +
               '}';
    }
}
