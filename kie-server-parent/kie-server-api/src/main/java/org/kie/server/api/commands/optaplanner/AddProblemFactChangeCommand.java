/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;
import org.optaplanner.core.impl.solver.ProblemFactChange;

@XmlRootElement(name = "add-problem-fact-change")
@XStreamAlias("add-problem-fact-change")
@XmlAccessorType(XmlAccessType.NONE)
public class AddProblemFactChangeCommand
        implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlAttribute(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    @XmlElement
    @XStreamAlias("problem-fact-change")
    // It's not possible to use ProblemFactChange type here due to JAXB marshaller limitations
    private Object problemFactChange;

    public AddProblemFactChangeCommand() {
    }

    public AddProblemFactChangeCommand(String containerId,
                                       String solverId,
                                       ProblemFactChange problemFactChange) {
        this.containerId = containerId;
        this.solverId = solverId;
        this.problemFactChange = problemFactChange;
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

    public ProblemFactChange getProblemFactChange() {
        return (ProblemFactChange) problemFactChange;
    }

    public void setProblemFactChange(ProblemFactChange problemFactChange) {
        this.problemFactChange = problemFactChange;
    }

    @Override
    public String toString() {
        return "AddProblemFactChangeCommand{" +
                "containerId='" + containerId + '\'' +
                ", solverId='" + solverId + '\'' +
                ", problemFactChange='" + problemFactChange + '\'' +
                '}';
    }
}
