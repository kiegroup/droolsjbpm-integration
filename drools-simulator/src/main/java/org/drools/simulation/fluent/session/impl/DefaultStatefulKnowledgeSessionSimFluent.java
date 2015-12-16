/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.drools.simulation.fluent.session.impl;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.SetVariableCommandFromLastReturn;
import org.drools.core.command.runtime.AddEventListenerCommand;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.drools.simulation.fluent.session.KieSessionSimulationFluent;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.simulation.fluent.test.impl.AbstractTestableFluent;
import org.drools.simulation.impl.command.AssertRulesFiredCommand;
import org.drools.simulation.impl.command.FiredRuleCounter;
import org.kie.api.command.Command;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.simulation.SimulationStep;

public class DefaultStatefulKnowledgeSessionSimFluent extends AbstractTestableFluent<KieSessionSimulationFluent>
        implements KieSessionSimulationFluent {
    
    private SimulationFluent simulationFluent;
    
    public DefaultStatefulKnowledgeSessionSimFluent(SimulationFluent simulationFluent) {
        super();
        this.simulationFluent = simulationFluent;
    }

    protected KieSessionSimulationFluent addCommand(Command command) {
        simulationFluent.addCommand(command);
        return this;
    }

    public KieSessionSimulationFluent setGlobal(String identifier,
                                                            Object object) {
        addCommand(new SetGlobalCommand(identifier, object));
        return this;
    }    
    
    public KieSessionSimulationFluent insert(Object object) {
        addCommand(new InsertObjectCommand(object));
        
        return this;
    }
    
    public KieSessionSimulationFluent update(FactHandle handle, Object object) {
        addCommand(new UpdateCommand(handle, object));
        return this;
    }
    
    public KieSessionSimulationFluent delete(FactHandle handle) {
        addCommand(new DeleteCommand(handle));
        return this;
    }
    
    public KieSessionSimulationFluent fireAllRules() {
        addCommand(new FireAllRulesCommand());
        return this;
    }

    public KieSessionSimulationFluent assertRuleFired(String ruleName) {
        return assertRuleFired(ruleName, 1);
    }

    public KieSessionSimulationFluent assertRuleFired(String ruleName, int fireCount) {
        SimulationStep activeStep = ((DefaultSimulationFluent) simulationFluent).getActiveStep();
        FiredRuleCounter firedRuleCounter = new FiredRuleCounter();
        firedRuleCounter.setInclusiveRuleNameList(Collections.singletonList(ruleName));
        insertFireRuleCounter(activeStep, firedRuleCounter);
        AssertRulesFiredCommand assertRulesFiredCommand = new AssertRulesFiredCommand(firedRuleCounter);
        assertRulesFiredCommand.addAssertRuleFired(ruleName, fireCount);
        addCommand(assertRulesFiredCommand);
        return this;
    }

    private void insertFireRuleCounter(SimulationStep activeStep, FiredRuleCounter firedRuleCounter) {
        // Add the EventListener just before the last FireAllRules in this step
        int lastFireAllRulesIndex = -1;
        List<Command> commands = activeStep.getCommands();
        for (ListIterator<Command> it = commands.listIterator(); it.hasNext();) {
            int i = it.nextIndex();
            Command command = it.next();
            if (command instanceof FireAllRulesCommand) {
                lastFireAllRulesIndex = i;
            }
        }
        if (lastFireAllRulesIndex < 0) {
            throw new IllegalArgumentException(
                    "Cannot assertRuleFired, because in this step, fireAllRules() hasn't been called yet.");
        }
        commands.add(lastFireAllRulesIndex, new AddEventListenerCommand(firedRuleCounter));
    }

    public String getActiveKnowledgeSessionId() {
        return simulationFluent.getActiveKieSessionId();
    }
    
    public SimulationFluent end(String context, String name) {
        addCommand(new GetVariableCommand(StatefulKnowledgeSession.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(context, name));
        return simulationFluent;
    }
    
    public SimulationFluent end(String name) {
        addCommand(new GetVariableCommand(StatefulKnowledgeSession.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(name));
        return simulationFluent;
    }

    public SimulationFluent end() {
        return simulationFluent;
    }

    public KieSessionSimulationFluent set(String name) {
        addCommand(new SetVariableCommandFromLastReturn(null, name));
        return this;
    }

    public KieSessionSimulationFluent startProcess(String identifier, Map<String, Object> params) {
        addCommand(new StartProcessCommand(identifier, params));
        return this;
    }

    public KieSessionSimulationFluent startProcess(String identifier) {
        addCommand(new StartProcessCommand(identifier));
        return this;
    }
    

    public KieSessionSimulationFluent createProcessInstance(String identifier, Map<String, Object> params) {
        addCommand(new CreateProcessInstanceCommand(identifier, params));
        return this;
    }

    public KieSessionSimulationFluent startProcessInstance(long processId) {
        addCommand(new StartProcessInstanceCommand(processId));
        return this;
    }

    public KieSessionSimulationFluent abortProcessInstance(long processInstanceId) {
        AbortProcessInstanceCommand cmd = new AbortProcessInstanceCommand();
        cmd.setProcessInstanceId(processInstanceId);
        addCommand(cmd);
        return this;
    }

    public KieSessionSimulationFluent signalEvent(String id, Object event, long processId) {
        addCommand(new SignalEventCommand(processId, id, event));
        return this;
    }

    public KieSessionSimulationFluent signalEvent(String id, Object event) {
        addCommand(new SignalEventCommand(id, event));
        return this;
    }

    public KieSessionSimulationFluent newStep(long distanceMillis) {
        simulationFluent.newStep(distanceMillis);
        return this;
    }

    public KieSessionSimulationFluent newStep(long distanceMillis, TimeUnit timeUnit) {
        simulationFluent.newStep(distanceMillis, timeUnit);
        return this;
    }

    public KieSessionSimulationFluent newRelativeStep(long relativeDistance) {
        simulationFluent.newRelativeStep(relativeDistance);
        return this;
    }

    public KieSessionSimulationFluent newRelativeStep(long relativeDistance, TimeUnit timeUnit) {
        simulationFluent.newRelativeStep(relativeDistance, timeUnit);
        return this;
    }

    @Override
    public KieSessionSimulationFluent out() {
        throw new UnsupportedOperationException("Not supported yet.");    }

    @Override
    public KieSessionSimulationFluent out(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieSessionSimulationFluent getGlobal(String identifier) {
        addCommand(new GetGlobalCommand(identifier));
        return this;
    }

    @Override
    public WorkItemManagerFluent<WorkItemManagerFluent, KieSessionSimulationFluent> getWorkItemManager() {
        return new DefaultWorkItemManagerSimFluentImpl(this);
    }

}
