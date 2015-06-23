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

package org.jbpm.simulation.impl.events;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class GatewaySimulationEvent extends GenericSimulationEvent {

    private String activityName;
    private String activityId;
    
    public GatewaySimulationEvent(String processId, long processInstanceId,
            long startTime, long endTime, 
            String activityId, String activityName, String type) {
        super(processId, processInstanceId, startTime, endTime, type);
        this.activityId = activityId;
        this.activityName = activityName;
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

    @Override
    public String toString() {
        
        return "StartSimulationEvent[process=" + processId + ", instance=" + processInstanceId + ", activity=" + activityName + ", startTime=" + new Date(startTime)+"]";
    }
}
