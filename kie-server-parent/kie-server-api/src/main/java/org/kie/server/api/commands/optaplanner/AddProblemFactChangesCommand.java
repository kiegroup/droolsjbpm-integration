/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.commands.optaplanner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;
import org.optaplanner.core.impl.solver.ProblemFactChange;

@XmlRootElement(name = "add-problem-fact-changes")
@XStreamAlias("add-problem-fact-changes")
@XmlAccessorType(XmlAccessType.NONE)
public class AddProblemFactChangesCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlAttribute(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    public AddProblemFactChangesCommand() {
    }

    @XmlElement
    @XStreamAlias("problem-fact-changes")
    // It's not possible to use ProblemFactChange list type here due to JAXB marshaller limitations
    private List<Object> problemFactChanges;

    public AddProblemFactChangesCommand(String containerId,
                                        String solverId,
                                        List<? extends ProblemFactChange> problemFactChanges) {
        this.containerId = containerId;
        this.solverId = solverId;
        this.problemFactChanges = new ArrayList<>(problemFactChanges);
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

    public List<ProblemFactChange> getProblemFactChanges() {
        if (problemFactChanges == null) {
            return null;
        }
        return problemFactChanges.stream().map(p -> (ProblemFactChange) p).collect(Collectors.toList());
    }

    public void setProblemFactChanges(List<? extends ProblemFactChange> problemFactChanges) {
        this.problemFactChanges = new ArrayList<>(problemFactChanges);
    }

    @Override
    public String toString() {
        return "AddProblemFactChangesCommand{" +
                "containerId='" + containerId + '\'' +
                ", solverId='" + solverId + '\'' +
                ", problemFactChanges='" + problemFactChanges + '\'' +
                '}';
    }
}
