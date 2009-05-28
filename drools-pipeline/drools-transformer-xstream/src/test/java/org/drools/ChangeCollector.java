package org.drools;

import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.ObjectRetractedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Neale
 */
public class ChangeCollector implements WorkingMemoryEventListener {

    private List<String> retracted;
    private List changes;




    public List<String> getRetracted() {
        return retracted;
    }


    public List getChanges() {
        return changes;
    }


    public void objectInserted(ObjectInsertedEvent event) {
        
    }

    public void objectUpdated(ObjectUpdatedEvent event) {
        if (changes == null) changes = new ArrayList();
        if (event.getObject() instanceof Cheese) {
            Cheese c = (Cheese) event.getObject();
            changes.add(c);
        }
    }

    public void objectRetracted(ObjectRetractedEvent event) {
        if (retracted == null) retracted = new ArrayList<String>();
        if (event.getOldObject() instanceof Cheese) {
            Cheese c = (Cheese) event.getOldObject();
            retracted.add(c.getType());
        }
    }
}
