package org.drools.grid.internal.commands;

import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.grid.internal.NodeData;

public class ExecutionNodeContext
    implements
    Context {
    private Context  context;
    private NodeData data;

    public ExecutionNodeContext(Context context,
                                NodeData data) {
        this.data = data;
    }

    public NodeData getServiceManagerData() {
        return this.data;
    }

    public ContextManager getContextManager() {
        return this.context.getContextManager();
    }

    public String getName() {
        return this.context.getName();
    }

    public Object get(String identifier) {
        return this.context.get( identifier );
    }

    public void set(String identifier,
                    Object value) {
        this.context.set( identifier,
                          value );
    }

    public void remove(String name) {
        this.context.remove( name );
    }

}
