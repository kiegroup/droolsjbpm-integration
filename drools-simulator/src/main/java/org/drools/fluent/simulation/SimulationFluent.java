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

package org.drools.fluent.simulation;

import org.drools.fluent.FluentRoot;
import org.drools.fluent.FluentTest;

public interface SimulationFluent extends FluentRoot, FluentTest<SimulationFluent> {
    
    SimulationPathFluent newPath(String name);
    
    SimulationPathFluent getPath(String name);

    // TODO SimulationStepFluent newStep(long distance);

    /**
     * Runs the Simulation with startTimeMillis now.
     */
    void runSimulation();

    /**
     * Runs the Simulation.
     * @param startTimeMillis never null
     */
    void runSimulation(long startTimeMillis);

}
