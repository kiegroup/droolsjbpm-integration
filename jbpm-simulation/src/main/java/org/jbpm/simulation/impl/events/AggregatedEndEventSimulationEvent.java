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

public class AggregatedEndEventSimulationEvent implements
        AggregatedSimulationEvent {

    protected String type;

    protected String activityName;
    protected String activityId;
    
    protected double minProcessDuration;
    protected double avgProcessDuration;
    protected double maxProcessDuration;
    
    protected long numberOfInstances; 
    
    public AggregatedEndEventSimulationEvent(String activityName, String activityId, Number minProcessDuration,
            Number avgProcessDuration, Number maxProcessDuration, Number numberOfInstances, String type) {
        super();        
        this.activityName = activityName;
        this.activityId = activityId;
        this.minProcessDuration = minProcessDuration.doubleValue();
        this.avgProcessDuration = avgProcessDuration.doubleValue();
        this.maxProcessDuration = maxProcessDuration.doubleValue();
        this.numberOfInstances = numberOfInstances.longValue();
        this.type = type;
    }

    

    public Object getProperty(String name) {
        if ("activityId".equalsIgnoreCase(name)) {
            
            return activityId;
        } else if ("activityName".equalsIgnoreCase(name)) {
            
            return activityName;
        } else if ("minProcessDuration".equalsIgnoreCase(name)) {
            
            return minProcessDuration;
        } else if ("avgProcessDuration".equalsIgnoreCase(name)) {
         
            return avgProcessDuration;
        } else if ("maxProcessDuration".equalsIgnoreCase(name)) {
         
            return maxProcessDuration;
        }
        return null;
    }

    public String getType() {
        return this.type;
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



    public double getMinProcessDuration() {
        return minProcessDuration;
    }



    public void setMinProcessDuration(double minProcessDuration) {
        this.minProcessDuration = minProcessDuration;
    }



    public double getAvgProcessDuration() {
        return avgProcessDuration;
    }



    public void setAvgProcessDuration(double avgProcessDuration) {
        this.avgProcessDuration = avgProcessDuration;
    }



    public double getMaxProcessDuration() {
        return maxProcessDuration;
    }



    public void setMaxProcessDuration(double maxProcessDuration) {
        this.maxProcessDuration = maxProcessDuration;
    }



    public long getNumberOfInstances() {
        return numberOfInstances;
    }



    public void setNumberOfInstances(long numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

}
