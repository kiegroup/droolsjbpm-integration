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

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.GetVariableCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.knowledge.KnowledgeBuilderFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationStepFluent;
import org.drools.fluent.simulation.SimulationStepFluent;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;

public class DefaultKnowledgeBuilderFluent extends AbstractFluentTest<KnowledgeBuilderFluent>
        implements KnowledgeBuilderFluent {
    
    private DefaultSimulationStepFluent step;
    
    public DefaultKnowledgeBuilderFluent(InternalSimulation sim,
                                         DefaultSimulationStepFluent step) {
        super();
        setSim( sim );
        this.step = step;
    }

    public KnowledgeBuilderFluent add(Resource resource,
                                              ResourceType type) {
        step.addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        
        return this;
    }

    public KnowledgeBuilderFluent add(Resource resource,
                                              ResourceType type,
                                              ResourceConfiguration configuration) {
        step.addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );
        
        return this;
    }

    public SimulationStepFluent end(String context, String name) {
        step.addCommand( new GetVariableCommand( KnowledgeBuilder.class.getName() ) );
        step.addCommand( new SetVariableCommandFromLastReturn( context, name ) );
        return step;
    }
    
    public SimulationStepFluent end(String name) {
        step.addCommand( new GetVariableCommand( KnowledgeBuilder.class.getName() ) );
        step.addCommand( new SetVariableCommandFromLastReturn( name ) );
        return step;
    }

    public SimulationStepFluent end() {
        return step;
    }

}
