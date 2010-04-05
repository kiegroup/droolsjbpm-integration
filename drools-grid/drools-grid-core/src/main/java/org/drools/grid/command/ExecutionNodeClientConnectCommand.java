package org.drools.grid.command;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.grid.generic.NodeData;

public class ExecutionNodeClientConnectCommand
    implements
    GenericCommand<Integer> {
    
    private String outIdentifier;

    

    public ExecutionNodeClientConnectCommand(String outIdentifier) {
        this.outIdentifier = outIdentifier;
    }



    public Integer execute(Context context) {
        NodeData data = (NodeData) context.get( NodeData.NODE_DATA );
        
        Integer sessionId = data.getSessionIdCounter().getAndIncrement();
        if ( this.outIdentifier != null ) {
            ((ExecutionResultImpl)((KnowledgeCommandContext) context).getExecutionResults()).getResults().put( this.outIdentifier, sessionId );
        } 
        
        return sessionId;
    }

}
