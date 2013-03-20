package org.jbpm.simulation.impl.ht;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllocatedResources {

    private int poolSize;
    private long performedWork = 0;
    private List<Long> allocatedTill = new ArrayList<Long>();
    
    public AllocatedResources(int poolSize) {
        this.poolSize = poolSize;
    }
    
    public AllocatedWork allocate(long startTime, long duration, long limit) {
        long waitTime = 0;
        AllocatedWork allocatedWork = new AllocatedWork(duration);
        performedWork += duration;
        
        if(allocatedTill.size() < poolSize) {
            long allocated = startTime + duration; 
            if (allocated > limit) {
                allocated = limit;
            }
            allocatedTill.add(allocated);
        
            allocatedWork.setAllocatedTime(allocated - startTime);
            allocatedWork.setWaitTime(waitTime);
         } else {
             Collections.sort(allocatedTill);
        
             long allocated = allocatedTill.get(0);
             
             if (allocated == limit) {
                 waitTime = allocated - startTime;
                 allocatedWork.setAllocatedTime(0);
                 allocatedWork.setWaitTime(waitTime);
                 
                 return allocatedWork;
             }
             
             
             if (allocated >= startTime) {
                 waitTime = allocated - startTime;
                 allocated += duration;
        
             } else {
                 allocated = startTime + duration;
             }
             if (allocated > limit) {
                 allocatedTill.set(0, limit);
                 allocatedWork.setAllocatedTime(duration - (allocated - limit));
                 allocatedWork.setWaitTime(waitTime);
             } else {
                 allocatedTill.set(0, allocated);
                 
                 allocatedWork.setAllocatedTime(allocated - startTime);
                 allocatedWork.setWaitTime(waitTime);
             }
             
        }
        
        return allocatedWork;
    }
}
