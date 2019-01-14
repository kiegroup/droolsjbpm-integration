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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.runtime.manager.impl.KModuleRegisterableItemsFactory;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


public class SpringRegisterableItemsFactory extends KModuleRegisterableItemsFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringRegisterableItemsFactory.class);
    
    private Map<String, WorkItemHandler> handlers;
    private List<ProcessEventListener> processEventListeners;
    private List<AgendaEventListener> agendaEventListeners;
    private List<RuleRuntimeEventListener> ruleRuntimeEventListeners;
    private List<TaskLifeCycleEventListener> taskListeners;
    
    private ApplicationContext context;

    public SpringRegisterableItemsFactory(ApplicationContext context, 
            KieContainer kieContainer, 
            String ksessionName) {
        super(kieContainer, ksessionName);
        this.context = context;       
    }


    @Override
    public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
        Map<String, WorkItemHandler> workItemHandlers = super.getWorkItemHandlers(runtime);
        processHandlers();
        
        workItemHandlers.putAll(handlers);
        
        return workItemHandlers;
    }

    @Override
    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
        List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
        processProcessEventListeners();
    
        listeners.addAll(processEventListeners);
        
        return listeners;
    }

    @Override
    public List<AgendaEventListener> getAgendaEventListeners(RuntimeEngine runtime) {        
        List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
        processAgendaEventListeners();
        
        listeners.addAll(agendaEventListeners);
        
        return listeners;
    }

    @Override
    public List<RuleRuntimeEventListener> getRuleRuntimeEventListeners(RuntimeEngine runtime) {
        List<RuleRuntimeEventListener> listeners = super.getRuleRuntimeEventListeners(runtime);
        processRuleRuntimeEventListeners();
        
        listeners.addAll(ruleRuntimeEventListeners);
        
        return listeners;
    }

    @Override
    public List<TaskLifeCycleEventListener> getTaskListeners() {
        
        List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
        processTaskEventListeners();
        
        listeners.addAll(taskListeners);
        
        return listeners;
    }
    
    
    /*
     * Helper methods
     */
    
    protected void processHandlers() {
        // processing should only be done once
        if (handlers == null) {
            handlers = new HashMap<>();
            Map<String, WorkItemHandler> foundBeans = context.getBeansOfType(WorkItemHandler.class);
            for (WorkItemHandler handler : foundBeans.values()) {
                String name = getComponentName(handler);
        
                if (name != null && !name.toString().isEmpty()) {
                    logger.debug("Registering {} work item handler under name {}", handler, name);
                    handlers.put(name.toString(), handler);
                } else {
                    logger.warn("Not possible to register {} handler due to missing name - annotate your class with @Component with given name", handler);
                }
    
            }
        }
    }
    
    protected void processAgendaEventListeners() {
        // processing should only be done once
        if (agendaEventListeners == null) {
            agendaEventListeners = new ArrayList<>();
            Map<String, AgendaEventListener> foundBeans = context.getBeansOfType(AgendaEventListener.class);
            for (AgendaEventListener listener : foundBeans.values()) {
                logger.debug("Registering {} agenda event listener", listener);
                agendaEventListeners.add(listener);
                
    
            }
        }
    }
    
    protected void processRuleRuntimeEventListeners() {
        // processing should only be done once
        if (ruleRuntimeEventListeners == null) {
            ruleRuntimeEventListeners = new ArrayList<>();
            Map<String, RuleRuntimeEventListener> foundBeans = context.getBeansOfType(RuleRuntimeEventListener.class);
            for (RuleRuntimeEventListener listener : foundBeans.values()) {
                logger.debug("Registering {} rule runtime event listener", listener);
                ruleRuntimeEventListeners.add(listener);
                
    
            }
        }
    }
    
    protected void processTaskEventListeners() {
        // processing should only be done once
        if (taskListeners == null) {
            taskListeners = new ArrayList<>();
            Map<String, TaskLifeCycleEventListener> foundBeans = context.getBeansOfType(TaskLifeCycleEventListener.class);
            for (TaskLifeCycleEventListener listener : foundBeans.values()) {
                logger.debug("Registering {} task event listener", listener);
                taskListeners.add(listener);
                
    
            }
        }
    }
    
    protected void processProcessEventListeners() {
        // processing should only be done once
        if (processEventListeners == null) {
            processEventListeners = new ArrayList<>();
            Map<String, ProcessEventListener> foundBeans = context.getBeansOfType(ProcessEventListener.class);
            for (ProcessEventListener listener : foundBeans.values()) {
                logger.debug("Registering {} process event listener", listener);
                processEventListeners.add(listener);
                
    
            }
        }
    }
    
    
    protected String getComponentName(Object component) {
        String name = null;
        if (component.getClass().isAnnotationPresent(Component.class)) {
            name = component.getClass().getAnnotation(Component.class).value();
            
        } else if (component.getClass().isAnnotationPresent(Wid.class)) {
            name = component.getClass().getAnnotation(Wid.class).name();
        }
        
        return name;
    }

}
