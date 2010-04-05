package org.drools.grid.command;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.generic.NodeData;

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

        data.getRoot().set( identifier,
                            type + ":" + instanceId );

        return null;
    }

}
