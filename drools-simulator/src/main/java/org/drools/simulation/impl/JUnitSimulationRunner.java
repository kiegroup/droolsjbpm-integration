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

package org.drools.simulation.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.command.Command;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.simulation.SimulationPath;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.Simulator.CommandExecutionHandler;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnitSimulationRunner extends Runner {

    private Description              descr;
    private Map<String, Description> testGroups  = new HashMap<String, Description>();

    private List<Simulation>         simulations = new ArrayList<Simulation>();

    public JUnitSimulationRunner(Class cls) {
        this.descr = Description.createSuiteDescription( cls );

        Object object = null;
        try {
            object = cls.newInstance();
        } catch ( Exception e ) {

        }

        List<Method> methods = new ArrayList<Method>();
        for ( Method method : cls.getDeclaredMethods() ) {
            if ( method.getReturnType().equals( Simulation.class ) && method.getAnnotation( Test.class ) != null ) {
                methods.add( method );
            }
        }

        try {
            for ( Method method : methods ) {

                Simulation simulation = (Simulation) method.invoke( object,
                                                                    null );

                for ( SimulationPath path : simulation.getPaths().values() ) {
                    for ( SimulationStep step : path.getSteps() ) {
                        for ( Command command : step.getCommands() ) {
                            if ( command instanceof TestGroupCommand ) {
                                String testName = ((TestGroupCommand) command).getName();
                                Description testGroupDescr = Description.createTestDescription( cls,
                                                                                                testName );
                                this.descr.addChild( testGroupDescr );
                                this.testGroups.put( testName,
                                                     testGroupDescr );
                            }
                        }
                    }
                }

                simulations.add( simulation );
            }
        } catch ( Exception e ) {
        }

        //        
        //        System.out.println( "class:" + cls );
        //        this.descr = Description.createSuiteDescription( cls );
        //        this.descr1 = Description.createTestDescription( cls, "test1" );
        //        this.descr2 = Description.createTestDescription( cls, "test2" );
        //        
        //        this.descr.addChild( descr1 );
        //        this.descr.addChild( descr2 );

    }

    @Override
    public Description getDescription() {
        return this.descr;
    }

    @Override
    public void run(RunNotifier notifier) {
        JunitCommandExecutionHandler executionHandler = new JunitCommandExecutionHandler( notifier,
                                                                                          this.testGroups );
        for ( Simulation simulation : simulations ) {
            Simulator simulator = new Simulator( simulation,
                                                 System.currentTimeMillis() );
            simulator.setCommandExecutionHandler( executionHandler );
            simulator.run();
        }

    }

    public static class JunitCommandExecutionHandler
            implements CommandExecutionHandler {

        private RunNotifier              notifier;
        private Map<String, Description> testGroups;

        public JunitCommandExecutionHandler(RunNotifier notifier,
                                            Map<String, Description> testGroups) {
            super();
            this.notifier = notifier;
            this.testGroups = testGroups;
        }

        public Object execute(GenericCommand command,
                              Context context) {
            if ( command instanceof TestGroupCommand ) {
                TestGroupCommand testGroupCmd = (TestGroupCommand) command;
                Description descr = this.testGroups.get( testGroupCmd.getName() );
                this.notifier.fireTestStarted( descr );
                try {
                    command.execute( context );
                    this.notifier.fireTestFinished( descr );
                    return null;
                } catch ( Exception e ) {
                    this.notifier.fireTestFailure( new Failure( descr,
                                                                e ) );
                    return null;
                }
            } else {
                return command.execute( context );
            }
        }

    }

}
