package org.drools.fluent.compact.imp;

import org.drools.KnowledgeBaseConfiguration;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.compact.FluentCompactKnowledgeBase;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;

public class FluentCompactKnowledgeBaseImpl extends AbstractFluentTest<FluentCompactKnowledgeBase>
    implements
    FluentCompactKnowledgeBase {

    private FluentCompactStatefulKnowledgeSession ksession;

    public FluentCompactKnowledgeBaseImpl(InternalSimulation sim,
                                          FluentCompactStatefulKnowledgeSession ksession) {
        super();
        setSim( sim );
        this.ksession = ksession;
    }
    
    public FluentCompactKnowledgeBase setKnowledgeBuilderconfiguration(KnowledgeBaseConfiguration kbaseConf) {
        // TODO Auto-generated method stub
        return null;
    }    
    
    public FluentCompactKnowledgeBase addKnowledgePackages() {
        getSim().addCommand(  new KnowledgeBaseAddKnowledgePackagesCommand() );
        return this;
    }    

    public FluentCompactKnowledgeBase addKnowledgePackages(Resource resource,
                                                           ResourceType type) {
        getSim().addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        return this;
    }

    public FluentCompactKnowledgeBase addKnowledgePackages(Resource resource,
                                                           ResourceType type,
                                                           ResourceConfiguration configuration) {
        getSim().addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );

        return this;
    }

    public FluentCompactStatefulKnowledgeSession end() {
        return ksession;
    }

}
