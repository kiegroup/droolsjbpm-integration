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
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.World;
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

        SimulationPathImpl path = new SimulationPathImpl( simulation,
                                      "path1" );

        List<SimulationStep> steps = new ArrayList<SimulationStep>();

        SimulationStepImpl step1 = new SimulationStepImpl(path, 0);
        List<Command> cmds1 = step1.getCommands();

        cmds1.add(new NewKnowledgeBuilderCommand( ) );
        cmds1.add(new SetVariableCommandFromLastReturn("path1",
                KnowledgeBuilder.class.getName()));

        cmds1.add(new KnowledgeBuilderAddCommand(ResourceFactory.newByteArrayResource(str.getBytes()),
                ResourceType.DRL,
                null));

        cmds1.add(new NewKnowledgeBaseCommand(null));
        cmds1.add(new SetVariableCommandFromLastReturn("path1",
                KnowledgeBase.class.getName()));

        cmds1.add(new KnowledgeBaseAddKnowledgePackagesCommand());

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption(ClockTypeOption.get("pseudo"));

        cmds1.add(new NewStatefulKnowledgeSessionCommand(ksessionConf));
        cmds1.add( new SetVariableCommandFromLastReturn( "path1",
                                          StatefulKnowledgeSession.class.getName() ) );

        List list = new ArrayList();

        cmds1.add(new SetGlobalCommand("list",
                list));

        steps.add(step1);


        SimulationStepImpl step2 = new SimulationStepImpl(path, 2000);
        List<Command> cmds2 = step2.getCommands();
        cmds2.add(new InsertObjectCommand(new Person("darth",
                97)));
        cmds2.add(new FireAllRulesCommand());
        steps.add( step2 );


        SimulationStepImpl step3 = new SimulationStepImpl(path, 4000);
        List<Command> cmds3 = step3.getCommands();
        cmds3.add( new InsertObjectCommand( new Person( "yoda",
                                                       98 ) ) );
        cmds3.add(new FireAllRulesCommand());
        steps.add( step3 );


        SimulationStepImpl step4 = new SimulationStepImpl(path, 5000);
        List<Command> cmds4 = new ArrayList<Command>();

        cmds4.add( new AssertEquals( "Check List size",
                                    2,
                                    new GetGlobalCommand( "list" ),
                                    "size()" ) );
        
        cmds4.add( new AssertEquals( "Check Person",
                                    new Person( "darth",
                                                97 ),
                                    new GetGlobalCommand( "list" ),
                                    "get( 0 )" ) );

        cmds4.add(new AssertEquals("Check Person",
                new Person("yoda",
                        98),
                new GetGlobalCommand("list"),
                "get( 1 )"));

        step4.getCommands().add(new TestGroupCommand( "test1", cmds4));
        steps.add( step4 );


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

        SimulationPathImpl path1 = new SimulationPathImpl( simulation,
                                      "path1" );

        List<SimulationStep> steps1 = new ArrayList<SimulationStep>();

        SimulationStepImpl step11 = new SimulationStepImpl(path1,
                0);
        List<Command> cmds11 = step11.getCommands();

        cmds11.add(new NewKnowledgeBuilderCommand( ) );
        cmds11.add(new SetVariableCommandFromLastReturn(World.ROOT,
                KnowledgeBuilder.class.getName()));

        cmds11.add(new KnowledgeBuilderAddCommand(ResourceFactory.newByteArrayResource(str.getBytes()),
                ResourceType.DRL,
                null));

        cmds11.add(new NewKnowledgeBaseCommand(null));

        cmds11.add(new SetVariableCommandFromLastReturn(World.ROOT,
                KnowledgeBase.class.getName()));

        cmds11.add(new KnowledgeBaseAddKnowledgePackagesCommand());

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption(ClockTypeOption.get("pseudo"));

        cmds11.add(new NewStatefulKnowledgeSessionCommand(ksessionConf));
        cmds11.add(new SetVariableCommandFromLastReturn(World.ROOT,
                StatefulKnowledgeSession.class.getName()));

        List list = new ArrayList();
        cmds11.add(new SetGlobalCommand("list",
                list));

        steps1.add(step11);


        SimulationStepImpl step12 = new SimulationStepImpl(path1,
                2000);
        List<Command> cmds12 = step12.getCommands();
        cmds12.add( new InsertObjectCommand( new Person( "darth",
                                                       97 ) ) );
        cmds12.add( new FireAllRulesCommand() );
        steps1.add(step12);


        SimulationStepImpl step13 = new SimulationStepImpl(path1,
                4000);
        List<Command> cmds13 = step13.getCommands();
        cmds13.add( new InsertObjectCommand( new Person( "yoda",
                                                       98 ) ) );
        cmds13.add(new FireAllRulesCommand());

        steps1.add(step13);


        path1.setSteps(steps1);
        simulation.getPaths().put( "path1",
                                   path1 );

        SimulationPathImpl path2 = new SimulationPathImpl( simulation,
                             "path2" );

        List<SimulationStep> steps2 = new ArrayList<SimulationStep>();


        SimulationStepImpl step21 = new SimulationStepImpl(path2,
                1500);
        List<Command> cmds21 = step21.getCommands();
        cmds21.add( new InsertObjectCommand( new Person( "bobba",
                                                       77 ) ) );
        cmds21.add(new FireAllRulesCommand());
        steps2.add(step21);


        SimulationStepImpl step22 = new SimulationStepImpl(path2,
                2200);
        List<Command> cmds22 = step22.getCommands();
        cmds22.add(new InsertObjectCommand(new Person("luke",
                30)));
        cmds22.add(new FireAllRulesCommand());
        steps2.add(step22);


        SimulationStepImpl step23 = new SimulationStepImpl(path2,
                4500);
        List<Command> cmds23 = step23.getCommands();
        cmds23.add(new InsertObjectCommand(new Person("ben",
                150)));
        cmds23.add(new FireAllRulesCommand());

        steps2.add(step23);


        SimulationStepImpl step24 = new SimulationStepImpl(path2,
                5000);
        List<Command> cmds24 = new ArrayList<Command>();
        cmds24.add(new AssertEquals("Check List size",
                5,
                new GetGlobalCommand("list"),
                "size()"));

        cmds24.add(new AssertEquals("Check Person",
                new Person("bobba",
                        77),
                new GetGlobalCommand("list"),
                "get( 0 )" ) );

        cmds24.add(new AssertEquals("Check Person",
                new Person("darth",
                        97),
                new GetGlobalCommand("list"),
                "get( 1 )"));

        cmds24.add(new AssertEquals("Check Person",
                new Person("luke",
                        30),
                new GetGlobalCommand("list"),
                "get( 2 )"));

        cmds24.add(new AssertEquals("Check Person",
                new Person("yoda",
                        98),
                new GetGlobalCommand("list"),
                "get( 3 )"));

        cmds24.add(new AssertEquals("Check Person",
                new Person("ben",
                        150),
                new GetGlobalCommand("list"),
                "get( 4 )"));
        step24.getCommands().add(new TestGroupCommand("test2", cmds24));
        steps2.add(step24);


        path2.setSteps(steps2);
        simulation.getPaths().put( "path2",
                                   path2 );

        return simulation;
    }

}
