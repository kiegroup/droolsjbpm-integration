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

package org.drools.fluent.standard.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.command.Command;
import org.drools.command.ContextManager;
import org.drools.fluent.FluentPath;
import org.drools.fluent.FluentStep;
import org.drools.fluent.VariableContext;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardKnowledgeBuilder;
import org.drools.fluent.standard.FluentStandardPath;
import org.drools.fluent.standard.FluentStandardSimulation;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.test.ReflectiveMatcherAssert;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;
import org.drools.simulation.Step;
import org.drools.simulation.impl.PathImpl;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.StepImpl;
import org.hamcrest.Matcher;

public class FluentStandardSimulationImpl extends
        AbstractFluentTest<FluentStandardSimulation>
    implements
    FluentStandardSimulation,
    InternalSimulation {

    private Path            path;

    private List<Step>      steps;

    private Step            step;

    private List<Command>   cmds;

    private SimulationImpl  sim;

    private VariableContext vars;

    public FluentStandardSimulationImpl() {
        super();
        setSim( this );

        vars = new MapVariableContext();
        sim = new SimulationImpl();
    }

    public <P> VariableContext<P> getVariableContext() {
        return vars;
    }

    public FluentStandardPath newPath(String name) {
        steps = new ArrayList<Step>();
        
        path = new PathImpl( sim,
                             name );
        sim.getPaths().put( path.getName(),
                            path );
        ((PathImpl) path).setSteps( steps );

        return new FluentStandardPathImpl( this,
                                           path.getName() );
    }

    public FluentStandardPath getPath(String name) {
        path = sim.getPaths().get( name );        
        steps = (List) path.getSteps();
        step = ( Step ) steps.get( steps.size() - 1 );
        if ( !step.getCommands().isEmpty() ) {
            cmds = (List) step.getCommands();
        }
        
        return new FluentStandardPathImpl( this,
                                           path.getName() );
    }

    public void addCommand(Command cmd) {
        cmds.add( cmd );
    }

    public void newStep(long distance) {
        cmds = new ArrayList<Command>();

        step = new StepImpl( path,
                             cmds,
                             distance );

        steps.add( step );
    }

    public Simulation getSimulation() {
        return sim;
    }

    public Map<String, Path> getPaths() {
        return sim.getPaths();
    }
}
