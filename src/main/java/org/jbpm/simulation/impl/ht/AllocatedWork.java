package org.jbpm.simulation.impl.ht;

public class AllocatedWork {

    private long allocatedTime;
    private long waitTime;
    private long duration;
    
    public AllocatedWork(long duration) {
        this.duration = duration;
    }
    public long getAllocatedTime() {
        return allocatedTime;
    }
    public void setAllocatedTime(long allocatedTime) {
        this.allocatedTime = allocatedTime;
    }
    public long getWaitTime() {
        return waitTime;
    }
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public boolean isAllocated() {
        return duration + waitTime == allocatedTime;
    }
    public void merge(AllocatedWork allocate) {
       this.allocatedTime += allocate.getAllocatedTime();
       this.waitTime += allocate.getWaitTime();
        
    }
}
