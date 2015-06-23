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

public class ProcessInstanceEndSimulationEvent extends GenericSimulationEvent {

    private String pathId;
    private long processDuration;
    private String processName;
    private String processVersion;
    
    public ProcessInstanceEndSimulationEvent(String processId,
            long processInstanceId, long startTime, long endTime, String pathId
            , String processName, String processVersion) {
        super(processId, processInstanceId, startTime, endTime, "process-instance");
        this.pathId = pathId;
        this.processDuration = endTime - startTime;
        this.processName = processName;
        this.processVersion = processVersion;
    }


    public String getPathId() {
        return pathId;
    }


    public void setPathId(String pathId) {
        this.pathId = pathId;
    }


    @Override
    public String toString() {
        
        return "GenericSimulationEvent[process=" + processId + ", instance=" + processInstanceId 
                + ", uuid=" + uuid + ", pathId=" + pathId + " started at " + new Date(startTime) + " finished at " + new Date(endTime) + "]";
    }


    public long getProcessDuration() {
        return processDuration;
    }


    public void setProcessDuration(long processDuration) {
        this.processDuration = processDuration;
    }


    public String getProcessName() {
        return processName;
    }


    public void setProcessName(String processName) {
        this.processName = processName;
    }


    public String getProcessVersion() {
        return processVersion;
    }


    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    
}
