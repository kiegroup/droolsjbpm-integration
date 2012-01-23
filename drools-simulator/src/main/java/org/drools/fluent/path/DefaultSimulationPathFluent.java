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

package org.drools.fluent.path;

import org.drools.fluent.InternalSimulation;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.standard.imp.FluentStandardStepImpl;
import org.drools.fluent.test.impl.AbstractFluentTest;

public class DefaultSimulationPathFluent extends AbstractFluentTest<SimulationPathFluent>
        implements SimulationPathFluent {

    private String defaultContext;

    public DefaultSimulationPathFluent(InternalSimulation sim,
                                       String defaultContext) {
        super();
        setSim( sim );
        this.defaultContext = defaultContext;
    }

    //    public FluentStandardKnowledgeBuilderImpl<DefaultSimulationPathFluent> newKnowledgeBuilder() {
    //        getSim().addCommand( new NewKnowledgeBuilderCommand( null,
    //                                                             KnowledgeBase.class.getName() ) );
    //        getSim().addCommand( new SetVariableCommand( defaultContext,
    //                                                     KnowledgeBuilder.class.getName() ) );
    //
    //        return new FluentStandardKnowledgeBuilderImpl<DefaultSimulationPathFluent>( getSim(),
    //                                                                               this,
    //                                                                               defaultContext );
    //    }
    //
    //    public FluentStandardKnowledgeBase newKnowledgeBase() {
    //        getSim().addCommand( new NewKnowledgeBuilderCommand( null,
    //                                                             KnowledgeBase.class.getName() ) );
    //        getSim().addCommand( new SetVariableCommand( defaultContext,
    //                                                     KnowledgeBuilder.class.getName() ) );
    //
    //        return null;
    //    }
    //
    //    public FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession() {
    //        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
    //        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
    //        getSim().addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
    //        getSim().addCommand( new SetVariableCommand( defaultContext,
    //                                                     StatefulKnowledgeSession.class.getName() ) );
    //
    //        return null;
    //    }
    //
    //    public FluentStandardKnowledgeBuilder getKnowledgeBuilder() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    public FluentStandardKnowledgeBase getKnowledgeBase() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    public FluentStandardStatefulKnowledgeSession getStatefulKnowledgeSession() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    public FluentStandardKnowledgeBuilder getKnowledgeBuilder(String name) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    public FluentStandardKnowledgeBase getKnowledgeBase(String name) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    public FluentStandardStatefulKnowledgeSession getStatefulKnowledgeSession(String name) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

    public FluentStandardStep newStep(long distance) {
        getSim().newStep( distance );
        
        return new FluentStandardStepImpl( getSim(), this );
    }
    
    public SimulationPathFluent newPath(String name) {
        return ((SimulationFluent) getSim()).newPath( name );
    }
    
    public SimulationPathFluent getPath(String name) {
        return ((SimulationFluent) getSim()).getPath( name );
    }    

    public SimulationFluent end() {
        return (SimulationFluent) getSim();
    }

}
