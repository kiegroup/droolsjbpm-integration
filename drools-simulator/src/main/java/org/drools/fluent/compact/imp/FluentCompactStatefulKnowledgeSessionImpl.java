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

import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.fluent.compact.FluentCompactKnowledgeBase;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.test.impl.AbstractFluentTest;

public class FluentCompactStatefulKnowledgeSessionImpl extends AbstractFluentTest<FluentCompactStatefulKnowledgeSession>
    implements
    FluentCompactStatefulKnowledgeSession {
    
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

    public FluentCompactStatefulKnowledgeSession newStep(long distance) {
        getSim().newStep( distance );
        return this;
    }

    public FluentCompactKnowledgeBase getKnowledgeBase() {
        return new FluentCompactKnowledgeBaseImpl(getSim(), this);
    }

    public FluentCompactStatefulKnowledgeSession set(String contextName, String variableName) {
        getSim().addCommand( new SetVariableCommand( contextName, variableName ) );
        return this;
    }
    
    public FluentCompactStatefulKnowledgeSession set(String name) {
        getSim().addCommand( new SetVariableCommand( null, name ) );
        return this;
    }

    public FluentCompactSimulation end() {
        return (FluentCompactSimulation) getSim();
    }

}
