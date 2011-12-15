package org.drools.fluent.standard.imp;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.command.GetVariableCommand;
import org.drools.command.NewKnowledgeBaseCommand;
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

public class FluentStandardStepImpl extends AbstractFluentTest<FluentStandardStep>
    implements
    FluentStandardStep {
    private FluentStandardPath path;

    public FluentStandardStepImpl(InternalSimulation sim,
                                  FluentStandardPath path) {
        super();
        setSim( sim );
        this.path = path;
    }

    public FluentStandardKnowledgeBuilder newKnowledgeBuilder() {
        getSim().addCommand( new NewKnowledgeBuilderCommand( null,
                                                             null ) );
        getSim().addCommand( new SetVariableCommand( KnowledgeBuilder.class.getName() ) );

        return new FluentStandardKnowledgeBuilderImpl( getSim(),
                                                       this );
    }

    public FluentStandardKnowledgeBase newKnowledgeBase() {
        getSim().addCommand( new NewKnowledgeBaseCommand( null ) );
        getSim().addCommand( new SetVariableCommand( KnowledgeBase.class.getName() ) );

        return new FluentStandardKnowledgeBaseImpl( getSim(),
                                                    this );
    }

    public FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        getSim().addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        getSim().addCommand( new SetVariableCommand( StatefulKnowledgeSession.class.getName() ) );

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
        getSim().addCommand( new SetVariableCommand( KnowledgeBuilder.class.getName() ) );

        return new FluentStandardKnowledgeBuilderImpl( getSim(),
                                                       this );
    }

    public FluentStandardKnowledgeBase getKnowledgeBase(String name) {
        getSim().addCommand( new GetVariableCommand( name ) );
        getSim().addCommand( new SetVariableCommand( KnowledgeBase.class.getName() ) );

        return new FluentStandardKnowledgeBaseImpl( getSim(),
                                                    this );
    }

    public FluentStandardStatefulKnowledgeSession getStatefulKnowledgeSession(String name) {
        getSim().addCommand( new GetVariableCommand( name ) );
        getSim().addCommand( new SetVariableCommand( StatefulKnowledgeSession.class.getName() ) );

        return new FluentStandardStatefulKnowledgeSessionImpl( getSim(),
                                                               this );
    }

    public FluentStandardStep newStep(long distance) {
        return path.newStep( distance );
    }

    public FluentStandardPath end() {
        return path;
    }

}
