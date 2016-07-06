package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.RequestContextImpl;
import org.drools.core.command.impl.ContextImpl;
import org.drools.core.command.impl.GenericCommand;
import org.kie.internal.command.Context;
import org.kie.internal.fluent.Scope;

public class SetCommand<T> implements GenericCommand<T> {
    private String name;
    private Scope scope;

    public SetCommand(String name) {
        this.name = name;
    }

    public SetCommand(String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    @Override
    public T execute(Context context) {
        RequestContextImpl reqContext = (RequestContextImpl)context;
        T returned = (T) reqContext.getLastReturned();

        if ( scope == Scope.REQUEST ) {
            reqContext.set(name, returned);
        } else if ( scope == Scope.CONVERSATION ) {
            if ( reqContext.getConversationContext() == null ) {
                throw new IllegalStateException("No Conversation Context Exists");
            }
            reqContext.getConversationContext().set(name, returned);
        } else  if ( scope == Scope.APPLICATION ) {
            if ( reqContext.getConversationContext() == null ) {
                throw new IllegalStateException("No Application Context Exists");
            }
            reqContext.getApplicationContext().set(name, returned);
        }

        ((RequestContextImpl)context).setLastSet(name);
        return returned;
    }

    @Override
    public String toString() {
        return "SetCommand{" +
               "name='" + name + '\'' +
               ", scope=" + scope +
               '}';
    }
}
