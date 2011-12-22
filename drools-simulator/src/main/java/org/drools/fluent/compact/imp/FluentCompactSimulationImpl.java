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

package org.drools.fluent.compact.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Command;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.VariableContext;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.PathImpl;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.SimulationStepImpl;

public class FluentCompactSimulationImpl extends AbstractFluentTest<FluentCompactSimulation>
        implements FluentCompactSimulation, InternalSimulation {

    private Path       path;

    private List<SimulationStep> steps;

    private SimulationStep step;

    private List<Command>      cmds;
    
    private SimulationImpl sim;
    
    private VariableContext vars;
    
    // ensures different path name for each ksession creation
    private int pathCounter;
    
    public FluentCompactSimulationImpl() {
        super();
        setSim( this );
        
    	vars = new MapVariableContext();    	
        sim = new SimulationImpl();         
    }

    public FluentCompactStatefulKnowledgeSession newStatefulKnowledgeSession() {
        steps = new ArrayList<SimulationStep>();
        path = new PathImpl( sim,
                             "path" + pathCounter++ );        
        sim.getPaths().put( path.getName(), path );     
        ((PathImpl)path).setSteps( steps );        
        
        newStep( 0l );


        addCommand( new NewKnowledgeBaseCommand(null) );        
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBase.class.getName() )); 
        
        
        addCommand( new NewKnowledgeBuilderCommand( null, KnowledgeBase.class.getName() ) );
        addCommand( new SetVariableCommandFromLastReturn( KnowledgeBuilder.class.getName() ));
        
        
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );             
        addCommand( new SetVariableCommandFromLastReturn( StatefulKnowledgeSession.class.getName() ));        

        return new FluentCompactStatefulKnowledgeSessionImpl(this);
    }

    public void newStep(long distance) {
        cmds = new ArrayList<Command>();

        step = new SimulationStepImpl( path,
                             cmds,
                             distance );

        steps.add( step );
    }

    public void addCommand(Command cmd) {
        cmds.add( cmd );
    }

    public <P> VariableContext<P> getVariableContext() {
        return vars;
    }

    public Simulation getSimulation() {
        return sim;
    }

    public Map<String, Path> getPaths() {
        return sim.getPaths();
    }

}
