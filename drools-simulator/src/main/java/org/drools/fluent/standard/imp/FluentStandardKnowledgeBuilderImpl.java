package org.drools.fluent.standard.imp;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.GetVariableCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.FluentPath;
import org.drools.fluent.FluentStep;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardKnowledgeBuilder;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;

public class FluentStandardKnowledgeBuilderImpl extends AbstractFluentTest<FluentStandardKnowledgeBuilder> implements FluentStandardKnowledgeBuilder {    
    private FluentStandardStepImpl step;
    
    public FluentStandardKnowledgeBuilderImpl(InternalSimulation sim,
                                              FluentStandardStepImpl step) {
        super();
        setSim( sim );
        this.step = step;
    }

    public FluentStandardKnowledgeBuilder add(Resource resource,
                                              ResourceType type) {
        getSim().addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        
        return this;
    }

    public FluentStandardKnowledgeBuilder add(Resource resource,
                                              ResourceType type,
                                              ResourceConfiguration configuration) {
        getSim().addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );
        
        return this;
    }

    public FluentStandardStep end(String context, String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBuilder.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( context, name ) );
        return step;
    }
    
    public FluentStandardStep end(String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBuilder.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( name ) );
        return step;
    }

    public FluentStandardStep end() {
        return step;
    }

}
