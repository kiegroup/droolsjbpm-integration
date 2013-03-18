/*
 * Copyright 2012 JBoss Inc
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

package org.drools.simulation.fluent.simulation;

import java.util.concurrent.TimeUnit;

import org.drools.simulation.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.simulation.fluent.test.TestableFluent;
import org.kie.builder.ReleaseId;
import org.kie.command.Command;
import org.kie.internal.fluent.FluentRoot;
import org.kie.simulation.Simulation;

public interface SimulationFluent extends FluentRoot, TestableFluent<SimulationFluent> {

    SimulationFluent newPath(String id);
    SimulationFluent getPath(String id);

    SimulationFluent newStep(long distanceMillis);
    SimulationFluent newStep(long distanceMillis, TimeUnit timeUnit);
    SimulationFluent newRelativeStep(long relativeDistance);
    SimulationFluent newRelativeStep(long relativeDistance, TimeUnit timeUnit);

    SimulationFluent addCommand(Command<?> command);

    String getActiveKieSessionId();
    StatefulKnowledgeSessionSimFluent newKieSession(ReleaseId releaseId, String id);
    StatefulKnowledgeSessionSimFluent getKieSession();
    StatefulKnowledgeSessionSimFluent getKieSession(String id);

    /**
     * Gets the Simulation
     * @return never null
     */
    Simulation getSimulation();

    /**
     * Run the Simulation with startTimeMillis now.
     */
    void runSimulation();

    /**
     * Run the Simulation.
     * @param startTimeMillis never null
     */
    void runSimulation(long startTimeMillis);

}
