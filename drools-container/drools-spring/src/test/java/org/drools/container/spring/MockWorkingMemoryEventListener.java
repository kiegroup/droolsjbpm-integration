/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring;

import org.kie.event.rule.ObjectInsertedEvent;
import org.kie.event.rule.ObjectRetractedEvent;
import org.kie.event.rule.ObjectUpdatedEvent;
import org.kie.event.rule.WorkingMemoryEventListener;

public class MockWorkingMemoryEventListener implements WorkingMemoryEventListener {
    public void objectInserted(ObjectInsertedEvent objectInsertedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectInserted");
        //System.out.println(objectInsertedEvent.getObject());
        SpringDroolsListenersTest.incrementValueFromListener();
    }

    public void objectUpdated(ObjectUpdatedEvent objectUpdatedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectUpdated");
    }

    public void objectRetracted(ObjectRetractedEvent objectRetractedEvent) {
        System.out.println("MockWorkingMemoryEventListener :: objectRetracted");
    }
}
