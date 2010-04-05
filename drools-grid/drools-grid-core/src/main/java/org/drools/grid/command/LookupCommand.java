package org.drools.grid.command;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.grid.generic.NodeData;

public class LookupCommand
    implements
    GenericCommand<String> {

    private String identifier;

    private String outIdentifier;

    public LookupCommand(String identfier) {
        this.identifier = identfier;
    }

    public LookupCommand(String identfier,
                         String outIdentifier) {
        this.identifier = identfier;
        this.outIdentifier = outIdentifier;
    }

    public String execute(Context context) {
        NodeData data = (NodeData) context.get( NodeData.NODE_DATA );

        String instanceId = (String) data.getRoot().get( identifier );

        if ( this.outIdentifier != null ) {
            ((ExecutionResultImpl) ((KnowledgeCommandContext) context).getExecutionResults()).getResults().put( this.outIdentifier,
                                                                                                                instanceId );
        }
        return instanceId;
    }

}
