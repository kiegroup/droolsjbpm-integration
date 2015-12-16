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
