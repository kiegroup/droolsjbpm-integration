/*
 * Copyright 2011 JBoss Inc
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

package org.drools.fluent.knowledge.impl;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.*;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.knowledge.KnowledgeBaseSimFluent;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.session.impl.DefaultStatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.test.impl.AbstractTestableFluent;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class DefaultKnowledgeBaseSimFluent extends AbstractTestableFluent<KnowledgeBaseSimFluent>
        implements KnowledgeBaseSimFluent {

    private SimulationFluent simulationFluent;
    
    public DefaultKnowledgeBaseSimFluent(SimulationFluent simulationFluent) {
        this.simulationFluent = simulationFluent;
    }

    protected KnowledgeBaseSimFluent addCommand(Command command) {
        simulationFluent.addCommand(command);
        return this;
    }
    
    public KnowledgeBaseSimFluent addKnowledgePackages() {
        addCommand(new KnowledgeBaseAddKnowledgePackagesCommand());
        return this;
    }
    
    public KnowledgeBaseSimFluent addKnowledgePackages(Resource resource,
                                                            ResourceType type) {
        addCommand(new KnowledgeBuilderAddCommand(resource,
                type,
                null));
        return this;
    }

    public KnowledgeBaseSimFluent addKnowledgePackages(Resource resource,
                                                            ResourceType type,
                                                            ResourceConfiguration configuration) {
        addCommand(new KnowledgeBuilderAddCommand(resource,
                type,
                configuration));
        
        return this;
    }

    public String getActiveKnowledgeBaseId() {
        return simulationFluent.getActiveKnowledgeBaseId();
    }

    public SimulationFluent end(String context, String name) {
        addCommand(new GetVariableCommand(KnowledgeBase.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(context, name));
        return simulationFluent;
    }

    public SimulationFluent end(String name) {
        addCommand(new GetVariableCommand(KnowledgeBase.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(name));
        return simulationFluent;
    }

    public SimulationFluent end() {
        return simulationFluent;
    }

}
