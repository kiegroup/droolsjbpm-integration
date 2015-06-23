/*
 * Copyright 2015 JBoss Inc
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

package org.jbpm.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.core.time.SessionPseudoClock;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.impl.ht.StaffPoolManager;

public class SimulationContext {

    protected static InheritableThreadLocal<SimulationContext> simulationContextThreadLocal = new InheritableThreadLocal<SimulationContext>();

    private SimulationRegistry registry;
    private SimulationRepository repository;
    private SimulationDataProvider dataProvider;
    private SimulationPath currentPath;
    private long startTime;
    private SessionPseudoClock clock;
    private StaffPoolManager staffPoolManager;
    private long maxEndTime;
    private List<String> executedNodes = new ArrayList<String>();
    private int loopLimit = 2;
    private long processInstanceId;
    
    public static SimulationContext getContext() {
        return simulationContextThreadLocal.get();
    }

    public static void setContext(SimulationContext context) {
        simulationContextThreadLocal.set(context);
    }
    
    public SimulationRepository getRepository() {
        return repository;
    }
    
    public SimulationRegistry getRegistry() {
        return registry;
    }
    
    protected void setRepository(SimulationRepository repository) {
        this.repository = repository;
    }
    
    protected void setRegistry(SimulationRegistry registry) {
        this.registry = registry;
    }

    public SimulationPath getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(SimulationPath currentPath) {
        this.currentPath = currentPath;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public SimulationDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(SimulationDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public SessionPseudoClock getClock() {
        return clock;
    }

    public void setClock(SessionPseudoClock clock) {
        this.clock = clock;
    }

    public StaffPoolManager getStaffPoolManager() {
        return staffPoolManager;
    }

    public void setStaffPoolManager(StaffPoolManager staffPoolManager) {
        this.staffPoolManager = staffPoolManager;
    }

    public long getMaxEndTime() {
        return maxEndTime;
    }

    public void setMaxEndTime(long maxEndTime) {
        if (maxEndTime > this.maxEndTime) {
            this.maxEndTime = maxEndTime;
        }
    }
    
    public void resetMaxEndTime() {
        this.maxEndTime = -1;
    }

    public List<String> getExecutedNodes() {
        return executedNodes;
    }

    public void setExecutedNodes(List<String> executedNodes) {
        this.executedNodes = executedNodes;
    }
    
    public void addExecutedNode(String node) {

        this.executedNodes.add(node);
    }
    
    public boolean isLoopLimitExceeded(String node) {
        int currentCount = Collections.frequency(executedNodes, node);
        return currentCount >= loopLimit;        
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public void incrementProcessInstanceId() {
        this.processInstanceId++;
    }
}
