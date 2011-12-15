package org.drools.fluent;

public interface VariableContext<P> {
     P get(String name);
    
    <T> T get(String name, Class<T> type);
}