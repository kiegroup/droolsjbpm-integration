package org.drools.fluent.compact.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.command.Command;
import org.drools.command.ContextManager;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.fluent.VariableContext;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.fluent.test.impl.MapVariableContext;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;
import org.drools.simulation.Step;
import org.drools.simulation.impl.PathImpl;
import org.drools.simulation.impl.PrintVariableCommand;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.StepImpl;

public class FluentCompactSimulationImpl extends AbstractFluentTest<FluentCompactSimulation>
    implements
    FluentCompactSimulation, InternalSimulation {

    private Path       path;

    private List<Step> steps;

    private Step       step;

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
    
    public void addCommand(Command cmd) {
        cmds.add( cmd );
    }


    public void newStep(long distance) {
        cmds = new ArrayList<Command>();

        step = new StepImpl( path,
                             cmds,
                             distance );

        steps.add( step );
    }

    public <P> VariableContext<P> getVariableContext() {
        return vars;
    }

    public FluentCompactStatefulKnowledgeSession newStatefulKnowledgeSession() {
        steps = new ArrayList<Step>();         
        path = new PathImpl( sim,
                             "path" + pathCounter++ );        
        sim.getPaths().put( path.getName(), path );     
        ((PathImpl)path).setSteps( steps );        
        
        newStep( 0l );


        addCommand( new NewKnowledgeBaseCommand(null) );        
        addCommand( new SetVariableCommand( ContextManager.ROOT, KnowledgeBase.class.getName() )); 
        
        
        addCommand( new NewKnowledgeBuilderCommand( null, KnowledgeBase.class.getName() ) );
        addCommand( new SetVariableCommand( ContextManager.ROOT, KnowledgeBuilder.class.getName() ));
        
        
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );             
        addCommand( new SetVariableCommand( ContextManager.ROOT, StatefulKnowledgeSession.class.getName() ));        

        return new FluentCompactStatefulKnowledgeSessionImpl(this);
    } 
    
    public Simulation getSimulation() {
        return sim;
    }

    public Map<String, Path> getPaths() {
        return sim.getPaths();
    }

}
