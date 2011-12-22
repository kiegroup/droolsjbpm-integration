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

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.ContextManager;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.assertion.AssertEquals;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.command.builder.NewKnowledgeBuilderCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.Simulation;
import org.drools.simulation.SimulationStep;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitSimulationRunner.class)
public class SimulationTest {

    @Test
    public Simulation testOnePathNoDSL() {

        Simulation simulation = new SimulationImpl();

        String str = "";
        str += "package org.drools \n";
        str += "import " + Person.class.getName() + ";\n"; 
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    $p : Person() \n";
        str += "then \n";
        str += "    list.add( $p ); \n";
        str += "end \n";

        PathImpl path = new PathImpl( simulation,
                                      "path1" );

        List<SimulationStep> steps = new ArrayList<SimulationStep>();

        List<Command> cmds = new ArrayList<Command>();

        cmds.add( new NewKnowledgeBuilderCommand( null ) );
        cmds.add( new SetVariableCommandFromLastReturn( "path1",
                                          KnowledgeBuilder.class.getName() ) );

        cmds.add( new KnowledgeBuilderAddCommand( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                                  ResourceType.DRL,
                                                  null ) );

        cmds.add( new NewKnowledgeBaseCommand( null ) );
        cmds.add( new SetVariableCommandFromLastReturn( "path1",
                                          KnowledgeBase.class.getName() ) );

        cmds.add( new KnowledgeBaseAddKnowledgePackagesCommand() );

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );

        cmds.add( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        cmds.add( new SetVariableCommandFromLastReturn( "path1",
                                          StatefulKnowledgeSession.class.getName() ) );

        List list = new ArrayList();

        cmds.add( new SetGlobalCommand( "list",
                                        list ) );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 0 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "darth",
                                                       97 ) ) );
        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 2000 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "yoda",
                                                       98 ) ) );

        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 4000 ) );

        cmds = new ArrayList<Command>();

        cmds.add( new AssertEquals( "Check List size",
                                    2,
                                    new GetGlobalCommand( "list" ),
                                    "size()" ) );
        
        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "darth",
                                                97 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 0 )" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "yoda",
                                                98 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 1 )" ) );

        steps.add( new SimulationStepImpl( path,
                                 new TestGroupCommand( "test1",
                                                       cmds ),
                                 5000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path1",
                                   path );

        return simulation;
    }

    @Test
    public Simulation testTwoPathsNoDSL() {

        Simulation simulation = new SimulationImpl();

        String str = "";
        str += "package org.drools \n";
        str += "import " + Person.class.getName() + ";\n";     
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    $p : Person() \n";
        str += "then \n";
        str += "    list.add( $p ); \n";
        str += "end \n";

        PathImpl path = new PathImpl( simulation,
                                      "path1" );

        List<SimulationStep> steps = new ArrayList<SimulationStep>();

        List<Command> cmds = new ArrayList<Command>();

        cmds.add( new NewKnowledgeBuilderCommand( null ) );
        cmds.add( new SetVariableCommandFromLastReturn( ContextManager.ROOT,
                                          KnowledgeBuilder.class.getName() ) );

        cmds.add( new KnowledgeBuilderAddCommand( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                                  ResourceType.DRL,
                                                  null ) );

        cmds.add( new NewKnowledgeBaseCommand( null ) );

        cmds.add( new SetVariableCommandFromLastReturn( ContextManager.ROOT,
                                          KnowledgeBase.class.getName() ) );

        cmds.add( new KnowledgeBaseAddKnowledgePackagesCommand() );

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );

        cmds.add( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );
        cmds.add( new SetVariableCommandFromLastReturn( ContextManager.ROOT,
                                          StatefulKnowledgeSession.class.getName() ) );

        List list = new ArrayList();

        cmds.add( new SetGlobalCommand( "list",
                                        list ) );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 0 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "darth",
                                                       97 ) ) );
        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 2000 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "yoda",
                                                       98 ) ) );
        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 4000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path1",
                                   path );

        path = new PathImpl( simulation,
                             "path2" );

        steps = new ArrayList<SimulationStep>();

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "bobba",
                                                       77 ) ) );
        cmds.add( new FireAllRulesCommand() );
        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 1500 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "luke",
                                                       30 ) ) );
        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 2200 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new InsertObjectCommand( new Person( "ben",
                                                       150 ) ) );
        cmds.add( new FireAllRulesCommand() );

        steps.add( new SimulationStepImpl( path,
                                 cmds,
                                 4500 ) );

        cmds = new ArrayList<Command>();

        cmds.add( new AssertEquals( "Check List size",
                                    5,
                                    new GetGlobalCommand( "list" ),
                                    "size()" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "bobba",
                                                77 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 0 )" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "darth",
                                                97 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 1 )" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "luke",
                                                30 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 2 )" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "yoda",
                                                98 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 3 )" ) );

        cmds.add( new AssertEquals( "Check Person",
                                    new Person( "ben",
                                                150 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 4 )" ) );

        steps.add( new SimulationStepImpl( path,
                                 new TestGroupCommand( "test2",
                                                       cmds ),
                                 5000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path2",
                                   path );

        return simulation;
    }
}
