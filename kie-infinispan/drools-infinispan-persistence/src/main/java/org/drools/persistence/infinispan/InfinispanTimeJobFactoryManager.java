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

package org.drools.persistence.infinispan;

import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.SelfRemovalJob;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.TimerJobFactoryManager;
import org.drools.core.time.impl.TimerJobInstance;
import org.kie.api.runtime.ExecutableRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfinispanTimeJobFactoryManager
    implements
    TimerJobFactoryManager {

    private ExecutableRunner commandService;
    private Map<Integer, Map<Long, TimerJobInstance>> timerInstances;
    private Map<Long, TimerJobInstance> singleTimerInstances;
    
    public void setCommandService(ExecutableRunner commandService) {
        this.commandService = commandService;
    }
    
    public InfinispanTimeJobFactoryManager() {
        timerInstances = new ConcurrentHashMap<Integer, Map<Long, TimerJobInstance>>();
        singleTimerInstances = new ConcurrentHashMap<Long, TimerJobInstance>();
        
    }
    
    public TimerJobInstance createTimerJobInstance(Job job,
                                                   JobContext ctx,
                                                   Trigger trigger,
                                                   JobHandle handle,
                                                   InternalSchedulerService scheduler) {
        Map<Long, TimerJobInstance> local = null;
        if (hasMethod(ctx.getClass(), "sessionId")) {
            int sessionId = getMethodIntValue(ctx, "getSessionId");
            Map<Long, TimerJobInstance> instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
            local = timerInstances.get(sessionId);
        } else {
            local = singleTimerInstances;
        }
        ctx.setJobHandle( handle );
        InfinispanTimerJobInstance jobInstance = new InfinispanTimerJobInstance( new SelfRemovalJob( job ),
                                                                   new SelfRemovalJobContext( ctx,
                                                                                              local ),
                                                                   trigger,
                                                                   handle,
                                                                   scheduler);
    
        return jobInstance;
    }
    
    public void addTimerJobInstance(TimerJobInstance instance) {
    
        JobContext ctx = instance.getJobContext();
        if (ctx instanceof SelfRemovalJobContext) {
            ctx = ((SelfRemovalJobContext) ctx).getJobContext();
        }
        Map<Long, TimerJobInstance> instances = null;
        if (hasMethod(ctx.getClass() ,"getSessionId")) {
            int sessionId = getMethodIntValue(ctx, "sessionId");
            instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
        } else {
            instances = singleTimerInstances;
        }
        instances.put( instance.getJobHandle().getId(),
                                 instance );        
    }
    
    public void removeTimerJobInstance(TimerJobInstance instance) {
    
        JobContext ctx = instance.getJobContext();
        if (ctx instanceof SelfRemovalJobContext) {
            ctx = ((SelfRemovalJobContext) ctx).getJobContext();
        }
        Map<Long, TimerJobInstance> instances = null;
        if (hasMethod(ctx.getClass(), "getSessionId")) {
            int sessionId = getMethodIntValue(ctx, "getSessionId");
            instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
        } else {
            instances = singleTimerInstances;
        }
        instances.remove( instance.getJobHandle().getId() );        
    }
    
    public Collection<TimerJobInstance> getTimerJobInstances() {
        return singleTimerInstances.values();
    }
    
    public Collection<TimerJobInstance> getTimerJobInstances(Integer sessionId) {
        Map<Long, TimerJobInstance> sessionTimerJobs = timerInstances.get(sessionId);
        if (sessionTimerJobs == null) {
            return Collections.emptyList();
        }
        return sessionTimerJobs.values();
    }
    
    public ExecutableRunner getCommandService() {
        return this.commandService;
    }
    
    private boolean hasMethod(Class<?> clazz, String methodName) {
    	try {
    		clazz.getDeclaredMethod(methodName);
    		return true;
    	} catch (Exception e) {
    		try {
    			clazz.getMethod(methodName);
    			return true;
    		} catch (Exception e2) {}
    	}
    	return false;
    }

    private int getMethodIntValue(Object obj, String methodName) {
    	java.lang.reflect.Method m = null;
    	Class<?> clazz = obj.getClass();
    	try {
    		m = clazz.getDeclaredMethod(methodName);
    	} catch (Exception e) {
    		try {
    			m = clazz.getMethod(methodName);
    		} catch (Exception e2) {}
    	}
    	try {
    		return (Integer) m.invoke(obj);
    	} catch (Exception e) {
    		return -1;
    	}
    }
    
}
