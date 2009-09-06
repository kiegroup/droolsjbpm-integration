package org.drools.simulation.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.drools.StatefulSession;
import org.drools.command.Command;
import org.drools.command.Context;
import org.drools.command.ContextManager;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.reteoo.ReteooStatefulSession;
import org.drools.reteoo.ReteooWorkingMemory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.simulation.Path;
import org.drools.simulation.Simulation;
import org.drools.simulation.Step;
import org.drools.time.SessionPseudoClock;

public class Simulator
    implements
    ContextManager {

    private PriorityQueue<Step>  queue;
    private SimulationImpl       simulation;
    //    private SessionPseudoClock  clock;
    private long                 startTime;

    private Context              root;
    private Map<String, Context> contexts;

    private String               ROOT = "ROOT";
    
    private Set<StatefulKnowledgeSession> ksessions;
    
    private CommandExecutionHandler executionHandler = new DefaultCommandExecutionHandler();

    public Simulator(Simulation simulation,
              //SessionPseudoClock clock,
              long startTime) {
        //        this.clock = clock;
        this.ksessions = new HashSet<StatefulKnowledgeSession>();
        
        this.startTime = startTime;
        this.simulation = (SimulationImpl) simulation;
        this.root = new ContextImpl( ROOT,
                                     this );

        this.contexts = new HashMap<String, Context>();
        this.contexts.put( ROOT,
                           this.root );

        Map<String, Path> paths = this.simulation.getPaths();

        // calculate capacity
        int capacity = 0;
        for ( Path path : paths.values() ) {
            this.contexts.put( path.getName(),
                               new ContextImpl( path.getName(),
                                                this,
                                                root ) );

            capacity += path.getSteps().size();
        }

        if ( capacity == 0 ) {
            return;
        }

        this.queue = new PriorityQueue( capacity,
                                        new Comparator<Step>() {
                                            public int compare(Step s1,
                                                               Step s2) {
                                                return (int) (s1.getTemporalDistance() - s2.getTemporalDistance());
                                            }
                                        } );

        for ( Path path : paths.values() ) {
            for ( Step step : path.getSteps() )
                this.queue.add( step );
        }
    }

    public void run() {
        Step step;
        while ( (step = executeNextStep()) != null ) {

        }
    }    

    public Step executeNextStep() {
        if ( this.queue.isEmpty() ) {
            return null;
        }
        StepImpl step = (StepImpl) this.queue.remove();
        PathImpl path = (PathImpl) step.getPath();

        Context pathContext = this.contexts.get( path.getName() );
        
        // increment the clock for all the registered ksessions
        for ( StatefulKnowledgeSession ksession : this.ksessions ) {
          SessionPseudoClock clock = (SessionPseudoClock) ksession.getSessionClock();
  
          long newTime = this.startTime + step.getTemporalDistance();
          long currentTime = clock.getCurrentTime();
          clock.advanceTime( (currentTime + (newTime - currentTime)),
                             TimeUnit.MICROSECONDS );            
        }
        
        for ( Command cmd : step.getCommands() ) {
            if ( cmd instanceof KnowledgeContextResolveFromContextCommand) {
                if ( ((KnowledgeContextResolveFromContextCommand)cmd).getCommand() instanceof NewStatefulKnowledgeSessionCommand ) {
                    // instantiate the ksession, set it's clock and register it
                    StatefulKnowledgeSession ksession = ( StatefulKnowledgeSession ) executionHandler.execute( (GenericCommand) cmd, pathContext );
                    if ( ksession != null ) {
                        SessionPseudoClock clock = (SessionPseudoClock) ksession.getSessionClock();
                        if ( clock.getCurrentTime() == 0 ) {
                            clock.advanceTime( startTime,
                                               TimeUnit.MILLISECONDS );
                        }
                        this.ksessions.add( ksession );
                    }
                } else if ( cmd instanceof GenericCommand) {
                    executionHandler.execute( (GenericCommand) cmd, pathContext );
                }
            }  else if ( cmd instanceof GenericCommand) {
                executionHandler.execute( (GenericCommand) cmd, pathContext );
            }
        }

        return step;
    }
    
    public void setCommandExecutionHandler(CommandExecutionHandler executionHandler) {
        this.executionHandler = executionHandler;
    }

    public Context getContext(String identifier) {
        return this.contexts.get( identifier );
    }
    
    public Simulation getSimulation() {
        return this.simulation;
    }
    
    public static interface CommandExecutionHandler  {
        public Object execute(GenericCommand command, Context context);
    }
    
    public static class DefaultCommandExecutionHandler implements CommandExecutionHandler  {
        public Object execute(GenericCommand command, Context context) {
            return command.execute( context );
        }
    }    


//    public static interface CommandExecutorService<T> {
//        T execute(Command command);
//    }
//    
//    public static class SimulatorCommandExecutorService<T> implements CommandExecutorService {
//        Map map = new HashMap() {
//            {
//               put( KnowledgeBuilderAddCommand.class, null); 
//            }
//        };
//        
//        public  T execute(Command command) {
//            return null;
//        }
//    }
//    
//    public static interface CommandContextAdapter {
//        Context getContext();
//    }
//    
//    public static class KnowledgeBuilderCommandContextAdapter implements CommandContextAdapter {
//
//        public Context getContext() {
//            return new KnowledgeBuilderCommandContext();
//        }
//        
//    }
    
    //    public void runUntil(Step step) {
    //        
    //    }
    //    
    //    public void runForTemporalDistance(long distance) {
    //        
    //    }
}
