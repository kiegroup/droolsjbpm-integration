package org.drools.fluent;

import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;

public interface FluentKnowledgeBuilder<T> extends FluentBase {
    
    T add(Resource resource,
          ResourceType type);

    T add(Resource resource,
          ResourceType type,
          ResourceConfiguration configuration);   
}