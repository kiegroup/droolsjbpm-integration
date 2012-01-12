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

package org.drools.fluent.compact.imp;

import java.util.Map;
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
import org.drools.fluent.compact.FluentCompactKnowledgeBase;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.rule.FactHandle;

public class FluentCompactStatefulKnowledgeSessionImpl extends AbstractFluentTest<FluentCompactStatefulKnowledgeSession>
        implements FluentCompactStatefulKnowledgeSession {
    
    public FluentCompactStatefulKnowledgeSessionImpl(InternalSimulation sim) {
        super();
        setSim( sim );
    }
    
    public FluentCompactStatefulKnowledgeSession setGlobal(String identifier,
                                                            Object object) {
        getSim().addCommand( new SetGlobalCommand( identifier, object ) );
        return this;
    }        

    public FluentCompactStatefulKnowledgeSession fireAllRules() {
        getSim().addCommand( new FireAllRulesCommand() );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession insert(Object object) {
        getSim().addCommand( new InsertObjectCommand( object ) );
        return this;
    }
    
     public FluentCompactStatefulKnowledgeSession update(FactHandle handle, Object object) {
        getSim().addCommand( new UpdateCommand( handle, object ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession retract(FactHandle handle) {
        getSim().addCommand( new RetractCommand( handle ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession newStep(long distance) {
        getSim().newStep( distance );
        return this;
    }

    public FluentCompactKnowledgeBase getKnowledgeBase() {
        return new FluentCompactKnowledgeBaseImpl(getSim(), this);
    }

    public FluentCompactStatefulKnowledgeSession set(String contextName, String variableName) {
        getSim().addCommand( new SetVariableCommandFromLastReturn( contextName, variableName ) );
        return this;
    }
    
    public FluentCompactStatefulKnowledgeSession set(String name) {
        getSim().addCommand( new SetVariableCommandFromLastReturn( null, name ) );
        return this;
    }

    public FluentCompactSimulation end() {
        return (FluentCompactSimulation) getSim();
    }

    public FluentCompactStatefulKnowledgeSession startProcess(String identifier, Map<String, Object> params) {
        getSim().addCommand( new StartProcessCommand( identifier, params ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession startProcess(String identifier) {
        getSim().addCommand( new StartProcessCommand( identifier ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession createProcessInstance(String identifier, Map<String, Object> params) {
        getSim().addCommand( new CreateProcessInstanceCommand( identifier , params ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession startProcessInstnace(long processId) {
        getSim().addCommand( new StartProcessInstanceCommand( processId ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession signalEvent(String id, Object event, long processId) {
        getSim().addCommand( new SignalEventCommand( processId, id, event ) );
        return this;
    }

    public FluentCompactStatefulKnowledgeSession signalEvent(String id, Object event) {
        getSim().addCommand( new SignalEventCommand( id, event ) );
        return this;
    }
}
