package org.drools.container.spring;

import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;

/**
 * Created by IntelliJ IDEA.
 * Date: 6/13/11
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockWorkingMemoryEventListener implements WorkingMemoryEventListener{
    public void objectInserted(ObjectInsertedEvent objectInsertedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectInserted");
    }

    public void objectUpdated(ObjectUpdatedEvent objectUpdatedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectUpdated");
    }

    public void objectRetracted(ObjectRetractedEvent objectRetractedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectRetracted");
    }
}
