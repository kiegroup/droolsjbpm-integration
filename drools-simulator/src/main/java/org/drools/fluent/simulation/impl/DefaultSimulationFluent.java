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

package org.drools.fluent.simulation.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.drools.command.Command;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.VariableContext;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.SimulationPathFluent;
import org.drools.fluent.simulation.SimulationStepFluent;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.simulation.SimulationPath;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.SimulationPathImpl;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.SimulationStepImpl;
import org.drools.simulation.impl.Simulator;

public class DefaultSimulationFluent extends AbstractFluentTest<SimulationFluent>
        implements SimulationFluent, InternalSimulation {

    private Simulation simulation;
    private VariableContext variableContext;

    private SimulationPath lastPath = null;

    private List<SimulationStep> steps;

    private List<Command> commands;


    public DefaultSimulationFluent() {
        super();
        setSim( this );

        simulation = new SimulationImpl();
        variableContext = new MapVariableContext();
    }

    public SimulationPathFluent newPath(String name) {
        steps = new ArrayList<SimulationStep>();

        lastPath = new SimulationPathImpl(simulation,
                             name );
        simulation.getPaths().put(lastPath.getName(),
                lastPath);
        ((SimulationPathImpl) lastPath).setSteps(steps);

        return new DefaultSimulationPathFluent( this,
                                           lastPath.getName() );
    }

    public SimulationPathFluent getPath(String name) {
        lastPath = simulation.getPaths().get( name );
        steps = (List<SimulationStep>) lastPath.getSteps();
        SimulationStep step = (SimulationStep) steps.get( steps.size() - 1 );
        if ( !step.getCommands().isEmpty() ) {
            commands = (List<Command>) step.getCommands();
        }
        
        return new DefaultSimulationPathFluent( this,
                                           lastPath.getName() );
    }

    public SimulationStepFluent newStep(long distance) {
        if (lastPath == null) {
            newPath("default");
        }
        return getPath("default").newStep(distance);
    }

    public void newInternalStep(long distance) {
        commands = new ArrayList<Command>();

        SimulationStep step = new SimulationStepImpl(lastPath, commands, distance );
        steps.add(step);
    }

    public void addCommand(Command cmd) {
        commands.add(cmd);
    }

    public <P> VariableContext<P> getVariableContext() {
        return variableContext;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Map<String, SimulationPath> getPaths() {
        return simulation.getPaths();
    }


    public void runSimulation() {
        runSimulation( new Date().getTime() );
    }

    public void runSimulation(long startTimeMillis) {
        Simulator simulator = new Simulator( simulation, startTimeMillis );
        simulator.run();
    }

}
