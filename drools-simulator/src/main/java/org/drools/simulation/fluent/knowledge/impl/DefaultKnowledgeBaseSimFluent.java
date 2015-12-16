/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.drools.simulation.fluent.knowledge.impl;

import org.drools.core.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.core.command.SetVariableCommandFromLastReturn;
import org.drools.core.command.builder.KnowledgeBuilderAddCommand;
import org.drools.simulation.fluent.knowledge.KnowledgeBaseSimFluent;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.test.impl.AbstractTestableFluent;
import org.kie.api.command.Command;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

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

    public SimulationFluent end(String context, String name) {
        addCommand(new SetVariableCommandFromLastReturn(context, name));
        return simulationFluent;
    }

    public SimulationFluent end(String name) {
        addCommand(new SetVariableCommandFromLastReturn(name));
        return simulationFluent;
    }

    public SimulationFluent end() {
        return simulationFluent;
    }

}
