package org.drools.grid.internal.commands;

import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.grid.internal.NodeData;

public class ExecutionNodeContext
    implements
    Context {
    private Context            context;
    private NodeData data;

    public ExecutionNodeContext(Context context,
                                       NodeData data) {
        this.data = data;
    }

    public NodeData getServiceManagerData() {
        return this.data;
    }

    public ContextManager getContextManager() {
        return context.getContextManager();
    }

    public String getName() {
        return context.getName();
    }

    public Object get(String identifier) {
        return context.get( identifier );
    }

    public void set(String identifier,
                    Object value) {
        context.set( identifier,
                     value );
    }

    public void remove(String name) {
        context.remove(name);
    }

}
