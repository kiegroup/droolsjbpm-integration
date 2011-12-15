package org.drools.fluent;

import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;

public interface FluentStatefulKnowledgeSession<T>  extends FluentBase  {
            
    T fireAllRules();
    
    T insert(Object object);  
    
    T setGlobal( String identifier, Object object );
    
    /**
     * The last executed command, if it returns a value, is set to a name in this executings context
     * @param name
     * @return
     */
    FluentStatefulKnowledgeSession<T> set(String name);    
}