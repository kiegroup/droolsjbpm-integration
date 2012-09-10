package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.simulation.PathContext;

public class SimulationPath {

    private String pathId;
    private List<String> sequenceFlowsIds = new ArrayList<String>();
    private List<String> activityIds = new ArrayList<String>();
    private List<PathContext> origPaths = new ArrayList<PathContext>();
    
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
    
    
}
