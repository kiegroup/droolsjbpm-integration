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

public class ActivitySimulationEvent extends GenericSimulationEvent {
    
    protected String activityName;
    protected String activityId;
    protected long duration;

    protected String type;
    
    public ActivitySimulationEvent(String processId, long processInstanceId,
            String activityName, String activityId, long duration, long startTime, long endTime, String type) {
        super(processId, processInstanceId, startTime, endTime, type);
        this.activityName = activityName;
        this.activityId = activityId;
        this.duration = duration;
    }
    

    public String getActivityName() {
        if (StringUtils.isNotEmpty(this.activityName)) {
            return this.activityName;
        }

        return this.activityId;
    }

    public String getActivityId() {
        return this.activityId;
    }

    public long getDuration() {
        return this.duration;
    }


    @Override
    public String toString() {
        
        return "ActivitySimulationEvent[process=" + processId + ", type = " + type + " instance="
        + processInstanceId + ", activity=" + activityName + ", duration=" + duration/1000+" seconds]";
    }


    public String getType() {
        return this.type;
    }
}
