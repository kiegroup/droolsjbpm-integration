package org.drools.fluent;

public interface FluentRoot {
    <P> VariableContext<P> getVariableContext();
}