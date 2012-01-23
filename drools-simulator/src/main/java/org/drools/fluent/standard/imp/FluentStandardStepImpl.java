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

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.GetVariableCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.path.SimulationPathFluent;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardKnowledgeBuilder;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class FluentStandardStepImpl extends AbstractFluentTest<FluentStandardStep> implements FluentStandardStep {

    private SimulationPathFluent pathFluent;

    public FluentStandardStepImpl(InternalSimulation sim,
                                  SimulationPathFluent pathFluent) {
        super();
        setSim( sim );
        this.pathFluent = pathFluent;
    }

    public FluentStandardKnowledgeBuilder newKnowledgeBuilder() {
        getSim().addCommand( new NewKnowledgeBuilderCommand( null,
                                                             null ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new FluentStandardKnowledgeBuilderImpl( getSim(),
                                                       this );
    }

    public FluentStandardKnowledgeBase newKnowledgeBase() {
        getSim().addCommand( new NewKnowledgeBaseCommand( null ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new FluentStandardKnowledgeBaseImpl( getSim(),
                                                    this );
    }

    public FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        getSim().addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new FluentStandardStatefulKnowledgeSessionImpl( getSim(),
                                                               this );
    }

    public FluentStandardKnowledgeBuilder getKnowledgeBuilder() {
        return new FluentStandardKnowledgeBuilderImpl( getSim(),
                                                       this );
    }

    public FluentStandardKnowledgeBase getKnowledgeBase() {
        return new FluentStandardKnowledgeBaseImpl( getSim(),
                                                    this );
    }

    public FluentStandardStatefulKnowledgeSession getStatefulKnowledgeSession() {
        return new FluentStandardStatefulKnowledgeSessionImpl( getSim(),
                                                               this );
    }

    public FluentStandardKnowledgeBuilder getKnowledgeBuilder(String name) {
        getSim().addCommand( new GetVariableCommand( name ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new FluentStandardKnowledgeBuilderImpl( getSim(),
                                                       this );
    }

    public FluentStandardKnowledgeBase getKnowledgeBase(String name) {
        getSim().addCommand( new GetVariableCommand( name ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new FluentStandardKnowledgeBaseImpl( getSim(),
                                                    this );
    }

    public FluentStandardStatefulKnowledgeSession getStatefulKnowledgeSession(String name) {
        getSim().addCommand( new GetVariableCommand( name ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new FluentStandardStatefulKnowledgeSessionImpl( getSim(),
                                                               this );
    }

    public FluentStandardStep newStep(long distance) {
        return pathFluent.newStep( distance );
    }

    public SimulationPathFluent end() {
        return pathFluent;
    }

}
