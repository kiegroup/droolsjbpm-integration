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

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.*;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.knowledge.KnowledgeBuilderSimFluent;
import org.drools.fluent.knowledge.impl.DefaultKnowledgeBaseSimFluent;
import org.drools.fluent.knowledge.impl.DefaultKnowledgeBuilderSimFluent;
import org.drools.fluent.knowledge.KnowledgeBaseSimFluent;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.session.impl.DefaultStatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.SimulationPathFluent;
import org.drools.fluent.simulation.SimulationStepFluent;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class DefaultSimulationStepFluent extends AbstractFluentTest<SimulationStepFluent> implements SimulationStepFluent {

    private SimulationPathFluent pathFluent;

    public DefaultSimulationStepFluent(InternalSimulation sim,
                                       SimulationPathFluent pathFluent) {
        super();
        setSim( sim );
        this.pathFluent = pathFluent;
    }

    public KnowledgeBuilderSimFluent newKnowledgeBuilder() {
        addCommand( new NewKnowledgeBuilderCommand( null,
                                                             null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new DefaultKnowledgeBuilderSimFluent( getSim(),
                                                       this );
    }

    public KnowledgeBaseSimFluent newKnowledgeBase() {
        addCommand( new NewKnowledgeBaseCommand( null ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent( getSim(),
                                                    this );
    }

    public StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new DefaultStatefulKnowledgeSessionSimFluent( getSim(),
                                                               this );
    }

    public KnowledgeBuilderSimFluent getKnowledgeBuilder() {
        return new DefaultKnowledgeBuilderSimFluent( getSim(),
                                                       this );
    }

    public KnowledgeBaseSimFluent getKnowledgeBase() {
        return new DefaultKnowledgeBaseSimFluent( getSim(),
                                                    this );
    }

    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession() {
        return new DefaultStatefulKnowledgeSessionSimFluent( getSim(),
                                                               this );
    }

    public KnowledgeBuilderSimFluent getKnowledgeBuilder(String name) {
        addCommand( new GetVariableCommand( name ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ) );

        return new DefaultKnowledgeBuilderSimFluent( getSim(),
                                                       this );
    }

    public KnowledgeBaseSimFluent getKnowledgeBase(String name) {
        addCommand( new GetVariableCommand( name ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() ) );

        return new DefaultKnowledgeBaseSimFluent( getSim(),
                                                    this );
    }

    public StatefulKnowledgeSessionSimFluent getStatefulKnowledgeSession(String name) {
        addCommand( new GetVariableCommand( name ) );
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ) );

        return new DefaultStatefulKnowledgeSessionSimFluent( getSim(),
                                                               this );
    }

    public SimulationStepFluent newStep(long distance) {
        return pathFluent.newStep( distance );
    }
    
    public void addCommand(Command cmd) {
        getSim().addCommand(cmd);
    }

    public SimulationPathFluent end() {
        return pathFluent;
    }

}
