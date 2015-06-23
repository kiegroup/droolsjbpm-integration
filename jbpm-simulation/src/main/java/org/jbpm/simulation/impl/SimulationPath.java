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

package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.simulation.PathContext;

public class SimulationPath {

    private String pathId;
    private List<String> sequenceFlowsIds = new ArrayList<String>();
    private List<String> activityIds = new ArrayList<String>();
    private List<String> boundaryEventIds = new ArrayList<String>();
    private List<PathContext> origPaths = new ArrayList<PathContext>();
    private Map<String, String> catchEvents = new HashMap<String, String>();
    private Map<String, String> throwEvents = new HashMap<String, String>();
    private Map<String, String> seqenceFlowsSources = new HashMap<String, String>();

    private String signalName;

    private boolean startable = false;
    
    private double probability;
    
    public double getProbability() {
        return probability;
    }
    public void setProbability(double probability) {
        this.probability = probability;
    }
    public void addSequenceFlow(String id) {
        this.sequenceFlowsIds.add(id);
    }
    public void addActivity(String id) {
        this.activityIds.add(id);
    }
    public List<String> getSequenceFlowsIds() {
        return sequenceFlowsIds;
    }
    public void setSequenceFlowsIds(List<String> sequenceFlowsIds) {
        this.sequenceFlowsIds = sequenceFlowsIds;
    }
    public List<String> getActivityIds() {
        return activityIds;
    }
    public void setActivityIds(List<String> activityIds) {
        this.activityIds = activityIds;
    }
    public List<PathContext> getOrigPaths() {
        return origPaths;
    }
    public void setOrigPaths(List<PathContext> origPaths) {
        this.origPaths = origPaths;
    }
    public String getPathId() {
        return pathId;
    }
    public void setPathId(String pathId) {
        this.pathId = pathId;
    }
    public List<String> getBoundaryEventIds() {
        return boundaryEventIds;
    }
    public void setBoundaryEventIds(List<String> boundaryEventIds) {
        this.boundaryEventIds = boundaryEventIds;
    }
    public void addBoundaryEventId(String id) {
        this.boundaryEventIds.add(id);
    }
    public Map<String, String> getCatchEvents() {
        return catchEvents;
    }
    public void setCatchEvents(Map<String, String> catchEvents) {
        this.catchEvents = catchEvents;
    }
    public void addCatchEvent(String ref, String activityId) {
        this.catchEvents.put(ref, activityId);
    }
    public Map<String, String> getThrowEvents() {
        return throwEvents;
    }
    public void setThrowEvents(Map<String, String> throwEvents) {
        this.throwEvents = throwEvents;
    }
    public void addThrowEvent(String activityId, String ref) {
        this.throwEvents.put(activityId, ref);
    }
    public String getSignalName() {
        return signalName;
    }
    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }
    public boolean isStartable() {
        return startable;
    }
    public void setStartable(boolean startable) {
        this.startable = startable;
    }
    public Map<String, String> getSeqenceFlowsSources() {
        return seqenceFlowsSources;
    }
    public void setSeqenceFlowsSources(Map<String, String> seqenceFlowsSources) {
        this.seqenceFlowsSources = seqenceFlowsSources;
    }
    public void addSequenceFlowSource(String seqId, String sourceElemId) {
        this.seqenceFlowsSources.put(seqId, sourceElemId);
    }
}
