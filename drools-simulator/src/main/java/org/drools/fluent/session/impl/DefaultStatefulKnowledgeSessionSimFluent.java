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

package org.drools.fluent.session.impl;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.command.Command;
import org.drools.command.GetVariableCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.runtime.AddEventListenerCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.process.StartProcessInstanceCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.RetractCommand;
import org.drools.command.runtime.rule.UpdateCommand;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.fluent.test.impl.AbstractTestableFluent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.command.AssertRulesFiredCommand;
import org.drools.simulation.impl.command.FiredRuleCounter;

public class DefaultStatefulKnowledgeSessionSimFluent extends AbstractTestableFluent<StatefulKnowledgeSessionSimFluent>
        implements StatefulKnowledgeSessionSimFluent {
    
    private SimulationFluent simulationFluent;
    
    public DefaultStatefulKnowledgeSessionSimFluent(SimulationFluent simulationFluent) {
        super();
        this.simulationFluent = simulationFluent;
    }

    protected StatefulKnowledgeSessionSimFluent addCommand(Command command) {
        simulationFluent.addCommand(command);
        return this;
    }

    public StatefulKnowledgeSessionSimFluent setGlobal(String identifier,
                                                            Object object) {
        addCommand(new SetGlobalCommand(identifier, object));
        return this;
    }    
    
    public StatefulKnowledgeSessionSimFluent insert(Object object) {
        addCommand(new InsertObjectCommand(object));
        
        return this;
    }
    
    public StatefulKnowledgeSessionSimFluent update(FactHandle handle, Object object) {
        addCommand(new UpdateCommand(handle, object));
        return this;
    }
    
    public StatefulKnowledgeSessionSimFluent retract(FactHandle handle) {
        addCommand(new RetractCommand(handle));
        return this;
    }
    
    public StatefulKnowledgeSessionSimFluent fireAllRules() {
        addCommand(new FireAllRulesCommand());
        return this;
    }

    public StatefulKnowledgeSessionSimFluent assertRuleFired(String ruleName) {
        return assertRuleFired(ruleName, 1);
    }

    public StatefulKnowledgeSessionSimFluent assertRuleFired(String ruleName, int fireCount) {
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
        return simulationFluent.getActiveKnowledgeSessionId();
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

    public StatefulKnowledgeSessionSimFluent set(String name) {
        addCommand(new SetVariableCommandFromLastReturn(null, name));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent startProcess(String identifier, Map<String, Object> params) {
        addCommand(new StartProcessCommand(identifier, params));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent startProcess(String identifier) {
        addCommand(new StartProcessCommand(identifier));
        return this;
    }
    

    public StatefulKnowledgeSessionSimFluent createProcessInstance(String identifier, Map<String, Object> params) {
        addCommand(new CreateProcessInstanceCommand(identifier, params));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent startProcessInstance(long processId) {
        addCommand(new StartProcessInstanceCommand(processId));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent signalEvent(String id, Object event, long processId) {
        addCommand(new SignalEventCommand(processId, id, event));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent signalEvent(String id, Object event) {
        addCommand(new SignalEventCommand(id, event));
        return this;
    }

    public StatefulKnowledgeSessionSimFluent newStep(long distanceMillis) {
        simulationFluent.newStep(distanceMillis);
        return this;
    }

    public StatefulKnowledgeSessionSimFluent newStep(long distanceMillis, TimeUnit timeUnit) {
        simulationFluent.newStep(distanceMillis, timeUnit);
        return this;
    }

    public StatefulKnowledgeSessionSimFluent newRelativeStep(long relativeDistance) {
        simulationFluent.newRelativeStep(relativeDistance);
        return this;
    }

    public StatefulKnowledgeSessionSimFluent newRelativeStep(long relativeDistance, TimeUnit timeUnit) {
        simulationFluent.newRelativeStep(relativeDistance, timeUnit);
        return this;
    }

}
