package org.drools.fluent;

import org.drools.KnowledgeBaseConfiguration;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;

public interface FluentKnowledgeBase<T> extends FluentBase {    
    
    T addKnowledgePackages();
    
    T addKnowledgePackages(Resource resource,
                           ResourceType type);
    
    T addKnowledgePackages(Resource resource,
                           ResourceType type,
                           ResourceConfiguration configuration);   
    
}