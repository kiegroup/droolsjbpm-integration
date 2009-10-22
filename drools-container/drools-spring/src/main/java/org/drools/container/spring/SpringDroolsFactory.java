package org.drools.container.spring;

import java.io.IOException;
import java.util.Collection;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.io.InternalResource;
import org.drools.io.Resource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;

public class SpringDroolsFactory {
    private static KnowledgeBase buildKnowledgeBase(Collection<Resource> resources) {        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for ( Resource resource : resources ) {            
            if ( ((InternalResource)resource).getConfiguration() == null ) {
                kbuilder.add( resource, ((InternalResource)resource).getResourceType() );                
            } else {
                kbuilder.add( resource, ((InternalResource)resource).getResourceType(), ((InternalResource)resource).getConfiguration() );                
            }
                        
        }
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        
        return kbase;
    }
    
    private static StatefulKnowledgeSession newStatefulKnowledgeSession(KnowledgeBase kbase) {
        return kbase.newStatefulKnowledgeSession();
    }
    
    private static StatelessKnowledgeSession newStatelessKnowledgeSession(KnowledgeBase kbase) {
        return kbase.newStatelessKnowledgeSession();
    }    
}
