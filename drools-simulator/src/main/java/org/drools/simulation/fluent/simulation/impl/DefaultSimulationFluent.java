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

package org.drools.simulation.fluent.simulation.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.NewKieSessionCommand;
import org.drools.core.command.SetVariableCommandFromLastReturn;
import org.drools.simulation.fluent.session.KieSessionSimulationFluent;
import org.drools.simulation.fluent.session.impl.DefaultStatefulKnowledgeSessionSimFluent;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.test.impl.AbstractTestableFluent;
import org.drools.simulation.fluent.test.impl.MapVariableContext;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.SimulationPathImpl;
import org.drools.simulation.impl.SimulationStepImpl;
import org.drools.simulation.impl.Simulator;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.internal.fluent.VariableContext;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.simulation.Simulation;
import org.kie.internal.simulation.SimulationPath;
import org.kie.internal.simulation.SimulationStep;

public class DefaultSimulationFluent extends AbstractTestableFluent<SimulationFluent>
        implements SimulationFluent {
    

    private Simulation simulation;
    private VariableContext variableContext;

    private SimulationPath activePath = null;
    private int pathCounter = 0;
    private SimulationStep activeStep = null;

    protected String activeKieSessionId = null;
    
    protected static final String DEFAULT_ID = "__DEFAULT__";

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
        activeKieSessionId = null;
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

    protected void assureActiveStep() {
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

    public String getActiveKieSessionId() {
        return activeKieSessionId;
    }

    public KieSessionSimulationFluent newKieSession() {
        return newKieSession( null, null);
    }
    
    public KieSessionSimulationFluent newKieSession(ReleaseId releaseId, String id) {
        assureActiveStep();
        activeKieSessionId = id == null ? DEFAULT_ID : id;
        addCommand( new NewKieSessionCommand( releaseId, id ) );
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public KieSessionSimulationFluent getKieSession() {
        if (activeKieSessionId == null) {
            throw new IllegalStateException("There is no activeKnowledgeSession. Call newStatefulKnowledgeSession() instead.");
        }
        return new DefaultStatefulKnowledgeSessionSimFluent( this );
    }

    public KieSessionSimulationFluent getKieSession(String id) {
        activeKieSessionId = id;
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
        simulator.dispose();
    }

}
