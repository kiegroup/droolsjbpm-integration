package org.drools.grid.remote.command;

import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.kie.command.Context;
import org.kie.runtime.process.WorkItemManager;

public class GetWorkItemManagerCommand
    implements
    GenericCommand<WorkItemManager> {

    private static final long serialVersionUID = 1L;

    public WorkItemManager execute(Context context) {
        WorkItemManager workItemManager = ((KnowledgeCommandContext) context).getWorkItemManager();
        return workItemManager;
    }
    
    
   

}
