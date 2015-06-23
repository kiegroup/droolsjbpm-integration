/*
 * Copyright 2015 JBoss Inc
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

package org.drools.simulation.fluent.session.impl;

import java.util.Map;

import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.RegisterWorkItemHandlerCommand;
import org.drools.simulation.fluent.session.KieSessionSimulationFluent;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.fluent.runtime.KieSessionFluent;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

public class DefaultWorkItemManagerSimFluentImpl implements WorkItemManagerFluent<WorkItemManagerFluent, KieSessionSimulationFluent> {
 
    private DefaultStatefulKnowledgeSessionSimFluent parent;
    
    public DefaultWorkItemManagerSimFluentImpl(DefaultStatefulKnowledgeSessionSimFluent parent) { 
        this.parent = parent;
    }

    @Override
    public WorkItemManagerFluent completeWorkItem(long id, Map<String, Object> results) {
        parent.addCommand(new CompleteWorkItemCommand(id, results));
        return this;
    }

    @Override
    public WorkItemManagerFluent abortWorkItem(long id) {
        parent.addCommand(new AbortWorkItemCommand(id));
        return this;
    }

    @Override
    public WorkItemManagerFluent registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
        parent.addCommand(new RegisterWorkItemHandlerCommand(workItemName, handler));
        return this;
    }

    @Override
    public KieSessionFluent<KieSessionSimulationFluent> getKieSession() {
        return parent;
    }
}
