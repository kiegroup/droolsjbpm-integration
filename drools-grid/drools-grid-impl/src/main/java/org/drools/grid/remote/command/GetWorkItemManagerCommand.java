package org.drools.grid.remote.command;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.kie.api.command.Context;
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
