/*
 * Copyright 2012 JBoss by Red Hat.
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
 */
package org.drools.simulation.impl;

import java.util.Collections;
import java.util.Set;
import org.drools.command.GetDefaultValue;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * Class in charge of narrow down the visibility of a Simulator object.
 */
public class SimulatorContext implements GetDefaultValue{
    
    private final Set<StatefulKnowledgeSession> ksessions;
    
    private final long startTime;
    private Object lastReturnValue;

    public SimulatorContext(Set<StatefulKnowledgeSession> ksessions, long startTime) {
        this.ksessions = ksessions;
        this.startTime = startTime;
    }

    public Set<StatefulKnowledgeSession> getKsessions() {
        return Collections.unmodifiableSet(ksessions);
    }
    
    public void addKsession(StatefulKnowledgeSession ksession){
        this.ksessions.add(ksession);
    }

    public long getStartTime() {
        return startTime;
    }
    
    public void setLastReturnValue(Object lastReturnValue){
        this.lastReturnValue = lastReturnValue;
    }
    
    public Object getObject() {
        return lastReturnValue;
    }
    
}
