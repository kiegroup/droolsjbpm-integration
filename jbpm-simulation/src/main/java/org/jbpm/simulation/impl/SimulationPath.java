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
}
