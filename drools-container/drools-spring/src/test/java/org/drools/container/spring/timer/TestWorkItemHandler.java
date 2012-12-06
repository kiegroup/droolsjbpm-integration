package org.drools.container.spring.timer;


public class TestWorkItemHandler
    implements
    WorkItemHandler {
    private static WorkItem workItem = null;

    public void abortWorkItem(WorkItem arg0,
                              WorkItemManager arg1) {
        // TODO Auto-generated method stub

    }

    public void executeWorkItem(WorkItem item,
                                WorkItemManager manager) {
        // TODO Auto-generated method stub
        workItem = item;
        System.out.println( "**** Inside TestWorkItemHandler, suspending.... ****" );
    }

    public static WorkItem getWorkItem() {
        return workItem;
    }

}
