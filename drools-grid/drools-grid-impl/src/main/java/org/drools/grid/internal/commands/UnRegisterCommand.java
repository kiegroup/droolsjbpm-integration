package org.drools.grid.internal.commands;

import org.drools.command.impl.GenericCommand;
import org.drools.grid.io.impl.NodeData;
import org.kie.command.Context;

public class UnRegisterCommand
    implements
    GenericCommand<Void> {

    private String identifier;

    public UnRegisterCommand(String identifier) {
        this.identifier = identifier;

    }

    public Void execute(Context context) {
        NodeData data = (NodeData) context.get( NodeData.NODE_DATA );

        data.getRoot().remove( this.identifier );

        return null;
    }

}
