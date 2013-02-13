package org.drools.container.spring.timer;

import org.kie.runtime.process.WorkItem;
import org.kie.runtime.process.WorkItemHandler;
import org.kie.runtime.process.WorkItemManager;


public class TestWorkItemHandler
    implements
    WorkItemHandler {
    private static WorkItem workItem = null;

    public void abortWorkItem(WorkItem arg0,
                              WorkItemManager arg1) {
    }

    public void executeWorkItem(WorkItem item,
                                WorkItemManager manager) {
        workItem = item;
        System.out.println( "**** Inside TestWorkItemHandler, suspending.... ****" );
    }

    public static WorkItem getWorkItem() {
        return workItem;
    }

}
