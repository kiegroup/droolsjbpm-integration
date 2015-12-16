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

package org.jbpm.simulation.impl.events;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.simulation.AggregatedSimulationEvent;


public class AggregatedActivitySimulationEvent implements AggregatedSimulationEvent {

    protected String type;

    protected String activityName;
    protected String activityId;
    
    protected double minExecutionTime;
    protected double avgExecutionTime;
    protected double maxExecutionTime;
    
    protected long numberOfInstances; 
    
    
    public AggregatedActivitySimulationEvent(String activityName, String activityId,
            double minExecutionTime, double avgExecutionTime,
            double maxExecutionTime, long numberOfInstances, String type) {
        super();
        this.activityName = activityName;
        this.activityId = activityId;
        this.minExecutionTime = minExecutionTime;
        this.avgExecutionTime = avgExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.numberOfInstances = numberOfInstances;
        this.type = type;
    }
    public double getMinExecutionTime() {
        return minExecutionTime;
    }
    public void setMinExecutionTime(double minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }
    public double getAvgExecutionTime() {
        return avgExecutionTime;
    }
    public void setAvgExecutionTime(double avgExecutionTime) {
        this.avgExecutionTime = avgExecutionTime;
    }
    public double getMaxExecutionTime() {
        return maxExecutionTime;
    }
    public void setMaxExecutionTime(double maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }
    public String getActivityName() {
        if (StringUtils.isNotEmpty(this.activityName)) {
            return this.activityName;
        }

        return this.activityId;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getActivityId() {
        return activityId;
    }
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    public Object getProperty(String name) {
        if ("activityId".equalsIgnoreCase(name)) {
         
            return activityId;
        } else if ("activityName".equalsIgnoreCase(name)) {
            
            return activityName;
        } else if ("minExecutionTime".equalsIgnoreCase(name)) {
            
            return minExecutionTime;
        } else if ("avgExecutionTime".equalsIgnoreCase(name)) {
         
            return avgExecutionTime;
        } else if ("maxExecutionTime".equalsIgnoreCase(name)) {
         
            return maxExecutionTime;
        }
        
        return null;
    }

    public String getType() {
        return this.type;
    }

    public long getNumberOfInstances() {
        return numberOfInstances;
    }
    public void setNumberOfInstances(long numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    
}
