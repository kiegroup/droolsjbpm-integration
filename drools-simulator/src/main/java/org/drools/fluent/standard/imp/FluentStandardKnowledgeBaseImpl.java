package org.drools.fluent.standard.imp;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.ContextManager;
import org.drools.command.GetVariableCommand;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.FluentStep;
import org.drools.fluent.compact.FluentCompactKnowledgeBase;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.compact.imp.FluentCompactStatefulKnowledgeSessionImpl;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardKnowledgeBuilder;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class FluentStandardKnowledgeBaseImpl extends AbstractFluentTest<FluentStandardKnowledgeBase> implements FluentStandardKnowledgeBase {

    private FluentStandardStepImpl step;
    
    public FluentStandardKnowledgeBaseImpl(InternalSimulation sim,
                                           FluentStandardStepImpl step) {
        super();
        setSim( sim );
        this.step = step;
    }
    
    public FluentStandardKnowledgeBase addKnowledgePackages() {
        getSim().addCommand(  new KnowledgeBaseAddKnowledgePackagesCommand() );
        return this;
    }    
    
    
    public FluentStandardKnowledgeBase addKnowledgePackages(Resource resource,
                                                            ResourceType type) {
        getSim().addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        return this;
    }

    public FluentStandardKnowledgeBase addKnowledgePackages(Resource resource,
                                                            ResourceType type,
                                                            ResourceConfiguration configuration) {
        getSim().addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );
        
        return this;
    }

    public FluentStandardStep end(String context, String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( context, name ) );
        return step;
    }
    
    public FluentStandardStep end(String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        getSim().addCommand( new SetVariableCommand( name ) );
        return step;
    }

    public FluentStandardStep end() {
        return step;
    }

    public FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        getSim().addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );             
        getSim().addCommand( new SetVariableCommand( ContextManager.ROOT, StatefulKnowledgeSession.class.getName() ));        

        return new FluentStandardStatefulKnowledgeSessionImpl(getSim(), step);
    }

}
