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
    

    private Simulation simulation;
    private VariableContext variableContext;

    private SimulationPath activePath = null;
    private int pathCounter = 0;
    private SimulationStep activeStep = null;

    private String activeKnowledgeBuilderId = null;
    private int knowledgeBuilderCounter = 0;
    private String activeKnowledgeBaseId = null;
    private int knowledgeBaseCounter = 0;
    private String activeKnowledgeSessionId = null;
    private int knowledgeSessionCounter = 0;

    public DefaultSimulationFluent() {
        super();

        simulation = new SimulationImpl();
        variableContext = new MapVariableContext();
    }

    public <P> VariableContext<P> getVariableContext() {
        return variableContext;
    }

    public SimulationFluent newPath() {
        String pathId = "path" + pathCounter;
        pathCounter++;
        activeKnowledgeSessionId = null;
        return newPath(pathId);
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
            newPath();
        }
    }

    public SimulationPath getActivePath() {
        return activePath;
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

    public SimulationStep getActiveStep() {
        return activeStep;
    }

    public SimulationFluent addCommand(Command command) {
        activeStep.getCommands().add(command);
        return this;
    }

    public String getActiveKnowledgeBuilderId() {
        return activeKnowledgeBuilderId;
    }

    public KnowledgeBuilderSimFluent newKnowledgeBuilder() {
        String knowledgeBuilderId = KnowledgeBuilder.class.getName() + knowledgeBuilderCounter;
        knowledgeBuilderCounter++;
        return newKnowledgeBuilder(knowledgeBuilderId);
    }

    public KnowledgeBuilderSimFluent newKnowledgeBuilder(String id) {
        assureActiveStep();
        activeKnowledgeBuilderId = id;
        addCommand(new NewKnowledgeBuilderCommand(null,
                null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );
        activeKnowledgeBaseId = null;

        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    public KnowledgeBuilderSimFluent getKnowledgeBuilder() {
        assureActiveStep();
        if (activeKnowledgeBuilderId == null) {
            throw new IllegalStateException("There is no activeKnowledgeBuilder. Call newKnowledgeBuilder() instead.");
        }
        activeKnowledgeBaseId = null;
        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    public KnowledgeBuilderSimFluent getKnowledgeBuilder(String id) {
        assureActiveStep();
        activeKnowledgeBuilderId = id;
        addCommand(new GetVariableCommand(id));
        addCommand(new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );
        activeKnowledgeBaseId = null;

        return new DefaultKnowledgeBuilderSimFluent( this );
    }

    public String getActiveKnowledgeBaseId() {
        return activeKnowledgeBaseId;
    }

    public KnowledgeBaseSimFluent newKnowledgeBase() {
        String knowledgeBaseId = KnowledgeBase.class.getName() + knowledgeBaseCounter;
        knowledgeBaseCounter++;
        return newKnowledgeBase(knowledgeBaseId);
    }

    public KnowledgeBaseSimFluent newKnowledgeBase(String id) {
        assureActiveStep();
        activeKnowledgeBaseId = id;
        addCommand( new NewKnowledgeBaseCommand( null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent(this);
    }

    public KnowledgeBaseSimFluent getKnowledgeBase() {
        assureActiveStep();
        if (activeKnowledgeBaseId == null) {
            throw new IllegalStateException("There is no activeKnowledgeBase. Call newKnowledgeBase() instead.");
        }
        return new DefaultKnowledgeBaseSimFluent(this);
    }

    public KnowledgeBaseSimFluent getKnowledgeBase(String id) {
        assureActiveStep();
        activeKnowledgeBaseId = id;
        addCommand( new GetVariableCommand( id ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent( this );
    }

    public void assureActiveKnowledgeBase() {
        assureActiveStep();
        if (activeKnowledgeBaseId == null) {
            newKnowledgeBase().addKnowledgePackages();
        }
    }

    public String getActiveKnowledgeSessionId() {
        return activeKnowledgeSessionId;
    }

    public StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession() {
        String knowledgeSessionId = StatefulKnowledgeSession.class.getName() + knowledgeSessionCounter;
        knowledgeSessionCounter++;
        return newStatefulKnowledgeSession(knowledgeSessionId);
    }
    
    public StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession(String id) {
        assureActiveKnowledgeBase();
        activeKnowledgeSessionId = id;
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get("pseudo") );
        addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession() {
        assureActiveKnowledgeBase();
        if (activeKnowledgeSessionId == null) {
            throw new IllegalStateException("There is no activeKnowledgeSession. Call newStatefulKnowledgeSession() instead.");
        }
        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession(String id) {
        assureActiveKnowledgeBase();
        activeKnowledgeSessionId = id;
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
