package org.kie.server.springboot.samples.workitems;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.springframework.stereotype.Component;

@Component("External Send Task")
public class ExternalSendTaskWIH implements WorkItemHandler {

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    }

}
