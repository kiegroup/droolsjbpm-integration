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

public class HumanTaskActivitySimulationEvent extends GenericSimulationEvent {

    private double resourceCost;
    private long waitTime;
    protected String activityName;
    protected String activityId;
    protected long duration;
    private double resourceUtilization;

    public HumanTaskActivitySimulationEvent(String processId,
            long processInstanceId, String activityName, String activityId,
            long duration, long waitTime, double resourceCost, long startTime, long endTime, double resourceUtilization) {
        
        super(processId, processInstanceId, startTime, endTime, "userTask");
        this.duration = duration;
        this.activityId = activityId;
        this.activityName = activityName;
        this.waitTime = waitTime;
        this.resourceCost = resourceCost;
        this.resourceUtilization = resourceUtilization;

    }

    public double getResourceCost() {
        return resourceCost;
    }

    public void setResourceCost(double resourceCost) {
        this.resourceCost = resourceCost;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getResourceUtilization() {
        return resourceUtilization;
    }

    public void setResourceUtilization(double resourceUtilization) {
        this.resourceUtilization = resourceUtilization;
    }
    
    @Override
    public String toString() {
        
        return "HumanTaskActivitySimulationEvent[process=" + processId + ", instance=" 
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds" +
        		", wait time=" + waitTime/1000 + " seconds , resource cost=" +resourceCost +
        		", resource utilization=" +resourceUtilization +"]";
    }
}
