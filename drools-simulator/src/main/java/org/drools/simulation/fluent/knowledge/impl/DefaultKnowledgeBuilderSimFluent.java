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

package org.drools.simulation.fluent.knowledge.impl;

import org.drools.core.command.GetVariableCommand;
import org.drools.core.command.SetVariableCommandFromLastReturn;
import org.drools.core.command.builder.KnowledgeBuilderAddCommand;
import org.drools.simulation.fluent.knowledge.KnowledgeBuilderSimFluent;
import org.drools.simulation.fluent.simulation.SimulationFluent;
import org.drools.simulation.fluent.test.impl.AbstractTestableFluent;
import org.kie.builder.KnowledgeBuilder;
import org.kie.command.Command;
import org.kie.io.Resource;
import org.kie.io.ResourceConfiguration;
import org.kie.io.ResourceType;

public class DefaultKnowledgeBuilderSimFluent extends AbstractTestableFluent<KnowledgeBuilderSimFluent>
        implements KnowledgeBuilderSimFluent {
    
    private SimulationFluent simulationFluent;
    
    public DefaultKnowledgeBuilderSimFluent(SimulationFluent simulationFluent) {
        super();
        this.simulationFluent = simulationFluent;
    }

    protected KnowledgeBuilderSimFluent addCommand(Command command) {
        simulationFluent.addCommand(command);
        return this;
    }

    public KnowledgeBuilderSimFluent add(Resource resource,
                                         ResourceType type) {
        addCommand(new KnowledgeBuilderAddCommand(resource,
                type,
                null));
        
        return this;
    }

    public KnowledgeBuilderSimFluent add(Resource resource,
                                         ResourceType type,
                                         ResourceConfiguration configuration) {
        addCommand(new KnowledgeBuilderAddCommand(resource,
                type,
                configuration));
        
        return this;
    }

//    public String getActiveKnowledgeBuilderId() {
//        return simulationFluent.getActiveKnowledgeBuilderId();
//    }

    public SimulationFluent end(String context, String name) {
        addCommand(new GetVariableCommand(KnowledgeBuilder.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(context, name));
        return simulationFluent;
    }
    
    public SimulationFluent end(String name) {
        addCommand(new GetVariableCommand(KnowledgeBuilder.class.getName()));
        addCommand(new SetVariableCommandFromLastReturn(name));
        return simulationFluent;
    }

    public SimulationFluent end() {
        return simulationFluent;
    }

}
