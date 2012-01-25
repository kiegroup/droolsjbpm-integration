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
import org.drools.command.ContextManager;
import org.drools.command.GetVariableCommand;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.knowledge.KnowledgeBaseSimFluent;
import org.drools.fluent.session.StatefulKnowledgeSessionSimFluent;
import org.drools.fluent.session.impl.DefaultStatefulKnowledgeSessionSimFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationStepFluent;
import org.drools.fluent.simulation.SimulationStepFluent;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class DefaultKnowledgeBaseSimFluent extends AbstractFluentTest<KnowledgeBaseSimFluent>
        implements KnowledgeBaseSimFluent {

    private DefaultSimulationStepFluent step;
    
    public DefaultKnowledgeBaseSimFluent(InternalSimulation sim,
                                         DefaultSimulationStepFluent step) {
        super();
        setSim( sim );
        this.step = step;
    }
    
    public KnowledgeBaseSimFluent addKnowledgePackages() {
        step.addCommand(  new KnowledgeBaseAddKnowledgePackagesCommand() );
        return this;
    }    
    
    
    public KnowledgeBaseSimFluent addKnowledgePackages(Resource resource,
                                                            ResourceType type) {
        step.addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        return this;
    }

    public KnowledgeBaseSimFluent addKnowledgePackages(Resource resource,
                                                            ResourceType type,
                                                            ResourceConfiguration configuration) {
        step.addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );
        
        return this;
    }

    public SimulationStepFluent end(String context, String name) {
        step.addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        step.addCommand( new SetVariableCommandFromLastReturn( context, name ) );
        return step;
    }
    
    public SimulationStepFluent end(String name) {
        step.addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        step.addCommand( new SetVariableCommandFromLastReturn( name ) );
        return step;
    }

    public SimulationStepFluent end() {
        return step;
    }

    public StatefulKnowledgeSessionSimFluent newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        step.addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        step.addCommand( new SetVariableCommandFromLastReturn( ContextManager.ROOT, StatefulKnowledgeSession.class.getName() ));

        return new DefaultStatefulKnowledgeSessionSimFluent(getSim(), step);
    }

}
