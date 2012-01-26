/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.simulation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.simulation.SimulationPath;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;

public class SimulationPathImpl implements SimulationPath {

    private Simulation   simulation;
    private String       name;

    private List<SimulationStep> steps = new ArrayList<SimulationStep>();

    public SimulationPathImpl(Simulation simulation,
            String name) {
        this.name = name;
        this.simulation = simulation;
    }

    public String getName() {
        return this.name;
    }

    public List<SimulationStep> getSteps() {
        return this.steps;
    }

    public void setSteps(List<SimulationStep> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "SimulationPathImpl [name=" + name + ", steps=" + steps.size() + "]";
    }

}
