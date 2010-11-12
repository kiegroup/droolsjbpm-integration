/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.drools.grid.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.command.impl.ContextImplWithEviction;
import org.drools.time.Job;
import org.drools.time.JobContext;

/**
 *
 * @author salaboy
 */
public class EvictionJob implements Job {
    private static Logger log = Logger.getLogger( EvictionJob.class.getName() );
    private ContextImplWithEviction contextImpl;

    public EvictionJob(ContextImplWithEviction contextImpl) {
        this.contextImpl = contextImpl;
    }
    
    
    
    public void execute(JobContext ctx) {
           Map<String, Long> evictionMap = contextImpl.getEvictionMap();
            for(String key : evictionMap.keySet()){
                Long lastTimeAccessed = evictionMap.get(key);
                long validTime = (lastTimeAccessed + contextImpl.getEntryEvictionTime());
                
                long evicted = validTime - System.currentTimeMillis();
                if( evicted < 0 ){
                    log.log(Level.FINE, "Removing _TEMP_ key ="+key+" Based on evictions policies / Evicted Time="+evicted);
                    evictionMap.remove(key);
                    contextImpl.remove(key);
                    
                }
            }    
    }

}
