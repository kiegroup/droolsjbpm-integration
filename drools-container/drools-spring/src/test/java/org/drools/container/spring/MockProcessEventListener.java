/*
 * Copyright 2010 JBoss Inc
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

import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;

public class MockProcessEventListener implements ProcessEventListener {
    public void beforeProcessStarted(ProcessStartedEvent processStartedEvent) {
        System.out.println("MockProcessEventListener :: beforeProcessStarted");
    }

    public void afterProcessStarted(ProcessStartedEvent processStartedEvent) {
        System.out.println("MockProcessEventListener :: afterProcessStarted");
    }

    public void beforeProcessCompleted(ProcessCompletedEvent processCompletedEvent) {
        System.out.println("MockProcessEventListener :: beforeProcessCompleted");
    }

    public void afterProcessCompleted(ProcessCompletedEvent processCompletedEvent) {
        System.out.println("MockProcessEventListener :: afterProcessCompleted");
    }

    public void beforeNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {
        System.out.println("MockProcessEventListener :: beforeNodeTriggered");
    }

    public void afterNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {
        System.out.println("MockProcessEventListener :: afterNodeTriggered");
    }

    public void beforeNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {
        System.out.println("MockProcessEventListener :: beforeNodeLeft");
    }

    public void afterNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {
        System.out.println("MockProcessEventListener :: afterNodeLeft");
    }

    public void beforeVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {
        System.out.println("MockProcessEventListener :: beforeVariableChanged");
    }

    public void afterVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {
        System.out.println("MockProcessEventListener :: afterVariableChanged");
    }
}
