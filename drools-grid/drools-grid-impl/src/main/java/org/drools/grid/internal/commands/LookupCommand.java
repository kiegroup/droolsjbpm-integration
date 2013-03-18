package org.drools.grid.internal.commands;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.grid.io.impl.NodeData;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.command.Context;

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

        String instanceId = (String) data.getRoot().get( this.identifier );

        if ( this.outIdentifier != null ) {
            ((ExecutionResultImpl) ((KnowledgeCommandContext) context).getExecutionResults()).getResults().put( this.outIdentifier,
                                                                                                                instanceId );
        }
        return instanceId;
    }

}
