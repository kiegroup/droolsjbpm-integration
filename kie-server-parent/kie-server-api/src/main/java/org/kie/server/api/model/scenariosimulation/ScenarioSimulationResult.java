/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.api.model.scenariosimulation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "scenario-simulation-result")
@XStreamAlias("scenario-simulation-result")
public class ScenarioSimulationResult {

    @XmlElement(name = "run-count")
    @XStreamAlias("run-count")
    private int runCount;

    @XmlElement(name = "ignore-count")
    @XStreamAlias("ignore-count")
    private int ignoreCount;

    @XmlElement(name = "run-time")
    @XStreamAlias("run-time")
    private long runTime;

    @XmlElement(name = "failures")
    @XStreamAlias("failures")
    private List<ScenarioSimulationFailure> failures = new ArrayList<>();

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public int getIgnoreCount() {
        return ignoreCount;
    }

    public void setIgnoreCount(int ignoreCount) {
        this.ignoreCount = ignoreCount;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public List<ScenarioSimulationFailure> getFailures() {
        return failures;
    }

    public void setFailures(List<ScenarioSimulationFailure> failures) {
        this.failures = failures;
    }
}
