package org.jbpm.simulation;

public class SimulationInfo {

    private long startTime;
    private long endTime;
    private String processId;
    private String processName;
    private String processVersion;
    private int numberOfExecutions;
    
    public SimulationInfo(long startTime, String processId, int numberOfExecutions) {
        this.startTime = startTime;
        this.processId = processId;
        this.numberOfExecutions = numberOfExecutions;
    }
    
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public String getProcessId() {
        return processId;
    }
    public void setProcessId(String processId) {
        this.processId = processId;
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

    public int getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(int numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }
    
    
}
