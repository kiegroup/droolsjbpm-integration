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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.drools.command.Command;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.VariableContext;
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

    private SimulationPath path;

    private List<SimulationStep>      steps;

    private SimulationStep step;

    private List<Command>   cmds;

    private SimulationImpl  sim;

    private VariableContext vars;

    public DefaultSimulationFluent() {
        super();
        setSim( this );

        vars = new MapVariableContext();
        sim = new SimulationImpl();
    }

    public SimulationPathFluent newPath(String name) {
        steps = new ArrayList<SimulationStep>();
        
        path = new SimulationPathImpl( sim,
                             name );
        sim.getPaths().put( path.getName(),
                            path );
        ((SimulationPathImpl) path).setSteps( steps );

        return new DefaultSimulationPathFluent( this,
                                           path.getName() );
    }

    public SimulationPathFluent getPath(String name) {
        path = sim.getPaths().get( name );        
        steps = (List) path.getSteps();
        step = (SimulationStep) steps.get( steps.size() - 1 );
        if ( !step.getCommands().isEmpty() ) {
            cmds = (List) step.getCommands();
        }
        
        return new DefaultSimulationPathFluent( this,
                                           path.getName() );
    }

    public void newStep(long distance) {
        cmds = new ArrayList<Command>();

        step = new SimulationStepImpl( path,
                             cmds,
                             distance );

        steps.add(step);
    }

    public void addCommand(Command cmd) {
        cmds.add( cmd );
    }

    public <P> VariableContext<P> getVariableContext() {
        return vars;
    }

    public Simulation getSimulation() {
        return sim;
    }

    public Map<String, SimulationPath> getPaths() {
        return sim.getPaths();
    }

    public void runSimulation() {
        runSimulation( new Date().getTime() );
    }

    public void runSimulation(long startTimeMillis) {
        Simulator simulator = new Simulator( getSimulation(), startTimeMillis );
        simulator.run();
    }

}
