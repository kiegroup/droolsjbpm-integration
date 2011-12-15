package org.drools.fluent.standard.imp;

import org.drools.KnowledgeBase;
import org.drools.command.GetVariableCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.fluent.FluentStep;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.runtime.StatefulKnowledgeSession;

public class FluentStandardStatefulKnowledgeSessionImpl extends AbstractFluentTest<FluentStandardStatefulKnowledgeSession> implements FluentStandardStatefulKnowledgeSession {
    
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
    
    public FluentStandardStatefulKnowledgeSession fireAllRules() {
        getSim().addCommand( new FireAllRulesCommand() );
        return this;
    }
    
    public FluentStandardStep end(String context, String name) {
        getSim().addCommand( new GetVariableCommand( StatefulKnowledgeSession.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( context, name ) );
        return step;
    }
    
    public FluentStandardStep end(String name) {
        getSim().addCommand( new GetVariableCommand( StatefulKnowledgeSession.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( name ) );
        return step;
    }

    public FluentStandardStep end() {
        return step;
    }

    public FluentStandardStatefulKnowledgeSession set(String name) {
        getSim().addCommand( new SetVariableCommand( null, name ) );
        return this;
    }


}
