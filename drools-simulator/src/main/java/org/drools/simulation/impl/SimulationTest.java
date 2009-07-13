package org.drools.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.Person;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.io.ResourceFactory;
import org.drools.process.command.AssertEquals;
import org.drools.process.command.FireAllRulesCommand;
import org.drools.process.command.GetGlobalCommand;
import org.drools.process.command.InsertObjectCommand;
import org.drools.process.command.SetGlobalCommand;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.simulation.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.simulation.KnowledgeContextResolveFromContextCommand;
import org.drools.simulation.NewStatefulKnowledgeSessionCommand;
import org.drools.simulation.SetVariableCommand;
import org.drools.simulation.KnowledgeBuilderAddCommand;
import org.drools.simulation.NewKnowledgeBaseCommand;
import org.drools.simulation.NewKnowledgeBuilderCommand;
import org.drools.simulation.Simulation;
import org.drools.simulation.Statement;
import org.drools.simulation.Step;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.TestGenerator;

import junit.framework.TestCase;

import static junit.framework.Assert.*;

@RunWith(JUnitSimulationRunner.class)
public class SimulationTest {

    @Test
    public Simulation testOnePathNoDSL() {

        Simulation simulation = new SimulationImpl();

        String str = "";
        str += "package org.drools \n";
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

        List<Step> steps = new ArrayList<Step>();

        List<Command> cmds = new ArrayList<Command>();

        cmds.add( new SetVariableCommand( "path1",
                                          "kbuilder",
                                          new NewKnowledgeBuilderCommand( null ) ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderAddCommand( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                                                                                 ResourceType.DRL,
                                                                                                 null ),
                                                                 "kbuilder",
                                                                 null,
                                                                 null ) );

        cmds.add( new SetVariableCommand( "path1",
                                          "kbase",
                                          new NewKnowledgeBaseCommand( null ) ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new KnowledgeBaseAddKnowledgePackagesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 null ) );

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );

        cmds.add( new SetVariableCommand( "path1",
                                          "ksession",
                                          new KnowledgeContextResolveFromContextCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ),
                                                                                         "kbuilder",
                                                                                         "kbase",
                                                                                         null ) ) );

        List list = new ArrayList();

        cmds.add( new KnowledgeContextResolveFromContextCommand( new SetGlobalCommand( "list",
                                                                                       list ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        steps.add( new StepImpl( path,
                                 cmds,
                                 0 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "darth",
                                                                                                      97 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 2000 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "yoda",
                                                                                                      98 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 4000 ) );

        cmds = new ArrayList<Command>();

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check List size",
                                                                                   2,
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "size()" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "darth",
                                                                                               97 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 0 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "yoda",
                                                                                               98 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 1 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        steps.add( new StepImpl( path,
                                 new TestGroupCommand( "test1",
                                                       cmds ),
                                 5000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path1",
                                   path );

        return simulation;

        //        Simulator simulator = new Simulator( simulation,
        //                                             System.currentTimeMillis() );
        //
        //        simulator.run();
    }

    @Test
    public Simulation testTwoPathsNoDSL() {

        Simulation simulation = new SimulationImpl();

        String str = "";
        str += "package org.drools \n";
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

        List<Step> steps = new ArrayList<Step>();

        List<Command> cmds = new ArrayList<Command>();

        cmds.add( new SetVariableCommand( "ROOT",
                                          "kbuilder",
                                          new NewKnowledgeBuilderCommand( null ) ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new KnowledgeBuilderAddCommand( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                                                                                 ResourceType.DRL,
                                                                                                 null ),
                                                                 "kbuilder",
                                                                 null,
                                                                 null ) );

        cmds.add( new SetVariableCommand( "ROOT",
                                          "kbase",
                                          new NewKnowledgeBaseCommand( null ) ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new KnowledgeBaseAddKnowledgePackagesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 null ) );

        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );

        cmds.add( new SetVariableCommand( "ROOT",
                                          "ksession",
                                          new KnowledgeContextResolveFromContextCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ),
                                                                                         "kbuilder",
                                                                                         "kbase",
                                                                                         null ) ) );

        List list = new ArrayList();

        cmds.add( new KnowledgeContextResolveFromContextCommand( new SetGlobalCommand( "list",
                                                                                       list ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        steps.add( new StepImpl( path,
                                 cmds,
                                 0 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "darth",
                                                                                                      97 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 2000 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "yoda",
                                                                                                      98 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 4000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path1",
                                   path );

        path = new PathImpl( simulation,
                             "path2" );

        steps = new ArrayList<Step>();

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "bobba",
                                                                                                      77 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 1500 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "luke",
                                                                                                      30 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 2200 ) );

        cmds = new ArrayList<Command>();
        cmds.add( new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( new Person( "ben",
                                                                                                      150 ) ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        cmds.add( new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand(),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );
        steps.add( new StepImpl( path,
                                 cmds,
                                 4500 ) );

        cmds = new ArrayList<Command>();

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check List size",
                                                                                   5,
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "size()" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "bobba",
                                                                                               77 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 0 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "darth",
                                                                                               97 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 1 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "luke",
                                                                                               30 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 2 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "yoda",
                                                                                               98 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 3 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        cmds.add( new KnowledgeContextResolveFromContextCommand( new AssertEquals( "Check Person",
                                                                                   new Person( "ben",
                                                                                               150 ),
                                                                                   new GetGlobalCommand( "list" ),
                                                                                   "get( 4 )" ),
                                                                 "kbuilder",
                                                                 "kbase",
                                                                 "ksession" ) );

        steps.add( new StepImpl( path,
                                 new TestGroupCommand( "test2",
                                                       cmds ),
                                 5000 ) );

        path.setSteps( steps );

        simulation.getPaths().put( "path2",
                                   path );

        return simulation;

    }
}
