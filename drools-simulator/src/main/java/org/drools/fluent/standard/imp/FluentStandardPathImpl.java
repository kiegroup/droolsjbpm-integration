package org.drools.fluent.standard.imp;

import java.util.ArrayList;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Command;
import org.drools.command.Context;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.fluent.FluentPath;
import org.drools.fluent.FluentStep;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardKnowledgeBuilder;
import org.drools.fluent.standard.FluentStandardPath;
import org.drools.fluent.standard.FluentStandardSimulation;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.impl.StepImpl;

public class FluentStandardPathImpl extends AbstractFluentTest<FluentStandardPath>
    implements
    FluentStandardPath {

    private String defaultContext;

    public FluentStandardPathImpl(InternalSimulation sim,
                                  String defaultContext) {
        super();
        setSim( sim );
        this.defaultContext = defaultContext;
    }

    //    public FluentStandardKnowledgeBuilderImpl<FluentStandardPathImpl> newKnowledgeBuilder() {
    //        getSim().addCommand( new NewKnowledgeBuilderCommand( null,
    //                                                             KnowledgeBase.class.getName() ) );
    //        getSim().addCommand( new SetVariableCommand( defaultContext,
    //                                                     KnowledgeBuilder.class.getName() ) );
    //
    //        return new FluentStandardKnowledgeBuilderImpl<FluentStandardPathImpl>( getSim(),
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
    
    public FluentStandardPath newPath(String name) {
        return (( FluentStandardSimulation ) getSim()).newPath( name );
    }
    
    public FluentStandardPath getPath(String name) {
        return (( FluentStandardSimulation ) getSim()).getPath( name );
    }    

    public FluentStandardSimulation end() {
        return ( FluentStandardSimulation ) getSim();
    }

}
