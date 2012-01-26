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
import java.util.List;

import org.drools.command.Command;
import org.drools.simulation.SimulationPath;
import org.drools.simulation.SimulationStep;

public class SimulationStepImpl implements SimulationStep {

    private SimulationPath path;
    private long distanceMillis;
    private List<Command> commands = new ArrayList<Command>();

    public SimulationStepImpl(SimulationPath path, long distanceMillis) {
        this.path = path;
        this.distanceMillis = distanceMillis;
    }

    public SimulationPath getPath() {
        return this.path;
    }

    public long getDistanceMillis() {
        return distanceMillis;
    }

    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return "SimulationStepImpl [path=" + path + ", distanceMillis=" + distanceMillis
                + ", commands=" + commands + "]";
    }

}
