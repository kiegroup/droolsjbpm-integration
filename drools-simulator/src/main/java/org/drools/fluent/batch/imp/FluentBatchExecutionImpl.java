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
package org.drools.fluent.batch.imp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.command.*;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.RetractCommand;
import org.drools.fluent.VariableContext;
import org.drools.fluent.batch.FluentBatchExecution;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.runtime.rule.FactHandle;

public class FluentBatchExecutionImpl extends AbstractFluentTest<FluentBatchExecution>
        implements FluentBatchExecution {

   
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

    public FluentBatchExecution fireAllRules() {
        addCommand(new FireAllRulesCommand());
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

    public FluentBatchExecution retract(FactHandle handle) {
        lastAddedCommand = new RetractCommand(handle);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution setGlobal(String identifier, Object object) {
        lastAddedCommand = new SetGlobalCommand(identifier, object);
        addCommand(lastAddedCommand);
        return this;
    }

    public FluentBatchExecution set(String name) {
        try {
            Method method = lastAddedCommand.getClass().getDeclaredMethod("setOutIdentifier", new Class[]{String.class});
            try {
                method.invoke(lastAddedCommand, name);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(FluentBatchExecutionImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
}
