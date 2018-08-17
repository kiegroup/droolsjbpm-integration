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

package org.jbpm.springboot.services;

import java.util.List;
import java.util.Map;

import org.jbpm.runtime.manager.impl.KModuleRegisterableItemsFactory;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;


public class SpringRegisterableItemsFactory extends KModuleRegisterableItemsFactory {
    
    private Map<String, WorkItemHandler> handlers;
    private List<ProcessEventListener> processEventListeners;
    private List<AgendaEventListener> agendaEventListeners;
    private List<RuleRuntimeEventListener> ruleRuntimeEventListeners;
    private List<TaskLifeCycleEventListener> taskListeners;

    public SpringRegisterableItemsFactory(KieContainer kieContainer, 
            String ksessionName,
            Map<String, WorkItemHandler> handlers,
            List<ProcessEventListener> processEventListeners,
            List<AgendaEventListener> agendaEventListeners,
            List<RuleRuntimeEventListener> ruleRuntimeEventListeners,
            List<TaskLifeCycleEventListener> taskListeners) {
        super(kieContainer, ksessionName);
        
        this.handlers = handlers;
        this.processEventListeners = processEventListeners;
        this.agendaEventListeners = agendaEventListeners;
        this.ruleRuntimeEventListeners = ruleRuntimeEventListeners;
        this.taskListeners = taskListeners;
    }


    @Override
    public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
        Map<String, WorkItemHandler> workItemHandlers = super.getWorkItemHandlers(runtime);
        
        if (handlers != null) {
            workItemHandlers.putAll(handlers);
        }
        
        return workItemHandlers;
    }

    @Override
    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
        List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
        if (processEventListeners != null) {
            listeners.addAll(processEventListeners);
        }
        return listeners;
    }

    @Override
    public List<AgendaEventListener> getAgendaEventListeners(RuntimeEngine runtime) {        
        List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
        if (agendaEventListeners != null) {
            listeners.addAll(agendaEventListeners);
        }
        return listeners;
    }

    @Override
    public List<RuleRuntimeEventListener> getRuleRuntimeEventListeners(RuntimeEngine runtime) {
        List<RuleRuntimeEventListener> listeners = super.getRuleRuntimeEventListeners(runtime);
        if (ruleRuntimeEventListeners != null) {
            listeners.addAll(ruleRuntimeEventListeners);
        }
        return listeners;
    }

    @Override
    public List<TaskLifeCycleEventListener> getTaskListeners() {
        
        List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
        if (taskListeners != null) {
            listeners.addAll(taskListeners);
        }
        return listeners;
    }

}
