package org.jbpm.simulation;

public class NodeStatistic {

    private String nodeId;
    
    private Long minTimeStamp;
    private Long maxTimeStamp;
    
    private Long instances;
    
    

    public NodeStatistic(String nodeId, Long minTimeStamp, Long maxTimeStamp,
            Long instances) {
        this.nodeId = nodeId;
        this.minTimeStamp = minTimeStamp;
        this.maxTimeStamp = maxTimeStamp;
        this.instances = instances;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Long getMinTimeStamp() {
        return minTimeStamp;
    }

    public void setMinTimeStamp(Long minTimeStamp) {
        this.minTimeStamp = minTimeStamp;
    }

    public Long getMaxTimeStamp() {
        return maxTimeStamp;
    }

    public void setMaxTimeStamp(Long maxTimeStamp) {
        this.maxTimeStamp = maxTimeStamp;
    }

    public Long getInstances() {
        return instances;
    }

    public void setInstances(Long instances) {
        this.instances = instances;
    }
    
    
}
