package org.jbpm.simulation.impl.ht;

import java.util.Set;
import java.util.TreeSet;

import org.joda.time.Interval;

public class RangeChain {

    private Set<Range> availableRanges = new TreeSet<Range>();
    
    public void addRange(Range range) {
        this.availableRanges.add(range);
    }
    
    public long allocateWork(long startTime, long duration) {
        AllocatedWork allocatedWork = new AllocatedWork(duration);
        Interval previousRange = null;
        for (Range range : availableRanges) {
            if (range.contains(startTime)) {
               
                allocatedWork.merge(range.allocate(startTime, duration));
                
                if (allocatedWork.isAllocated()) {
                    break;
                } else {
                    
                }
                
                previousRange = range.getInterval();
            } else if (!allocatedWork.isAllocated()) {
                long gap = 0;
                if (previousRange != null) {
                    gap = previousRange.gap(range.getInterval()).toDurationMillis();
                    allocatedWork.setWaitTime(allocatedWork.getWaitTime() + gap);
                }
                allocatedWork.merge(range.allocate(range.getInterval().getStartMillis(), duration-allocatedWork.getAllocatedTime()));
            }
        }
        
        return allocatedWork.getWaitTime();
    }
}
