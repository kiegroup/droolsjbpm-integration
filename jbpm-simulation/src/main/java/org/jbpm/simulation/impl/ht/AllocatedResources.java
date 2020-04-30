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

        if(poolSize == 0) {
            // no available resources
            allocatedWork.setAllocatedTime(startTime + duration);
            allocatedWork.setWaitTime(duration);

            return allocatedWork;
        }
        
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
