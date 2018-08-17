/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.springboot.samples.listeners;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomProcessEventListener implements ProcessEventListener {

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {

    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
       
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
       
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
       
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        
    }

}
