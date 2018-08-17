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

import org.drools.core.event.AbstractEventSupport;
import org.jbpm.casemgmt.api.event.CaseEventListener;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.process.audit.event.AuditEventBuilder;
import org.jbpm.runtime.manager.impl.PerCaseRuntimeManager;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


public class SpringKModuleDeploymentService extends KModuleDeploymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringKModuleDeploymentService.class);

    private Map<String, WorkItemHandler> handlers = new HashMap<>();
    private List<ProcessEventListener> processEventListeners = new ArrayList<>();
    private List<AgendaEventListener> agendaEventListeners = new ArrayList<>();
    private List<RuleRuntimeEventListener> ruleRuntimeEventListeners = new ArrayList<>();
    private List<TaskLifeCycleEventListener> taskListeners = new ArrayList<>();
    private List<CaseEventListener> caseEventListeners = new ArrayList<>();
    
    @Override
    protected RegisterableItemsFactory getRegisterableItemsFactory(AuditEventBuilder auditLoggerBuilder, KieContainer kieContainer, KModuleDeploymentUnit unit) {
        SpringRegisterableItemsFactory factory = new SpringRegisterableItemsFactory(kieContainer, 
                unit.getKsessionName(), 
                handlers,
                processEventListeners,
                agendaEventListeners,
                ruleRuntimeEventListeners,
                taskListeners);
        factory.setAuditBuilder(auditLoggerBuilder);
        return factory;
    }
    
    public void registerWorkItemHandler(String name, WorkItemHandler handler) {
        logger.debug("Registering {} work item handler under name {}", handler, name);
        handlers.put(name, handler);
    }
    
    public void registerWorkItemHandlers(List<WorkItemHandler> workItemhandlers) {
        
        for (WorkItemHandler handler : workItemhandlers) {
            String name = getComponentName(handler);
    
            if (name != null && !name.toString().isEmpty()) {
                logger.debug("Registering {} work item handler under name {}", handler, name);
                handlers.put(name.toString(), handler);
            } else {
                logger.warn("Not possible to register {} handler due to missing name - annotate your class with @Component with given name", handler);
            }

        }
    }
    
    public void registerProcessEventListener(ProcessEventListener eventListener) {
        this.processEventListeners.add(eventListener);
    }
    
    public void registerProcessEventListeners(List<ProcessEventListener> eventListeners) {
        this.processEventListeners.addAll(eventListeners);
    }
    
    public void registerAgendaEventListener(AgendaEventListener eventListener) {
        this.agendaEventListeners.add(eventListener);
    }
    
    public void registerAgendaEventListeners(List<AgendaEventListener> eventListeners) {
        this.agendaEventListeners.addAll(eventListeners);
    }
    
    public void registerRuleRuntimeEventListener(RuleRuntimeEventListener eventListener) {
        this.ruleRuntimeEventListeners.add(eventListener);
    }
    
    public void registerRuleRuntimeEventListeners(List<RuleRuntimeEventListener> eventListeners) {
        this.ruleRuntimeEventListeners.addAll(eventListeners);
    }
    
    public void registerTaskListener(TaskLifeCycleEventListener eventListener) {
        this.taskListeners.add(eventListener);
    }
    
    public void registerTaskListeners(List<TaskLifeCycleEventListener> eventListeners) {
        this.taskListeners.addAll(eventListeners);
    }
    
    public void registerCaseEventListener(CaseEventListener eventListener) {
        this.caseEventListeners.add(eventListener);
    }
    
    public void registerCaseEventListeners(List<CaseEventListener> eventListeners) {
        this.caseEventListeners.addAll(eventListeners);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void notifyOnDeploy(DeploymentUnit unit, DeployedUnit deployedUnit) {
        super.notifyOnDeploy(unit, deployedUnit);
        InternalRuntimeManager runtimeManager = (InternalRuntimeManager) deployedUnit.getRuntimeManager();
        if (runtimeManager instanceof PerCaseRuntimeManager) {            
            AbstractEventSupport eventSupport =((PerCaseRuntimeManager) runtimeManager).getCaseEventSupport();
            if (caseEventListeners != null) {
                for (CaseEventListener listener : caseEventListeners) {
                    eventSupport.addEventListener(listener);
                }
            }
            
            
        }
    }

    protected String getComponentName(Object component) {
        String name = null;
        if (component.getClass().isAnnotationPresent(Component.class)) {
            name = component.getClass().getAnnotation(Component.class).value();
        }
        
        return name;
    }

}
