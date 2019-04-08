/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.instance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "solver-instance")
@XStreamAlias("solver-instance")
public class SolverInstance {

    public enum SolverStatus {
        NOT_SOLVING,
        TERMINATING_EARLY,
        SOLVING
    }

    @XmlElement(name = "container-id")
    @XStreamAlias("container-id")
    private String containerId;

    @XmlElement(name = "solver-id")
    @XStreamAlias("solver-id")
    private String solverId;

    @XmlElement(name = "solver-config-file")
    @XStreamAlias("solver-config-file")
    private String solverConfigFile;

    @XmlElement(name = "status")
    @XStreamAlias("status")
    private SolverStatus status;

    @XmlElement(name = "score")
    @XStreamAlias("score")
    private ScoreWrapper scoreWrapper;

    @XmlElement(name = "best-solution")
    @XStreamAlias("best-solution")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Object bestSolution;

    public SolverInstance() {
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

    public SolverStatus getStatus() {
        return status;
    }

    public void setStatus(SolverStatus status) {
        this.status = status;
    }

    public ScoreWrapper getScoreWrapper() {
        return scoreWrapper;
    }

    public void setScoreWrapper(ScoreWrapper scoreWrapper) {
        this.scoreWrapper = scoreWrapper;
    }

    public Object getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(Object bestSolution) {
        this.bestSolution = bestSolution;
    }

    @Override
    public String toString() {
        return "SolverInstance{"
                + "containerId='" + containerId + '\''
                + ", solverId='" + solverId + '\''
                + ", solverConfigFile='" + solverConfigFile + '\''
                + ", status=" + status
                + ", scoreWrapper=" + scoreWrapper
                + '}';
    }

    public String getSolverInstanceKey() {
        return getSolverInstanceKey(this.containerId,
                                    this.solverId);
    }

    public static String getSolverInstanceKey(String containerId,
                                              String solverId) {
        return containerId + "/" + solverId;
    }
}
