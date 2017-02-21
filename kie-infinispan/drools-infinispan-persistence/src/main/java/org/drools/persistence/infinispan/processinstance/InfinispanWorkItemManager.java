/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.drools.persistence.infinispan.processinstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.persistence.api.PersistenceContext;
import org.drools.persistence.api.PersistenceContextManager;
import org.drools.persistence.api.PersistentWorkItem;
import org.drools.persistence.info.WorkItemInfo;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;

public class InfinispanWorkItemManager extends JPAWorkItemManager implements WorkItemManager {

    private InternalKnowledgeRuntime kruntime;
    private Map<String, WorkItemHandler> workItemHandlers = new HashMap<String, WorkItemHandler>();
    
    public InfinispanWorkItemManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
        this.kruntime = kruntime;
    }
    
    @Override
    public void internalExecuteWorkItem(WorkItem workItem) {
        Environment env = this.kruntime.getEnvironment();
//        EntityManager em = (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);

        WorkItemInfo workItemInfo = new WorkItemInfo(workItem, env);
//        em.persist(workItemInfo);
        
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
        context.persist( workItemInfo );

        ((WorkItemImpl) workItem).setId(workItemInfo.getId());
        workItemInfo.transform();
        context.merge(workItemInfo);
        
        WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
        if (handler != null) {
            handler.executeWorkItem(workItem, this);
        	workItemInfo.transform();
        	context.merge(workItemInfo);
        } else {
            throwWorkItemNotFoundException( workItem );
        }
    }

    private void throwWorkItemNotFoundException(WorkItem workItem) {
        throw new WorkItemHandlerNotFoundException( "Could not find work item handler for " + workItem.getName(),
                                                    workItem.getName() );
    }
    
    @Override
    public WorkItemHandler getWorkItemHandler(String name) {
    	return this.workItemHandlers.get(name);
    }
    
    @Override
    public void retryWorkItem(long workItemId) {
    	WorkItem workItem = getWorkItem(workItemId);
    	if (workItem != null) {
            WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
            if (handler != null) {
                handler.executeWorkItem(workItem, this);
            } else {
                throwWorkItemNotFoundException( workItem );
            }
    	}
    }
    
    @Override
    public void retryWorkItemWithParams(long workItemId,Map<String,Object> map) {
        
        Environment env = this.kruntime.getEnvironment();
        WorkItem workItem = getWorkItem(workItemId);
        
        if (workItem != null) {
            workItem.setParameters( map );
            WorkItemInfo workItemInfo = new WorkItemInfo(workItem, env);
          
            PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
            context.merge( workItemInfo );
            retryWorkItem(workItemInfo.getId());
        }
    }

    @Override
    public void internalAbortWorkItem(long id) {
        Environment env = this.kruntime.getEnvironment();
        //EntityManager em = (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();

        
        PersistentWorkItem workItemInfo = context.findWorkItem( id );
        // work item may have been aborted
        if (workItemInfo != null) {
            WorkItemImpl workItem = (WorkItemImpl) internalGetWorkItem(workItemInfo);
            WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
            if (handler != null) {
                handler.abortWorkItem(workItem, this);
            } else {
                throwWorkItemNotFoundException( workItem );
            }
            context.remove(workItemInfo);
        }
    }

    @Override
    public void internalAddWorkItem(WorkItem workItem) {
    }

    @Override
    public void completeWorkItem(long id, Map<String, Object> results) {
        Environment env = this.kruntime.getEnvironment();
//        EntityManager em = (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();

        
        PersistentWorkItem workItemInfo = context.findWorkItem( id );
        
        // work item may have been aborted
        if (workItemInfo != null) {
            WorkItem workItem = internalGetWorkItem(workItemInfo);
            workItem.setResults(results);
            ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
            workItem.setState(WorkItem.COMPLETED);
            // process instance may have finished already
            if (processInstance != null) {
                processInstance.signalEvent("workItemCompleted", workItem);
            }
            context.remove(workItemInfo);
        }
    }

    @Override
    public void abortWorkItem(long id) {
        Environment env = this.kruntime.getEnvironment();
//        EntityManager em = (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();

        PersistentWorkItem workItemInfo = context.findWorkItem( id );
        
        // work item may have been aborted
        if (workItemInfo != null) {
            WorkItem workItem = (WorkItemImpl) internalGetWorkItem(workItemInfo);
            ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
            workItem.setState(WorkItem.ABORTED);
            // process instance may have finished already
            if (processInstance != null) {
                processInstance.signalEvent("workItemAborted", workItem);
            }
            context.remove(workItemInfo);
        }
    }

    @Override
    public WorkItem getWorkItem(long id) {
        Environment env = this.kruntime.getEnvironment();
//        EntityManager em = (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
        PersistenceContext context = ((PersistenceContextManager) env.get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getCommandScopedPersistenceContext();
        
        PersistentWorkItem workItemInfo = null;
        
        if (context != null) {
            workItemInfo = context.findWorkItem( id );
        }

        if (workItemInfo == null) {
            return null;
        }
        return internalGetWorkItem(workItemInfo);
    }

    private WorkItem internalGetWorkItem(PersistentWorkItem workItemInfo) { 
        Environment env = kruntime.getEnvironment();
        InternalKnowledgeBase ruleBase = (InternalKnowledgeBase) kruntime.getKieBase();
        WorkItem workItem = ((WorkItemInfo) workItemInfo).getWorkItem(env, ruleBase); 
        ((WorkItemImpl) workItem).setId(workItemInfo.getId());
        return workItem;
    }
    
    @Override
    public Set<WorkItem> getWorkItems() {
        return new HashSet<WorkItem>();
    }

    @Override
    public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
    	super.registerWorkItemHandler(workItemName, handler);
        this.workItemHandlers.put(workItemName, handler);
    }

    @Override
    public void clearWorkItems() {
    }

    @Override
    public void clear() {
        clearWorkItems();
    }
}
