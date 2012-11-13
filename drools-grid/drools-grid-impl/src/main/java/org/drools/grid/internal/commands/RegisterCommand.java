package org.drools.grid.internal.commands;

import org.drools.command.impl.GenericCommand;
import org.drools.grid.io.impl.NodeData;
import org.kie.command.Context;

public class RegisterCommand
    implements
    GenericCommand<Void> {

    private String identifier;
    private String instanceId;
    private int    type;

    public RegisterCommand(String identifier,
                           String instanceId,
                           int type) {
        this.identifier = identifier;
        this.instanceId = instanceId;
        this.type = type;
    }

    public Void execute(Context context) {
        NodeData data = (NodeData) context.get( NodeData.NODE_DATA );

        data.getRoot().set( this.identifier,
                            this.type + ":" + this.instanceId );

        return null;
    }

}
