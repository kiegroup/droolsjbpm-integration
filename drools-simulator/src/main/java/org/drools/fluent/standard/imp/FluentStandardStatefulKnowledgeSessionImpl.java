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

import java.util.Map;
import org.drools.command.GetVariableCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.process.StartProcessInstanceCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.RetractCommand;
import org.drools.command.runtime.rule.UpdateCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public class FluentStandardStatefulKnowledgeSessionImpl extends AbstractFluentTest<FluentStandardStatefulKnowledgeSession>
        implements FluentStandardStatefulKnowledgeSession {
    
    private FluentStandardStepImpl step;
    
    public FluentStandardStatefulKnowledgeSessionImpl(InternalSimulation sim,
                                                     FluentStandardStepImpl step) {
        super();
        setSim( sim );
        this.step = step;
    }    


    public FluentStandardStatefulKnowledgeSession setGlobal(String identifier,
                                                            Object object) {
        getSim().addCommand( new SetGlobalCommand( identifier, object ) );
        return this;
    }    
    
    public FluentStandardStatefulKnowledgeSession insert(Object object) {
        getSim().addCommand( new InsertObjectCommand( object ) );
        
        return this;
    }
    
    public FluentStandardStatefulKnowledgeSession update(FactHandle handle, Object object) {
        getSim().addCommand( new UpdateCommand( handle, object ) );
        return this;
    }
    
    public FluentStandardStatefulKnowledgeSession retract(FactHandle handle) {
        getSim().addCommand( new RetractCommand( handle ) );
        return this;
    }
    
    public FluentStandardStatefulKnowledgeSession fireAllRules() {
        getSim().addCommand( new FireAllRulesCommand() );
        return this;
    }
    
    public FluentStandardStep end(String context, String name) {
        getSim().addCommand( new GetVariableCommand( StatefulKnowledgeSession.class.getName() ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( context, name ) );
        return step;
    }
    
    public FluentStandardStep end(String name) {
        getSim().addCommand( new GetVariableCommand( StatefulKnowledgeSession.class.getName() ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( name ) );
        return step;
    }

    public FluentStandardStep end() {
        return step;
    }

    public FluentStandardStatefulKnowledgeSession set(String name) {
        getSim().addCommand( new SetVariableCommandFromLastReturn( null, name ) );
        return this;
    }

    public FluentStandardStatefulKnowledgeSession startProcess(String identifier, Map<String, Object> params) {
        getSim().addCommand(new StartProcessCommand(identifier, params));
        return this;
    }

    public FluentStandardStatefulKnowledgeSession startProcess(String identifier) {
        getSim().addCommand(new StartProcessCommand(identifier));
        return this;
    }
    

    public FluentStandardStatefulKnowledgeSession createProcessInstance(String identifier, Map<String, Object> params) {
        getSim().addCommand(new CreateProcessInstanceCommand(identifier, params));
        return this;
    }

    public FluentStandardStatefulKnowledgeSession startProcessInstnace(long processId) {
        getSim().addCommand(new StartProcessInstanceCommand(processId));
        return this;
    }

    public FluentStandardStatefulKnowledgeSession signalEvent(String id, Object event, long processId) {
        getSim().addCommand(new SignalEventCommand(processId, id, event));
        return this;
    }

    public FluentStandardStatefulKnowledgeSession signalEvent(String id, Object event) {
        getSim().addCommand(new SignalEventCommand(id, event));
        return this;
    }

}
