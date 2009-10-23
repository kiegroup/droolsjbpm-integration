package org.drools.container.spring;

import java.io.IOException;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.io.InternalResource;
import org.drools.io.Resource;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;

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
    
    private static ServiceManager buildServiceManager(Map<String, CommandExecutor> map) {
        ServiceManagerLocalClient sm = new ServiceManagerLocalClient();
        for ( Entry<String, CommandExecutor> entry : map.entrySet() ) {
            sm.register( entry.getKey(), entry.getValue() );
        }
        return sm; 
    }      
    
    
}
