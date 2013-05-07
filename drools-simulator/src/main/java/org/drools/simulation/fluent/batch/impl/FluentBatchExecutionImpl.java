/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.IdentifiableResult;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.simulation.fluent.batch.FluentBatchExecution;
import org.drools.simulation.fluent.test.impl.MapVariableContext;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.internal.fluent.VariableContext;
import org.kie.api.runtime.rule.FactHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO Do we really want this as a separate class hierarchy just to do batches? Does this fit in with the SimulationFluent?
public class FluentBatchExecutionImpl implements FluentBatchExecution {

    private List<GenericCommand<?>> cmds = new ArrayList<GenericCommand<?>>();
    private VariableContext vars;
    private GenericCommand<?> lastAddedCommand;

    public FluentBatchExecutionImpl() {
        vars = new MapVariableContext();
    }

    public void addCommand(GenericCommand<?> cmd) {
        cmds.add(cmd);
    }

    public <P> VariableContext<P> getVariableContext() {
        return vars;
    }

    public BatchExecutionCommand getBatchExecution() {
        return new BatchExecutionCommandImpl((List<GenericCommand<?>>) cmds);
    }

    public FluentBatchExecution newBatchExecution() {
        return this;
    }

    public FluentBatchExecution insert(Object object) {
        lastAddedCommand = new InsertObjectCommand(object);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution update(FactHandle handle, Object object) {
        lastAddedCommand = new InsertObjectCommand(object);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution delete(FactHandle handle) {
        lastAddedCommand = new DeleteCommand(handle);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution fireAllRules() {
        addCommand(new FireAllRulesCommand());
        return this;
    }

    public FluentBatchExecution assertRuleFired(String ruleName) {
        throw new UnsupportedOperationException(
                "FluentBatchExecutionImpl duplicates DefaultStatefulKnowledgeSessionSimFluent");
    }

    public FluentBatchExecution assertRuleFired(String ruleName, int fireCount) {
        throw new UnsupportedOperationException(
                "FluentBatchExecutionImpl duplicates DefaultStatefulKnowledgeSessionSimFluent");
    }

    public FluentBatchExecution setGlobal(String identifier, Object object) {
        lastAddedCommand = new SetGlobalCommand(identifier, object);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution set(String name) {
        if (lastAddedCommand instanceof IdentifiableResult){
            ((IdentifiableResult) lastAddedCommand).setOutIdentifier(name);
        } else {
            Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.WARNING,
                    "The lastAddedCommand class (" + lastAddedCommand.getClass()
                            + ") is not an instanceof IdentifiableResult.\n "
                    + "As result, the variable '"+name+"' will not be set.");
        }
        return this;
    }

    public FluentBatchExecution startProcess(String identifier, Map<String, Object> params) {
        lastAddedCommand = new StartProcessCommand(identifier, params);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution startProcess(String identifier) {
        lastAddedCommand = new StartProcessCommand(identifier);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution createProcessInstance(String identifier, Map<String, Object> params) {
        lastAddedCommand = new CreateProcessInstanceCommand(identifier, params);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution startProcessInstance(long processId) {
        lastAddedCommand = new StartProcessInstanceCommand(processId);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution signalEvent(String id, Object event, long processId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // added but not yet tested
    public FluentBatchExecution signalEvent(String id, Object event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FluentBatchExecution out() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FluentBatchExecution out(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // added but not yet tested
    public FluentBatchExecution getGlobal(String identifier) {
        lastAddedCommand = new GetGlobalCommand(identifier);
        addCommand(lastAddedCommand);
        return this;
    }

}
