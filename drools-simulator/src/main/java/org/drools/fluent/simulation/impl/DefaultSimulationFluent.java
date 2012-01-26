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

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.*;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.fluent.VariableContext;
import org.drools.fluent.knowledge.KnowledgeBaseSimFluent;
import org.drools.fluent.knowledge.KnowledgeBuilderSimFluent;
import org.drools.fluent.knowledge.impl.DefaultKnowledgeBaseSimFluent;
import org.drools.fluent.knowledge.impl.DefaultKnowledgeBuilderSimFluent;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.session.impl.DefaultStatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.test.impl.AbstractTestableFluent;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.SimulationPath;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.SimulationPathImpl;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.SimulationStepImpl;
import org.drools.simulation.impl.Simulator;

public class DefaultSimulationFluent extends AbstractTestableFluent<SimulationFluent>
        implements SimulationFluent {
    
    public static final String DEFAULT_PATH_ID = "default";

    private Simulation simulation;
    private VariableContext variableContext;

    private SimulationPath activePath = null;
    private SimulationStep activeStep = null;

    public DefaultSimulationFluent() {
        super();

        simulation = new SimulationImpl();
        variableContext = new MapVariableContext();
    }

    public <P> VariableContext<P> getVariableContext() {
        return variableContext;
    }

    public SimulationFluent newPath(String id) {
        activePath = new SimulationPathImpl(simulation, id);
        simulation.getPaths().put(id, activePath);
        activeStep = null;
        return this;
    }

    public SimulationFluent getPath(String id) {
        activePath = simulation.getPaths().get(id);
        if (activePath == null) {
            throw new IllegalArgumentException("There is no path with path id (" + id + ") created yet." +
                    " Call newPath(id) first.");
        }
        List<SimulationStep> steps = activePath.getSteps();
        activeStep = steps.get(steps.size() - 1);
        return this;
    }

    private void assureActivePath() {
        if (activePath == null) {
            newPath(DEFAULT_PATH_ID);
        }
    }

    public SimulationFluent newStep(long distanceMillis) {
        assureActivePath();
        activeStep = new SimulationStepImpl(activePath, distanceMillis);
        activePath.getSteps().add(activeStep);

        return this;
    }

    public SimulationFluent newStep(long distanceMillis, TimeUnit timeUnit) {
        return newStep(timeUnit.toMillis(distanceMillis));
    }

    public SimulationFluent newRelativeStep(long relativeDistanceMillis) {
        if (activeStep == null) {
            return newStep(relativeDistanceMillis);
        }
        return newStep(activeStep.getDistanceMillis() + relativeDistanceMillis);
    }

    public SimulationFluent newRelativeStep(long relativeDistance, TimeUnit timeUnit) {
        return newRelativeStep(timeUnit.toMillis(relativeDistance));
    }

    private void assureActiveStep() {
        if (activeStep == null) {
            newStep(0L);
        }
    }

    public SimulationFluent addCommand(Command command) {
        activeStep.getCommands().add(command);
        return this;
    }

    // TODO Doesn't this need an id?
    public KnowledgeBuilderSimFluent newKnowledgeBuilder() {
        assureActiveStep();
        addCommand(new NewKnowledgeBuilderCommand(null,
                null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    public KnowledgeBuilderSimFluent getKnowledgeBuilder() {
        assureActiveStep();
        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    // TODO What's the point of this id?
    public KnowledgeBuilderSimFluent getKnowledgeBuilder(String id) {
        assureActiveStep();
        addCommand( new GetVariableCommand( id ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    // TODO Doesn't this need an id?
    public KnowledgeBaseSimFluent newKnowledgeBase() {
        addCommand( new NewKnowledgeBaseCommand( null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent( this );
    }

    public KnowledgeBaseSimFluent getKnowledgeBase() {
        return new DefaultKnowledgeBaseSimFluent( this );
    }

    // TODO What's the point of this id?
    public KnowledgeBaseSimFluent getKnowledgeBase(String id) {
        addCommand( new GetVariableCommand( id ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent( this );
    }

    // TODO Doesn't this need an id?
    public StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get("pseudo") );
        addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession() {
        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    // TODO What's the point of this id?
    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession(String id) {
        addCommand(new GetVariableCommand(id));
        addCommand(new SetVariableCommandFromLastReturn(StatefulKnowledgeSession.class.getName()));

        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void runSimulation() {
        runSimulation( new Date().getTime() );
    }

    public void runSimulation(long startTimeMillis) {
        Simulator simulator = new Simulator( simulation, startTimeMillis );
        simulator.run();
    }

}
